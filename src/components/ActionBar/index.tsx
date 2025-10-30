// English comments only: A flat rectangular action bar.
// - Theme-colored background, left optional illustration, right arrow.
// - Click to navigate to another page.
// - Uses reusable PressFX animation and classnames.
// - No `any`.

import { View, Image, Text } from "@tarojs/components"
import Taro from "@tarojs/taro"
import classNames from "classnames"
import { CSSProperties, useCallback, useMemo } from "react"
import { usePressFX } from "../PressFX"
import "./index.scss"

export type ActionBarSize = "sm" | "md" | "lg"

export interface ActionBarProps {
  /** English: Text content to display in the center-left area. */
  label: string
  /** English: Optional illustration URL shown at the far left. */
  iconSrc?: string
  /** English: Target page path, e.g. "/pages/booking/index". */
  to: string
  /** English: Theme color in hex or rgb, default to brand primary. */
  color?: string
  /** English: Visual size preset. */
  size?: ActionBarSize
  /** English: Optional full-width block. */
  block?: boolean
  /** English: Optional extra inline styles. */
  style?: CSSProperties
  /** English: Optional test id. */
  testId?: string
}

const DEFAULT_PRIMARY = "#C87DA3" // English: your brand primary
const TAB_PAGES = new Set([
  "/pages/home/index",
  "/pages/store/index",
  "/pages/booking/index",
  "/pages/profile/index"
])

export default function ActionBar({
  label,
  iconSrc,
  to,
  color = DEFAULT_PRIMARY,
  size = "md",
  block = true,
  style,
  testId,
}: ActionBarProps) {
  // English: Reusable press animation hook (works on H5; WeApp also uses hoverClass).
  const { active, activeClass, bind } = usePressFX()

  // English: Text color for contrast (simple luminance check).
  const textColor = useMemo<string>(() => {
    const hex = color.startsWith("#") ? color.slice(1) : ""
    if (hex.length === 6 || hex.length === 3) {
      const h = hex.length === 3 ? hex.split("").map(c => c + c).join("") : hex
      const r = parseInt(h.slice(0, 2), 16)
      const g = parseInt(h.slice(2, 4), 16)
      const b = parseInt(h.slice(4, 6), 16)
      const L = 0.2126*(r/255) + 0.7152*(g/255) + 0.0722*(b/255)
      return L > 0.6 ? "#111111" : "#FFFFFF"
    }
    return "#FFFFFF"
  }, [color])

  // English: Navigate to target page.
  const onClick = useCallback(() => {
    const isTab = TAB_PAGES.has(to)
    const action = isTab ? Taro.switchTab({ url: to }) : Taro.navigateTo({ url: to })
    action.catch(() => {
      Taro.showToast({ title: "Navigation failed", icon: "none" })
    })
  }, [to])

  return (
    <View
      role="button"
      aria-label={label}
      className={classNames(
        "actionbar",
        `actionbar--${size}`,
        { "actionbar--block": block },
        { [activeClass]: active } // English: H5 press feedback via hook
      )}
      style={{ backgroundColor: color, color: textColor, ...style }}
      hoverClass="pressfx--active"   // English: WeApp native press feedback
      hoverStayTime={30}
      onClick={onClick}
      {...bind}
      data-testid={testId}
    >
      {iconSrc ? (
        <Image className="actionbar__icon" src={iconSrc} mode="aspectFit" />
      ) : (
        <View className="actionbar__icon--placeholder" />
      )}

      <Text className="actionbar__label">{label}</Text>

      <View className="actionbar__arrow" />
    </View>
  )
}
