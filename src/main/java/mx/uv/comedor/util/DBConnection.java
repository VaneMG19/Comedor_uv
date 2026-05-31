package mx.uv.comedor.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URI;

/*
  Conexion a PostgreSQL.
  Funciona tanto en local (lee .env) como en Railway (lee variables de entorno).
 */
public class DBConnection {

    // Cargar el .env del proyecto local. En Railway no existe y se ignora.
    private static final Dotenv dotenv;
    static {
        Dotenv tmp;
        try {
            tmp = Dotenv.configure()
                    .directory("C:/Users/vmg19/IdeaProjects/comedor_universitario")
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();
        } catch (Exception e) {
            tmp = null;
        }
        dotenv = tmp;
    }

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Prioridad 1: DATABASE_URL completa (Railway/Heroku la dan asi)
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            try {
                if (databaseUrl.startsWith("postgres://")) {
                    databaseUrl = databaseUrl.replaceFirst("postgres://", "postgresql://");
                }
                URI uri = new URI(databaseUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    USER = parts[0];
                    PASSWORD = parts[1];
                } else {
                    USER = userInfo;
                    PASSWORD = "";
                }
                URL = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
            } catch (Exception e) {
                throw new RuntimeException("DATABASE_URL invalida: " + e.getMessage(), e);
            }
        } else {
            // Prioridad 2: variables separadas (modo local con .env)
            String host = getEnv("DB_HOST", "localhost");
            String port = getEnv("DB_PORT", "5433");
            String db   = getEnv("DB_NAME", "comedor_uv");
            URL = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            USER = getEnv("DB_USER", "postgres");
            PASSWORD = getEnv("DB_PASSWORD", "");
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado", e);
        }

        System.out.println("[DBConnection] Conectando a: " + URL);
        System.out.println("[DBConnection] Usuario: " + USER);
        // No imprimir password por seguridad
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String getEnv(String key, String defaultValue) {
        // Primero variables del sistema (Railway)
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) return value;
        // Luego .env (local)
        if (dotenv != null) {
            value = dotenv.get(key, null);
            if (value != null && !value.isBlank()) return value;
        }
        return defaultValue;
    }

    private DBConnection() {}
}