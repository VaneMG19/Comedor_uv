package mx.uv.comedor.dao;

import mx.uv.comedor.model.EmpleadoCocina;
import mx.uv.comedor.model.TurnoEnum;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadoCocinaDAO {

    public Long insertar(EmpleadoCocina e) throws SQLException {
        String sql = "INSERT INTO empleado_cocina (id_usuario, num_empleado, turno, puesto) " +
                "VALUES (?, ?, CAST(? AS turno_enum), ?) RETURNING id_empleado";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, e.getIdUsuario());
            ps.setString(2, e.getNumEmpleado());
            ps.setString(3, e.getTurno().name());
            ps.setString(4, e.getPuesto());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_empleado");
            throw new SQLException("No se obtuvo ID al insertar empleado");
        }
    }

    public EmpleadoCocina buscarPorIdUsuario(Long idUsuario) throws SQLException {
        String sql = "SELECT id_empleado, id_usuario, num_empleado, turno, puesto " +
                "FROM empleado_cocina WHERE id_usuario = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EmpleadoCocina e = new EmpleadoCocina();
                e.setIdEmpleado(rs.getLong("id_empleado"));
                e.setIdUsuario(rs.getLong("id_usuario"));
                e.setNumEmpleado(rs.getString("num_empleado"));
                e.setTurno(TurnoEnum.valueOf(rs.getString("turno")));
                e.setPuesto(rs.getString("puesto"));
                return e;
            }
            return null;
        }
    }
}