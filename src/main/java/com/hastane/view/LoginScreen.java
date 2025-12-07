package com.hastane.view;

import com.hastane.model.Doctor;
import com.hastane.model.Patient;
import com.hastane.model.User;
import com.hastane.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField txtTc;
    private JPasswordField txtSifre;
    private JButton btnGiris;
    private AuthService authService;

    public LoginScreen() {
        authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Hastane Sistemi");
        setSize(400, 450); // Yükseklik biraz arttı
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // Pencere boyutu sabit kalsın, bozulmasın

        // Ana Panel (Kenarlardan boşluklu)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40)); // İçeriden 40px boşluk
        mainPanel.setBackground(Color.WHITE); // Arka plan beyaz

        // 1. Logo veya Başlık Kısmı
        JLabel lblTitle = new JLabel("Hoşgeldiniz");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setForeground(new Color(50, 50, 50));

        JLabel lblSubTitle = new JLabel("Lütfen bilgilerinizi giriniz");
        lblSubTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSubTitle.setForeground(Color.GRAY);

        // 2. Form Alanları
        JLabel lblTc = new JLabel("TC Kimlik No");
        lblTc.setAlignmentX(Component.LEFT_ALIGNMENT); // Sola dayalı başlık

        txtTc = new JTextField();
        txtTc.setPreferredSize(new Dimension(300, 35));
        txtTc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Yüksekliği sabitle

        JLabel lblPass = new JLabel("Şifre");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSifre = new JPasswordField();
        txtSifre.setPreferredSize(new Dimension(300, 35));
        txtSifre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // 3. Buton (Mavi ve Modern)
        btnGiris = new JButton("Giriş Yap");
        btnGiris.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGiris.setBackground(new Color(33, 150, 243)); // Modern Mavi
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setFocusPainted(false);
        btnGiris.setPreferredSize(new Dimension(300, 40));
        btnGiris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnGiris.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Üzerine gelince el işareti

        // Olay (Action)
        btnGiris.addActionListener(e -> girisYap());

        // Bileşenleri Panele Ekle (Aralara boşluk koyarak)
        mainPanel.add(Box.createVerticalGlue()); // Üstten biraz esnek boşluk
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(lblSubTitle);
        mainPanel.add(Box.createVerticalStrut(30)); // Başlık ile form arası boşluk

        // TC Alanı
        JPanel pnlTcLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTcLabel.setBackground(Color.WHITE);
        pnlTcLabel.add(lblTc);
        pnlTcLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        mainPanel.add(pnlTcLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtTc);
        mainPanel.add(Box.createVerticalStrut(15));

        // Şifre Alanı
        JPanel pnlPassLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlPassLabel.setBackground(Color.WHITE);
        pnlPassLabel.add(lblPass);
        pnlPassLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        mainPanel.add(pnlPassLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtSifre);
        mainPanel.add(Box.createVerticalStrut(30));

        // Buton
        mainPanel.add(btnGiris);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        setVisible(true);
    }

    private void girisYap() {
        String tc = txtTc.getText();
        String sifre = new String(txtSifre.getPassword());

        User user = authService.login(tc, sifre);

        if (user != null) {
            this.dispose();
            if (user instanceof Patient) {
                new PatientDashboard((Patient) user);
            } else if (user instanceof Doctor) {
                new DoctorDashboard((Doctor) user);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Hatalı TC veya Şifre!", "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }
}