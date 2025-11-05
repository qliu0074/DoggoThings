package app.nail.domain.repository;
import app.nail.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
/** English: Basic CRUD for appointments. */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {}