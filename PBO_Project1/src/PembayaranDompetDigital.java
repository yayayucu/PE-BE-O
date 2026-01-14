package sistemtitip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class PembayaranDompetDigital implements SistemPembayaran {
    private String provider; // Gopay, OVO, Dana, ShopeePay
    private String nomorTelepon;
    private double jumlah;
    private String status;
    private LocalDateTime waktuTransaksi;
    private String nomorTransaksi;
    private String qrCode;

    public PembayaranDompetDigital(String provider, String nomorTelepon) {
        this.provider = provider;
        this.nomorTelepon = nomorTelepon;
        this.status = "PENDING";
        this.waktuTransaksi = LocalDateTime.now();
        generateNomorTransaksi();
        generateQRCode();
    }

    private void generateNomorTransaksi() {
        Random rand = new Random();
        this.nomorTransaksi = "EWLT" +
                waktuTransaksi.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%04d", rand.nextInt(10000));
    }

    private void generateQRCode() {
        Random rand = new Random();
        this.qrCode = "QR" + provider.toUpperCase() +
                String.format("%08d", rand.nextInt(100000000));
    }

    @Override
    public boolean prosesPembayaran(double jumlah, String metode) {
        this.jumlah = jumlah;

        // Simulasi pembayaran dompet digital
        if(nomorTelepon.length() >= 10 && jumlah > 0) {
            System.out.println("Memproses pembayaran " + provider + " ke nomor " + nomorTelepon);
            System.out.println("Scan QR Code: " + qrCode);
            System.out.println("Jumlah: Rp " + String.format("%,.0f", jumlah));

            // Simulasi delay
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.status = "SUCCESS";
            this.waktuTransaksi = LocalDateTime.now();
            System.out.println("Pembayaran " + provider + " berhasil!");
            return true;
        }

        this.status = "FAILED";
        return false;
    }

    @Override
    public String generateInvoice(String pesananId, double jumlah) {
        double cashback = hitungCashback();
        double totalSetelahCashback = jumlah - cashback;

        return String.format("""
            ===== INVOICE DOMPET DIGITAL =====
            No. Transaksi: %s
            No. Pesanan: %s
            Provider: %s
            No. Telepon: %s
            QR Code: %s
            Jumlah: Rp %,.0f
            Biaya Admin: Rp %,.0f
            Cashback: Rp %,.0f
            Total: Rp %,.0f
            Status: %s
            Waktu: %s
            =================================
            """, nomorTransaksi, pesananId, provider, nomorTelepon, qrCode,
                this.jumlah, hitungBiayaAdmin(), cashback,
                totalSetelahCashback + hitungBiayaAdmin(),
                status,
                waktuTransaksi.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
    }

    @Override
    public boolean validasiPembayaran(String nomorTransaksi) {
        return this.nomorTransaksi.equals(nomorTransaksi) &&
                this.status.equals("SUCCESS");
    }

    @Override
    public double hitungBiayaAdmin() {
        return 0; // Dompet digital biasanya tidak ada biaya admin
    }

    public double hitungCashback() {
        switch(provider.toUpperCase()) {
            case "GOPAY":
                return jumlah * 0.02; // 2% cashback
            case "OVO":
                return jumlah * 0.015; // 1.5% cashback
            case "DANA":
                return Math.min(jumlah * 0.01, 10000); // 1% max 10k
            default:
                return 0;
        }
    }

    @Override
    public String getStatusPembayaran() {
        return status;
    }

    // Getter
    public String getNomorTransaksi() { return nomorTransaksi; }
    public String getQRCode() { return qrCode; }
}