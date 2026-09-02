import fs from "node:fs/promises";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const workspace = "D:\\TrainingIT_site\\.codex_artifacts\\presentation_5min";
const starter = `${workspace}\\template-starter.pptx`;
const finalPptx = "D:\\TrainingIT_site\\TrainingIT_5_minute_presentation.pptx";
const renderDir = `${workspace}\\final-render`;
const layoutDir = `${workspace}\\final-layout`;

await fs.mkdir(renderDir, { recursive: true });
await fs.mkdir(layoutDir, { recursive: true });

const presentation = await PresentationFile.importPptx(await FileBlob.load(starter));

const runtimeInspect = await presentation.inspect({
  kind: "slide,textbox,shape,image,table,chart,notes,layout",
  include: "id,slide,name,title,text,textPreview,textChars,textLines,bbox,bboxUnit,rows,cols,preview,isPlaceholder,placeholders",
  maxChars: 400000,
});
const runtimeRecords = (runtimeInspect.ndjson || "")
  .split(/\r?\n/)
  .filter(Boolean)
  .map((line) => JSON.parse(line));

function findRecord(predicate, label) {
  const matches = runtimeRecords.filter(predicate);
  if (matches.length !== 1) {
    throw new Error(`${label}: expected exactly one match, found ${matches.length}`);
  }
  return matches[0];
}

function replaceText(_sourceElementId, oldText, newText) {
  const record = findRecord(
    (item) => item.kind === "textbox" && item.text === oldText,
    `Text target ${oldText.slice(0, 60)}`,
  );
  const target = presentation.resolve(record.id);
  target.text.replace(oldText, newText);
}

function replaceFragment(oldText, newText) {
  const record = findRecord(
    (item) => item.kind === "textbox" && typeof item.text === "string" && item.text.includes(oldText),
    `Text fragment ${oldText.slice(0, 60)}`,
  );
  presentation.resolve(record.id).text.replace(oldText, newText);
}

function deleteRecord(record) {
  presentation.resolve(record.id).delete();
}

// Slide 1 - Problem and Motivation
replaceText(
  "sh/zut0zyh0",
  "One storefront and one back office - normally two different systems",
  "One platform for learning and CRM operations",
);
replaceText("sh/qp0jm90j", "Page 1 / 13", "Page 1 / 7");
replaceText(
  "sh/c3a94ny1",
  "The customer side.  A learner expects what any modern shop offers: a clear catalogue, prices and levels, reviews they can trust, a purchase in one click, and a private tutoring session booked at a time that suits them.",
  "The customer side.  Learners need a clear catalogue, trusted reviews, fast purchase and simple tutoring bookings.",
);
replaceText(
  "sh/x4jqdszm",
  "The business side.  Behind the storefront the company must know who its leads are and how close each is to buying, keep a reliable purchase history, issue correct invoices with negotiated discounts, read the health of the business, and handle corporate clients who send whole teams.",
  "The business side.  The company must track leads, purchases, invoices, analytics, corporate teams and support requests.",
);
replaceText(
  "sh/a18r2dgv",
  "The integration gap.  These two sides are normally separate tools that must be synchronised. Every synchronisation point is a place where data can drift, and manual reconciliation is slow and error-prone.",
  "The integration gap.  Separate shop and CRM systems drift out of sync and require slow, error-prone reconciliation.",
);
replaceText(
  "sh/2d87ytgz",
  "Build a single web application in which the customer-facing storefront and the business-facing CRM are not two integrated systems but one system - so that every customer action is captured exactly once and automatically drives enrolment, invoicing, lead scoring, auditing and support.",
  "TrainingIT implements both experiences on one backend and one data model. Each customer action is stored once and automatically updates enrolment, billing, lead scoring, audit and support.",
);
replaceText(
  "sh/8vu9c7y9",
  "It must also stay approachable for non-technical staff, keep the two roles strictly separated, and remain fully functional when optional services are switched off.",
  "USER and ADMIN remain strictly separated; optional services never block the core workflows.",
);

// Slide 2 - Related Work
replaceText("sh/fed4nmho", "Related Work Gap", "Related Work");
replaceText(
  "sh/sbmls7yd",
  "Existing tools solve parts of the problem, not the whole workflow",
  "Existing tools cover one side of the workflow",
);
replaceText("sh/58vmhczm", "Page 2 / 13", "Page 2 / 7");
replaceText(
  "sh/547yhkjm",
  "Each family of tools covers one side well; none unifies a course storefront and a CRM in a single product.",
  "Each tool family is strong in one area; none owns the full storefront-to-CRM workflow.",
);
replaceText(
  "sh/i1wfm5kb",
  "TrainingIT unifies both sides in one application built on one shared data model, and specialises the generic CRM notions for the training domain: course sessions, trainer calendars, per-hour tutoring with employee discounts, and course-level click-through analytics.",
  "TrainingIT's distinction: one shared backend and data model, specialised for course sessions, trainer calendars, employee discounts and course analytics.",
);

