package sistemtitip;

import java.util.ArrayList;
import java.util.List;

public class Pelanggan {
    private String idPelanggan;
    private String username;
    private String password;
    private String nama;
    private String email;
    private String noTelepon;
    private String alamat;
    private int poinLoyalty;
    private String levelMembership;
    private List<String> riwayatPemesanan;
    private double totalBelanja;

    public Pelanggan(String idPelanggan, String nama, String email,
                     String noTelepon, String alamat) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.email = email;
        this.noTelepon = noTelepon;
        this.alamat = alamat;
        this.username = "";
        this.password = "";
        this.poinLoyalty = 0;
        this.levelMembership = "Regular";
        this.riwayatPemesanan = new ArrayList<>();
        this.totalBelanja = 0;
    }

    // Getter dan Setter baru untuk username & password
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Getter lainnya tetap sama...
    public String getIdPelanggan() { return idPelanggan; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if(email.contains("@")) this.email = email;
    }

    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) {
        if(noTelepon.matches("\\d+")) this.noTelepon = noTelepon;
    }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public int getPoinLoyalty() { return poinLoyalty; }

    public String getLevelMembership() { return levelMembership; }

    public List<String> getRiwayatPemesanan() {
        return new ArrayList<>(riwayatPemesanan);
    }

    public double getTotalBelanja() { return totalBelanja; }

    // Method bisnis tetap sama...
    public void tambahPoin(int poin) {
        if(poin > 0) {
            this.poinLoyalty += poin;
            updateLevelMembership();
        }
    }

    public void tambahBelanja(double jumlah) {
        if(jumlah > 0) {
            this.totalBelanja += jumlah;
            tambahPoin((int)(jumlah / 10000));
        }
    }

    public void tambahRiwayatPemesanan(String pesananId) {
        riwayatPemesanan.add(pesananId);
    }

    private void updateLevelMembership() {
        if(totalBelanja >= 5000000) {
            levelMembership = "Platinum";
        } else if(totalBelanja >= 2000000) {
            levelMembership = "Gold";
        } else if(totalBelanja >= 500000) {
            levelMembership = "Silver";
        }
    }

    public double hitungDiskon() {
        switch(levelMembership) {
            case "Silver": return 0.05;
            case "Gold": return 0.10;
            case "Platinum": return 0.15;
            default: return 0.0;
        }
    }

    public String getInfoPelanggan() {
        return String.format("""
            ===== INFO PELANGGAN =====
            ID: %s
            Username: %s
            Nama: %s
            Email: %s
            Telepon: %s
            Alamat: %s
            Level Membership: %s
            Poin Loyalty: %d
            Total Belanja: Rp %,.0f
            Diskon: %.0f%%
            Jumlah Pesanan: %d
            ==========================
            """, idPelanggan, username, nama, email, noTelepon, alamat,
                levelMembership, poinLoyalty, totalBelanja,
                hitungDiskon() * 100, riwayatPemesanan.size());
    }
}