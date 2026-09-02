import fs from "node:fs/promises";
import path from "node:path";
import JSZip from "jszip";
import { FileBlob, Presentation, PresentationFile } from "@oai/artifact-tool";

const starterPath = "D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\template-starter.pptx";
const sourcePath = "C:\\Users\\raulv\\Downloads\\5 min.pptx";
const outputPath = "D:\\TrainingIT_site\\5 min_contribution_scurt.pptx";
const previewDir = "D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\final-preview";
const layoutDir = "D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\final-layout";
const montagePath = "D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\final-montage.webp";
const inspectPath = "D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\final-inspect.ndjson";

async function writeBlob(filePath, blob) {
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

function getElement(slide, id) {
  const element = (slide.elements ?? []).find((item) => String(item.id) === String(id));
  if (!element) throw new Error(`Missing slide element ${id}.`);
  return element;
}

function replaceSingleParagraph(element, text) {
  if ((element.paragraphs ?? []).length !== 1) {
    throw new Error(`Expected one paragraph in element ${element.id}.`);
  }
  const paragraph = element.paragraphs[0];
  if ((paragraph.runs ?? []).length < 1) {
    throw new Error(`Expected at least one text run in element ${element.id}.`);
  }
  paragraph.runs[0].text = text;
  paragraph.runs = [paragraph.runs[0]];
}

function replaceParagraphs(element, texts) {
  const paragraphs = element.paragraphs ?? [];
  if (paragraphs.length < texts.length) {
    throw new Error(`Element ${element.id} has too few paragraphs.`);
  }
  for (let index = 0; index < texts.length; index += 1) {
    const paragraph = paragraphs[index];
    if ((paragraph.runs ?? []).length < 1) {
      throw new Error(`Expected a text run in paragraph ${index + 1} of element ${element.id}.`);
    }
    paragraph.runs[0].text = texts[index];
    paragraph.runs = [paragraph.runs[0]];
  }
  element.paragraphs = paragraphs.slice(0, texts.length);
}

function replaceSeparatedParagraphs(element, texts) {
  const paragraphs = element.paragraphs ?? [];
  if (paragraphs.length !== 3 || (paragraphs[1].runs ?? []).length !== 0) {
    throw new Error(`Expected two text paragraphs separated by one blank paragraph in element ${element.id}.`);
  }
  for (const [paragraphIndex, textIndex] of [[0, 0], [2, 1]]) {
    const paragraph = paragraphs[paragraphIndex];
    if ((paragraph.runs ?? []).length < 1) {
      throw new Error(`Expected a text run in paragraph ${paragraphIndex + 1} of element ${element.id}.`);
    }
    paragraph.runs[0].text = texts[textIndex];
    paragraph.runs = [paragraph.runs[0]];
  }
}

async function restoreThemeParts(referencePath, editedPath) {
  const referenceZip = await JSZip.loadAsync(await fs.readFile(referencePath));
  const editedZip = await JSZip.loadAsync(await fs.readFile(editedPath));
  const themeNames = Object.keys(referenceZip.files)
    .filter((name) => /^ppt\/theme\/theme\d+\.xml$/.test(name))
    .sort();

  for (const name of themeNames) {
    const bytes = await referenceZip.file(name).async("uint8array");
    editedZip.file(name, bytes, { binary: true });
  }

  const restored = await editedZip.generateAsync({
    type: "nodebuffer",
    compression: "DEFLATE",
    compressionOptions: { level: 6 },
  });
  await fs.writeFile(editedPath, restored);
}

await fs.mkdir(previewDir, { recursive: true });
await fs.mkdir(layoutDir, { recursive: true });

const imported = await PresentationFile.importPptx(await FileBlob.load(starterPath));
const proto = structuredClone(imported.toProto());
if (proto.slides.length !== 8) {
  throw new Error(`Expected 8 slides; found ${proto.slides.length}.`);
}

const contribution = proto.slides[3];
replaceSingleParagraph(
  getElement(contribution, 13),
  "TrainingIT combines a course marketplace and a complete CRM in one working application, built on an event-driven Java core with optional Claude-powered features.",
);
replaceParagraphs(getElement(contribution, 17), [
  "A learner portal for courses, purchases, reviews and tutoring.",
  "A seven-tab CRM portal for daily operations.",
  "An event-driven Java domain with automatic reactions.",
  "MariaDB persistence and optional AI that degrades safely.",
]);
replaceSeparatedParagraphs(getElement(contribution, 21), [
  "Not a general-purpose marketplace or CRM.",
  "TrainingIT is built for training companies: a simple learner storefront over a practical CRM core. AI remains optional.",
]);

const presentation = Presentation.load(proto);

for (const [index, slide] of presentation.slides.items.entries()) {
  const stem = `slide-${String(index + 1).padStart(2, "0")}`;
  await writeBlob(
    path.join(previewDir, `${stem}.png`),
    await presentation.export({ slide, format: "png", scale: 2 }),
  );
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(path.join(layoutDir, `${stem}.layout.json`), await layout.text());
}

await writeBlob(
  montagePath,
  await presentation.export({ format: "webp", montage: true, scale: 1 }),
);
const inspection = await presentation.inspect({
  kind: "slide,textbox,shape,image,table,chart,notes,layout",
  include: "id,slide,name,title,text,textPreview,textChars,bbox,isPlaceholder",
  maxChars: 50000,
});
await fs.writeFile(inspectPath, inspection.ndjson);

const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(outputPath);
await restoreThemeParts(sourcePath, outputPath);
process.stdout.write(`${outputPath}\n`);
