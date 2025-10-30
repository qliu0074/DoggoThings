type TabItem = {
  pagePath: string
  text: string
  iconPath: string
  selectedIconPath: string
}

const iconHome = require("../assets/icons/unselectedhome.svg") as string
const iconHomeActive = require("../assets/icons/selectedhome.svg") as string
const iconStore = require("../assets/icons/unselectedshop.svg") as string
const iconStoreActive = require("../assets/icons/selectedshop.svg") as string
const iconBooking = require("../assets/icons/unselectedBooking.svg") as string
const iconBookingActive = require("../assets/icons/selectedBooking.svg") as string
const iconProfile = require("../assets/icons/unselectedProfile.svg") as string
const iconProfileActive = require("../assets/icons/selectedProfile.svg") as string

function resolveCurrentIndex(items: TabItem[]): number {
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  const route = current?.route ? `/${current.route}` : ""
  return items.findIndex((item) => item.pagePath === route)
}

Component({
  data: {
    selected: 0,
    color: "rgba(255,255,255,0.75)",
    selectedColor: "#FFFFFF",
    backgroundColor: "#CB7DA3",
    items: <TabItem[]>[
      {
        pagePath: "/pages/home/index",
        text: "主页",
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
    ]
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
    switchTab(event: WechatMiniprogram.TapEvent) {
      const { path, index } = event.currentTarget.dataset as {
        path: string
        index: number
      }
      if (typeof path === "string") {
        wx.switchTab({ url: path })
      }
      if (typeof index === "number") {
        this.setSelected(index)
      }
    },

    setSelected(index: number) {
      this.setData({ selected: index })
    }
  }
})
