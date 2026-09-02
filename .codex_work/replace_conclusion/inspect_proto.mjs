import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const target = await PresentationFile.importPptx(await FileBlob.load("C:\\Users\\raulv\\Downloads\\5 min_fara_note.pptx"));
const source = await PresentationFile.importPptx(await FileBlob.load("D:\\TrainingIT_site\\Powerpoint_Presentation_5_minute.pptx"));

function brief(presentation, label) {
  const proto = presentation.toProto();
  const slide = presentation.slides.items.at(-1);
  const slideProto = slide.toProto?.();
  return {
    label,
    presentationKeys: Object.keys(proto),
    slideCount: presentation.slides.items.length,
    protoSlideArrayKeys: Object.entries(proto).filter(([, value]) => Array.isArray(value)).map(([key, value]) => [key, value.length]),
    slideFacadeKeys: Object.keys(slide),
    slideProtoKeys: slideProto ? Object.keys(slideProto) : null,
    slideProto,
    layoutCount: presentation.layouts.items.length,
    masterCount: presentation.masters.items.length,
    layoutIds: presentation.layouts.items.map((x) => x.id),
    masterIds: presentation.masters.items.map((x) => x.id),
  };
}

process.stdout.write(JSON.stringify({ target: brief(target, "target"), source: brief(source, "source") }, null, 2));
