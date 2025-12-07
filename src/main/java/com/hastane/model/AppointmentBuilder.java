package com.hastane.model; // BU SATIR ÇOK ÖNEMLİ

import java.time.LocalDateTime;

public class AppointmentBuilder {
    private int doktorId;
    private int hastaId;
    private LocalDateTime tarih;

    // Zincirleme metodlar (Fluent Interface)
    public AppointmentBuilder setDoktorId(int doktorId) {
        this.doktorId = doktorId;
        return this;
    }

    public AppointmentBuilder setHastaId(int hastaId) {
        this.hastaId = hastaId;
        return this;
    }

    public AppointmentBuilder setTarih(LocalDateTime tarih) {
        this.tarih = tarih;
        return this;
    }

    public Appointment build() {
        // Appointment sınıfı aynı pakette (com.hastane.model) olduğu için import gerekmez.
        return new Appointment(doktorId, hastaId, tarih);
    }
}
