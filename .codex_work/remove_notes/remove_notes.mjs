import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const starter = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\template-starter.pptx";
const output = "D:\\TrainingIT_site\\5 min_fara_note.pptx";
const previewDir = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\final-preview";
const layoutDir = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\final-layout";
const montagePath = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\final-montage.webp";
const inspectPath = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\final-notes-inspect.ndjson";

async function writeBlob(filePath, blob) {
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

await fs.mkdir(previewDir, { recursive: true });
await fs.mkdir(layoutDir, { recursive: true });

const presentation = await PresentationFile.importPptx(await FileBlob.load(starter));

for (const slide of presentation.slides.items) {
  slide.speakerNotes.clear();
  slide.speakerNotes.setVisible(false);
}

for (const [index, slide] of presentation.slides.items.entries()) {
  const stem = `slide-${String(index + 1).padStart(2, "0")}`;
  const preview = await presentation.export({ slide, format: "png", scale: 2 });
  await writeBlob(path.join(previewDir, `${stem}.png`), preview);
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(path.join(layoutDir, `${stem}.layout.json`), await layout.text());
}

const montage = await presentation.export({ format: "webp", montage: true, scale: 1 });
await writeBlob(montagePath, montage);

const noteInspection = await presentation.inspect({
  kind: "slide,notes",
  include: "id,slide,title,text,textPreview,textChars",
  maxChars: 20000,
});
await fs.writeFile(inspectPath, noteInspection.ndjson);

const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(output);
process.stdout.write(`${output}\n`);
