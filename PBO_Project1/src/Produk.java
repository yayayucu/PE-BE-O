package sistemtitip;

import java.time.LocalDate;

public abstract class Produk {
    private String kodeProduk;
    private String nama;
    private double hargaBeli;
    private double hargaJual;
    private int stok;
    private String kategori;
    private LocalDate tanggalRilis;
    private boolean isLimitedEdition;

    // Constructor
    public Produk(String kodeProduk, String nama, double hargaBeli,
                  double hargaJual, int stok, String kategori,
                  LocalDate tanggalRilis, boolean isLimitedEdition) {
        this.kodeProduk = kodeProduk;
        this.nama = nama;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.kategori = kategori;
        this.tanggalRilis = tanggalRilis;
        this.isLimitedEdition = isLimitedEdition;
    }

    // Getter dan Setter (Enkapsulasi)
    public String getKodeProduk() { return kodeProduk; }
    public void setKodeProduk(String kodeProduk) { this.kodeProduk = kodeProduk; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public double getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(double hargaBeli) {
        if(hargaBeli >= 0) this.hargaBeli = hargaBeli;
    }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) {
        if(hargaJual >= this.hargaBeli) this.hargaJual = hargaJual;
    }

    public int getStok() { return stok; }
    public void setStok(int stok) {
        if(stok >= 0) this.stok = stok;
    }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public LocalDate getTanggalRilis() { return tanggalRilis; }
    public void setTanggalRilis(LocalDate tanggalRilis) {
        this.tanggalRilis = tanggalRilis;
    }

    public boolean isLimitedEdition() { return isLimitedEdition; }
    public void setLimitedEdition(boolean limitedEdition) {
        this.isLimitedEdition = limitedEdition;
    }

    // Abstract method untuk polimorfisme
    public abstract double hitungHargaSetelahPPN();
    public abstract String getInfoProduk();
    public abstract boolean validasiPemesanan(int jumlah);

    // Method konkrit
    public double hitungKeuntungan() {
        return (hargaJual - hargaBeli) * stok;
    }

    public boolean kurangiStok(int jumlah) {
        if(jumlah > 0 && jumlah <= stok) {
            stok -= jumlah;
            return true;
        }
        return false;
    }

    public void tambahStok(int jumlah) {
        if(jumlah > 0) {
            stok += jumlah;
        }
    }

    public boolean isProdukBaru() {
        return tanggalRilis.isAfter(LocalDate.now().minusMonths(6));
    }
}