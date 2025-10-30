// 批量把一个文件夹下的 PNG 转成 WebP（像素不变，压缩更小）
// Usage: node batch-convert.js ./images
// 输出结果会保存在同目录下，每个文件生成同名 .webp

const fs = require("fs")
const path = require("path")
const sharp = require("sharp")

// 读取命令行参数，获取输入文件夹路径
const folder = process.argv[2] || "./images"

if (!fs.existsSync(folder)) {
  console.error(`❌ 文件夹不存在: ${folder}`)
  process.exit(1)
}

// 读取文件夹内所有文件
const files = fs.readdirSync(folder).filter((f) => f.endsWith(".png"))

if (files.length === 0) {
  console.log("⚠️ 文件夹中没有 PNG 图片。")
  process.exit(0)
}

console.log(`开始转换：共 ${files.length} 张图片...`)

// 遍历并处理每个 PNG 文件
Promise.all(
  files.map(async (file) => {
    const inputPath = path.join(folder, file)
    const outputPath = path.join(folder, file.replace(/\.png$/, ".webp"))

    try {
      await sharp(inputPath)
        .toFormat("webp", { quality: 85 })
        .toFile(outputPath)
      console.log(`✅ 已转换: ${file} → ${path.basename(outputPath)}`)
    } catch (err) {
      console.error(`❌ 转换失败: ${file}`, err)
    }
  })
).then(() => {
  console.log("🎉 所有 PNG 转 WebP 完成。")
})
