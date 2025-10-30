// English comments only: Reusable "press" animation helper for WeApp/H5.
// - Adds a CSS class while pressing for H5 (touchstart → touchend).
// - For WeApp, you can also use hoverClass on View with the same class name.
// - No `any`.

import { useState, useMemo } from "react"

export type PressFXOptions = {
  /** English: CSS class applied during press, e.g. "pressfx--active". */
  activeClass?: string
}

export type PressFXBind = {
  /** English: Bind these to the pressable element. */
  onTouchStart: () => void
  onTouchEnd: () => void
  onTouchCancel: () => void
}

export function usePressFX(
  { activeClass = "pressfx--active" }: PressFXOptions = {},
) {
  const [active, setActive] = useState<boolean>(false)

  const bind: PressFXBind = useMemo(
    () => ({
      onTouchStart: () => setActive(true),
      onTouchEnd: () => setActive(false),
      onTouchCancel: () => setActive(false),
    }),
    []
  )

  return { active, activeClass, bind }
}
