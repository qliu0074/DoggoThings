// English comments only: Global layout wrapper.
// - Renders children page content.
// - Shows NavBar on all pages except the login page.
// - No `any`.

import { PropsWithChildren, useMemo } from "react"
import { View } from "@tarojs/components"
import  { getCurrentInstance } from "@tarojs/taro"
import NavBar, { NavKey } from "../components/NavBar"

export type AppLayoutProps = {
  /** English: Active tab key for NavBar highlight. */
  active?: NavKey
  /** English: Optional override to hide NavBar manually. */
  hideNav?: boolean
}

export default function AppLayout({
  children,
  active,
  hideNav,
}: PropsWithChildren<AppLayoutProps>) {
  // English: Read current route path, e.g. "/pages/login/index"
  const path = useMemo<string>(() => {
    const inst = getCurrentInstance()
    return inst?.router?.path ?? ""
  }, [])

  // English: Hide NavBar on login page or if forced by prop.
  const shouldHideNav = hideNav === true || path.includes("/pages/login/index")

  return (
    <View className="app-layout" style={{ paddingBottom: shouldHideNav ? 0 : "140rpx" }}>
      {children}
      {!shouldHideNav && <NavBar active={active ?? "home"} themeColor="#C87DA3" />}
    </View>
  )
}
