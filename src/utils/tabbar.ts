import { getCurrentInstance } from "@tarojs/taro"

type CustomTabBarInstance = WechatMiniprogram.Component.TrivialInstance & {
  setSelected?: (index: number) => void
}

export function setTabBarSelected(index: number) {
  const inst = getCurrentInstance()
  const tabbar = inst?.page?.getTabBar?.() as CustomTabBarInstance | undefined
  if (tabbar?.setSelected) {
    tabbar.setSelected(index)
  }
}
