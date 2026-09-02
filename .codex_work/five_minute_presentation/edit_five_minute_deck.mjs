import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const buildDir = "D:\\TrainingIT_site\\.codex_work\\five_minute_presentation";
const inputPptx = path.join(buildDir, "template-starter.pptx");
const outputPptx = "D:\\TrainingIT_site\\Powerpoint_Presentation_5_minute.pptx";
const previewDir = path.join(buildDir, "final-preview");
const layoutDir = path.join(buildDir, "final-layout");
let importedTextRecords = [];

async function writeBlob(filePath, blob) {
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

function replaceExact(presentation, id, oldText, newText) {
  const record = importedTextRecords.find(
    (item) => item.kind === "textbox" && typeof item.text === "string" && item.text.includes(oldText),
  );
  if (!record) {
    throw new Error(`Could not resolve imported text target ${id}: ${oldText}`);
  }
  const target = presentation.resolve(record.id);
  target.text.replace(oldText, newText);
}

async function main() {
  await fs.mkdir(previewDir, { recursive: true });
  await fs.mkdir(layoutDir, { recursive: true });

  const presentation = await PresentationFile.importPptx(
    await FileBlob.load(inputPptx),
  );
  const importedSnapshot = await presentation.inspect({
    kind: "slide,textbox",
    include: "id,slide,name,text",
    maxChars: 50000,
  });
  importedTextRecords = importedSnapshot.ndjson
    .split(/\r?\n/)
    .filter((line) => line.trim().startsWith("{"))
    .map((line) => JSON.parse(line));

  // Slide 2 - problem: keep the three-part structure, remove prose density.
  replaceExact(
    presentation,
    "sh/u503ydcn",
    "One storefront and one back office - normally two different systems",
    "A storefront and a CRM should not depend on fragile synchronization",
  );
  replaceExact(
    presentation,
    "sh/zux876ho",
    "A learner expects what any modern shop offers: a clear catalogue, prices and levels, reviews they can trust, a purchase in one click, and a private tutoring session booked at a time that suits them.",
    "Learners need a clear catalogue, trusted reviews, quick purchase and simple tutoring reservations.",
  );
  replaceExact(
    presentation,
    "sh/ytory1g3",
    "Behind the storefront the company must know who its leads are and how close each is to buying, keep a reliable purchase history, issue correct invoices with negotiated discounts, read the health of the business, and handle corporate clients who send whole teams.",
    "The company needs leads, purchases, invoices, analytics and corporate clients in one operational view.",
  );
  replaceExact(
    presentation,
    "sh/lwfa9gzu",
    "These two sides are normally separate tools that must be synchronised. Every synchronisation point is a place where data can drift, and manual reconciliation is slow and error-prone.",
    "Separate tools create duplicate or inconsistent data and require slow manual reconciliation.",
  );
  replaceExact(
    presentation,
    "sh/90zadgzq",
    "Build a single web application in which the customer-facing storefront and the business-facing CRM are not two integrated systems but one system - so that every customer action is captured exactly once and automatically drives enrolment, invoicing, lead scoring, auditing and support.",
    "Use one application and one shared data model so each customer action is captured once and drives the relevant CRM processes automatically.",
  );
  replaceExact(
    presentation,
    "sh/vmhsfqhw",
    "It must also stay approachable for non-technical staff, keep the two roles strictly separated, and remain fully functional when optional services are switched off.",
    "One system. Two role-based experiences. No manual reconciliation.",
  );
  replaceExact(presentation, "sh/z2d4j2tc", "Page 1 / 13", "Page 1 / 6");

  // Slide 3 - solution in one view.
  replaceExact(presentation, "sh/t8jyts3q", "System Context", "One Platform, Two Experiences");
  replaceExact(
    presentation,
    "sh/e9szmxkb",
    "Three actors, one platform, two external services",
    "The learner portal and the CRM share the same operational data",
  );
  replaceExact(
    presentation,
    "sh/1kby18jq",
    "Learners and partner-company employees buy and review courses; administrators run the CRM, catalogue, billing and analytics; trainers provide tutoring availability. MariaDB stores everything; the Anthropic Claude API is the only other dependency - and it is optional.",
    "Learners create business events; administrators immediately see the resulting contacts, purchases, invoices and issues. MariaDB stores both operational and CRM data; Claude remains optional.",
  );
  replaceExact(presentation, "sh/vehgjylc", "Page 4 / 13", "Page 2 / 6");

  // Slide 4 - architecture: keep the existing four bands and their typography.
  replaceExact(presentation, "sh/dgfixcna", "Application Architecture", "Architecture in Four Layers");
  replaceExact(
    presentation,
    "sh/i94jyx4n",
    "Four layers behind one REST API and a real-time event stream",
    "REST for commands, SSE for live statistics, EventBus for reactions",
  );
  replaceExact(
    presentation,
    "sh/pwfqp4bu",
    "Pages, route guards from the local session, a typed REST client and a Server-Sent Events client for live statistics.",
    "Role-based learner and administrator portals, a typed REST client and live statistics over SSE.",
  );
  replaceExact(
    presentation,
    "sh/bih8ret0",
    "REST controllers under /api, the auth, AI, trainer and report services, and a public statistics broadcaster.",
    "REST controllers delegate to authentication, AI, trainer, reporting and statistics services.",
  );
  replaceExact(
    presentation,
    "sh/dkzqtor6",
    "Commands, domain services, an EventBus with observers, and repositories. CrmFacade is the single entry point over all of it.",
    "Commands, services and EventBus observers implement the business rules behind one facade.",
  );
  replaceExact(
    presentation,
    "sh/zmh8ve9w",
    "Contacts, enrolments, invoices and issues survive restarts; the schema is created and updated at start-up.",
    "JDBC and HikariCP persist both CRM and operational data in one relational schema.",
  );
  replaceExact(presentation, "sh/n6xk36lc", "Page 5 / 13", "Page 3 / 6");

  // Slide 5 - central event-driven flow.
  replaceExact(
    presentation,
    "sh/dkjah8n6",
    "Purchase and review, from the client's confirmation to live statistics",
    "One purchase becomes enrollment, audit, CRM history and live statistics",
  );
  replaceExact(
    presentation,
    "sh/3ax0bmd0",
    "A single purchase finds or creates the contact and the course session, saves the enrolment, publishes an event, notifies the observers, and updates the live figures over SSE - with no manual reconciliation anywhere.",
    "The purchase is saved once. Independent observers handle secondary reactions, so the main operation stays focused and the CRM updates without reconciliation.",
  );
  replaceExact(presentation, "sh/4va943mp", "Page 9 / 13", "Page 4 / 6");

  // Slide 6 - concise three-view application demo cue.
  replaceExact(
    presentation,
    "sh/ra943il8",
    "The running application",
    "Three views of the same shared system",
  );
  replaceExact(
    presentation,
    "sh/hkbm5wzu",
    "The same session drives both sides: a purchase made on the catalogue appears immediately in the administrator's Purchases tab, a booking generates its invoice by itself, and a reported issue lands straight in the Issues tab.",
    "A learner action appears immediately in the administrator portal, while live statistics and CRM records update from the same data.",
  );
  replaceExact(presentation, "sh/a543mx4r", "Page 11 / 13", "Page 5 / 6");

  // Slide 7 - concise outcome, evidence and next steps.
  replaceExact(presentation, "sh/a9cz2lsv", "Conclusion and Future Work", "Conclusion");
  replaceExact(
    presentation,
    "sh/983ytgra",
    "One product where the storefront and the CRM are the same system",
    "TrainingIT demonstrates one coherent storefront and CRM",
  );
  replaceExact(
    presentation,
    "sh/ps7ed8jy",
    "A client portal: catalogue, one-click purchase, star reviews, 1:1 booking with calendar and payment, issue reporting.",
    "Learner portal: catalogue, purchase, reviews and tutoring.",
  );
  replaceExact(
    presentation,
    "sh/ps7ed8jy",
    "A seven-tab administrator portal: contacts, courses, purchases, invoices, analytics, employees, issues.",
    "CRM portal: contacts, purchases, invoices, analytics and issues.",
  );
  replaceExact(
    presentation,
    "sh/ps7ed8jy",
    "An event-driven Java domain layer on classic design patterns, with automatic invoicing and lead scoring.",
    "Event-driven domain: automated lead scoring and invoicing.",
  );
  replaceExact(
    presentation,
    "sh/ps7ed8jy",
    "Live statistics over SSE, Excel import, and CSV / Excel / PDF export.",
    "Live SSE statistics, imports and multi-format exports.",
  );
  replaceExact(
    presentation,
    "sh/ps7ed8jy",
    "An optional Claude layer: assistant, recommendation quiz, per-company advice, live translation.",
    "Optional Claude assistant, recommendations and translation.",
  );
  replaceExact(presentation, "sh/f25w7y1w", "Limitations and next steps", "Validated and next steps");
  replaceExact(
    presentation,
    "sh/14ne98j2",
    "Automated test suite - unit and integration tests over the commands, observers, scoring strategies and REST endpoints, enabling continuous integration.",
    "End-to-end flows verified against a real MariaDB instance.",
  );
  replaceExact(
    presentation,
    "sh/14ne98j2",
    "Stronger authentication - token-based sessions, password hashing at rest, e-mail verification and a genuine reset flow with expiring links.",
    "Duplicate invoices prevented through idempotent processing.",
  );
  replaceExact(
    presentation,
    "sh/14ne98j2",
    "Richer analytics - historical trends, cohort analysis and exportable dashboards beyond point-in-time metrics.",
    "Observer failures are isolated; optional AI degrades safely.",
  );
  replaceExact(
    presentation,
    "sh/14ne98j2",
    "Deeper AI - conversation memory and AI summaries of support issues.",
    "Next: automated tests and production-grade authentication.",
  );
  replaceExact(
    presentation,
    "sh/14ne98j2",
    "Deployment and scaling - containerising the two services for a cloud deployment.",
    "Next: richer analytics and cloud deployment.",
  );
  replaceExact(
    presentation,
    "sh/ry5w3y10",
    "A visitor becomes a tracked lead on registration and a customer on the first purchase, and every action after that propagates into the administration portal by itself. That is the result: one click, several reactions, no reconciliation.",
    "Each customer action is recorded once and becomes immediately available to the business.",
  );
  replaceExact(presentation, "sh/03uhwrat", "Page 13 / 13", "Page 6 / 6");

  const notes = [
    `Timing: 0:00-0:15\nGood morning. My name is Raul Veca, and I will present TrainingIT, a web application for selling programming courses. The main idea is simple: the learner-facing storefront and the company's CRM are implemented as one product.\n\n[Sources]\n- Powerpoint_Presentation.pptx, source slide 1\n- TrainingIT_thePDF.pdf, cover and abstract`,
    `Timing: 0:15-0:55\nA training company serves two audiences. Learners expect a modern storefront: clear courses, trusted reviews, quick purchasing and simple tutoring reservations. The company needs a reliable back office for leads, purchases, invoices, analytics and corporate customers. These are normally separate systems. Every synchronization point can create missing or contradictory data, so the practical problem is not only selling courses; it is keeping the customer experience and the CRM consistent.\n\n[Sources]\n- TrainingIT_thePDF.pdf, Problem Description\n- TrainingIT_conspectare.docx, Chapter 2`,
    `Timing: 0:55-1:35\nTrainingIT removes that synchronization boundary. Learners, administrators and trainers use different interfaces, but they act on the same backend and the same data model. When a learner registers, buys, reviews, books a session or reports an issue, the corresponding CRM information already exists. MariaDB stores both the operational and CRM records. Claude adds chat, recommendations and translation, but it is optional, so the product remains functional without it.\n\n[Sources]\n- TrainingIT_thePDF.pdf, Proposed Solution and Application Architecture\n- TrainingIT_conspectare.docx, Chapters 1 and 5`,
    `Timing: 1:35-2:25\nThe implementation has four layers. Next.js and React provide the learner and administrator portals. Spring Boot exposes the REST API and the live statistics stream. The domain layer contains commands, services, repositories and the EventBus, so business rules are separated from the web controllers. MariaDB persists contacts, enrollments, invoices and issues through JDBC and HikariCP. This structure keeps the application understandable and allows secondary reactions to evolve without rewriting the main user operation.\n\n[Sources]\n- TrainingIT_thePDF.pdf, Application Architecture and Technologies Used\n- TrainingIT_conspectare.docx, Chapters 5 and 7`,
    `Timing: 2:25-3:10\nThis purchase flow is the key technical example. After confirmation, the server finds or creates the contact and the course session, saves the enrollment, and publishes an event. Independent observers can then update the audit trail, lead state and live statistics. The main purchase service does not need to know every secondary effect. The same design supports idempotency: if a booking event is repeated, it does not generate a second invoice.\n\n[Sources]\n- TrainingIT_thePDF.pdf, Implementation Details: event-driven core\n- TrainingIT_conspectare.docx, Chapter 8`,
    `Timing: 3:10-4:25\nDemo path: log in as a learner; open the catalogue; purchase one course; show that it appears in My Courses; add or update a review; then switch to the administrator portal and open Purchases or Contacts to show the same action in the CRM. If time remains, show a tutoring booking and the generated invoice. Keep the narration focused on one action becoming several business reactions.\n\n[Sources]\n- Embedded application demo from Powerpoint_Presentation.pptx\n- TrainingIT_thePDF.pdf, Application Features\n- TrainingIT_conspectare.docx, Chapters 6 and 10`,
    `Timing: 4:25-5:00\nIn conclusion, TrainingIT delivers the learner portal, the CRM portal and an event-driven domain on one shared model. The end-to-end flows were verified against MariaDB, including duplicate-event and observer-failure scenarios. The next priorities are automated tests, stronger production authentication, richer analytics and cloud deployment. The central result is that each customer action is recorded once and becomes immediately available to the business. Thank you.\n\n[Sources]\n- TrainingIT_thePDF.pdf, Testing and Conclusion\n- TrainingIT_conspectare.docx, Chapters 9 and 11`,
  ];

  for (let i = 0; i < presentation.slides.items.length; i += 1) {
    const slide = presentation.slides.items[i];
    slide.speakerNotes.textFrame.setText(notes[i]);
    slide.speakerNotes.setVisible(true);
  }

  const inspection = await presentation.inspect({
    kind: "deck,slide,textbox,shape,image,table,chart,notes,layout",
    include: "id,slide,name,title,text,textPreview,textChars,textLines,bbox,isPlaceholder",
    maxChars: 50000,
  });
  await fs.writeFile(path.join(buildDir, "final-inspect.ndjson"), inspection.ndjson, "utf8");

  for (const [index, slide] of presentation.slides.items.entries()) {
    const stem = `slide-${String(index + 1).padStart(2, "0")}`;
    await writeBlob(
      path.join(previewDir, `${stem}.png`),
      await presentation.export({ slide, format: "png", scale: 2 }),
    );
    const layout = await slide.export({ format: "layout" });
    await fs.writeFile(path.join(layoutDir, `${stem}.layout.json`), await layout.text(), "utf8");
  }

  const montage = await presentation.export({ format: "webp", montage: true, scale: 1 });
  await writeBlob(path.join(buildDir, "final-montage.webp"), montage);

  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(outputPptx);
  console.log(outputPptx);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
