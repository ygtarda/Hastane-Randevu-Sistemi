package com.hastane.state;

import com.hastane.model.Appointment;

public class CancelledState implements IAppointmentState {

    @Override
    public void handle(Appointment context) {
        // İptal durumunda yapılacak ekstra işlemler buraya eklenebilir.
        // Örneğin: Loglama, SMS servisine bildirim tetikleme vb.
        System.out.println("Durum Değişimi: Randevu İPTAL EDİLDİ. Slot boşa çıktı.");
    }

    @Override
    public String getDurumAdi() {
        return "İPTAL EDİLDİ";
    }
}