package sistemtitip;

import java.time.LocalDateTime;

public class UserSession {
    private String sessionId;
    private Object user;
    private String role; // "USER" atau "ADMIN"
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;

    public UserSession(String sessionId, Object user, String role) {
        this.sessionId = sessionId;
        this.user = user;
        this.role = role;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }

    // Getter
    public String getSessionId() { return sessionId; }
    public Object getUser() { return user; }
    public String getRole() { return role; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public LocalDateTime getLastActivity() { return lastActivity; }

    // Update activity
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    // Cek jika session expired (30 menit tidak aktif)
    public boolean isExpired() {
        return lastActivity.plusMinutes(30).isBefore(LocalDateTime.now());
    }

    // Helper methods untuk mendapatkan user tertentu
    public Pelanggan getPelanggan() {
        if ("USER".equals(role) && user instanceof Pelanggan) {
            return (Pelanggan) user;
        }
        return null;
    }

    public Admin getAdmin() {
        if ("ADMIN".equals(role) && user instanceof Admin) {
            return (Admin) user;
        }
        return null;
    }
}