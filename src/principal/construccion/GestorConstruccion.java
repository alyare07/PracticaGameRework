package principal.construccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import principal.controles.Raton;
import principal.entes.objetos.items.Consumible;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Gestor maestro de edificación y despliegue holográfico con Smart Placement
 * mediante A*, oclusión DDA anti-paredes y validación fotorreactiva en penumbra
 * y Blackout (Zero-GC / O(1)).
 * 
 * @version 4.1 (Vanilla Java 8 - Pitch-Black & Lighting Validation)
 */
public class GestorConstruccion {

	// =========================================================================
	// === 1. ESTADOS DE CONTROL Y MODOS
	// =========================================================================
	private boolean activo = false;
	private boolean modoDespliegueItem = false;

	// Modo Estructuras
	private TipoEstructura estructuraSeleccionada = TipoEstructura.MURO_MADERA;

	// Modo Ítem Desplegable
	private Consumible itemDesplegableOrigen = null;
	private BufferedImage texturaPreviewPersonalizada = null;
	private AccionColocacion accionColocacionPersonalizada = null;

	// =========================================================================
	// === 2. GEOMETRÍA Y VALIDACIÓN ZERO-GC
	// =========================================================================
	private final Rectangle areaPreview = new Rectangle(0, 0, 16, 16);
	private final Rectangle areaValidacionStand = new Rectangle(0, 0, 8, 8);
	private final Point puntoStandAux = new Point();

	private boolean posicionValida = false;
	private boolean tieneMateriales = false;
	private boolean celdaIluminada = true;

	// Rango táctico realista de contacto manual (2.75 tiles ~ 44 px)
	private static final double RANGO_MAXIMO_CONSTRUCCION = 44.0;
	private static final double RANGO_MAXIMO_SQ = RANGO_MAXIMO_CONSTRUCCION * RANGO_MAXIMO_CONSTRUCCION;

	// =========================================================================
	// === 3. ESTADO DE SMART PLACEMENT (AUTO-CAMINAR CON A*)
	// =========================================================================
	private boolean enCaminoAColocar = false;
	private int pendingSnapX;
	private int pendingSnapY;
	private int pendingAncho;
	private int pendingAlto;

	private final GestorTiempo GT_COLOCACION = new GestorTiempo();
	private static final int COOLDOWN_COLOCACION_MS = 200;

	// Paleta cromática de retroalimentación
	private static final Color COLOR_RANGO = new Color(255, 255, 255, 20);
	private static final Color COLOR_BORDE_VALIDO = new Color(60, 255, 60, 220);
	private static final Color COLOR_BORDE_SMART = new Color(80, 200, 255, 220); // Azul cian: en camino
	private static final Color COLOR_BORDE_INVALIDO = new Color(255, 60, 60, 220);

	public GestorConstruccion() {
	}

	// =========================================================================
	// === API DE ACTIVACIÓN Y DESPLIEGUE UNIVERSAL
	// =========================================================================

	public void iniciarDespliegueItem(final Consumible itemOrigen, final BufferedImage texturaPreview, final int ancho,
			final int alto, final AccionColocacion callbackColocacion) {
		if ((itemOrigen == null) || (texturaPreview == null) || (callbackColocacion == null)) {
			return;
		}

		this.cancelarSmartWalk();
		this.modoDespliegueItem = true;
		this.itemDesplegableOrigen = itemOrigen;
		this.texturaPreviewPersonalizada = texturaPreview;
		this.accionColocacionPersonalizada = callbackColocacion;
		this.areaPreview.setSize(Math.max(16, ancho), Math.max(16, alto));
		this.activo = true;

		this.GT_COLOCACION.establecerReferenciaTiempoActual();

		if ((Globales.GESTOR_INVENTARIO != null) && (Globales.GESTOR_INVENTARIO.getInventarioJugador() != null)) {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().ocultar();
		}
	}

	public void cancelar() {
		this.cancelarSmartWalk();
		this.activo = false;
		this.modoDespliegueItem = false;
		this.itemDesplegableOrigen = null;
		this.texturaPreviewPersonalizada = null;
		this.accionColocacionPersonalizada = null;
	}

	private void cancelarSmartWalk() {
		if (this.enCaminoAColocar) {
			this.enCaminoAColocar = false;
			if (Globales.JUGADOR != null) {
				Globales.JUGADOR.detenerMovimientoPathfinding();
			}
		}
	}

	// =========================================================================
	// === CICLO DE ACTUALIZACIÓN LÓGICA (60 APS)
	// =========================================================================

