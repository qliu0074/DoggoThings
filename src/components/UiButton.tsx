// English comment: A typed, themeable button for Taro (WeApp/H5).
// Uses your brand colors only: primary (#C87DA3) and secondary (#A8C6E6).
// No `any`. Works with @tarojs/components Button.
// Note: Do NOT pass `type="button"` to avoid conflicting with WeApp Button's `type` prop.

import { Button } from "@tarojs/components"
import { CSSProperties, ReactNode, useMemo } from "react"

// English comment: Theme palette names. Only your two brand colors.
export type ThemeName = "primary" | "secondary"

// English comment: Visual size of the button.
export type ButtonSize = "sm" | "md" | "lg"

// English comment: Which color source to use.
export type ColorVariant = "theme" | "custom"

export interface UiButtonProps {
  /** English: Color source. "theme" uses ThemeName. "custom" uses `customColor`. */
  variant?: ColorVariant
  /** English: Theme palette when variant="theme". */
  theme?: ThemeName
  /** English: Custom background color like "#C87DA3" or "rgb(200,125,163)". */
  customColor?: string

  /** English: Button label. If children exists, children will render instead. */
  label?: string
  /** English: Optional React children to allow icons or richer content. */
  children?: ReactNode

  /** English: Button size. */
  size?: ButtonSize
  /** English: Full-width block button. */
  block?: boolean
  /** English: Disabled state. */
  disabled?: boolean

  /** English: Click handler. */
  onClick?: () => void
}

// English comment: Brand theme map from your design tokens.
const THEME_MAP: Record<ThemeName, string> = {
  primary: "#C87DA3",
  secondary: "#A8C6E6",
}

// English comment: Compute readable text color (black or white) for a given background.
function readableTextOn(bg: string): "#000000" | "#FFFFFF" {
  // English: Parse hex like #rrggbb or #rgb
  const norm = bg.trim()
  const hexLike = norm.startsWith("#") ? norm.slice(1) : null
  let r = 0, g = 0, b = 0

  if (hexLike && (hexLike.length === 6 || hexLike.length === 3)) {
    const h = hexLike.length === 3
      ? hexLike.split("").map((ch) => ch + ch).join("")
      : hexLike
    r = parseInt(h.slice(0, 2), 16)
    g = parseInt(h.slice(2, 4), 16)
    b = parseInt(h.slice(4, 6), 16)
  } else {
    // English: Minimal rgb() parse as a fallback.
    const m = norm.match(/rgb\((\d+)\s*,\s*(\d+)\s*,\s*(\d+)\)/i)
    if (m) {
      r = Number(m[1]); g = Number(m[2]); b = Number(m[3])
    } else {
      // English: Unknown format -> default to dark text.
      return "#000000"
    }
  }
  // English: Relative luminance approximation (simple).
  const luminance = 0.2126 * (r / 255) + 0.7152 * (g / 255) + 0.0722 * (b / 255)
  return luminance > 0.6 ? "#000000" : "#FFFFFF"
}

// English comment: Map size to padding/radius/fontSize in px for H5; Taro will convert for WeApp.
function sizeStyle(size: ButtonSize): Pick<CSSProperties, "padding" | "borderRadius" | "fontSize"> {
  switch (size) {
    case "sm": return { padding: "6px 12px", borderRadius: "8px",  fontSize: "12px" }
    case "lg": return { padding: "12px 18px", borderRadius: "12px", fontSize: "16px" }
    default:   return { padding: "8px 14px", borderRadius: "10px", fontSize: "14px" }
  }
}

export default function UiButton({
  variant = "theme",
  theme = "primary",
  customColor,
  label,
  children,
  size = "md",
  block = false,
  disabled = false,
  onClick,
}: UiButtonProps) {

  // English comment: Decide final background color based on variant.
  const bg = useMemo<string>(() => {
    if (variant === "custom" && customColor) return customColor
    return THEME_MAP[theme]
  }, [variant, customColor, theme])

  // English comment: Text color that keeps good contrast on chosen bg.
  const fg = readableTextOn(bg)

  // English comment: Build inline style that works on WeApp and H5.
  const { padding, borderRadius, fontSize } = sizeStyle(size)
  const style: CSSProperties = {
    backgroundColor: disabled ? "#A1A1AA" : bg,
    color: fg,
    padding,
    borderRadius,
    fontSize,
    width: block ? "100%" : undefined,
    opacity: disabled ? 0.6 : 1,
    // English: Remove default border in H5 render.
    border: "none",
  }

  return (
    <Button
      // English: Avoid `type="button"` to prevent conflicting with WeApp Button's `type` values.
      disabled={disabled}
      onClick={disabled ? undefined : onClick}
      style={style}
    >
      {children ?? label}
    </Button>
  )
}
