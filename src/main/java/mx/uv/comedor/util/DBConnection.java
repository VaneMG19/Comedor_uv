package mx.uv.comedor.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
  Conexión a PostgreSQL.
  Lee las credenciales del archivo .env en la raíz del proyecto.
 */
public class DBConnection {

    // Una sola declaración de dotenv con la ruta exacta del proyecto
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("C:/Users/vmg19/IdeaProjects/comedor_universitario")
            .ignoreIfMissing()
            .load();

    private static final String HOST     = getEnv("DB_HOST",     "localhost");
    private static final String PORT     = getEnv("DB_PORT",     "5433");
    private static final String DATABASE = getEnv("DB_NAME",     "comedor_uv");
    private static final String USER     = getEnv("DB_USER",     "postgres");
    private static final String PASSWORD = getEnv("DB_PASSWORD", "");

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = dotenv.get(key, null);
        if (value != null && !value.isBlank()) return value;
        value = System.getenv(key);
        if (value != null && !value.isBlank()) return value;
        return defaultValue;
    }

    private DBConnection() {}
}