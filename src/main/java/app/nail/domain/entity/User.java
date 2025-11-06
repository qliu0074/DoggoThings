package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

/**
 * 用户实体
 * 对应表：app.users
 * 说明：保存用户基础信息及微信标识，包含手机号哈希与密文字段
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", schema = "app")
public class User {

    /**
     * 主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 昵称
     */
    @Column(length = 60)
    private String nickname;

    /**
     * 明文手机号（可空，唯一）。建议仅在必要场景使用
     */
    @Column(length = 30, unique = true)
    private String phone;

    /**
     * 手机号哈希（SHA-256 等），与明文解耦，用于检索与去重
     */
    @Column(name = "phone_hash", length = 64, unique = true)
    private String phoneHash;

    /**
     * 手机号密文（对称加密存储）
     */
    @Lob
    @Column(name = "phone_enc")
    private byte[] phoneEnc;

    /**
     * 微信 openid / unionid（可空，唯一索引在表层约束）
     */
    @Column(name = "wx_openid", length = 64)
    private String wxOpenid;

    @Column(name = "wx_unionid", length = 64)
    private String wxUnionid;

    /**
     * 创建/更新时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * 乐观锁版本
     */
    @Version
    private Integer version;
}
