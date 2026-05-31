package modelo;

import java.util.ArrayList;
import java.util.List;
import modelo.InscripcionTorneo;
import modelo.Torneo;
import modelo.DiaSemana;

import persistencia.PersistenciaSistema;

public class SistemasDulcesDados
{
    private Cafe cafe;
    private List<Usuario> usuarios;
    private PersistenciaSistema persistencia;
    private Usuario sesionActual;
    private List<SugerenciaPlatillo> sugerencias;

    public SistemasDulcesDados(Cafe cafe, PersistenciaSistema persistencia)
    {
        this.cafe = cafe;
        this.usuarios = new ArrayList<Usuario>();
        this.persistencia = persistencia;
        this.sesionActual = null;
        this.sugerencias = new ArrayList<>();
    }

    public void inicializarSistema()
    {
        cargarDatos();
        asegurarEstructuraMinima();
    }

    public void cargarDatos()
    {
        if (persistencia == null)
        {
            asegurarEstructuraMinima();
            return;
        }

        List<Usuario> usuariosCargados = persistencia.cargarUsuarios();
        Cafe cafeCargado = persistencia.cargarCafe();
        List<SugerenciaPlatillo> sugsGuardadas = persistencia.cargarSugerencias();

        if (sugsGuardadas != null) {
            this.sugerencias = sugsGuardadas;
        }

        if (usuariosCargados != null)
        {
            usuarios = usuariosCargados;
        }
        else
        {
            usuarios = new ArrayList<Usuario>();
        }

        if (cafeCargado != null)
        {
            cafe = cafeCargado;
        }

        List<Torneo> torneosCargados = persistencia.cargarTorneos(cafe, usuarios);
        if (torneosCargados != null)
        {
            cafe.setTorneos(torneosCargados);
        }

        asegurarEstructuraMinima();
    }

    public void guardarDatos()
    {
        if (persistencia != null)
        {
            persistencia.guardarUsuarios(usuarios);
            persistencia.guardarCafe(cafe);
            persistencia.guardarTorneos(cafe.getTorneos());

        }
    }

    private void asegurarEstructuraMinima()
    {
        if (cafe == null)
        {
            cafe = new Cafe(50);
        }

        if (usuarios == null)
        {
            usuarios = new ArrayList<Usuario>();
        }
    }

    public Usuario autenticarUsuario(String login, String password)
    {
        if (login == null || password == null)
        {
            return null;
        }

        Usuario usuario = buscarUsuarioPorLogin(login);

        if (usuario != null && usuario.validarPassword(password))
        {
            sesionActual = usuario;
            return usuario;
        }

        return null;
    }

    public void cerrarSesion()
    {
        sesionActual = null;
    }

    public boolean haySesionIniciada()
    {
        return sesionActual != null;
    }

    public Usuario buscarUsuarioPorLogin(String login)
    {
        if (login == null || usuarios == null)
        {
            return null;
        }

        for (Usuario usuario : usuarios)
        {
            if (usuario.getLogin() != null && usuario.getLogin().equals(login))
            {
                return usuario;
            }
        }
        return null;
    }

    public boolean agregarUsuario(Usuario usuario)
    {
        if (usuario == null)
        {
            return false;
        }

        if (usuario.getLogin() == null || usuario.getLogin().trim().isEmpty())
        {
            return false;
        }

        if (buscarUsuarioPorLogin(usuario.getLogin()) != null)
        {
            return false;
        }

        usuarios.add(usuario);
        return true;
    }

    public boolean eliminarUsuarioPorLogin(String login)
    {
        Usuario usuario = buscarUsuarioPorLogin(login);

        if (usuario == null)
        {
            return false;
        }

        if (sesionActual != null && sesionActual == usuario)
        {
            sesionActual = null;
        }

        return usuarios.remove(usuario);
    }

    public Cafe obtenerCafe()
    {
        return cafe;
    }

    public Cafe getCafe()
    {
        return cafe;
    }

    public void setCafe(Cafe cafe)
    {
        if (cafe != null)
        {
            this.cafe = cafe;
        }
    }

    public List<Usuario> getUsuarios()
    {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios)
    {
        if (usuarios != null)
        {
            this.usuarios = usuarios;
        }
        else
        {
            this.usuarios = new ArrayList<Usuario>();
        }
    }

    public PersistenciaSistema getPersistencia()
    {
        return persistencia;
    }

    public void setPersistencia(PersistenciaSistema persistencia)
    {
        this.persistencia = persistencia;
    }

    public Usuario getSesionActual()
    {
        return sesionActual;
    }

