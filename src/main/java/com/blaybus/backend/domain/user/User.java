package com.blaybus.backend.domain.user;

import com.blaybus.backend.global.entity.BaseTimeEntity;
import com.blaybus.backend.global.enum_type.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role; // MENTOR, MENTEE

    private String profileImage;

    @Builder
    public User(String username, String password, String name, String nickname, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.role = role;
    }
}
