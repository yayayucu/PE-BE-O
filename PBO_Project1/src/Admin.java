package sistemtitip;

public class Admin {
    private String username;
    private String password;
    private String nama;
    private String level; // Super Admin, Manager, Staff
    private String email;

    public Admin(String username, String password, String nama) {
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.level = "Super Admin";
        this.email = "";
    }

    // Getter dan Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Method untuk admin
    public String getInfoAdmin() {
        return String.format("""
            ===== INFO ADMIN =====
            Username: %s
            Nama: %s
            Level: %s
            Email: %s
            =====================
            """, username, nama, level, email);
    }

    public boolean hasPermission(String permission) {
        switch(level) {
            case "Super Admin":
                return true;
            case "Manager":
                return !permission.equals("DELETE_ADMIN");
            case "Staff":
                return permission.equals("VIEW_PRODUCT") ||
                        permission.equals("UPDATE_PRODUCT") ||
                        permission.equals("VIEW_ORDER") ||
                        permission.equals("UPDATE_ORDER_STATUS");
            default:
                return false;
        }
    }
}