package principal.interaccion;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import principal.entes.Ente;
import principal.entes.criaturas.Jugador;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;

public class GestorInteraccion {

	private static final double RANGO_INTERACCION_MAX = 28.0;
	private final Rectangle areaEscaneo = new Rectangle();
	private final PromptInteraccion promptVisual = new PromptInteraccion();

	private Interactuable objetivoCercano = null;
	private double menorDistanciaSq = Double.MAX_VALUE;

	private final AccionEntidad<Ente> accionEscaneo = new AccionEntidad<Ente>() {
		@Override
		public void ejecutar(final Ente ente) {
			if (ente instanceof Interactuable && !ente.estaEliminado()) {
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

		if (mundo == null || Globales.JUGADOR == null || Globales.JUGADOR.estaEliminado()) {
			return;
		}

		// Si hay un diálogo o cinemática en curso, se bloquea la interacción
		if (Globales.GESTOR_DIALOGOS.isActivo() || Globales.GESTOR_EVENTOS.haySecuenciaEnCurso()) {
			return;
		}

		final Jugador j = Globales.JUGADOR;
		final int r = (int) RANGO_INTERACCION_MAX;
		this.areaEscaneo.setBounds(j.getCentroX() - r, j.getCentroY() - r, r * 2, r * 2);

		// Escaneo en celdas espaciales ZoneBox sin generar basura
		mundo.paraCadaEnteEn(this.areaEscaneo, false, true, this.accionEscaneo);

		// Disparo de interacción al presionar E
		if (this.objetivoCercano != null && Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_E)) {
			this.objetivoCercano.interactuar(j);
		}
	}

	public void pintar(final Graphics2D g) {
		if (this.objetivoCercano != null && !Globales.GESTOR_DIALOGOS.isActivo()
				&& !Globales.GESTOR_EVENTOS.haySecuenciaEnCurso()) {
			this.promptVisual.pintar(g, this.objetivoCercano);
		}
	}

	public Interactuable getObjetivoCercano() {
		return this.objetivoCercano;
	}
}