package sistemtitip;

import java.time.LocalDate;

public class MerchJpop extends Produk {
    private String grupIdol;
    private String member;
    private String jenisKonser; // Tour, Single Release, dll
    private String venue;
    private LocalDate tanggalKonser;
    private boolean includePhotocard;
    private int jumlahMember;

    public MerchJpop(String kodeProduk, String nama, double hargaBeli,
                     double hargaJual, int stok, String kategori,
                     LocalDate tanggalRilis, boolean isLimitedEdition,
                     String grupIdol, String member, String jenisKonser,
                     String venue, LocalDate tanggalKonser,
                     boolean includePhotocard, int jumlahMember) {
        super(kodeProduk, nama, hargaBeli, hargaJual, stok, kategori,
                tanggalRilis, isLimitedEdition);
        this.grupIdol = grupIdol;
        this.member = member;
        this.jenisKonser = jenisKonser;
        this.venue = venue;
        this.tanggalKonser = tanggalKonser;
        this.includePhotocard = includePhotocard;
        this.jumlahMember = jumlahMember;
    }

    // Override abstract methods
    @Override
    public double hitungHargaSetelahPPN() {
        double ppn = 0.1; // PPN standar untuk merch J-pop
        if(isLimitedEdition()) {
            ppn += 0.05; // Tambahan PPN untuk limited edition
        }
        return getHargaJual() * (1 + ppn);
    }

    @Override
    public String getInfoProduk() {
        return String.format("""
            ===== MERCH J-POP =====
            Kode: %s
            Nama: %s
            Grup: %s
            Member: %s
            Jenis Konser: %s
            Venue: %s
            Tanggal Konser: %s
            Include Photocard: %s
            Jumlah Member: %d
            Harga: Rp %,.0f
            Stok: %d
            Limited Edition: %s
            Tanggal Rilis Merch: %s
            =======================
            """, getKodeProduk(), getNama(), grupIdol, member,
                jenisKonser, venue, tanggalKonser,
                includePhotocard ? "Ya" : "Tidak", jumlahMember,
                getHargaJual(), getStok(),
                isLimitedEdition() ? "Ya" : "Tidak",
                getTanggalRilis());
    }

    @Override
    public boolean validasiPemesanan(int jumlah) {
        if(jumlah > 5) {
            System.out.println("Maksimal pembelian 5 item untuk merch J-pop");
            return false;
        }
        if(includePhotocard && jumlah > 2) {
            System.out.println("Produk dengan photocard maksimal 2 item");
            return false;
        }
        return super.getStok() >= jumlah;
    }

    // Method khusus untuk merch J-pop
    public double hitungBiayaPengirimanKhusus() {
        double biayaDasar = 50000;
        if(isLimitedEdition()) {
            biayaDasar += 30000; // Asuransi tambahan
        }
        if(includePhotocard) {
            biayaDasar += 20000; // Packaging khusus
        }
        return biayaDasar;
    }

    public boolean isKonserSudahBerlangsung() {
        return tanggalKonser.isBefore(java.time.LocalDate.now());
    }

    // Getter dan Setter tambahan
    public String getGrupIdol() { return grupIdol; }
    public void setGrupIdol(String grupIdol) { this.grupIdol = grupIdol; }

    public String getMember() { return member; }
    public void setMember(String member) { this.member = member; }

    public String getJenisKonser() { return jenisKonser; }
    public void setJenisKonser(String jenisKonser) { this.jenisKonser = jenisKonser; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDate getTanggalKonser() { return tanggalKonser; }
    public void setTanggalKonser(LocalDate tanggalKonser) { this.tanggalKonser = tanggalKonser; }

    public boolean isIncludePhotocard() { return includePhotocard; }
    public void setIncludePhotocard(boolean includePhotocard) { this.includePhotocard = includePhotocard; }

    public int getJumlahMember() { return jumlahMember; }
    public void setJumlahMember(int jumlahMember) {
        if(jumlahMember > 0) this.jumlahMember = jumlahMember;
    }
}