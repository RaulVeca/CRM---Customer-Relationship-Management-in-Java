import fs from "node:fs/promises";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const buildDir = "D:/TrainingIT_site/.codex-pptx-contribution-innovation";
const inputPptx = `${buildDir}/template-starter.pptx`;
const outputPptx = `${buildDir}/artifact-edited.pptx`;
const oldText = "TrainingIT combines a course marketplace and CRM in one Java web application. Each customer action is stored once and drives event-based workflows; the Claude AI layer remains optional.";
const newText = `${oldText} Major market innovations include real-time commerce-to-CRM automation, training-specific workflows from lead scoring to invoicing, and explainable recommendations for learners and corporate teams.`;

async function writeBlob(path, blob) {
  await fs.writeFile(path, new Uint8Array(await blob.arrayBuffer()));
}

const presentation = await PresentationFile.importPptx(await FileBlob.load(inputPptx));
const before = await presentation.inspect({
  kind: "slide,textbox,shape,layout",
  search: "TrainingIT combines",
  maxChars: 6000,
});
await fs.writeFile(`${buildDir}/artifact-before-inspect.ndjson`, before.ndjson);

const targetRecord = before.ndjson
  .split(/\r?\n/)
  .filter(Boolean)
  .map((line) => JSON.parse(line))
  .find((record) => record.kind === "textbox" && record.name === "applied-contribution-body");
if (!targetRecord?.id) {
  throw new Error("Could not resolve the applied-contribution-body text box.");
}
const target = presentation.resolve(targetRecord.id);
const contributionSlide = presentation.slides.getItem(3);
await writeBlob(
  `${buildDir}/artifact-before-slide-04.png`,
  await presentation.export({ slide: contributionSlide, format: "png", scale: 2 }),
);
await fs.writeFile(
  `${buildDir}/artifact-before-slide-04.layout.json`,
  await (await contributionSlide.export({ format: "layout" })).text(),
);

target.text.replace(oldText, newText);

await writeBlob(
  `${buildDir}/artifact-after-slide-04.png`,
  await presentation.export({ slide: contributionSlide, format: "png", scale: 2 }),
);
await fs.writeFile(
  `${buildDir}/artifact-after-slide-04.layout.json`,
  await (await contributionSlide.export({ format: "layout" })).text(),
);
await writeBlob(
  `${buildDir}/artifact-after-montage.webp`,
  await presentation.export({ format: "webp", montage: true, scale: 1 }),
);

const after = await presentation.inspect({
  kind: "slide,textbox,shape,layout",
  search: "Major market innovations",
  maxChars: 6000,
});
await fs.writeFile(`${buildDir}/artifact-after-inspect.ndjson`, after.ndjson);

const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(outputPptx);
console.log(outputPptx);
