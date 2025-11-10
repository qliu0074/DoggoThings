# Client APIs

本文档覆盖小程序/客户端可调用的主要接口，所有请求需携带 `Authorization: Bearer <JWT>` 头（除健康检查外）。如果未登录将返回 401。

## 认证

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | 传入 `{"username","password","userId"}`，成功返回 `{"token":"..."}`。`userId` 用于识别业务用户。 |

## 商品

| Method | Path | 描述 |
| --- | --- | --- |
| `GET` | `/api/v1/client/products` | 查询商品分页。支持 query：`page`、`size`、`category`。返回 `PageResp<ProductResp>`。 |
| `GET` | `/api/v1/client/products/{id}` | 单个商品详情。 |

`ProductResp` 字段：`id,name,category,priceCents,stockDisplay,status,updatedAt`。

## 服务项目

| Method | Path | 描述 |
| --- | --- | --- |
| `GET` | `/api/v1/client/services` | 服务分页，支持 `page/size/category`，只返回上架服务。 |
| `GET` | `/api/v1/client/services/{id}` | 服务详情。响应包括 `estimatedMinutes`。 |

## 订单

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/api/v1/client/orders` | 创建商城订单。请求体 `CreateOrderReq`：`items:[{productId, qty}]`, `freezeBalance`, `address`, `phone`。返回订单 ID。 |
| `POST` | `/api/v1/client/orders/{orderId}/cancel` | 取消自己的订单。 |
| `GET` | `/api/v1/client/orders` | 查询我的订单，支持 `page/size`。 |
| `GET` | `/api/v1/client/orders/{orderId}` | 查看订单详情（含明细）。 |

## 预约

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/api/v1/client/appointments` | 预约服务。`BookReq`：`time`(ISO8601)、`items:[{serviceId, qty}]`、`freezeBalance`。返回预约 ID。 |
| `POST` | `/api/v1/client/appointments/{id}/cancel` | 取消预约。 |
| `POST` | `/api/v1/client/appointments/{id}/refund` | 预约退款，`RefundReq`：`amountCents`,`reason`。 |
| `GET` | `/api/v1/client/appointments` | 按时间范围查询：必填 `from`、`to`，支持 `page/size`。 |
| `GET` | `/api/v1/client/appointments/{id}` | 预约详情。 |

## 余额

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/api/v1/client/balance/top-up` | 给自己的储值卡充值。请求体 `{ "amountCents": 10000 }`。通常由支付回调触发。 |

## 健康检查

| Method | Path | 描述 |
| --- | --- | --- |
| `GET` | `/actuator/health` | 公开健康检查（无需登录）。 |

> 提示：所有金额均为整数“分”。预约/订单成功后会产生审计记录与通知，客户端无需关心。
