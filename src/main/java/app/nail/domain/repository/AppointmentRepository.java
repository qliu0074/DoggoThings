package app.nail.domain.repository;

import app.nail.domain.entity.Appointment;
import app.nail.domain.enums.ApptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

/**
 * 预约单仓储
 * 作用：按用户/状态/时间范围分页查询
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** 用户某时间段的预约，按时间倒序 */
    Page<Appointment> findByUserIdAndAppointmentAtBetweenOrderByAppointmentAtDesc(
            Long userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /** 按状态分页查询（后台看板） */
    Page<Appointment> findByStatus(ApptStatus status, Pageable pageable);

    /** 是否存在时间重叠的预约（排除已取消/已退款） */
    @Query("""
            SELECT (COUNT(a) > 0) FROM Appointment a
            WHERE a.user.id = :userId
              AND a.status NOT IN (app.nail.domain.enums.ApptStatus.CANCELLED, app.nail.domain.enums.ApptStatus.REFUNDED)
              AND a.appointmentAt < :endAt
              AND a.endAt > :startAt
            """)
    boolean existsOverlapping(Long userId, OffsetDateTime startAt, OffsetDateTime endAt);
}
