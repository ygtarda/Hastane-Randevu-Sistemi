package com.hastane;

import com.formdev.flatlaf.FlatDarkLaf; // Dark temayı import et
import com.formdev.flatlaf.FlatIntelliJLaf; // Veya bunu dene (IntelliJ Teması)
import com.hastane.view.LoginScreen;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Burayı değiştir: FlatDarkLaf() veya FlatIntelliJLaf()
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // Butonlara yuvarlak kenar eklemek için ekstra ayar:
            UIManager.put( "Button.arc", 15 );
            UIManager.put( "Component.arc", 10 );
            UIManager.put( "TextComponent.arc", 10 );

        } catch (Exception ex) {
            System.err.println("Tema yüklenemedi!");
        }

        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}