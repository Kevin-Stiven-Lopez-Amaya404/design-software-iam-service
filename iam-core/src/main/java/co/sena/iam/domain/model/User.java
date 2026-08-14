package co.sena.iam.domain.model;

/** Entidad de dominio pura (sin framework/ORM). */
public final class User {
    private final String id;
    private final String email;
    private final boolean active;

    public User(String id, String email, boolean active) {
        this.id = id;
        this.email = email;
        this.active = active;
    }

    public String id() { return id; }
    public String email() { return email; }
    public boolean isActive() { return active; }
}
