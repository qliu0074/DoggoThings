// English comments only
/**
 * Auto-generate utils.wxss based on tokens.json.
 * Do not edit utils.wxss manually.
 */
import fs from "node:fs";

const tokens = JSON.parse(fs.readFileSync("styles/tokens.json", "utf8"));
const px = (n) => `${n}rpx`;

let out = `/* Auto-generated. Do not edit manually. */\n`;

// Spacing utilities
for (const [k, v] of Object.entries(tokens.spacing)) {
  out += `.m-${k}{margin:${px(v)}}\n`;
  out += `.mx-${k}{margin-left:${px(v)};margin-right:${px(v)}}\n`;
  out += `.my-${k}{margin-top:${px(v)};margin-bottom:${px(v)}}\n`;
  out += `.p-${k}{padding:${px(v)}}\n`;
  out += `.px-${k}{padding-left:${px(v)};padding-right:${px(v)}}\n`;
  out += `.py-${k}{padding-top:${px(v)};padding-bottom:${px(v)}}\n`;
}

// Border radius utilities
for (const [k, v] of Object.entries(tokens.radius)) {
  out += `.rounded-${k}{border-radius:${px(v)}}\n`;
}

// Font size utilities
for (const [k, v] of Object.entries(tokens.fontSize)) {
  out += `.text-${k}{font-size:${px(v)}}\n`;
}

// Flex and layout
out += `.flex{display:flex}\n.items-center{align-items:center}\n.justify-center{justify-content:center}\n.justify-between{justify-content:space-between}\n.w-full{width:100%}\n`;

// Colors
const flatColors = {
  primary: tokens.colors.primary,
  secondary: tokens.colors.secondary,
  "ink-900": tokens.colors.ink["900"],
  "ink-600": tokens.colors.ink["600"],
  "ink-400": tokens.colors.ink["400"],
  "bg-base": tokens.colors.bg.base,
  "bg-muted": tokens.colors.bg.muted
};

for (const [name, hex] of Object.entries(flatColors)) {
  out += `.text-${name}{color:${hex}}\n`;
  out += `.bg-${name}{background-color:${hex}}\n`;
  out += `.border-${name}{border-color:${hex}}\n`;
}

fs.writeFileSync("styles/utils.wxss", out);
console.log("✅ Generated styles/utils.wxss successfully");
