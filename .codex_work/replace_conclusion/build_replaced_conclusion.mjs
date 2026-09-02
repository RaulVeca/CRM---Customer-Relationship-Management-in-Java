import fs from "node:fs/promises";
import path from "node:path";
import JSZip from "jszip";
import { FileBlob, Presentation, PresentationFile } from "@oai/artifact-tool";

const primaryPath = "D:\\TrainingIT_site\\.codex_work\\replace_conclusion\\template-starter.pptx";
const primaryThemePath = "C:\\Users\\raulv\\Downloads\\5 min_fara_note.pptx";
const secondaryPath = "D:\\TrainingIT_site\\Powerpoint_Presentation_5_minute.pptx";
const outputPath = "D:\\TrainingIT_site\\5 min_fara_note_conclusion.pptx";
const previewDir = "D:\\TrainingIT_site\\.codex_work\\replace_conclusion\\final-preview";
const layoutDir = "D:\\TrainingIT_site\\.codex_work\\replace_conclusion\\final-layout";
const montagePath = "D:\\TrainingIT_site\\.codex_work\\replace_conclusion\\final-montage.webp";
const inspectPath = "D:\\TrainingIT_site\\.codex_work\\replace_conclusion\\final-inspect.ndjson";

async function writeBlob(filePath, blob) {
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

function replacePageMarker(slideProto) {
  let replacements = 0;
  for (const element of slideProto.elements ?? []) {
    for (const paragraph of element.paragraphs ?? []) {
      for (const run of paragraph.runs ?? []) {
        if (run.text === "Page 6 / 6") {
          run.text = "Page 7 / 7";
          replacements += 1;
        }
      }
    }
  }
  if (replacements !== 1) {
    throw new Error(`Expected one Page 6 / 6 marker on the source Conclusion slide; found ${replacements}.`);
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

const primary = await PresentationFile.importPptx(await FileBlob.load(primaryPath));
const secondary = await PresentationFile.importPptx(await FileBlob.load(secondaryPath));
const primaryProto = structuredClone(primary.toProto());
const secondaryProto = secondary.toProto();

if (primaryProto.slides.length !== 8) {
  throw new Error(`Expected 8 slides in the primary deck; found ${primaryProto.slides.length}.`);
}
if (secondaryProto.slides.length !== 7) {
  throw new Error(`Expected 7 slides in the secondary deck; found ${secondaryProto.slides.length}.`);
}

const oldFinalSlide = primaryProto.slides[7];
const replacement = structuredClone(secondaryProto.slides[6]);
replacement.id = oldFinalSlide.id;
replacement.index = 7;
replacement.creationId = oldFinalSlide.creationId;
replacement.notesSlide = structuredClone(oldFinalSlide.notesSlide);
replacePageMarker(replacement);
primaryProto.slides[7] = replacement;

const presentation = Presentation.load(primaryProto);
presentation.slides.items[7].speakerNotes.clear();
presentation.slides.items[7].speakerNotes.setVisible(false);

for (const [index, slide] of presentation.slides.items.entries()) {
  const stem = `slide-${String(index + 1).padStart(2, "0")}`;
  await writeBlob(path.join(previewDir, `${stem}.png`), await presentation.export({ slide, format: "png", scale: 2 }));
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(path.join(layoutDir, `${stem}.layout.json`), await layout.text());
}

await writeBlob(montagePath, await presentation.export({ format: "webp", montage: true, scale: 1 }));
const inspection = await presentation.inspect({
  kind: "slide,textbox,shape,image,notes,layout",
  include: "id,slide,name,title,text,textPreview,textChars,bbox,isPlaceholder",
  maxChars: 40000,
});
await fs.writeFile(inspectPath, inspection.ndjson);

const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(outputPath);
await restoreThemeParts(primaryThemePath, outputPath);
process.stdout.write(`${outputPath}\n`);
