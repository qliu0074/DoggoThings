# 退款审批与补偿机制设计 (Draft)

## 1. 财务审批域模型

| 字段 | 说明 |
| --- | --- |
| `id` | 主键 |
| `biz_type` | `ORDER` / `APPOINTMENT` / `BALANCE` 等 |
| `biz_id` | 业务单号 |
| `apply_user_id` | 发起人 |
| `apply_reason` | 申请原因（富文本 / 枚举） |
| `amount_cents` | 申请金额（支持部分退款） |
| `status` | `APPLYING` → `REVIEWING` → `APPROVED` → `EXECUTED` / `REJECTED` / `FAILED` |
| `reviewer_id` / `review_comment` | 审核节点 |
| `executor_id` / `exec_comment` | 执行节点（打款、回滚库存） |
| `attachments` | JSON，上传流水/票据 |
| `audit_trace` | JSON，存储每个节点的时间戳/操作者 |

实现建议：
1. 新建表 `app.refund_approvals` + `app.refund_events`（事件表用于追踪多次退回/重提）。
2. 通过 `RefundApprovalService` 暴露 `apply / approve / reject / execute / failAndRetry`。
3. Backoffice API 增加列表 + 详情 + 审核动作，所有动作写入 `AuditLog`。

## 2. 后台状态机扩展

```
客户端/客服 -> APPLYING
财务复核 -> REVIEWING (校验余额、库存、支付渠道可退额度)
审核通过 -> APPROVED (生成实际执行任务)
执行成功 -> EXECUTED，回写原业务单状态为 REFUNDED
执行失败 -> FAILED，挂入补偿队列或退回 REVIEWING
```

关键策略：
* 审核节点必须二人授权（可配置最少 2 人），记录 reviewer list。
* 对于已发货订单先触发 `reverse logistics`，确认仓库入库后才允许 `EXECUTED`。
* 所有状态流转统一由 `RefundApprovalAggregate` 完成，拒绝直接修改 `ShopOrder`/`Appointment` 状态。

## 3. 库存冲减二次校验

1. **下单阶段**：继续沿用 `stock_pending` 冻结。
2. **财务审核阶段**：新增 `InventoryValidationService`，对 `products.stock_actual`、`stock_pending`、`order_items.qty` 进行重新比对；发现不一致立即标记审批单 `REVIEW_BLOCKED`。
3. **执行阶段**：退款成功后调用 `ProductService.restoreStock`。该操作需要幂等：引入 `idempotency_key = refund_approval_id` 写入新表 `app.stock_events` 以防重复返库。

## 4. 支付成功但业务失败的补偿

### 4.1 异常检测
* `PaymentGatewayClient` 所有调用都落地 `payment_events` 表（状态：INIT → CAPTURED → REFUNDING → REFUNDED / FAILED）。
* `AuditService` 在 `BalanceService` / `OrderService` / `AppointmentService` 中记录 `payment_ref`，用于后续对账。

### 4.2 补偿通道
1. **Scheduled 对账作业**（每 5 分钟）：扫描 `payment_events` 与 `shop_orders` / `appointments` 不一致的记录，自动重试 `capture/refund` 或生成补偿单。
2. **死信队列 / Outbox**：所有外部 HTTP 调用先写入 `integration_outbox`，只有成功才删除；失败则由专职消费者重试 / 人工认领。
3. **手工处理入口**：Backoffice 提供「异常支付池」界面，可对 `FAILED` 的事件一键重试或挂到审批流程。

### 4.3 通知串联
* `NotificationService` 拓展渠道字段：`SMS` / `WeCom` / `Email`。审批、补偿结果均推送到相应群组。
* 结合 WireMock/Wedmock Server 的集成测试，保证创建→支付→退款→通知的链路在真实事务里可回放。

## 5. 实施步骤建议

1. **数据层**：创建 `refund_approvals`、`refund_events`、`payment_events`, `integration_outbox`, `stock_events` 表，并补充必要索引。
2. **服务层**：封装 `RefundApprovalService`、`InventoryValidationService`、`CompensationJob`，并为 `OrderService` / `AppointmentService` 引入审批开关。
3. **接口层**：Backoffice REST API + 管理端 UI，支持审批、执行、异常池处理。
4. **测试**：新增审批流程的单元测试 + compensation job 的集成测试；现有 `OrderFlowIT` / `AppointmentFlowIT` 可作为自动化回归基线。

> Roadmap：先落地表结构 + 服务骨架，再逐步接入微信/短信，最后打通运营审批工作台。
