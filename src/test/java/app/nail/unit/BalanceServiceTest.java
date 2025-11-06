package app.nail.unit;

import app.nail.application.service.BalanceService;
import app.nail.domain.entity.Consumption;
import app.nail.domain.enums.ConsumeType;
import app.nail.domain.repository.ConsumptionRepository;
import app.nail.domain.repository.SavingsCardRepository;
import app.nail.domain.repository.SavingsPendingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** English: Unit test for balance flows. */
@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock SavingsCardRepository cardRepo;
    @Mock SavingsPendingRepository pendingRepo;
    @Mock ConsumptionRepository consRepo;

    @InjectMocks BalanceService balanceService;

    @Test
    void topUp_shouldWriteConsumption_whenCardUpdated() {
        when(cardRepo.topUp(1L, 500)).thenReturn(1);
        ArgumentCaptor<Consumption> captor = ArgumentCaptor.forClass(Consumption.class);

        balanceService.topUp(1L, 500);

        verify(consRepo).save(captor.capture());
        assertThat(captor.getValue().getKind()).isEqualTo(ConsumeType.TOP_UP);
        assertThat(captor.getValue().getAmountCents()).isEqualTo(500);
    }

    @Test
    void topUp_shouldFail_whenCardMissing() {
        when(cardRepo.topUp(1L, 500)).thenReturn(0);
        assertThatThrownBy(() -> balanceService.topUp(1L, 500))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("余额卡不存在");
    }
}
