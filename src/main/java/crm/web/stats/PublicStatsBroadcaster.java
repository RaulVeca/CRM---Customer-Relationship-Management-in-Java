package crm.web.stats;

import crm.facade.CrmFacade;
import crm.web.dto.PublicStatsDto;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Pushes the public headline figures to subscribed browsers whenever they
 * change, so the landing page never shows a stale count.
 *
 * <p>The watch deliberately keys off the <em>computed figures</em> rather than
 * off {@link crm.observer.EventBus} events. Only some mutations publish an
 * event (a new enrollment does; saving a review or activating a course does
 * not), and nothing publishes when rows are edited straight in the database —
 * so an event-driven push would silently miss real changes. Recomputing and
 * comparing catches every source of change with one mechanism.</p>
 *
 * <p>The cost of that choice is a poll, so it is kept cheap: the watch only
 * runs while at least one browser is listening, and a tick that finds the
 * figures unchanged sends nothing.</p>
 */
@Component
public class PublicStatsBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(PublicStatsBroadcaster.class);

    /** How soon a change reaches an open page. Fast enough to read as live. */
    private static final long POLL_MS = 2_000;

    /**
     * How long the stream may stay silent before a comment is written purely to
     * prove the connection is still there. See {@link #tick()}.
     */
    private static final long HEARTBEAT_MS = 15_000;

    /**
     * Emitters are closed after this long; EventSource reconnects by itself, so
     * this just stops abandoned connections accumulating.
     */
    private static final long STREAM_TIMEOUT_MS = 30 * 60 * 1_000L;

    private final CrmFacade facade;
    private final List<SseEmitter> subscribers = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "public-stats-watch");
        t.setDaemon(true);
        return t;
    });

    /** Only ever read/written by the watch thread or under {@code this} lock. */
    private ScheduledFuture<?> watch;
    private volatile PublicStatsDto lastBroadcast;
    /** When the watch thread last wrote anything to subscribers. */
    private long lastContact;

    public PublicStatsBroadcaster(CrmFacade facade) {
        this.facade = facade;
    }

    /**
     * Registers a browser for updates. The current figures are sent immediately,
     * so a subscriber paints correct numbers without a separate request.
     *
     * <p>This snapshot goes to the new subscriber only, and deliberately leaves
     * {@link #lastBroadcast} alone — that field tracks what <em>every</em>
     * subscriber has seen. Setting it here would make the next tick consider the
     * value already delivered and skip the broadcast, stranding subscribers that
     * never received it. The cost is that the first subscriber may see one
     * duplicate event; identical values are harmless to apply twice.</p>
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        emitter.onCompletion(() -> subscribers.remove(emitter));
        emitter.onTimeout(() -> subscribers.remove(emitter));
        emitter.onError(e -> subscribers.remove(emitter));
        subscribers.add(emitter);

        PublicStatsDto current = readStats();
        if (current != null) {
            send(emitter, current);
        }
        startWatch();
        return emitter;
    }

    private synchronized void startWatch() {
        if (watch != null) return;
        watch = scheduler.scheduleWithFixedDelay(this::tick, POLL_MS, POLL_MS, TimeUnit.MILLISECONDS);
        logger.debug("Public stats watch started");
    }

    private synchronized void stopWatch() {
        if (watch == null) return;
        watch.cancel(false);
        watch = null;
        logger.debug("Public stats watch stopped - no subscribers");
    }

    private void tick() {
        // Nobody is listening: idle out rather than query the database forever.
        if (subscribers.isEmpty()) {
            stopWatch();
            return;
        }
        PublicStatsDto current = readStats();
        if (current == null) return;

        if (!current.equals(lastBroadcast)) {
            lastBroadcast = current;
            for (SseEmitter emitter : subscribers) {
                send(emitter, current);
            }
            lastContact = System.currentTimeMillis();
            return;
        }

        // Figures unchanged, so there is nothing to say — but staying silent
        // would keep a departed browser in the list forever: a dropped
        // connection only surfaces when something is written to it, and with no
        // writes this watch would poll the database on behalf of nobody. A
        // periodic comment forces that write (and keeps proxies from culling an
        // idle stream). EventSource ignores comments, so pages see nothing.
        if (System.currentTimeMillis() - lastContact >= HEARTBEAT_MS) {
            lastContact = System.currentTimeMillis();
            for (SseEmitter emitter : subscribers) {
                heartbeat(emitter);
            }
        }
    }

    /** Never lets a database hiccup kill the watch thread. */
    private PublicStatsDto readStats() {
        try {
            return PublicStatsDto.from(facade.getSiteStats());
        } catch (Exception e) {
            logger.warn("Could not read public stats: {}", e.getMessage());
            return null;
        }
    }

    private void send(SseEmitter emitter, PublicStatsDto stats) {
        try {
            emitter.send(SseEmitter.event().name("stats").data(stats));
        } catch (Exception e) {
            // A browser that navigated away is the normal case here, not a fault.
            subscribers.remove(emitter);
        }
    }

    /** A no-op write whose only purpose is to fail if the browser is gone. */
    private void heartbeat(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().comment("alive"));
        } catch (Exception e) {
            subscribers.remove(emitter);
        }
    }

    @PreDestroy
    void shutdown() {
        stopWatch();
        scheduler.shutdownNow();
        subscribers.forEach(SseEmitter::complete);
        subscribers.clear();
    }
}
