# YAZ16303 – Yazılım Mimarisi ve Tasarımı Dönem Projesi (PRJ-3)

## 🏥 Hastane Randevu Yönetim Sistemi

Bu proje, **Yazılım Mimarisi ve Tasarımı** dersi kapsamında geliştirilmiş; hastalar ve doktorlar için kapsamlı randevu yönetim süreçlerini içeren, modern tasarım desenleri ve nesne yönelimli programlama prensiplerine uygun bir masaüstü uygulamasıdır.

---

## 👥 Proje Ekibi

| Adı Soyadı | Öğrenci No |
| :--- | :--- |
| **Arda Yiğit** | 1230505022 |
| **** | 1230505028 |

---

## 📋 Proje Özellikleri

Uygulama iki temel kullanıcı rolü üzerine kurulmuştur: **Hasta** ve **Doktor**.

### 👤 Hasta Modülü
* **Randevu Alma:** Poliklinik, doktor, tarih ve saat seçerek yeni randevu oluşturma.
    * *Akıllı Kontrol:* Sadece doktorun müsait olduğu (dolu olmayan ve çalışma saati içinde kalan) saatler listelenir.
* **Randevu Yönetimi:**
    * **Aktif Randevular:** Gelecek randevuları görüntüleme, iptal etme veya tarihini değiştirme (Reschedule).
    * **Geçmiş Randevular:** Tamamlanan veya iptal edilen randevuların tarihçesini ayrı bir sekmede görüntüleme.
* **Gelişmiş Arama:** Doktor adı, branş veya randevu durumuna göre dinamik filtreleme.
* **Profil Yönetimi:** Kişisel bilgileri güncelleme ve şifre değiştirme.

### 👨‍⚕️ Doktor Modülü
* **Randevu Takvimi:**
    * **Günlük/Haftalık Görünüm:** Randevuları tek tıkla günlük veya haftalık periyotlarda filtreleme.
    * **Detaylı Liste:** Hastanın adı, randevu saati ve notları içeren liste görünümü.
* **Çalışma Saati Yönetimi:**
    * Hangi günlerde, hangi saat aralıklarında (Örn: 09:00 - 12:00) çalışılacağını belirleme.
    * "Bu gün çalışmıyorum" seçeneği ile günü kapatma ve o güne randevu alınmasını engelleme.
* **Hasta Takibi:** Randevuya gelmeyen hastaları işaretleme ("GELMEDİ") veya muayeneyi tamamlama ("TAMAMLANDI").
* **İptal Takibi:** İptal edilen ve gelmeyen hastaları ayrı bir sekmede görüntüleme.
* **Arama ve Filtreleme:** Hasta Adı veya TC Kimlik No ile hızlı arama yapabilme.

---

## 🏗️ Kullanılan Mimari ve Tasarım Desenleri

Proje, **S.O.L.I.D** prensiplerine uygun olarak geliştirilmiş ve aşağıdaki tasarım desenleri (Design Patterns) aktif olarak kullanılmıştır:

### 1. Zorunlu Desenler
* **Singleton Pattern:** `DatabaseConnection` sınıfında kullanılmıştır. [cite_start]Veritabanı bağlantısının uygulama genelinde tek bir örnek (instance) üzerinden yönetilmesini sağlar. [cite: 3]
* **Factory Pattern:** `UserFactory` sınıfında kullanılmıştır. [cite_start]Kullanıcı giriş tipine göre (`"HASTA"` veya `"DOKTOR"`) ilgili nesnenin üretimini sağlar. [cite: 5]
* **Observer Pattern:** `AppointmentService` içerisindeki bildirim yapısında kullanılmıştır. [cite_start]Randevu alındığında, güncellendiğinde veya iptal edildiğinde sisteme (ConsoleLogger) anlık bildirim gönderilir. [cite: 8]
* **State Pattern:** Randevu durumlarını yönetmek için altyapı hazırlanmıştır (`IAppointmentState`, `PendingState`, `ConfirmedState`, `CancelledState`). [cite_start]Randevunun yaşam döngüsü bu durumlar üzerinden yönetilir. [cite: 10]

### 2. Ekstra Desenler (Bonus)
* **Facade Pattern:** `HospitalFacade` sınıfında kullanılmıştır. Karmaşık alt sistemleri (Auth, Appointment, Doctor servisleri) tek bir arayüz arkasında toplayarak kullanımı basitleştirir.
* **Builder Pattern:** `AppointmentBuilder` sınıfında kullanılmıştır. Karmaşık randevu nesnelerinin adım adım ve okunaklı bir şekilde oluşturulmasını sağlar.

### 3. Kullanılan Abstract Sınıflar
* [cite_start]**`BaseEntity`**: Tüm veritabanı varlıklarının (ID, Oluşturulma Tarihi vb.) türediği temel sınıf. [cite: 13]
* [cite_start]**`User`**: `Doctor` ve `Patient` sınıflarının ortak özelliklerini (Ad, Soyad, TC, Şifre, İletişim) taşıyan soyut sınıf. [cite: 13]

---

## 🛠️ Teknolojiler ve Kütüphaneler

* **Dil:** Java (JDK 21+)
* **Arayüz (GUI):** Java Swing
* **Veritabanı:** MySQL
* **Tema Motoru:** [FlatLaf](https://www.formdev.com/flatlaf/) (Modern Dark/Light temalar için)
* **Tarih Seçici:** [LGoodDatePicker](https://github.com/LGoodDatePicker/LGoodDatePicker) (Takvim bileşeni için)
* **Build Tool:** Maven

---

## 🚀 Kurulum ve Çalıştırma

1.  **Veritabanını Oluşturun:**
    * MySQL'de `hastane_db` adında boş bir veritabanı oluşturun.
    * `src/main/java/com/hastane/common/DatabaseConnection.java` dosyasındaki kullanıcı adı ve şifreyi kendi lokal ayarlarınıza göre güncelleyin.

2.  **Tabloları ve Verileri Yükleyin:**
    * `src/main/java/com/hastane/DatabaseSetup.java` dosyasını çalıştırın.
    * *Bu işlem; gerekli tabloları oluşturacak ve sisteme test için doktor/hasta verilerini yükleyecektir.*

3.  **Uygulamayı Başlatın:**
    * `src/main/java/com/hastane/Main.java` dosyasını çalıştırarak uygulamayı başlatın.

---

## 👥 Test Kullanıcıları

**Doktor Girişi:**
* **TC:** `11`
* **Şifre:** `1234`

**Hasta Girişi:**
* **TC:** `21`
* **Şifre:** `1234`
