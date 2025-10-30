// English comment: Minimal Taro React page.
// Each page must export a default React component.

import { View, Text } from "@tarojs/components"
import { useDidShow } from "@tarojs/taro"
import { setTabBarSelected } from "../../utils/tabbar"

export default function Store() {
  useDidShow(() => {
    setTabBarSelected(1)
  })
  return (
    <View style={{ padding: "20px" }}>
      <Text style={{ fontSize: 18, fontWeight: 600 }}>Hello, Taro!</Text>
    </View>
  )
}
