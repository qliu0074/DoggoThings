package app.nail.domain.repository;

import app.nail.domain.entity.AppointmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 预约明细仓储
 * 作用：按预约单读取所有服务项
 */
public interface AppointmentItemRepository extends JpaRepository<AppointmentItem, Long> {

    List<AppointmentItem> findByAppointmentId(Long appointmentId);
}
