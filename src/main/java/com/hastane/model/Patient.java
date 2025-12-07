package com.hastane.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Patient extends User {

    public Patient(String tcKimlikNo, String ad, String soyad, String sifre) {
        super(tcKimlikNo, ad, soyad, sifre, "HASTA");
    }

    @Override
    public void ekraniGoster() {
        System.out.println("Hasta Paneli Açılıyor: " + getAd() + " " + getSoyad());
        // İleride buraya GUI kodu (PatientDashboard) bağlanacak
    }
}