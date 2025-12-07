package com.hastane.factory;

import com.hastane.model.Doctor;
import com.hastane.model.Patient;
import com.hastane.model.User;

public class UserFactory {

    // GEREKSİNİM: Factory Design Pattern
    public static User createUser(String type, String tc, String ad, String soyad, String sifre, String brans) {
        if (type.equalsIgnoreCase("HASTA")) {
            return new Patient(tc, ad, soyad, sifre);
        } else if (type.equalsIgnoreCase("DOKTOR")) {
            return new Doctor(tc, ad, soyad, sifre, brans);
        } else {
            throw new IllegalArgumentException("Geçersiz kullanıcı tipi: " + type);
        }
    }
}