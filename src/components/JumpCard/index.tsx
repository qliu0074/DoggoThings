// English comments only: A rectangular "jump button" card for Taro (WeApp/H5).
// - Illustration on top, label at bottom.
// - Click to navigate to another page.
// - Includes enter animation and press feedback.
// - No `any`, fully typed.

import { View, Image, Text } from "@tarojs/components"
import Taro from "@tarojs/taro"
import { useCallback } from "react"
import classNames from "classnames"
import "./index.scss"

const TAB_PAGES = new Set([
  "/pages/home/index",
  "/pages/store/index",
  "/pages/booking/index",
  "/pages/profile/index"
])

export type JumpCardSize = "sm" | "md" | "lg"

export interface JumpCardProps {
  /** English: Image URL for the top illustration. */
  imageSrc: string 
  /** English: Text label shown below the image. */
  label: string
  /** English: Target path for navigation, e.g. "/pages/detail/index". */
  to: string
  /** English: Visual size preset. */
  size?: JumpCardSize
  /** English: Optional test id for E2E tests. */
  testId?: string
  /** English: Optional aria label for accessibility. */
  ariaLabel?: string
}

export default function JumpCard({
  imageSrc,
  label,
  to,
  size = "md",
  testId,
  ariaLabel,
}: JumpCardProps) {
  // English: Navigate to target page.
  const handleClick = useCallback(() => {
    const isTab = TAB_PAGES.has(to)
    const action = isTab ? Taro.switchTab({ url: to }) : Taro.navigateTo({ url: to })
    action.catch(() => {
      // English: Fallback toast on failure.
      Taro.showToast({ title: "Navigation failed", icon: "none" })
    })
  }, [to])

  return (
    <View
      // English: Treat as a pressable card; role + aria aids screen readers.
      role="button"
      aria-label={ariaLabel ?? label}
      className={classNames("jumpcard", `jumpcard--${size}`)}
      hoverClass="jumpcard--hover"            // English: WeApp press feedback
      hoverStayTime={30}
      onClick={handleClick}
      data-testid={testId}
    >
      <Image
        className="jumpcard__img"
        src={imageSrc}
        mode="aspectFit"                      // English: Keep full image visible
        lazyLoad                               
      />
      <Text className="jumpcard__label">{label}</Text>
    </View>
  )
}
