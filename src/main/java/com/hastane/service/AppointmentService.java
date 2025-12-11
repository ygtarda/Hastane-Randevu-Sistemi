package com.hastane.service;

import com.hastane.common.DatabaseConnection;
import com.hastane.model.Appointment;
import com.hastane.observer.ConsoleLoggerObserver;
import com.hastane.observer.INotificationObserver;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private List<INotificationObserver> observers;

    public AppointmentService() {
        this.observers = new ArrayList<>();
        // Varsayılan gözlemciyi ekle
        this.observers.add(new ConsoleLoggerObserver());
    }

    public boolean randevuAl(int doktorId, int hastaId, LocalDateTime tarih) {
        // ÇAKIŞMA KONTROLÜ
        if (isSlotOccupied(doktorId, tarih)) {
            System.out.println("❌ HATA: Bu saatte doktorun başka randevusu var!");
            return false;
        }

        String sql = "INSERT INTO appointments (doktor_id, hasta_id, tarih, durum) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setInt(2, hastaId);
            stmt.setTimestamp(3, Timestamp.valueOf(tarih));
            stmt.setString(4, "BEKLEMEDE");

            boolean sonuc = stmt.executeUpdate() > 0;
            if (sonuc) {
                notifyObservers("Yeni randevu alındı. Hasta ID: " + hastaId + " - Tarih: " + tarih);
            }
            return sonuc;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getRandevularByTarihAraligi(int userId, LocalDate baslangic, LocalDate bitis, boolean isDoctor) {
        return aramaVeFiltrele(userId, baslangic, bitis, "", isDoctor);
    }

    public List<Appointment> getRandevularByHasta(int hastaId) {
        return aramaVeFiltrele(hastaId, null, null, "", false);
    }

    public List<Appointment> getRandevularByDoktor(int doktorId) {
        return aramaVeFiltrele(doktorId, null, null, "", true);
    }

    public List<String> getDoluSaatler(int doktorId, String tarihGun) {
        List<String> doluSaatler = new ArrayList<>();
        // İptal edilenler dolu saat listesine girmesin diye durum kontrolü
        String sql = "SELECT DATE_FORMAT(tarih, '%H:%i') as saat FROM appointments " +
                "WHERE doktor_id = ? AND DATE(tarih) = ? AND durum != 'İPTAL EDİLDİ'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setString(2, tarihGun);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                doluSaatler.add(rs.getString("saat"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doluSaatler;
    }

    public boolean randevuDurumGuncelle(int randevuId, String yeniDurum) {
        String sql = "UPDATE appointments SET durum = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, yeniDurum);
            stmt.setInt(2, randevuId);
            boolean sonuc = stmt.executeUpdate() > 0;
            if (sonuc) {
                notifyObservers("Randevu durumu güncellendi ID: " + randevuId + " -> " + yeniDurum);
            }
            return sonuc;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean notEkle(int randevuId, String not) {
        String sql = "UPDATE appointments SET notlar = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, not);
            stmt.setInt(2, randevuId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> aramaVeFiltrele(int userId, LocalDate baslangic, LocalDate bitis, String aramaMetni, boolean isDoctor) {
        List<Appointment> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        if (isDoctor) {
            sql.append("SELECT a.*, u.ad as h_ad, u.soyad as h_soyad, u.tc_kimlik_no as h_tc FROM appointments a ")
                    .append("JOIN users u ON a.hasta_id = u.id ")
                    .append("WHERE a.doktor_id = ? ");
        } else {
            sql.append("SELECT a.*, u.ad as dr_ad, u.soyad as dr_soyad, u.brans as dr_brans FROM appointments a ")
                    .append("JOIN users u ON a.doktor_id = u.id ")
                    .append("WHERE a.hasta_id = ? ");
        }

        if (baslangic != null && bitis != null) {
            sql.append("AND DATE(a.tarih) BETWEEN ? AND ? ");
        }

        if (aramaMetni != null && !aramaMetni.trim().isEmpty()) {
            sql.append("AND (u.ad LIKE ? OR u.soyad LIKE ? OR a.durum LIKE ? ");
            if (isDoctor) sql.append("OR u.tc_kimlik_no LIKE ? ");
            else sql.append("OR u.brans LIKE ? ");
            sql.append(") ");
        }

        sql.append("ORDER BY a.tarih DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setInt(index++, userId);

            if (baslangic != null && bitis != null) {
                stmt.setDate(index++, Date.valueOf(baslangic));
                stmt.setDate(index++, Date.valueOf(bitis));
            }

            if (aramaMetni != null && !aramaMetni.trim().isEmpty()) {
                String desen = "%" + aramaMetni + "%";
                stmt.setString(index++, desen);
                stmt.setString(index++, desen);
                stmt.setString(index++, desen);
                stmt.setString(index++, desen);
            }

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public boolean randevuGuncelle(int randevuId, LocalDateTime yeniTarih) {
        Appointment mevcut = getAppointmentById(randevuId);
        if (mevcut == null) return false;

        if (isSlotOccupied(mevcut.getDoktorId(), yeniTarih)) {
            return false;
        }

        String sql = "UPDATE appointments SET tarih = ?, durum = 'BEKLEMEDE' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(yeniTarih));
            stmt.setInt(2, randevuId);
            boolean sonuc = stmt.executeUpdate() > 0;
            if (sonuc) notifyObservers("Randevu güncellendi ID: " + randevuId);
            return sonuc;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Appointment getAppointmentById(int id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToAppointment(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private boolean isSlotOccupied(int doktorId, LocalDateTime tarih) {
        // İptal edilen randevular slot işgal etmez
        String sql = "SELECT COUNT(*) FROM appointments WHERE doktor_id = ? AND tarih = ? AND durum != 'İPTAL EDİLDİ'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setTimestamp(2, Timestamp.valueOf(tarih));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void notifyObservers(String mesaj) {
        if (observers != null) {
            for (INotificationObserver observer : observers) {
                observer.update(mesaj);
            }
        }
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

    // --- YENİ EKLENEN METODLAR (AKTİF VE GEÇMİŞ AYRIMI İÇİN) ---

    // HASTA İÇİN: Sadece Gelecek ve İptal Edilmemiş Randevular
    public List<Appointment> getAktifRandevular(int hastaId) {
        List<Appointment> liste = new ArrayList<>();
        // Tarihi şu andan büyük olanlar VE durumu İptal/Gelmedi/Tamamlandı OLMAYANLAR
        String sql = "SELECT a.*, u.ad as dr_ad, u.soyad as dr_soyad, u.brans as dr_brans FROM appointments a " +
                "JOIN users u ON a.doktor_id = u.id " +
                "WHERE a.hasta_id = ? AND a.tarih >= NOW() AND a.durum NOT IN ('İPTAL EDİLDİ', 'GELMEDİ', 'TAMAMLANDI') " +
                "ORDER BY a.tarih ASC";
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

    // HASTA İÇİN: Geçmiş Tarihli VEYA İptal/Tamamlanmış Randevular
    public List<Appointment> getGecmisRandevular(int hastaId) {
        List<Appointment> liste = new ArrayList<>();
        String sql = "SELECT a.*, u.ad as dr_ad, u.soyad as dr_soyad, u.brans as dr_brans FROM appointments a " +
                "JOIN users u ON a.doktor_id = u.id " +
                "WHERE a.hasta_id = ? AND (a.tarih < NOW() OR a.durum IN ('İPTAL EDİLDİ', 'TAMAMLANDI', 'GELMEDİ')) " +
                "ORDER BY a.tarih DESC";
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
}