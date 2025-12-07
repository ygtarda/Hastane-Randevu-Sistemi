package com.hastane;

import com.formdev.flatlaf.FlatLightLaf; // Bu importu ekle
import com.hastane.view.LoginScreen;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // 1. Temayı Ayarla (FlatLaf)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Tema yüklenemedi!");
        }

        // 2. Uygulamayı Başlat
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen();
            }
        });
    }
}