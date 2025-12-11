package com.hastane.state;

import com.hastane.model.Appointment;

public class CompletedState implements IAppointmentState {

    @Override
    public void handle(Appointment context) {
        // Muayene tamamlandığında çalışır.
        System.out.println("Durum Değişimi: Muayene TAMAMLANDI, geçmiş kayıtlara eklendi.");
    }

    @Override
    public String getDurumAdi() {
        return "TAMAMLANDI";
    }
}