# Admin APIs

后台接口需携带 ADMIN 角色的 JWT；所有路径前缀为 `/api/v1/admin`，除特殊说明外均返回 JSON。

## 认证

管理员复用 `/api/v1/auth/login`，但必须使用配置中的 admin 账户获取包含 `ROLE_ADMIN` 的 token。

## 商品管理 (`AdminProductController`)

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/products` | 新建商品，`CreateProductReq {name, category, priceCents}`。返回新商品 ID。 |
| `PUT` | `/products/{id}` | 更新商品，`UpdateProductReq {name, category, priceCents, status}` 任填。 |
| `GET` | `/products` | 分页查询所有商品，query：`page`、`size`。返回 `PageResp<ProductDetailResp>`。 |
| `POST` | `/products/{id}/images` | 新增商品图片，`AddImageReq {url, cover, sortOrder}`。 |

## 服务管理 (`AdminServiceController`)

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/services` | 新建服务，`CreateServiceReq {category, priceCents, description, estimatedMinutes}`。 |
| `PUT` | `/services/{id}` | 更新服务，`UpdateServiceReq {category, priceCents, description, status, estimatedMinutes}`。 |
| `GET` | `/services` | 分页查询服务。 |
| `POST` | `/services/{id}/images` | 上传服务图片，字段同商品。 |

## 订单运营 (`AdminOrderController`)

| Method | Path | 描述 |
| --- | --- | --- |
| `POST` | `/orders/{id}/confirm` | 审核并确认订单，捕获支付。 |
| `POST` | `/orders/{id}/cancel` | 管理员取消订单。 |
| `POST` | `/orders/{id}/ship` | 发货，`ShipReq {trackingNo}`。 |
| `POST` | `/orders/{id}/complete` | 标记完成。 |
| `POST` | `/orders/{id}/refund` | 退款，`RefundReq {amountCents, reason}`。 |
| `GET` | `/orders` | 分页查询全部订单，支持 `status` 过滤。 |
| `GET` | `/orders/{orderId}` | 查看订单详情。 |

## 报表与健康

- `/actuator/health`：整体健康（可用于 SRE）。
- `ReportService` 尚未暴露 REST 端点，如需统计可新增 controller。

## 其他说明

- 所有金额单位为分。
- 写操作均自动记录审计日志；无需额外调用。
- 若需要强制 HTTPS，可设置 `app.security.require-https=true` 以保护后台入口。
