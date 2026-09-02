import fs from "node:fs/promises";
import JSZip from "jszip";

const sourcePath = "C:\\Users\\raulv\\Downloads\\5 min.pptx";
const finalPath = "D:\\TrainingIT_site\\5 min_fara_note.pptx";
const tempPath = "D:\\TrainingIT_site\\.codex_work\\remove_notes\\theme-restored.pptx";

const sourceZip = await JSZip.loadAsync(await fs.readFile(sourcePath));
const finalZip = await JSZip.loadAsync(await fs.readFile(finalPath));

const themeNames = Object.keys(sourceZip.files)
  .filter((name) => /^ppt\/theme\/theme\d+\.xml$/.test(name))
  .sort();

for (const name of themeNames) {
  const bytes = await sourceZip.file(name).async("uint8array");
  finalZip.file(name, bytes, { binary: true });
}

const output = await finalZip.generateAsync({
  type: "nodebuffer",
  compression: "DEFLATE",
  compressionOptions: { level: 6 },
});
await fs.writeFile(tempPath, output);
await fs.rename(tempPath, finalPath);
process.stdout.write(`${themeNames.length} theme parts restored\n`);
