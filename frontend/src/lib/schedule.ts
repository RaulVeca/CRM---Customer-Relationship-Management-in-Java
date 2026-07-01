// Visibility of the "Online sessions" booking window. The window does not exist
// until the contact presses "Schedule a session" next to a purchased course;
// it stays available until a session is booked successfully, then disappears for
// good until the button is pressed again. Kept in localStorage (mirroring the
// auth session) with a custom event so PrimaryNav and the schedule page react
// live.

const KEY = "ctrScheduleOpen";

/** Fired on the window whenever the schedule-window flag changes. */
export const SCHEDULE_EVENT = "ctr-schedule-change";

export function isScheduleOpen(): boolean {
  if (typeof window === "undefined") return false;
  try {
    return localStorage.getItem(KEY) === "1";
  } catch {
    return false;
  }
}

/** Reveal the window — called when "Schedule a session" is pressed. */
export function openSchedule(): void {
  try {
    localStorage.setItem(KEY, "1");
    window.dispatchEvent(new Event(SCHEDULE_EVENT));
  } catch {
    /* localStorage unavailable — flag simply won't persist */
  }
}

/** Hide the window — called after a session is booked successfully. */
export function closeSchedule(): void {
  try {
    localStorage.removeItem(KEY);
    window.dispatchEvent(new Event(SCHEDULE_EVENT));
  } catch {
    /* ignore storage errors */
  }
}
