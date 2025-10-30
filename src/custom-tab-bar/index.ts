type TabItem = {
  pagePath: string
  text: string
}

Component({
  data: {
    selected: 0,
    color: "#F4DDEA",
    selectedColor: "#FFFFFF",
    backgroundColor: "#CB7DA3",
    items: <TabItem[]>[
      { pagePath: "/pages/home/index", text: "主页" },
      { pagePath: "/pages/store/index", text: "商店" },
      { pagePath: "/pages/booking/index", text: "预约" },
      { pagePath: "/pages/profile/index", text: "我的" }
    ]
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
