package app.nail.application.service;

import app.nail.domain.entity.User;
import app.nail.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务
 * 职责：
 * 1. 注册与基础信息维护
 * 2. 绑定或更新微信 openid/unionid
 * 3. 简单查询（按 id / openid / unionid / phone）
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** 按主键查用户，找不到返回空 Optional */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /** 新建用户（仅示例：昵称与手机号） */
    @Transactional
    public User createUser(String nickname, String phone) {
        User u = User.builder()
                .nickname(nickname)
                .phone(phone)
                .build();
        return userRepository.save(u);
    }

    /** 绑定微信 openid/unionid（存在则更新，不存在则抛出） */
    @Transactional
    public void bindWeChat(Long userId, String openid, String unionid) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setWxOpenid(openid);
        user.setWxUnionid(unionid);
        userRepository.save(user);
    }

    /** 修改昵称或手机号（演示用） */
    @Transactional
    public void updateProfile(Long userId, String nickname, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setNickname(nickname);
        user.setPhone(phone);
        userRepository.save(user);
    }
}
