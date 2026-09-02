import fs from "node:fs/promises";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const source = "D:\\TrainingIT_site\\.codex_artifacts\\presentation_5min\\Powerpoint_Presentation.pptx";
const output = "D:\\TrainingIT_site\\.codex_artifacts\\presentation_5min\\source_detail\\api-help.txt";
const presentation = await PresentationFile.importPptx(await FileBlob.load(source));
const shape = presentation.resolve("sh/zi98nu94");
const image = presentation.resolve("im/zax8jq94");
const slide = presentation.resolve("sl/x8f69ofe");
const ownAndPrototype = (value) => ({
  own: Object.getOwnPropertyNames(value).sort(),
  prototype: Object.getOwnPropertyNames(Object.getPrototypeOf(value)).sort(),
});
const results = {
  shape: ownAndPrototype(shape),
  image: ownAndPrototype(image),
  imageValues: {
    frame: image.frame,
    position: image.position,
    size: image.size,
    width: image.width,
    height: image.height,
    fit: image.fit,
    crop: image.crop,
    lockAspectRatio: image.lockAspectRatio,
  },
  slide: ownAndPrototype(slide),
  shapesCollection: ownAndPrototype(slide.shapes),
  imagesCollection: ownAndPrototype(slide.images),
  elementsCollection: slide.elements ? ownAndPrototype(slide.elements) : null,
};
await fs.writeFile(output, JSON.stringify(results, null, 2), "utf8");
console.log(output);
