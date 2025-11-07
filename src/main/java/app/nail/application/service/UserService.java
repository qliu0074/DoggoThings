package app.nail.application.service;

import app.nail.common.exception.ApiException;
import app.nail.domain.entity.User;
import app.nail.domain.repository.UserRepository;
import app.nail.domain.service.PhoneProtector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户服务
 * 职责：
 * 1. 注册与基础信息维护
 * 2. 绑定或更新微信 openid/unionid
 * 3. 简单查询（按 id / openid / unionid / 手机号）
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PhoneProtector phoneProtector;

    /** 按主键查用户，找不到返回 Optional */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /** 按手机号查找（内部先哈希再比对） */
    public Optional<User> findByPhone(String phonePlaintext) {
        if (!StringUtils.hasText(phonePlaintext)) {
            return Optional.empty();
        }
        String hash = phoneProtector.hash(phonePlaintext);
        return userRepository.findByPhoneHash(hash);
    }

    /** 新建用户（昵称与手机号示例） */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User createUser(String nickname, String phonePlaintext) {
        User user = User.builder()
                .nickname(nickname)
                .build();
        phoneProtector.apply(user, phonePlaintext);
        return userRepository.save(user);
    }

    /** 绑定微信 openid/unionid（存在则更新，不存在则抛出） */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void bindWeChat(Long userId, String openid, String unionid) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.resourceNotFound("用户不存在"));
        user.setWxOpenid(openid);
        user.setWxUnionid(unionid);
        userRepository.save(user);
    }

    /** 修改昵称或手机号（演示用） */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateProfile(Long userId, String nickname, String phonePlaintext) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.resourceNotFound("用户不存在"));
        user.setNickname(nickname);
        phoneProtector.apply(user, phonePlaintext);
        userRepository.save(user);
    }
}
