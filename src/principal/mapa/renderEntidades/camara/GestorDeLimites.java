package principal.mapa.renderEntidades.camara;

import java.awt.Rectangle;

import principal.entes.Ente;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;

/**
 * Encapsula el comportamiento de delimitación y restricción de bordes para que
 * la cámara nunca muestre zonas vacías por fuera de los límites del mapa.
 * <p>
 * <b>Arquitectura y Funcionamiento:</b>
 * <ul>
 * <li><b>Ente Virtual Proxy:</b> Esta clase hereda de {@link Ente}. Cuando el
 * personaje enfocado se acerca a los bordes del mapa, el gestor "toma el
 * control" de las coordenadas ({@code gestionandoX = true} o
 * {@code gestionandoY = true}), frenando el avance de la cámara en el borde
 * exacto del terreno mientras el jugador sigue moviéndose libremente.</li>
 * <li><b>Adaptación Dinámica al Zoom:</b> Calcula los límites en tiempo real
 * dividiendo la resolución de pantalla entre el zoom activo, evitando que la
 * cámara se frene antes de tiempo en Zoom-In o muestre el vacío en
 * Zoom-Out.</li>
 * <li><b>Zero-GC:</b> Reutiliza estructuras geométricas internas sin crear
 * objetos {@code new} en cada actualización.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.5
 */
public class GestorDeLimites extends Ente {

	private static final long serialVersionUID = 1L;

	// =========================================================================
	// === 1. COORDENADAS VIRTUALES Y OBJETIVO
	// =========================================================================

	/** Coordenada X virtual donde se frena la cámara. */
	private int x;

	/** Coordenada Y virtual donde se frena la cámara. */
	private int y;

	/**
	 * Entidad real a la que la cámara está siguiendo (Jugador, proyectil, etc.).
	 */
	private Ente entidadEnfocada;

	// =========================================================================
	// === 2. BORDES Y RESTRICCIONES RECTANGULARES DEL MAPA
	// =========================================================================

	private int anchoTerreno;
	private int altoTerreno;
	private boolean limitesPersonalizados;

	private int limiteMaximoX;
	private int limiteMaximoY;
	private int limiteMinimoX;
	private int limiteMinimoY;

	// =========================================================================
	// === 3. ESTADOS DE CONTROL Y FLAGS
	// =========================================================================

	private boolean eliminado;
	private boolean gestionandoX;
	private boolean gestionandoY;

	/** Contenedor de área pre-asignado para consultas sin recolección de basura. */
	private final Rectangle areaReutilizable = new Rectangle();

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Crea el gestor en estado inactivo hasta que se invoque {@link #restituir()} o
	 * {@link #setEntidadEnfocada(Ente)}.
	 */
	public GestorDeLimites() {
		this.eliminado = true; // Por defecto desactivado
		this.limitesPersonalizados = false;
	}

	// =========================================================================
	// === ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	/**
	 * Evalúa si la entidad enfocada ha cruzado los límites visibles del mapa y toma
	 * el control de las coordenadas para frenar la vista.
	 */
	@Override
	public void actualizar() {
		if (this.entidadEnfocada == null) {
			return;
		}

		// Recalculamos los límites dinámicos en función del zoom activo
		this.recalcularLimitesDinamicos();

		final int posX = this.entidadEnfocada.getPosicionXInt();
		final int posY = this.entidadEnfocada.getPosicionYInt();

		/*
		 * =====================================================================
		 * EXPLICACIÓN DIDÁCTICA: CONTROL DEL EJE X
		 * --------------------------------------------------------------------- 1. MAPA
		 * MÁS PEQUEÑO QUE LA PANTALLA (minX > maxX): Si estás en una habitación cerrada
		 * pequeña, la cámara se bloquea automáticamente en el centro exacto del cuarto.
		 * 
		 * 2. BORDE IZQUIERDO (posX <= minX): El jugador llegó al borde izquierdo: la
		 * cámara deja de moverse a la izquierda y se frena en 'limiteMinimoX'.
		 * 
		 * 3. BORDE DERECHO (posX >= maxX): El jugador llegó al borde derecho: la cámara
		 * se frena en 'limiteMaximoX'.
		 * 
		 * 4. ZONA LIBRE (gestionandoX = false): El jugador está en el medio del mapa:
		 * la cámara sigue la posición real del jugador con total libertad.
		 * =====================================================================
		 */
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
			this.gestionandoX = false;
		}

		// --- Control del Eje Y ---
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
			this.gestionandoY = false;
		}
	}

