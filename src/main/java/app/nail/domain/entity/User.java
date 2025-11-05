package app.nail.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

/** English: Core user table (WeChat + privacy ready). */
@Getter @Setter
@Entity @Table(name="users", schema="app")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(unique = true)
    private String phone;         // English: temporary plaintext for debugging

    @Column(length = 64, name="phone_hash")
    private String phoneHash;     // English: SHA-256(phone)

    @Lob
    @Column(name="phone_enc")
    private byte[] phoneEnc;      // English: AES-GCM payload (optional)

    @Column(name="wx_openid", length=64, unique = false)
    private String wxOpenid;

    @Column(name="wx_unionid", length=64, unique = false)
    private String wxUnionid;

    @Column(name="created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
