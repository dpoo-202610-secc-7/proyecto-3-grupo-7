package persistencia;
import modelo.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia del sistema Dulces & Dados.
 *
 * Archivos que gestiona (todos dentro de rutaDatos/):
 *   usuarios.txt          – clientes, meseros, cocineros, administrador
 *   cafe.txt              – capacidad + números de mesa
 *   empleados_cafe.txt    – logins de los empleados asociados al café
 *   admin_cafe.txt        – login del administrador del café
 *   juegos_mesa.txt       – catálogo de juegos de mesa
 *   copias_prestamo.txt   – copias de cada juego (una por línea)
 *   juegos_venta.txt      – juegos en venta
 *   menu.txt              – bebidas y pastelerías del menú
 *   turnos.txt            – turnos de empleados
 *   juegos_conocidos.txt  – juegos que conoce cada mesero
 *   juegos_favoritos.txt  – favoritos de clientes y empleados
 *   solicitudes_turno.txt – solicitudes de cambio de turno
 *   sugerencias.txt       – sugerencias de platillos
 *   reservas.txt          – reservas de mesas
 *   prestamos.txt         – préstamos de juegos
 *   detalles_prestamo.txt – detalles (copias) de cada préstamo
 *   ventas.txt            – ventas registradas
 *   items_venta.txt       – ítems de cada venta
 */
public class PersistenciaSistema {

    private String rutaDatos;

    // ── Cache para evitar lecturas repetidas ────────────────────────────
    private boolean cargado = false;
    private List<Usuario> usuariosCacheados = null;
    private Cafe cafeCacheado = null;
    private List<SugerenciaPlatillo> sugerenciasCacheadas = null;

    // ═══════════════════════════════════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════════════════════════════════

    public PersistenciaSistema(String rutaDatos) {
        this.rutaDatos = rutaDatos;
        crearCarpetaSiNoExiste();
    }

    // ═══════════════════════════════════════════════════════════════════
    // API pública
    // ═══════════════════════════════════════════════════════════════════

    public List<Usuario> cargarUsuarios() {
        if (!cargado) cargarTodo();
        return usuariosCacheados;
    }

    public Cafe cargarCafe() {
        if (!cargado) cargarTodo();
        return cafeCacheado;
    }

    public List<SugerenciaPlatillo> cargarSugerencias() {
        if (!cargado) cargarTodo();
        return sugerenciasCacheadas;
    }

    public void guardarUsuarios(List<Usuario> usuarios) {
        escribirUsuariosBasico(usuarios);
        escribirTurnos(usuarios);
        escribirJuegosConocidosMesero(usuarios);
        escribirJuegosFavoritos(usuarios);
        escribirSolicitudesTurno(usuarios);
        invalidarCache();
    }

    public void guardarCafe(Cafe cafe) {
        escribirCafeBasico(cafe);
        escribirEmpleadosCafe(cafe);
        escribirAdministradorCafe(cafe);
        escribirJuegosMesa(cafe);
        escribirCopiasPrestamo(cafe);
        escribirJuegosVenta(cafe);
        escribirMenu(cafe);
        escribirReservas(cafe);
        escribirPrestamos(cafe);
        escribirVentas(cafe);
        invalidarCache();
    }

    public void guardarSugerencias(List<SugerenciaPlatillo> sugerencias) {
        this.sugerenciasCacheadas = sugerencias;
        escribirSugerencias();
        invalidarCache();
    }

    /** Compatibilidad — las ventas se persisten dentro de guardarCafe. */
    public List<Venta> cargarVentas() {
        if (!cargado) cargarTodo();
        return cafeCacheado != null ? cafeCacheado.getVentas() : new ArrayList<Venta>();
    }

    public void guardarVentas(List<Venta> ventas) { /* se hace en guardarCafe */ }

    /** Compatibilidad — los préstamos se persisten dentro de guardarCafe. */
    public List<Prestamo> cargarPrestamos() {
        if (!cargado) cargarTodo();
        return cafeCacheado != null ? cafeCacheado.getPrestamos() : new ArrayList<Prestamo>();
    }

    public void guardarPrestamos(List<Prestamo> prestamos) { /* se hace en guardarCafe */ }

    public List<Turno> cargarTurnos() { return new ArrayList<Turno>(); }
    public void guardarTurnos(List<Turno> turnos) { /* se hace en guardarUsuarios */ }

