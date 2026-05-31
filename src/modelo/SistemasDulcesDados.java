package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        this.sugerencias = new ArrayList<SugerenciaPlatillo>();
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
        List<SugerenciaPlatillo> sugerenciasCargadas = persistencia.cargarSugerencias();

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

        if (sugerenciasCargadas != null)
        {
            sugerencias = sugerenciasCargadas;
        }
        else
        {
            sugerencias = new ArrayList<SugerenciaPlatillo>();
        }

        if (cafe != null)
        {
            List<Torneo> torneosCargados = persistencia.cargarTorneos(cafe, usuarios);

            if (torneosCargados != null)
            {
                cafe.setTorneos(torneosCargados);
            }
        }

        asegurarEstructuraMinima();
    }

    public void guardarDatos()
    {
        if (persistencia != null)
        {
            if (usuarios != null)
            {
                persistencia.guardarUsuarios(usuarios);
            }

            if (cafe != null)
            {
                persistencia.guardarCafe(cafe);

                if (cafe.getTorneos() != null)
                {
                    persistencia.guardarTorneos(cafe.getTorneos());
                }
            }

            if (sugerencias != null)
            {
                persistencia.guardarSugerencias(sugerencias);
            }
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

        if (sugerencias == null)
        {
            sugerencias = new ArrayList<SugerenciaPlatillo>();
        }

        if (cafe.getTorneos() == null)
        {
            cafe.setTorneos(new ArrayList<Torneo>());
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
        guardarDatos();

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

        boolean eliminado = usuarios.remove(usuario);

        if (eliminado)
        {
            guardarDatos();
        }

        return eliminado;
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
            guardarDatos();
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

        guardarDatos();
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

    public void agregarSugerencia(SugerenciaPlatillo sugerencia)
    {
        if (sugerencia != null)
        {
            sugerencias.add(sugerencia);
            guardarDatos();
        }
    }

    public List<SugerenciaPlatillo> getSugerencias()
    {
        return sugerencias;
    }

    public Torneo crearTorneoAmistoso(String nombre, DiaSemana dia, JuegoMesa juego, int cupos, double bono)
    {
        if (!(sesionActual instanceof Administrador))
        {
            return null;
        }

        Administrador admin = (Administrador) sesionActual;
        Torneo torneo = admin.crearTorneoAmistoso(nombre, dia, juego, cupos, bono, cafe);

        if (torneo != null)
        {
            guardarDatos();
        }

        return torneo;
    }

    public Torneo crearTorneoCompetitivo(String nombre, DiaSemana dia, JuegoMesa juego, int cupos, double tarifa)
    {
        if (!(sesionActual instanceof Administrador))
        {
            return null;
        }

        Administrador admin = (Administrador) sesionActual;
        Torneo torneo = admin.crearTorneoCompetitivo(nombre, dia, juego, cupos, tarifa, cafe);

        if (torneo != null)
        {
            guardarDatos();
        }

        return torneo;
    }

    public List<Torneo> consultarTorneos()
    {
        if (cafe == null || cafe.getTorneos() == null)
        {
            return new ArrayList<Torneo>();
        }

        return cafe.getTorneos();
    }

    public List<Torneo> consultarTorneosDisponibles()
    {
        if (cafe == null)
        {
            return new ArrayList<Torneo>();
        }

        return cafe.consultarTorneosDisponibles();
    }

    public InscripcionTorneo inscribirEnTorneo(Torneo torneo, int numeroCupos)
    {
        if (sesionActual == null || torneo == null)
        {
            return null;
        }

        InscripcionTorneo inscripcion = torneo.inscribir(sesionActual, numeroCupos);

        if (inscripcion != null)
        {
            guardarDatos();
        }

        return inscripcion;
    }

    public boolean desinscribirDeTorneo(Torneo torneo)
    {
        if (sesionActual == null || torneo == null)
        {
            return false;
        }

        boolean desinscrito = torneo.desinscribir(sesionActual);

        if (desinscrito)
        {
            guardarDatos();
        }

        return desinscrito;
    }

    public boolean aplicarBonoATorneo(Venta venta)
    {
        boolean aplicado = false;

        if (sesionActual instanceof Cliente)
        {
            aplicado = ((Cliente) sesionActual).aplicarBonoAVenta(venta);
        }
        else if (sesionActual instanceof Empleado)
        {
            aplicado = ((Empleado) sesionActual).aplicarBonoAVenta(venta);
        }

        if (aplicado)
        {
            guardarDatos();
        }

        return aplicado;
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

        if (documentoIdentidad.trim().isEmpty() || nombre.trim().isEmpty() || correoElectronico.trim().isEmpty()
                || login.trim().isEmpty() || password.trim().isEmpty()
                || codigoEmpleado.trim().isEmpty() || cargo.trim().isEmpty())
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

        if (cafe != null)
        {
            cafe.agregarEmpleado(empleado);
        }

        guardarDatos();

        return true;
    }

    public List<Venta> getVentasPorRangoFechas(LocalDate inicio, LocalDate fin)
    {
        List<Venta> resultado = new ArrayList<Venta>();

        if (cafe == null || cafe.getVentas() == null || inicio == null || fin == null)
        {
            return resultado;
        }

        for (Venta venta : cafe.getVentas())
        {
            if (venta.getFechaHora() == null || venta.getFechaHora().length() < 10)
            {
                continue;
            }

            try
            {
                LocalDate fecha = LocalDate.parse(venta.getFechaHora().substring(0, 10));

                if (!fecha.isBefore(inicio) && !fecha.isAfter(fin))
                {
                    resultado.add(venta);
                }
            }
            catch (Exception e)
            {
                // Si una fecha está mal escrita en el archivo, se ignora esa venta.
            }
        }

        return resultado;
    }
}