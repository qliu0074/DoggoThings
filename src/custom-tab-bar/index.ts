// English comment: Custom tabBar component for WeChat Mini Program.
// Runs in native layer (not React). Uses PNG assets bundled by Taro.

import iconHome from "../assets/icons/unselectedHome.webp"
import iconHomeActive from "../assets/icons/selectedHome.webp"
import iconStore from "../assets/icons/unselectedShop.webp"
import iconStoreActive from "../assets/icons/selectedShop.webp"
import iconBooking from "../assets/icons/unselectedBooking.webp"
import iconBookingActive from "../assets/icons/selectedBooking.webp"
import iconProfile from "../assets/icons/unselectedProfile.webp"
import iconProfileActive from "../assets/icons/selectedProfile.webp"

import "./index.scss"

type TabItem = {
  pagePath: string
  text: string
  iconPath: string
  selectedIconPath: string
}

interface TabbarData {
  selected: number
  color: string
  selectedColor: string
  backgroundColor: string
  items: ReadonlyArray<TabItem>
}

function resolveCurrentIndex(items: ReadonlyArray<TabItem>): number {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current?.route ? `/${current.route}` : ""
  return items.findIndex((item) => item.pagePath === route)
}

Component<TabbarData, {
  switchTab(e: TapEvent): void
  setSelected(index: number): void
}>({
  data: {
    selected: 0,
    color: "rgba(255,255,255,0.75)",
    selectedColor: "#111111",
    backgroundColor: "#CB7DA3",
    items: [
      {
        pagePath: "/pages/home/index",
        text: "首页",
        iconPath: iconHome,
        selectedIconPath: iconHomeActive
      },
      {
        pagePath: "/pages/store/index",
        text: "商店",
        iconPath: iconStore,
        selectedIconPath: iconStoreActive
      },
      {
        pagePath: "/pages/booking/index",
        text: "预约",
        iconPath: iconBooking,
        selectedIconPath: iconBookingActive
      },
      {
        pagePath: "/pages/profile/index",
        text: "我的",
        iconPath: iconProfile,
        selectedIconPath: iconProfileActive
      }
    ] as const
  },

  lifetimes: {
    attached() {
      const index = resolveCurrentIndex(this.data.items)
      if (index >= 0) {
        this.setData({ selected: index })
      }
    }
  },

  pageLifetimes: {
    show() {
      const index = resolveCurrentIndex(this.data.items)
      if (index >= 0) {
        this.setData({ selected: index })
      }
    }
  },

  methods: {
    switchTab(e: TapEvent) {
      const ds = e.currentTarget.dataset as { path?: string; index?: number }
      if (typeof ds.path === "string") {
        wx.switchTab({ url: ds.path })
      }
      if (typeof ds.index === "number") {
        this.setData({ selected: ds.index })
      }
    },

    setSelected(index: number) {
      this.setData({ selected: index })
    }
  }
})