    public String getRutaDatos() { return rutaDatos; }
    public void setRutaDatos(String rutaDatos) {
        this.rutaDatos = rutaDatos;
        crearCarpetaSiNoExiste();
        invalidarCache();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Carga completa (una sola pasada)
    // ═══════════════════════════════════════════════════════════════════

    private void cargarTodo() {
    	
        if (cargado) return;

        usuariosCacheados   = new ArrayList<Usuario>();
        sugerenciasCacheadas = new ArrayList<SugerenciaPlatillo>();

        leerUsuariosBasico();

        cafeCacheado = leerCafeBasico();
        if (cafeCacheado == null) cafeCacheado = new Cafe(30);

        leerEmpleadosCafe();
        leerAdministradorCafe();
        leerJuegosMesa();
        leerCopiasPrestamo();
        leerJuegosVenta();
        leerMenu();
        leerTurnos();
        leerJuegosConocidosMesero();
        leerJuegosFavoritos();
        leerSolicitudesTurno();
        leerSugerencias();
        leerReservas();
        leerPrestamos();
        leerVentas();

        cargado = true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Métodos de LECTURA
    // ═══════════════════════════════════════════════════════════════════

    private void leerUsuariosBasico() {
        File archivo = path("usuarios.txt");


        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                if (linea.isEmpty()) continue;

    

                String[] p = linea.split(";", -1);

                if ("CLIENTE".equals(p[0]) && p.length >= 7) {
                    Cliente c = new Cliente(p[1], p[2], p[3], p[4], p[5]);
                    c.setPuntosFidelidad(Integer.parseInt(p[6]));
                    usuariosCacheados.add(c);

         

                } else if ("MESERO".equals(p[0]) && p.length >= 7) {

                    Mesero m = new Mesero(p[1], p[2], p[3], p[4], p[5], p[6]);
                    usuariosCacheados.add(m);

 

                } else if ("COCINERO".equals(p[0]) && p.length >= 7) {

                    Cocinero c = new Cocinero(p[1], p[2], p[3], p[4], p[5], p[6]);
                    usuariosCacheados.add(c);

        

                } else if ("ADMIN".equals(p[0]) && p.length >= 6) {

                    Administrador a = new Administrador(p[1], p[2], p[3], p[4], p[5]);
                    usuariosCacheados.add(a);

                    
                }
            }

   

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Cafe leerCafeBasico() {
        File archivo = path("cafe.txt");
        if (!archivo.exists()) return null;

        try (BufferedReader br = abrir(archivo)) {
            String linea = br.readLine();
            if (linea == null) return null;

            Cafe cafe = new Cafe(Integer.parseInt(linea.trim()));
            String ml;
            while ((ml = br.readLine()) != null) {
                ml = ml.trim();
                if (!ml.isEmpty()) cafe.agregarMesa(new Mesa(Integer.parseInt(ml)));
            }
            return cafe;
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    private void leerEmpleadosCafe() {
        File archivo = path("empleados_cafe.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
          
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                Usuario u = usuarioPorLogin(linea);
                if (u instanceof Empleado) cafeCacheado.agregarEmpleado((Empleado) u);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerAdministradorCafe() {
        File archivo = path("admin_cafe.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea = br.readLine();
            if (linea != null && !linea.trim().isEmpty()) {
                Usuario u = usuarioPorLogin(linea.trim());
                if (u instanceof Administrador) cafeCacheado.setAdministrador((Administrador) u);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerJuegosMesa() {
        File archivo = path("juegos_mesa.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 8) {
                    cafeCacheado.agregarJuegoCatalogo(new JuegoMesa(
                        p[0], Integer.parseInt(p[1]), p[2],
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                        Integer.parseInt(p[5]), Boolean.parseBoolean(p[6]),
                        CategoriaJuego.valueOf(p[7])
                    ));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerCopiasPrestamo() {
        File archivo = path("copias_prestamo.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 3) {
                    JuegoMesa juego = juegoMesaPorNombre(p[2]);
                    if (juego != null) {
                        juego.agregarCopia(new CopiaJuegoPrestamo(
                            EstadoJuego.valueOf(p[0]),
                            Boolean.parseBoolean(p[1]),
                            juego
                        ));
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerJuegosVenta() {
        File archivo = path("juegos_venta.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 4) {
                    cafeCacheado.agregarJuegoVenta(new JuegoVenta(
                        p[0], Double.parseDouble(p[1]),
                        Boolean.parseBoolean(p[2]), Integer.parseInt(p[3])
                    ));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerMenu() {
        File archivo = path("menu.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);

                if ("BEBIDA".equals(p[0]) && p.length >= 6) {
                    cafeCacheado.agregarProductoMenu(new Bebida(
                        p[1], Double.parseDouble(p[2]),
                        Boolean.parseBoolean(p[3]),
                        Boolean.parseBoolean(p[4]),
                        Boolean.parseBoolean(p[5])
                    ));
                } else if ("PASTELERIA".equals(p[0]) && p.length >= 4) {
                    Pasteleria pas = new Pasteleria(p[1], Double.parseDouble(p[2]), Boolean.parseBoolean(p[3]));
                    if (p.length >= 5 && !p[4].trim().isEmpty()) {
                        for (String al : p[4].split(",")) {
                            al = al.trim();
                            if (!al.isEmpty()) pas.agregarAlergeno(Alergeno.valueOf(al));
                        }
                    }
                    cafeCacheado.agregarProductoMenu(pas);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerTurnos() {
        File archivo = path("turnos.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 4) {
                    Usuario u = usuarioPorLogin(p[0]);
                    if (u instanceof Empleado) {
                        ((Empleado) u).getTurnos().add(new Turno(DiaSemana.valueOf(p[1]), p[2], p[3]));
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerJuegosConocidosMesero() {
        File archivo = path("juegos_conocidos.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 2) {
                    Usuario u = usuarioPorLogin(p[0]);
                    JuegoMesa j = juegoMesaPorNombre(p[1]);
                    if (u instanceof Mesero && j != null) ((Mesero) u).registrarJuegoConocido(j);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerJuegosFavoritos() {
        File archivo = path("juegos_favoritos.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                if (p.length >= 2) {
                    Usuario u = usuarioPorLogin(p[0]);
                    JuegoMesa j = juegoMesaPorNombre(p[1]);
                    if (u instanceof Cliente && j != null)  ((Cliente) u).agregarJuegoFavorito(j);
                    else if (u instanceof Empleado && j != null) ((Empleado) u).agregarJuegoFavorito(j);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerSolicitudesTurno() {
        File archivo = path("solicitudes_turno.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                // formato: loginSol;tipo;fechaHora;estado;loginDest;diaOrig;horaIniOrig;horaFinOrig;diaProp;horaIniProp;horaFinProp
                if (p.length >= 11) {
                    Usuario uSol = usuarioPorLogin(p[0]);
                    if (!(uSol instanceof Empleado)) continue;

                    TipoSolicitud tipo = TipoSolicitud.valueOf(p[1]);
                    String fechaHora = fromNull(p[2]);
                    String estado    = fromNull(p[3]);

                    Usuario uDest = usuarioPorLogin(p[4]);
                    Empleado destino = (uDest instanceof Empleado) ? (Empleado) uDest : null;

                    Turno turnoOrig = turnoDeEmpleado((Empleado) uSol, p[5], p[6], p[7]);
                    Turno turnoProp = (!p[8].isEmpty())
                        ? new Turno(DiaSemana.valueOf(p[8]), p[9], p[10])
                        : null;

                    SolicitudCambioTurno sol = new SolicitudCambioTurno(
                        tipo, fechaHora, estado, (Empleado) uSol, turnoOrig, turnoProp, destino
                    );
                    ((Empleado) uSol).getSolicitudesCambioTurno().add(sol);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerSugerencias() {
        File archivo = path("sugerencias.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // formato: loginEmpleado;nombrePropuesto;categoria;fechaHora;estado;loginAdmin
                String[] p = linea.split(";", -1);
                if (p.length >= 6) {
                    Usuario uEmp   = usuarioPorLogin(p[0]);
                    Usuario uAdmin = usuarioPorLogin(p[5]);

                    if (uEmp instanceof Empleado) {
                        Administrador admin = (uAdmin instanceof Administrador) ? (Administrador) uAdmin : null;
                        EstadoSugerencia estado = p[4].isEmpty() ? null : EstadoSugerencia.valueOf(p[4]);

                        SugerenciaPlatillo sug = new SugerenciaPlatillo(
                            p[1], CategoriaPropuesta.valueOf(p[2]),
                            fromNull(p[3]), estado, (Empleado) uEmp, admin
                        );
                        sugerenciasCacheadas.add(sug);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerReservas() {
        File archivo = path("reservas.txt");
        if (!archivo.exists()) return;

        try (BufferedReader br = abrir(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(";", -1);
                // formato: fechaHora;numPersonas;hayNinos;hayMenores;estado;loginCliente;numMesa
                if (p.length >= 7) {
                    Usuario u  = usuarioPorLogin(p[5]);
                    Mesa mesa  = mesaPorNumero(Integer.parseInt(p[6]));
                    if (u instanceof Cliente && mesa != null) {
                        ReservaMesa reserva = new ReservaMesa(
                            fromNull(p[0]), Integer.parseInt(p[1]),
                            Boolean.parseBoolean(p[2]), Boolean.parseBoolean(p[3]),
                            (Cliente) u, mesa
                        );
                        reserva.setEstadoReserva(EstadoReserva.valueOf(p[4]));
                        mesa.asignarReserva(reserva);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerPrestamos() {
        File archivoPrest = path("prestamos.txt");
        if (!archivoPrest.exists()) return;

        List<Prestamo> indicePrestamos = new ArrayList<Prestamo>();

        try (BufferedReader br = abrir(archivoPrest)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // formato: fechaInicio;fechaFin;advertencia;loginUsuario;numMesa
                String[] p = linea.split(";", -1);
                if (p.length >= 5) {
                    Usuario usuario = usuarioPorLogin(p[3]);
                    Mesa mesa = "NULL".equals(p[4]) ? null : mesaPorNumero(Integer.parseInt(p[4]));
                    Prestamo pr = new Prestamo(fromNull(p[0]), fromNull(p[1]), Boolean.parseBoolean(p[2]), usuario, mesa);
                    indicePrestamos.add(pr);
                    cafeCacheado.registrarPrestamo(pr);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        File archivoDetalles = path("detalles_prestamo.txt");
        if (!archivoDetalles.exists()) return;

        try (BufferedReader br = abrir(archivoDetalles)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // formato: indicePrestamo;nombreJuego;indiceCopia;fechaAsig;fechaDev
                String[] p = linea.split(";", -1);
                if (p.length >= 5) {
                    int idxPr     = Integer.parseInt(p[0]);
                    int idxCopia  = Integer.parseInt(p[2]);
                    JuegoMesa jue = juegoMesaPorNombre(p[1]);
                    if (jue != null && idxPr < indicePrestamos.size() && idxCopia < jue.getCopias().size()) {
                        CopiaJuegoPrestamo copia = jue.getCopias().get(idxCopia);
                        DetallePrestamo det = new DetallePrestamo(copia);
                        det.setFechaAsignacion(fromNull(p[3]));
                        det.setFechaDevolucion(fromNull(p[4]));
                        indicePrestamos.get(idxPr).getDetalles().add(det);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void leerVentas()
    {
        File archivoVentas = path("ventas.txt");
        if (!archivoVentas.exists())
        {
            return;
        }

        List<Venta> indiceVentas = new ArrayList<Venta>();

        try (BufferedReader br = abrir(archivoVentas))
        {
            String linea;
            while ((linea = br.readLine()) != null)
            {
                linea = linea.trim();
                if (linea.isEmpty())
                {
                    continue;
                }

                String[] p = linea.split(";", -1);
                if (p.length >= 6)
                {
                    String fechaHora = fromNull(p[0]);
                    TipoVenta tipoVenta = TipoVenta.valueOf(p[1]);
                    double descuento = Double.parseDouble(p[2]);
                    double propina = Double.parseDouble(p[3]);
                    Usuario comprador = usuarioPorLogin(p[4]);

                    Mesero mesero = null;
                    if (!"NULL".equals(p[5]))
                    {
                        Usuario u = usuarioPorLogin(p[5]);
                        if (u instanceof Mesero)
                        {
                            mesero = (Mesero) u;
                        }
                    }

                    Venta venta = new Venta(fechaHora, tipoVenta, descuento, propina, comprador, mesero);
                    indiceVentas.add(venta);
                    cafeCacheado.registrarVenta(venta);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File archivoItems = path("items_venta.txt");
        if (!archivoItems.exists())
        {
            return;
        }

        try (BufferedReader br = abrir(archivoItems))
        {
            String linea;
            while ((linea = br.readLine()) != null)
            {
                linea = linea.trim();
                if (linea.isEmpty())
                {
                    continue;
                }

                String[] p = linea.split(";", -1);
                if (p.length >= 5)
                {
                    int indiceVenta = Integer.parseInt(p[0]);
                    int cantidad = Integer.parseInt(p[1]);
                    double precio = Double.parseDouble(p[2]);
                    String nombreProducto = p[3];
                    String tipoProducto = p[4];

                    if (indiceVenta < indiceVentas.size())
                    {
                        ProductoVendible producto = null;

                        if ("MENU".equals(tipoProducto))
                        {
                            producto = productoMenuPorNombre(nombreProducto);
                        }
                        else if ("JUEGO_VENTA".equals(tipoProducto))
                        {
                            producto = juegoVentaPorNombre(nombreProducto);
                        }

                        if (producto != null)
                        {
                            ItemVenta item = new ItemVenta(cantidad, precio, producto);
                            item.setPrecioUnitario(precio);
                            indiceVentas.get(indiceVenta).agregarItem(item);
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Métodos de ESCRITURA
    // ═══════════════════════════════════════════════════════════════════

    private void escribirUsuariosBasico(List<Usuario> usuarios) {
        try (FileWriter fw = crear("usuarios.txt")) {
            for (Usuario u : usuarios) {
                if (u instanceof Cliente) {
                    Cliente c = (Cliente) u;
                    fw.write("CLIENTE;" + safe(c.getDocumentoIdentidad()) + ";" + safe(c.getNombre()) + ";"
                        + safe(c.getCorreoElectronico()) + ";" + safe(c.getLogin()) + ";"
                        + safe(c.getPassword()) + ";" + c.getPuntosFidelidad() + "\n");
                } else if (u instanceof Mesero) {
                    Mesero m = (Mesero) u;
                    fw.write("MESERO;" + safe(m.getDocumentoIdentidad()) + ";" + safe(m.getNombre()) + ";"
                        + safe(m.getCorreoElectronico()) + ";" + safe(m.getLogin()) + ";"
                        + safe(m.getPassword()) + ";" + safe(m.getCodigoEmpleado()) + "\n");
                } else if (u instanceof Cocinero) {
                    Cocinero c = (Cocinero) u;
                    fw.write("COCINERO;" + safe(c.getDocumentoIdentidad()) + ";" + safe(c.getNombre()) + ";"
                        + safe(c.getCorreoElectronico()) + ";" + safe(c.getLogin()) + ";"
                        + safe(c.getPassword()) + ";" + safe(c.getCodigoEmpleado()) + "\n");
                } else if (u instanceof Administrador) {
                    Administrador a = (Administrador) u;
                    fw.write("ADMIN;" + safe(a.getDocumentoIdentidad()) + ";" + safe(a.getNombre()) + ";"
                        + safe(a.getCorreoElectronico()) + ";" + safe(a.getLogin()) + ";"
                        + safe(a.getPassword()) + "\n");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirTurnos(List<Usuario> usuarios) {
        try (FileWriter fw = crear("turnos.txt")) {
            for (Usuario u : usuarios) {
                if (u instanceof Empleado) {
                    Empleado emp = (Empleado) u;
                    for (Turno t : emp.getTurnos()) {
                        fw.write(emp.getLogin() + ";" + t.getDia() + ";" + t.getHoraInicio() + ";" + t.getHoraFin() + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirJuegosConocidosMesero(List<Usuario> usuarios) {
        try (FileWriter fw = crear("juegos_conocidos.txt")) {
            for (Usuario u : usuarios) {
                if (u instanceof Mesero) {
                    Mesero m = (Mesero) u;
                    for (JuegoMesa j : m.getJuegosConocidos()) {
                        fw.write(m.getLogin() + ";" + safe(j.getNombre()) + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirJuegosFavoritos(List<Usuario> usuarios) {
        try (FileWriter fw = crear("juegos_favoritos.txt")) {
            for (Usuario u : usuarios) {
                List<JuegoMesa> favs = null;
                if (u instanceof Cliente)        favs = ((Cliente) u).consultarJuegosFavoritos();
                else if (u instanceof Empleado)  favs = ((Empleado) u).consultarJuegosFavoritos();
                if (favs != null) {
                    for (JuegoMesa j : favs) fw.write(u.getLogin() + ";" + safe(j.getNombre()) + "\n");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirSolicitudesTurno(List<Usuario> usuarios) {
        try (FileWriter fw = crear("solicitudes_turno.txt")) {
            for (Usuario u : usuarios) {
                if (u instanceof Empleado) {
                    Empleado emp = (Empleado) u;
                    for (SolicitudCambioTurno s : emp.getSolicitudesCambioTurno()) {
                        Turno orig = s.getTurnoOriginal();
                        Turno prop = s.getTurnoPropuesto();
                        String loginDest = s.getEmpleadoDestino() != null ? s.getEmpleadoDestino().getLogin() : "NULL";
                        String diaO  = orig != null ? orig.getDia().name()    : "";
                        String hiniO = orig != null ? orig.getHoraInicio()    : "";
                        String hfinO = orig != null ? orig.getHoraFin()       : "";
                        String diaP  = prop != null ? prop.getDia().name()    : "";
                        String hiniP = prop != null ? prop.getHoraInicio()    : "";
                        String hfinP = prop != null ? prop.getHoraFin()       : "";

                        fw.write(emp.getLogin() + ";" + s.getTipoSolicitud() + ";"
                            + toNull(s.getFechaHora()) + ";" + toNull(s.getEstado()) + ";"
                            + loginDest + ";" + diaO + ";" + hiniO + ";" + hfinO + ";"
                            + diaP + ";" + hiniP + ";" + hfinP + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirSugerencias() {
        try (FileWriter fw = crear("sugerencias.txt")) {
            if (sugerenciasCacheadas != null) {
                for (SugerenciaPlatillo s : sugerenciasCacheadas) {
                    String loginEmp   = s.getEmpleado()   != null ? s.getEmpleado().getLogin()   : "NULL";
                    String loginAdmin = s.getRevisadaPor() != null ? s.getRevisadaPor().getLogin() : "NULL";
                    fw.write(loginEmp + ";" + safe(s.getNombrePropuesto()) + ";"
                        + s.getCategoriaPropuesta() + ";" + toNull(s.getFechaHora()) + ";"
                        + (s.getEstado() != null ? s.getEstado() : "") + ";" + loginAdmin + "\n");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirCafeBasico(Cafe cafe) {
        try (FileWriter fw = crear("cafe.txt")) {
            if (cafe != null) {
                fw.write(cafe.getCapacidadMaximaClientes() + "\n");
                for (Mesa m : cafe.getMesas()) fw.write(m.getNumeroMesa() + "\n");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirEmpleadosCafe(Cafe cafe) {
        try (FileWriter fw = crear("empleados_cafe.txt")) {
            if (cafe != null) {
                for (Empleado emp : cafe.getEmpleados()) fw.write(emp.getLogin() + "\n");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirAdministradorCafe(Cafe cafe) {
        try (FileWriter fw = crear("admin_cafe.txt")) {
            if (cafe != null && cafe.getAdministrador() != null)
                fw.write(cafe.getAdministrador().getLogin() + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirJuegosMesa(Cafe cafe) {
        try (FileWriter fw = crear("juegos_mesa.txt")) {
            if (cafe != null) {
                for (JuegoMesa j : cafe.getCatalogoJuegos()) {
                    fw.write(safe(j.getNombre()) + ";" + j.getAnioPublicacion() + ";"
                        + safe(j.getEmpresaMatriz()) + ";" + j.getMinJugadores() + ";"
                        + j.getMaxJugadores() + ";" + j.getEdadMinima() + ";"
                        + j.isDificil() + ";" + j.getCategoria() + "\n");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirCopiasPrestamo(Cafe cafe) {
        try (FileWriter fw = crear("copias_prestamo.txt")) {
            if (cafe != null) {
                for (JuegoMesa j : cafe.getCatalogoJuegos()) {
                    for (CopiaJuegoPrestamo c : j.getCopias()) {
                        fw.write(c.getEstado() + ";" + c.isDisponible() + ";" + safe(j.getNombre()) + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirJuegosVenta(Cafe cafe) {
        try (FileWriter fw = crear("juegos_venta.txt")) {
            if (cafe != null) {
                for (JuegoVenta j : cafe.getInventarioVenta()) {
                    fw.write(safe(j.getNombre()) + ";" + j.getPrecio() + ";"
                        + j.isDisponible() + ";" + j.getStockDisponible() + "\n");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirMenu(Cafe cafe) {
        try (FileWriter fw = crear("menu.txt")) {
            if (cafe != null) {
                for (ProductoMenu p : cafe.getMenu()) {
                    if (p instanceof Bebida) {
                        Bebida b = (Bebida) p;
                        fw.write("BEBIDA;" + safe(b.getNombre()) + ";" + b.getPrecio() + ";"
                            + b.isDisponible() + ";" + b.esAlcoholica() + ";" + b.esCaliente() + "\n");
                    } else if (p instanceof Pasteleria) {
                        Pasteleria pas = (Pasteleria) p;
                        StringBuilder alergenos = new StringBuilder();
                        List<Alergeno> lista = pas.obtenerAlergenos();
                        for (int i = 0; i < lista.size(); i++) {
                            if (i > 0) alergenos.append(",");
                            alergenos.append(lista.get(i).name());
                        }
                        fw.write("PASTELERIA;" + safe(pas.getNombre()) + ";" + pas.getPrecio() + ";"
                            + pas.isDisponible() + ";" + alergenos + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirReservas(Cafe cafe) {
        try (FileWriter fw = crear("reservas.txt")) {
            if (cafe != null) {
                for (Mesa mesa : cafe.getMesas()) {
                    for (ReservaMesa r : mesa.getReservas()) {
                        if (r.getCliente() != null) {
                            fw.write(toNull(r.getFechaHora()) + ";" + r.getNumeroPersonas() + ";"
                                + r.isHayNinosMenores5() + ";" + r.isHayMenoresEdad() + ";"
                                + r.getEstadoReserva() + ";" + r.getCliente().getLogin() + ";"
                                + mesa.getNumeroMesa() + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirPrestamos(Cafe cafe) {
        try (FileWriter fwPr = crear("prestamos.txt");
             FileWriter fwDe = crear("detalles_prestamo.txt")) {
            if (cafe != null) {
                List<Prestamo> prestamos = cafe.getPrestamos();
                for (int i = 0; i < prestamos.size(); i++) {
                    Prestamo pr = prestamos.get(i);
                    if (pr.getUsuario() == null) continue;
                    String numMesa = pr.getMesa() != null ? String.valueOf(pr.getMesa().getNumeroMesa()) : "NULL";
                    fwPr.write(toNull(pr.getFechaInicio()) + ";" + toNull(pr.getFechaFin()) + ";"
                        + pr.isAdvertenciaSinMesero() + ";" + pr.getUsuario().getLogin() + ";" + numMesa + "\n");

                    for (DetallePrestamo det : pr.getDetalles()) {
                        CopiaJuegoPrestamo copia = det.getCopiaJuego();
                        if (copia != null && copia.getJuegoMesa() != null) {
                            JuegoMesa j = copia.getJuegoMesa();
                            int idxCopia = j.getCopias().indexOf(copia);
                            fwDe.write(i + ";" + safe(j.getNombre()) + ";" + idxCopia + ";"
                                + toNull(det.getFechaAsignacion()) + ";" + toNull(det.getFechaDevolucion()) + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void escribirVentas(Cafe cafe) {
        try (FileWriter fwV = crear("ventas.txt");
             FileWriter fwI = crear("items_venta.txt")) {
            if (cafe != null) {
                List<Venta> ventas = cafe.getVentas();
                for (int i = 0; i < ventas.size(); i++) {
                    Venta v = ventas.get(i);
                    if (v.getComprador() == null) continue;
                    String loginMes = v.getRegistradaPor() != null ? v.getRegistradaPor().getLogin() : "NULL";
                    fwV.write(toNull(v.getFechaHora()) + ";" + v.getTipoVenta() + ";"
                        + v.getDescuentoAplicado() + ";" + v.getPropina() + ";"
                        + v.getComprador().getLogin() + ";" + loginMes + "\n");

                    for (ItemVenta item : v.getItems()) {
                        if (item.getProducto() == null) continue;
                        String tipo = (item.getProducto() instanceof ProductoMenu) ? "MENU" : "JUEGO_VENTA";
                        fwI.write(i + ";" + item.getCantidad() + ";" + item.getPrecioUnitario() + ";"
                            + safe(item.getProducto().getNombre()) + ";" + tipo + "\n");
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Utilidades privadas
    // ═══════════════════════════════════════════════════════════════════

    private Usuario usuarioPorLogin(String login) {
        if (login == null || usuariosCacheados == null) return null;
        for (Usuario u : usuariosCacheados) {
            if (u.getLogin() != null && u.getLogin().equals(login)) return u;
        }
        return null;
    }

    private JuegoMesa juegoMesaPorNombre(String nombre) {
        if (nombre == null || cafeCacheado == null) return null;
        for (JuegoMesa j : cafeCacheado.getCatalogoJuegos()) {
            if (j.getNombre() != null && j.getNombre().equals(nombre)) return j;
        }
        return null;
    }

    private Mesa mesaPorNumero(int numero) {
        if (cafeCacheado == null) return null;
        for (Mesa m : cafeCacheado.getMesas()) {
            if (m.getNumeroMesa() == numero) return m;
        }
        return null;
    }

    private ProductoMenu productoMenuPorNombre(String nombre) {
        if (nombre == null || cafeCacheado == null) return null;
        for (ProductoMenu p : cafeCacheado.getMenu()) {
            if (p.getNombre() != null && p.getNombre().equals(nombre)) return p;
        }
        return null;
    }

    private JuegoVenta juegoVentaPorNombre(String nombre) {
        if (nombre == null || cafeCacheado == null) return null;
        for (JuegoVenta j : cafeCacheado.getInventarioVenta()) {
            if (j.getNombre() != null && j.getNombre().equals(nombre)) return j;
        }
        return null;
    }

    private Turno turnoDeEmpleado(Empleado empleado, String dia, String horaIni, String horaFin) {
        if (empleado == null || dia == null || dia.isEmpty()) return null;
        for (Turno t : empleado.getTurnos()) {
            if (t.getDia() == DiaSemana.valueOf(dia)
                && t.getHoraInicio().equals(horaIni)
                && t.getHoraFin().equals(horaFin)) return t;
        }
        return null;
    }

    private String toNull(String texto) { return texto == null ? "NULL" : texto; }
    private String fromNull(String texto) { return "NULL".equals(texto) ? null : texto; }
    private String safe(String texto) { return texto == null ? "" : texto.replace(";", ","); }

    private File path(String nombreArchivo) {
        return new File(rutaDatos + File.separator + nombreArchivo);
    }

    private FileWriter crear(String nombreArchivo) throws IOException {
        return new FileWriter(path(nombreArchivo), false);
    }

    private BufferedReader abrir(File archivo) throws IOException {
        return new BufferedReader(new FileReader(archivo));
    }

    private void crearCarpetaSiNoExiste() {
        File carpeta = new File(rutaDatos);
        if (!carpeta.exists()) carpeta.mkdirs();
    }

    private void invalidarCache() {
        cargado = false;
        usuariosCacheados    = null;
        cafeCacheado         = null;
        sugerenciasCacheadas = null;
    }
 // ── Torneos ───────────────────────────────────────────────────────────────

    public List<Torneo> cargarTorneos(Cafe cafe, List<Usuario> usuarios) {
        List<Torneo> torneos = new ArrayList<>();
        File archivo = new File(rutaDatos + "/torneos.txt");
        if (!archivo.exists()) return torneos;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = linea.split(";");
                if (partes.length < 7) continue;

                try {
                    String tipo              = partes[0];
                    String nombre            = partes[1];
                    DiaSemana dia            = DiaSemana.valueOf(partes[2]);
                    String juegoNombre       = partes[3];
                    int cuposTotales         = Integer.parseInt(partes[4]);
                    int cuposFanaticosUsados = Integer.parseInt(partes[5]);
                    int cuposRegularesUsados = Integer.parseInt(partes[6]);

                    JuegoMesa juego = cafe.buscarJuegoMesaPorNombre(juegoNombre);
                    if (juego == null) continue; // juego no existe, saltar

                    Torneo torneo = null;
                    if (tipo.equalsIgnoreCase("AMISTOSO") && partes.length >= 8) {
                        double bono = Double.parseDouble(partes[7]);
                        torneo = new TorneoAmistoso(nombre, dia, juego, cuposTotales, bono);
                    } else if (tipo.equalsIgnoreCase("COMPETITIVO") && partes.length >= 8) {
                        double tarifa = Double.parseDouble(partes[7]);
                        torneo = new TorneoCompetitivo(nombre, dia, juego, cuposTotales, tarifa);
                    }

                    if (torneo == null) continue;

                    // Restaurar contadores (setters directos o reflection no disponible,
                    // usamos re-inscripciones ficticias para reconstruir el estado)
                    torneo.setCuposFanaticosUsados(cuposFanaticosUsados);
                    torneo.setCuposRegularesUsados(cuposRegularesUsados);

                    // Cargar inscripciones del torneo desde archivo separado
                    cargarInscripciones(torneo, usuarios, archivo.getParent());

                    torneos.add(torneo);
                } catch (Exception e) {
                    // Línea malformada, se ignora
                }
            }
        } catch (IOException e) {
            // Sin archivo = sin torneos
        }
        return torneos;
    }

    public void guardarTorneos(List<Torneo> torneos) {
        if (torneos == null) return;
        File archivo = new File(rutaDatos + "/torneos.txt");
        try (FileWriter fw = new FileWriter(archivo)) {
            for (Torneo t : torneos) {
                String tipo  = (t instanceof TorneoAmistoso)    ? "AMISTOSO"    : "COMPETITIVO";
                String extra = (t instanceof TorneoAmistoso)
                        ? String.valueOf(((TorneoAmistoso) t).getBonoDescuento())
                        : String.valueOf(((TorneoCompetitivo) t).getTarifaEntrada());

                fw.write(tipo + ";"
                        + t.getNombre() + ";"
                        + t.getDia() + ";"
                        + t.getJuego().getNombre() + ";"
                        + t.getCuposTotales() + ";"
                        + t.getCuposFanaticosUsados() + ";"
                        + t.getCuposRegularesUsados() + ";"
                        + extra + "\n");
            }
        } catch (IOException e) {
            // Error escribiendo torneos
        }
        guardarInscripciones(torneos);
    }

    // ── Helpers internos ──────────────────────────────────────────────────────

    private void cargarInscripciones(Torneo torneo, List<Usuario> usuarios, String carpeta) {
        File archivo = new File(carpeta + "/inscripciones_torneos.txt");
        if (!archivo.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split(";");
                if (p.length < 4) continue;
                if (!p[0].equals(torneo.getNombre())) continue;
                String loginUsuario    = p[1];
                int    numeroCupos     = Integer.parseInt(p[2]);
                boolean cupoFanatico   = Boolean.parseBoolean(p[3]);
                Usuario u = buscarUsuarioPorLogin(loginUsuario, usuarios);
                if (u != null) {
                    InscripcionTorneo ins = new InscripcionTorneo(u, numeroCupos, cupoFanatico);
                    torneo.getInscripciones().add(ins);
                }
            }
        } catch (IOException | NumberFormatException e) {
            // Ignorar
        }
    }

    private void guardarInscripciones(List<Torneo> torneos) {
        File archivo = new File(rutaDatos + "/inscripciones_torneos.txt");
        try (FileWriter fw = new FileWriter(archivo)) {
            for (Torneo t : torneos) {
                for (InscripcionTorneo ins : t.getInscripciones()) {
                    fw.write(t.getNombre() + ";"
                            + ins.getUsuario().getLogin() + ";"
                            + ins.getNumeroCupos() + ";"
                            + ins.isUsoCupoFanatico() + "\n");
                }
            }
        } catch (IOException e) {
            // Error
        }
    }

    private Usuario buscarUsuarioPorLogin(String login, List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            if (u.getLogin().equals(login)) return u;
        }
        return null;
    }
    
}
