package com.paymentsbe.user.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor
public class User extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    public User(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}