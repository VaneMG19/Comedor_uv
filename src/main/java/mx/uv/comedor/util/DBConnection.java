package mx.uv.comedor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String PORT     = "5433";         //
    private static final String PASSWORD = "1234";  //
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


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private DBConnection() {} // No instanciar
}