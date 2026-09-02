import fs from "node:fs/promises";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const SOURCE = "D:/TrainingIT_site/.codex-pptx-center-video/template-starter.pptx";
const OUT = "D:/TrainingIT_site/.codex-pptx-center-video/artifact-preview.pptx";
const RENDER = "D:/TrainingIT_site/.codex-pptx-center-video/artifact-preview-slide-07.png";
const LAYOUT = "D:/TrainingIT_site/.codex-pptx-center-video/artifact-preview-slide-07.layout.json";

async function writeBlob(path, blob) {
  await fs.writeFile(path, new Uint8Array(await blob.arrayBuffer()));
}

const presentation = await PresentationFile.importPptx(await FileBlob.load(SOURCE));
const snapshot = await presentation.inspect({
  kind: "image",
  include: "id,slide,name,bbox",
  maxChars: 20000,
});
const images = snapshot.ndjson
  .split(/\r?\n/)
  .filter(Boolean)
  .map((line) => JSON.parse(line));
const videoRecord = images.find((record) => record.slide === 7 && record.name === "In Powerpoint");
if (!videoRecord) throw new Error("Embedded video poster was not found on slide 7.");

const videoPoster = presentation.resolve(videoRecord.id);
videoPoster.frame = { left: 320, top: 180, width: 1280, height: 720 };

const slide = presentation.slides.getItem(6);
await writeBlob(RENDER, await presentation.export({ slide, format: "png", scale: 2 }));
await fs.writeFile(LAYOUT, await (await slide.export({ format: "layout" })).text());

const exported = await PresentationFile.exportPptx(presentation);
await exported.save(OUT);
console.log(JSON.stringify({ targetId: videoRecord.id, frame: videoPoster.frame, output: OUT }));
