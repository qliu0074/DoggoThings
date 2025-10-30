import { View, Text, Image, Button } from "@tarojs/components"
import Taro from "@tarojs/taro"
import type { CSSProperties } from "react"
import "./index.scss"

export interface LatLng {
  latitude: number
  longitude: number
}

export interface ShopInfoCardProps {
  businessHours: string
  address: string
  phone: string
  coords?: LatLng
  iconTimeSrc?: string
  iconAddressSrc?: string
  iconPhoneSrc?: string
  themeColor?: string
}

export default function ShopInfoCard({
  businessHours,
  address,
  phone,
  coords,
  iconTimeSrc,
  iconAddressSrc,
  iconPhoneSrc,
  themeColor = "#C87DA3"
}: ShopInfoCardProps) {
  const handleNavigate = () => {
    if (coords) {
      Taro.openLocation({
        latitude: coords.latitude,
        longitude: coords.longitude,
        name: "门店位置",
        address,
        scale: 16
      })
    } else {
      Taro.setClipboardData({ data: address })
      Taro.showToast({ title: "已复制地址，无法直接导航", icon: "none" })
    }
  }

  const handleCall = () => {
    Taro.makePhoneCall({ phoneNumber: phone })
  }

  const cardStyle = { "--shop-theme": themeColor } as CSSProperties

  return (
    <View className="shop-card" style={cardStyle}>
      <View className="shop-card__row">
        {iconTimeSrc ? (
          <Image className="shop-card__icon" src={iconTimeSrc} mode="aspectFit" lazyLoad />
        ) : (
          <Text className="shop-card__icon-fallback" aria-hidden>
            🕒
          </Text>
        )}
        <Text className="shop-card__label">营业时间</Text>
        <Text className="shop-card__value">{businessHours}</Text>
      </View>

      <View className="shop-card__row">
        {iconAddressSrc ? (
          <Image className="shop-card__icon" src={iconAddressSrc} mode="aspectFit" lazyLoad />
        ) : (
          <Text className="shop-card__icon-fallback" aria-hidden>
            📍
          </Text>
        )}
        <Text className="shop-card__label">地址</Text>
        <Text className="shop-card__value">{address}</Text>
      </View>

      <View className="shop-card__row">
        {iconPhoneSrc ? (
          <Image className="shop-card__icon" src={iconPhoneSrc} mode="aspectFit" lazyLoad />
        ) : (
          <Text className="shop-card__icon-fallback" aria-hidden>
            ☎️
          </Text>
        )}
        <Text className="shop-card__label">联系方式</Text>
        <Text className="shop-card__value">{phone}</Text>
      </View>

      <View className="shop-card__actions">
        <Button
          className="shop-card__btn shop-card__btn--primary"
          onClick={handleNavigate}
        >
          一键导航
        </Button>
        <Button className="shop-card__btn shop-card__btn--ghost" onClick={handleCall}>
          联系店家
        </Button>
      </View>
    </View>
  )
}
