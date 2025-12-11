package com.hastane.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.hastane.model.Appointment;
import com.hastane.model.Doctor;
import com.hastane.service.AppointmentService;
import com.hastane.service.AuthService;
import com.hastane.service.DoctorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DoctorDashboard extends JFrame {
    private Doctor loggedInDoctor;
    private AppointmentService appointmentService;
    private AuthService authService;
    private DoctorService doctorService;

    // Tablolar
    private JTable tableAktif, tableIptal, tableSaatler;
    private DefaultTableModel modelAktif, modelIptal, modelSaatler;

    private JTextField txtSearch, txtAd, txtSoyad, txtTel, txtEmail;
    private JPasswordField txtSifre;
    private DatePicker dpBaslangic, dpBitis;

    public DoctorDashboard(Doctor doctor) {
        this.loggedInDoctor = doctor;
        this.appointmentService = new AppointmentService();
        this.authService = new AuthService();
        this.doctorService = new DoctorService();
        initUI();
    }

    private void initUI() {
        setTitle("Doktor Paneli - Dr. " + loggedInDoctor.getAd());
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 150, 136));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Dr. " + loggedInDoctor.getAd() + " " + loggedInDoctor.getSoyad() + " (" + loggedInDoctor.getBrans() + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTitle.setForeground(Color.WHITE);

        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.addActionListener(e -> { dispose(); new LoginScreen(); });
        header.add(lblTitle, BorderLayout.WEST); header.add(btnCikis, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBorder(new EmptyBorder(10,10,10,10));

        // 1. Sekme: Aktif Randevular
        tabs.addTab("Randevu Takvimi (Aktif)", createAktifPanel());

        // 2. Sekme: İptal Edilenler (YENİ)
        tabs.addTab("İptal Edilen / Gelmeyenler", createIptalPanel());

        // 3. Sekme: Çalışma Saatleri (Tablo Görünümü)
        tabs.addTab("Çalışma Saatlerim", createHoursPanel());

        // 4. Sekme: Profil
        tabs.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // --- SEKME 1: AKTİF RANDEVULAR ---
    private JPanel createAktifPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Filtre Barı
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.setBorder(BorderFactory.createTitledBorder("Takvim"));

        JButton btnBugun = new JButton("GÜNLÜK");
        btnBugun.addActionListener(e -> { dpBaslangic.setDateToToday(); dpBitis.setDateToToday(); loadAktifList(); });
        filterBar.add(btnBugun);

        JButton btnHafta = new JButton("HAFTALIK");
        btnHafta.addActionListener(e -> { dpBaslangic.setDate(LocalDate.now()); dpBitis.setDate(LocalDate.now().plusDays(7)); loadAktifList(); });
        filterBar.add(btnHafta);

        filterBar.add(new JLabel(" | Tarih:"));
        dpBaslangic = new DatePicker(); filterBar.add(dpBaslangic);
        dpBitis = new DatePicker(); filterBar.add(dpBitis);

        filterBar.add(new JLabel(" Ara:"));
        txtSearch = new JTextField(12); filterBar.add(txtSearch);

        JButton btnAra = new JButton("Uygula");
        btnAra.addActionListener(e -> loadAktifList()); filterBar.add(btnAra);

        JButton btnTumu = new JButton("Tümünü Göster");
        btnTumu.addActionListener(e -> { dpBaslangic.setDate(null); dpBitis.setDate(null); txtSearch.setText(""); loadAktifList(); });
        filterBar.add(btnTumu);

        panel.add(filterBar, BorderLayout.NORTH);

        // Tablo
        String[] cols = {"ID", "Hasta Adı", "Tarih", "Saat", "Durum", "Notlar"};
        modelAktif = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableAktif = new JTable(modelAktif);
        styleTable(tableAktif);

        loadAktifList();
        panel.add(new JScrollPane(tableAktif), BorderLayout.CENTER);

        // Butonlar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGelmedi = new JButton("GELMEDİ");
        btnGelmedi.setBackground(Color.ORANGE);
        btnGelmedi.addActionListener(e -> durumDegistir("GELMEDİ"));

        JButton btnTamamla = new JButton("TAMAMLA");
        btnTamamla.setBackground(new Color(40, 167, 69)); btnTamamla.setForeground(Color.WHITE);
        btnTamamla.addActionListener(e -> durumDegistir("TAMAMLANDI"));

        JButton btnNot = new JButton("Not Gir");
        btnNot.addActionListener(e -> notGirisiYap());

        btnPanel.add(btnNot); btnPanel.add(btnGelmedi); btnPanel.add(btnTamamla);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- SEKME 2: İPTAL EDİLENLER (YENİ SEKME) ---
    private JPanel createIptalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("İptal Edilen ve Gelmeyen Hastalar"));

        String[] cols = {"ID", "Hasta Adı", "Tarih", "Saat", "Durum", "Notlar"};
        modelIptal = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableIptal = new JTable(modelIptal);
        styleTable(tableIptal);

        loadIptalList();
        panel.add(new JScrollPane(tableIptal), BorderLayout.CENTER);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> loadIptalList());
        panel.add(btnYenile, BorderLayout.SOUTH);

        return panel;
    }

    // --- SEKME 3: ÇALIŞMA SAATLERİ (YENİ TABLO GÖRÜNÜMÜ) ---
    private JPanel createHoursPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Sol Kısım: Ayar Formu ---
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Yeni Saat Ekle / Güncelle"));

        String[] gunlerTr = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"};
        String[] gunlerEng = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        JComboBox<String> cmbGun = new JComboBox<>(gunlerTr);
        JComboBox<String> cmbBas = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00"});
        JComboBox<String> cmbBit = new JComboBox<>(new String[]{"13:00", "14:00", "15:00", "16:00", "17:00"});

        formPanel.add(new JLabel("Gün:")); formPanel.add(cmbGun);
        formPanel.add(new JLabel("Başlangıç:")); formPanel.add(cmbBas);
        formPanel.add(new JLabel("Bitiş:")); formPanel.add(cmbBit);

        JButton btnKaydet = new JButton("Kaydet");
        btnKaydet.setBackground(new Color(0, 150, 136)); btnKaydet.setForeground(Color.WHITE);
        btnKaydet.addActionListener(e -> {
            if(doctorService.calismaSaatiEkle(loggedInDoctor.getId(), gunlerEng[cmbGun.getSelectedIndex()], (String)cmbBas.getSelectedItem(), (String)cmbBit.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Çalışma saatleri güncellendi!");
                loadHoursTable(); // Tabloyu anında güncelle
            }
        });
        formPanel.add(btnKaydet);

        // --- Orta Kısım: Mevcut Saatler Tablosu ---
        String[] cols = {"Gün", "Başlangıç Saati", "Bitiş Saati", "Durum"};
        modelSaatler = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableSaatler = new JTable(modelSaatler);
        styleTable(tableSaatler);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Mevcut Çalışma Programım"));
        listPanel.add(new JScrollPane(tableSaatler), BorderLayout.CENTER);

        // Alt Bilgi Notu
        JLabel lblInfo = new JLabel("<html><center>NOT: Eğer bir gün için saat ayarlamazsanız, sistem varsayılan olarak <b>09:00 - 17:00</b> arasını kabul eder.<br>Özel bir saatiniz varsa yukarıdan ekleyiniz.</center></html>");
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setForeground(Color.GRAY);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);
        panel.add(lblInfo, BorderLayout.SOUTH);

        loadHoursTable();
        return panel;
    }

    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 20, 20));
        panel.setBorder(new EmptyBorder(50, 150, 50, 150));
        panel.add(new JLabel("Ad:")); txtAd = new JTextField(loggedInDoctor.getAd()); panel.add(txtAd);
        panel.add(new JLabel("Soyad:")); txtSoyad = new JTextField(loggedInDoctor.getSoyad()); panel.add(txtSoyad);
        panel.add(new JLabel("Tel:")); txtTel = new JTextField(loggedInDoctor.getTelefon()); panel.add(txtTel);
        panel.add(new JLabel("Email:")); txtEmail = new JTextField(loggedInDoctor.getEmail()); panel.add(txtEmail);
        panel.add(new JLabel("Yeni Şifre:")); txtSifre = new JPasswordField(); panel.add(txtSifre);
        JButton btn = new JButton("Güncelle"); btn.addActionListener(e -> profilGuncelle()); panel.add(btn);
        return panel;
    }

    // --- YARDIMCILAR VE MANTIK ---

    private void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        if(t.getColumnModel().getColumnCount() > 4) { // Randevu tabloları için
            t.getColumnModel().getColumn(0).setMinWidth(0); t.getColumnModel().getColumn(0).setMaxWidth(0);
            try { t.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer()); } catch (Exception e) {}
        }
    }

    // Aktif Listeyi Yükle
    private void loadAktifList() {
        modelAktif.setRowCount(0);
        LocalDate bas = (dpBaslangic != null) ? dpBaslangic.getDate() : null;
        LocalDate bit = (dpBitis != null) ? dpBitis.getDate() : null;

        List<Appointment> list = appointmentService.aramaVeFiltrele(loggedInDoctor.getId(), bas, bit, txtSearch.getText(), true);

        for (Appointment a : list) {
            // Sadece AKTİF olanları göster
            if (!a.getDurum().equals("İPTAL EDİLDİ") && !a.getDurum().equals("GELMEDİ")) {
                modelAktif.addRow(new Object[]{a.getId(), a.getHastaAdi(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()});
            }
        }
    }

    // İptal Listesini Yükle
    private void loadIptalList() {
        modelIptal.setRowCount(0);
        List<Appointment> list = appointmentService.getDoktorIptalRandevular(loggedInDoctor.getId());
        for (Appointment a : list) {
            modelIptal.addRow(new Object[]{a.getId(), a.getHastaAdi(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()});
        }
    }

    // Çalışma Saatleri Tablosunu Yükle
    private void loadHoursTable() {
        modelSaatler.setRowCount(0);
        List<String[]> list = doctorService.getTumCalismaSaatleriTablosu(loggedInDoctor.getId());
        for(String[] row : list) {
            modelSaatler.addRow(new Object[]{row[0], row[1], row[2], "Aktif"});
        }
    }

    private void durumDegistir(String s) {
        int r = tableAktif.getSelectedRow();
        if(r!=-1) {
            int id = (int) modelAktif.getValueAt(r, 0);
            if(appointmentService.randevuDurumGuncelle(id, s)) {
                loadAktifList(); // Aktiften düşer
                loadIptalList(); // İptal sekmesine gider
            }
        } else JOptionPane.showMessageDialog(this, "Randevu seçiniz.");
    }

    private void notGirisiYap() {
        int r = tableAktif.getSelectedRow();
        if(r!=-1) {
            String n = JOptionPane.showInputDialog(this, "Not:", modelAktif.getValueAt(r, 5));
            if(n!=null && appointmentService.notEkle((int)modelAktif.getValueAt(r,0), n)) loadAktifList();
        }
    }

    private void profilGuncelle() {
        if(authService.profilGuncelle(loggedInDoctor.getId(), txtAd.getText(), txtSoyad.getText(), txtTel.getText(), txtEmail.getText(), new String(txtSifre.getPassword())))
            JOptionPane.showMessageDialog(this, "Güncellendi.");
    }
}