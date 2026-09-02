import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, PresentationFile } from "@oai/artifact-tool";

const BUILD_DIR = "D:\\TrainingIT_site\\.codex-pptx-edit";
const INPUT = path.join(BUILD_DIR, "template-starter.pptx");
const OUTPUT = "D:\\TrainingIT_site\\5 min - contribution scurtat.pptx";
const RENDER_DIR = path.join(BUILD_DIR, "final-render");

async function saveBlob(filePath, blob) {
  await fs.mkdir(path.dirname(filePath), { recursive: true });
  await fs.writeFile(filePath, new Uint8Array(await blob.arrayBuffer()));
}

function records(snapshot) {
  return (snapshot.ndjson || "")
    .split(/\r?\n/)
    .filter(Boolean)
    .map((line) => JSON.parse(line));
}

async function findTextShape(presentation, needle) {
  const snapshot = await presentation.inspect({
    kind: "textbox,shape",
    include: "id,slide,name,text,textPreview,bbox",
    search: needle,
    maxChars: 20000,
  });
  const hit = records(snapshot).find(
    (record) => record.slide === 4 && typeof record.text === "string" && record.text.includes(needle),
  );
  if (!hit?.id) throw new Error(`Could not locate slide 4 text: ${needle}`);
  return presentation.resolve(hit.id);
}

async function main() {
  await fs.mkdir(RENDER_DIR, { recursive: true });
  const presentation = await PresentationFile.importPptx(await FileBlob.load(INPUT));
  const slide = presentation.slides.getItem(3);

  await saveBlob(
    path.join(BUILD_DIR, "before-contribution.png"),
    await presentation.export({ slide, format: "png", scale: 2 }),
  );
  await fs.writeFile(
    path.join(BUILD_DIR, "before-contribution.layout.json"),
    await (await slide.export({ format: "layout" })).text(),
    "utf8",
  );

  const applied = await findTextShape(
    presentation,
    "TrainingIT is a functional web application",
  );
  applied.text.replace(
    "TrainingIT is a functional web application: a course marketplace whose every customer action is recorded exactly once in a complete CRM back office, on a Java domain layer organised around classic design patterns and an event-driven core, with an optional Claude-powered AI layer.",
    "TrainingIT combines a course marketplace and CRM in one Java web application. Each customer action is stored once and drives event-based workflows; the Claude AI layer remains optional.",
  );

  const objectives = await findTextShape(
    presentation,
    "A public client portal: browse, purchase, review",
  );
  const objectiveRewrites = [
    [
      "A public client portal: browse, purchase, review, book one-to-one tutoring.",
      "Learner portal: browse, buy, review and book tutoring.",
    ],
    [
      "An administrator portal with seven management tabs.",
      "Admin portal with seven management tabs.",
    ],
    [
      "A domain layer on design patterns and an event-driven core, so one action propagates into several automatic reactions.",
      "Event-driven domain automation.",
    ],
    [
      "An optional AI assistant that degrades gracefully.",
      "Optional, fault-tolerant AI assistant.",
    ],
    [
      "All operational and CRM data persisted in MariaDB.",
      "Operational and CRM data in MariaDB.",
    ],
  ];
  for (const [before, after] of objectiveRewrites) objectives.text.replace(before, after);

  const boundary = await findTextShape(
    presentation,
    "The goal is not a general-purpose e-commerce platform",
  );
  boundary.text.replace(
    "The goal is not a general-purpose e-commerce platform, nor a general-purpose CRM.",
    "Not a general-purpose e-commerce platform or CRM.",
  );
  boundary.text.replace(
    "The goal is one coherent product for a training company: a storefront a learner can use without instructions, over a CRM core a small team can actually run - where the AI layer is strictly additive and never a dependency.",
    "A focused training-company system: learner storefront plus a CRM a small team can run, with AI strictly additive.",
  );

  await saveBlob(
    path.join(BUILD_DIR, "after-contribution.png"),
    await presentation.export({ slide, format: "png", scale: 2 }),
  );
  await fs.writeFile(
    path.join(BUILD_DIR, "after-contribution.layout.json"),
    await (await slide.export({ format: "layout" })).text(),
    "utf8",
  );

  for (let index = 0; index < presentation.slides.items.length; index += 1) {
    const current = presentation.slides.items[index];
    const stem = `slide-${String(index + 1).padStart(2, "0")}`;
    await saveBlob(
      path.join(RENDER_DIR, `${stem}.png`),
      await presentation.export({ slide: current, format: "png", scale: 2 }),
    );
    await fs.writeFile(
      path.join(RENDER_DIR, `${stem}.layout.json`),
      await (await current.export({ format: "layout" })).text(),
      "utf8",
    );
  }

  await saveBlob(
    path.join(BUILD_DIR, "final-montage.webp"),
    await presentation.export({ format: "webp", montage: true, scale: 1 }),
  );

  const finalInspect = await presentation.inspect({
    kind: "slide,textbox,shape,image,table,chart,notes,thread,layout",
    include: "id,slide,name,title,text,textPreview,bbox,rows,cols,preview,isPlaceholder,placeholders",
    maxChars: 200000,
  });
  await fs.writeFile(path.join(BUILD_DIR, "final-inspect.ndjson"), finalInspect.ndjson || "", "utf8");

  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(OUTPUT);
  console.log(OUTPUT);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
