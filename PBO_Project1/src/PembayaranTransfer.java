package sistemtitip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class PembayaranTransfer implements SistemPembayaran {
    private String nomorRekening;
    private String bank;
    private double jumlah;
    private String status;
    private LocalDateTime waktuTransaksi;
    private String nomorTransaksi;

    public PembayaranTransfer(String nomorRekening, String bank) {
        this.nomorRekening = nomorRekening;
        this.bank = bank;
        this.status = "PENDING";
        this.waktuTransaksi = LocalDateTime.now();
        generateNomorTransaksi();
    }

    private void generateNomorTransaksi() {
        Random rand = new Random();
        this.nomorTransaksi = "TRF" +
                waktuTransaksi.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%04d", rand.nextInt(10000));
    }

    @Override
    public boolean prosesPembayaran(double jumlah, String metode) {
        this.jumlah = jumlah;

        // Simulasi proses transfer
        if(nomorRekening.length() >= 10 && jumlah > 0) {
            System.out.println("Memproses transfer ke rekening " + nomorRekening + " bank " + bank);
            System.out.println("Jumlah: Rp " + String.format("%,.0f", jumlah));

            // Simulasi delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.status = "SUCCESS";
            this.waktuTransaksi = LocalDateTime.now();
            System.out.println("Transfer berhasil!");
            return true;
        }

        this.status = "FAILED";
        return false;
    }

    @Override
    public String generateInvoice(String pesananId, double jumlah) {
        return String.format("""
            ===== INVOICE TRANSFER =====
            No. Transaksi: %s
            No. Pesanan: %s
            Bank: %s
            No. Rekening: %s
            Jumlah: Rp %,.0f
            Biaya Admin: Rp %,.0f
            Total: Rp %,.0f
            Status: %s
            Waktu: %s
            ===========================
            """, nomorTransaksi, pesananId, bank, nomorRekening,
                this.jumlah, hitungBiayaAdmin(),
                this.jumlah + hitungBiayaAdmin(),
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
        switch(bank.toUpperCase()) {
            case "BCA":
            case "MANDIRI":
            case "BRI":
                return 6500;
            case "BNI":
                return 7500;
            default:
                return 10000;
        }
    }

    @Override
    public String getStatusPembayaran() {
        return status;
    }

    // Getter
    public String getNomorTransaksi() { return nomorTransaksi; }
    public LocalDateTime getWaktuTransaksi() { return waktuTransaksi; }
}