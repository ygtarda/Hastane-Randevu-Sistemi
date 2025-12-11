package com.hastane.state;

import com.hastane.model.Appointment;

public class PendingState implements IAppointmentState {

    @Override
    public void handle(Appointment context) {
        // Randevu ilk oluşturulduğunda veya beklemeye alındığında çalışır
        System.out.println("Durum Değişimi: Randevu BEKLEMEDE statüsüne geçti.");
    }

    @Override
    public String getDurumAdi() {
        return "BEKLEMEDE";
    }
}