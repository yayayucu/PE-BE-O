package sistemtitip;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AuthService {
    private static AuthService instance;
    private Database db;

    private AuthService() {
        db = Database.getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // Hash password dengan SHA-256
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Register pelanggan baru
    public UserSession register(String username, String password, String nama,
                                String email, String noTelepon, String alamat) {
        // Validasi input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter");
        }
        if (db.isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }

        // Generate ID pelanggan
        String idPelanggan = "CUST" + String.format("%03d", db.getAllPelanggan().size() + 1);

        // Hash password
        String hashedPassword = hashPassword(password);

        // Buat objek pelanggan
        Pelanggan pelanggan = new Pelanggan(idPelanggan, nama, email, noTelepon, alamat);
        pelanggan.setUsername(username);
        pelanggan.setPassword(hashedPassword);

        // Simpan ke database
        db.addPelanggan(pelanggan);

        // Buat session
        return createSession(pelanggan, "USER");
    }

    // Login untuk user/admin
    public UserSession login(String username, String password) {
        String hashedPassword = hashPassword(password);

        // Cek sebagai admin terlebih dahulu
        Admin admin = db.getAdminByUsername(username);
        if (admin != null && admin.getPassword().equals(hashedPassword)) {
            return createSession(admin, "ADMIN");
        }

        // Cek sebagai pelanggan
        Pelanggan pelanggan = db.getPelangganByUsername(username);
        if (pelanggan != null && pelanggan.getPassword().equals(hashedPassword)) {
            return createSession(pelanggan, "USER");
        }

        throw new IllegalArgumentException("Username atau password salah");
    }

    // Buat session baru
    private UserSession createSession(Object user, String role) {
        String sessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession(sessionId, user, role);
        db.addSession(sessionId, session);
        return session;
    }

    // Logout
    public void logout(String sessionId) {
        db.removeSession(sessionId);
    }

    // Validasi session
    public boolean isValidSession(String sessionId) {
        return db.getSession(sessionId) != null;
    }

    public UserSession getSession(String sessionId) {
        return db.getSession(sessionId);
    }
}