// Slide 3 - Contribution
replaceText(
  "sh/zy1s3ipk",
  "A working product, not a prototype: storefront and CRM as one system",
  "An implemented product: storefront and CRM as one system",
);
replaceText("sh/at8r6d83", "Page 3 / 13", "Page 3 / 7");
replaceText(
  "sh/zah4falg",
  "TrainingIT is a functional web application: a course marketplace whose every customer action is recorded exactly once in a complete CRM back office, on a Java domain layer organised around classic design patterns and an event-driven core, with an optional Claude-powered AI layer.",
  "TrainingIT is an implemented full-stack product - not a prototype - that unifies a course storefront and CRM on one backend and shared data model.",
);
replaceFragment(
  "A public client portal: browse, purchase, review, book one-to-one tutoring.",
  "Client portal: catalogue, purchase, reviews and 1:1 booking.",
);
replaceFragment(
  "An administrator portal with seven management tabs.",
  "Admin portal: seven CRM and operational sections.",
);
replaceFragment(
  "A domain layer on design patterns and an event-driven core, so one action propagates into several automatic reactions.",
  "Event-driven Java domain layer with classic patterns.",
);
replaceFragment(
  "An optional AI assistant that degrades gracefully.",
  "Optional AI with graceful degradation.",
);
replaceFragment(
  "All operational and CRM data persisted in MariaDB.",
  "Operational and CRM data persisted in MariaDB.",
);
replaceFragment(
  "The goal is not a general-purpose e-commerce platform, nor a general-purpose CRM.",
  "TrainingIT is specialised for one training company, not a general e-commerce platform or generic CRM.",
);
replaceFragment(
  "The goal is one coherent product for a training company: a storefront a learner can use without instructions, over a CRM core a small team can actually run - where the AI layer is strictly additive and never a dependency.",
  "The AI layer adds capabilities, but core workflows never depend on it.",
);

// Slide 4 - System Architecture: only the authentic source diagram.
const architectureDiagramRecord = findRecord(
  (item) => item.kind === "image" && item.slide === 4 && item.name === "Image 1",
  "Architecture diagram",
);
for (const record of runtimeRecords.filter(
  (item) => item.slide === 4 && ["textbox", "shape", "image"].includes(item.kind) && item.id !== architectureDiagramRecord.id,
)) {
  deleteRecord(record);
}
const architectureDiagram = presentation.resolve(architectureDiagramRecord.id);
architectureDiagram.fit = "contain";
architectureDiagram.frame = { left: 0, top: 0, width: 960, height: 540 };
architectureDiagram.lockAspectRatio = true;

// Slide 5 - The AI Layer
replaceText(
  "sh/dsbapone",
  "Optional intelligence, powered by the Anthropic Claude API",
  "Four optional capabilities; zero dependency for core workflows",
);
replaceText("sh/8vy94zm9", "Page 10 / 13", "Page 5 / 7");
replaceText(
  "sh/8zapoj6x",
  "All four features share one Claude client and one API key. I implemented the prompts, the REST endpoints, the structured JSON response handling and the matching interfaces - and made sure the rest of the application stays fully operational when AI is not configured.",
  "One Claude client powers all four features. Prompts, REST endpoints and structured responses are isolated so the application remains fully operational without an API key.",
);

// Slide 6 - Application Demo: keep the transition shell, remove the embedded media.
replaceText("sh/q1gbmpcv", "Video", "Live demonstration");
replaceText("sh/f69cjudc", "Page 12 / 13", "Page 6 / 7");
deleteRecord(findRecord((item) => item.kind === "image" && item.slide === 6 && item.name === "Demo", "Demo media"));

