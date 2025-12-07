package com.hastane.state;

public class CompletedState implements IAppointmentState {
    @Override
    public void handle() {
        System.out.println("Durum İşleniyor: Muayene tamamlandı, geçmişe eklendi.");
    }

    @Override
    public String getDurumAdi() {
        return "TAMAMLANDI";
    }
}