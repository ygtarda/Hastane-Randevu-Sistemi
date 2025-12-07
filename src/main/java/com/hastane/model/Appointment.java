package com.hastane.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Appointment extends BaseEntity {
    private int doktorId;
    private int hastaId;
    private LocalDateTime tarih;
    private String durum;
    private String notlar; // Doktor notu

    // Ekranda göstermek için eklediğimiz geçici alanlar (Veritabanında sütun olarak yok, JOIN ile dolduracağız)
    private String doktorAdi;
    private String hastaAdi;
    private String doktorBrans;

    public Appointment(int id, int doktorId, int hastaId, LocalDateTime tarih, String durum, String notlar) {
        this.setId(id);
        this.doktorId = doktorId;
        this.hastaId = hastaId;
        this.tarih = tarih;
        this.durum = durum;
        this.notlar = notlar;
    }

    public Appointment(int doktorId, int hastaId, LocalDateTime tarih) {
        this.doktorId = doktorId;
        this.hastaId = hastaId;
        this.tarih = tarih;
        this.durum = "BEKLEMEDE";
    }
}