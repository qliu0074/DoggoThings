import { promises as fs } from "node:fs"
import path from "node:path"
import sharp from "sharp"

const ROOT = new URL("../", import.meta.url).pathname
const ASSET_DIRS = [
  path.join(ROOT, "src", "assets", "icons"),
  path.join(ROOT, "src", "assets", "images")
]

async function collectPngFiles(dir) {
  const entries = await fs.readdir(dir, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      files.push(...await collectPngFiles(fullPath))
    } else if (entry.isFile() && entry.name.toLowerCase().endsWith(".png")) {
      files.push(fullPath)
    }
  }
  return files
}

async function convertFile(filePath) {
  const webpPath = filePath.replace(/\.png$/i, ".webp")
  const buffer = await fs.readFile(filePath)
  const image = sharp(buffer)
  const metadata = await image.metadata()
  const quality = metadata.hasAlpha ? 90 : 85
  await image.webp({ quality }).toFile(webpPath)
  return webpPath
}

async function main() {
  for (const dir of ASSET_DIRS) {
    try {
      await fs.access(dir)
    } catch {
      continue
    }
    const pngs = await collectPngFiles(dir)
    if (pngs.length === 0) continue
    for (const png of pngs) {
      const target = png.replace(/\.png$/i, ".webp")
      const targetExists = await fs.stat(target).then(() => true).catch(() => false)
      if (targetExists) continue
      console.log(`Converting ${path.relative(ROOT, png)} -> ${path.relative(ROOT, target)}`)
      await convertFile(png)
    }
  }
}

main().catch((err) => {
  console.error(err)
  process.exitCode = 1
})
