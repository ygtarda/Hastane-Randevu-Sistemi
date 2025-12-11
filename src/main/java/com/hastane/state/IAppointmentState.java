package com.hastane.state;

import com.hastane.model.Appointment;

public interface IAppointmentState {
    // Context (Appointment) nesnesini parametre olarak alıyoruz
    void handle(Appointment context);
    String getDurumAdi();
}