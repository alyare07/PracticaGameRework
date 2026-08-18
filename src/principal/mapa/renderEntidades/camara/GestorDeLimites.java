package principal.mapa.renderEntidades.camara;

import principal.entes.Ente;
import principal.utilidades.Globales;

/**
 * Encapsula el comportamiento de delimitación de bordes para que la cámara no
 * muestre zonas vacías por fuera de los límites del mapa.
 */
public class GestorDeLimites extends Ente {

	private static final long serialVersionUID = 1L;

	private int x;
	private int y;
	private Ente entidadEnfocada;

	private int limiteMaximoX;
	private int limiteMaximoY;
	private int limiteMinimoX;
	private int limiteMinimoY;

	private boolean eliminado;
	private boolean gestionandoX;
	private boolean gestionandoY;

	public GestorDeLimites() {
		this.eliminado = true; // Por defecto desactivado hasta llamar habilitar
	}

	/**
	 * Verifica si la entidad enfocada sobrepasa los bordes del mapa y toma el
	 * control de las coordenadas en caso afirmativo.
	 */
	@Override
	public void actualizar() {
		if (this.entidadEnfocada == null) {
			return;
		}

		final int posX = this.entidadEnfocada.getPosicionXInt();
		final int posY = this.entidadEnfocada.getPosicionYInt();

		// --- Control Eje X ---
		if (this.limiteMinimoX > this.limiteMaximoX) {
			// El mapa es más pequeño que la resolución de pantalla: Centrar fijamente
			this.gestionandoX = true;
			this.x = (this.limiteMinimoX + this.limiteMaximoX) / 2;
		} else if (posX <= this.limiteMinimoX) {
			this.gestionandoX = true;
			this.x = this.limiteMinimoX;
		} else if (posX >= this.limiteMaximoX) {
			this.gestionandoX = true;
			this.x = this.limiteMaximoX;
		} else {
			this.gestionandoX = false;
		}

		// --- Control Eje Y ---
		if (this.limiteMinimoY > this.limiteMaximoY) {
			// El mapa es más pequeño que la resolución de pantalla: Centrar fijamente
			this.gestionandoY = true;
			this.y = (this.limiteMinimoY + this.limiteMaximoY) / 2;
		} else if (posY <= this.limiteMinimoY) {
			this.gestionandoY = true;
			this.y = this.limiteMinimoY;
		} else if (posY >= this.limiteMaximoY) {
			this.gestionandoY = true;
			this.y = this.limiteMaximoY;
		} else {
			this.gestionandoY = false;
		}
	}

	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;

		int anchoTerreno = Globales.CONSTANTES.ANCHO_JUEGO;
		int altoTerreno = Globales.CONSTANTES.ALTO_JUEGO;

		// Protección contra NullPointerException si el mundo/terreno no está
		// inicializado aún
		if ((e.getMundo() != null) && (e.getMundo().getTerreno() != null)) {
			anchoTerreno = e.getMundo().getTerreno().getAncho();
			altoTerreno = e.getMundo().getTerreno().getAlto();
		}

		final int enteAncho = (e.getArea() != null) ? e.getArea().width : 0;
		final int enteAlto = (e.getArea() != null) ? e.getArea().height : 0;

		this.limiteMinimoX = (Globales.CONSTANTES.ANCHO_JUEGO / 2) - (enteAncho / 2);
		this.limiteMaximoX = anchoTerreno - (Globales.CONSTANTES.ANCHO_JUEGO / 2) - (enteAncho / 2);

		this.limiteMinimoY = (Globales.CONSTANTES.ALTO_JUEGO / 2) - (enteAlto / 2);
		this.limiteMaximoY = altoTerreno - (Globales.CONSTANTES.ALTO_JUEGO / 2) - (enteAlto / 2);
	}

	public void setEntidadEnfocada(final Ente e, final int limiteMaximoX, final int limiteMinimoX,
			final int limiteMaximoY, final int limiteMinimoY, final boolean contarDimensionEnte) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;

		final int enteAncho = (contarDimensionEnte && (e.getArea() != null)) ? e.getArea().width / 2 : 0;
		final int enteAlto = (contarDimensionEnte && (e.getArea() != null)) ? e.getArea().height / 2 : 0;

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

	@Override
	public void eliminar() {
		this.eliminado = true;
		this.gestionandoX = false;
		this.gestionandoY = false;
	}

	public void restituir() {
		this.eliminado = false;
	}

	@Override
	public int getPosicionXInt() {
		return (this.gestionandoX) ? this.x : this.entidadEnfocada.getPosicionXInt();
	}

	@Override
	public int getPosicionYInt() {
		return (this.gestionandoY) ? this.y : this.entidadEnfocada.getPosicionYInt();
	}

	@Override
	public double getPosicionX() {
		return (this.gestionandoX) ? this.x : this.entidadEnfocada.getPosicionX();
	}

	@Override
	public double getPosicionY() {
		return (this.gestionandoY) ? this.y : this.entidadEnfocada.getPosicionY();
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

//	@Override
//	public Rectangle getArea() {
//		if ((this.entidadEnfocada != null) && (this.entidadEnfocada.getArea() != null)) {
//			final Rectangle a = this.entidadEnfocada.getArea();
//			return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), a.width, a.height);
//		}
//		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), 1, 1);
//	}

	@Override
	public int getAncho() {
		return ((this.entidadEnfocada != null) && (this.entidadEnfocada.getArea() != null))
				? this.entidadEnfocada.getArea().width
				: 1;
	}

	@Override
	public int getAlto() {
		return ((this.entidadEnfocada != null) && (this.entidadEnfocada.getArea() != null))
				? this.entidadEnfocada.getArea().height
				: 1;
	}
}