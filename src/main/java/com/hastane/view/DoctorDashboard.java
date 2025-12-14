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

    // ... Diğer metodlar ...

    // --- SEKME 2: İPTAL EDİLENLER (GÜNCELLENDİ: TEMİZLEME BUTONU EKLENDİ) ---
    private JPanel createIptalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("İptal Edilen ve Gelmeyen Hastalar"));

        String[] cols = {"ID", "Hasta Adı", "Tarih", "Saat", "Durum", "Notlar"};
        modelIptal = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableIptal = new JTable(modelIptal);
        styleTable(tableIptal);

        loadIptalList();
        panel.add(new JScrollPane(tableIptal), BorderLayout.CENTER);

        // --- BUTON PANELİ ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> loadIptalList());

        // YENİ BUTON: SİL
        JButton btnSil = new JButton("Kaydı Temizle (Sil)");
        btnSil.setBackground(new Color(220, 53, 69)); // Kırmızı
        btnSil.setForeground(Color.WHITE);
        btnSil.addActionListener(e -> randevuSilIslemi());

        btnPanel.add(btnYenile);
        btnPanel.add(btnSil);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    //  DOKTOR İÇİN SİLME MANTIĞI
    private void randevuSilIslemi() {
        int selectedRow = tableIptal.getSelectedRow(); // İptal tablosundan seçim alıyoruz
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek bir kayıt seçin.");
            return;
        }

        int id = (int) modelIptal.getValueAt(selectedRow, 0);

        int secim = JOptionPane.showConfirmDialog(this,
                "Bu kayıt listeden ve veritabanından kalıcı olarak silinecek.\nOnaylıyor musunuz?",
                "Kayıt Temizleme",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (secim == JOptionPane.YES_OPTION) {
            // AppointmentService'e eklediğimiz metodu kullanıyoruz
            if (appointmentService.randevuSil(id)) {
                JOptionPane.showMessageDialog(this, "Kayıt başarıyla silindi.");
                loadIptalList(); // Listeyi yenile
            } else {
                JOptionPane.showMessageDialog(this, "İşlem başarısız oldu.");
            }
        }
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

    // ==========================================
    // 4. SEKME: PROFİL (MODERN TASARIM - GÜNCELLENDİ)
    // ==========================================
    private JPanel createProfilPanel() {
        // Ana taşıyıcı
        JPanel mainWrapper = new JPanel(new GridBagLayout());
        mainWrapper.setBackground(new Color(245, 245, 245));

        // Form Paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Başlık ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("Doktor Profil Ayarları");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(0, 150, 136)); // Doktor temasına uygun yeşil ton
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(lblHeader, gbc);

        // Ayırıcı
        gbc.gridy = 1;
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(300, 1));
        formPanel.add(sep, gbc);

        gbc.gridwidth = 1;
        int y = 2;

        // TC (Salt Okunur)
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("TC Kimlik No:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField txtTcInfo = new JTextField(loggedInDoctor.getTcKimlikNo());
        txtTcInfo.setEditable(false);
        txtTcInfo.setBackground(new Color(240, 240, 240));
        txtTcInfo.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtTcInfo, gbc);
        y++;

        // Ad
        addFormRow(formPanel, "Ad:", txtAd = new JTextField(loggedInDoctor.getAd()), y++, gbc);

        // Soyad
        addFormRow(formPanel, "Soyad:", txtSoyad = new JTextField(loggedInDoctor.getSoyad()), y++, gbc);

        // Branş (Salt Okunur - Bilgi Amaçlı)
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Branş:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField txtBrans = new JTextField(loggedInDoctor.getBrans());
        txtBrans.setEditable(false);
        txtBrans.setBackground(new Color(240, 240, 240));
        txtBrans.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtBrans, gbc);
        y++;

        // Telefon
        addFormRow(formPanel, "Telefon:", txtTel = new JTextField(loggedInDoctor.getTelefon()), y++, gbc);

        // Email
        addFormRow(formPanel, "E-Mail:", txtEmail = new JTextField(loggedInDoctor.getEmail()), y++, gbc);

        // Şifre
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Yeni Şifre:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        txtSifre = new JPasswordField();
        txtSifre.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtSifre, gbc);
        y++;

        // Not
        gbc.gridx = 1; gbc.gridy = y++;
        JLabel lblNote = new JLabel("(Değişmeyecekse boş bırakın)");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);

        // --- Buton ---
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);

        JButton btnGuncelle = new JButton("BİLGİLERİ GÜNCELLE");
        btnGuncelle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuncelle.setBackground(new Color(0, 150, 136)); // Yeşil
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.setPreferredSize(new Dimension(250, 40));
        btnGuncelle.setFocusPainted(false);
        btnGuncelle.addActionListener(e -> profilGuncelle());

        formPanel.add(btnGuncelle, gbc);

        mainWrapper.add(formPanel);
        return mainWrapper;
    }

    // Yardımcı Metot
    private void addFormRow(JPanel panel, String labelText, JTextField field, int y, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), gbc); // JLabel fontunu panel fontundan alabilir veya createLabel kullan

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        field.setPreferredSize(new Dimension(250, 35));
        panel.add(field, gbc);
    }

    // createLabel metodu DoctorDashboard sınıfında yoksa ekle:
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
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