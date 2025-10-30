// English comments only: Bottom navigation bar for Taro (WeApp/H5).
// - Four items: Home, Store, Booking, Profile.
// - Theme-colored container; active icon/text use contrast color.
// - Press animation via reusable PressFX.
// - No `any`.

import { View, Image, Text } from "@tarojs/components"
import Taro from "@tarojs/taro"
import classNames from "classnames"
import { useCallback, useMemo } from "react"
import { usePressFX } from "@/components/PressFX"
import "./index.scss"

export type NavKey = "home" | "store" | "booking" | "profile"

export interface NavBarProps {
  /** English: Active key to highlight. */
  active: NavKey
  /** English: Primary theme color for the bar background. */
  themeColor?: string
  /** English: Contrast color for active icons/text (default white). */
  activeColor?: string
  /** English: Muted color for inactive icons/text (default semi-white). */
  inactiveColor?: string
}

type Item = {
  key: NavKey
  label: string
  to: string
  /** English: SVG path "d" string to draw the icon. */
  d: string
}

/** English: Build a data URL SVG with given path and fill color. */
function iconDataUrl(d: string, fill: string): string {
  const svg =
    `<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'>` +
    `<path fill='${fill}' d='${d}'/></svg>`
  return "data:image/svg+xml;utf8," + encodeURIComponent(svg)
}

// English: Minimal icons (material-like paths)
const ICONS: Record<NavKey, string> = {
  home:    "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z",
  store:   "M4 6h16l-1 7a5 5 0 0 1-10 0L4 6zm0 0V4h16v2",
  booking: "M7 11h10v2H7zm12-6h-2V3h-2v2H9V3H7v2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z",
  profile: "M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5zm0 2c-4.33 0-8 2.17-8 5v1h16v-1c0-2.83-3.67-5-8-5z",
}

const ITEMS: Item[] = [
  { key: "home",    label: "主页",     to: "/pages/home/index",    d: ICONS.home },
  { key: "store",   label: "商店",     to: "/pages/store/index",   d: ICONS.store },
  { key: "booking", label: "美甲预约", to: "/pages/booking/index", d: ICONS.booking },
  { key: "profile", label: "我的",     to: "/pages/profile/index", d: ICONS.profile },
]

export default function NavBar({
  active,
  themeColor = "#C87DA3",          // English: your brand primary
  activeColor = "#FFFFFF",
  inactiveColor = "rgba(255,255,255,0.72)",
}: NavBarProps) {
  // English: One press hook reused for H5 feedback; WeApp uses hoverClass too.
  const { active: pressing, activeClass, bind } = usePressFX()

  const navigate = useCallback((url: string) => {
    // English: Use switchTab if you later configure real tabBar; navigateTo works for normal pages.
    Taro.navigateTo({ url }).catch(() => {
      Taro.showToast({ title: "Navigation failed", icon: "none" })
    })
  }, [])

  // English: Prebuild icon urls for current colors.
  const icons = useMemo(() => {
    const map: Record<NavKey, { active: string; inactive: string }> = {
      home:    { active: iconDataUrl(ICONS.home, activeColor),    inactive: iconDataUrl(ICONS.home, inactiveColor) },
      store:   { active: iconDataUrl(ICONS.store, activeColor),   inactive: iconDataUrl(ICONS.store, inactiveColor) },
      booking: { active: iconDataUrl(ICONS.booking, activeColor), inactive: iconDataUrl(ICONS.booking, inactiveColor) },
      profile: { active: iconDataUrl(ICONS.profile, activeColor), inactive: iconDataUrl(ICONS.profile, inactiveColor) },
    }
    return map
  }, [activeColor, inactiveColor])

  return (
    <View
      className={classNames("navbar", { [activeClass]: pressing })}
      style={{ backgroundColor: themeColor }}
      hoverClass="pressfx--active"
      hoverStayTime={30}
      {...bind}
    >
      {ITEMS.map((it) => {
        const isActive = it.key === active
        const color = isActive ? activeColor : inactiveColor
        const src = isActive ? icons[it.key].active : icons[it.key].inactive
        return (
          <View
            key={it.key}
            className={classNames("navitem", { "navitem--active": isActive })}
            onClick={() => navigate(it.to)}
          >
            <Image className="navitem__icon" src={src} mode="widthFix" />
            <Text className="navitem__label" style={{ color }}>{it.label}</Text>
          </View>
        )
      })}
      {/* English: Safe-area inset for iOS */}
      <View className="navbar__safe" />
    </View>
  )
}
