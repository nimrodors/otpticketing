package com.example.otp.dto.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usertoken")
public class UserToken {
    @Id
    @Column(name = "userid")
    private int userId;
    @Column(name = "token")
    private String token;
}
