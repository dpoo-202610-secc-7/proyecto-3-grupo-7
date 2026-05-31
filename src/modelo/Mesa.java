package modelo;

import java.util.ArrayList;
import java.util.List;

public class Mesa {
	private int numeroMesa;
    private List<ReservaMesa> reservas;
    private List<Bebida> bebidasCalientesActivas;

    public Mesa(int numeroMesa)
    {
        this.numeroMesa = numeroMesa;
        this.reservas = new ArrayList<ReservaMesa>();
        this.bebidasCalientesActivas = new ArrayList<Bebida>();
    }

    public void asignarReserva(ReservaMesa reserva)
    {
        if (reserva != null)
        {
            reservas.add(reserva);
        }
    }

    public void cerrarReservaActiva()
    {
        ReservaMesa reservaActiva = getReservaActiva();
        if (reservaActiva != null)
        {
            reservaActiva.cerrar();
        }
        bebidasCalientesActivas.clear();
    }

    // ─── Bebidas calientes ───────────────────────────────────────────────────

    /**
     * Registra que se sirvió una bebida caliente en esta mesa.
     * Debe llamarse desde la capa de aplicación al confirmar un pedido
     * que incluya una Bebida con esCaliente() == true.
     */
    public void registrarBebidaCaliente(Bebida bebida)
    {
        if (bebida != null && bebida.esCaliente())
        {
            bebidasCalientesActivas.add(bebida);
        }
    }

    /**
     * Devuelve true si actualmente hay al menos una bebida caliente
     * registrada en esta mesa.
     */
    public boolean tieneBebidaCalienteActiva()
    {
        return !bebidasCalientesActivas.isEmpty();
    }

    // ─── Juego de Acción ────────────────────────────────────────────────────

    /**
     * Devuelve true si la mesa tiene actualmente un préstamo activo
     * que incluya un juego de categoría ACCION.
     *
     * @param cafe necesario para consultar el historial de préstamos.
     */
    public boolean tieneJuegoAccionActivo(Cafe cafe)
    {
        if (cafe == null)
        {
            return false;
        }

        for (Prestamo prestamo : cafe.getPrestamos())
        {
            if (!prestamo.estaActivo())
            {
                continue;
            }

            if (prestamo.getMesa() == null
                    || prestamo.getMesa().getNumeroMesa() != this.numeroMesa)
            {
                continue;
            }

            for (DetallePrestamo detalle : prestamo.getDetalles())
            {
                if (!detalle.estaDevuelto()
                        && detalle.getCopiaJuego().getJuegoMesa().esCategoriaAccion())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Versión sin Cafe — conservada por compatibilidad.
     * No puede determinar el estado real de los préstamos.
     * Preferir tieneJuegoAccionActivo(Cafe).
     */
    public boolean tieneJuegoAccionActivo()
    {
        return false;
    }

    // ─── Validaciones de despacho ────────────────────────────────────────────

    public boolean tieneMenoresEdad()
    {
        ReservaMesa reservaActiva = getReservaActiva();
        return reservaActiva != null && reservaActiva.isHayMenoresEdad();
    }

    public boolean tieneNinosMenores5()
    {
        ReservaMesa reservaActiva = getReservaActiva();
        return reservaActiva != null && reservaActiva.isHayNinosMenores5();
    }

    public boolean admiteJuego(JuegoMesa juego, int numeroPersonas)
    {
        if (juego == null)
        {
            return false;
        }
        return juego.esAptoParaCantidadJugadores(numeroPersonas)
                && juego.esAptoParaEdad(tieneNinosMenores5(), tieneMenoresEdad());
    }

    /**
     * Verifica si la mesa puede recibir la bebida indicada.
     * Usa tieneJuegoAccionActivo(Cafe) para la restricción de bebida caliente.
     *
     * @param bebida bebida a verificar.
     * @param cafe   referencia al café para consultar préstamos activos.
     */
    public boolean puedeRecibirBebida(Bebida bebida, Cafe cafe)
    {
        if (bebida == null)
        {
            return false;
        }

        if (bebida.esAlcoholica() && tieneMenoresEdad())
        {
            return false;
        }

        if (bebida.esCaliente() && tieneJuegoAccionActivo(cafe))
        {
            return false;
        }

        return true;
    }

    /**
     * Versión sin Cafe — conservada por compatibilidad.
     * No evalúa la restricción bebida caliente + juego Acción.
     * Preferir puedeRecibirBebida(Bebida, Cafe).
     */
    public boolean puedeRecibirBebida(Bebida bebida)
    {
        if (bebida == null)
        {
            return false;
        }

        if (bebida.esAlcoholica() && tieneMenoresEdad())
        {
            return false;
        }

        if (bebida.esCaliente() && tieneJuegoAccionActivo())
        {
            return false;
        }

        return true;
    }

    /**
     * Verifica si la mesa puede recibir el juego indicado en préstamo.
     * Usa tieneBebidaCalienteActiva() para la restricción de juego Acción.
     *
     * @param juego juego a verificar.
     */
    public boolean puedeRecibirJuego(JuegoMesa juego)
    {
        if (juego == null)
        {
            return false;
        }

        if (juego.esCategoriaAccion() && tieneBebidaCalienteActiva())
        {
            return false;
        }

        ReservaMesa reservaActiva = getReservaActiva();
        if (reservaActiva == null)
        {
            return false;
        }

        return admiteJuego(juego, reservaActiva.getNumeroPersonas());
    }

    // ─── Reserva activa ──────────────────────────────────────────────────────

    public ReservaMesa getReservaActiva()
    {
        for (ReservaMesa reserva : reservas)
        {
            if (reserva.estaActiva())
            {
                return reserva;
            }
        }
        return null;
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public int getNumeroMesa()
    {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa)
    {
        this.numeroMesa = numeroMesa;
    }

    public List<ReservaMesa> getReservas()
    {
        return reservas;
    }

    public void setReservas(List<ReservaMesa> reservas)
    {
        this.reservas = reservas;
    }

    public List<Bebida> getBebidasCalientesActivas()
    {
        return bebidasCalientesActivas;
    }

    public void setBebidasCalientesActivas(List<Bebida> bebidasCalientesActivas)
    {
        this.bebidasCalientesActivas = bebidasCalientesActivas;
    }
}