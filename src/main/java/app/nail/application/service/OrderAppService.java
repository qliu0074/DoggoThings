package app.nail.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** English: Order workflow placeholder. */
@Service
public class OrderAppService {

    /** English: Ensure order processing runs inside repeatable-read transaction. */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void processOrder(OrderRequest request) {
        // English: implement stock checks, payment capture, and order persistence here.
    }

    /** English: Minimal request payload placeholder. Extend with real fields later. */
    public record OrderRequest(Long userId) {}
}
