// English comment: Import defineAppConfig macro so TS knows the symbol.


export default defineAppConfig({
  pages: [
    "pages/home/index",
    "pages/store/index",
    "pages/booking/index",
    "pages/profile/index",
  ],
  window: {
    navigationBarTitleText: "",
    navigationBarBackgroundColor: "#CB7DA3",
    navigationBarTextStyle: "white",
    backgroundTextStyle: "light",
    navigationStyle: "custom"
  },
  tabBar: {
    custom: true,
    color: "#F4DDEA",
    selectedColor: "#FFFFFF",
    backgroundColor: "#CB7DA3",
    borderStyle: "white",
    list: [
      { pagePath: "pages/home/index", text: "主页" },
      { pagePath: "pages/store/index", text: "商店" },
      { pagePath: "pages/booking/index", text: "预约" },
      { pagePath: "pages/profile/index", text: "我的" }
    ]
  }
})
