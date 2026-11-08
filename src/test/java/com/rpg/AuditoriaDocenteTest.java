package com.rpg;

import com.rpg.controller.LoginController;
import com.rpg.controller.MonstruoController;
import com.rpg.controller.RankingController;
import com.rpg.dao.ConexionDB;
import com.rpg.dao.JugadorDAO;
import com.rpg.dao.MonstruoDAO;
import com.rpg.dao.VictoriaDAO;
import com.rpg.demo.LoginInseguroDemo;
import com.rpg.model.Jugador;
import com.rpg.model.Monstruo;
import com.rpg.model.RankingItem;
import com.rpg.model.Victoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuditoriaDocenteTest {

    private static JugadorDAO jugadorDAO;
    private static MonstruoDAO monstruoDAO;
    private static VictoriaDAO victoriaDAO;
    private static LoginController loginController;
    private static MonstruoController monstruoController;
    private static RankingController rankingController;

    @BeforeAll
    public static void setUp() throws Exception {
        // Configuramos H2 en memoria con compatibilidad MySQL para ejecutar las pruebas sin requerir servidor MySQL activo
        String h2Url = "jdbc:h2:mem:rpg_arena_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        ConexionDB.setUrlOverride(h2Url);

        try (Connection conn = ConexionDB.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            // Crear esquema exacto requerido
            stmt.execute("CREATE TABLE jugadores (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "usuario VARCHAR(50) NOT NULL UNIQUE, " +
                    "clave VARCHAR(255) NOT NULL, " +
                    "rol VARCHAR(20) NOT NULL DEFAULT 'jugador')");

            stmt.execute("CREATE TABLE monstruos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(50) NOT NULL UNIQUE, " +
                    "vida INT NOT NULL, " +
                    "ataque INT NOT NULL, " +
                    "defensa INT NOT NULL DEFAULT 5)");

            stmt.execute("CREATE TABLE victorias (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "jugador_id INT NOT NULL, " +
                    "monstruo_id INT NOT NULL, " +
                    "fecha TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (jugador_id) REFERENCES jugadores(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (monstruo_id) REFERENCES monstruos(id) ON DELETE RESTRICT)");

            // Semillas
            stmt.execute("INSERT INTO jugadores (usuario, clave, rol) VALUES ('admin', 'admin123', 'admin')");
            stmt.execute("INSERT INTO jugadores (usuario, clave, rol) VALUES ('Thor', 'clave123', 'jugador')");
            stmt.execute("INSERT INTO monstruos (nombre, vida, ataque, defensa) VALUES ('Goblin', 50, 8, 2)");
            stmt.execute("INSERT INTO monstruos (nombre, vida, ataque, defensa) VALUES ('Dragon', 160, 20, 10)");
        }

        jugadorDAO = new JugadorDAO();
        monstruoDAO = new MonstruoDAO();
        victoriaDAO = new VictoriaDAO();

        loginController = new LoginController(jugadorDAO);
        monstruoController = new MonstruoController(monstruoDAO, victoriaDAO);
        rankingController = new RankingController(victoriaDAO);
    }

    @AfterAll
    public static void tearDown() {
        ConexionDB.resetConfiguracion();
    }

    @Test
    @Order(1)
    public void testDemostracionInyeccionSqlInsegura() {
        LoginInseguroDemo demo = new LoginInseguroDemo();
        // Con concatenacion y Statement, el bypass tiene exito
        boolean entraSinClave = demo.autenticarInseguro("admin' OR 1=1 -- ", "clave_falsa");
        assertTrue(entraSinClave, "La inyeccion SQL debe tener exito en LoginInseguroDemo");
    }

    @Test
    @Order(2)
    public void testDefensaInyeccionSqlConPreparedStatement() {
        // Con PreparedStatement en JugadorDAO, la misma entrada DEBE FALLAR
        Jugador resultado = jugadorDAO.autenticar("admin' OR 1=1 -- ", "clave_falsa");
        assertNull(resultado, "PreparedStatement debe neutralizar el payload y fallar la autenticacion");
    }

    @Test
    @Order(3)
    public void testMaximoTresIntentosLoginYBloqueo() {
        LoginController controller = new LoginController(jugadorDAO);
        assertFalse(controller.isSesionBloqueada());

        // 1er intento fallido
        controller.iniciarSesion("admin", "clave_erronea_1");
        assertEquals(1, controller.getIntentosFallidos());
        assertFalse(controller.isSesionBloqueada());

        // 2do intento fallido
        controller.iniciarSesion("admin", "clave_erronea_2");
        assertEquals(2, controller.getIntentosFallidos());
        assertFalse(controller.isSesionBloqueada());

        // 3er intento fallido -> BLOQUEO
        controller.iniciarSesion("admin", "clave_erronea_3");
        assertEquals(3, controller.getIntentosFallidos());
        assertTrue(controller.isSesionBloqueada(), "Al 3er intento fallido la sesion debe quedar bloqueada");

        // Intentos subsiguientes bloqueados inmediatamente
        Jugador bloqueo = controller.iniciarSesion("admin", "admin123");
        assertNull(bloqueo, "Con la sesion bloqueada no debe permitir ingresar incluso con clave correcta");
    }

    @Test
    @Order(4)
    public void testReglaAdminGestionMonstruos() {
        Jugador normal = new Jugador(2, "Thor", "clave123", Jugador.ROL_JUGADOR);
        Jugador admin = new Jugador(1, "admin", "admin123", Jugador.ROL_ADMIN);

        // Usuario normal no puede crear monstruos
        boolean creoNormal = monstruoController.crearMonstruo(normal, "Ogro", 100, 15, 5);
        assertFalse(creoNormal, "Un jugador normal no debe tener permisos para crear monstruos");

        // Admin si puede crear monstruos
        boolean creoAdmin = monstruoController.crearMonstruo(admin, "Ogro", 100, 15, 5);
        assertTrue(creoAdmin, "El admin debe poder crear monstruos");
        assertNotNull(monstruoDAO.buscarPorNombre("Ogro"));
    }

    @Test
    @Order(5)
    public void testReglaIntegridadReferencialNoEliminarConVictorias() {
        Jugador admin = new Jugador(1, "admin", "admin123", Jugador.ROL_ADMIN);
        Monstruo goblin = monstruoDAO.buscarPorNombre("Goblin");
        assertNotNull(goblin);

        // Registrar una victoria sobre Goblin
        boolean regVictoria = victoriaDAO.registrar(1, goblin.getId());
        assertTrue(regVictoria);

        // El Controller debe detectar victorias y rehusar eliminar sin que reviente la FK
        boolean eliminado = monstruoController.eliminarMonstruo(admin, goblin.getId());
        assertFalse(eliminado, "No se debe permitir eliminar un monstruo que posea victorias registradas");
        assertNotNull(monstruoDAO.obtenerPorId(goblin.getId()), "El monstruo debe permanecer en la base de datos");
    }

    @Test
    @Order(6)
    public void testRankingConJoinYGroupBy() {
        List<RankingItem> ranking = victoriaDAO.rankingPorJugador();
        assertNotNull(ranking);
        assertFalse(ranking.isEmpty(), "El ranking debe retornar al menos un elemento");
        assertEquals("admin", ranking.get(0).getUsuario());
        assertTrue(ranking.get(0).getTotalVictorias() >= 1);
    }

    @Test
    @Order(7)
    public void testBenchmarkMilInserciones() throws SQLException {
        // Test requerido para INFORME.md: 1000 inserciones con Statement vs PreparedStatement
        try (Connection conn = ConexionDB.obtenerConexion();
             Statement stmtInit = conn.createStatement()) {
            stmtInit.execute("CREATE TABLE IF NOT EXISTS bench_stmt (id INT AUTO_INCREMENT PRIMARY KEY, val VARCHAR(50))");
            stmtInit.execute("CREATE TABLE IF NOT EXISTS bench_pstmt (id INT AUTO_INCREMENT PRIMARY KEY, val VARCHAR(50))");
        }

        int total = 1000;

        // 1. Benchmark Statement
        long inicioStmt = System.currentTimeMillis();
        try (Connection conn = ConexionDB.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            for (int i = 0; i < total; i++) {
                stmt.executeUpdate("INSERT INTO bench_stmt (val) VALUES ('item_" + i + "')");
            }
        }
        long duracionStmt = System.currentTimeMillis() - inicioStmt;

        // 2. Benchmark PreparedStatement
        long inicioPstmt = System.currentTimeMillis();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO bench_pstmt (val) VALUES (?)")) {
            for (int i = 0; i < total; i++) {
                pstmt.setString(1, "item_" + i);
                pstmt.executeUpdate();
            }
        }
        long duracionPstmt = System.currentTimeMillis() - inicioPstmt;

        System.out.println("=================================================");
        System.out.printf("BENCHMARK 1000 INSERCIONES: Statement = %d ms | PreparedStatement = %d ms%n",
                duracionStmt, duracionPstmt);
        System.out.println("=================================================");

        assertTrue(duracionStmt >= 0 && duracionPstmt >= 0);
    }
}