    public void setSesionActual(Usuario sesionActual)
    {
        this.sesionActual = sesionActual;
    }
    public void agregarSugerencia(SugerenciaPlatillo sugerencia) {
        if (sugerencia != null) {
            sugerencias.add(sugerencia);
        }
    }

    public List<SugerenciaPlatillo> getSugerencias() {
        return sugerencias;
    }
 // ── Torneos ───────────────────────────────────────────────────────────

    public Torneo crearTorneoAmistoso(String nombre, DiaSemana dia,
                                      JuegoMesa juego, int cupos, double bono)
    {
        if (!(sesionActual instanceof Administrador))
        {
            return null; // solo el administrador puede crear torneos
        }
        Administrador admin = (Administrador) sesionActual;
        return admin.crearTorneoAmistoso(nombre, dia, juego, cupos, bono, cafe);
    }

    public Torneo crearTorneoCompetitivo(String nombre, DiaSemana dia,
                                         JuegoMesa juego, int cupos, double tarifa)
    {
        if (!(sesionActual instanceof Administrador))
        {
            return null;
        }
        Administrador admin = (Administrador) sesionActual;
        return admin.crearTorneoCompetitivo(nombre, dia, juego, cupos, tarifa, cafe);
    }

    public List<Torneo> consultarTorneos()
    {
        return cafe.getTorneos();
    }

    public List<Torneo> consultarTorneosDisponibles()
    {
        return cafe.consultarTorneosDisponibles();
    }

    public InscripcionTorneo inscribirEnTorneo(Torneo torneo, int numeroCupos)
    {
        if (sesionActual == null || torneo == null)
        {
            return null;
        }
        return torneo.inscribir(sesionActual, numeroCupos);
    }

    public boolean desinscribirDeTorneo(Torneo torneo)
    {
        if (sesionActual == null || torneo == null)
        {
            return false;
        }
        return torneo.desinscribir(sesionActual);
    }

    public boolean aplicarBonoATorneo(Venta venta)
    {
        if (sesionActual instanceof Cliente)
        {
            return ((Cliente) sesionActual).aplicarBonoAVenta(venta);
        }
        if (sesionActual instanceof Empleado)
        {
            return ((Empleado) sesionActual).aplicarBonoAVenta(venta);
        }
        return false;
    }
    
    public boolean registrarCliente(
            String documentoIdentidad,
            String nombre,
            String correoElectronico,
            String login,
            String password)
    {
        if (documentoIdentidad == null || nombre == null || correoElectronico == null || login == null || password == null)
        {
            return false;
        }

        if (documentoIdentidad.trim().isEmpty() || nombre.trim().isEmpty() || correoElectronico.trim().isEmpty()
                || login.trim().isEmpty() || password.trim().isEmpty())
        {
            return false;
        }

        if (buscarUsuarioPorLogin(login) != null)
        {
            return false;
        }

        Cliente cliente = new Cliente(documentoIdentidad, nombre, correoElectronico, login, password);
        usuarios.add(cliente);
        guardarDatos();

        return true;
    }
    
    public Usuario iniciarSesion(String login, String password)
    {
        return autenticarUsuario(login, password);
    }
    
    public boolean validarCredenciales(String login, String password)
    {
        Usuario usuario = buscarUsuarioPorLogin(login);

        if (usuario == null)
        {
            return false;
        }

        return usuario.validarPassword(password);
    }
    
    public boolean registrarEmpleadoPorAdministrador(
            String documentoIdentidad,
            String nombre,
            String correoElectronico,
            String login,
            String password,
            String codigoEmpleado,
            String cargo)
    {
        if (!(sesionActual instanceof Administrador))
        {
            return false;
        }

        if (documentoIdentidad == null || nombre == null || correoElectronico == null 
            || login == null || password == null || codigoEmpleado == null || cargo == null)
        {
            return false;
        }

        if (documentoIdentidad.length() == 0 || nombre.length() == 0 || correoElectronico.length() == 0
                || login.length() == 0 || password.length() == 0 || codigoEmpleado.length() == 0 || cargo.length() == 0)
        {
            return false;
        }

        if (buscarUsuarioPorLogin(login) != null)
        {
            return false;
        }

        Empleado empleado = null;

        if (cargo.equalsIgnoreCase("MESERO"))
        {
            empleado = new Mesero(documentoIdentidad, nombre, correoElectronico, login, password, codigoEmpleado);
        }
        else if (cargo.equalsIgnoreCase("COCINERO"))
        {
            empleado = new Cocinero(documentoIdentidad, nombre, correoElectronico, login, password, codigoEmpleado);
        }
        else
        {
            return false;
        }

        usuarios.add(empleado);
        guardarDatos();

        return true;
    }
}