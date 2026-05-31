package test.torneos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import modelo.*;
import persistencia.PersistenciaSistema;

import java.io.File;

public class PruebasIntegracionPersistencia
{
    private SistemasDulcesDados sistema;
    private Administrador admin;
    private JuegoMesa juego;
    private String rutaPruebas = "datos_prueba_persistencia_test";

    @BeforeEach
    public void setUp()
    {
        PersistenciaSistema persistencia = new PersistenciaSistema(rutaPruebas);
        Cafe cafe = new Cafe(50);
        sistema = new SistemasDulcesDados(cafe, persistencia);

        admin = new Administrador("000", "Admin Test", "admin@test.com", "admin", "admin123");
        sistema.agregarUsuario(admin);
        sistema.setSesionActual(admin);
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
        File carpeta = new File(rutaPruebas);
        if (carpeta.exists())
        {
            for (File f : carpeta.listFiles()) f.delete();
            carpeta.delete();
        }
    }

    // ── Persistencia de usuarios ──────────────────────────────────────────

    @Test
    public void testUsuariosPersistenCorrectamente()
    {
        sistema.registrarCliente("123", "Juan", "juan@test.com", "juan01", "pass123");

        // Guardar y crear sistema nuevo que cargue desde archivos
        sistema.guardarDatos();
        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        Usuario cargado = sistema2.buscarUsuarioPorLogin("juan01");
        assertNotNull(cargado, "El cliente debe persistir y cargarse correctamente");
        assertEquals("Juan", cargado.getNombre());
        assertTrue(cargado instanceof Cliente);
    }

    @Test
    public void testEmpleadoPersisteDespuesDeGuardar()
    {
        sistema.registrarEmpleadoPorAdministrador(
            "789", "Carlos", "carlos@test.com", "carlos01", "pass789", "EMP001", "MESERO");

        sistema.guardarDatos();
        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        Usuario cargado = sistema2.buscarUsuarioPorLogin("carlos01");
        assertNotNull(cargado, "El empleado debe persistir correctamente");
        assertTrue(cargado instanceof Mesero);
    }

    // ── Persistencia de torneos ───────────────────────────────────────────

    @Test
    public void testTorneoAmistosoPersiste()
    {
        sistema.crearTorneoAmistoso("Chess Classic", DiaSemana.MIERCOLES, juego, 8, 20000);

        sistema.guardarDatos();
        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        java.util.List<Torneo> torneos = sistema2.consultarTorneos();
        assertFalse(torneos.isEmpty(), "Debe haber torneos cargados");
        assertEquals("Chess Classic", torneos.get(0).getNombre());
        assertTrue(torneos.get(0) instanceof TorneoAmistoso);
    }

    @Test
    public void testTorneoCompetitivoPersiste()
    {
        sistema.crearTorneoCompetitivo("Poker Nocturno", DiaSemana.VIERNES, juego, 10, 50000);

        sistema.guardarDatos();
        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        java.util.List<Torneo> torneos = sistema2.consultarTorneos();
        assertFalse(torneos.isEmpty());
        assertTrue(torneos.get(0) instanceof TorneoCompetitivo);
        assertEquals(50000, ((TorneoCompetitivo) torneos.get(0)).getTarifaEntrada(), 0.01);
    }

    // ── Persistencia de inscripciones ─────────────────────────────────────

    @Test
    public void testInscripcionPersisteDentroDelTorneo()
    {
        Torneo torneo = sistema.crearTorneoAmistoso("Chess Classic", DiaSemana.MIERCOLES, juego, 8, 20000);

        Cliente cliente = new Cliente("222", "Juan", "juan@test.com", "juan01", "pass123");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);
        sistema.inscribirEnTorneo(torneo, 2);

        // Guardar con admin y recargar
        sistema.setSesionActual(admin);
        sistema.guardarDatos();

        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        java.util.List<Torneo> torneos = sistema2.consultarTorneos();
        assertFalse(torneos.isEmpty());
        assertFalse(torneos.get(0).getInscripciones().isEmpty(),
            "La inscripcion debe persistir dentro del torneo");
        assertEquals("juan01",
            torneos.get(0).getInscripciones().get(0).getUsuario().getLogin());
    }

    // ── Flujo completo: crear → inscribir → guardar → cargar → verificar ──

    @Test
    public void testFlujoCompletoTorneoInscripcionPersistencia()
    {
        // 1. Crear torneo
        Torneo torneo = sistema.crearTorneoCompetitivo("Gran Final", DiaSemana.SABADO, juego, 6, 100000);
        assertNotNull(torneo);

        // 2. Registrar y autenticar cliente
        sistema.registrarCliente("321", "Laura", "laura@test.com", "laura01", "pass321");
        sistema.setSesionActual(sistema.buscarUsuarioPorLogin("laura01"));

        // 3. Inscribir
        InscripcionTorneo inscripcion = sistema.inscribirEnTorneo(torneo, 1);
        assertNotNull(inscripcion, "La inscripcion debe ser exitosa");

        // 4. Guardar
        sistema.setSesionActual(admin);
        sistema.guardarDatos();

        // 5. Cargar en sistema nuevo
        SistemasDulcesDados sistema2 = crearSistemaNuevo();
        sistema2.cargarDatos();

        // 6. Verificar que todo persiste
        Usuario clienteCargado = sistema2.buscarUsuarioPorLogin("laura01");
        assertNotNull(clienteCargado, "El cliente debe persistir");

        java.util.List<Torneo> torneos = sistema2.consultarTorneos();
        assertFalse(torneos.isEmpty(), "El torneo debe persistir");
        assertFalse(torneos.get(0).getInscripciones().isEmpty(),
            "La inscripcion debe persistir");
    }

    // ── Utilidad ──────────────────────────────────────────────────────────

    private SistemasDulcesDados crearSistemaNuevo()
    {
        PersistenciaSistema persistencia2 = new PersistenciaSistema(rutaPruebas);
        Cafe cafe2 = new Cafe(50);

        // Recrear juego en el nuevo cafe para que cargarTorneos pueda referenciarlo
        JuegoMesa juego2 = new JuegoMesa("Ajedrez", 2, "Clasico", 2, 4, 1, false, CategoriaJuego.TABLERO);
        for (int i = 0; i < 15; i++)
        {
            juego2.agregarCopia(new CopiaJuegoPrestamo(EstadoJuego.NUEVO, true, juego2));
        }
        cafe2.agregarJuegoCatalogo(juego2);

        return new SistemasDulcesDados(cafe2, persistencia2);
    }
}