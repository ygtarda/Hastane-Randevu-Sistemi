package com.hastane.service;

import com.hastane.common.DatabaseConnection;
import com.hastane.factory.UserFactory;
import com.hastane.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // 1. Kullanıcı Giriş Kontrolü
    public User login(String tcKimlikNo, String sifre) {
        String sql = "SELECT * FROM users WHERE tc_kimlik_no = ? AND sifre = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tcKimlikNo);
            stmt.setString(2, sifre);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("rol");
                String ad = rs.getString("ad");
                String soyad = rs.getString("soyad");
                String brans = rs.getString("brans");
                int id = rs.getInt("id");

                // Factory ile temel nesneyi oluştur
                User user = UserFactory.createUser(rol, tcKimlikNo, ad, soyad, sifre, brans);
                user.setId(id);

                // --- EKSİK OLAN KISIM BURASIYDI ---
                // Telefon ve Email bilgilerini de veritabanından nesneye yüklüyoruz
                user.setTelefon(rs.getString("telefon"));
                user.setEmail(rs.getString("email"));

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // AuthService.java içindeki profilGuncelle metodunu bununla değiştir:

    public boolean profilGuncelle(int userId, String ad, String soyad, String telefon, String email, String yeniSifre) {
        // Eğer şifre boşsa güncelleme, doluysa güncelle
        String sql;
        boolean sifreDegisecek = (yeniSifre != null && !yeniSifre.trim().isEmpty());

        if (sifreDegisecek) {
            sql = "UPDATE users SET ad = ?, soyad = ?, telefon = ?, email = ?, sifre = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET ad = ?, soyad = ?, telefon = ?, email = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ad);
            stmt.setString(2, soyad);
            stmt.setString(3, telefon);
            stmt.setString(4, email);

            if (sifreDegisecek) {
                stmt.setString(5, yeniSifre);
                stmt.setInt(6, userId);
            } else {
                stmt.setInt(5, userId);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}