// English comment: Enable React framework plugin so Babel loads React preset.
import { defineConfig } from "@tarojs/cli"

export default defineConfig({
  projectName: "nail-reserve",
  framework: "react",
  compiler: "webpack5",
  plugins: ["@tarojs/plugin-framework-react"], // ← 必加
  sourceRoot: "src",
  outputRoot: "dist",
    babel: {
    presets: [
      ["@babel/preset-env", { modules: false }],
      ["@babel/preset-react", { runtime: "automatic" }],
      ["@babel/preset-typescript", { isTSX: true, allExtensions: true }]
    ],
    plugins: []
  },
  mini: {  },
  h5: {
    publicPath: "/",                  // English: static assets base
    router: { mode: "hash" }, 
    devServer: { historyApiFallback: true }
  }
})
