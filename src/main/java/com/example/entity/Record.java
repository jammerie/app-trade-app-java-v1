package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Record")
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;
    
    // Getters and Setters
    
    @Column(name = "BuyerParty")
    private String BuyerParty;
    public String getBuyerParty () {
        return BuyerParty;
    }

    public void setBuyerParty(String BuyerParty) {
        this.BuyerParty = BuyerParty;
    }
        
    @Column(name = "SellerParty")
    private String SellerParty;
    public String getSellerParty () {
        return SellerParty;
    }

    public void setSellerParty(String SellerParty) {
        this.SellerParty = SellerParty;
    }
        
    @Column(name = "Amount")
    private String Amount;
    public String getAmount () {
        return Amount;
    }

    public void setAmount(String Amount) {
        this.Amount = Amount;
    }
        
    @Column(name = "Currency")
    private String Currency;
    public String getCurrency () {
        return Currency;
    }

    public void setCurrency(String Currency) {
        this.Currency = Currency;
    }
        
}