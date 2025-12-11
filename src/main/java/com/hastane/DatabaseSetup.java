package com.hastane;

import com.hastane.common.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        System.out.println("Veritabanı ve Test Verileri Hazırlanıyor...");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            if (conn == null) {
                System.out.println("❌ Hata: Bağlantı yok!");
                return;
            }

            // 1. Tabloları Oluştur (Yoksa)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "tc_kimlik_no VARCHAR(11) NOT NULL UNIQUE, " +
                    "ad VARCHAR(50), soyad VARCHAR(50), sifre VARCHAR(50), " +
                    "rol VARCHAR(10), brans VARCHAR(50), telefon VARCHAR(20), email VARCHAR(100), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "doktor_id INT, hasta_id INT, tarih DATETIME, " +
                    "durum VARCHAR(20) DEFAULT 'BEKLEMEDE', notlar TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (doktor_id) REFERENCES users(id), FOREIGN KEY (hasta_id) REFERENCES users(id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS doctor_availability (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, doctor_id INT, " +
                    "gun VARCHAR(20), baslangic VARCHAR(5), bitis VARCHAR(5), " +
                    "FOREIGN KEY (doktor_id) REFERENCES users(id))");

            // 2. Önceki Test Verilerini Temizle (TC çakışmasını önlemek için)
            // Not: Gerçek projede DELETE kullanılmaz ama test için hayat kurtarır.
            stmt.executeUpdate("DELETE FROM doctor_availability");
            stmt.executeUpdate("DELETE FROM appointments");
            stmt.executeUpdate("DELETE FROM users");

            // 3. Doktorları Ekle
            System.out.println("👨‍⚕️ Doktorlar ekleniyor...");
            stmt.executeUpdate("INSERT INTO users (tc_kimlik_no, ad, soyad, sifre, rol, brans) VALUES " +
                    "('11', 'Mehmet', 'Öz', '1234', 'DOKTOR', 'Kardiyoloji'), " +
                    "('12', 'Canan', 'Karatay', '1234', 'DOKTOR', 'Dahiliye'), " +
                    "('13', 'İlber', 'Ortaylı', '1234', 'DOKTOR', 'Göz')");

            // 4. Hastaları Ekle
            System.out.println("ea Hastalar ekleniyor...");
            stmt.executeUpdate("INSERT INTO users (tc_kimlik_no, ad, soyad, sifre, rol, telefon) VALUES " +
                    "('21', 'Ahmet', 'Yılmaz', '1234', 'HASTA', '05551112233'), " +
                    "('22', 'Ayşe', 'Demir', '1234', 'HASTA', '05554445566'), " +
                    "('23', 'Ali', 'Test', '1234', 'HASTA', '05001234567')"); // Senin test kullanıcın

            // 5. Doktor ID'lerini Alıp Çalışma Saatlerini Ekle
            // Mehmet Hoca (ID'si muhtemelen yeni eklendiği için dinamik almalıydık ama sıfırladığımız için 1,2,3 varsayıyoruz)
            // SQL'de AUTO_INCREMENT sıfırlanmadıysa ID'ler kayabilir, bu yüzden subquery kullanıyoruz.

            // Dr. Mehmet (Kardiyoloji) - Hafta İçi 09:00-17:00
            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'MONDAY', '09:00', '17:00' FROM users WHERE tc_kimlik_no='11'");
            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'WEDNESDAY', '09:00', '12:00' FROM users WHERE tc_kimlik_no='11'");

            // Dr. Canan (Dahiliye) - Salı/Perşembe
            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'TUESDAY', '10:00', '16:00' FROM users WHERE tc_kimlik_no='12'");
            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'THURSDAY', '10:00', '16:00' FROM users WHERE tc_kimlik_no='12'");

            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'TUESDAY', '10:00', '16:00' FROM users WHERE tc_kimlik_no='13'");
            stmt.executeUpdate("INSERT INTO doctor_availability (doctor_id, gun, baslangic, bitis) " +
                    "SELECT id, 'THURSDAY', '10:00', '16:00' FROM users WHERE tc_kimlik_no='13'");

            System.out.println("✅ TÜM VERİLER VE AYARLAR YÜKLENDİ!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}