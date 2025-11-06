package app.nail.domain.repository;

import app.nail.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户仓储
 * 作用：增删改查用户，按手机号或微信标识检索
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 按手机号精确查找（可用于登录或绑定） */
    User findByPhone(String phone);

    /** 按 openid 查找（微信小程序场景） */
    User findByWxOpenid(String openid);

    /** 按 unionid 查找（同主体多应用场景） */
    User findByWxUnionid(String unionid);

    /** 模糊查昵称，分页返回 */
    Page<User> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);
}
