package mx.uv.comedor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilidad para obtener conexiones a PostgreSQL.
 * ─────────────────────────────────────────────
 * CAMBIA los valores de PORT y PASSWORD según tu instalación.
 * Los demás valores normalmente no necesitan cambio.
 */
public class DBConnection {

    // ── Cambia estos dos valores ──────────────────────────────────
    private static final String PORT     = "5433";         // 5432 o 5433
    private static final String PASSWORD = "1234";  // tu contraseña de PostgreSQL
    // ─────────────────────────────────────────────────────────────

    private static final String HOST     = "localhost";
    private static final String DATABASE = "comedor_uv";
    private static final String USER     = "postgres";

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Driver PostgreSQL no encontrado. " +
                            "Verifica que el pom.xml tiene la dependencia de postgresql.", e);
        }
    }

    /**
     * Retorna una nueva conexión a la base de datos.
     * Úsala siempre dentro de un try-with-resources para
     * que se cierre automáticamente:
     *
     *   try (Connection con = DBConnection.getConnection()) {
     *       ...
     *   }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private DBConnection() {} // No instanciar
}