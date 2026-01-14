package sistemtitip;

import java.time.LocalDate;
import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Database db = Database.getInstance();
    private static AuthService authService = AuthService.getInstance();
    private static UserSession currentSession;

    public static void main(String[] args) {
        tampilkanMenuAwal();
    }

    private static void tampilkanMenuAwal() {
        while(true) {
            System.out.println("""
                \n=== SISTEM JASA TITIP MERCH ANIME & J-POP ===
                1. Login
                2. Register User Baru
                3. Keluar
                ============================================
                Pilih menu (1-3):""");

            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch(pilihan) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> {
                    System.out.println("Terima kasih!");
                    System.exit(0);
                }
                default -> System.out.println("Pilihan tidak valid!");
            }
        }
    }

    private static void login() {
        System.out.println("\n=== LOGIN ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            currentSession = authService.login(username, password);
            System.out.println("Login berhasil!");

            // Redirect berdasarkan role
            if ("ADMIN".equals(currentSession.getRole())) {
                tampilkanMenuAdmin();
            } else {
                tampilkanMenuUser();
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void register() {
        System.out.println("\n=== REGISTER USER BARU ===");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password (min 6 karakter): ");
        String password = scanner.nextLine();

        System.out.print("Nama Lengkap: ");
        String nama = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("No. Telepon: ");
        String noTelepon = scanner.nextLine();

        System.out.print("Alamat: ");
        String alamat = scanner.nextLine();

        try {
            currentSession = authService.register(username, password, nama, email, noTelepon, alamat);
            System.out.println("Registrasi berhasil! Anda telah login.");
            tampilkanMenuUser();

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void logout() {
        if (currentSession != null) {
            authService.logout(currentSession.getSessionId());
            currentSession = null;
            System.out.println("Logout berhasil!");
        }
        tampilkanMenuAwal();
    }

    // ===============================
    // MENU USER
    // ===============================

    private static void tampilkanMenuUser() {
        Pelanggan pelanggan = currentSession.getPelanggan();
        System.out.println("\nSelamat datang, " + pelanggan.getNama() + "!");

        while(true) {
            System.out.println("""
                \n=== MENU USER ===
                1. Lihat Semua Produk
                2. Lihat Detail Produk
                3. Buat Pesanan Baru
                4. Lihat Pesanan Saya
                5. Edit Pesanan (Sebelum Checkout)
                6. Proses Pembayaran Pesanan
                7. Lihat Status Pembayaran
                8. Lihat Profil Saya
                9. Logout
                =================
                Pilih menu (1-9):""");

            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch(pilihan) {
                case 1 -> lihatSemuaProdukUser();
                case 2 -> lihatDetailProdukUser();
                case 3 -> buatPesananBaru();
                case 4 -> lihatPesananSaya();
                case 5 -> editPesanan();
                case 6 -> prosesPembayaranPesanan();
                case 7 -> lihatStatusPembayaran();
                case 8 -> lihatProfilSaya();
                case 9 -> {
                    logout();
                    return;
                }
                default -> System.out.println("Pilihan tidak valid!");
            }
        }
    }

    private static void lihatSemuaProdukUser() {
        System.out.println("\n=== DAFTAR PRODUK ===");
        List<Produk> produkList = db.getAllProduk();

        if (produkList.isEmpty()) {
            System.out.println("Belum ada produk tersedia.");
            return;
        }

        for (int i = 0; i < produkList.size(); i++) {
            Produk p = produkList.get(i);
            System.out.printf("%d. %s - Rp %,.0f (Stok: %d)%n",
                    i + 1, p.getNama(), p.getHargaJual(), p.getStok());
        }
    }

    private static void lihatDetailProdukUser() {
        lihatSemuaProdukUser();

        System.out.print("\nMasukkan nomor produk: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        List<Produk> produkList = db.getAllProduk();
        if (nomor > 0 && nomor <= produkList.size()) {
            Produk produk = produkList.get(nomor - 1);
            System.out.println(produk.getInfoProduk());

            System.out.print("Ingin memesan produk ini? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                tambahKePesanan(produk);
            }
        } else {
            System.out.println("Produk tidak ditemukan.");
        }
    }

    private static void tambahKePesanan(Produk produk) {
        System.out.print("Jumlah: ");
        int jumlah = scanner.nextInt();
        scanner.nextLine();

        if (produk.validasiPemesanan(jumlah)) {
            // Cari pesanan aktif yang belum checkout
            Pemesanan pesananAktif = null;
            for (Pemesanan p : db.getAllPemesanan()) {
                if (p.getPelanggan().getIdPelanggan().equals(currentSession.getPelanggan().getIdPelanggan())
                        && p.getStatus().equals("PENDING")) {
                    pesananAktif = p;
                    break;
                }
            }

            // Jika belum ada pesanan aktif, buat baru
            if (pesananAktif == null) {
                pesananAktif = new Pemesanan(currentSession.getPelanggan());
                db.addPemesanan(pesananAktif);
            }

            pesananAktif.tambahItem(produk, jumlah);
            System.out.println("Produk berhasil ditambahkan ke pesanan!");
        }
    }

    private static void buatPesananBaru() {
        System.out.println("\n=== BUAT PESANAN BARU ===");

        // Cek jika ada pesanan pending
        for (Pemesanan p : db.getAllPemesanan()) {
            if (p.getPelanggan().getIdPelanggan().equals(currentSession.getPelanggan().getIdPelanggan())
                    && p.getStatus().equals("PENDING")) {
                System.out.println("Anda sudah memiliki pesanan yang belum checkout.");
                System.out.print("Lanjutkan dengan pesanan tersebut? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    prosesCheckout(p);
                    return;
                }
            }
        }

        // Buat pesanan baru
        Pemesanan pesanan = new Pemesanan(currentSession.getPelanggan());
        db.addPemesanan(pesanan);

        boolean tambahLagi = true;
        while (tambahLagi) {
            lihatSemuaProdukUser();
            System.out.print("\nMasukkan nomor produk (0 untuk selesai): ");
            int nomor = scanner.nextInt();
            scanner.nextLine();

            if (nomor == 0) {
                tambahLagi = false;
                continue;
            }

            List<Produk> produkList = db.getAllProduk();
            if (nomor > 0 && nomor <= produkList.size()) {
                Produk produk = produkList.get(nomor - 1);

                System.out.print("Jumlah: ");
                int jumlah = scanner.nextInt();
                scanner.nextLine();

                pesanan.tambahItem(produk, jumlah);
            } else {
                System.out.println("Produk tidak ditemukan.");
            }

            System.out.print("Tambah produk lagi? (y/n): ");
            tambahLagi = scanner.nextLine().equalsIgnoreCase("y");
        }

        if (!pesanan.getItems().isEmpty()) {
            prosesCheckout(pesanan);
        } else {
            System.out.println("Pesanan dibatalkan karena kosong.");
        }
    }

    private static void prosesCheckout(Pemesanan pesanan) {
        System.out.println("\n=== CHECKOUT PESANAN ===");
        System.out.println(pesanan.generateInvoice());

        System.out.print("Lanjutkan checkout? (y/n): ");
        if (!scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.println("Checkout dibatalkan.");
            return;
        }

        System.out.println("""
            \n=== PILIH METODE PENGIRIMAN ===
            1. REGULER (Rp 15,000)
            2. EXPRESS (Rp 30,000)
            3. SAME_DAY (Rp 50,000)
            ===============================
            Pilih metode (1-3):""");

        int metodePengiriman = scanner.nextInt();
        scanner.nextLine();

        String metode = "REGULER";
        switch (metodePengiriman) {
            case 2 -> metode = "EXPRESS";
            case 3 -> metode = "SAME_DAY";
        }

        System.out.println("""
            \n=== PILIH METODE PEMBAYARAN ===
            1. Transfer Bank
            2. Dompet Digital
            ===============================
            Pilih metode (1-2):""");

        int metodeBayar = scanner.nextInt();
        scanner.nextLine();

        SistemPembayaran sistemPembayaran = null;

        if (metodeBayar == 1) {
            System.out.print("Bank: ");
            String bank = scanner.nextLine();

            System.out.print("No. Rekening: ");
            String rekening = scanner.nextLine();

            sistemPembayaran = new PembayaranTransfer(rekening, bank);
        } else if (metodeBayar == 2) {
            System.out.print("Provider (Gopay/OVO/Dana/ShopeePay): ");
            String provider = scanner.nextLine();

            System.out.print("No. Telepon: ");
            String telepon = scanner.nextLine();

            sistemPembayaran = new PembayaranDompetDigital(provider, telepon);
        } else {
            System.out.println("Metode tidak valid.");
            return;
        }

        boolean success = pesanan.checkout(sistemPembayaran, metode);
        if (success) {
            System.out.println("Pesanan berhasil dibuat!");
            System.out.println("ID Pesanan: " + pesanan.getIdPesanan());
        }
    }

    private static void lihatPesananSaya() {
        System.out.println("\n=== PESANAN SAYA ===");

        List<Pemesanan> pesananList = db.getPemesananByPelanggan(
                currentSession.getPelanggan().getIdPelanggan());

        if (pesananList.isEmpty()) {
            System.out.println("Anda belum memiliki pesanan.");
            return;
        }

        for (int i = 0; i < pesananList.size(); i++) {
            Pemesanan p = pesananList.get(i);
            System.out.printf("%d. ID: %s | Status: %s | Total: Rp %,.0f | Tanggal: %s%n",
                    i + 1, p.getIdPesanan(), p.getStatus(), p.getTotalHarga(),
                    p.getTanggalPemesanan().toLocalDate());
        }

        System.out.print("\nLihat detail pesanan? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Masukkan nomor pesanan: ");
            int nomor = scanner.nextInt();
            scanner.nextLine();

            if (nomor > 0 && nomor <= pesananList.size()) {
                System.out.println(pesananList.get(nomor - 1).generateInvoice());
            }
        }
    }

    private static void editPesanan() {
        System.out.println("\n=== EDIT PESANAN ===");

        // Cari pesanan pending milik user
        Pemesanan pesananPending = null;
        for (Pemesanan p : db.getAllPemesanan()) {
            if (p.getPelanggan().getIdPelanggan().equals(currentSession.getPelanggan().getIdPelanggan())
                    && p.getStatus().equals("PENDING")) {
                pesananPending = p;
                break;
            }
        }

        if (pesananPending == null) {
            System.out.println("Tidak ada pesanan yang bisa diedit.");
            return;
        }

        System.out.println("Pesanan yang akan diedit:");
        System.out.println(pesananPending.generateInvoice());

        System.out.println("""
            \n=== PILIH AKSI ===
            1. Tambah Item
            2. Hapus Item
            3. Ubah Alamat Pengiriman
            4. Tambah Catatan
            5. Batalkan Edit
            =================
            Pilih aksi (1-5):""");

        int aksi = scanner.nextInt();
        scanner.nextLine();

        switch (aksi) {
            case 1 -> {
                lihatSemuaProdukUser();
                System.out.print("Masukkan nomor produk: ");
                int nomor = scanner.nextInt();
                scanner.nextLine();

                List<Produk> produkList = db.getAllProduk();
                if (nomor > 0 && nomor <= produkList.size()) {
                    Produk produk = produkList.get(nomor - 1);

                    System.out.print("Jumlah: ");
                    int jumlah = scanner.nextInt();
                    scanner.nextLine();

                    pesananPending.tambahItem(produk, jumlah);
                    System.out.println("Item berhasil ditambahkan.");
                }
            }
            case 2 -> {
                List<Map<Produk, Integer>> items = pesananPending.getItems();
                if (items.isEmpty()) {
                    System.out.println("Pesanan kosong.");
                    break;
                }

                System.out.println("Item dalam pesanan:");
                for (int i = 0; i < items.size(); i++) {
                    for (Map.Entry<Produk, Integer> entry : items.get(i).entrySet()) {
                        System.out.printf("%d. %s (Jumlah: %d)%n",
                                i + 1, entry.getKey().getNama(), entry.getValue());
                    }
                }

                System.out.print("Masukkan nomor item yang akan dihapus: ");
                int nomor = scanner.nextInt();
                scanner.nextLine();

                if (nomor > 0 && nomor <= items.size()) {
                    Map<Produk, Integer> item = items.get(nomor - 1);
                    Produk produk = item.keySet().iterator().next();
                    if (pesananPending.hapusItem(produk)) {
                        System.out.println("Item berhasil dihapus.");
                    }
                }
            }
            case 3 -> {
                System.out.print("Alamat pengiriman baru: ");
                String alamatBaru = scanner.nextLine();
                pesananPending.setAlamatPengiriman(alamatBaru);
                System.out.println("Alamat pengiriman berhasil diubah.");
            }
            case 4 -> {
                System.out.print("Catatan untuk pesanan: ");
                String catatan = scanner.nextLine();
                pesananPending.setCatatan(catatan);
                System.out.println("Catatan berhasil ditambahkan.");
            }
            case 5 -> System.out.println("Edit dibatalkan.");
        }
    }

    private static void prosesPembayaranPesanan() {
        System.out.println("\n=== PROSES PEMBAYARAN ===");

        // Cari pesanan pending
        List<Pemesanan> pesananPending = new ArrayList<>();
        for (Pemesanan p : db.getAllPemesanan()) {
            if (p.getPelanggan().getIdPelanggan().equals(currentSession.getPelanggan().getIdPelanggan())
                    && p.getStatus().equals("PENDING")) {
                pesananPending.add(p);
            }
        }

        if (pesananPending.isEmpty()) {
            System.out.println("Tidak ada pesanan yang perlu dibayar.");
            return;
        }

        System.out.println("Pesanan yang belum dibayar:");
        for (int i = 0; i < pesananPending.size(); i++) {
            Pemesanan p = pesananPending.get(i);
            System.out.printf("%d. ID: %s | Total: Rp %,.0f%n",
                    i + 1, p.getIdPesanan(), p.getTotalHarga());
        }

        System.out.print("Pilih nomor pesanan: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        if (nomor > 0 && nomor <= pesananPending.size()) {
            Pemesanan pesanan = pesananPending.get(nomor - 1);
            System.out.println(pesanan.generateInvoice());

            System.out.print("Lanjutkan pembayaran? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                // Simulasi pembayaran
                System.out.println("Pembayaran sedang diproses...");
                System.out.println("Silakan selesaikan pembayaran di aplikasi/mobile banking Anda.");
                System.out.println("Status pembayaran akan diperbarui oleh admin.");
            }
        }
    }

    private static void lihatStatusPembayaran() {
        System.out.println("\n=== STATUS PEMBAYARAN ===");

        List<Pemesanan> pesananList = db.getPemesananByPelanggan(
                currentSession.getPelanggan().getIdPelanggan());

        if (pesananList.isEmpty()) {
            System.out.println("Anda belum memiliki pesanan.");
            return;
        }

        System.out.println("Status Pembayaran Pesanan Anda:");
        System.out.println("===============================================");
        System.out.printf("%-15s %-15s %-15s %-15s%n",
                "ID Pesanan", "Status Pesanan", "Status Bayar", "Total");
        System.out.println("===============================================");

        for (Pemesanan p : pesananList) {
            String statusBayar = "BELUM DIBAYAR";
            if (!"PENDING".equals(p.getStatus())) {
                statusBayar = "SUDAH DIBAYAR";
            }

            System.out.printf("%-15s %-15s %-15s Rp %,-12.0f%n",
                    p.getIdPesanan(), p.getStatus(), statusBayar, p.getTotalHarga());
        }
        System.out.println("===============================================");
    }

    private static void lihatProfilSaya() {
        System.out.println(currentSession.getPelanggan().getInfoPelanggan());
    }

    // ===============================
    // MENU ADMIN
    // ===============================

    private static void tampilkanMenuAdmin() {
        Admin admin = currentSession.getAdmin();
        System.out.println("\nSelamat datang, Admin " + admin.getNama() + "!");

        while(true) {
            System.out.println("""
                \n=== MENU ADMIN ===
                1. Kelola Produk
                2. Kelola Pelanggan
                3. Kelola Pesanan
                4. Update Status Pesanan
                5. Update Status Pembayaran
                6. Lihat Laporan Transaksi
                7. Logout
                =================
                Pilih menu (1-7):""");

            int pilihan = scanner.nextInt();
            scanner.nextLine();

            switch(pilihan) {
                case 1 -> kelolaProdukAdmin();
                case 2 -> kelolaPelangganAdmin();
                case 3 -> kelolaPesananAdmin();
                case 4 -> updateStatusPesanan();
                case 5 -> updateStatusPembayaran();
                case 6 -> lihatLaporanTransaksi();
                case 7 -> {
                    logout();
                    return;
                }
                default -> System.out.println("Pilihan tidak valid!");
            }
        }
    }

    private static void kelolaProdukAdmin() {
        System.out.println("""
            \n=== KELOLA PRODUK ===
            1. Lihat Semua Produk
            2. Tambah Produk Baru
            3. Update Produk
            4. Hapus Produk
            5. Kembali
            =====================
            Pilih menu (1-5):""");

        int pilihan = scanner.nextInt();
        scanner.nextLine();

        switch(pilihan) {
            case 1 -> lihatSemuaProdukAdmin();
            case 2 -> tambahProdukBaru();
            case 3 -> updateProduk();
            case 4 -> hapusProduk();
        }
    }

    private static void lihatSemuaProdukAdmin() {
        System.out.println("\n=== DAFTAR PRODUK ===");
        List<Produk> produkList = db.getAllProduk();

        if (produkList.isEmpty()) {
            System.out.println("Belum ada produk.");
            return;
        }

        for (int i = 0; i < produkList.size(); i++) {
            Produk p = produkList.get(i);
            System.out.printf("%d. %s - Rp %,.0f - Stok: %d - Kode: %s%n",
                    i + 1, p.getNama(), p.getHargaJual(), p.getStok(), p.getKodeProduk());
        }
    }

    private static void tambahProdukBaru() {
        System.out.println("""
            \n=== TAMBAH PRODUK BARU ===
            1. Merch Anime
            2. Merch J-pop
            =========================
            Pilih jenis (1-2):""");

        int jenis = scanner.nextInt();
        scanner.nextLine();

        if (jenis == 1) {
            tambahProdukAnimeAdmin();
        } else if (jenis == 2) {
            tambahProdukJpopAdmin();
        }
    }

    private static void tambahProdukAnimeAdmin() {
        System.out.println("\n=== TAMBAH PRODUK ANIME ===");

        System.out.print("Kode Produk: ");
        String kode = scanner.nextLine();

        System.out.print("Nama Produk: ");
        String nama = scanner.nextLine();

        System.out.print("Harga Beli: ");
        double hargaBeli = scanner.nextDouble();

        System.out.print("Harga Jual: ");
        double hargaJual = scanner.nextDouble();

        System.out.print("Stok: ");
        int stok = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Kategori: ");
        String kategori = scanner.nextLine();

        System.out.print("Judul Anime: ");
        String judulAnime = scanner.nextLine();

        System.out.print("Karakter: ");
        String karakter = scanner.nextLine();

        System.out.print("Jenis Merchandise: ");
        String jenisMerch = scanner.nextLine();

        System.out.print("Studio Produksi: ");
        String studio = scanner.nextLine();

        System.out.print("Official Licensed? (y/n): ");
        boolean licensed = scanner.nextLine().equalsIgnoreCase("y");

        System.out.print("Rating Anime (0-10): ");
        double rating = scanner.nextDouble();

        System.out.print("Limited Edition? (y/n): ");
        boolean limited = scanner.nextLine().equalsIgnoreCase("y");
        scanner.nextLine();

        MerchAnime produkBaru = new MerchAnime(
                kode, nama, hargaBeli, hargaJual, stok, kategori,
                LocalDate.now(), limited, judulAnime, karakter,
                jenisMerch, studio, licensed, rating
        );

        db.addProduk(produkBaru);
        System.out.println("Produk anime berhasil ditambahkan!");
    }

    private static void tambahProdukJpopAdmin() {
        System.out.println("\n=== TAMBAH PRODUK J-POP ===");

        System.out.print("Kode Produk: ");
        String kode = scanner.nextLine();

        System.out.print("Nama Produk: ");
        String nama = scanner.nextLine();

        System.out.print("Harga Beli: ");
        double hargaBeli = scanner.nextDouble();

        System.out.print("Harga Jual: ");
        double hargaJual = scanner.nextDouble();

        System.out.print("Stok: ");
        int stok = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Kategori: ");
        String kategori = scanner.nextLine();

        System.out.print("Grup Idol: ");
        String grup = scanner.nextLine();

        System.out.print("Member: ");
        String member = scanner.nextLine();

        System.out.print("Jenis Konser: ");
        String jenisKonser = scanner.nextLine();

        System.out.print("Venue: ");
        String venue = scanner.nextLine();

        System.out.print("Include Photocard? (y/n): ");
        boolean photocard = scanner.nextLine().equalsIgnoreCase("y");

        System.out.print("Jumlah Member: ");
        int jumlahMember = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Limited Edition? (y/n): ");
        boolean limited = scanner.nextLine().equalsIgnoreCase("y");

        MerchJpop produkBaru = new MerchJpop(
                kode, nama, hargaBeli, hargaJual, stok, kategori,
                LocalDate.now(), limited, grup, member, jenisKonser,
                venue, LocalDate.now().plusMonths(2), photocard, jumlahMember
        );

        db.addProduk(produkBaru);
        System.out.println("Produk J-pop berhasil ditambahkan!");
    }

    private static void updateProduk() {
        lihatSemuaProdukAdmin();

        System.out.print("\nMasukkan nomor produk yang akan diupdate: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        List<Produk> produkList = db.getAllProduk();
        if (nomor > 0 && nomor <= produkList.size()) {
            Produk produk = produkList.get(nomor - 1);

            System.out.println("Produk yang akan diupdate:");
            System.out.println(produk.getInfoProduk());

            System.out.println("""
                \n=== PILIH FIELD YANG DIUPDATE ===
                1. Nama
                2. Harga Jual
                3. Stok
                4. Semua Field
                5. Batal
                ================================
                Pilih (1-5):""");

            int field = scanner.nextInt();
            scanner.nextLine();

            switch (field) {
                case 1 -> {
                    System.out.print("Nama baru: ");
                    String namaBaru = scanner.nextLine();
                    produk.setNama(namaBaru);
                    db.updateProduk(produk);
                    System.out.println("Nama produk berhasil diupdate.");
                }
                case 2 -> {
                    System.out.print("Harga jual baru: ");
                    double hargaBaru = scanner.nextDouble();
                    scanner.nextLine();
                    produk.setHargaJual(hargaBaru);
                    db.updateProduk(produk);
                    System.out.println("Harga produk berhasil diupdate.");
                }
                case 3 -> {
                    System.out.print("Stok baru: ");
                    int stokBaru = scanner.nextInt();
                    scanner.nextLine();
                    produk.setStok(stokBaru);
                    db.updateProduk(produk);
                    System.out.println("Stok produk berhasil diupdate.");
                }
                case 4 -> {
                    // Update semua field (sederhana)
                    System.out.print("Nama baru: ");
                    produk.setNama(scanner.nextLine());

                    System.out.print("Harga jual baru: ");
                    produk.setHargaJual(scanner.nextDouble());
                    scanner.nextLine();

                    System.out.print("Stok baru: ");
                    produk.setStok(scanner.nextInt());
                    scanner.nextLine();

                    db.updateProduk(produk);
                    System.out.println("Produk berhasil diupdate.");
                }
                case 5 -> System.out.println("Update dibatalkan.");
            }
        }
    }

    private static void hapusProduk() {
        lihatSemuaProdukAdmin();

        System.out.print("\nMasukkan nomor produk yang akan dihapus: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        List<Produk> produkList = db.getAllProduk();
        if (nomor > 0 && nomor <= produkList.size()) {
            Produk produk = produkList.get(nomor - 1);

            System.out.print("Yakin hapus produk " + produk.getNama() + "? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                // Cek apakah produk ada di pesanan aktif
                boolean adaDiPesanan = false;
                for (Pemesanan p : db.getAllPemesanan()) {
                    for (Map<Produk, Integer> item : p.getItems()) {
                        if (item.containsKey(produk)) {
                            adaDiPesanan = true;
                            break;
                        }
                    }
                    if (adaDiPesanan) break;
                }

                if (adaDiPesanan) {
                    System.out.println("Produk tidak bisa dihapus karena ada di pesanan aktif.");
                } else {
                    produkList.remove(nomor - 1);
                    System.out.println("Produk berhasil dihapus.");
                }
            }
        }
    }

    private static void kelolaPelangganAdmin() {
        System.out.println("\n=== DAFTAR PELANGGAN ===");
        List<Pelanggan> pelangganList = db.getAllPelanggan();

        if (pelangganList.isEmpty()) {
            System.out.println("Belum ada pelanggan terdaftar.");
            return;
        }

        for (int i = 0; i < pelangganList.size(); i++) {
            Pelanggan p = pelangganList.get(i);
            System.out.printf("%d. %s (%s) - Total Belanja: Rp %,.0f%n",
                    i + 1, p.getNama(), p.getUsername(), p.getTotalBelanja());
        }

        System.out.print("\nLihat detail pelanggan? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Masukkan nomor pelanggan: ");
            int nomor = scanner.nextInt();
            scanner.nextLine();

            if (nomor > 0 && nomor <= pelangganList.size()) {
                System.out.println(pelangganList.get(nomor - 1).getInfoPelanggan());
            }
        }
    }

    private static void kelolaPesananAdmin() {
        System.out.println("\n=== KELOLA PESANAN ===");
        List<Pemesanan> pesananList = db.getAllPemesanan();

        if (pesananList.isEmpty()) {
            System.out.println("Belum ada pesanan.");
            return;
        }

        System.out.println("Daftar Semua Pesanan:");
        System.out.println("==================================================================");
        System.out.printf("%-4s %-12s %-20s %-12s %-15s %-10s%n",
                "No", "ID Pesanan", "Pelanggan", "Status", "Total", "Tanggal");
        System.out.println("==================================================================");

        for (int i = 0; i < pesananList.size(); i++) {
            Pemesanan p = pesananList.get(i);
            System.out.printf("%-4d %-12s %-20s %-12s Rp %,-12.0f %-10s%n",
                    i + 1, p.getIdPesanan(),
                    p.getPelanggan().getNama().substring(0, Math.min(20, p.getPelanggan().getNama().length())),
                    p.getStatus(), p.getTotalHarga(),
                    p.getTanggalPemesanan().toLocalDate());
        }
        System.out.println("==================================================================");

        System.out.print("\nLihat detail pesanan? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Masukkan nomor pesanan: ");
            int nomor = scanner.nextInt();
            scanner.nextLine();

            if (nomor > 0 && nomor <= pesananList.size()) {
                System.out.println(pesananList.get(nomor - 1).generateInvoice());
            }
        }
    }

    private static void updateStatusPesanan() {
        System.out.println("\n=== UPDATE STATUS PESANAN ===");

        // Tampilkan pesanan dengan status bukan DELIVERED atau CANCELLED
        List<Pemesanan> pesananAktif = new ArrayList<>();
        List<Pemesanan> semuaPesanan = db.getAllPemesanan();

        for (Pemesanan p : semuaPesanan) {
            if (!"DELIVERED".equals(p.getStatus()) && !"CANCELLED".equals(p.getStatus())) {
                pesananAktif.add(p);
            }
        }

        if (pesananAktif.isEmpty()) {
            System.out.println("Tidak ada pesanan yang perlu diupdate status.");
            return;
        }

        System.out.println("Pesanan yang bisa diupdate:");
        for (int i = 0; i < pesananAktif.size(); i++) {
            Pemesanan p = pesananAktif.get(i);
            System.out.printf("%d. ID: %s | Pelanggan: %s | Status: %s%n",
                    i + 1, p.getIdPesanan(), p.getPelanggan().getNama(), p.getStatus());
        }

        System.out.print("\nPilih nomor pesanan: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        if (nomor > 0 && nomor <= pesananAktif.size()) {
            Pemesanan pesanan = pesananAktif.get(nomor - 1);

            System.out.println("Status saat ini: " + pesanan.getStatus());
            System.out.println("""
                \nPilih status baru:
                1. PROCESSING
                2. SHIPPED
                3. DELIVERED
                4. CANCELLED
                ================
                Pilih (1-4):""");

            int statusBaru = scanner.nextInt();
            scanner.nextLine();

            String status = pesanan.getStatus();
            switch (statusBaru) {
                case 1 -> status = "PROCESSING";
                case 2 -> status = "SHIPPED";
                case 3 -> status = "DELIVERED";
                case 4 -> status = "CANCELLED";
            }

            pesanan.updateStatus(status);
            System.out.println("Status pesanan berhasil diupdate menjadi: " + status);

            // Jika status menjadi DELIVERED, update poin pelanggan
            if ("DELIVERED".equals(status)) {
                pesanan.getPelanggan().tambahBelanja(pesanan.getTotalHarga());
                System.out.println("Poin loyalty pelanggan telah ditambahkan.");
            }
        }
    }

    private static void updateStatusPembayaran() {
        System.out.println("\n=== UPDATE STATUS PEMBAYARAN ===");

        // Tampilkan pesanan dengan status PENDING atau PROCESSING
        List<Pemesanan> pesananBelumLunas = new ArrayList<>();
        for (Pemesanan p : db.getAllPemesanan()) {
            if ("PENDING".equals(p.getStatus()) || "PROCESSING".equals(p.getStatus())) {
                pesananBelumLunas.add(p);
            }
        }

        if (pesananBelumLunas.isEmpty()) {
            System.out.println("Semua pesanan sudah lunas.");
            return;
        }

        System.out.println("Pesanan yang belum lunas:");
        for (int i = 0; i < pesananBelumLunas.size(); i++) {
            Pemesanan p = pesananBelumLunas.get(i);
            System.out.printf("%d. ID: %s | Pelanggan: %s | Total: Rp %,.0f | Status: %s%n",
                    i + 1, p.getIdPesanan(), p.getPelanggan().getNama(),
                    p.getTotalHarga(), p.getStatus());
        }

        System.out.print("\nPilih nomor pesanan: ");
        int nomor = scanner.nextInt();
        scanner.nextLine();

        if (nomor > 0 && nomor <= pesananBelumLunas.size()) {
            Pemesanan pesanan = pesananBelumLunas.get(nomor - 1);

            System.out.println("Detail pesanan:");
            System.out.println(pesanan.generateInvoice());

            System.out.print("\nUpdate status pembayaran menjadi LUNAS? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                if ("PENDING".equals(pesanan.getStatus())) {
                    pesanan.updateStatus("PROCESSING");
                }
                System.out.println("Status pembayaran berhasil diupdate.");
                System.out.println("Pesanan sekarang diproses.");
            }
        }
    }

    private static void lihatLaporanTransaksi() {
        System.out.println("""
            \n=== LAPORAN TRANSAKSI ===
            1. Ringkasan Penjualan
            2. Transaksi Per Periode
            3. Produk Terlaris
            4. Pelanggan Terbaik
            5. Kembali
            ========================
            Pilih menu (1-5):""");

        int pilihan = scanner.nextInt();
        scanner.nextLine();

        switch(pilihan) {
            case 1 -> tampilkanRingkasanPenjualanAdmin();
            case 2 -> tampilkanTransaksiPeriode();
            case 3 -> tampilkanProdukTerlarisAdmin();
            case 4 -> tampilkanPelangganTerbaikAdmin();
        }
    }

    private static void tampilkanRingkasanPenjualanAdmin() {
        System.out.println("\n=== RINGKASAN PENJUALAN ===");

        List<Pemesanan> semuaPesanan = db.getAllPemesanan();
        double totalPendapatan = 0;
        int totalPesanan = 0;
        int pesananSelesai = 0;

        for (Pemesanan p : semuaPesanan) {
            if (!"CANCELLED".equals(p.getStatus()) && !"PENDING".equals(p.getStatus())) {
                totalPendapatan += p.getTotalHarga();
                totalPesanan++;

                if ("DELIVERED".equals(p.getStatus())) {
                    pesananSelesai++;
                }
            }
        }

        System.out.printf("""
            Total Pesanan: %d
            Pesanan Selesai: %d
            Total Pendapatan: Rp %,.0f
            Rata-rata Transaksi: Rp %,.0f
            Conversion Rate: %.1f%%
            """, totalPesanan, pesananSelesai, totalPendapatan,
                totalPesanan > 0 ? totalPendapatan / totalPesanan : 0,
                totalPesanan > 0 ? (pesananSelesai * 100.0 / totalPesanan) : 0);
    }

    private static void tampilkanTransaksiPeriode() {
        System.out.println("\n=== TRANSAKSI 30 HARI TERAKHIR ===");

        List<Pemesanan> semuaPesanan = db.getAllPemesanan();
        LocalDate batasTanggal = LocalDate.now().minusDays(30);

        System.out.println("Transaksi dari " + batasTanggal + " hingga " + LocalDate.now());
        System.out.println("========================================================");
        System.out.printf("%-12s %-20s %-15s %-15s%n",
                "ID Pesanan", "Pelanggan", "Tanggal", "Total");
        System.out.println("========================================================");

        int count = 0;
        double total = 0;

        for (Pemesanan p : semuaPesanan) {
            if (p.getTanggalPemesanan().toLocalDate().isAfter(batasTanggal.minusDays(1))) {
                System.out.printf("%-12s %-20s %-15s Rp %,-12.0f%n",
                        p.getIdPesanan(),
                        p.getPelanggan().getNama().substring(0, Math.min(20, p.getPelanggan().getNama().length())),
                        p.getTanggalPemesanan().toLocalDate(),
                        p.getTotalHarga());
                count++;
                total += p.getTotalHarga();
            }
        }

        System.out.println("========================================================");
        System.out.printf("Total Transaksi: %d | Total Nilai: Rp %,.0f%n", count, total);
    }

    private static void tampilkanProdukTerlarisAdmin() {
        System.out.println("\n=== PRODUK TERLARIS ===");

        // Hitung jumlah terjual per produk
        Map<String, Integer> penjualanProduk = new HashMap<>();
        Map<String, Double> revenueProduk = new HashMap<>();

        for (Pemesanan pesanan : db.getAllPemesanan()) {
            if (!"CANCELLED".equals(pesanan.getStatus())) {
                for (Map<Produk, Integer> item : pesanan.getItems()) {
                    for (Map.Entry<Produk, Integer> entry : item.entrySet()) {
                        String namaProduk = entry.getKey().getNama();
                        int jumlah = entry.getValue();
                        double revenue = entry.getKey().getHargaJual() * jumlah;

                        penjualanProduk.put(namaProduk,
                                penjualanProduk.getOrDefault(namaProduk, 0) + jumlah);
                        revenueProduk.put(namaProduk,
                                revenueProduk.getOrDefault(namaProduk, 0.0) + revenue);
                    }
                }
            }
        }

        if (penjualanProduk.isEmpty()) {
            System.out.println("Belum ada penjualan.");
            return;
        }

        System.out.println("Top 5 Produk Terlaris:");
        System.out.println("==========================================================");
        System.out.printf("%-30s %-15s %-20s%n", "Nama Produk", "Terjual", "Total Revenue");
        System.out.println("==========================================================");

        penjualanProduk.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    String nama = entry.getKey();
                    int terjual = entry.getValue();
                    double revenue = revenueProduk.get(nama);
                    System.out.printf("%-30s %-15d Rp %,-17.0f%n",
                            nama.length() > 30 ? nama.substring(0, 27) + "..." : nama,
                            terjual, revenue);
                });
        System.out.println("==========================================================");
    }

    private static void tampilkanPelangganTerbaikAdmin() {
        System.out.println("\n=== PELANGGAN TERBAIK ===");

        List<Pelanggan> pelangganList = db.getAllPelanggan();

        if (pelangganList.isEmpty()) {
            System.out.println("Belum ada pelanggan.");
            return;
        }

        System.out.println("Top 5 Pelanggan Berdasarkan Total Belanja:");
        System.out.println("=========================================================================");
        System.out.printf("%-5s %-20s %-15s %-15s %-15s%n",
                "Rank", "Nama", "Username", "Total Belanja", "Jumlah Pesanan");
        System.out.println("=========================================================================");

        pelangganList.stream()
                .sorted((p1, p2) -> Double.compare(p2.getTotalBelanja(), p1.getTotalBelanja()))
                .limit(5)
                .forEach(p -> System.out.printf("%-5d %-20s %-15s Rp %,-12.0f %-15d%n",
                        pelangganList.indexOf(p) + 1,
                        p.getNama().length() > 20 ? p.getNama().substring(0, 17) + "..." : p.getNama(),
                        p.getUsername(),
                        p.getTotalBelanja(),
                        p.getRiwayatPemesanan().size()));
        System.out.println("=========================================================================");
    }
}