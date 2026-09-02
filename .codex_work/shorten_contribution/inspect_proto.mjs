import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const deck = await PresentationFile.importPptx(
  await FileBlob.load("D:\\TrainingIT_site\\.codex_work\\shorten_contribution\\template-starter.pptx"),
);
const proto = deck.toProto();
const slide = proto.slides[3];
for (const element of slide.elements ?? []) {
  const text = (element.paragraphs ?? [])
    .flatMap((paragraph) => paragraph.runs ?? [])
    .map((run) => run.text ?? "")
    .join(" | ");
  if (text) {
    process.stdout.write(`${element.id ?? ""}\t${element.name ?? ""}\t${text}\n`);
  }
}
