package app.nail.domain.repository;

import app.nail.domain.entity.SavingsCard;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/**
 * 余额卡仓储
 * 作用：查询与原子更新余额（防止出现负数）
 */
public interface SavingsCardRepository extends JpaRepository<SavingsCard, Long> {

    /**
     * 充值：余额 += delta
     * 说明：delta 必须为正数
     */
    @Modifying
    @Query(value = """
        UPDATE app.savings_cards
           SET balance_cents = balance_cents + :delta,
               updated_at = NOW()
         WHERE user_id = :userId
        """, nativeQuery = true)
    int topUp(@Param("userId") long userId, @Param("delta") int delta);

    /**
     * 扣款：余额 -= delta 且不得为负
     * 成功返回 1，失败返回 0
     */
    @Modifying
    @Query(value = """
        UPDATE app.savings_cards
           SET balance_cents = balance_cents - :delta,
               updated_at = NOW()
         WHERE user_id = :userId
           AND balance_cents >= :delta
        """, nativeQuery = true)
    int trySpend(@Param("userId") long userId, @Param("delta") int delta);
}
