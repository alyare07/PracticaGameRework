package principal.interaccion;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import principal.entes.Ente;
import principal.entes.criaturas.Jugador;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Gestor maestro de interacción espacial [E] con prevención de rebote (Zero-GC
 * / O(1)).
 * 
 * @version 2.0 (Vanilla Java 8 - Anti-Bounce Latch Guard)
 */
public class GestorInteraccion {

	private static final double RANGO_INTERACCION_MAX = 28.0;
	private final Rectangle areaEscaneo = new Rectangle();
	private final PromptInteraccion promptVisual = new PromptInteraccion();

	private Interactuable objetivoCercano = null;
	private double menorDistanciaSq = Double.MAX_VALUE;

	// Cooldown para evitar que la misma pulsación de [E] reabra el contenedor al
	// cerrarlo
	private final GestorTiempo gtCooldownInteraccion = new GestorTiempo();
	private static final int COOLDOWN_INTERACCION_MS = 300;

	private final AccionEntidad<Ente> accionEscaneo = new AccionEntidad<Ente>() {
		@Override
		public void ejecutar(final Ente ente) {
			if ((ente instanceof Interactuable) && !ente.estaEliminado()) {
				final Interactuable candidata = (Interactuable) ente;
				if (candidata.puedeInteractuar(Globales.JUGADOR)) {
					final double dx = Globales.JUGADOR.getCentroX() - candidata.getCentroX();
					final double dy = Globales.JUGADOR.getCentroY() - candidata.getPosicionYInt();
					final double distSq = (dx * dx) + (dy * dy);

					if (distSq < GestorInteraccion.this.menorDistanciaSq) {
						GestorInteraccion.this.menorDistanciaSq = distSq;
						GestorInteraccion.this.objetivoCercano = candidata;
					}
				}
			}
		}
	};

	public void actualizar(final Mundo mundo) {
		this.objetivoCercano = null;
		this.menorDistanciaSq = RANGO_INTERACCION_MAX * RANGO_INTERACCION_MAX;

		if ((mundo == null) || (Globales.JUGADOR == null) || Globales.JUGADOR.estaEliminado()) {
			return;
		}

		// Si hay diálogo, cinemática, inventario o cofre abierto, resetea el
		// temporizador y bloquea el escáner
		if (Globales.GESTOR_DIALOGOS.isActivo() || Globales.GESTOR_EVENTOS.haySecuenciaEnCurso()
				|| Globales.GESTOR_INVENTARIO.hayInventarioTerceroAbierto()
				|| Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible()) {
			this.gtCooldownInteraccion.establecerReferenciaTiempoActual();
			return;
		}

		// Protección anti-rebote: exige 300 ms tras cerrar cualquier ventana antes de
		// permitir abrir otra
		if (!this.gtCooldownInteraccion.transcurrioMiliSegundos(COOLDOWN_INTERACCION_MS)) {
			return;
		}

		final Jugador j = Globales.JUGADOR;
		final int r = (int) RANGO_INTERACCION_MAX;
		this.areaEscaneo.setBounds(j.getCentroX() - r, j.getCentroY() - r, r * 2, r * 2);

		// Escaneo espacial en ZoneBox sin allocaciones
		mundo.paraCadaEnteEn(this.areaEscaneo, false, true, this.accionEscaneo);

		// Disparo de interacción al presionar E
		if ((this.objetivoCercano != null) && Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)) {
			this.gtCooldownInteraccion.establecerReferenciaTiempoActual();
			this.objetivoCercano.interactuar(j);
		}
	}

	public void pintar(final Graphics2D g) {
		final boolean bloqueado = Globales.GESTOR_DIALOGOS.isActivo() || Globales.GESTOR_EVENTOS.haySecuenciaEnCurso()
				|| Globales.GESTOR_INVENTARIO.hayInventarioTerceroAbierto()
				|| Globales.GESTOR_INVENTARIO.getInventarioJugador().esVisible();

		if ((this.objetivoCercano != null) && !bloqueado) {
			this.promptVisual.pintar(g, this.objetivoCercano);
		}
	}

	public Interactuable getObjetivoCercano() {
		return this.objetivoCercano;
	}
}