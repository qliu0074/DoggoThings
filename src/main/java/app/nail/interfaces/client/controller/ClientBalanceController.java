package app.nail.interfaces.client.controller;

import app.nail.application.service.BalanceService;
import app.nail.common.exception.ApiException;
import app.nail.common.security.PrincipalUser;
import app.nail.interfaces.client.dto.ClientBalanceDtos.TopUpReq;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** English: Client balance controller (top-up). */
@RestController
@RequestMapping("/api/v1/client/balance")
@RequiredArgsConstructor
public class ClientBalanceController {

    private final BalanceService balanceService;

    /** English: Top up balance; usually validated via payment callback. */
    @PostMapping("/top-up")
    public void topUp(@AuthenticationPrincipal PrincipalUser principal,
                      @RequestBody TopUpReq req) {
        Long userId = requireUserId(principal);
        balanceService.topUp(userId, req.amountCents());
    }

    private Long requireUserId(PrincipalUser principal) {
        if (principal == null || principal.id() == null) {
            throw ApiException.unauthorized("未登录或token缺少用户信息");
        }
        return principal.id();
    }
}
