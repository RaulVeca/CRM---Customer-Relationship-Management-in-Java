import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const BUILD_DIR = "D:\\TrainingIT_site\\.codex-pptx-template-architecture";
const INPUT = path.join(BUILD_DIR, "template-starter.pptx");
const OUTPUT = "D:\\TrainingIT_site\\5 min - UVT template.pptx";
const RENDER_DIR = path.join(BUILD_DIR, "final-render");
const ARCHITECTURE_IMAGE = path.join(BUILD_DIR, "content-inspect", "assets", "ppt", "media", "image4.png");
const AI_SCREENSHOT = path.join(BUILD_DIR, "content-inspect", "assets", "ppt", "media", "image5.png");

const NAVY = "#022461";
const MID_BLUE = "#2F5D8C";
const BRIGHT_BLUE = "#2588E6";
const PALE_BLUE = "#EEF5FC";
const PALEST_BLUE = "#F7FAFE";
const INK = "#172033";
const MUTED = "#5A6472";
const WHITE = "#FFFFFF";
const DISPLAY = "Helvetica Now Display Bold";
const BODY = "Calibri";

async function saveBlob(filePath, blob) {
  await fs.mkdir(path.dirname(filePath), { recursive: true });
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

async function imageBytes(filePath) {
  const bytes = await fs.readFile(filePath);
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
}

function records(snapshot) {
  return (snapshot.ndjson || "")
    .split(/\r?\n/)
    .filter(Boolean)
    .map((line) => JSON.parse(line));
}

async function findTextShape(presentation, slideNumber, needle) {
  const snapshot = await presentation.inspect({
    kind: "textbox,shape",
    include: "id,slide,name,text,textPreview,bbox",
    search: needle,
    maxChars: 16000,
  });
  const hit = records(snapshot).find(
    (record) => record.slide === slideNumber && typeof record.text === "string" && record.text.includes(needle),
  );
  if (!hit?.id) throw new Error(`Could not locate slide ${slideNumber} text: ${needle}`);
  return presentation.resolve(hit.id);
}

function addText(slide, {
  name,
  text,
  left,
  top,
  width,
  height,
  fontSize = 24,
  color = INK,
  bold = false,
  typeface = BODY,
  alignment = "left",
  verticalAlignment = "top",
  fill = "none",
  line = { style: "solid", fill: "none", width: 0 },
  geometry = "textbox",
  borderRadius,
  shadow,
  insets = { top: 0, right: 0, bottom: 0, left: 0 },
}) {
  const shape = slide.shapes.add({
    geometry,
    name,
    position: { left, top, width, height },
    fill,
    line,
    ...(borderRadius ? { borderRadius } : {}),
    ...(shadow ? { shadow } : {}),
  });
  shape.text = text;
  shape.text.style = {
    fontSize,
    color,
    bold,
    typeface,
    alignment,
    verticalAlignment,
    wrap: "square",
    autoFit: "none",
    insets,
  };
  return shape;
}

function addHeader(slide, title, subtitle, pageNumber) {
  addText(slide, {
    name: `slide-${pageNumber}-title`,
    text: title,
    left: 250,
    top: 34,
    width: 1000,
    height: 58,
    fontSize: 45,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
    verticalAlignment: "middle",
  });
  addText(slide, {
    name: `slide-${pageNumber}-subtitle`,
    text: subtitle,
    left: 250,
    top: 96,
    width: 1120,
    height: 44,
    fontSize: 25,
    color: MID_BLUE,
    typeface: BODY,
    verticalAlignment: "middle",
  });
  addText(slide, {
    name: `slide-${pageNumber}-page-number`,
    text: `${pageNumber} / 7`,
    left: 905,
    top: 1034,
    width: 110,
    height: 22,
    fontSize: 15,
    color: MID_BLUE,
    bold: true,
    typeface: BODY,
    alignment: "center",
    verticalAlignment: "middle",
  });
}

function addSection(slide, { name, heading, body, left, top, width, headingSize = 29, bodySize = 24 }) {
  addText(slide, {
    name: `${name}-heading`,
    text: heading,
    left,
    top,
    width,
    height: 38,
    fontSize: headingSize,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
    verticalAlignment: "middle",
  });
  addText(slide, {
    name: `${name}-body`,
    text: body,
    left,
    top: top + 45,
    width,
    height: 94,
    fontSize: bodySize,
    color: INK,
    typeface: BODY,
    verticalAlignment: "top",
  });
}

function addRule(slide, left, top, width, color = "#B8C9DC", thickness = 2) {
  slide.shapes.add({
    geometry: "line",
    position: { left, top, width, height: 0 },
    fill: "none",
    line: { style: "solid", fill: color, width: thickness },
  });
}

function addBulletList(slide, { name, items, left, top, width, height, fontSize = 23, color = INK, gap = 8 }) {
  const shape = slide.shapes.add({
    geometry: "textbox",
    name,
    position: { left, top, width, height },
    fill: "none",
    line: { style: "solid", fill: "none", width: 0 },
  });
  shape.text = items.map((item) => ({
    bulletCharacter: "•",
    marginLeft: 30,
    indent: -16,
    spaceAfter: gap,
    runs: [{ run: item }],
  }));
  shape.text.style = {
    fontSize,
    typeface: BODY,
    color,
    alignment: "left",
    verticalAlignment: "top",
    wrap: "square",
    autoFit: "none",
    insets: { top: 0, right: 0, bottom: 0, left: 0 },
  };
  return shape;
}

function styleTable(table) {
  table.borders.assign({ style: "solid", fill: WHITE, width: 2 });
  for (let column = 0; column < 3; column += 1) {
    const cell = table.getCell(0, column);
    cell.fill = NAVY;
    cell.text.style = {
      fontSize: 21,
      typeface: DISPLAY,
      color: WHITE,
      bold: true,
      verticalAlignment: "middle",
      insets: { top: 8, right: 10, bottom: 8, left: 10 },
    };
  }
  for (let row = 1; row < 5; row += 1) {
    for (let column = 0; column < 3; column += 1) {
      const cell = table.getCell(row, column);
      cell.fill = row % 2 === 1 ? PALE_BLUE : PALEST_BLUE;
      cell.text.style = {
        fontSize: column === 0 ? 19 : 18,
        typeface: BODY,
        color: column === 0 ? NAVY : INK,
        bold: column === 0,
        verticalAlignment: "middle",
        insets: { top: 8, right: 10, bottom: 8, left: 10 },
      };
    }
  }
  table.rows[0].height = 58;
  for (let row = 1; row < 5; row += 1) table.rows[row].height = 112;
}

async function build() {
  await fs.mkdir(RENDER_DIR, { recursive: true });
  const presentation = await PresentationFile.importPptx(await FileBlob.load(INPUT));

  // Cover: retain the authentic template and rewrite only inherited text.
  const coverTitle = await findTextShape(presentation, 1, "TITLUL LUCRĂRII");
  coverTitle.position = { left: 270, top: 340, width: 1380, height: 180 };
  coverTitle.text = [
    {
      spaceAfter: 10,
      runs: [{ run: "TRAININGIT", textStyle: { fontSize: "48pt", typeface: DISPLAY, bold: true, color: WHITE } }],
    },
    {
      runs: [{ run: "A WEB-BASED APPLICATION FOR SELLING PROGRAMMING COURSES", textStyle: { fontSize: "21pt", typeface: DISPLAY, bold: true, color: WHITE } }],
    },
  ];
  coverTitle.text.style = {
    alignment: "center",
    verticalAlignment: "middle",
    wrap: "square",
    autoFit: "none",
    insets: { top: 0, right: 0, bottom: 0, left: 0 },
  };

  const faculty = await findTextShape(presentation, 1, "FACULTATEA DE INFORMATICĂ");
  faculty.text.replace("FACULTATEA DE INFORMATICĂ", "FACULTY OF COMPUTER SCIENCE");
  faculty.text.replace("PROGRAMUL DE STUDII: INFORMATICĂ", "BACHELOR'S DEGREE PROGRAMME: COMPUTER SCIENCE");

  const supervisor = await findTextShape(presentation, 1, "COORDONATOR:");
  supervisor.position = { left: 108, top: 808, width: 760, height: 112 };
  supervisor.text.replace("COORDONATOR:", "SUPERVISOR:");
  supervisor.text.replace("Lect. Univ. Dr. Ștefănigă Sebastian-Aurelian", "Lect. Dr. Sebastian Ștefănigă");

  const candidate = await findTextShape(presentation, 1, "CANDIDAT:");
  candidate.position = { left: 1470, top: 808, width: 342, height: 112 };
  candidate.text.replace("CANDIDAT:", "CANDIDATE:");
  candidate.text.replace("...................................", "Raul Veca");

  // Slide 2: Problem and Motivation.
  const slide2 = presentation.slides.getItem(1);
  addHeader(slide2, "Problem and Motivation", "One system removes fragile synchronization between the storefront and CRM", 1);
  addSection(slide2, {
    name: "customer-side",
    heading: "The customer side",
    body: "Learners need a clear catalogue, trusted reviews, quick purchase and simple tutoring reservations.",
    left: 280,
    top: 220,
    width: 760,
  });
  addRule(slide2, 280, 380, 760);
  addSection(slide2, {
    name: "business-side",
    heading: "The business side",
    body: "The company needs leads, purchases, invoices, analytics and corporate clients in one operational view.",
    left: 280,
    top: 430,
    width: 760,
  });
  addRule(slide2, 280, 590, 760);
  addSection(slide2, {
    name: "integration-gap",
    heading: "The integration gap",
    body: "Separate tools create duplicate or inconsistent data and require slow manual reconciliation.",
    left: 280,
    top: 640,
    width: 760,
  });
  const problem = addText(slide2, {
    name: "problem-statement",
    text: [
      { spaceAfter: 20, runs: [{ run: "PROBLEM STATEMENT", textStyle: { fontSize: "23pt", typeface: DISPLAY, bold: true, color: NAVY } }] },
      { spaceAfter: 28, runs: [{ run: "Use one application and one shared data model so each customer action is captured once and drives the relevant CRM processes automatically.", textStyle: { fontSize: "19pt", typeface: BODY, color: INK } }] },
      { runs: [{ run: "One system. Two role-based experiences. No manual reconciliation.", textStyle: { fontSize: "18pt", typeface: BODY, bold: true, color: MID_BLUE } }] },
    ],
    left: 1140,
    top: 225,
    width: 560,
    height: 590,
    fill: PALE_BLUE,
    line: { style: "solid", fill: "#BCD0E5", width: 2 },
    geometry: "roundRect",
    borderRadius: 18,
    shadow: "shadow-sm",
    insets: { top: 32, right: 34, bottom: 28, left: 34 },
  });
  problem.text.style = { alignment: "left", verticalAlignment: "top", wrap: "square", autoFit: "none", insets: { top: 32, right: 34, bottom: 28, left: 34 } };

  // Slide 3: Related Work.
  const slide3 = presentation.slides.getItem(2);
  addHeader(slide3, "Related Work", "Existing tools solve parts of the problem, not the whole workflow", 2);
  addText(slide3, {
    name: "related-work-intro",
    text: "Each tool family covers one side well; none unifies a course storefront and a CRM in a single product.",
    left: 270,
    top: 175,
    width: 1400,
    height: 40,
    fontSize: 22,
    color: MUTED,
    verticalAlignment: "middle",
  });
  const table = slide3.tables.add({
    rows: 5,
    columns: 3,
    left: 270,
    top: 230,
    width: 1400,
    height: 506,
    columnTracks: [{ mode: "fr", value: 1.2 }, { mode: "fr", value: 1.55 }, { mode: "fr", value: 1.85 }],
    values: [
      ["Tool family", "Strength", "Limitation"],
      ["Course marketplaces\ne.g. Udemy, Coursera", "Excellent storefront, catalogue, reviews and checkout", "Aggregators: no owned customer relationship and no integrated CRM"],
      ["Learning management\ne.g. Moodle", "Strong at delivering and tracking course content", "Built around content, not selling; commerce and CRM are bolted on"],
      ["CRM platforms\ne.g. Salesforce, HubSpot", "Powerful contacts, pipelines, lead scoring and reporting", "Generic: no course catalogue, trainer calendar or public storefront"],
      ["E-commerce engines\ne.g. WooCommerce", "Self-hosted and able to sell anything, including courses", "A course is only a product: no lead stages, tutoring sessions or corporate training"],
    ],
  });
  styleTable(table);
  addText(slide3, {
    name: "related-work-takeaway",
    text: "TrainingIT unifies both sides on one shared data model and specializes CRM workflows for training: sessions, calendars, tutoring, discounts and course analytics.",
    left: 270,
    top: 785,
    width: 1400,
    height: 112,
    fontSize: 22,
    color: WHITE,
    bold: true,
    typeface: BODY,
    alignment: "center",
    verticalAlignment: "middle",
    fill: NAVY,
    line: { style: "solid", fill: NAVY, width: 0 },
    geometry: "roundRect",
    borderRadius: 14,
    insets: { top: 18, right: 28, bottom: 18, left: 28 },
  });

  // Slide 4: Contribution.
  const slide4 = presentation.slides.getItem(3);
  addHeader(slide4, "Contribution", "A working product: storefront and CRM operate as one system", 3);
  addText(slide4, {
    name: "applied-contribution-heading",
    text: "Applied contribution",
    left: 270,
    top: 185,
    width: 1400,
    height: 42,
    fontSize: 30,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
  });
  addText(slide4, {
    name: "applied-contribution-body",
    text: "TrainingIT combines a course marketplace and CRM in one Java web application. Each customer action is stored once and drives event-based workflows; the Claude AI layer remains optional.",
    left: 270,
    top: 235,
    width: 1400,
    height: 120,
    fontSize: 24,
    color: INK,
  });
  addRule(slide4, 270, 380, 1400, NAVY, 3);
  addText(slide4, {
    name: "objectives-heading",
    text: "Objectives",
    left: 270,
    top: 415,
    width: 650,
    height: 44,
    fontSize: 30,
    color: BRIGHT_BLUE,
    bold: true,
    typeface: DISPLAY,
  });
  addBulletList(slide4, {
    name: "objectives-list",
    items: [
      "Learner portal: browse, buy, review and book tutoring.",
      "Admin portal with seven management tabs.",
      "Event-driven domain automation.",
      "Optional, fault-tolerant AI assistant.",
      "Operational and CRM data in MariaDB.",
    ],
    left: 280,
    top: 480,
    width: 640,
    height: 360,
    fontSize: 23,
    gap: 14,
  });
  addText(slide4, {
    name: "boundary-heading",
    text: "Design boundary",
    left: 1010,
    top: 415,
    width: 650,
    height: 44,
    fontSize: 30,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
  });
  addText(slide4, {
    name: "boundary-body",
    text: "Not a general-purpose e-commerce platform or CRM.\n\nA focused training-company system: learner storefront plus a CRM a small team can run, with AI strictly additive.",
    left: 1010,
    top: 480,
    width: 650,
    height: 300,
    fontSize: 24,
    color: INK,
  });

  // Slide 5: System Architecture — centered and maximized for clarity.
  const slide5 = presentation.slides.getItem(4);
  addHeader(slide5, "System Architecture", "Four layers behind one REST API and a real-time event stream", 4);
  slide5.images.add({
    blob: await imageBytes(ARCHITECTURE_IMAGE),
    contentType: "image/png",
    alt: "TrainingIT system architecture showing browser, Spring Boot backend, Java domain layer, MariaDB and Anthropic Claude API",
    fit: "contain",
    position: { left: 465, top: 170, width: 990, height: 800 },
  });

  // Slide 6: AI Layer.
  const slide6 = presentation.slides.getItem(5);
  addHeader(slide6, "The AI Layer", "Optional intelligence, powered by the Anthropic Claude API", 5);
  const aiFeatures = [
    ["Conversational assistant", "/api/ai/chat", "Answers questions about courses, enrolment and corporate training using the live catalogue as context."],
    ["Recommendation quiz", "/api/ai/recommendations/visitor", "Matches a visitor's interests, level and goal to courses, each with a fit score and a reason."],
    ["Per-company recommendations", "/api/ai/recommendations/company", "Analyses employee roles and interests to propose a training programme for a whole team."],
    ["Live page translation", "/api/ai/translate", "Translates the interface while preserving brand names, people, e-mails and numbers."],
  ];
  aiFeatures.forEach(([heading, endpoint, body], index) => {
    const top = 185 + index * 164;
    addText(slide6, {
      name: `ai-feature-${index + 1}-heading`,
      text: heading,
      left: 270,
      top,
      width: 420,
      height: 34,
      fontSize: 26,
      color: NAVY,
      bold: true,
      typeface: DISPLAY,
    });
    addText(slide6, {
      name: `ai-feature-${index + 1}-endpoint`,
      text: endpoint,
      left: 690,
      top: top + 2,
      width: 350,
      height: 30,
      fontSize: 17,
      color: BRIGHT_BLUE,
      typeface: "Courier New",
      alignment: "right",
    });
    addText(slide6, {
      name: `ai-feature-${index + 1}-body`,
      text: body,
      left: 270,
      top: top + 45,
      width: 770,
      height: 78,
      fontSize: 21,
      color: INK,
    });
    if (index < 3) addRule(slide6, 270, top + 142, 770);
  });
  slide6.images.add({
    blob: await imageBytes(AI_SCREENSHOT),
    contentType: "image/png",
    alt: "TrainingIT course recommendation interface",
    fit: "contain",
    position: { left: 1110, top: 190, width: 570, height: 355 },
  });
  addText(slide6, {
    name: "ai-implementation-note",
    text: "All four features share one Claude client and one API key. The prompts, REST endpoints, structured JSON handling and matching interfaces are isolated so the application remains fully operational when AI is not configured.",
    left: 1110,
    top: 585,
    width: 570,
    height: 210,
    fontSize: 22,
    color: INK,
  });
  addText(slide6, {
    name: "ai-additive-callout",
    text: "Strictly additive: without an API key, AI controls disappear and the core application keeps working.",
    left: 270,
    top: 850,
    width: 1190,
    height: 60,
    fontSize: 21,
    color: WHITE,
    bold: true,
    typeface: BODY,
    alignment: "center",
    verticalAlignment: "middle",
    fill: NAVY,
    line: { style: "solid", fill: NAVY, width: 0 },
    geometry: "roundRect",
    borderRadius: 12,
    insets: { top: 8, right: 20, bottom: 8, left: 20 },
  });

  // Slide 7: Application Demo.
  const slide7 = presentation.slides.getItem(6);
  addHeader(slide7, "Application Demo", "Live application walkthrough", 6);
  addText(slide7, {
    name: "live-demo-label",
    text: "LIVE DEMO",
    left: 490,
    top: 360,
    width: 940,
    height: 120,
    fontSize: 76,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
    alignment: "center",
    verticalAlignment: "middle",
  });
  addText(slide7, {
    name: "live-demo-flow",
    text: "Storefront  →  CRM workflow  →  AI layer",
    left: 490,
    top: 505,
    width: 940,
    height: 55,
    fontSize: 28,
    color: MID_BLUE,
    typeface: BODY,
    alignment: "center",
    verticalAlignment: "middle",
  });

  // Slide 8: Conclusion.
  const slide8 = presentation.slides.getItem(7);
  addHeader(slide8, "Conclusion", "TrainingIT demonstrates one coherent storefront and CRM", 7);
  addText(slide8, {
    name: "implemented-heading",
    text: "Implemented",
    left: 270,
    top: 195,
    width: 670,
    height: 46,
    fontSize: 31,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
  });
  addBulletList(slide8, {
    name: "implemented-list",
    items: [
      "Learner portal: catalogue, purchase, reviews and tutoring.",
      "CRM portal: contacts, purchases, invoices, analytics and issues.",
      "Event-driven domain: automated lead scoring and invoicing.",
      "Live SSE statistics, imports and multi-format exports.",
      "Optional Claude assistant, recommendations and translation.",
    ],
    left: 280,
    top: 265,
    width: 660,
    height: 460,
    fontSize: 22,
    gap: 16,
  });
  addText(slide8, {
    name: "validated-heading",
    text: "Validated and next steps",
    left: 1010,
    top: 195,
    width: 670,
    height: 46,
    fontSize: 31,
    color: NAVY,
    bold: true,
    typeface: DISPLAY,
  });
  addBulletList(slide8, {
    name: "validated-list",
    items: [
      "End-to-end flows verified against a real MariaDB instance.",
      "Duplicate invoices prevented through idempotent processing.",
      "Observer failures are isolated; optional AI degrades safely.",
      "Next: automated tests and production-grade authentication.",
      "Next: richer analytics and cloud deployment.",
    ],
    left: 1020,
    top: 265,
    width: 660,
    height: 460,
    fontSize: 22,
    gap: 16,
  });
  addText(slide8, {
    name: "conclusion-takeaway",
    text: "Each customer action is recorded once and becomes immediately available to the business.",
    left: 270,
    top: 810,
    width: 1190,
    height: 92,
    fontSize: 23,
    color: WHITE,
    bold: true,
    typeface: BODY,
    alignment: "center",
    verticalAlignment: "middle",
    fill: NAVY,
    line: { style: "solid", fill: NAVY, width: 0 },
    geometry: "roundRect",
    borderRadius: 14,
    insets: { top: 12, right: 24, bottom: 12, left: 24 },
  });

  for (let index = 0; index < presentation.slides.items.length; index += 1) {
    const slide = presentation.slides.items[index];
    const stem = `slide-${String(index + 1).padStart(2, "0")}`;
    await saveBlob(path.join(RENDER_DIR, `${stem}.png`), await presentation.export({ slide, format: "png", scale: 1 }));
    await fs.writeFile(path.join(RENDER_DIR, `${stem}.layout.json`), await (await slide.export({ format: "layout" })).text(), "utf8");
  }

  await saveBlob(path.join(BUILD_DIR, "final-montage.webp"), await presentation.export({ format: "webp", montage: true, scale: 1 }));
  const finalInspect = await presentation.inspect({
    kind: "slide,textbox,shape,image,table,chart,notes,thread,layout",
    include: "id,slide,name,title,text,textPreview,bbox,rows,cols,preview,isPlaceholder,placeholders",
    maxChars: 200000,
  });
  await fs.writeFile(path.join(BUILD_DIR, "final-inspect.ndjson"), finalInspect.ndjson || "", "utf8");

  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(OUTPUT);
  console.log(OUTPUT);
}

build().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
