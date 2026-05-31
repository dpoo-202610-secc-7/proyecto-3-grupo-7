package test.torneos;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import modelo.*;
import persistencia.PersistenciaSistema;

import java.io.File;

public class PruebasIntegracionUsuarios
{
    private SistemasDulcesDados sistema;
    private Administrador admin;
    private String rutaPruebas = "datos_prueba_usuarios_test";

    @BeforeEach
    public void setUp()
    {
        PersistenciaSistema persistencia = new PersistenciaSistema(rutaPruebas);
        Cafe cafe = new Cafe(50);
        sistema = new SistemasDulcesDados(cafe, persistencia);
        

        admin = new Administrador("000", "Admin Test", "admin@test.com", "admin", "admin123");
        sistema.agregarUsuario(admin);
        sistema.setSesionActual(admin);
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

    // ── HU-1: Cliente se registra en el sistema ───────────────────────────

    @Test
    public void testRegistrarCliente()
    {
        boolean resultado = sistema.registrarCliente("123", "Juan Perez", "juan@test.com", "juan01", "pass123");

        assertTrue(resultado, "El cliente debe registrarse correctamente");
        assertNotNull(sistema.buscarUsuarioPorLogin("juan01"), "El cliente debe encontrarse en el sistema");
    }

    @Test
    public void testRegistrarClienteLoginDuplicadoFalla()
    {
        sistema.registrarCliente("123", "Juan Perez", "juan@test.com", "juan01", "pass123");
        boolean resultado = sistema.registrarCliente("456", "Otro Juan", "otro@test.com", "juan01", "otropass");

        assertFalse(resultado, "No debe permitir registrar dos usuarios con el mismo login");
    }

    @Test
    public void testRegistrarClienteCamposVaciosFalla()
    {
        boolean resultado = sistema.registrarCliente("", "Juan", "juan@test.com", "juan01", "pass");
        assertFalse(resultado, "No debe registrar cliente con documento vacio");
    }

    // ── HU-2: Administrador registra empleado ─────────────────────────────

    @Test
    public void testAdminRegistraMesero()
    {
        boolean resultado = sistema.registrarEmpleadoPorAdministrador(
            "789", "Carlos Lopez", "carlos@test.com", "carlos01", "pass789", "EMP001", "MESERO");

        assertTrue(resultado, "El administrador debe poder registrar un mesero");
        Usuario u = sistema.buscarUsuarioPorLogin("carlos01");
        assertNotNull(u);
        assertTrue(u instanceof Mesero, "El usuario registrado debe ser Mesero");
    }

    @Test
    public void testAdminRegistraCocinero()
    {
        boolean resultado = sistema.registrarEmpleadoPorAdministrador(
            "321", "Laura Ruiz", "laura@test.com", "laura01", "pass321", "EMP002", "COCINERO");

        assertTrue(resultado, "El administrador debe poder registrar un cocinero");
        Usuario u = sistema.buscarUsuarioPorLogin("laura01");
        assertTrue(u instanceof Cocinero, "El usuario registrado debe ser Cocinero");
    }

    @Test
    public void testClienteNoPuedeRegistrarEmpleado()
    {
        Cliente cliente = new Cliente("555", "Maria", "maria@test.com", "maria01", "pass555");
        sistema.agregarUsuario(cliente);
        sistema.setSesionActual(cliente);

        boolean resultado = sistema.registrarEmpleadoPorAdministrador(
            "999", "Empleado Ilegal", "emp@test.com", "emp01", "pass", "EMP999", "MESERO");

        assertFalse(resultado, "Un cliente no debe poder registrar empleados");
    }

    @Test
    public void testCargoInvalidoFalla()
    {
        boolean resultado = sistema.registrarEmpleadoPorAdministrador(
            "777", "Pedro", "pedro@test.com", "pedro01", "pass", "EMP003", "GERENTE");

        assertFalse(resultado, "Un cargo invalido no debe ser aceptado");
    }

    // ── HU-3: Autenticacion de usuarios ───────────────────────────────────

    @Test
    public void testAutenticacionExitosa()
    {
        sistema.registrarCliente("123", "Juan", "juan@test.com", "juan01", "pass123");
        sistema.setSesionActual(null);

        Usuario usuario = sistema.iniciarSesion("juan01", "pass123");

        assertNotNull(usuario, "La autenticacion debe ser exitosa con credenciales correctas");
        assertEquals("juan01", usuario.getLogin());
    }

    @Test
    public void testAutenticacionPasswordIncorrectaFalla()
    {
        sistema.registrarCliente("123", "Juan", "juan@test.com", "juan01", "pass123");

        Usuario usuario = sistema.iniciarSesion("juan01", "wrongpass");

        assertNull(usuario, "La autenticacion debe fallar con password incorrecta");
    }

    @Test
    public void testAutenticacionLoginInexistenteFalla()
    {
        Usuario usuario = sistema.iniciarSesion("noexiste", "pass");

        assertNull(usuario, "La autenticacion debe fallar con login inexistente");
    }

    // ── HU-4: Buscar usuario por login ────────────────────────────────────

    @Test
    public void testBuscarUsuarioPorLogin()
    {
        sistema.registrarCliente("123", "Juan", "juan@test.com", "juan01", "pass123");

        Usuario encontrado = sistema.buscarUsuarioPorLogin("juan01");

        assertNotNull(encontrado);
        assertEquals("Juan", encontrado.getNombre());
    }

    @Test
    public void testBuscarUsuarioInexistenteRetornaNull()
    {
        Usuario encontrado = sistema.buscarUsuarioPorLogin("fantasma");
        assertNull(encontrado, "Buscar un usuario inexistente debe retornar null");
    }
}