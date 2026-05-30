package mx.uv.comedor.dao;

import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class MenuSemanalDAO {

    public Long obtenerOCrearMenuSemanaActual(Long idAdmin) throws SQLException {
        LocalDate hoy = LocalDate.now();
        WeekFields wf = WeekFields.of(new Locale("es","MX"));
        int semana = hoy.get(wf.weekOfWeekBasedYear());
        int anio   = hoy.getYear();

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_menu FROM menu_semanal WHERE semana = ? AND anio = ?")) {
                ps.setInt(1, semana);
                ps.setInt(2, anio);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getLong("id_menu");
            }

            LocalDate inicioSemana = hoy.with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemana    = inicioSemana.plusDays(5);

            Long idMenu;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO menu_semanal (id_admin, semana, anio, fecha_inicio, fecha_fin, activo) " +
                            "VALUES (?, ?, ?, ?, ?, true) RETURNING id_menu")) {
                ps.setLong(1, idAdmin);
                ps.setInt(2, semana);
                ps.setInt(3, anio);
                ps.setDate(4, Date.valueOf(inicioSemana));
                ps.setDate(5, Date.valueOf(finSemana));
                ResultSet rs = ps.executeQuery();
                rs.next();
                idMenu = rs.getLong("id_menu");
            }

            String[] dias = {"LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","SABADO"};
            for (String dia : dias) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO dia_menu (id_menu, dia_semana, disponible, " +
                                "cupo_total, cupo_apartado_becados, cupo_restante) " +
                                "VALUES (?, CAST(? AS dia_enum), true, 110, 0, 110)")) {
                    ps.setLong(1, idMenu);
                    ps.setString(2, dia);
                    ps.executeUpdate();
                }
            }
            return idMenu;
        }
    }

    /** Lista los platillos del dia con cupo y vendidos. */
    public List<Platillo> listarPlatillosDelDia(Long idMenu, DiaEnum dia, CatMenuEnum cat)
            throws SQLException {
        String sql =
                "SELECT p.id_platillo, p.nombre, p.descripcion, p.precio, " +
                        "       p.precio_subsidiado, p.imagen, p.disponible, p.tipo, " +
                        "       p.categoria, p.tiempo_prep, " +
                        "       dmp.cantidad AS cupo, dmp.vendidos " +
                        "FROM dia_menu_platillo dmp " +
                        "JOIN dia_menu dm ON dmp.id_dia_menu = dm.id_dia_menu " +
                        "JOIN platillo p  ON dmp.id_platillo = p.id_platillo " +
                        "WHERE dm.id_menu = ? " +
                        "AND dm.dia_semana = CAST(? AS dia_enum) " +
                        "AND dmp.categoria = CAST(? AS cat_menu_enum) " +
                        "ORDER BY p.nombre";

        List<Platillo> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idMenu);
            ps.setString(2, dia.name());
            ps.setString(3, cat.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Platillo p = new Platillo();
                p.setIdPlatillo(rs.getLong("id_platillo"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getBigDecimal("precio"));
                p.setPrecioSubsidiado(rs.getBigDecimal("precio_subsidiado"));
                p.setImagen(rs.getString("imagen"));
                p.setDisponible(rs.getBoolean("disponible"));
                p.setTipo(TipoPlatEnum.valueOf(rs.getString("tipo")));
                String catStr = rs.getString("categoria");
                if (catStr != null) {
                    try { p.setCategoria(CategoriaPlatEnum.valueOf(catStr)); }
                    catch (Exception e) { p.setCategoria(CategoriaPlatEnum.OTRO); }
                }
                p.setTiempoPrep(rs.getInt("tiempo_prep"));
                p.setCupo(rs.getInt("cupo"));
                p.setVendidos(rs.getInt("vendidos"));
                lista.add(p);
            }
        }
        return lista;
    }

    /** Agregar/actualizar platillo a un dia con su cupo. */
    public void agregarPlatilloADia(Long idMenu, DiaEnum dia, CatMenuEnum cat,
                                    Long idPlatillo, int cupo) throws SQLException {
        String sqlBuscarDia =
                "SELECT id_dia_menu FROM dia_menu " +
                        "WHERE id_menu = ? AND dia_semana = CAST(? AS dia_enum)";

        String sqlUpsert =
                "INSERT INTO dia_menu_platillo (id_dia_menu, id_platillo, categoria, cantidad) " +
                        "VALUES (?, ?, CAST(? AS cat_menu_enum), ?) " +
                        "ON CONFLICT (id_dia_menu, id_platillo, categoria) DO UPDATE " +
                        "SET cantidad = EXCLUDED.cantidad";

        try (Connection con = DBConnection.getConnection()) {
            Long idDia;
            try (PreparedStatement ps = con.prepareStatement(sqlBuscarDia)) {
                ps.setLong(1, idMenu);
                ps.setString(2, dia.name());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Dia no existe: " + dia);
                idDia = rs.getLong("id_dia_menu");
            }
            try (PreparedStatement ps = con.prepareStatement(sqlUpsert)) {
                ps.setLong(1, idDia);
                ps.setLong(2, idPlatillo);
                ps.setString(3, cat.name());
                ps.setInt(4, cupo);
                ps.executeUpdate();
            }
        }
    }

    public void quitarPlatilloDeDia(Long idMenu, DiaEnum dia, CatMenuEnum cat, Long idPlatillo)
            throws SQLException {
        String sql =
                "DELETE FROM dia_menu_platillo dmp " +
                        "USING dia_menu dm " +
                        "WHERE dmp.id_dia_menu = dm.id_dia_menu " +
                        "AND dm.id_menu = ? " +
                        "AND dm.dia_semana = CAST(? AS dia_enum) " +
                        "AND dmp.categoria = CAST(? AS cat_menu_enum) " +
                        "AND dmp.id_platillo = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idMenu);
            ps.setString(2, dia.name());
            ps.setString(3, cat.name());
            ps.setLong(4, idPlatillo);
            ps.executeUpdate();
        }
    }

    /**
     * Incrementa el contador de vendidos de un platillo en el dia/cat actual.
     * Si el nuevo total excede el cupo, lanza excepcion (rollback).
     * Retorna true si funciono.
     */
    public boolean incrementarVendidos(Long idPlatillo, DiaEnum dia,
                                       CatMenuEnum cat, int cantidad) throws SQLException {
        String sql =
                "UPDATE dia_menu_platillo dmp " +
                        "SET vendidos = vendidos + ? " +
                        "FROM dia_menu dm, menu_semanal ms " +
                        "WHERE dmp.id_dia_menu = dm.id_dia_menu " +
                        "AND dm.id_menu = ms.id_menu " +
                        "AND ms.activo = true " +
                        "AND CURRENT_DATE BETWEEN ms.fecha_inicio AND ms.fecha_fin " +
                        "AND dm.dia_semana = CAST(? AS dia_enum) " +
                        "AND dmp.categoria = CAST(? AS cat_menu_enum) " +
                        "AND dmp.id_platillo = ? " +
                        "AND dmp.vendidos + ? <= dmp.cantidad";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, dia.name());
            ps.setString(3, cat.name());
            ps.setLong(4, idPlatillo);
            ps.setInt(5, cantidad);
            return ps.executeUpdate() > 0;
        }
    }
}
