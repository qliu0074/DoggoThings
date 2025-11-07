package app.nail.unit;

import app.nail.application.service.ProductService;
import app.nail.domain.entity.Product;
import app.nail.domain.entity.ProductImage;
import app.nail.domain.enums.ProductStatus;
import app.nail.domain.repository.ProductImageRepository;
import app.nail.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepo;
    @Mock ProductImageRepository imageRepo;

    @InjectMocks ProductService productService;

    @Test
    void createProduct_shouldPersistWithDefaults() {
        var stub = Product.builder()
                .id(1L).name("A").category("TOY").priceCents(1000)
                .stockActual(0).stockPending(0).status(ProductStatus.ON).build();
        when(productRepo.save(any(Product.class))).thenReturn(stub);

        Long id = productService.createProduct("A","TOY",1000);

        assertThat(id).isEqualTo(1L);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("A");
        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ON);
    }

    @Test
    void updateProduct_shouldMergeProvidedFieldsOnly() {
        var origin = Product.builder().id(9L).name("old").category("X").priceCents(1).status(ProductStatus.ON).build();
        when(productRepo.findById(9L)).thenReturn(Optional.of(origin));

        productService.updateProduct(9L, "new", null, 200, ProductStatus.OFF);

        verify(productRepo).save(origin);
        assertThat(origin.getName()).isEqualTo("new");
        assertThat(origin.getCategory()).isEqualTo("X");
        assertThat(origin.getPriceCents()).isEqualTo(200);
        assertThat(origin.getStatus()).isEqualTo(ProductStatus.OFF);
    }

    @Test
    void addImage_shouldRejectDuplicateCover() {
        var product = Product.builder().id(2L).build();
        when(productRepo.lockById(2L)).thenReturn(Optional.of(product));
        when(imageRepo.existsByProductIdAndCoverTrue(2L)).thenReturn(true);

        assertThatThrownBy(() -> productService.addImage(2L, "url", true, (short) 0))
                .isInstanceOf(RuntimeException.class);
    }
}