	public void actualizar(final Raton raton, final Mundo mundo) {
		if (!this.activo || (raton == null) || (mundo == null) || (Globales.JUGADOR == null)) {
			return;
		}

		// 1. Cancelación manual si el jugador toca WASD, Escape o Clic Derecho
		final boolean moviendoManual = Globales.TECLADO.TECLA_ARRIBA.presionado()
				|| Globales.TECLADO.TECLA_ABAJO.presionado() || Globales.TECLADO.TECLA_IZQUIERDA.presionado()
				|| Globales.TECLADO.TECLA_DERECHA.presionado();

		if (moviendoManual || Globales.TECLADO.isTeclaPresionadaUnaVez(KeyEvent.VK_ESCAPE)
				|| (this.GT_COLOCACION.transcurrioMiliSegundos(COOLDOWN_COLOCACION_MS)
						&& raton.presionadoClickDerUnicaAct())) {

			if (!this.enCaminoAColocar) {
				this.cancelar();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				return;
			}
			this.cancelarSmartWalk();
		}

		// 2. Proyección de ratón a mundo y snapping a grilla discreta de 16x16
		final Point pMouse = raton.getPuntoPosicionEscaladoConDesplazamientoCamara();
		final int snapX = Math.floorDiv(pMouse.x, Constantes.LADO_TILE) * Constantes.LADO_TILE;
		final int snapY = Math.floorDiv(pMouse.y, Constantes.LADO_TILE) * Constantes.LADO_TILE;

		final int anchoActual = this.modoDespliegueItem ? this.areaPreview.width
				: this.estructuraSeleccionada.getAncho();
		final int altoActual = this.modoDespliegueItem ? this.areaPreview.height
				: this.estructuraSeleccionada.getAlto();

		this.areaPreview.setBounds(snapX, snapY, anchoActual, altoActual);

		// 3. Validación de Recursos disponibles
		if (this.modoDespliegueItem) {
			this.tieneMateriales = (this.itemDesplegableOrigen != null)
					&& (this.itemDesplegableOrigen.getCantidad() > 0) && !this.itemDesplegableOrigen.estaEliminado();
		} else {
			final int materialDisponible = Globales.GESTOR_INVENTARIO.getInventarioJugador()
					.contarMunicionTotal(this.estructuraSeleccionada.getCodMaterialRequerido());
			this.tieneMateriales = materialDisponible >= this.estructuraSeleccionada.getCantidadMaterialRequerido();
		}

		// 4. Validación de Celda Libre de Colisión
		final boolean libreDeColision = !mundo.colisionaConZonaUObjetoSolido(this.areaPreview)
				&& !mundo.intersectaAlgunaCriatura(this.areaPreview, true)
				&& mundo.getTerreno().areaDentroDelTerreno(this.areaPreview);

		// 5. Validación de Rango, Línea de Visión DDA y Visibilidad Lumínica
		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();
		final double targetCenterX = snapX + (anchoActual / 2.0);
		final double targetCenterY = snapY + (altoActual / 2.0);

		// Verificación lumínica (Blackout / Penumbra): Solo se puede interactuar donde
		// la luz llega
		this.celdaIluminada = (Globales.GESTOR_LUZ == null)
				|| Globales.GESTOR_LUZ.isPosicionIluminada(targetCenterX, targetCenterY);

		final double dx = targetCenterX - jx;
		final double dy = targetCenterY - jy;
		final double distSq = (dx * dx) + (dy * dy);

		final boolean dentroDeRango = distSq <= RANGO_MAXIMO_SQ;
		final boolean lineaVisionLimpia = dentroDeRango
				&& mundo.hayLineaDeTiroLimpia(jx, jy, targetCenterX, targetCenterY);

		this.posicionValida = this.celdaIluminada && dentroDeRango && lineaVisionLimpia && libreDeColision
				&& this.tieneMateriales;

		// =====================================================================
		// 6. EJECUCIÓN CONTINUA DE SMART PLACEMENT EN TRÁNSITO
		// =====================================================================
		if (this.enCaminoAColocar) {
			final double targetPendingCenterX = this.pendingSnapX + (this.pendingAncho / 2.0);
			final double targetPendingCenterY = this.pendingSnapY + (this.pendingAlto / 2.0);

			// Comprueba si la casilla sigue visible
			final boolean sigueVisible = (Globales.GESTOR_LUZ == null)
					|| Globales.GESTOR_LUZ.isPosicionIluminada(targetPendingCenterX, targetPendingCenterY);

			if (!sigueVisible) {
				Globales.GESTOR_TEXTOS.agregarTexto("¡Demasiado oscuro!", jx, jy - 10,
						principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				this.cancelarSmartWalk();
				return;
			}

			final double pdx = targetPendingCenterX - jx;
			final double pdy = targetPendingCenterY - jy;
			final double pDistSq = (pdx * pdx) + (pdy * pdy);

			final boolean enRangoLlegada = pDistSq <= RANGO_MAXIMO_SQ;
			final boolean lineaLlegadaLimpia = enRangoLlegada
					&& mundo.hayLineaDeTiroLimpia(jx, jy, targetPendingCenterX, targetPendingCenterY);

			if (lineaLlegadaLimpia) {
				this.areaPreview.setBounds(this.pendingSnapX, this.pendingSnapY, this.pendingAncho, this.pendingAlto);

				final boolean sigueLibre = !mundo.colisionaConZonaUObjetoSolido(this.areaPreview)
						&& !mundo.intersectaAlgunaCriatura(this.areaPreview, true);

				if (sigueLibre && this.tieneMateriales) {
					this.ejecutarColocacion(this.pendingSnapX, this.pendingSnapY, mundo);
				} else {
					Globales.GESTOR_TEXTOS.agregarTexto("¡Espacio bloqueado!", jx, jy - 10,
							principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
					GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				}
				this.cancelarSmartWalk();
			}
		}

		// =====================================================================
		// 7. GESTIÓN DE CLIC IZQUIERDO: DIRECTO O SMART A*
		// =====================================================================
		if (raton.presionadoClickIzq() && this.GT_COLOCACION.transcurrioMiliSegundos(COOLDOWN_COLOCACION_MS)) {
			this.GT_COLOCACION.establecerReferenciaTiempoActual();

			// A. Bloqueo por Oscuridad Total
			if (!this.celdaIluminada) {
				Globales.GESTOR_TEXTOS.agregarTexto("¡Demasiado oscuro!", jx, jy - 10,
						principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				return;
			}

			if (!libreDeColision || !this.tieneMateriales) {
				GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				return;
			}

			// B. Colocación Directa Inmediata
			if (this.posicionValida) {
				this.ejecutarColocacion(snapX, snapY, mundo);
				this.cancelarSmartWalk();
			} else {
				// C. Smart Placement: Auto-aproximación con A*
				final Point puntoStand = this.calcularPuntoAproximacion(mundo, snapX, snapY, anchoActual, altoActual);

				if (puntoStand != null) {
					this.pendingSnapX = snapX;
					this.pendingSnapY = snapY;
					this.pendingAncho = anchoActual;
					this.pendingAlto = altoActual;
					this.enCaminoAColocar = true;

					Globales.JUGADOR.calcularRutaAEstrella(puntoStand.x + 8, puntoStand.y + 8);
				} else {
					Globales.GESTOR_TEXTOS.agregarTexto("¡Inaccesible!", jx, jy - 10,
							principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
					GestorSonido.reproducir(IDSonido.SIN_MUNICION);
				}
			}
		}
	}

	private void ejecutarColocacion(final int x, final int y, final Mundo mundo) {
		if (this.modoDespliegueItem) {
			if (this.accionColocacionPersonalizada != null) {
				this.accionColocacionPersonalizada.colocar(x, y, mundo);
			}

			this.itemDesplegableOrigen.reducirCantidad(1);

			if (this.itemDesplegableOrigen.getCantidad() <= 0) {
				this.cancelar();
			}
		} else {
			Globales.GESTOR_INVENTARIO.getInventarioJugador().extraerMunicion(
					this.estructuraSeleccionada.getCodMaterialRequerido(),
					this.estructuraSeleccionada.getCantidadMaterialRequerido());

			final EstructuraConstruible nuevaEstructura = new EstructuraConstruible(x, y, this.estructuraSeleccionada);
			mundo.meterEntidad(nuevaEstructura);
		}

		mundo.notificarModificacionEstructura();
		GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, x, y, Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
				Globales.CAMARA.getEntidadEnfocada().getPosicionY());
	}

	/**
	 * Escanea las casillas transitables e iluminadas adyacentes al objetivo para
	 * calcular la mejor posición de aproximación (Zero-GC / O(1)).
	 */
	private Point calcularPuntoAproximacion(final Mundo mundo, final int targetX, final int targetY, final int targetW,
			final int targetH) {

		final double jx = Globales.JUGADOR.getCentroX();
		final double jy = Globales.JUGADOR.getCentroY();
		final double targetCenterX = targetX + (targetW / 2.0);
		final double targetCenterY = targetY + (targetH / 2.0);

		Point mejorPunto = null;
		double menorDistanciaSq = Double.MAX_VALUE;
		final int lado = Constantes.LADO_TILE;

		for (int dy = -lado; dy <= lado; dy += lado) {
			for (int dx = -lado; dx <= lado; dx += lado) {
				if ((dx == 0) && (dy == 0)) {
					continue;
				}

				final int standX = targetX + dx;
				final int standY = targetY + dy;

				this.areaValidacionStand.setBounds(standX + 4, standY + 10, 8, 6);

				if (mundo.getTerreno().areaDentroDelTerreno(this.areaValidacionStand)
						&& !mundo.colisionaConZonaUObjetoSolido(this.areaValidacionStand) && mundo.hayLineaDeTiroLimpia(
								standX + (lado / 2.0), standY + (lado / 2.0), targetCenterX, targetCenterY)) {

					final double distAlJugadorSq = Math.pow((standX + (lado / 2.0)) - jx, 2)
							+ Math.pow((standY + (lado / 2.0)) - jy, 2);

					if (distAlJugadorSq < menorDistanciaSq) {
						menorDistanciaSq = distAlJugadorSq;
						this.puntoStandAux.setLocation(standX, standY);
						mejorPunto = this.puntoStandAux;
					}
				}
			}
		}

		return mejorPunto;
	}

	// =========================================================================
	// === RENDERIZADO DEL HOLOGRAMA (60 FPS)
	// =========================================================================

	public void pintar(final Graphics2D g) {
		if (!this.activo) {
			return;
		}

		final int x = this.areaPreview.x;
		final int y = this.areaPreview.y;
		final int w = this.areaPreview.width;
		final int h = this.areaPreview.height;

		// 1. Dibuja el radio de alcance manual alrededor del jugador
		final int rMax = (int) RANGO_MAXIMO_CONSTRUCCION;
		final int jx = Globales.JUGADOR.getCentroX();
		final int jy = Globales.JUGADOR.getCentroY();
		Render2D.dibujarFiguraEllipseRefCamara(g, jx - rMax, jy - rMax, rMax * 2, rMax * 2, COLOR_RANGO);

		// 2. Dibuja la silueta holográfica (se atenúa fuertemente si está en penumbra
		// absoluta)
		final BufferedImage textura = this.modoDespliegueItem ? this.texturaPreviewPersonalizada
				: (this.estructuraSeleccionada != null ? this.estructuraSeleccionada.getTextura() : null);

		if (textura != null) {
			final float opacidad = this.celdaIluminada ? 0.65f : 0.15f;
			Render2D.dibujarImagenConTransparenciaRefCamara(g, textura, x, y, opacidad);
		}

		// 3. Color del marco: Verde (Válido) / Azul Cian (Smart Pathfinding en camino)
		// / Rojo (Inválido u Oscuro)
		Color colorBorde = (this.posicionValida && this.celdaIluminada) ? COLOR_BORDE_VALIDO : COLOR_BORDE_INVALIDO;
		if (this.enCaminoAColocar && (x == this.pendingSnapX) && (y == this.pendingSnapY)) {
			colorBorde = COLOR_BORDE_SMART;
		}

		Render2D.dibujarRectanguloContornoRefCamara(g, x, y, w, h, colorBorde);

		// 4. Marca visual de destino pendiente si está caminando
		if (this.enCaminoAColocar) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.pendingSnapX, this.pendingSnapY, this.pendingAncho,
					this.pendingAlto, COLOR_BORDE_SMART);
		}
	}

	// =========================================================================
	// === GETTERS Y SETTERS
	// =========================================================================

	public boolean isActivo() {
		return this.activo;
	}

	public void setActivo(final boolean activo) {
		this.activo = activo;
		if (!activo) {
			this.cancelar();
		}
	}

	public void conmutarModoConstruccion() {
		if (this.activo) {
			this.cancelar();
		} else {
			this.modoDespliegueItem = false;
			this.activo = true;
		}
	}

	public TipoEstructura getEstructuraSeleccionada() {
		return this.estructuraSeleccionada;
	}

	public void setEstructuraSeleccionada(final TipoEstructura tipo) {
		if (tipo != null) {
			this.modoDespliegueItem = false;
			this.estructuraSeleccionada = tipo;
			this.areaPreview.setSize(tipo.getAncho(), tipo.getAlto());
		}
	}
}