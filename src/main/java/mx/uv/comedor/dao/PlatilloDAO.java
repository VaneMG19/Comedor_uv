package mx.uv.comedor.dao;

import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'platillo' e 'informacion_nutricional'.
 */
public class PlatilloDAO {

    // ── CREATE ────────────────────────────────────────────────────

    public Long insertar(Platillo p) throws SQLException {
        String sql = "INSERT INTO platillo " +
                "(nombre, descripcion, precio, precio_subsidiado, " +
                " imagen, disponible, tipo, tiempo_prep) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS tipo_plat_enum), ?) " +
                "RETURNING id_platillo";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setBigDecimal(4, p.getPrecioSubsidiado());
            ps.setString(5, p.getImagen());
            ps.setBoolean(6, p.isDisponible());
            ps.setString(7, p.getTipo().name());
            ps.setInt(8, p.getTiempoPrep());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_platillo");
            throw new SQLException("No se obtuvo ID al insertar platillo");
        }
    }

    public void insertarNutricional(InformacionNutricional n) throws SQLException {
        String sql = "INSERT INTO informacion_nutricional " +
                "(id_platillo, calorias, proteinas, carbohidratos, grasas, " +
                " fibra, sodio, azucar, alergenos, " +
                " es_vegetariano, es_vegano, es_gluten_free, huella_carbono_kg) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, n.getIdPlatillo());
            ps.setBigDecimal(2, n.getCalorias());
            ps.setBigDecimal(3, n.getProteinas());
            ps.setBigDecimal(4, n.getCarbohidratos());
            ps.setBigDecimal(5, n.getGrasas());
            ps.setBigDecimal(6, n.getFibra());
            ps.setBigDecimal(7, n.getSodio());
            ps.setBigDecimal(8, n.getAzucar());
            ps.setString(9, n.getAlergenos());
            ps.setBoolean(10, n.isEsVegetariano());
            ps.setBoolean(11, n.isEsVegano());
            ps.setBoolean(12, n.isEsGlutenFree());
            ps.setBigDecimal(13, n.getHuellaCarbonoKg());
            ps.executeUpdate();
        }
    }

    // ── READ ──────────────────────────────────────────────────────

    public Platillo buscarPorId(Long idPlatillo) throws SQLException {
        String sql = "SELECT p.*, " +
                "n.id_nutricional, n.calorias, n.proteinas, n.carbohidratos, " +
                "n.grasas, n.fibra, n.sodio, n.azucar, n.alergenos, " +
                "n.es_vegetariano, n.es_vegano, n.es_gluten_free, " +
                "n.huella_carbono_kg, n.nivel_huella " +
                "FROM platillo p " +
                "LEFT JOIN informacion_nutricional n ON p.id_platillo = n.id_platillo " +
                "WHERE p.id_platillo = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPlatillo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
            return null;
        }
    }

    public List<Platillo> listarPorMenuActivoYCategoria(CatMenuEnum categoria)
            throws SQLException {

        // Calcular el día actual en español para el ENUM de PostgreSQL
        String[] dias = {"LUNES","MARTES","MIERCOLES","JUEVES","VIERNES","LUNES","LUNES"};
        int dow = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1=Lun, 7=Dom
        String diaHoy = dow <= 5 ? dias[dow - 1] : "LUNES";

        String sql = "SELECT p.id_platillo, p.nombre, p.descripcion, p.precio, " +
                "p.precio_subsidiado, p.imagen, p.disponible, p.tipo, p.tiempo_prep, " +
                "n.id_nutricional, n.calorias, n.proteinas, n.carbohidratos, n.grasas, " +
                "n.fibra, n.sodio, n.azucar, n.alergenos, n.es_vegetariano, n.es_vegano, " +
                "n.es_gluten_free, n.huella_carbono_kg, n.nivel_huella " +
                "FROM vista_menu_del_dia v " +
                "JOIN platillo p ON v.id_platillo = p.id_platillo " +
                "LEFT JOIN informacion_nutricional n ON p.id_platillo = n.id_platillo " +
                "WHERE v.categoria = CAST(? AS cat_menu_enum) " +
                "AND v.dia_semana  = CAST(? AS dia_enum)";

        List<Platillo> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.name());
            ps.setString(2, diaHoy);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Platillo> listarCarta() throws SQLException {
        String sql = "SELECT p.id_platillo, p.nombre, p.descripcion, p.precio, " +
                "p.precio_subsidiado, p.imagen, p.disponible, p.tipo, p.tiempo_prep, " +
                "n.id_nutricional, n.calorias, n.proteinas, n.carbohidratos, n.grasas, " +
                "n.fibra, n.sodio, n.azucar, n.alergenos, n.es_vegetariano, n.es_vegano, " +
                "n.es_gluten_free, n.huella_carbono_kg, n.nivel_huella " +
                "FROM platillo p " +
                "LEFT JOIN informacion_nutricional n ON p.id_platillo = n.id_platillo " +
                "WHERE p.tipo = 'CARTA' AND p.disponible = TRUE " +
                "ORDER BY p.nombre";

        List<Platillo> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Platillo> listarTodos() throws SQLException {
        String sql = "SELECT p.id_platillo, p.nombre, p.descripcion, p.precio, " +
                "p.precio_subsidiado, p.imagen, p.disponible, p.tipo, p.tiempo_prep, " +
                "n.id_nutricional, n.calorias, n.proteinas, n.carbohidratos, n.grasas, " +
                "n.fibra, n.sodio, n.azucar, n.alergenos, n.es_vegetariano, n.es_vegano, " +
                "n.es_gluten_free, n.huella_carbono_kg, n.nivel_huella " +
                "FROM platillo p " +
                "LEFT JOIN informacion_nutricional n ON p.id_platillo = n.id_platillo " +
                "ORDER BY p.tipo, p.nombre";

        List<Platillo> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── UPDATE ────────────────────────────────────────────────────

    public boolean actualizar(Platillo p) throws SQLException {
        String sql = "UPDATE platillo " +
                "SET nombre = ?, descripcion = ?, precio = ?, " +
                "precio_subsidiado = ?, imagen = ?, disponible = ?, tiempo_prep = ? " +
                "WHERE id_platillo = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setBigDecimal(4, p.getPrecioSubsidiado());
            ps.setString(5, p.getImagen());
            ps.setBoolean(6, p.isDisponible());
            ps.setInt(7, p.getTiempoPrep());
            ps.setLong(8, p.getIdPlatillo());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean toggleDisponibilidad(Long idPlatillo) throws SQLException {
        String sql = "UPDATE platillo SET disponible = NOT disponible " +
                "WHERE id_platillo = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPlatillo);
            return ps.executeUpdate() > 0;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────

    private Platillo mapear(ResultSet rs) throws SQLException {
        Platillo p = new Platillo();
        p.setIdPlatillo(rs.getLong("id_platillo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setPrecioSubsidiado(rs.getBigDecimal("precio_subsidiado"));
        p.setImagen(rs.getString("imagen"));
        p.setDisponible(rs.getBoolean("disponible"));
        p.setTipo(TipoPlatEnum.valueOf(rs.getString("tipo")));
        p.setTiempoPrep(rs.getInt("tiempo_prep"));

        if (rs.getObject("id_nutricional") != null) {
            InformacionNutricional n = new InformacionNutricional();
            n.setIdNutricional(rs.getLong("id_nutricional"));
            n.setIdPlatillo(p.getIdPlatillo());
            n.setCalorias(rs.getBigDecimal("calorias"));
            n.setProteinas(rs.getBigDecimal("proteinas"));
            n.setCarbohidratos(rs.getBigDecimal("carbohidratos"));
            n.setGrasas(rs.getBigDecimal("grasas"));
            n.setFibra(rs.getBigDecimal("fibra"));
            n.setSodio(rs.getBigDecimal("sodio"));
            n.setAzucar(rs.getBigDecimal("azucar"));
            n.setAlergenos(rs.getString("alergenos"));
            n.setEsVegetariano(rs.getBoolean("es_vegetariano"));
            n.setEsVegano(rs.getBoolean("es_vegano"));
            n.setEsGlutenFree(rs.getBoolean("es_gluten_free"));
            n.setHuellaCarbonoKg(rs.getBigDecimal("huella_carbono_kg"));
            String nh = rs.getString("nivel_huella");
            if (nh != null) n.setNivelHuella(NivelHuellaEnum.valueOf(nh));
            p.setInformacionNutricional(n);
        }
        return p;
    }
}