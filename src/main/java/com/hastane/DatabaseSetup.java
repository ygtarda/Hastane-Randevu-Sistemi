package com.hastane;

import com.hastane.common.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        System.out.println("Veritabanı kontrol ediliyor...");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            if (conn == null) {
                System.out.println("❌ Veritabanı bağlantısı yok!");
                return;
            }

            // 1. Users Tablosu
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "tc_kimlik_no VARCHAR(11) NOT NULL UNIQUE, " +
                    "ad VARCHAR(50) NOT NULL, " +
                    "soyad VARCHAR(50) NOT NULL, " +
                    "sifre VARCHAR(50) NOT NULL, " +
                    "rol VARCHAR(10) NOT NULL, " +
                    "brans VARCHAR(50), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 2. Appointments Tablosu
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "doktor_id INT NOT NULL, " +
                    "hasta_id INT NOT NULL, " +
                    "tarih DATETIME NOT NULL, " +
                    "durum VARCHAR(20) DEFAULT 'BEKLEMEDE', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (doktor_id) REFERENCES users(id), " +
                    "FOREIGN KEY (hasta_id) REFERENCES users(id)" +
                    ")");

            // 3. EKSİK SÜTUNLARI EKLE (Varsa hata verir, catch ile yakalarız)
            try {
                stmt.executeUpdate("ALTER TABLE appointments ADD COLUMN notlar TEXT");
                System.out.println("✅ 'notlar' sütunu eklendi/kontrol edildi.");
            } catch (Exception e) { /* Zaten var */ }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN telefon VARCHAR(20)");
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN email VARCHAR(100)");
                System.out.println("✅ İletişim sütunları eklendi/kontrol edildi.");
            } catch (Exception e) { /* Zaten var */ }

            // 4. YENİ: Doktor Çalışma Saatleri Tablosu
            String sqlAvailability = "CREATE TABLE IF NOT EXISTS doctor_availability (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "doctor_id INT NOT NULL, " +
                    "gun VARCHAR(20) NOT NULL, " +
                    "baslangic VARCHAR(5) NOT NULL, " +
                    "bitis VARCHAR(5) NOT NULL, " +
                    "FOREIGN KEY (doctor_id) REFERENCES users(id)" +
                    ")";
            stmt.executeUpdate(sqlAvailability);
            System.out.println("✅ 'doctor_availability' tablosu hazır.");

            System.out.println("🎉 VERİTABANI KURULUMU TAMAMLANDI!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}