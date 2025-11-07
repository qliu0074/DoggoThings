package app.nail.domain.repository;

import app.nail.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储
 * 作用：增删改查用户，按手机号或微信标识检索
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 按手机号哈希查找（避免存储明文） */
    Optional<User> findByPhoneHash(String phoneHash);

    /** 微信 openid 查找（微信小程序场景） */
    User findByWxOpenid(String openid);

    /** 微信 unionid 查找（同主体多应用场景） */
    User findByWxUnionid(String unionid);

    /** 模糊查昵称，分页返回 */
    Page<User> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);

    /** 查找待脱敏的历史手机号记录 */
    List<User> findByPhoneIsNotNullAndPhoneHashIsNull();

    /** 查找缺少密文的手机号记录 */
    List<User> findByPhoneIsNotNullAndPhoneEncIsNull();
}
