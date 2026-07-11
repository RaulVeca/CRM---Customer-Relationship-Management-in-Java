// Types mirroring the Spring Boot REST DTOs / domain entities.

export type AuthRole = "USER" | "ADMIN";

/** Identity returned by /api/auth/login/* and kept in the browser session. */
export interface AuthSession {
  role: AuthRole;
  id: number;
  firstName: string | null;
  lastName: string | null;
  email: string;
  /**
   * Automatic price reduction for this account (0 = none). Only employee
   * accounts receive a discount; contacts and admins are always 0. Optional so
   * sessions stored before this field existed still parse (treated as 0).
   */
  discountRate?: number;
}

export interface PublicCourse {
  id: number;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  categoryLabel: string | null;
  level: string | null;
  durationHours: number | null;
  averageRating: number;
  reviewCount: number;
}

/** A course the logged-in contact has bought — for their "my courses" page. */
export interface MyPurchase {
  courseId: number;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  categoryLabel: string | null;
  level: string | null;
  durationHours: number | null;
  purchaseDate: string | null;
  rating: number | null;
}

/** One of the trainers a contact can book an online session with. */
export interface Trainer {
  id: number;
  firstName: string | null;
  lastName: string | null;
  fullName: string;
  email: string;
}

/** A booked hour range on a trainer's calendar, half-open: covers [start, end). */
export interface BookedInterval {
  start: number;
  end: number;
}

/** A trainer's availability for a single calendar day. */
export interface DayAvailability {
  date: string; // ISO yyyy-MM-dd
  working: boolean;
  full: boolean;
  booked: BookedInterval[];
}

/** One booked session the logged-in contact owns — for their "my sessions" window. */
export interface MySession {
  id: number;
  trainerId: number;
  trainerName: string;
  date: string; // ISO yyyy-MM-dd
  startHour: number;
  endHour: number;
}

/** Result of booking a session. */
export interface BookingResponse {
  id: number;
  trainerId: number;
  trainerName: string;
  date: string;
  startHour: number;
  endHour: number;
  message: string;
}

export interface CourseReview {
  author: string;
  rating: number;
  comment: string | null;
  date: string | null;
}

export interface CourseReviews {
  average: number;
  count: number;
  reviews: CourseReview[];
}

/** A single review across the whole catalog, tagged with its course. */
export interface PublicReview {
  courseId: number;
  courseCode: string;
  courseName: string;
  author: string;
  rating: number;
  comment: string | null;
  date: string | null;
}

export interface Demographics {
  totalContacts: number;
  byType: Record<string, number>;
  byExperience: Record<string, number>;
  byLeadSource: Record<string, number>;
  byCounty: Record<string, number>;
  byIndustry: Record<string, number>;
}

export interface Churn {
  leadChurnRate: number;
  lostLeads: number;
  enrolledLeads: number;
  enrollmentDropoutRate: number;
  droppedEnrollments: number;
  totalEnrollments: number;
  opportunityLossRate: number;
  lostOpportunities: number;
  wonOpportunities: number;
}

export interface CtrRow {
  courseId: number;
  courseName: string;
  impressions: number;
  clicks: number;
  ctr: number;
}

export interface Ctr {
  overallCtr: number;
  totalImpressions: number;
  totalClicks: number;
  courses: CtrRow[];
}

export interface Analytics {
  demographics: Demographics;
  churn: Churn;
  ctr: Ctr;
}

export interface Purchase {
  enrollmentId: number;
  studentName: string;
  studentEmail: string | null;
  courseName: string;
  courseCode: string | null;
  status: string | null;
  date: string | null;
  rating: number | null;
}

// A session-booking invoice generated automatically at each trainer booking.
export interface Invoice {
  id: number;
  invoiceNumber: string;
  sessionId: number | null;
  clientId: number | null;
  clientEmail: string | null;
  issueDate: string | null;
  hours: number;
  hourlyRate: number;
  subtotal: number;
  discountRate: number; // 0 or 0.6
  discountAmount: number;
  total: number;
  paidAmount: number;
  status: string;
  paymentDate: string | null;
  description: string | null;
}

export interface PublicCompany {
  id: number;
  companyName: string;
  industry: string | null;
  employeeCount: number | null;
}

export interface Category {
  name: string;
  label: string;
}

export interface Contact {
  id: number;
  contactType: "INDIVIDUAL" | "CORPORATE";
  firstName: string | null;
  lastName: string | null;
  companyName: string | null;
  industry: string | null;
  employeeCount: number | null;
  email: string;
  phone: string | null;
  leadSource: string | null;
  leadStatus: string | null;
  leadScore: number | null;
  experienceLevel: string | null;
}

/**
 * Self-service registration payload — the "cards" of an INDIVIDUAL contact.
 * Sent to POST /api/auth/register, which returns an {@link AuthSession}.
 */
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  birthDate: string; // ISO yyyy-MM-dd
  email: string;
  phone: string;
  addressStreet: string;
  addressCity: string;
  addressCounty: string;
  addressPostalCode: string;
  experienceLevel: string;
  learningGoal: string;
  leadSource: string;
  password: string;
  confirmPassword: string;
  gdprConsent: boolean;
  marketingConsent: boolean;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  level: string | null;
  durationHours: number | null;
  active: boolean | null;
}

export interface Option {
  name: string;
  label: string;
}

export interface Employee {
  id: number;
  companyId: number;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  jobTitle: string | null;
  workProfile: string | null;
  interestProfiles: string[];
  fullName?: string;
}

/** Summary returned by POST /api/employees/import. */
export interface EmployeeImportResult {
  imported: number;
  skipped: number;
  errors: { row: number; message: string }[];
}

export interface CourseRecommendation {
  courseId: number;
  courseName: string;
  reason: string;
  matchScore: number;
}
