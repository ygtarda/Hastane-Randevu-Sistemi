package com.hastane.model;

import com.hastane.state.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Appointment extends BaseEntity {
    private int doktorId;
    private int hastaId;
    private LocalDateTime tarih;

    // Veritabanı uyumluluğu için String tutmaya devam ediyoruz
    private String durum;

    // PATTERN GEREĞİ: State nesnesini burada tutuyoruz
    private IAppointmentState state;

    private String notlar;

    // UI alanları
    private String doktorAdi;
    private String hastaAdi;
    private String doktorBrans;

    public Appointment(int id, int doktorId, int hastaId, LocalDateTime tarih, String durum, String notlar) {
        this.setId(id);
        this.doktorId = doktorId;
        this.hastaId = hastaId;
        this.tarih = tarih;
        this.notlar = notlar;

        // String gelen durumu State nesnesine çeviriyoruz (Init)
        this.durum = durum;
        this.state = StateFactory.getStateByString(durum);
    }

    public Appointment(int doktorId, int hastaId, LocalDateTime tarih) {
        this.doktorId = doktorId;
        this.hastaId = hastaId;
        this.tarih = tarih;
        // Varsayılan durum
        changeState(new PendingState());
    }

    // State Pattern'in kalbi: Durum değiştiğinde bu metod çağrılır
    public void changeState(IAppointmentState newState) {
        this.state = newState;
        // State nesnesi context üzerinde işlem yapabilir (handle)
        this.state.handle(this);
        // String alanını da güncelleyelim ki veritabanına bu yazılsın
        this.durum = this.state.getDurumAdi();
    }
}