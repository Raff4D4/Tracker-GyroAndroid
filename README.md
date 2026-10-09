# GlassMotion: Multi-Sensor Android App

Aplikasi Android modern berbasis Kotlin dan Jetpack Compose yang memanfaatkan sensor bawaan smartphone untuk menciptakan antarmuka interaktif bertema Glassmorphism. Proyek ini mendemonstrasikan integrasi sensor perangkat keras dari berbagai kategori, kepatuhan terhadap siklus hidup (*lifecycle* Android), serta pemrosesan Computer Vision untuk kontrol tanpa sentuh (*contactless*).

---

## 📱 Daftar Sensor & Kategori

Aplikasi ini menggunakan minimal 2 kategori sensor Android yang berbeda untuk memicu efek langsung pada antarmuka pengguna (UI):

| No | Sensor | Tipe Sensor Android | Kategori | Implementasi & Efek UI |
|---|---|---|---|---|
| 1 | **Ambient Light Sensor** | `Sensor.TYPE_LIGHT` | **Environment** | Membaca intensitas cahaya ruangan (lux). Nilai ini mengubah tema warna frosted glass secara dinamis (terang/gelap) serta mengaktifkan senter (*flashlight*) otomatis saat kondisi ruangan gelap (< 10 lux). |
| 2 | **Rotation Vector / Gyroscope** | `Sensor.TYPE_ROTATION_VECTOR` | **Motion** | Mengukur orientasi dan sudut kemiringan rotasi 3D perangkat (Pitch & Roll). Menggerakkan efek *3D Parallax Tilt* dan kilauan hologram (*specular sheen*) pada kartu UI secara real-time. |
| 3 | **Accelerometer** | `Sensor.TYPE_ACCELEROMETER` | **Motion** | Menghitung vektor percepatan linier dan gravitasi. Digunakan untuk menstabilkan indikator waterpas pada tab *Bubble Level* serta mendeteksi gestur *Shake-to-Refresh*. |
| 4 | **Proximity Sensor** | `Sensor.TYPE_PROXIMITY` | **Position** | Mendeteksi keberadaan objek di depan layar untuk memicu mode privasi/layar redup (*screen dimming*). |

---

## 🛠️ Fitur Utama Aplikasi

- **3D Parallax Hologram Card (Motion):** Kartu frosted glass interaktif yang merespons kemiringan perangkat secara 3D menggunakan transformasi grafis (`rotationX`, `rotationY`) dengan lapisan pantulan cahaya dinamis.
- **Contactless AirScroll Feed (Computer Vision):** Antarmuka scroll feed vertikal tanpa sentuh menggunakan integrasi Google MediaPipe Hand Gesture Recognizer dan CameraX front camera.
- **Precision Bubble Level (Motion):** Indikator waterpas 2 sumbu presisi dengan peredam *low-pass filter*, feedback getar (*haptic*), dan perubahan warna border saat posisi datar sempurna (±0.5°).
- **Auto Flashlight & Ambient Glow (Environment):** Kontrol pencahayaan adaptif yang mengontrol hardware senter via `CameraManager` berdasarkan ambang batas lux lingkungan.
- **Floating Capsule Navigation:** Navigasi bilah bawah berbentuk kapsul melayang yang aman terhadap inset gesture sistem (`navigationBarsPadding()`).

---

## 🔄 Manajemen Siklus Hidup (Sensor Lifecycle)

Untuk mencegah kebocoran memori (*memory leaks*) dan pemborosan daya baterai saat aplikasi tidak digunakan, seluruh listener sensor dikelola secara ketat mengikuti siklus hidup Android:

- **`onResume()`**: Melakukan registrasi listener sensor (`SensorManager.registerListener()`) saat tampilan aplikasi aktif di layar depan.
- **`onPause()`**: Melepas atau menghentikan listener sensor (`SensorManager.unregisterListener()`) segera setelah aplikasi masuk ke latar belakang atau berpindah layar.
- **Compose DisposableEffect**: Integrasi `DisposableEffect` bersama `LocalLifecycleOwner` memastikan setiap komponen Composable memutus langganan sensor saat keluar dari komposisi.

-## Video Singkat ##-
https://drive.google.com/file/d/1xhV2_BxcpB0OgxWlan3KetBPrDiQDJwt/view?usp=sharing
