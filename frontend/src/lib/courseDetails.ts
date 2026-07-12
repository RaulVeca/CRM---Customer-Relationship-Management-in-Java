// Builds an extensive, structured description for a course out of the fields the
// catalog already carries (name, category, level, duration, short description).
// The backend only stores a one-line summary, so the long-form copy shown when a
// card is expanded is generated here — tailored per category and per level, with
// a sensible generic fallback for any category not explicitly covered.

import type { PublicCourse } from "./types";

export interface CourseDetails {
  /** Two intro paragraphs setting up the course. */
  overview: string[];
  /** "What you'll learn" bullet points. */
  learn: string[];
  /** "Who it's for" paragraph. */
  audience: string;
  /** "Format & commitment" paragraph. */
  format: string;
}

// Category-specific framing + concrete learning outcomes. Keyed by the backend
// CourseCategory enum name (course.category).
const BY_CATEGORY: Record<string, { theme: string; learn: string[] }> = {
  PROGRAMMING: {
    theme:
      "solid software-engineering foundations: writing clean, maintainable code and reasoning about programs the way professional developers do",
    learn: [
      "Core language syntax, data structures and control flow, applied to real problems",
      "Object-oriented and functional patterns, and when to reach for each",
      "Debugging, testing and refactoring techniques used on production codebases",
      "Version control with Git and a professional branch-and-review workflow",
      "Reading, structuring and documenting code so a team can build on it",
    ],
  },
  AI: {
    theme:
      "applied artificial intelligence and machine learning: turning data and models into working, useful systems",
    learn: [
      "The maths and intuition behind supervised and unsupervised learning",
      "Building, training and evaluating models with Python and modern libraries",
      "Feature engineering, model selection and avoiding over- and under-fitting",
      "Working with neural networks and, where relevant, large language models",
      "Taking a model from a notebook prototype to a deployable service",
    ],
  },
  DATA_SCIENCE: {
    theme:
      "the full data-science workflow: from messy raw data to insight that drives decisions",
    learn: [
      "Collecting, cleaning and shaping data from real, imperfect sources",
      "Exploratory analysis and statistics to find what the data actually says",
      "Clear, honest visualisation that communicates results to non-experts",
      "Predictive modelling and how to validate that it generalises",
      "Reproducible analysis pipelines you can rerun and trust",
    ],
  },
  WEB_DEVELOPMENT: {
    theme:
      "building modern web applications end to end, from the browser to the server",
    learn: [
      "Semantic HTML, responsive CSS and accessible, mobile-first layouts",
      "Interactive front-ends with a component-based JavaScript framework",
      "Designing and consuming REST APIs and handling application state",
      "Authentication, data persistence and connecting to a backend",
      "Deploying, monitoring and iterating on a live web app",
    ],
  },
  MOBILE: {
    theme: "designing and shipping native-quality mobile apps that people enjoy using",
    learn: [
      "Mobile UI patterns and building fluid, responsive screens",
      "App architecture, navigation and managing state across screens",
      "Working with device features, storage and remote APIs",
      "Handling the app lifecycle, permissions and offline behaviour",
      "Packaging, testing and publishing to the app stores",
    ],
  },
  DEVOPS: {
    theme:
      "the DevOps culture and toolchain that lets teams ship reliably and often",
    learn: [
      "Containerising applications and orchestrating them at scale",
      "Building CI/CD pipelines that test and deploy automatically",
      "Infrastructure as code and reproducible environments",
      "Monitoring, logging and alerting for real production systems",
      "Cloud fundamentals and cost-aware, resilient architecture",
    ],
  },
  CYBERSECURITY: {
    theme:
      "thinking like both an attacker and a defender to keep systems and data safe",
    learn: [
      "The most common vulnerability classes and how they are exploited",
      "Securing applications, networks and identities in depth",
      "Practical hands-on labs in a safe, contained environment",
      "Threat modelling, risk assessment and secure-by-design principles",
      "Incident response and hardening systems after the fact",
    ],
  },
  PROFESSIONAL_RECONVERSION: {
    theme:
      "a structured path into tech for career changers, assuming no prior background",
    learn: [
      "The fundamentals of how software and the web actually work",
      "A first programming language, learned from absolute zero",
      "Hands-on projects that become the start of a real portfolio",
      "How the tech industry hires, and what the day-to-day job looks like",
      "Building the study habits and confidence to keep growing after the course",
    ],
  },
  WORKSHOP: {
    theme:
      "an intensive, focused deep-dive into a single topic, hands-on from the first minute",
    learn: [
      "A tightly-scoped skill you can apply immediately afterwards",
      "Guided, practical exercises rather than passive lecturing",
      "Real-world examples drawn from working practitioners",
      "Common pitfalls and the shortcuts experienced engineers use",
      "A concrete artefact or technique to take straight back to work",
    ],
  },
};

const GENERIC = {
  theme: "practical, job-ready skills taught by working engineers",
  learn: [
    "The core concepts of the subject, explained from the ground up",
    "Hands-on practice on realistic exercises and projects",
    "The tools and workflows professionals actually use day to day",
    "How to keep learning and apply the skills after the course ends",
  ],
};

const LEVEL_AUDIENCE: Record<string, string> = {
  BEGINNER:
    "This course starts from the fundamentals and assumes no prior experience with the topic, so it's ideal if you're just getting started or moving into the field.",
  INTERMEDIATE:
    "This course suits learners who already know the basics and want to become genuinely productive — you'll consolidate the fundamentals and push well beyond them.",
  ADVANCED:
    "This is an advanced course aimed at people who are already comfortable with the essentials and want to master the harder, real-world topics and edge cases.",
};

export function buildCourseDetails(course: PublicCourse): CourseDetails {
  const cat = course.category ? BY_CATEGORY[course.category] ?? GENERIC : GENERIC;
  const summary = course.description?.trim();
  const label = course.categoryLabel ?? "technology";

  const overview: string[] = [];
  overview.push(
    summary
      ? `${summary} Across the programme, "${course.name}" focuses on ${cat.theme}.`
      : `"${course.name}" is a hands-on ${label} course focused on ${cat.theme}.`,
  );
  overview.push(
    "Every topic is reinforced with guided, practical work rather than passive lectures, so you finish able to apply what you've learned — not just recognise it. You'll build things throughout, get feedback on your work, and leave with concrete experience you can point to.",
  );

  const audience =
    (course.level && LEVEL_AUDIENCE[course.level]) ??
    "The course is designed to meet you where you are and take you to a genuinely useful, applied level of skill.";

  const hours = course.durationHours;
  const format = hours
    ? `The course runs for around ${hours} hours of instructor-led time, combining short focused explanations with hands-on labs and projects. You can work through the material at a steady pace, with a trainer available for questions, code review and guidance along the way.`
    : "The course combines short focused explanations with hands-on labs and projects, with a trainer available for questions, code review and guidance along the way.";

  return { overview, learn: cat.learn, audience, format };
}
