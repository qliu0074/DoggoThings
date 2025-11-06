package app.nail.application.service;

import app.nail.domain.entity.Consumption;
import app.nail.domain.entity.ShopOrder;
import app.nail.domain.enums.ConsumeType;
import app.nail.domain.enums.ShopStatus;
import app.nail.domain.repository.ConsumptionRepository;
import app.nail.domain.repository.ShopOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 报表服务（可选）
 * 职责：
 * 1. 销售与订单数量统计
 * 2. 余额变化趋势（充值/消费）
 * 可根据需要改用原生 SQL 或视图进行聚合
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ShopOrderRepository orderRepo;
    private final ConsumptionRepository consRepo;

    /** 最近 N 条已完成订单（演示） */
    public List<ShopOrder> recentCompleted(int limit) {
        return orderRepo.findByStatus(ShopStatus.COMPLETED, PageRequest.of(0, limit)).getContent();
    }

    /** 时间段内充值与消费记录（演示） */
    public List<Consumption> rangeOf(long userId, OffsetDateTime from, OffsetDateTime to, ConsumeType type, int limit) {
        return consRepo.findByUserIdAndKindAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, type, from, to, PageRequest.of(0, limit)).getContent();
    }

    /** 占位：可扩展为更复杂的聚合统计 */
    @Transactional
    public void rebuildMaterializedViews() {
        // 若建立物化视图，可在此集中刷新
    }
}
