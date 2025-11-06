package app.nail.interfaces.client.controller;

import app.nail.application.service.BalanceService;
import app.nail.interfaces.client.dto.ClientBalanceDtos.TopUpReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** English: Client balance controller (top-up). */
@RestController
@RequestMapping("/api/client/balance")
@RequiredArgsConstructor
public class ClientBalanceController {

    private final BalanceService balanceService;

    /** English: Top up balance; usually validated via payment callback. */
    @PostMapping("/{userId}/top-up")
    public void topUp(@PathVariable long userId, @RequestBody TopUpReq req) {
        balanceService.topUp(userId, req.amountCents());
    }
}
