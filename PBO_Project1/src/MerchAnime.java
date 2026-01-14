package sistemtitip;

import java.time.LocalDate;

public class MerchAnime extends Produk {
    private String judulAnime;
    private String karakter;
    private String jenisMerchandise; // Figure, Poster, Keychain, dll
    private String studioProduksi;
    private boolean isOfficialLicensed;
    private double ratingAnime;

    public MerchAnime(String kodeProduk, String nama, double hargaBeli,
                      double hargaJual, int stok, String kategori,
                      LocalDate tanggalRilis, boolean isLimitedEdition,
                      String judulAnime, String karakter, String jenisMerchandise,
                      String studioProduksi, boolean isOfficialLicensed, double ratingAnime) {
        super(kodeProduk, nama, hargaBeli, hargaJual, stok, kategori,
                tanggalRilis, isLimitedEdition);
        this.judulAnime = judulAnime;
        this.karakter = karakter;
        this.jenisMerchandise = jenisMerchandise;
        this.studioProduksi = studioProduksi;
        this.isOfficialLicensed = isOfficialLicensed;
        this.ratingAnime = ratingAnime;
    }

    // Override abstract methods
    @Override
    public double hitungHargaSetelahPPN() {
        double ppn = isOfficialLicensed ? 0.11 : 0.15; // PPN berbeda untuk produk licensed
        return getHargaJual() * (1 + ppn);
    }

    @Override
    public String getInfoProduk() {
        return String.format("""
            ===== MERCH ANIME =====
            Kode: %s
            Nama: %s
            Anime: %s
            Karakter: %s
            Jenis: %s
            Studio: %s
            Licensed: %s
            Rating Anime: %.1f/10
            Harga: Rp %,.0f
            Stok: %d
            Limited Edition: %s
            Tanggal Rilis: %s
            =======================
            """, getKodeProduk(), getNama(), judulAnime, karakter,
                jenisMerchandise, studioProduksi,
                isOfficialLicensed ? "Ya" : "Tidak", ratingAnime,
                getHargaJual(), getStok(),
                isLimitedEdition() ? "Ya" : "Tidak",
                getTanggalRilis());
    }

    @Override
    public boolean validasiPemesanan(int jumlah) {
        if(isLimitedEdition() && jumlah > 1) {
            System.out.println("Produk Limited Edition hanya bisa dibeli 1 item per pelanggan");
            return false;
        }
        if(jumlah > getStok()) {
            System.out.println("Stok tidak mencukupi");
            return false;
        }
        return true;
    }

    // Method khusus untuk merch anime
    public double hitungHargaPreOrder(double deposit) {
        if(deposit < getHargaJual() * 0.3) {
            throw new IllegalArgumentException("Deposit minimal 30% dari harga");
        }
        return getHargaJual() - deposit;
    }

    public boolean isFromPopularAnime() {
        return ratingAnime >= 8.0;
    }

    // Getter dan Setter tambahan
    public String getJudulAnime() { return judulAnime; }
    public void setJudulAnime(String judulAnime) { this.judulAnime = judulAnime; }

    public String getKarakter() { return karakter; }
    public void setKarakter(String karakter) { this.karakter = karakter; }

    public String getJenisMerchandise() { return jenisMerchandise; }
    public void setJenisMerchandise(String jenisMerchandise) { this.jenisMerchandise = jenisMerchandise; }

    public String getStudioProduksi() { return studioProduksi; }
    public void setStudioProduksi(String studioProduksi) { this.studioProduksi = studioProduksi; }

    public boolean isOfficialLicensed() { return isOfficialLicensed; }
    public void setOfficialLicensed(boolean officialLicensed) { isOfficialLicensed = officialLicensed; }

    public double getRatingAnime() { return ratingAnime; }
    public void setRatingAnime(double ratingAnime) {
        if(ratingAnime >= 0 && ratingAnime <= 10) this.ratingAnime = ratingAnime;
    }
}