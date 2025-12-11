package com.hastane.state;

import com.hastane.model.Appointment;

public class MissedState implements IAppointmentState {

    @Override
    public void handle(Appointment context) {
        System.out.println("Durum Değişimi: Hasta randevuya GELMEDİ olarak işaretlendi.");
    }

    @Override
    public String getDurumAdi() {
        return "GELMEDİ";
    }
}