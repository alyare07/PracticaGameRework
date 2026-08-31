package principal.mapa.renderEntidades.camara;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Controlador de restricciones geométricas con soporte para Look-Ahead.
 * 
 * @version 3.5
 */
public class GestorDeLimites {

	private int x;
	private int y;
	private Ente entidadEnfocada;

	private int anchoTerreno;
	private int altoTerreno;
	private boolean limitesPersonalizados;

	private int limiteMaximoX;
	private int limiteMaximoY;
	private int limiteMinimoX;
	private int limiteMinimoY;

	private boolean activo;
	private boolean gestionandoX;
	private boolean gestionandoY;

	public GestorDeLimites() {
		this.activo = false;
		this.limitesPersonalizados = false;
	}

	public void actualizar() {
		this.actualizar(0.0, 0.0);
	}

	/**
	 * Evalúa los límites del mapa incorporando el desplazamiento predictivo
	 * (Look-Ahead) antes de aplicar el clamp.
	 */
	public void actualizar(final double lookAheadX, final double lookAheadY) {
		if (!this.activo || (this.entidadEnfocada == null)) {
			this.gestionandoX = false;
			this.gestionandoY = false;
			return;
		}

		this.recalcularLimitesDinamicos();

		final int posX = (int) Math.round(this.entidadEnfocada.getPosicionX() + lookAheadX);
		final int posY = (int) Math.round(this.entidadEnfocada.getPosicionY() + lookAheadY);

		// --- Control del Eje X con Clamping Estricto ---
		if (this.limiteMinimoX > this.limiteMaximoX) {
			this.gestionandoX = true;
			this.x = (this.limiteMinimoX + this.limiteMaximoX) / 2;
		} else if (posX <= this.limiteMinimoX) {
			this.gestionandoX = true;
			this.x = this.limiteMinimoX;
		} else if (posX >= this.limiteMaximoX) {
			this.gestionandoX = true;
			this.x = this.limiteMaximoX;
		} else {
			this.gestionandoX = true;
			this.x = posX;
		}

		// --- Control del Eje Y con Clamping Estricto ---
		if (this.limiteMinimoY > this.limiteMaximoY) {
			this.gestionandoY = true;
			this.y = (this.limiteMinimoY + this.limiteMaximoY) / 2;
		} else if (posY <= this.limiteMinimoY) {
			this.gestionandoY = true;
			this.y = this.limiteMinimoY;
		} else if (posY >= this.limiteMaximoY) {
			this.gestionandoY = true;
			this.y = this.limiteMaximoY;
		} else {
			this.gestionandoY = true;
			this.y = posY;
		}
	}

	private void recalcularLimitesDinamicos() {
		if (this.limitesPersonalizados || (this.entidadEnfocada == null)) {
			return;
		}

		final double zoomBase = (Globales.CAMARA != null) ? Math.max(0.2, Globales.CAMARA.getZoom()) : 1.0;

		final int enteAncho = (this.entidadEnfocada.getArea() != null) ? this.entidadEnfocada.getArea().width : 0;
		final int enteAlto = (this.entidadEnfocada.getArea() != null) ? this.entidadEnfocada.getArea().height : 0;

		final int semiAnchoVisible = (int) Math.round((Constantes.ANCHO_JUEGO / zoomBase) / 2.0);
		final int semiAltoVisible = (int) Math.round((Constantes.ALTO_JUEGO / zoomBase) / 2.0);

		this.limiteMinimoX = semiAnchoVisible - (enteAncho / 2);
		this.limiteMaximoX = this.anchoTerreno - semiAnchoVisible - (enteAncho / 2);

		this.limiteMinimoY = semiAltoVisible - (enteAlto / 2);
		this.limiteMaximoY = this.altoTerreno - semiAltoVisible - (enteAlto / 2);
	}

	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;
		this.limitesPersonalizados = false;

		this.anchoTerreno = Constantes.ANCHO_JUEGO;
		this.altoTerreno = Constantes.ALTO_JUEGO;

		if ((e.getMundo() != null) && (e.getMundo().getTerreno() != null)) {
			this.anchoTerreno = e.getMundo().getTerreno().getAncho();
			this.altoTerreno = e.getMundo().getTerreno().getAlto();
		}

		this.recalcularLimitesDinamicos();
	}

	public void setEntidadEnfocada(final Ente e, final int limiteMaximoX, final int limiteMinimoX,
			final int limiteMaximoY, final int limiteMinimoY, final boolean contarDimensionEnte) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;
		this.limitesPersonalizados = true;

		final int enteAncho = (contarDimensionEnte && (e.getArea() != null)) ? (e.getArea().width / 2) : 0;
		final int enteAlto = (contarDimensionEnte && (e.getArea() != null)) ? (e.getArea().height / 2) : 0;

		this.limiteMinimoX = limiteMinimoX - enteAncho;
		this.limiteMaximoX = limiteMaximoX - enteAncho;
		this.limiteMinimoY = limiteMinimoY - enteAlto;
		this.limiteMaximoY = limiteMaximoY - enteAlto;
	}

	public Ente getEntidadEnfocada() {
		return this.entidadEnfocada;
	}

	public boolean gestionandoX() {
		return this.gestionandoX;
	}

	public boolean gestionandoY() {
		return this.gestionandoY;
	}

	public void eliminar() {
		this.activo = false;
		this.gestionandoX = false;
		this.gestionandoY = false;
	}

	public void restituir() {
		this.activo = true;
	}

	public boolean estaEliminado() {
		return !this.activo;
	}

	public int getPosicionXInt() {
		return (this.gestionandoX) ? this.x : this.entidadEnfocada.getPosicionXInt();
	}

	public int getPosicionYInt() {
		return (this.gestionandoY) ? this.y : this.entidadEnfocada.getPosicionYInt();
	}

	public double getPosicionX() {
		return (this.gestionandoX) ? (double) this.x : this.entidadEnfocada.getPosicionX();
	}

	public double getPosicionY() {
		return (this.gestionandoY) ? (double) this.y : this.entidadEnfocada.getPosicionY();
	}
}