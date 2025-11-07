package app.nail.unit;

import app.nail.application.service.BalanceService;
import app.nail.application.service.AuditService;
import app.nail.domain.entity.Consumption;
import app.nail.domain.entity.SavingsCard;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** English: Unit test for balance flows. */
@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock SavingsCardRepository cardRepo;
    @Mock SavingsPendingRepository pendingRepo;
    @Mock ConsumptionRepository consRepo;
    @Mock AuditService auditService;

    @InjectMocks BalanceService balanceService;

    @Test
    void topUp_shouldWriteConsumption_whenCardUpdated() {
        when(cardRepo.lockByUserId(1L)).thenReturn(Optional.of(SavingsCard.builder().userId(1L).build()));
        when(cardRepo.topUp(1L, 500)).thenReturn(1);
        when(consRepo.save(any(Consumption.class))).thenAnswer(invocation -> {
            Consumption c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });
        ArgumentCaptor<Consumption> captor = ArgumentCaptor.forClass(Consumption.class);

        balanceService.topUp(1L, 500);

        verify(consRepo).save(captor.capture());
        assertThat(captor.getValue().getKind()).isEqualTo(ConsumeType.TOP_UP);
        assertThat(captor.getValue().getAmountCents()).isEqualTo(500);
    }

    @Test
    void topUp_shouldFail_whenCardMissing() {
        when(cardRepo.lockByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> balanceService.topUp(1L, 500))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("余额卡不存在");
    }
}