// Slide 7 - Conclusion and Future Work
replaceText(
  "sh/6xgz25wf",
  "One product where the storefront and the CRM are the same system",
  "One product, one data model, no manual reconciliation",
);
replaceText("sh/jup0rqd4", "Page 13 / 13", "Page 7 / 7");
replaceFragment(
  "A client portal: catalogue, one-click purchase, star reviews, 1:1 booking with calendar and payment, issue reporting.",
  "Client portal for course discovery, purchase, reviews, tutoring and issues.",
);
replaceFragment(
  "A seven-tab administrator portal: contacts, courses, purchases, invoices, analytics, employees, issues.",
  "Seven-tab admin portal for CRM and operations.",
);
replaceFragment(
  "An event-driven Java domain layer on classic design patterns, with automatic invoicing and lead scoring.",
  "Event-driven Java domain layer with automatic reactions.",
);
replaceFragment(
  "Live statistics over SSE, Excel import, and CSV / Excel / PDF export.",
  "Shared MariaDB data and live statistics via SSE.",
);
replaceFragment(
  "An optional Claude layer: assistant, recommendation quiz, per-company advice, live translation.",
  "Optional Claude assistant, recommendations and translation.",
);
replaceFragment(
  "Automated test suite - unit and integration tests over the commands, observers, scoring strategies and REST endpoints, enabling continuous integration.",
  "Automated unit and integration tests with CI.",
);
replaceFragment(
  "Stronger authentication - token-based sessions, password hashing at rest, e-mail verification and a genuine reset flow with expiring links.",
  "Hardened authentication and password handling.",
);
replaceFragment(
  "Richer analytics - historical trends, cohort analysis and exportable dashboards beyond point-in-time metrics.",
  "Historical and cohort analytics with exports.",
);
replaceFragment(
  "Deeper AI - conversation memory and AI summaries of support issues.",
  "AI memory and summaries of support issues.",
);
replaceFragment(
  "Deployment and scaling - containerising the two services for a cloud deployment.",
  "Containerised deployment and cloud scaling.",
);
replaceText(
  "sh/mx4futcv",
  "A visitor becomes a tracked lead on registration and a customer on the first purchase, and every action after that propagates into the administration portal by itself. That is the result: one click, several reactions, no reconciliation.",
  "Result: one customer action is stored once and propagated automatically - no manual reconciliation.",
);

// Speaker notes: approximately 4:40 of planned speech plus demo transition.
const notes = [
  "A training company serves two audiences. Learners expect a simple online shop, while the company needs leads, invoices, analytics and support history. If the shop and CRM are separate, every synchronisation point can lose or contradict data. TrainingIT removes that gap: both experiences use the same backend and data model, so each customer action is recorded once and triggers the necessary administrative reactions.",
  "Existing products solve only part of this workflow. Course marketplaces offer discovery and checkout but do not give one provider an integrated CRM. Moodle focuses on learning delivery. Salesforce and HubSpot manage customers but do not understand course sessions or trainer calendars. WooCommerce sells courses as ordinary products. TrainingIT's difference is domain-specific integration: storefront and CRM are one product.",
  "The contribution is applied: a working full-stack system, not only a design. It includes a client portal, a seven-section administrator portal, persistent MariaDB data and an event-driven Java domain layer. Classic patterns keep responsibilities separated, while the AI layer is optional. The boundary is deliberate: this is a specialised product for a training company, not a generic shop or generic CRM.",
  "The browser layer is Next.js and React, with route protection, a typed REST client and SSE for live statistics. Spring Boot exposes thin controllers and application services. The Java domain layer contains commands, services, EventBus observers, repositories and DAOs. MariaDB persists both operational and CRM data through JDBC and HikariCP. Claude is the only optional external service.",
  "The AI layer provides four capabilities: catalogue-aware chat, a visitor recommendation quiz, training suggestions for company teams and dynamic page translation. They share one Claude client, while prompts and structured responses stay behind dedicated endpoints. The key architectural property is graceful degradation: without an API key, the AI controls disappear and all core application workflows continue normally.",
  "I will now switch to the running application. I will show the learner catalogue, a purchase appearing in the administrator portal, and the AI recommendation flow if time allows. The important observation is that both portals operate on the same session data and shared backend, so the administrative result appears without a separate synchronisation step.",
  "TrainingIT meets the main objective: the storefront and CRM work as one coherent product. A registration creates a lead; a purchase, review, booking or issue is reflected automatically in administration. The next priorities are automated test coverage, stronger authentication, richer analytics, deeper AI context and cloud deployment. The final result is simple: one action, several reactions, no manual reconciliation.",
];

for (const [index, slide] of presentation.slides.items.entries()) {
  slide.speakerNotes.textFrame.setText(notes[index]);
  slide.speakerNotes.setVisible(true);
}

const finalInspect = await presentation.inspect({
  kind: "slide,textbox,shape,image,table,chart,notes,layout",
  include: "id,slide,name,title,text,textPreview,textChars,textLines,bbox,bboxUnit,rows,cols,preview,isPlaceholder,placeholders",
  maxChars: 400000,
});
await fs.writeFile(`${workspace}\\final-inspect.ndjson`, finalInspect.ndjson || "", "utf8");

for (const [index, slide] of presentation.slides.items.entries()) {
  const stem = `slide-${String(index + 1).padStart(2, "0")}`;
  const png = await presentation.export({ slide, format: "png", scale: 2 });
  await fs.writeFile(`${renderDir}\\${stem}.png`, new Uint8Array(await png.arrayBuffer()));
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(`${layoutDir}\\${stem}.layout.json`, await layout.text(), "utf8");
}

const montage = await presentation.export({ format: "webp", montage: true, scale: 1 });
await fs.writeFile(`${workspace}\\final-montage.webp`, new Uint8Array(await montage.arrayBuffer()));

const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(finalPptx);
console.log(finalPptx);
