package com.blaybus.backend.domain.content;

import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "memos")
public class Memo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @Column(nullable = false, length = 1000)
    private String content;

    //메모 조회
    public Memo(User mentor, User mentee, String content) {
        this.mentor = mentor;
        this.mentee = mentee;
        this.content = content;
    }

    //메모 작성
    public static Memo create(User mentor, User mentee, String content) {
        return new Memo(mentor, mentee, content);
    }

}