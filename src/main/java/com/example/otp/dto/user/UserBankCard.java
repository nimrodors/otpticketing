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
@Table(name = "userbankcard")
public class UserBankCard {
    @Id
    @Column(name = "userid")
    private int userId;

    @Column(name = "cardid")
    private String cardId;

    @Column(name = "cardnumber")
    private String cardnumber;

    @Column(name = "cvc")
    private int cvc;

    @Column(name = "name")
    private String name;

    @Column(name = "amount")
    private String amount;

    @Column(name = "currency")
    private String currency;
}
