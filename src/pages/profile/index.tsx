// English comment: Minimal Taro React page.
// Each page must export a default React component.

import { View, Text } from "@tarojs/components"
import { useDidShow } from "@tarojs/taro"
import { setTabBarSelected } from "../../utils/tabbar"

export default function Profile() {
  useDidShow(() => {
    setTabBarSelected(3)
  })
  return (
    <View style={{ padding: "20px" }}>
      <Text style={{ fontSize: 18, fontWeight: 600 }}>Profile</Text>
    </View>
  )
}
