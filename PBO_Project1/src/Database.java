package sistemtitip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static Database instance;
    private List<Produk> produkList;
    private List<Pelanggan> pelangganList;
    private List<Pemesanan> pemesananList;
    private List<Admin> adminList;
    private Map<String, UserSession> activeSessions;

    private Database() {
        produkList = new ArrayList<>();
        pelangganList = new ArrayList<>();
        pemesananList = new ArrayList<>();
        adminList = new ArrayList<>();
        activeSessions = new HashMap<>();
        initData();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void initData() {
        // Inisialisasi admin default
        adminList.add(new Admin("admin", "admin123", "Super Admin"));

        // Inisialisasi data merch anime
        produkList.add(new MerchAnime(
                "ANM001", "Figure Goku Super Saiyan", 500000, 750000, 10,
                "Figure", java.time.LocalDate.of(2024, 1, 15), true,
                "Dragon Ball Z", "Son Goku", "Action Figure",
                "Bandai", true, 8.7
        ));

        produkList.add(new MerchAnime(
                "ANM002", "Poster Attack on Titan", 50000, 85000, 50,
                "Poster", java.time.LocalDate.of(2024, 2, 20), false,
                "Attack on Titan", "Eren Yeager", "Poster",
                "WIT Studio", true, 9.0
        ));

        // Inisialisasi data merch J-pop
        produkList.add(new MerchJpop(
                "JPP001", "Official Light Stick", 800000, 1200000, 5,
                "Konser Merch", java.time.LocalDate.of(2024, 3, 10), true,
                "Nogizaka46", "Mai Shiraishi", "Tour Final",
                "Tokyo Dome", java.time.LocalDate.of(2024, 4, 15), true, 46
        ));

        produkList.add(new MerchJpop(
                "JPP002", "Photocard Set", 150000, 250000, 20,
                "Collectible", java.time.LocalDate.of(2024, 1, 5), false,
                "Arashi", "All Members", "20th Anniversary",
                "Nippon Budokan", java.time.LocalDate.of(2023, 12, 25), true, 5
        ));
    }

    // CRUD untuk Produk
    public void addProduk(Produk produk) {
        produkList.add(produk);
    }

    public List<Produk> getAllProduk() {
        return new ArrayList<>(produkList);
    }

    public Produk getProdukByKode(String kode) {
        return produkList.stream()
                .filter(p -> p.getKodeProduk().equals(kode))
                .findFirst()
                .orElse(null);
    }

    public boolean updateProduk(Produk updatedProduk) {
        for (int i = 0; i < produkList.size(); i++) {
            if (produkList.get(i).getKodeProduk().equals(updatedProduk.getKodeProduk())) {
                produkList.set(i, updatedProduk);
                return true;
            }
        }
        return false;
    }

    // CRUD untuk Pelanggan
    public void addPelanggan(Pelanggan pelanggan) {
        pelangganList.add(pelanggan);
    }

    public Pelanggan getPelangganByUsername(String username) {
        return pelangganList.stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public List<Pelanggan> getAllPelanggan() {
        return new ArrayList<>(pelangganList);
    }

    // CRUD untuk Pemesanan
    public void addPemesanan(Pemesanan pemesanan) {
        pemesananList.add(pemesanan);
    }

    public List<Pemesanan> getAllPemesanan() {
        return new ArrayList<>(pemesananList);
    }

    public Pemesanan getPemesananById(String id) {
        return pemesananList.stream()
                .filter(p -> p.getIdPesanan().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Pemesanan> getPemesananByPelanggan(String pelangganId) {
        List<Pemesanan> result = new ArrayList<>();
        for (Pemesanan p : pemesananList) {
            if (p.getPelanggan().getIdPelanggan().equals(pelangganId)) {
                result.add(p);
            }
        }
        return result;
    }

    // CRUD untuk Admin
    public Admin getAdminByUsername(String username) {
        return adminList.stream()
                .filter(a -> a.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // Session Management
    public void addSession(String sessionId, UserSession session) {
        activeSessions.put(sessionId, session);
    }

    public UserSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    // Utility methods
    public boolean isUsernameTaken(String username) {
        // Cek di pelanggan
        boolean takenByPelanggan = pelangganList.stream()
                .anyMatch(p -> p.getUsername().equals(username));

        // Cek di admin
        boolean takenByAdmin = adminList.stream()
                .anyMatch(a -> a.getUsername().equals(username));

        return takenByPelanggan || takenByAdmin;
    }
}