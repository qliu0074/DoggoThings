// 首页：包含顶部横幅、功能卡片、操作栏、VIP 大卡片和店铺信息。
// 适用于 Taro + 自定义底部 TabBar。

import { View } from "@tarojs/components"
import { useDidShow } from "@tarojs/taro"
import classNames from "classnames"
import JumpCard from "../../components/JumpCard"
import BigCard from "../../components/BigCard"
import ActionBar from "../../components/ActionBar"
import ShopInfoCard from "../../components/ShopInfoCard"
import { setTabBarSelected } from "../../utils/tabbar"

// 本地图片资源
import heroBg from "../../assets/images/background.png"
import shopIcon from "../../assets/images/shop.png"
import reservationIcon from "../../assets/images/reservation.png"
import membershipIcon from "../../assets/images/membership.png"
import iconTime from "../../assets/icons/time.svg"
import iconLocation from "../../assets/icons/location.svg"
import iconPhone from "../../assets/icons/contact.svg"

import "./index.scss"

export default function Home() {
  useDidShow(() => {
    setTabBarSelected(0)
  })

  // 跳转路径
  const toShop = "/pages/store/index"
  const toBooking = "/pages/booking/index"
  const toProfile = "/pages/profile/index"

  return (
    <View className="home">
      {/* 顶部横幅 */}
      <View
        className="home__hero"
        style={{ backgroundImage: `url(${heroBg})` }}
      />

      <View className="home__overlay">
        {/* 功能导航卡片 */}
        <View className={classNames("home__cards")}>
          <JumpCard
            imageSrc={shopIcon}
            label="商店"
            to={toShop}
            size="md"
            ariaLabel="前往商店"
          />
          <JumpCard
            imageSrc={reservationIcon}
            label="美甲预约"
            to={toBooking}
            size="md"
            ariaLabel="前往预约"
          />
        </View>

        {/* 底部操作栏 */}
        <View className="home__action">
          <ActionBar
            label="成为会员，享更多专属权益"
            to={toProfile}
            color="#C87DA3"
            size="md"
            block
          />
        </View>

        {/* VIP 大卡片 */}
        <BigCard
          title="VIP 会员"
          subtitle="￥300｜会员享专属优惠与活动"
          imageUrl={membershipIcon}
          targetUrl={toProfile}
        />

        {/* 店铺信息 */}
        <ShopInfoCard
          businessHours="周一至周日 10:00 - 20:00"
          address="维多利亚州墨尔本某某街 123 号"
          phone="+61 3 1234 5678"
          coords={{ latitude: -37.8136, longitude: 144.9631 }}
          iconTimeSrc={iconTime}
          iconAddressSrc={iconLocation}
          iconPhoneSrc={iconPhone}
          themeColor="#C87DA3"
        />
      </View>
    </View>
  )
}
