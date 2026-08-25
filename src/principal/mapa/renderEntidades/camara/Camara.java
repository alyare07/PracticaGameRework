package principal.mapa.renderEntidades.camara;

import principal.entes.Ente;
import principal.utilidades.Constantes;

/**
 * Gestiona el enfoque y desplazamiento de la vista en pantalla sobre una
 * entidad (jugador o asistente).
 */
public class Camara {

	private Ente entidadEnfocada;
	private int margenX;
	private int margenY;
	private final GestorDeLimites gestorLimite;
	private static final double ZOOM_BASE = 1.0;
	private double zoom = 1.0; // 1.0 = normal, 1.5 = zoom-in, 0.5 = zoom-out

	public Camara(final Ente entidadEnfocada) {
		this.gestorLimite = new GestorDeLimites();
		if (entidadEnfocada != null) {
			this.setEntidadEnfocada(entidadEnfocada);
		}
	}

	/**
	 * Actualiza el gestor de límites en cada ciclo del juego si está activo.
	 */
	public void actualizar() {
		if (!this.gestorLimite.estaEliminado()) {
			this.gestorLimite.actualizar();
		}
	}

	public Ente getEntidadEnfocada() {
		return this.entidadEnfocada;
	}

	/**
	 * Habilita el control automático de bordes basado en el tamaño del mapa del
	 * mundo actual.
	 */
	public void habilitarGestorLimite() {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada);
	}

	/**
	 * Habilita el control automático de bordes especificando límites
	 * personalizados.
	 */
	public void habilitarGestorLimite(final int limiteMaximoX, final int limiteMinimoX, final int limiteMaximoY,
			final int limiteMinimoY, final boolean contarDimensionEnte) {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada, limiteMaximoX, limiteMinimoX, limiteMaximoY,
				limiteMinimoY, contarDimensionEnte);
	}

	/**
	 * Deshabilita la restricción de bordes de la cámara.
	 */
	public void deshabilitarGestorLimite() {
		this.gestorLimite.eliminar();
	}

	/**
	 * Establece una nueva entidad objetivo a enfocar.
	 *
	 * @param e Entidad a enfocar (Jugador, AsistenteCamara, etc.).
	 */
	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}

		this.entidadEnfocada = e;
		final int enteAncho = (e.getArea() != null) ? e.getArea().width : 0;
		final int enteAlto = (e.getArea() != null) ? e.getArea().height : 0;

		this.margenX = Constantes.CENTROX - (enteAncho / 2);
		this.margenY = Constantes.CENTROY - (enteAlto / 2);

		if (this.entidadEnfocada != this.gestorLimite.getEntidadEnfocada()) {
			this.gestorLimite.eliminar();
		}
	}

	public double getZoom() {
		return this.zoom;
	}

	public void setZoom(final double nuevoZoom) {
		// Clamping de seguridad redondeado al múltiplo de 0.25 más cercano
		final double zoomClampeado = Math.max(0.5, Math.min(2.5, nuevoZoom));
		this.zoom = Math.round(zoomClampeado * 4.0) / 4.0; // Redondea a 0.50, 0.75, 1.00, 1.25, 1.50...
	}

	public void reiniciarZoom() {
		this.zoom = ZOOM_BASE;
	}

	public void aumentarZoom() {
		this.setZoom(this.zoom + 0.25);
	}

	public void reducirZoom() {
		this.setZoom(this.zoom - 0.25);
	}

	public double getPosicionX() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionX()
				: this.gestorLimite.getPosicionX();
	}

	public double getPosicionY() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionY()
				: this.gestorLimite.getPosicionY();
	}

	public int getPosicionXInt() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionXInt()
				: this.gestorLimite.getPosicionXInt();
	}

	public int getPosicionYInt() {
		return (this.gestorLimite.estaEliminado()) ? this.entidadEnfocada.getPosicionYInt()
				: this.gestorLimite.getPosicionYInt();
	}

	public int getMargenX() {
		return this.margenX;
	}

	public int getMargenY() {
		return this.margenY;
	}
}