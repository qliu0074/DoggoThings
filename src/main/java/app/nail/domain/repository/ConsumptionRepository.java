package app.nail.domain.repository;

import app.nail.domain.entity.Consumption;
import app.nail.domain.enums.ConsumeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

/**
 * 余额变动记录仓储
 * 作用：查询用户的充值/消费流水
 */
public interface ConsumptionRepository extends JpaRepository<Consumption, Long> {

    /** 用户的全部流水，按时间倒序 */
    Page<Consumption> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 用户在时间段内的某类流水 */
    Page<Consumption> findByUserIdAndKindAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, ConsumeType kind, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
