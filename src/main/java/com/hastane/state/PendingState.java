package com.hastane.state;

public class PendingState implements IAppointmentState {

    @Override
    public void handle() {
        System.out.println("Durum İşleniyor: Randevu şu an beklemede.");
    }

    @Override
    public String getDurumAdi() {
        return "BEKLEMEDE";
    }
}