package com.hastane.service;

import com.hastane.common.DatabaseConnection;
import com.hastane.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    // --- MEVCUT METODLAR (KORUNDU) ---
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
                d.setTelefon(rs.getString("telefon"));
                d.setEmail(rs.getString("email"));
                doktorlar.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return doktorlar;
    }

    public List<Doctor> tumDoktorlariGetir() {
        return getDoktorlarByBrans("Kardiyoloji");
    }

    // --- ÇALIŞMA SAATİ İŞLEMLERİ ---

    public boolean calismaSaatiEkle(int doktorId, String gun, String baslangic, String bitis) {
        // Önce temizle, sonra ekle (Güncelleme mantığı)
        calismaSaatiSil(doktorId, gun);

        String insSql = "INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement insStmt = conn.prepareStatement(insSql)) {
            insStmt.setInt(1, doktorId);
            insStmt.setString(2, gun);
            insStmt.setString(3, baslangic);
            insStmt.setString(4, bitis);
            return insStmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // YENİ: Çalışma Saati Silme (Doktor o gün çalışmıyorsa)
    public boolean calismaSaatiSil(int doktorId, String gun) {
        String delSql = "DELETE FROM doctor_availability WHERE doctor_id = ? AND gun = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement delStmt = conn.prepareStatement(delSql)) {
            delStmt.setInt(1, doktorId);
            delStmt.setString(2, gun);
            return delStmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

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
        return null; // Kayıt yoksa null döner (Bu sayede çalışmadığı anlaşılır)
    }

    public List<String[]> getTumCalismaSaatleriTablosu(int doktorId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT gun, baslangic, bitis FROM doctor_availability WHERE doctor_id = ? " +
                "ORDER BY FIELD(gun, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doktorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String gunIng = rs.getString("gun");
                String gunTr = gunIng;
                switch(gunIng) {
                    case "MONDAY": gunTr = "Pazartesi"; break;
                    case "TUESDAY": gunTr = "Salı"; break;
                    case "WEDNESDAY": gunTr = "Çarşamba"; break;
                    case "THURSDAY": gunTr = "Perşembe"; break;
                    case "FRIDAY": gunTr = "Cuma"; break;
                    case "SATURDAY": gunTr = "Cumartesi"; break;
                    case "SUNDAY": gunTr = "Pazar"; break;
                }
                list.add(new String[]{gunTr, rs.getString("baslangic"), rs.getString("bitis")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}