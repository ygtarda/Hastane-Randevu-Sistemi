package com.hastane.state;

public class ConfirmedState implements IAppointmentState {
    @Override
    public void handle() {
        // İleride buraya SMS gönderme vb. eklenebilir
        System.out.println("Durum İşleniyor: Randevu onaylandı, hasta bekleniyor.");
    }

    @Override
    public String getDurumAdi() {
        return "ONAYLANDI";
    }
}