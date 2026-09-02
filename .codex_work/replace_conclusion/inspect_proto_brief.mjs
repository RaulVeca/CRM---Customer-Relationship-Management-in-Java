import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const target = await PresentationFile.importPptx(await FileBlob.load("C:\\Users\\raulv\\Downloads\\5 min_fara_note.pptx"));
const source = await PresentationFile.importPptx(await FileBlob.load("D:\\TrainingIT_site\\Powerpoint_Presentation_5_minute.pptx"));

function slideText(slideProto) {
  return (slideProto.elements ?? [])
    .flatMap((element) => element.paragraphs ?? [])
    .flatMap((paragraph) => paragraph.runs ?? [])
    .map((run) => run.text ?? "")
    .join(" | ")
    .slice(0, 220);
}

function summarize(presentation) {
  const proto = presentation.toProto();
  return {
    slides: proto.slides.map((slide) => ({
      id: slide.id,
      index: slide.index,
      useLayoutId: slide.useLayoutId,
      elementCount: slide.elements?.length ?? 0,
      imageRefs: [...new Set((slide.elements ?? []).map((e) => e.imageReference?.id).filter(Boolean))],
      titleText: slideText(slide),
      noteText: (slide.notesSlide?.elements ?? []).flatMap((e) => e.paragraphs ?? []).flatMap((p) => p.runs ?? []).map((r) => r.text ?? "").join(" | ").slice(0, 180),
    })),
    layouts: proto.layouts.map((layout) => ({ id: layout.id, parentLayoutId: layout.parentLayoutId, type: layout.type })),
    images: proto.images.map((image) => ({ id: image.id, mimeType: image.mimeType, bytes: image.data?.length ?? image.bytes?.length ?? null })),
  };
}

process.stdout.write(JSON.stringify({ target: summarize(target), source: summarize(source) }, null, 2));
