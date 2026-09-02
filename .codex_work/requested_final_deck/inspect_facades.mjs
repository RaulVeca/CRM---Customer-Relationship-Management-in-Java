import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const presentation = await PresentationFile.importPptx(
  await FileBlob.load("C:\\Users\\raulv\\Downloads\\Powerpoint_Presentation.pptx"),
);
const snapshot = await presentation.inspect({
  kind: "slide,textbox,shape,image",
  search: "Presentation - Next.js 16",
  include: "id,slide,name,text,bbox",
  maxChars: 5000,
});
const records = snapshot.ndjson.split(/\r?\n/).filter((line) => line.startsWith("{")).map(JSON.parse);
const record = records.find((item) => item.kind === "textbox" && item.text?.includes("Presentation - Next.js 16"));
const target = presentation.resolve(record.id);
const imageSnapshot = await presentation.inspect({ kind: "image", include: "id,slide,name,bbox", maxChars: 10000 });
const images = imageSnapshot.ndjson.split(/\r?\n/).filter((line) => line.startsWith("{")).map(JSON.parse);
const imageRecord = images.find((item) => item.slide === 6 && item.name === "Image 1");
const image = presentation.resolve(imageRecord.id);
const slide = presentation.slides.items[5];
function methods(value) {
  const out = new Set();
  let cursor = value;
  while (cursor && cursor !== Object.prototype) {
    for (const name of Object.getOwnPropertyNames(cursor)) out.add(name);
    cursor = Object.getPrototypeOf(cursor);
  }
  return [...out].sort();
}
console.log(JSON.stringify({
  targetMethods: methods(target),
  imageMethods: methods(image),
  shapesMethods: methods(slide.shapes),
  slideMethods: methods(slide),
  imageFrame: image.frame,
  imagePosition: image.position,
}, null, 2));
