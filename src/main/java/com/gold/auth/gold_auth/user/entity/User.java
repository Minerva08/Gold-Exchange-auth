package com.gold.auth.gold_auth.user.entity;

import com.gold.auth.gold_auth.user.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="member")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private String userId;

    private String address;

    private Date birth;

    private String password;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    public User() {

    }
    public void updateUserStatus(){
        this.status=MemberStatus.USE;
    }
}
