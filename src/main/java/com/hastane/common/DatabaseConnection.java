package com.hastane.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. Statik ve private instance (tekil örnek)
    private static DatabaseConnection instance;
    private Connection connection;

    // Bağlantı bilgileri (Kendi bilgisayarına göre güncellemelisin)
    private final String URL = "jdbc:mysql://localhost:3306/hastane_db";
    private final String USER = "root";     // Genelde "root"tur
    private final String PASSWORD = "77117711aa";     // Şifren varsa buraya yaz (XAMPP kullanıyorsan boş bırak)

    // 2. Private constructor (Dışarıdan 'new' ile üretilmesini engeller)
    private DatabaseConnection() {
        try {
            // Sürücüyü yükle
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Bağlantıyı aç
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Veritabanı bağlantısı BAŞARILI!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Veritabanı bağlantısı BAŞARISIZ!");
            e.printStackTrace();
        }
    }

    // 3. Global erişim noktası (Thread-safe olması için synchronized eklenebilir ama şart değil)
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}