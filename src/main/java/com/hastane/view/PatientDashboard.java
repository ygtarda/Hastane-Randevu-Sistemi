package com.hastane.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import com.hastane.model.Appointment;
import com.hastane.model.Doctor;
import com.hastane.model.Patient;
import com.hastane.service.AppointmentService;
import com.hastane.service.AuthService;
import com.hastane.service.DoctorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDashboard extends JFrame {
    private Patient loggedInPatient;
    private AppointmentService appointmentService;
    private DoctorService doctorService;
    private AuthService authService;

    // UI Bileşenleri
    private JTable tableRandevular; // Aktif randevular
    private DefaultTableModel tableModel;

    private JTable tableGecmis; // Geçmiş randevular
    private DefaultTableModel modelGecmis;

    // Filtreleme
    private DatePicker dpBaslangic, dpBitis;
    private JTextField txtSearch;

    // Randevu Alma
    private JComboBox<String> cmbBrans;
    private JComboBox<Doctor> cmbDoktorlar;
    private JComboBox<String> cmbSaat;
    private DatePicker datePicker;

    // Profil
    private JTextField txtAd, txtSoyad, txtTel, txtEmail;
    private JPasswordField txtSifre;

    public PatientDashboard(Patient patient) {
        this.loggedInPatient = patient;
        this.appointmentService = new AppointmentService();
        this.doctorService = new DoctorService();
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Hasta Portalı - " + loggedInPatient.getAd() + " " + loggedInPatient.getSoyad());
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("HASTA PANELİ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);

        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.addActionListener(e -> {
            this.dispose();
            new LoginScreen();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnCikis, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- SEKMELER ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("Randevularım (Aktif)", createRandevularPanel());
        tabbedPane.addTab("Geçmiş Randevular", createGecmisRandevularPanel());
        tabbedPane.addTab("Yeni Randevu Al", createRandevuAlPanel());
        tabbedPane.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // ==========================================
    // 1. SEKME: AKTİF RANDEVULAR (GÜNLÜK/HAFTALIK EKLENDİ)
    // ==========================================
    private JPanel createRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // --- Arama ve Filtreleme Barı ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.setBorder(BorderFactory.createTitledBorder("Takvim ve Arama"));

        // YENİ: Günlük ve Haftalık Butonları
        JButton btnBugun = new JButton("GÜNLÜK");
        btnBugun.addActionListener(e -> {
            dpBaslangic.setDateToToday();
            dpBitis.setDateToToday();
            loadAktifRandevular();
        });
        filterBar.add(btnBugun);

        JButton btnHaftalik = new JButton("HAFTALIK");
        btnHaftalik.addActionListener(e -> {
            dpBaslangic.setDate(LocalDate.now());
            dpBitis.setDate(LocalDate.now().plusDays(7));
            loadAktifRandevular();
        });
        filterBar.add(btnHaftalik);

        filterBar.add(new JLabel("| Tarih:"));
        dpBaslangic = new DatePicker();
        dpBaslangic.setDate(LocalDate.now()); // Varsayılan bugün
        filterBar.add(dpBaslangic);

        filterBar.add(new JLabel("-"));
        dpBitis = new DatePicker();
        dpBitis.setDate(LocalDate.now().plusMonths(6));
        filterBar.add(dpBitis);

        filterBar.add(new JLabel(" Ara:"));
        txtSearch = new JTextField(12);
        filterBar.add(txtSearch);

        JButton btnAra = new JButton("Uygula");
        btnAra.addActionListener(e -> loadAktifRandevular());
        filterBar.add(btnAra);

        JButton btnTumu = new JButton("Tümü");
        btnTumu.addActionListener(e -> {
            dpBaslangic.setDate(LocalDate.now()); // Aktif olduğu için bugünden başlasın
            dpBitis.setDate(null); // Sonsuza kadar
            txtSearch.setText("");
            loadAktifRandevular();
        });
        filterBar.add(btnTumu);

        panel.add(filterBar, BorderLayout.NORTH);

        // --- Tablo ---
        String[] colNames = {"ID", "Doktor", "Branş", "Tarih", "Saat", "Durum", "Doktor Notu"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableRandevular = new JTable(tableModel);
        styleTable(tableRandevular);

        loadAktifRandevular(); // İlk yükleme
        panel.add(new JScrollPane(tableRandevular), BorderLayout.CENTER);

        // --- Alt Butonlar ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnGuncelle = new JButton("Tarihi Değiştir");
        btnGuncelle.setBackground(new Color(255, 193, 7)); // Sarı
        btnGuncelle.setForeground(Color.BLACK);
        btnGuncelle.addActionListener(e -> randevuGuncelleDialog());

        JButton btnIptal = new JButton("İptal Et");
        btnIptal.setBackground(new Color(220, 53, 69)); // Kırmızı
        btnIptal.setForeground(Color.WHITE);
        btnIptal.addActionListener(e -> durumDegistir("İPTAL EDİLDİ"));

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> { loadAktifRandevular(); loadGecmisRandevular(); });
        btnPanel.add(btnYenile);

        btnPanel.add(btnGuncelle);
        btnPanel.add(btnIptal);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // 2. SEKME: GEÇMİŞ RANDEVULAR
    // ==========================================
    private JPanel createGecmisRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] colNames = {"ID", "Doktor", "Branş", "Tarih", "Saat", "Durum", "Sonuç/Not"};
        modelGecmis = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableGecmis = new JTable(modelGecmis);
        styleTable(tableGecmis);

        loadGecmisRandevular(); // Verileri yükle
        panel.add(new JScrollPane(tableGecmis), BorderLayout.CENTER);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> loadGecmisRandevular());
        panel.add(btnYenile, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // 3. SEKME: YENİ RANDEVU ALMA
    // ==========================================
    private JPanel createRandevuAlPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Branş
        gbc.gridx = 0; gbc.gridy = 0;
        container.add(createLabel("1. Poliklinik (Branş):"), gbc);

        gbc.gridx = 1;
        cmbBrans = new JComboBox<>();
        cmbBrans.setPreferredSize(new Dimension(250, 35));
        for(String b : doctorService.getTumBranslar()) {
            cmbBrans.addItem(b);
        }
        container.add(cmbBrans, gbc);

        // 2. Doktor
        gbc.gridx = 0; gbc.gridy = 1;
        container.add(createLabel("2. Doktor Seçiniz:"), gbc);

        gbc.gridx = 1;
        cmbDoktorlar = new JComboBox<>();
        cmbDoktorlar.setPreferredSize(new Dimension(250, 35));
        container.add(cmbDoktorlar, gbc);

        // 3. Tarih (DatePicker)
        gbc.gridx = 0; gbc.gridy = 2;
        container.add(createLabel("3. Tarih Seçiniz:"), gbc);

        gbc.gridx = 1;
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setAllowKeyboardEditing(false);
        dateSettings.setFormatForDatesCommonEra("dd MMMM yyyy");

        datePicker = new DatePicker(dateSettings);
        datePicker.setDateToToday();
        datePicker.setPreferredSize(new Dimension(250, 35));

        datePicker.addDateChangeListener(new DateChangeListener() {
            @Override
            public void dateChanged(DateChangeEvent dateChangeEvent) {
                saatleriGuncelle();
            }
        });
        container.add(datePicker, gbc);

        // 4. Saat (ComboBox)
        gbc.gridx = 0; gbc.gridy = 3;
        container.add(createLabel("4. Müsait Saatler:"), gbc);

        gbc.gridx = 1;
        cmbSaat = new JComboBox<>();
        cmbSaat.setPreferredSize(new Dimension(250, 35));
        container.add(cmbSaat, gbc);

        // 5. Kaydet Butonu
        gbc.gridx = 1; gbc.gridy = 4;
        JButton btnKaydet = new JButton("RANDEVUYU ONAYLA");
        btnKaydet.setBackground(new Color(40, 167, 69));
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnKaydet.setPreferredSize(new Dimension(250, 45));
        btnKaydet.addActionListener(e -> randevuOlustur());
        container.add(btnKaydet, gbc);

        // Listener'lar
        cmbBrans.addActionListener(e -> {
            cmbDoktorlar.removeAllItems();
            String secilenBrans = (String) cmbBrans.getSelectedItem();
            if (secilenBrans != null) {
                for (Doctor d : doctorService.getDoktorlarByBrans(secilenBrans)) {
                    cmbDoktorlar.addItem(d);
                }
            }
            saatleriGuncelle();
        });

        cmbDoktorlar.addActionListener(e -> saatleriGuncelle());

        // İlk açılış tetiklemesi
        if (cmbBrans.getItemCount() > 0) cmbBrans.setSelectedIndex(0);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(container);
        return wrapper;
    }

    // ==========================================
    // 4. SEKME: PROFİL
    // ==========================================
    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 20, 20));
        panel.setBorder(new EmptyBorder(30, 100, 30, 100));

        panel.add(createLabel("TC Kimlik No:"));
        JTextField txtTc = new JTextField(loggedInPatient.getTcKimlikNo());
        txtTc.setEditable(false);
        txtTc.setBackground(new Color(240, 240, 240));
        panel.add(txtTc);

        panel.add(createLabel("Ad:"));
        txtAd = new JTextField(loggedInPatient.getAd());
        panel.add(txtAd);

        panel.add(createLabel("Soyad:"));
        txtSoyad = new JTextField(loggedInPatient.getSoyad());
        panel.add(txtSoyad);

        panel.add(createLabel("Telefon:"));
        txtTel = new JTextField(loggedInPatient.getTelefon());
        panel.add(txtTel);

        panel.add(createLabel("E-Mail:"));
        txtEmail = new JTextField(loggedInPatient.getEmail());
        panel.add(txtEmail);

        panel.add(createLabel("Yeni Şifre (Boşsa değişmez):"));
        txtSifre = new JPasswordField();
        panel.add(txtSifre);

        JButton btnGuncelle = new JButton("Bilgilerimi Güncelle");
        btnGuncelle.setBackground(new Color(33, 150, 243));
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.addActionListener(e -> profilGuncelle());
        panel.add(btnGuncelle);

        return panel;
    }

    // ==========================================
    // İŞ MANTIĞI VE YARDIMCI METOTLAR
    // ==========================================

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0); // ID Gizle
        try { table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer()); } catch (Exception e) {}
    }

    // AKTİF LİSTEYİ DOLDUR (FİLTRELİ)
    private void loadAktifRandevular() {
        tableModel.setRowCount(0);

        LocalDate bas = (dpBaslangic != null) ? dpBaslangic.getDate() : LocalDate.now();
        LocalDate bit = (dpBitis != null) ? dpBitis.getDate() : null;
        String arama = txtSearch.getText();

        // Servisten tüm aralığı çek
        List<Appointment> tumList = appointmentService.aramaVeFiltrele(loggedInPatient.getId(), bas, bit, arama, false);

        // Java tarafında "Aktif" kriterine göre süz (Gelecek + İptal Edilmemiş)
        for (Appointment a : tumList) {
            boolean isFuture = a.getTarih().toLocalDate().isAfter(LocalDate.now().minusDays(1)); // Bugünü de dahil et
            boolean isNotCancelled = !a.getDurum().equals("İPTAL EDİLDİ") && !a.getDurum().equals("GELMEDİ") && !a.getDurum().equals("TAMAMLANDI");

            if (isFuture && isNotCancelled) {
                tableModel.addRow(new Object[]{
                        a.getId(), a.getDoktorAdi(), a.getDoktorBrans(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()
                });
            }
        }
    }

    // GEÇMİŞ LİSTEYİ DOLDUR
    private void loadGecmisRandevular() {
        modelGecmis.setRowCount(0);
        // Servisten tüm veriyi çek, Java'da süz
        List<Appointment> tumList = appointmentService.aramaVeFiltrele(loggedInPatient.getId(), null, null, "", false);

        for (Appointment a : tumList) {
            boolean isPast = a.getTarih().toLocalDate().isBefore(LocalDate.now());
            boolean isFinished = a.getDurum().equals("İPTAL EDİLDİ") || a.getDurum().equals("GELMEDİ") || a.getDurum().equals("TAMAMLANDI");

            if (isPast || isFinished) {
                modelGecmis.addRow(new Object[]{
                        a.getId(), a.getDoktorAdi(), a.getDoktorBrans(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()
                });
            }
        }
    }

    private void saatleriGuncelle() {
        cmbSaat.removeAllItems();
        Doctor doc = (Doctor) cmbDoktorlar.getSelectedItem();
        LocalDate tarih = datePicker.getDate();

        if (doc == null || tarih == null) return;

        if (tarih.isBefore(LocalDate.now())) {
            cmbSaat.addItem("Geçmiş tarih seçilemez");
            return;
        }

        String gun = tarih.getDayOfWeek().toString();
        String[] saatler = doctorService.getCalismaSaatleri(doc.getId(), gun);

        if (saatler == null) {
            saatler = new String[]{"09:00", "17:00"}; // Varsayılan
        }

        LocalTime start = LocalTime.parse(saatler[0]);
        LocalTime end = LocalTime.parse(saatler[1]);

        List<String> doluSaatler = appointmentService.getDoluSaatler(doc.getId(), tarih.toString());

        while (start.isBefore(end)) {
            String saatStr = start.format(DateTimeFormatter.ofPattern("HH:mm"));
            if (!doluSaatler.contains(saatStr)) {
                cmbSaat.addItem(saatStr);
            }
            start = start.plusMinutes(30);
        }

        if (cmbSaat.getItemCount() == 0) {
            cmbSaat.addItem("Dolu");
        }
    }

    private void randevuOlustur() {
        try {
            Doctor doc = (Doctor) cmbDoktorlar.getSelectedItem();
            String saatStr = (String) cmbSaat.getSelectedItem();

            if (doc == null || saatStr == null || saatStr.equals("Dolu") ||
                    saatStr.equals("Geçmiş tarih seçilemez")) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli bir seçim yapınız.");
                return;
            }

            LocalDateTime ldt = LocalDateTime.of(datePicker.getDate(), LocalTime.parse(saatStr));

            if (appointmentService.randevuAl(doc.getId(), loggedInPatient.getId(), ldt)) {
                JOptionPane.showMessageDialog(this, "✅ Randevu Başarıyla Alındı!");
                loadAktifRandevular(); // Aktif listeyi yenile
                saatleriGuncelle();
            } else {
                JOptionPane.showMessageDialog(this, "Hata: Seçilen saat maalesef dolu.");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void randevuGuncelleDialog() {
        int row = tableRandevular.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen 'Aktif Randevular' listesinden seçim yapın.");
            return;
        }

        int randevuId = (int) tableModel.getValueAt(row, 0);
        Appointment mevcut = appointmentService.getAppointmentById(randevuId);
        if (mevcut == null) return;
        int doktorId = mevcut.getDoktorId();

        JDialog dialog = new JDialog(this, "Randevu Tarihini Değiştir", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        DatePicker dp = new DatePicker();
        dp.setDateToToday();

        JComboBox<String> cbSaat = new JComboBox<>();

        Runnable saatDoldur = () -> {
            cbSaat.removeAllItems();
            LocalDate t = dp.getDate();
            if (t == null) return;

            String gun = t.getDayOfWeek().toString();
            String[] saatler = doctorService.getCalismaSaatleri(doktorId, gun);
            if (saatler == null) saatler = new String[]{"09:00", "17:00"};

            LocalTime s = LocalTime.parse(saatler[0]);
            LocalTime e = LocalTime.parse(saatler[1]);
            List<String> dolu = appointmentService.getDoluSaatler(doktorId, t.toString());

            while (s.isBefore(e)) {
                String str = s.format(DateTimeFormatter.ofPattern("HH:mm"));
                if (!dolu.contains(str)) cbSaat.addItem(str);
                s = s.plusMinutes(30);
            }
        };

        dp.addDateChangeListener(e -> saatDoldur.run());
        saatDoldur.run();

        gbc.gridx=0; gbc.gridy=0; dialog.add(new JLabel("Yeni Tarih:"), gbc);
        gbc.gridx=1; dialog.add(dp, gbc);

        gbc.gridx=0; gbc.gridy=1; dialog.add(new JLabel("Uygun Saatler:"), gbc);
        gbc.gridx=1; dialog.add(cbSaat, gbc);

        JButton btnKaydet = new JButton("GÜNCELLE");
        btnKaydet.setBackground(new Color(255, 193, 7));
        btnKaydet.addActionListener(ev -> {
            String s = (String) cbSaat.getSelectedItem();
            if (s != null) {
                LocalDateTime yeniTarih = LocalDateTime.of(dp.getDate(), LocalTime.parse(s));
                if(appointmentService.randevuGuncelle(randevuId, yeniTarih)) {
                    JOptionPane.showMessageDialog(dialog, "Randevu güncellendi!");
                    dialog.dispose();
                    loadAktifRandevular();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Hata oluştu.");
                }
            }
        });

        gbc.gridx=1; gbc.gridy=2; dialog.add(btnKaydet, gbc);
        dialog.setVisible(true);
    }

    private void profilGuncelle() {
        if(authService.profilGuncelle(loggedInPatient.getId(), txtAd.getText(), txtSoyad.getText(), txtTel.getText(), txtEmail.getText(), new String(txtSifre.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Bilgileriniz güncellendi.");
            loggedInPatient.setAd(txtAd.getText());
            loggedInPatient.setSoyad(txtSoyad.getText());
        }
    }

    private void durumDegistir(String s) {
        int r = tableRandevular.getSelectedRow();
        if(r!=-1) {
            int randevuId = (int) tableModel.getValueAt(r, 0);
            if (appointmentService.randevuDurumGuncelle(randevuId, s)) {
                JOptionPane.showMessageDialog(this, "Randevu iptal edildi ve 'Geçmiş' sekmesine taşındı.");
                loadAktifRandevular(); // Aktiften sil
                loadGecmisRandevular(); // Geçmişe ekle
                saatleriGuncelle(); // Combo yenilensin
            }
        } else {
            JOptionPane.showMessageDialog(this, "İptal edilecek randevuyu seçiniz.");
        }
    }
}