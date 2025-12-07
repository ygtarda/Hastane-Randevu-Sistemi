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
    private JTable tableRandevular;
    private DefaultTableModel tableModel;

    // Randevu Alma
    private JComboBox<String> cmbBrans;
    private JComboBox<Doctor> cmbDoktorlar;
    private JComboBox<String> cmbSaat;

    // Modern Takvim Bileşeni
    private DatePicker datePicker;

    // Profil (Şifre Eklendi)
    private JTextField txtAd, txtSoyad, txtTel, txtEmail;
    private JPasswordField txtSifre; // YENİ

    public PatientDashboard(Patient patient) {
        this.loggedInPatient = patient;
        this.appointmentService = new AppointmentService();
        this.doctorService = new DoctorService();
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Hasta Portalı");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ANA PANEL
        JPanel mainPanel = new JPanel(new BorderLayout());

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Hoşgeldiniz, Sayın " + loggedInPatient.getAd() + " " + loggedInPatient.getSoyad());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.setBackground(Color.WHITE);
        btnCikis.setForeground(new Color(33, 150, 243));
        btnCikis.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCikis.setFocusPainted(false);
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

        tabbedPane.addTab("Randevularım", createRandevularPanel());
        tabbedPane.addTab("Yeni Randevu Al", createRandevuAlPanel());
        tabbedPane.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    // --- SEKME 1: RANDEVULAR ---
    private JPanel createRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] colNames = {"GizliID", "Doktor", "Branş", "Tarih", "Saat", "Durum", "Doktor Notu"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableRandevular = new JTable(tableModel);
        tableRandevular.setRowHeight(35);
        tableRandevular.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableRandevular.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // ID Gizle
        tableRandevular.getColumnModel().getColumn(0).setMinWidth(0);
        tableRandevular.getColumnModel().getColumn(0).setMaxWidth(0);
        tableRandevular.getColumnModel().getColumn(0).setWidth(0);

        randevulariListele();

        panel.add(new JScrollPane(tableRandevular), BorderLayout.CENTER);

        JButton btnYenile = new JButton("Listeyi Yenile");
        btnYenile.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnYenile.addActionListener(e -> randevulariListele());
        panel.add(btnYenile, BorderLayout.SOUTH);

        return panel;
    }

    // --- SEKME 2: RANDEVU AL ---
    private JPanel createRandevuAlPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. BRANŞ SEÇİMİ
        gbc.gridx = 0; gbc.gridy = 0;
        container.add(createLabel("1. Poliklinik (Branş):"), gbc);

        gbc.gridx = 1;
        cmbBrans = new JComboBox<>();
        cmbBrans.setPreferredSize(new Dimension(250, 35));
        List<String> branslar = doctorService.getTumBranslar();
        for(String b : branslar) cmbBrans.addItem(b);
        container.add(cmbBrans, gbc);

        // 2. DOKTOR SEÇİMİ
        gbc.gridx = 0; gbc.gridy = 1;
        container.add(createLabel("2. Doktor Seçiniz:"), gbc);

        gbc.gridx = 1;
        cmbDoktorlar = new JComboBox<>();
        cmbDoktorlar.setPreferredSize(new Dimension(250, 35));
        container.add(cmbDoktorlar, gbc);

        // 3. TARİH SEÇİMİ
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

        // 4. SAAT SEÇİMİ
        gbc.gridx = 0; gbc.gridy = 3;
        container.add(createLabel("4. Müsait Saatler:"), gbc);

        gbc.gridx = 1;
        cmbSaat = new JComboBox<>();
        cmbSaat.setPreferredSize(new Dimension(250, 35));
        container.add(cmbSaat, gbc);

        // 5. ONAY BUTONU
        gbc.gridx = 1; gbc.gridy = 4;
        JButton btnKaydet = new JButton("RANDEVUYU ONAYLA");
        btnKaydet.setBackground(new Color(40, 167, 69));
        btnKaydet.setForeground(Color.WHITE);
        btnKaydet.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnKaydet.setPreferredSize(new Dimension(250, 45));
        btnKaydet.addActionListener(e -> randevuOlustur());
        container.add(btnKaydet, gbc);

        cmbBrans.addActionListener(e -> {
            cmbDoktorlar.removeAllItems();
            String secilenBrans = (String) cmbBrans.getSelectedItem();
            if (secilenBrans != null) {
                List<Doctor> docs = doctorService.getDoktorlarByBrans(secilenBrans);
                for (Doctor d : docs) cmbDoktorlar.addItem(d);
            }
            saatleriGuncelle();
        });

        cmbDoktorlar.addActionListener(e -> saatleriGuncelle());

        if (cmbBrans.getItemCount() > 0) cmbBrans.setSelectedIndex(0);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(container);
        return wrapper;
    }

    // --- SEKME 3: PROFIL (GÜNCELLENDİ: ŞİFRE ALANI EKLENDİ) ---
    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 100, 30, 100));

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 20, 20)); // Satır sayısı arttı
        formPanel.setMaximumSize(new Dimension(800, 450));

        formPanel.add(createLabel("TC Kimlik No:"));
        JTextField txtTc = new JTextField(loggedInPatient.getTcKimlikNo());
        txtTc.setEditable(false);
        txtTc.setBackground(new Color(240, 240, 240));
        formPanel.add(txtTc);

        formPanel.add(createLabel("Ad:"));
        txtAd = new JTextField(loggedInPatient.getAd());
        formPanel.add(txtAd);

        formPanel.add(createLabel("Soyad:"));
        txtSoyad = new JTextField(loggedInPatient.getSoyad());
        formPanel.add(txtSoyad);

        formPanel.add(createLabel("Telefon:"));
        txtTel = new JTextField(loggedInPatient.getTelefon());
        formPanel.add(txtTel);

        formPanel.add(createLabel("E-Mail:"));
        txtEmail = new JTextField(loggedInPatient.getEmail());
        formPanel.add(txtEmail);

        // YENİ ŞİFRE ALANI
        formPanel.add(createLabel("Yeni Şifre (Boşsa değişmez):"));
        txtSifre = new JPasswordField();
        formPanel.add(txtSifre);

        formPanel.add(new JLabel(""));
        JButton btnGuncelle = new JButton("Bilgilerimi Güncelle");
        btnGuncelle.setBackground(new Color(33, 150, 243));
        btnGuncelle.setForeground(Color.WHITE);
        btnGuncelle.addActionListener(e -> profilGuncelle());
        formPanel.add(btnGuncelle);

        panel.add(formPanel);
        return panel;
    }

    // --- YARDIMCI METOTLAR ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void saatleriGuncelle() {
        cmbSaat.removeAllItems();
        Doctor doc = (Doctor) cmbDoktorlar.getSelectedItem();
        LocalDate secilenTarih = datePicker.getDate();

        if (doc == null || secilenTarih == null) return;

        if (secilenTarih.isBefore(LocalDate.now())) {
            cmbSaat.addItem("Geçmiş tarih seçilemez");
            return;
        }

        // Doktorun çalışma saatlerine göre doluluk kontrolü
        String gun = secilenTarih.getDayOfWeek().toString();
        String[] calismaSaatleri = doctorService.getCalismaSaatleri(doc.getId(), gun);

        if (calismaSaatleri == null) {
            cmbSaat.addItem("Doktor bu gün çalışmıyor");
            return;
        }

        LocalTime baslangic = LocalTime.parse(calismaSaatleri[0]);
        LocalTime bitis = LocalTime.parse(calismaSaatleri[1]);

        List<String> doluSaatler = appointmentService.getDoluSaatler(doc.getId(), secilenTarih.toString());

        while (baslangic.isBefore(bitis)) {
            String saatStr = baslangic.format(DateTimeFormatter.ofPattern("HH:mm"));
            if (!doluSaatler.contains(saatStr)) {
                cmbSaat.addItem(saatStr);
            }
            baslangic = baslangic.plusMinutes(30);
        }

        if (cmbSaat.getItemCount() == 0) {
            cmbSaat.addItem("Dolu");
        }
    }

    private void randevuOlustur() {
        try {
            Doctor doc = (Doctor) cmbDoktorlar.getSelectedItem();
            LocalDate tarih = datePicker.getDate();
            String saatStr = (String) cmbSaat.getSelectedItem();

            if (doc == null || tarih == null || saatStr == null || saatStr.equals("Dolu") ||
                    saatStr.equals("Geçmiş tarih seçilemez") || saatStr.equals("Doktor bu gün çalışmıyor")) {
                JOptionPane.showMessageDialog(this, "Lütfen geçerli doktor, tarih ve saat seçiniz.");
                return;
            }

            LocalDateTime ldt = LocalDateTime.of(tarih, LocalTime.parse(saatStr));

            if (appointmentService.randevuAl(doc.getId(), loggedInPatient.getId(), ldt)) {
                JOptionPane.showMessageDialog(this, "✅ Randevu Başarıyla Alındı!");
                randevulariListele();
                saatleriGuncelle();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void randevulariListele() {
        tableModel.setRowCount(0);
        List<Appointment> list = appointmentService.getRandevularByHasta(loggedInPatient.getId());
        for (Appointment a : list) {
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

    // HATAYI ÇÖZEN KISIM BURASI
    private void profilGuncelle() {
        String yeniSifre = new String(txtSifre.getPassword());

        // Artık 6 parametre gönderiyoruz (ID, Ad, Soyad, Tel, Email, ŞİFRE)
        if(authService.profilGuncelle(loggedInPatient.getId(), txtAd.getText(), txtSoyad.getText(), txtTel.getText(), txtEmail.getText(), yeniSifre)) {
            JOptionPane.showMessageDialog(this, "Bilgileriniz güncellendi.");
            loggedInPatient.setAd(txtAd.getText());
            loggedInPatient.setSoyad(txtSoyad.getText());
            loggedInPatient.setTelefon(txtTel.getText());
            loggedInPatient.setEmail(txtEmail.getText());
            if (!yeniSifre.isEmpty()) loggedInPatient.setSifre(yeniSifre);
        }
    }
}