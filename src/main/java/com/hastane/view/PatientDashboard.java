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
    private JTable tableRandevular; // Aktif randevular tablosu
    private DefaultTableModel tableModel; // Aktif model

    // YENİ: Geçmiş Randevular Tablosu ve Modeli
    private JTable tableGecmis;
    private DefaultTableModel modelGecmis;

    // Filtreleme
    private DatePicker dpBaslangic;
    private DatePicker dpBitis;
    private JTextField txtSearch;

    // Randevu Alma Kısmı
    private JComboBox<String> cmbBrans;
    private JComboBox<Doctor> cmbDoktorlar;
    private JComboBox<String> cmbSaat;
    private DatePicker datePicker;

    // Profil Kısmı
    private JTextField txtAd;
    private JTextField txtSoyad;
    private JTextField txtTel;
    private JTextField txtEmail;
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

        // 1. Sekme: Aktif Randevular (İsim güncellendi)
        tabbedPane.addTab("Aktif Randevularım", createRandevularPanel());

        // 2. Sekme: GEÇMİŞ RANDEVULAR (YENİ EKLENDİ)
        tabbedPane.addTab("Geçmiş Randevular", createGecmisRandevularPanel());

        // 3. Sekme: Yeni Randevu Al
        tabbedPane.addTab("Yeni Randevu Al", createRandevuAlPanel());

        // 4. Sekme: Profil
        tabbedPane.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // ==========================================
    // 1. SEKME: AKTİF RANDEVULARIM LİSTESİ
    // ==========================================
    private JPanel createRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // --- Arama ve Filtreleme Barı (Doktor Paneliyle Aynı Yapıya Getirildi) ---
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.setBorder(BorderFactory.createTitledBorder("Takvim ve Arama"));

        // Günlük Butonu
        JButton btnBugun = new JButton("GÜNLÜK");
        btnBugun.addActionListener(e -> {
            dpBaslangic.setDateToToday();
            dpBitis.setDateToToday();
            loadAktifRandevular();
        });
        filterBar.add(btnBugun);

        // Haftalık Butonu
        JButton btnHafta = new JButton("HAFTALIK");
        btnHafta.addActionListener(e -> {
            dpBaslangic.setDate(LocalDate.now());
            dpBitis.setDate(LocalDate.now().plusDays(7));
            loadAktifRandevular();
        });
        filterBar.add(btnHafta);

        filterBar.add(new JLabel(" | Tarih:"));
        dpBaslangic = new DatePicker();
        // Varsayılan olarak bugünden başlatıyoruz ki eski randevular aktifte görünmesin
        dpBaslangic.setDate(LocalDate.now());
        filterBar.add(dpBaslangic);

        dpBitis = new DatePicker();
        dpBitis.setDate(LocalDate.now().plusMonths(6));
        filterBar.add(dpBitis);

        filterBar.add(new JLabel("  Ara (Doktor/Branş):"));
        txtSearch = new JTextField(12);
        filterBar.add(txtSearch);

        JButton btnAra = new JButton("Uygula");
        btnAra.addActionListener(e -> loadAktifRandevular());
        filterBar.add(btnAra);

        // Tümünü Göster Butonu
        JButton btnTumu = new JButton("Tümünü Göster");
        btnTumu.addActionListener(e -> {
            dpBaslangic.setDate(null);
            dpBitis.setDate(null);
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
        styleTable(tableRandevular); // Stil uygulandı

        loadAktifRandevular(); // İlk yükleme
        panel.add(new JScrollPane(tableRandevular), BorderLayout.CENTER);

        // --- Alt Butonlar ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnGuncelle = new JButton("Randevu Tarihini Değiştir");
        btnGuncelle.setBackground(new Color(255, 193, 7)); // Sarı
        btnGuncelle.setForeground(Color.BLACK);
        btnGuncelle.addActionListener(e -> randevuGuncelleDialog());

        JButton btnIptal = new JButton("İptal Et");
        btnIptal.setBackground(new Color(220, 53, 69)); // Kırmızı
        btnIptal.setForeground(Color.WHITE);
        btnIptal.addActionListener(e -> durumDegistir("İPTAL EDİLDİ"));

        // Yenile Butonu
        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> { loadAktifRandevular(); loadGecmisRandevular(); });
        btnPanel.add(btnYenile);

        btnPanel.add(btnGuncelle);
        btnPanel.add(btnIptal);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ... Diğer metodlar ...

    // ==========================================
    // 2. SEKME: GEÇMİŞ RANDEVULAR (GÜNCELLENDİ: SİLME BUTONU EKLENDİ)
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

        // --- BUTON PANELİ ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.addActionListener(e -> loadGecmisRandevular());

        // YENİ BUTON: SİL
        JButton btnSil = new JButton("Seçileni Sil (Temizle)");
        btnSil.setBackground(new Color(220, 53, 69)); // Kırmızı ton
        btnSil.setForeground(Color.WHITE);
        btnSil.addActionListener(e -> randevuSilIslemi());

        btnPanel.add(btnYenile);
        btnPanel.add(btnSil); // Panele ekledik

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // YENİ YARDIMCI METOT: SİLME MANTIĞI
    private void randevuSilIslemi() {
        int selectedRow = tableGecmis.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen listeden silinecek bir kayıt seçin.");
            return;
        }

        int id = (int) modelGecmis.getValueAt(selectedRow, 0); // ID'yi al

        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this,
                "Bu randevu kaydı kalıcı olarak silinecek.\nEmin misiniz?",
                "Kayıt Silme",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (secim == JOptionPane.YES_OPTION) {
            if (appointmentService.randevuSil(id)) {
                JOptionPane.showMessageDialog(this, "Kayıt başarıyla silindi.");
                loadGecmisRandevular(); // Tabloyu yenile
            } else {
                JOptionPane.showMessageDialog(this, "Silme işlemi başarısız oldu.");
            }
        }
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
    // 4. SEKME: PROFİL (MODERN TASARIM - DÜZELTİLDİ)
    // ==========================================
    private JPanel createProfilPanel() {
        // Ana taşıyıcı (Formu ekranın ortasında tutar)
        JPanel mainWrapper = new JPanel(new GridBagLayout());
        mainWrapper.setBackground(new Color(245, 245, 245)); // Hafif gri arka plan

        // Formun kendisi (Beyaz kart görünümü)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 40, 30, 40) // İç boşluk
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Bileşenler arası boşluk
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Başlık ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblHeader = new JLabel("Profil Bilgilerim");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 150, 243));
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(lblHeader, gbc);

        // Ayırıcı çizgi
        gbc.gridy = 1;
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(300, 1));
        formPanel.add(sep, gbc);

        // --- Form Alanları ---
        gbc.gridwidth = 1; // Tekrar normale döndür
        int y = 2;

        // DÜZELTME BURADA: txtTc değişkenine atama yapmadan doğrudan yeni nesne oluşturuyoruz.
        addFormRow(formPanel, "TC Kimlik No:", new JTextField(loggedInPatient.getTcKimlikNo()), y++, false, gbc);

        // Ad
        addFormRow(formPanel, "Ad:", txtAd = new JTextField(loggedInPatient.getAd()), y++, true, gbc);

        // Soyad
        addFormRow(formPanel, "Soyad:", txtSoyad = new JTextField(loggedInPatient.getSoyad()), y++, true, gbc);

        // Telefon
        addFormRow(formPanel, "Telefon:", txtTel = new JTextField(loggedInPatient.getTelefon()), y++, true, gbc);

        // Email
        addFormRow(formPanel, "E-Mail:", txtEmail = new JTextField(loggedInPatient.getEmail()), y++, true, gbc);

        // Şifre
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Yeni Şifre:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtSifre = new JPasswordField();
        txtSifre.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtSifre, gbc);
        y++;

        // Şifre Bilgi Notu
        gbc.gridx = 1; gbc.gridy = y++;
        JLabel lblNote = new JLabel("(Değiştirmek istemiyorsanız boş bırakın)");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(Color.GRAY);
        formPanel.add(lblNote, gbc);

        // --- Buton ---
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0); // Üstten biraz daha boşluk

        JButton btnGuncelle = new JButton("BİLGİLERİ GÜNCELLE");
        btnGuncelle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuncelle.setBackground(new Color(33, 150, 243));
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.setPreferredSize(new Dimension(250, 40));
        btnGuncelle.setFocusPainted(false);
        btnGuncelle.addActionListener(e -> profilGuncelle());

        formPanel.add(btnGuncelle, gbc);

        mainWrapper.add(formPanel);
        return mainWrapper;
    }

    // Eğer helper metodu eklemediysen bunu da sınıfın en altına eklemelisin:
    private void addFormRow(JPanel panel, String labelText, JTextField field, int y, boolean editable, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST; // Etiket sağa yaslı
        panel.add(createLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // Kutu sola yaslı
        field.setPreferredSize(new Dimension(250, 35));
        field.setEditable(editable);
        if (!editable) field.setBackground(new Color(240, 240, 240));
        panel.add(field, gbc);
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

    // Tablo Stili (Yardımcı Metot)
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0); // ID Gizle
        try { table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer()); } catch (Exception e) {}
    }

    // AKTİF LİSTEYİ DOLDUR
    private void loadAktifRandevular() {
        tableModel.setRowCount(0);

        // DatePicker'dan değerleri al
        LocalDate bas = (dpBaslangic != null) ? dpBaslangic.getDate() : null;
        LocalDate bit = (dpBitis != null) ? dpBitis.getDate() : null;

        // Doktor Dashboard'undaki gibi "aramaVeFiltrele" servisini kullanıyoruz.
        // false parametresi "isDoctor = false" anlamına geliyor.
        List<Appointment> list = appointmentService.aramaVeFiltrele(
                loggedInPatient.getId(),
                bas,
                bit,
                txtSearch.getText(),
                false
        );

        for (Appointment a : list) {
            // Sadece aktif olanları tabloda göster (İptal ve Tamamlananları filtrele)
            if (!a.getDurum().equals("İPTAL EDİLDİ") &&
                    !a.getDurum().equals("GELMEDİ") &&
                    !a.getDurum().equals("TAMAMLANDI")) {

                tableModel.addRow(new Object[]{
                        a.getId(),
                        a.getDoktorAdi(),
                        a.getDoktorBrans(),
                        a.getTarih().toLocalDate(),
                        a.getTarih().toLocalTime(),
                        a.getDurum(),
                        a.getNotlar()
                });
            }
        }
    }

    // GEÇMİŞ LİSTEYİ DOLDUR
    private void loadGecmisRandevular() {
        modelGecmis.setRowCount(0);
        for (Appointment a : appointmentService.getGecmisRandevular(loggedInPatient.getId())) {
            modelGecmis.addRow(new Object[]{
                    a.getId(), a.getDoktorAdi(), a.getDoktorBrans(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()
            });
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

        // Doktorun çalışma saatlerini çek
        String gun = tarih.getDayOfWeek().toString();
        String[] saatler = doctorService.getCalismaSaatleri(doc.getId(), gun);

        if (saatler == null) {
            saatler = new String[]{"09:00", "17:00"}; // Varsayılan
        }

        LocalTime start = LocalTime.parse(saatler[0]);
        LocalTime end = LocalTime.parse(saatler[1]);

        // Dolu saatleri çek
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
                    loadAktifRandevular(); // Tabloyu yenile
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
                saatleriGuncelle(); // Saat boşa çıktı, combo'yu yenile
            }
        } else {
            JOptionPane.showMessageDialog(this, "İptal edilecek randevuyu seçiniz.");
        }
    }
}