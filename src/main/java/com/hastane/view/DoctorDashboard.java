package com.hastane.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.hastane.model.Appointment;
import com.hastane.model.Doctor;
import com.hastane.service.AppointmentService;
import com.hastane.service.AuthService;
import com.hastane.service.DoctorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DoctorDashboard extends JFrame {
    private Doctor loggedInDoctor;
    private AppointmentService appointmentService;
    private AuthService authService;
    private DoctorService doctorService;

    private JTable tableRandevular;
    private DefaultTableModel tableModel;

    // Profil
    private JTextField txtAd, txtSoyad, txtTel, txtEmail;
    private JPasswordField txtSifre;

    // Filtre
    private DatePicker dpBaslangic, dpBitis;

    public DoctorDashboard(Doctor doctor) {
        this.loggedInDoctor = doctor;
        this.appointmentService = new AppointmentService();
        this.authService = new AuthService();
        this.doctorService = new DoctorService();
        initUI();
    }

    private void initUI() {
        setTitle("Doktor Paneli");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 150, 136));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Dr. " + loggedInDoctor.getAd() + " (" + loggedInDoctor.getBrans() + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.addActionListener(e -> { this.dispose(); new LoginScreen(); });
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnCikis, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // SEKMELER
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("Randevu Listesi", createRandevuPanel());
        tabbedPane.addTab("Çalışma Saatlerim", createCalismaSaatleriPanel()); // YENİ
        tabbedPane.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createRandevuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // FİLTRE PANELİ (YENİ)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Tarih Aralığı Sorgula"));

        filterPanel.add(new JLabel("Başlangıç:"));
        dpBaslangic = new DatePicker();
        dpBaslangic.setDateToToday();
        filterPanel.add(dpBaslangic);

        filterPanel.add(new JLabel("Bitiş:"));
        dpBitis = new DatePicker();
        dpBitis.setDateToToday(); // Varsayılan bugün
        filterPanel.add(dpBitis);

        JButton btnFiltrele = new JButton("Sorgula");
        btnFiltrele.addActionListener(e -> randevulariListele(true)); // Filtreli getir
        filterPanel.add(btnFiltrele);

        JButton btnTumu = new JButton("Tümünü Göster");
        btnTumu.addActionListener(e -> randevulariListele(false));
        filterPanel.add(btnTumu);

        panel.add(filterPanel, BorderLayout.NORTH);

        // TABLO
        String[] columns = {"GizliID", "Hasta Adı", "Tarih", "Saat", "Durum", "Mevcut Not"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableRandevular = new JTable(tableModel);
        tableRandevular.setRowHeight(30);
        tableRandevular.getColumnModel().getColumn(0).setMinWidth(0);
        tableRandevular.getColumnModel().getColumn(0).setMaxWidth(0);

        randevulariListele(false); // İlk açılışta hepsi

        panel.add(new JScrollPane(tableRandevular), BorderLayout.CENTER);

        // BUTONLAR (GELMEDİ EKLENDİ)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnGelmedi = new JButton("GELMEDİ");
        btnGelmedi.setBackground(Color.GRAY); btnGelmedi.setForeground(Color.WHITE);
        btnGelmedi.addActionListener(e -> durumDegistir("GELMEDİ")); // YENİ

        JButton btnTamamla = new JButton("TAMAMLA");
        btnTamamla.setBackground(new Color(40, 167, 69)); btnTamamla.setForeground(Color.WHITE);
        btnTamamla.addActionListener(e -> durumDegistir("TAMAMLANDI"));

        JButton btnNot = new JButton("Not Gir");
        btnNot.addActionListener(e -> notGirisiYap());

        bottomPanel.add(btnNot);
        bottomPanel.add(btnGelmedi);
        bottomPanel.add(btnTamamla);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // YENİ: ÇALIŞMA SAATLERİ AYARLAMA EKRANI
    private JPanel createCalismaSaatleriPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] gunler = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        String[] gunlerTr = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"};

        JComboBox<String> cmbGun = new JComboBox<>(gunlerTr);
        JComboBox<String> cmbBaslangic = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00", "14:00"});
        JComboBox<String> cmbBitis = new JComboBox<>(new String[]{"12:00", "13:00", "14:00", "15:00", "16:00", "17:00"});

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Gün Seçiniz:"), gbc);
        gbc.gridx=1; panel.add(cmbGun, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Başlangıç Saati:"), gbc);
        gbc.gridx=1; panel.add(cmbBaslangic, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Bitiş Saati:"), gbc);
        gbc.gridx=1; panel.add(cmbBitis, gbc);

        JButton btnKaydet = new JButton("Saatleri Kaydet");
        btnKaydet.setBackground(new Color(0, 150, 136));
        btnKaydet.setForeground(Color.WHITE);

        btnKaydet.addActionListener(e -> {
            int index = cmbGun.getSelectedIndex();
            String secilenGun = gunler[index]; // İngilizce gün adını al (MONDAY)
            String bas = (String) cmbBaslangic.getSelectedItem();
            String bit = (String) cmbBitis.getSelectedItem();

            if(doctorService.calismaSaatiEkle(loggedInDoctor.getId(), secilenGun, bas, bit)) {
                JOptionPane.showMessageDialog(this, gunlerTr[index] + " günü için saatler ayarlandı!");
            }
        });

        gbc.gridx=1; gbc.gridy=3; panel.add(btnKaydet, gbc);

        // Bilgi Notu
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2;
        panel.add(new JLabel("<html><i>Not: Ayarlamadığınız günlerde müsait görünmezsiniz.<br>Varsayılan: Ayar yoksa randevu alınamaz.</i></html>"), gbc);

        return panel;
    }

    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 20, 20)); // Satır sayısı arttı
        panel.setBorder(new EmptyBorder(50, 100, 50, 100));

        panel.add(new JLabel("Ad:")); txtAd = new JTextField(loggedInDoctor.getAd()); panel.add(txtAd);
        panel.add(new JLabel("Soyad:")); txtSoyad = new JTextField(loggedInDoctor.getSoyad()); panel.add(txtSoyad);
        panel.add(new JLabel("Tel:")); txtTel = new JTextField(loggedInDoctor.getTelefon()); panel.add(txtTel);
        panel.add(new JLabel("Email:")); txtEmail = new JTextField(loggedInDoctor.getEmail()); panel.add(txtEmail);

        // YENİ: ŞİFRE DEĞİŞTİRME
        panel.add(new JLabel("Yeni Şifre (Boş bırakırsan değişmez):"));
        txtSifre = new JPasswordField();
        panel.add(txtSifre);

        JButton btnGuncelle = new JButton("Bilgilerimi Güncelle");
        btnGuncelle.addActionListener(e -> profilGuncelle());
        panel.add(btnGuncelle);

        return panel;
    }

    private void randevulariListele(boolean filtreli) {
        tableModel.setRowCount(0);
        List<Appointment> list;

        if (filtreli) {
            LocalDate bas = dpBaslangic.getDate();
            LocalDate bit = dpBitis.getDate();
            if(bas == null || bit == null) { JOptionPane.showMessageDialog(this, "Tarih seçin!"); return; }
            list = appointmentService.getRandevularByTarihAraligi(loggedInDoctor.getId(), bas, bit, true);
        } else {
            list = appointmentService.getRandevularByDoktor(loggedInDoctor.getId());
        }

        for (Appointment app : list) {
            tableModel.addRow(new Object[]{app.getId(), app.getHastaAdi(), app.getTarih().toLocalDate(), app.getTarih().toLocalTime(), app.getDurum(), app.getNotlar()});
        }
    }

    private void durumDegistir(String yeniDurum) {
        int row = tableRandevular.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if(appointmentService.randevuDurumGuncelle(id, yeniDurum)) {
            randevulariListele(false);
        }
    }

    private void notGirisiYap() {
        int row = tableRandevular.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String yeniNot = JOptionPane.showInputDialog(this, "Not:");
        if (yeniNot != null) appointmentService.notEkle(id, yeniNot);
    }

    private void profilGuncelle() {
        String yeniSifre = new String(txtSifre.getPassword());
        if(authService.profilGuncelle(loggedInDoctor.getId(), txtAd.getText(), txtSoyad.getText(), txtTel.getText(), txtEmail.getText(), yeniSifre)) {
            JOptionPane.showMessageDialog(this, "Güncellendi.");
        }
    }
}