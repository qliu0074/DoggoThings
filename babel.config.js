// English comment: Enable Taro preset for React + add TypeScript parsing.
module.exports = {
  presets: [
    [
      "taro",
      {
        framework: "react",
        ts: true
      }
    ],
    "@babel/preset-typescript"
  ]
};
