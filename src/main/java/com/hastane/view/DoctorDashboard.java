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
        setTitle("Doktor Paneli - Dr. " + loggedInDoctor.getAd() + " " + loggedInDoctor.getSoyad());
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 150, 136));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitle = new JLabel("Dr. " + loggedInDoctor.getAd() + " (" + loggedInDoctor.getBrans() + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTitle.setForeground(Color.WHITE);
        JButton btnCikis = new JButton("Çıkış Yap");
        btnCikis.addActionListener(e -> { dispose(); new LoginScreen(); });
        header.add(lblTitle, BorderLayout.WEST); header.add(btnCikis, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBorder(new EmptyBorder(10,10,10,10));
        tabs.addTab("Randevu Listesi", createListPanel());
        tabs.addTab("Çalışma Saatlerim", createHoursPanel());
        tabs.addTab("Profil Ayarları", createProfilPanel());

        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.setBorder(BorderFactory.createTitledBorder("Takvim ve Arama"));

        // 1. Günlük / Haftalık Butonları (PDF İsteği)
        JButton btnBugun = new JButton("GÜNLÜK");
        btnBugun.addActionListener(e -> {
            dpBaslangic.setDateToToday();
            dpBitis.setDateToToday();
            randevulariListele();
        });
        filterBar.add(btnBugun);

        JButton btnHaftalik = new JButton("HAFTALIK");
        btnHaftalik.addActionListener(e -> {
            dpBaslangic.setDate(LocalDate.now());
            dpBitis.setDate(LocalDate.now().plusDays(7));
            randevulariListele();
        });
        filterBar.add(btnHaftalik);

        // 2. Tarih Seçiciler
        filterBar.add(new JLabel("| Tarih:"));
        dpBaslangic = new DatePicker(); filterBar.add(dpBaslangic); // Boş başlar
        dpBitis = new DatePicker(); filterBar.add(dpBitis);

        // 3. Arama Kutusu
        filterBar.add(new JLabel("Ara (Hasta/TC):"));
        txtSearch = new JTextField(12); filterBar.add(txtSearch);

        JButton btnAra = new JButton("Uygula");
        btnAra.addActionListener(e -> randevulariListele());
        filterBar.add(btnAra);

        // 4. İptal Edilenleri Göster (PDF İsteği)
        JButton btnIptaller = new JButton("İptal Edilenler");
        btnIptaller.setForeground(Color.RED);
        btnIptaller.addActionListener(e -> {
            dpBaslangic.setDate(null);
            dpBitis.setDate(null);
            txtSearch.setText("İPTAL");
            randevulariListele();
        });
        filterBar.add(btnIptaller);

        // 5. Tümünü Göster
        JButton btnTumu = new JButton("Tümünü Göster");
        btnTumu.addActionListener(e -> tumunuGoster());
        filterBar.add(btnTumu);

        panel.add(filterBar, BorderLayout.NORTH);

        // Tablo
        String[] cols = {"ID", "Hasta Adı", "Tarih", "Saat", "Durum", "Notlar"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableRandevular = new JTable(tableModel);
        tableRandevular.setRowHeight(35);
        try { tableRandevular.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer()); } catch (Exception e) {}
        tableRandevular.getColumnModel().getColumn(0).setMinWidth(0); tableRandevular.getColumnModel().getColumn(0).setMaxWidth(0);

        randevulariListele(); // İlk açılışta listele
        panel.add(new JScrollPane(tableRandevular), BorderLayout.CENTER);

        // Butonlar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGelmedi = new JButton("GELMEDİ"); btnGelmedi.setBackground(Color.GRAY); btnGelmedi.setForeground(Color.WHITE);
        btnGelmedi.addActionListener(e -> durumDegistir("GELMEDİ"));

        JButton btnTamamla = new JButton("TAMAMLA"); btnTamamla.setBackground(new Color(40, 167, 69)); btnTamamla.setForeground(Color.WHITE);
        btnTamamla.addActionListener(e -> durumDegistir("TAMAMLANDI"));

        JButton btnNot = new JButton("Not Gir"); btnNot.setBackground(new Color(23, 162, 184)); btnNot.setForeground(Color.WHITE);
        btnNot.addActionListener(e -> notGirisiYap());

        btnPanel.add(btnNot); btnPanel.add(btnGelmedi); btnPanel.add(btnTamamla);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHoursPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] gunlerTr = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"};
        String[] gunlerEng = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        JComboBox<String> cmbGun = new JComboBox<>(gunlerTr);
        JComboBox<String> cmbBas = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "12:00", "13:00"});
        JComboBox<String> cmbBit = new JComboBox<>(new String[]{"13:00", "14:00", "15:00", "16:00", "17:00"});
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Gün:"), gbc); gbc.gridx=1; panel.add(cmbGun, gbc);
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Başlangıç:"), gbc); gbc.gridx=1; panel.add(cmbBas, gbc);
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Bitiş:"), gbc); gbc.gridx=1; panel.add(cmbBit, gbc);
        JButton btn = new JButton("Kaydet"); btn.setBackground(new Color(0, 150, 136)); btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> {
            if(doctorService.calismaSaatiEkle(loggedInDoctor.getId(), gunlerEng[cmbGun.getSelectedIndex()], (String)cmbBas.getSelectedItem(), (String)cmbBit.getSelectedItem()))
                JOptionPane.showMessageDialog(this, "Kaydedildi!");
        });
        gbc.gridx=1; gbc.gridy=3; panel.add(btn, gbc);
        return panel;
    }

    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(new EmptyBorder(50, 150, 50, 150));
        panel.add(new JLabel("Ad:")); txtAd = new JTextField(loggedInDoctor.getAd()); panel.add(txtAd);
        panel.add(new JLabel("Soyad:")); txtSoyad = new JTextField(loggedInDoctor.getSoyad()); panel.add(txtSoyad);
        panel.add(new JLabel("Tel:")); txtTel = new JTextField(loggedInDoctor.getTelefon()); panel.add(txtTel);
        panel.add(new JLabel("Email:")); txtEmail = new JTextField(loggedInDoctor.getEmail()); panel.add(txtEmail);
        panel.add(new JLabel("Yeni Şifre:")); txtSifre = new JPasswordField(); panel.add(txtSifre);
        JButton btn = new JButton("Güncelle"); btn.addActionListener(e -> profilGuncelle());
        panel.add(btn);
        return panel;
    }

    private void randevulariListele() {
        tableModel.setRowCount(0);
        // Null kontrolü ile filtreleme
        LocalDate bas = (dpBaslangic != null) ? dpBaslangic.getDate() : null;
        LocalDate bit = (dpBitis != null) ? dpBitis.getDate() : null;

        List<Appointment> list = appointmentService.aramaVeFiltrele(
                loggedInDoctor.getId(),
                bas,
                bit,
                txtSearch.getText(),
                true
        );
        for (Appointment a : list) tableModel.addRow(new Object[]{a.getId(), a.getHastaAdi(), a.getTarih().toLocalDate(), a.getTarih().toLocalTime(), a.getDurum(), a.getNotlar()});
        tableModel.fireTableDataChanged();
    }

    private void tumunuGoster() {
        txtSearch.setText("");
        dpBaslangic.setDate(null);
        dpBitis.setDate(null);
        randevulariListele();
    }

    private void durumDegistir(String s) {
        int r = tableRandevular.getSelectedRow();
        if(r!=-1 && appointmentService.randevuDurumGuncelle((int)tableModel.getValueAt(r,0), s)) randevulariListele();
    }

    private void notGirisiYap() {
        int r = tableRandevular.getSelectedRow();
        if(r!=-1) {
            String n = JOptionPane.showInputDialog(this, "Not:", tableModel.getValueAt(r, 5));
            if(n!=null && appointmentService.notEkle((int)tableModel.getValueAt(r,0), n)) randevulariListele();
        }
    }

    private void profilGuncelle() {
        if(authService.profilGuncelle(loggedInDoctor.getId(), txtAd.getText(), txtSoyad.getText(), txtTel.getText(), txtEmail.getText(), new String(txtSifre.getPassword())))
            JOptionPane.showMessageDialog(this, "Güncellendi.");
    }
}