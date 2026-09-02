import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const finalPath = "D:\\TrainingIT_site\\5 min_fara_note_conclusion.pptx";
const presentation = await PresentationFile.importPptx(await FileBlob.load(finalPath));
const inspection = await presentation.inspect({
  kind: "slide,notes",
  include: "id,slide,title,text,textPreview,textChars",
  maxChars: 20000,
});
process.stdout.write(inspection.ndjson);
