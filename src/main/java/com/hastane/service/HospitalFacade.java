package com.hastane.service;

import com.hastane.model.Appointment;
import com.hastane.model.Doctor;
import com.hastane.model.User;
import java.time.LocalDateTime;
import java.util.List;

// GEREKSİNİM: Ekstra Tasarım Deseni 2 (Facade)
// Tüm servisleri tek bir çatı altında toplayan sınıf.
public class HospitalFacade {
    private AuthService authService;
    private AppointmentService appointmentService;
    private DoctorService doctorService;

    public HospitalFacade() {
        this.authService = new AuthService();
        this.appointmentService = new AppointmentService();
        this.doctorService = new DoctorService();
    }

    // Login İşlemi
    public User girisYap(String tc, String sifre) {
        return authService.login(tc, sifre);
    }

    // Randevu Alma (Builder deseni ile birleştirilebilir)
    public boolean randevuAl(int doktorId, int hastaId, LocalDateTime tarih) {
        return appointmentService.randevuAl(doktorId, hastaId, tarih);
    }

    // Doktorları Listeleme
    public List<Doctor> doktorlariGetir() {
        return doctorService.tumDoktorlariGetir();
    }
}