package app.nail.domain.repository;
import app.nail.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
/** English: Basic CRUD for users. */
public interface UserRepository extends JpaRepository<User, Long> {}