package com.hastane.state;

import com.hastane.model.Appointment;

public class ConfirmedState implements IAppointmentState {
    @Override
    public void handle(Appointment context) {
        // State pattern burada aktif rol oynuyor.
        // Örneğin: Durum ONAYLANDI olduğunda, context üzerindeki durum stringini güncelliyoruz.
        System.out.println("Durum Değişimi: Randevu ONAYLANDI statüsüne geçti.");

        // Burada ileride ekstra mantıklar olabilir (örn: context.setNotlar("Otomatik onay"))
    }

    @Override
    public String getDurumAdi() {
        return "ONAYLANDI";
    }
}