package com.hastane.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class User extends BaseEntity {
    protected String tcKimlikNo;
    protected String ad;
    protected String soyad;
    protected String sifre;
    protected String rol; // "HASTA" veya "DOKTOR"

    // --- YENİ EKLENEN ALANLAR ---
    protected String telefon;
    protected String email;

    public User(String tcKimlikNo, String ad, String soyad, String sifre, String rol) {
        super();
        this.tcKimlikNo = tcKimlikNo;
        this.ad = ad;
        this.soyad = soyad;
        this.sifre = sifre;
        this.rol = rol;
    }

    public abstract void ekraniGoster();
}