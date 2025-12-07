package com.hastane.service;

import com.hastane.common.DatabaseConnection;
import com.hastane.model.Appointment;
import com.hastane.observer.ConsoleLoggerObserver;
import com.hastane.observer.INotificationObserver;

import java.sql.*;
import java.time.LocalDate; // BU EKSİKTİ
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private List<INotificationObserver> observers;

    public AppointmentService() {
        this.observers = new ArrayList<>();
        this.observers.add(new ConsoleLoggerObserver());
    }

    // 1. Randevu Al
    public boolean randevuAl(int doktorId, int hastaId, LocalDateTime tarih) {
        String sql = "INSERT INTO appointments (doktor_id, hasta_id, tarih, durum) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setInt(2, hastaId);
            stmt.setTimestamp(3, Timestamp.valueOf(tarih));
            stmt.setString(4, "BEKLEMEDE");

            boolean sonuc = stmt.executeUpdate() > 0;
            if(sonuc) notifyObservers("Yeni randevu alındı. Hasta ID: " + hastaId);
            return sonuc;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 2. Tarih Aralığına Göre Filtreleme (YENİ EKLENEN KRİTİK METOD)
    public List<Appointment> getRandevularByTarihAraligi(int userId, LocalDate baslangic, LocalDate bitis, boolean isDoctor) {
        List<Appointment> liste = new ArrayList<>();
        String sql;

        if (isDoctor) {
            sql = "SELECT a.*, u.ad as h_ad, u.soyad as h_soyad FROM appointments a " +
                    "JOIN users u ON a.hasta_id = u.id " +
                    "WHERE a.doktor_id = ? AND DATE(a.tarih) BETWEEN ? AND ? ORDER BY a.tarih DESC";
        } else {
            sql = "SELECT a.*, u.ad as dr_ad, u.soyad as dr_soyad, u.brans as dr_brans FROM appointments a " +
                    "JOIN users u ON a.doktor_id = u.id " +
                    "WHERE a.hasta_id = ? AND DATE(a.tarih) BETWEEN ? AND ? ORDER BY a.tarih DESC";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(baslangic)); // java.sql.Date çevrimi
            stmt.setDate(3, Date.valueOf(bitis));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment app = mapResultSetToAppointment(rs);
                if (isDoctor) {
                    app.setHastaAdi(rs.getString("h_ad") + " " + rs.getString("h_soyad"));
                } else {
                    app.setDoktorAdi(rs.getString("dr_ad") + " " + rs.getString("dr_soyad"));
                    app.setDoktorBrans(rs.getString("dr_brans"));
                }
                liste.add(app);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    // 3. Hastanın Randevularını Getir
    public List<Appointment> getRandevularByHasta(int hastaId) {
        List<Appointment> liste = new ArrayList<>();
        String sql = "SELECT a.*, u.ad as dr_ad, u.soyad as dr_soyad, u.brans as dr_brans " +
                "FROM appointments a " +
                "JOIN users u ON a.doktor_id = u.id " +
                "WHERE a.hasta_id = ? ORDER BY a.tarih DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hastaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment app = mapResultSetToAppointment(rs);
                app.setDoktorAdi(rs.getString("dr_ad") + " " + rs.getString("dr_soyad"));
                app.setDoktorBrans(rs.getString("dr_brans"));
                liste.add(app);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    // 4. Doktorun Randevularını Getir
    public List<Appointment> getRandevularByDoktor(int doktorId) {
        List<Appointment> liste = new ArrayList<>();
        String sql = "SELECT a.*, u.ad as h_ad, u.soyad as h_soyad " +
                "FROM appointments a " +
                "JOIN users u ON a.hasta_id = u.id " +
                "WHERE a.doktor_id = ? ORDER BY a.tarih DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment app = mapResultSetToAppointment(rs);
                app.setHastaAdi(rs.getString("h_ad") + " " + rs.getString("h_soyad"));
                liste.add(app);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    // 5. Dolu Saatleri Getir
    public List<String> getDoluSaatler(int doktorId, String tarihGun) {
        List<String> doluSaatler = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(tarih, '%H:%i') as saat FROM appointments " +
                "WHERE doktor_id = ? AND DATE(tarih) = ? AND durum != 'İPTAL EDİLDİ'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setString(2, tarihGun);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { doluSaatler.add(rs.getString("saat")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return doluSaatler;
    }

    // 6. Durum Güncelleme
    public boolean randevuDurumGuncelle(int randevuId, String yeniDurum) {
        String sql = "UPDATE appointments SET durum = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, yeniDurum);
            stmt.setInt(2, randevuId);
            boolean sonuc = stmt.executeUpdate() > 0;
            if(sonuc) notifyObservers("Randevu durumu güncellendi: " + yeniDurum);
            return sonuc;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 7. Not Ekleme
    public boolean notEkle(int randevuId, String not) {
        String sql = "UPDATE appointments SET notlar = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, not);
            stmt.setInt(2, randevuId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void notifyObservers(String mesaj) {
        for (INotificationObserver observer : observers) { observer.update(mesaj); }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("doktor_id"),
                rs.getInt("hasta_id"),
                rs.getTimestamp("tarih").toLocalDateTime(),
                rs.getString("durum"),
                rs.getString("notlar")
        );
    }
}