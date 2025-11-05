package app.nail.unit;

import app.nail.application.service.ProductAppService;
import app.nail.domain.entity.Product;
import app.nail.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/** English: minimal unit test for service skeleton. */
class ProductAppServiceTest {
    @Test
    void getById_returnPresent() {
        ProductRepository repo = mock(ProductRepository.class);
        Product p = new Product(); p.setId(1L); p.setName("X");
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        ProductAppService svc = new ProductAppService(repo);
        assertThat(svc.getById(1L)).isPresent();
    }
}
