import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const buildDir = "D:\\TrainingIT_site\\.codex_work\\requested_final_deck";
const inputPptx = path.join(buildDir, "template-starter.pptx");
const outputPptx = "D:\\TrainingIT_site\\Powerpoint_Presentation_final.pptx";
const previewDir = path.join(buildDir, "final-preview");
const layoutDir = path.join(buildDir, "final-layout");

async function writeBlob(filePath, blob) {
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

function parseNdjson(ndjson) {
  return ndjson
    .split(/\r?\n/)
    .filter((line) => line.trim().startsWith("{"))
    .map((line) => JSON.parse(line));
}

async function main() {
  await fs.mkdir(previewDir, { recursive: true });
  await fs.mkdir(layoutDir, { recursive: true });

  const presentation = await PresentationFile.importPptx(
    await FileBlob.load(inputPptx),
  );
  const before = await presentation.inspect({
    kind: "slide,textbox,shape,image,notes,layout",
    include: "id,slide,name,title,text,bbox,isPlaceholder",
    maxChars: 80000,
  });
  const records = parseNdjson(before.ndjson);

  function resolveRecord({ slide, name, text, kind }) {
    const record = records.find((item) =>
      (slide === undefined || item.slide === slide) &&
      (name === undefined || item.name === name) &&
      (kind === undefined || item.kind === kind) &&
      (text === undefined || (typeof item.text === "string" && item.text.includes(text))),
    );
    if (!record) throw new Error(`Target not found: ${JSON.stringify({ slide, name, text, kind })}`);
    return presentation.resolve(record.id);
  }

  function replaceText({ slide, oldText, newText }) {
    const target = resolveRecord({ slide, text: oldText, kind: "textbox" });
    target.text.replace(oldText, newText);
  }

  // Slide 2 - exact approved shortened copy from the five-minute deck.
  replaceText({
    slide: 2,
    oldText: "One storefront and one back office - normally two different systems",
    newText: "A storefront and a CRM should not depend on fragile synchronization",
  });
  replaceText({
    slide: 2,
    oldText: "A learner expects what any modern shop offers: a clear catalogue, prices and levels, reviews they can trust, a purchase in one click, and a private tutoring session booked at a time that suits them.",
    newText: "Learners need a clear catalogue, trusted reviews, quick purchase and simple tutoring reservations.",
  });
  replaceText({
    slide: 2,
    oldText: "Behind the storefront the company must know who its leads are and how close each is to buying, keep a reliable purchase history, issue correct invoices with negotiated discounts, read the health of the business, and handle corporate clients who send whole teams.",
    newText: "The company needs leads, purchases, invoices, analytics and corporate clients in one operational view.",
  });
  replaceText({
    slide: 2,
    oldText: "These two sides are normally separate tools that must be synchronised. Every synchronisation point is a place where data can drift, and manual reconciliation is slow and error-prone.",
    newText: "Separate tools create duplicate or inconsistent data and require slow manual reconciliation.",
  });
  replaceText({
    slide: 2,
    oldText: "Build a single web application in which the customer-facing storefront and the business-facing CRM are not two integrated systems but one system - so that every customer action is captured exactly once and automatically drives enrolment, invoicing, lead scoring, auditing and support.",
    newText: "Use one application and one shared data model so each customer action is captured once and drives the relevant CRM processes automatically.",
  });
  replaceText({
    slide: 2,
    oldText: "It must also stay approachable for non-technical staff, keep the two roles strictly separated, and remain fully functional when optional services are switched off.",
    newText: "One system. Two role-based experiences. No manual reconciliation.",
  });

  // Content-slide page markers.
  const pageMarkers = [
    [2, "Page 1 / 13", "Page 1 / 7"],
    [3, "Page 2 / 13", "Page 2 / 7"],
    [4, "Page 3 / 13", "Page 3 / 7"],
    [5, "Page 5 / 13", "Page 4 / 7"],
    [6, "Page 10 / 13", "Page 5 / 7"],
    [7, "Page 12 / 13", "Page 6 / 7"],
    [8, "Page 13 / 13", "Page 7 / 7"],
  ];
  for (const [slide, oldText, newText] of pageMarkers) {
    replaceText({ slide, oldText, newText });
  }

  // Slide 5 - enlarged architecture diagram on the inherited template.
  replaceText({
    slide: 5,
    oldText: "Application Architecture",
    newText: "System Architecture",
  });
  for (const name of ["Shape 7", "Text 8", "Shape 9", "Text 10", "Shape 11", "Text 12", "Shape 13", "Text 14"]) {
    resolveRecord({ slide: 5, name }).delete();
  }
  const architecture = resolveRecord({ slide: 5, name: "Image 1", kind: "image" });
  architecture.lockAspectRatio = false;
  architecture.position = { left: 64, top: 105, width: 832, height: 360 };

  const sourceBlocks = [
    "Powerpoint_Presentation.pptx, source slide 1",
    "Powerpoint_Presentation_5_minute.pptx, approved slide 2 copy; Powerpoint_Presentation.pptx, source slide 2 template",
    "Powerpoint_Presentation.pptx, source slide 3",
    "Powerpoint_Presentation.pptx, source slide 4",
    "Powerpoint_Presentation.pptx, source slide 6",
    "Powerpoint_Presentation.pptx, source slide 11",
    "Powerpoint_Presentation.pptx, source slide 14 video poster",
    "Powerpoint_Presentation.pptx, source slide 15",
  ];
  for (let i = 0; i < presentation.slides.items.length; i += 1) {
    const slide = presentation.slides.items[i];
    slide.speakerNotes.append(`\n\n[Sources]\n- ${sourceBlocks[i]}`);
    slide.speakerNotes.setVisible(true);
  }

  const after = await presentation.inspect({
    kind: "deck,slide,textbox,shape,image,table,chart,notes,layout",
    include: "id,slide,name,title,text,textPreview,textChars,textLines,bbox,isPlaceholder",
    maxChars: 100000,
  });
  await fs.writeFile(path.join(buildDir, "final-inspect.ndjson"), after.ndjson, "utf8");

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
