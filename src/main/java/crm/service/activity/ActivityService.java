package crm.service.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Activity;
import crm.observer.EventBus;
import crm.observer.events.ActivityCompletedEvent;
import crm.repository.ActivityRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    private static volatile ActivityService instance;

    private final ActivityRepository activityRepository;
    private final EventBus eventBus;

    private ActivityService() {
        this.activityRepository = ActivityRepository.getInstance();
        this.eventBus = EventBus.getInstance();
    }

    public static ActivityService getInstance() {
        if (instance == null) {
            synchronized (ActivityService.class) {
                if (instance == null) {
                    instance = new ActivityService();
                }
            }
        }
        return instance;
    }

    public Activity createActivity(Activity activity) {
        if (activity.getStatus() == null) activity.setStatus("SCHEDULED");
        if (activity.getPriority() == null) activity.setPriority("MEDIUM");

        Activity saved = activityRepository.save(activity);
        logger.info("Activity created: {} (type: {})", saved.getSubject(), saved.getActivityType());
        return saved;
    }

    public void completeActivity(Long activityId, String outcome, String nextSteps) {
        Activity activity = activityRepository.getById(activityId);
        activity.setStatus("COMPLETED");
        activity.setCompletedDate(LocalDateTime.now());
        activity.setOutcome(outcome);
        activity.setNextSteps(nextSteps);

        activityRepository.save(activity);
        eventBus.publish(new ActivityCompletedEvent(activity, "ActivityService"));

        logger.info("Activity completed: {}", activityId);
    }

    public List<Activity> getByContact(Long contactId) {
        return activityRepository.findByContactId(contactId);
    }

    public List<Activity> getByOpportunity(Long opportunityId) {
        return activityRepository.findByOpportunityId(opportunityId);
    }

    public List<Activity> getUpcomingForUser(Long userId, int days) {
        return activityRepository.findUpcomingForUser(userId, days);
    }
}
