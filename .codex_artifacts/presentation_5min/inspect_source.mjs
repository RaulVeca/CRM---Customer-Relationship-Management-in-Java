import fs from "node:fs/promises";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const source = "D:\\TrainingIT_site\\.codex_artifacts\\presentation_5min\\Powerpoint_Presentation.pptx";
const outDir = "D:\\TrainingIT_site\\.codex_artifacts\\presentation_5min\\source_detail";
await fs.mkdir(outDir, { recursive: true });

const presentation = await PresentationFile.importPptx(await FileBlob.load(source));
const snapshot = await presentation.inspect({
  kind: "slide,textbox,shape,image,table,chart,notes,layout",
  include: "id,slide,name,title,text,textPreview,textChars,textLines,bbox,bboxUnit,rows,cols,preview,isPlaceholder,placeholders",
  maxChars: 400000,
});
await fs.writeFile(`${outDir}\\source-inspect.ndjson`, snapshot.ndjson || "", "utf8");

const layouts = await presentation.inspect({ kind: "layout", maxChars: 200000 });
await fs.writeFile(`${outDir}\\source-layouts.ndjson`, layouts.ndjson || "", "utf8");

const summary = {
  slideCount: presentation.slides.items.length,
  masterCount: presentation.masters?.items?.length ?? 0,
  layoutCount: presentation.layouts?.items?.length ?? 0,
  masters: (presentation.masters?.items || []).map((master) => ({
    id: master.id,
    name: master.name,
    placeholders: master.placeholders?.summary?.() ?? null,
  })),
  layouts: (presentation.layouts?.items || []).map((layout) => ({
    id: layout.id,
    name: layout.name,
    parentLayoutId: layout.parentLayoutId,
    placeholders: layout.placeholders?.summary?.() ?? null,
  })),
};
await fs.writeFile(`${outDir}\\structure.json`, JSON.stringify(summary, null, 2), "utf8");
console.log(`${outDir}\\source-inspect.ndjson`);
