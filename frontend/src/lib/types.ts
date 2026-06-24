// Types mirroring the Spring Boot REST DTOs / domain entities.

export interface PublicCourse {
  id: number;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  categoryLabel: string | null;
  level: string | null;
  durationHours: number | null;
  priceIndividual: number | null;
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

export interface Course {
  id: number;
  code: string;
  name: string;
  description: string | null;
  category: string | null;
  level: string | null;
  durationHours: number | null;
  priceIndividual: number | null;
  active: boolean | null;
}

export interface Opportunity {
  id: number;
  clientId: number | null;
  title: string;
  description: string | null;
  estimatedValue: number | null;
  quotedValue: number | null;
  probabilityPercent: number | null;
  stage: string;
  estimatedParticipants: number | null;
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
  experienceLevel: string | null;
  fullName?: string;
}

export interface Auction {
  id: number;
  courseId: number | null;
  title: string;
  description: string | null;
  startingPrice: number;
  status: "OPEN" | "AWARDED" | "CANCELLED";
  closesAt: string | null;
  winnerCompanyId: number | null;
  winningAmount: number | null;
}

export interface Bid {
  id: number;
  auctionId: number;
  companyId: number;
  companyName: string | null;
  amount: number;
  createdAt: string | null;
}

export interface CourseRecommendation {
  courseId: number;
  courseName: string;
  reason: string;
  matchScore: number;
}

export interface DashboardStats {
  totalContacts: number;
  hotLeads: number;
  activeOpportunities: number;
  weightedPipelineValue: number;
  contactsByStatus: Record<string, number>;
  pipelineByStage: Record<string, number>;
}
