package mx.uv.comedor.dao;

import mx.uv.comedor.model.BecadoAutorizado;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
  DAO para becados_autorizados.
 */
public class BecadoAutorizadoDAO {

    /*
      Busca un becado autorizado por email.
      Si retorna != null y estaVigente() == true, el alumno puede registrarse como becado.
     */
    public BecadoAutorizado buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM becados_autorizados WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
            return null;
        }
    }

    /*
      Marca un becado autorizado como REGISTRADO cuando el alumno completa su registro.
     */
    public boolean marcarRegistrado(Long idBecadoAut) throws SQLException {
        String sql = "UPDATE becados_autorizados SET estado = 'REGISTRADO' WHERE id_becado_aut = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idBecadoAut);
            return ps.executeUpdate() > 0;
        }
    }

    /*
      Inserta un nuevo becado autorizado (lo carga el admin).
     */
    public Long insertar(BecadoAutorizado b) throws SQLException {
        String sql = "INSERT INTO becados_autorizados " +
            "(email, matricula, nombre_completo, tipo_beca, comidas_semana, " +
            " vigencia_desde, vigencia_hasta, notas) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_becado_aut";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getEmail());
            ps.setString(2, b.getMatricula());
            ps.setString(3, b.getNombreCompleto());
            ps.setString(4, b.getTipoBeca());
            ps.setInt(5, b.getComidasSemana());
            ps.setObject(6, b.getVigenciaDesde());
            ps.setObject(7, b.getVigenciaHasta());
            ps.setString(8, b.getNotas());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_becado_aut");
            throw new SQLException("No se obtuvo ID al insertar becado autorizado");
        }
    }

    public List<BecadoAutorizado> listarTodos() throws SQLException {
        String sql = "SELECT * FROM becados_autorizados ORDER BY fecha_carga DESC";
        List<BecadoAutorizado> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private BecadoAutorizado mapear(ResultSet rs) throws SQLException {
        BecadoAutorizado b = new BecadoAutorizado();
        b.setIdBecadoAut(rs.getLong("id_becado_aut"));
        b.setEmail(rs.getString("email"));
        b.setMatricula(rs.getString("matricula"));
        b.setNombreCompleto(rs.getString("nombre_completo"));
        b.setTipoBeca(rs.getString("tipo_beca"));
        b.setComidasSemana(rs.getInt("comidas_semana"));
        if (rs.getObject("vigencia_desde") != null)
            b.setVigenciaDesde(rs.getDate("vigencia_desde").toLocalDate());
        if (rs.getObject("vigencia_hasta") != null)
            b.setVigenciaHasta(rs.getDate("vigencia_hasta").toLocalDate());
        b.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fecha_carga") != null)
            b.setFechaCarga(rs.getTimestamp("fecha_carga").toLocalDateTime());
        b.setNotas(rs.getString("notas"));
        return b;
    }
}
