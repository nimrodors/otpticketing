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
@Table(name = "users")
public class Users {
    @Id
    @Column(name = "userid")
    private int userId;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
}
