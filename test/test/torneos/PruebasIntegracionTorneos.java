package test.torneos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import modelo.*;
import persistencia.PersistenciaSistema;

import java.io.File;

public class PruebasIntegracionTorneos
{
    private SistemasDulcesDados sistema;
    private Administrador admin;
    private JuegoMesa juego;
    private String rutaPruebas = "datos_prueba_torneos_test";

    @BeforeEach
    public void setUp()
    {
        PersistenciaSistema persistencia = new PersistenciaSistema(rutaPruebas);
        Cafe cafe = new Cafe(50);
        sistema = new SistemasDulcesDados(cafe, persistencia);

        // Crear admin y simular sesion iniciada
        admin = new Administrador("111", "Admin Test", "admin@test.com", "admin", "admin123");
        sistema.agregarUsuario(admin);
        sistema.setSesionActual(admin);

        // Crear juego disponible en el cafe
        juego = new JuegoMesa("Ajedrez", 2, "Clasico", 2, 4, 1, false, CategoriaJuego.TABLERO);
        for (int i = 0; i < 15; i++)
        {
            CopiaJuegoPrestamo copia = new CopiaJuegoPrestamo(EstadoJuego.NUEVO, true, juego);
            juego.agregarCopia(copia);
        }
        sistema.getCafe().agregarJuegoCatalogo(juego);
    }

    @AfterEach
    public void tearDown()
    {
        // Limpiar archivos de prueba generados
        File carpeta = new File(rutaPruebas);
        if (carpeta.exists())
        {
            for (File f : carpeta.listFiles()) f.delete();
            carpeta.delete();
        }
    }

    // ── HU-1: Administrador crea torneo amistoso ──────────────────────────

    @Test
    public void testCrearTorneoAmistoso()
    {
        Torneo torneo = sistema.crearTorneoAmistoso("Chess Classic", DiaSemana.MIERCOLES, juego, 8, 20000);

        assertNotNull(torneo, "El torneo amistoso debe crearse correctamente");
        assertEquals("Chess Classic", torneo.getNombre());
        assertEquals(8, torneo.getCuposTotales());
        assertTrue(torneo instanceof TorneoAmistoso);
    }

    @Test
    public void testCrearTorneoAmistosoSinAdminFalla()
    {
        // Cliente intentando crear torneo — debe fallar
        Cliente cliente = new Cliente("222", "Juan", "juan@test.com", "juan01", "pass123");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);

        Torneo torneo = sistema.crearTorneoAmistoso("Torneo Ilegal", DiaSemana.LUNES, juego, 4, 10000);

        assertNull(torneo, "Un cliente no debe poder crear torneos");
    }

    // ── HU-2: Administrador crea torneo competitivo ───────────────────────

    @Test
    public void testCrearTorneoCompetitivo()
    {
        Torneo torneo = sistema.crearTorneoCompetitivo("Poker Nocturno", DiaSemana.VIERNES, juego, 10, 50000);

        assertNotNull(torneo, "El torneo competitivo debe crearse correctamente");
        assertEquals("Poker Nocturno", torneo.getNombre());
        assertTrue(torneo instanceof TorneoCompetitivo);
        assertEquals(50000, ((TorneoCompetitivo) torneo).getTarifaEntrada(), 0.01);
    }

    // ── HU-3: Cliente se inscribe en torneo amistoso ──────────────────────

    @Test
    public void testClienteSeInscribeEnTorneoAmistoso()
    {
        Torneo torneo = sistema.crearTorneoAmistoso("Chess Classic", DiaSemana.MIERCOLES, juego, 8, 20000);

        Cliente cliente = new Cliente("222", "Juan", "juan@test.com", "juan01", "pass123");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);

        InscripcionTorneo inscripcion = sistema.inscribirEnTorneo(torneo, 2);

        assertNotNull(inscripcion, "La inscripcion debe ser exitosa");
        assertEquals(2, inscripcion.getNumeroCupos());
        assertFalse(torneo.getInscripciones().isEmpty(), "El torneo debe tener inscripciones");
    }

    // ── HU-4: Cliente se inscribe en torneo competitivo ──────────────────

    @Test
    public void testClienteSeInscribeEnTorneoCompetitivo()
    {
        // 1. Crear torneo con admin (sesionActual ya es admin en setUp)
        Torneo torneo = sistema.crearTorneoCompetitivo("Poker Nocturno", DiaSemana.VIERNES, juego, 10, 50000);
        assertNotNull(torneo, "El torneo debe crearse");

        // 2. Ahora cambiar a cliente
        Cliente cliente = new Cliente("333", "Maria", "maria@test.com", "maria01", "pass456");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);

        // 3. Inscribir
        InscripcionTorneo inscripcion = sistema.inscribirEnTorneo(torneo, 1);
        assertNotNull(inscripcion, "La inscripcion competitiva debe ser exitosa");
    }

    // ── HU-5: Cliente se desinscribe de torneo ────────────────────────────

    @Test
    public void testClienteSeDesinscribeDeUnTorneo()
    {
        Torneo torneo = sistema.crearTorneoAmistoso("Chess Classic", DiaSemana.MIERCOLES, juego, 8, 20000);

        Cliente cliente = new Cliente("222", "Juan", "juan@test.com", "juan01", "pass123");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);
        sistema.inscribirEnTorneo(torneo, 2);

        boolean resultado = sistema.desinscribirDeTorneo(torneo);

        assertTrue(resultado, "La desinscripcion debe ser exitosa");
        assertTrue(torneo.getInscripciones().isEmpty(), "El torneo no debe tener inscripciones");
    }

    // ── HU-6: Validacion de cupos ─────────────────────────────────────────

    @Test
    public void testInscripcionFallaSiNoHayCupos()
    {
        // Torneo con solo 2 cupos
        Torneo torneo = sistema.crearTorneoAmistoso("Mini Torneo", DiaSemana.LUNES, juego, 2, 10000);

        Cliente c1 = new Cliente("001", "Pedro", "pedro@t.com", "pedro01", "p1");
        Cliente c2 = new Cliente("002", "Ana", "ana@t.com", "ana01", "p2");
        sistema.agregarUsuario(c1);
        sistema.agregarUsuario(c2);

        sistema.setSesionActual(c1);
        sistema.inscribirEnTorneo(torneo, 1);

        sistema.setSesionActual(c2);
        sistema.inscribirEnTorneo(torneo, 1);

        // Tercer usuario — no debe caber
        Cliente c3 = new Cliente("003", "Luis", "luis@t.com", "luis01", "p3");
        sistema.agregarUsuario(c3);
        sistema.setSesionActual(c3);
        InscripcionTorneo resultado = sistema.inscribirEnTorneo(torneo, 1);

        assertNull(resultado, "No debe poderse inscribir si el torneo esta lleno");
    }

    // ── HU-7: Consultar torneos disponibles ───────────────────────────────

    @Test
    public void testConsultarTorneosDisponibles()
    {
        sistema.crearTorneoAmistoso("Torneo A", DiaSemana.LUNES, juego, 4, 10000);
        sistema.crearTorneoCompetitivo("Torneo B", DiaSemana.MARTES, juego, 6, 30000);

        java.util.List<Torneo> torneos = sistema.consultarTorneosDisponibles();

        assertNotNull(torneos);
        assertEquals(2, torneos.size(), "Deben existir 2 torneos disponibles");
    }
}