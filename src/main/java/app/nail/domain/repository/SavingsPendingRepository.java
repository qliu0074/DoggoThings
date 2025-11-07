package app.nail.domain.repository;

import app.nail.domain.entity.SavingsPending;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 待结算余额仓储
 * 作用：下单/预约冻结与释放余额
 */
public interface SavingsPendingRepository extends JpaRepository<SavingsPending, Long> {

    /**
     * 冻结：pending += delta
     * 说明：delta 为正
     */
    @Modifying
    @Query(value = """
        INSERT INTO app.savings_pending(user_id, pending_cents, updated_at)
        VALUES (:userId, :delta, NOW())
        ON CONFLICT (user_id)
        DO UPDATE SET pending_cents = app.savings_pending.pending_cents + EXCLUDED.pending_cents,
                      updated_at = NOW()
        """, nativeQuery = true)
    int freeze(@Param("userId") long userId, @Param("delta") int delta);

    /**
     * 释放或扣减：pending += delta（delta 可为负）
     * 约束：结果不得为负
     */
    @Modifying
    @Query(value = """
        UPDATE app.savings_pending
           SET pending_cents = pending_cents + :delta,
               updated_at = NOW()
         WHERE user_id = :userId
           AND pending_cents + :delta >= 0
        """, nativeQuery = true)
    int adjust(@Param("userId") long userId, @Param("delta") int delta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sp from SavingsPending sp where sp.userId = :userId")
    java.util.Optional<SavingsPending> lockByUserId(@Param("userId") long userId);
}
