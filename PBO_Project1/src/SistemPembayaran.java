package sistemtitip;

public interface SistemPembayaran {
    boolean prosesPembayaran(double jumlah, String metode);
    String generateInvoice(String pesananId, double jumlah);
    boolean validasiPembayaran(String nomorTransaksi);
    double hitungBiayaAdmin();
    String getStatusPembayaran();
}