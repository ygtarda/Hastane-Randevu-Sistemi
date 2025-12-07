package com.hastane.service;

import com.hastane.common.DatabaseConnection;
import com.hastane.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    // Tüm branşları getir (Distinct)
    public List<String> getTumBranslar() {
        List<String> branslar = new ArrayList<>();
        String sql = "SELECT DISTINCT brans FROM users WHERE rol = 'DOKTOR' AND brans IS NOT NULL";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { branslar.add(rs.getString("brans")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return branslar;
    }

    // Branşa göre doktorları getir
    public List<Doctor> getDoktorlarByBrans(String brans) {
        List<Doctor> doktorlar = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE rol = 'DOKTOR' AND brans = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, brans);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Doctor d = new Doctor(rs.getString("tc_kimlik_no"), rs.getString("ad"),
                        rs.getString("soyad"), rs.getString("sifre"), rs.getString("brans"));
                d.setId(rs.getInt("id"));
                d.setTelefon(rs.getString("telefon")); // Telefonu da çekelim
                d.setEmail(rs.getString("email"));
                doktorlar.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return doktorlar;
    }

    // Tüm doktorlar (eski metod, uyumluluk için kalsın)
    public List<Doctor> tumDoktorlariGetir() {
        // ... (Bu metodun içi aynı kalabilir ama genelde branş filtresi kullanacağız)
        return getDoktorlarByBrans("Kardiyoloji"); // Örnek
    }

    // DoctorService.java içine ekle:

    // 1. Çalışma Saati Ekle / Güncelle
    public boolean calismaSaatiEkle(int doktorId, String gun, String baslangic, String bitis) {
        // Önce o gün için eski kaydı silelim (Basitlik için)
        String delSql = "DELETE FROM doctor_availability WHERE doctor_id = ? AND gun = ?";
        String insSql = "INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                delStmt.setInt(1, doktorId);
                delStmt.setString(2, gun);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                insStmt.setInt(1, doktorId);
                insStmt.setString(2, gun);
                insStmt.setString(3, baslangic);
                insStmt.setString(4, bitis);
                return insStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 2. Doktorun O Günkü Çalışma Saatlerini Getir (Yoksa null döner)
// Dönüş: String[] { "09:00", "12:00" } gibi
    public String[] getCalismaSaatleri(int doktorId, String gun) {
        String sql = "SELECT baslangic, bitis FROM doctor_availability WHERE doctor_id = ? AND gun = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            stmt.setString(2, gun);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[] { rs.getString("baslangic"), rs.getString("bitis") };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; // O gün çalışmıyor demek
    }
}