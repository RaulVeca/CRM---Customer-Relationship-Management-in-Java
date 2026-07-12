// One-time access to a course's dedicated reviews page. The page at
// /courses/[id]/reviews does not exist as a normal, linkable route: it may only
// be reached by pressing "See Reviews" on that course's card, and it stops being
// reachable the moment the visitor leaves it (mirroring how the schedule window
// works — see lib/schedule.ts).
//
// The grant is kept in sessionStorage (per-tab, gone when the tab closes) and is
// cleared when the visitor navigates away from the reviews route — the leave is
// detected by the always-mounted PrimaryNav watching the pathname, which is
// strict-mode safe (a page-unmount cleanup would misfire under React's dev
// double-mount).

const KEY = "ctrReviewsTarget";

/** Fired on the window whenever the reviews-access grant changes. */
export const REVIEWS_EVENT = "ctr-reviews-change";

export interface ReviewsTarget {
  id: number;
  name: string;
}

/** Grant access to a course's reviews page — called when "See Reviews" is pressed. */
export function openReviews(target: ReviewsTarget): void {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(target));
    window.dispatchEvent(new Event(REVIEWS_EVENT));
  } catch {
    /* sessionStorage unavailable — grant simply won't persist */
  }
}

/** The course currently granted access, or null if none. */
export function getReviewsTarget(): ReviewsTarget | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = sessionStorage.getItem(KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as ReviewsTarget;
    return typeof parsed?.id === "number" ? parsed : null;
  } catch {
    return null;
  }
}

/** Revoke access — called when the visitor leaves the reviews route. */
export function closeReviews(): void {
  try {
    sessionStorage.removeItem(KEY);
    window.dispatchEvent(new Event(REVIEWS_EVENT));
  } catch {
    /* ignore storage errors */
  }
}
