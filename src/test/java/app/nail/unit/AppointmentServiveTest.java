package app.nail.unit;

import app.nail.application.service.AppointmentService;
import app.nail.application.service.BalanceService;
import app.nail.domain.entity.Appointment;
import app.nail.domain.entity.ServiceItem;
import app.nail.domain.entity.User;
import app.nail.domain.enums.ApptStatus;
import app.nail.domain.enums.PaymentMethod;
import app.nail.domain.repository.AppointmentItemRepository;
import app.nail.domain.repository.AppointmentRepository;
import app.nail.domain.repository.ServiceItemRepository;
import app.nail.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** English: Unit test focusing on price sum and balance freezing. */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock AppointmentRepository apptRepo;
    @Mock AppointmentItemRepository itemRepo;
    @Mock ServiceItemRepository serviceRepo;
    @Mock UserRepository userRepo;
    @Mock BalanceService balanceService;

    @InjectMocks AppointmentService apptService;

    @Test
    void book_shouldSumItems_andFreezeBalance() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(serviceRepo.findById(10L)).thenReturn(Optional.of(ServiceItem.builder().id(10L).priceCents(100).build()));
        when(serviceRepo.findById(11L)).thenReturn(Optional.of(ServiceItem.builder().id(11L).priceCents(200).build()));
        when(apptRepo.existsByUserIdAndAppointmentAt(eq(1L), any())).thenReturn(false);

        when(apptRepo.save(any(Appointment.class))).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setId(99L);
            return a;
        });

        var items = List.of(
                new AppointmentService.ServiceItemDTO(10L, 2),
                new AppointmentService.ServiceItemDTO(11L, 1)
        );

        Long id = apptService.book(1L, OffsetDateTime.now(), items, true);

        assertThat(id).isEqualTo(99L);
        verify(balanceService).freeze(1L, 400); // 100*2 + 200*1
        verify(apptRepo).save(argThat(a ->
                a.getTotalCents() == 400 &&
                a.getPayMethod() == PaymentMethod.BALANCE &&
                a.getStatus() == ApptStatus.UNCONFIRMED));
    }
}
