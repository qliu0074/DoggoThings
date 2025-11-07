package app.nail.infra.bootstrap;

import app.nail.domain.entity.User;
import app.nail.domain.repository.UserRepository;
import app.nail.domain.service.PhoneProtector;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * English: One-time backfill that masks and encrypts legacy phone numbers on startup.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PhoneSanitizerRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PhoneProtector phoneProtector;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> missingHash = userRepository.findByPhoneIsNotNullAndPhoneHashIsNull();
        List<User> missingCipher = userRepository.findByPhoneIsNotNullAndPhoneEncIsNull();
        if (CollectionUtils.isEmpty(missingHash) && CollectionUtils.isEmpty(missingCipher)) {
            return;
        }

        Map<Long, User> toFix = new LinkedHashMap<>();
        if (missingHash != null) {
            missingHash.forEach(user -> toFix.put(user.getId(), user));
        }
        if (missingCipher != null) {
            missingCipher.forEach(user -> toFix.put(user.getId(), user));
        }

        if (toFix.isEmpty()) {
            return;
        }

        log.info("Sanitizing {} legacy phone records", toFix.size());
        for (User user : toFix.values()) {
            String rawPhone = user.getPhone();
            if (!StringUtils.hasText(rawPhone) || phoneProtector.isMasked(rawPhone)) {
                continue;
            }
            phoneProtector.apply(user, rawPhone);
        }
        userRepository.saveAll(toFix.values());
    }
}
