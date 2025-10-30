import { View, Text, Image } from "@tarojs/components"
import Taro from "@tarojs/taro"
import "./index.scss"

interface BigCardProps {
  /** English: Main headline text. */
  title: string
  /** English: Supporting subtitle text. */
  subtitle: string
  /** English: Illustration image path. */
  imageUrl: string
  /** English: Destination page url. */
  targetUrl: string
}

const TAB_PAGES = new Set([
  "/pages/home/index",
  "/pages/store/index",
  "/pages/booking/index",
  "/pages/profile/index"
])

export default function BigCard({ title, subtitle, imageUrl, targetUrl }: BigCardProps) {
  const handleClick = () => {
    const isTab = TAB_PAGES.has(targetUrl)
    const action = isTab ? Taro.switchTab({ url: targetUrl }) : Taro.navigateTo({ url: targetUrl })
    action.catch(() => {
      Taro.showToast({ title: "Navigation failed", icon: "none" })
    })
  }

  return (
    <View
      className="big-card"
      onClick={handleClick}
      hoverClass="pressfx--active"
      hoverStayTime={30}
    >
      <View className="text-area">
        <Text className="title">{title}</Text>
        <Text className="subtitle">{subtitle}</Text>
      </View>
      <Image className="illustration" src={imageUrl} mode="aspectFit" lazyLoad />
    </View>
  )
}
