import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const source = process.argv[2] ?? "C:\\Users\\raulv\\Downloads\\5 min.pptx";
const presentation = await PresentationFile.importPptx(await FileBlob.load(source));
const result = await presentation.inspect({
  kind: "slide,notes",
  include: "id,slide,title,text,textPreview,textChars",
  maxChars: 20000,
});
process.stdout.write(result.ndjson);
