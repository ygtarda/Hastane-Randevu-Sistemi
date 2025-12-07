package com.hastane.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Doctor extends User {
    private String brans; // Doktorun uzmanlık alanı

    public Doctor(String tcKimlikNo, String ad, String soyad, String sifre, String brans) {
        super(tcKimlikNo, ad, soyad, sifre, "DOKTOR");
        this.brans = brans;
    }

    @Override
    public void ekraniGoster() {
        System.out.println("Doktor Paneli Açılıyor: Dr. " + getAd() + " " + getSoyad());
        // İleride buraya GUI kodu (DoctorDashboard) bağlanacak
    }


    // Bu metodu eklemezsen Combobox'ta saçma sapan kodlar yazar.
    @Override
    public String toString() {
        return "Dr. " + getAd() + " " + getSoyad() + " (" + getBrans() + ")";
    }
} // Sınıfın bittiği yer
