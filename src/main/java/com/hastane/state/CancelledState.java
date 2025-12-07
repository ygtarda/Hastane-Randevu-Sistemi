package com.hastane.state;

public class CancelledState implements IAppointmentState {
    @Override
    public void handle() {
        System.out.println("Durum İşleniyor: Randevu iptal edildi. Slot boşa çıktı.");
    }

    @Override
    public String getDurumAdi() {
        return "İPTAL EDİLDİ";
    }
}