	// =========================================================================
	// === CÁLCULO DE LÍMITES DINÁMICOS CON ZOOM
	// =========================================================================

	/**
	 * Calcula el semiancho y semialto visible en tiempo real según el factor de
	 * Zoom.
	 */
	private void recalcularLimitesDinamicos() {
		if (this.limitesPersonalizados || (this.entidadEnfocada == null)) {
			return;
		}

		final double zoom = (Globales.CAMARA != null) ? Math.max(0.2, Globales.CAMARA.getZoomFinal()) : 1.0;

		final int enteAncho = (this.entidadEnfocada.getArea() != null) ? this.entidadEnfocada.getArea().width : 0;
		final int enteAlto = (this.entidadEnfocada.getArea() != null) ? this.entidadEnfocada.getArea().height : 0;

		// Semidimensiones visibles del cono óptico en píxeles de mundo
		final int semiAnchoVisible = (int) Math.round((Constantes.ANCHO_JUEGO / zoom) / 2.0);
		final int semiAltoVisible = (int) Math.round((Constantes.ALTO_JUEGO / zoom) / 2.0);

		this.limiteMinimoX = semiAnchoVisible - (enteAncho / 2);
		this.limiteMaximoX = this.anchoTerreno - semiAnchoVisible - (enteAncho / 2);

		this.limiteMinimoY = semiAltoVisible - (enteAlto / 2);
		this.limiteMaximoY = this.altoTerreno - semiAltoVisible - (enteAlto / 2);
	}

	// =========================================================================
	// === CONFIGURACIÓN DEL OBJETIVO Y LÍMITES
	// =========================================================================

	/**
	 * Establece la entidad a seguir y obtiene las dimensiones del mapa actual.
	 *
	 * @param e Entidad a enfocar (Jugador, proyectil, asistente cinemático).
	 */
	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;
		this.limitesPersonalizados = false;

		this.anchoTerreno = Constantes.ANCHO_JUEGO;
		this.altoTerreno = Constantes.ALTO_JUEGO;

		// Protección contra NullPointerException si el terreno no está cargado aún
		if ((e.getMundo() != null) && (e.getMundo().getTerreno() != null)) {
			this.anchoTerreno = e.getMundo().getTerreno().getAncho();
			this.altoTerreno = e.getMundo().getTerreno().getAlto();
		}

		this.recalcularLimitesDinamicos();
	}

	/**
	 * Habilita límites rectangulares fijos personalizados (ej: para cinemáticas o
	 * salas cerradas).
	 */
	public void setEntidadEnfocada(final Ente e, final int limiteMaximoX, final int limiteMinimoX,
			final int limiteMaximoY, final int limiteMinimoY, final boolean contarDimensionEnte) {
		if (e == null) {
			return;
		}
		this.entidadEnfocada = e;
		this.limitesPersonalizados = true;

		final int enteAncho = (contarDimensionEnte && (e.getArea() != null)) ? e.getArea().width / 2 : 0;
		final int enteAlto = (contarDimensionEnte && (e.getArea() != null)) ? e.getArea().height / 2 : 0;

		this.limiteMinimoX = limiteMinimoX - enteAncho;
		this.limiteMaximoX = limiteMaximoX - enteAncho;
		this.limiteMinimoY = limiteMinimoY - enteAlto;
		this.limiteMaximoY = limiteMaximoY - enteAlto;
	}

	// =========================================================================
	// === ACCESORES Y COORDENADAS POLIMÓRFICAS (ENTE)
	// =========================================================================

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

	@Override
	public Rectangle getArea() {
		if ((this.entidadEnfocada != null) && (this.entidadEnfocada.getArea() != null)) {
			final Rectangle a = this.entidadEnfocada.getArea();
			this.areaReutilizable.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), a.width, a.height);
			return this.areaReutilizable;
		}
		this.areaReutilizable.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), 1, 1);
		return this.areaReutilizable;
	}

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