package sistemtitip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Pemesanan {
    private String idPesanan;
    private Pelanggan pelanggan;
    private List<Map<Produk, Integer>> items; // Produk dan jumlah
    private LocalDateTime tanggalPemesanan;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private double totalHarga;
    private double biayaPengiriman;
    private String alamatPengiriman;
    private SistemPembayaran sistemPembayaran;
    private String metodePengiriman;
    private String nomorResi;
    private String catatan;

    public Pemesanan(Pelanggan pelanggan) {
        this.idPesanan = generateIdPesanan();
        this.pelanggan = pelanggan;
        this.items = new ArrayList<>();
        this.tanggalPemesanan = LocalDateTime.now();
        this.status = "PENDING";
        this.totalHarga = 0;
        this.biayaPengiriman = 0;
        this.alamatPengiriman = pelanggan.getAlamat();
        this.catatan = "";
    }

    private String generateIdPesanan() {
        Random rand = new Random();
        return "ORD" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%04d", rand.nextInt(10000));
    }

    // Method untuk menambah item
    public void tambahItem(Produk produk, int jumlah) {
        if(produk.validasiPemesanan(jumlah)) {
            Map<Produk, Integer> item = new HashMap<>();
            item.put(produk, jumlah);
            items.add(item);
            produk.kurangiStok(jumlah);
            hitungTotalHarga();
            System.out.println("Item berhasil ditambahkan ke pesanan");
        }
    }

    // Method untuk menghapus item
    public boolean hapusItem(Produk produk) {
        for(Map<Produk, Integer> item : items) {
            if(item.containsKey(produk)) {
                int jumlah = item.get(produk);
                produk.tambahStok(jumlah); // Kembalikan stok
                items.remove(item);
                hitungTotalHarga();
                return true;
            }
        }
        return false;
    }

    // Hitung total harga dengan diskon
    private void hitungTotalHarga() {
        totalHarga = 0;
        for(Map<Produk, Integer> item : items) {
            for(Map.Entry<Produk, Integer> entry : item.entrySet()) {
                Produk produk = entry.getKey();
                int jumlah = entry.getValue();
                totalHarga += produk.hitungHargaSetelahPPN() * jumlah;
            }
        }

        // Apply diskon membership
        double diskon = pelanggan.hitungDiskon();
        totalHarga -= totalHarga * diskon;

        // Tambah biaya pengiriman
        totalHarga += biayaPengiriman;
    }

    // Proses checkout
    public boolean checkout(SistemPembayaran sistemPembayaran, String metodePengiriman) {
        if(items.isEmpty()) {
            System.out.println("Tidak ada item dalam pesanan");
            return false;
        }

        this.sistemPembayaran = sistemPembayaran;
        this.metodePengiriman = metodePengiriman;

        // Hitung biaya pengiriman
        hitungBiayaPengiriman(metodePengiriman);

        // Proses pembayaran
        boolean pembayaranBerhasil = sistemPembayaran.prosesPembayaran(
                totalHarga, "Pembayaran Pesanan #" + idPesanan);

        if(pembayaranBerhasil) {
            status = "PROCESSING";
            generateNomorResi();

            // Update data pelanggan
            pelanggan.tambahBelanja(totalHarga);
            pelanggan.tambahRiwayatPemesanan(idPesanan);

            System.out.println("Checkout berhasil!");
            System.out.println(generateInvoice());
            return true;
        }

        status = "PAYMENT_FAILED";
        System.out.println("Pembayaran gagal");
        return false;
    }

    private void hitungBiayaPengiriman(String metode) {
        switch(metode.toUpperCase()) {
            case "REGULER":
                biayaPengiriman = 15000;
                break;
            case "EXPRESS":
                biayaPengiriman = 30000;
                break;
            case "SAME_DAY":
                biayaPengiriman = 50000;
                break;
            case "INTERNATIONAL":
                biayaPengiriman = 200000;
                break;
            default:
                biayaPengiriman = 20000;
        }

        // Tambah biaya khusus untuk produk tertentu
        for(Map<Produk, Integer> item : items) {
            for(Produk produk : item.keySet()) {
                if(produk instanceof MerchJpop) {
                    MerchJpop merchJpop = (MerchJpop) produk;
                    biayaPengiriman += merchJpop.hitungBiayaPengirimanKhusus();
                }
            }
        }
    }

    private void generateNomorResi() {
        Random rand = new Random();
        this.nomorResi = "RESI" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%010d", rand.nextInt(1000000000));
    }

    public String generateInvoice() {
        StringBuilder invoice = new StringBuilder();
        invoice.append(String.format("""
            ===== INVOICE PESANAN =====
            No. Pesanan: %s
            Pelanggan: %s
            Tanggal: %s
            Status: %s
            ===========================
            ITEM YANG DIPESAN:
            """, idPesanan, pelanggan.getNama(),
                tanggalPemesanan.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                status));

        int counter = 1;
        for(Map<Produk, Integer> item : items) {
            for(Map.Entry<Produk, Integer> entry : item.entrySet()) {
                Produk produk = entry.getKey();
                int jumlah = entry.getValue();
                invoice.append(String.format("""
                    %d. %s
                       Jumlah: %d
                       Harga Satuan: Rp %,.0f
                       Subtotal: Rp %,.0f
                    """, counter++, produk.getNama(), jumlah,
                        produk.hitungHargaSetelahPPN(),
                        produk.hitungHargaSetelahPPN() * jumlah));
            }
        }

        invoice.append(String.format("""
            ===========================
            Subtotal Item: Rp %,.0f
            Diskon Membership: %.0f%%
            Biaya Pengiriman: Rp %,.0f
            TOTAL: Rp %,.0f
            ===========================
            Metode Pengiriman: %s
            Alamat: %s
            No. Resi: %s
            Catatan: %s
            ===========================
            """, totalHarga - biayaPengiriman,
                pelanggan.hitungDiskon() * 100,
                biayaPengiriman,
                totalHarga,
                metodePengiriman,
                alamatPengiriman,
                nomorResi != null ? nomorResi : "Belum tersedia",
                catatan));

        return invoice.toString();
    }

    // Update status pesanan
    public void updateStatus(String status) {
        this.status = status;
        System.out.println("Status pesanan " + idPesanan + " diubah menjadi: " + status);
    }

    // Getters
    public String getIdPesanan() { return idPesanan; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public LocalDateTime getTanggalPemesanan() { return tanggalPemesanan; }
    public String getStatus() { return status; }
    public double getTotalHarga() { return totalHarga; }
    public String getNomorResi() { return nomorResi; }
    public List<Map<Produk, Integer>> getItems() { return new ArrayList<>(items); }

    // Setters
    public void setAlamatPengiriman(String alamat) {
        this.alamatPengiriman = alamat;
    }
    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}