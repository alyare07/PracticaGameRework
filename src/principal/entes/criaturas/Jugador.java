package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONObject;

import principal.animaciones.Animaciones;
import principal.entes.Ente;
import principal.entes.modelos.tile.ListaModeloTile;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.armas.Desarmado;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.entes.proyectil.GolpeMele;
import principal.ia.Lista;
import principal.ia.aEstrella.NodoA;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;

/**
 * Representa al personaje principal controlado por el usuario.
 * <p>
 * <b>Responsabilidades del Jugador:</b>
 * <ul>
 * <li><b>Entrada y Movimiento Híbrido:</b> Soporta control reactivo por teclado
 * (WASD/Flechas) y navegación automática guiada por clic de ratón mediante
 * Dijkstra masivo o A*.</li>
 * <li><b>Sistema de Estamina y Físicas:</b> Regula el consumo al correr y la
 * regeneración según el estado de reposo/caminata, aplicando modificadores de
 * velocidad por el tipo de terreno pisado.</li>
 * <li><b>Combate e Interacciones:</b> Administra armas equipadas
 * (melee/distancia), arrojadizos, recolección de ítems por área de proximidad
 * elíptica e interacción con cofres.</li>
 * <li><b>Cero Asignaciones en Bucle (GC-Friendly):</b> Reutiliza estructuras
 * geométricas auxiliares ({@link Rectangle2D}, {@link Rectangle},
 * {@link Point}) para evitar pausas del Garbage Collector.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Jugador extends Criatura {

	// =========================================================================
	// === CENTRADO DE CÁMARA Y DESPLAZAMIENTO
	// =========================================================================

	/**
	 * Coordenada X fija de centrado en pantalla para el renderizado relativo a la
	 * cámara.
	 */
	protected final int MARGENX;

	/**
	 * Coordenada Y fija de centrado en pantalla para el renderizado relativo a la
	 * cámara.
	 */
	protected final int MARGENY;

	/** Desplazamiento acumulado en el eje X para la interpolación de la cámara. */
	protected double desplazamientoX;

	/** Desplazamiento acumulado en el eje Y para la interpolación de la cámara. */
	protected double desplazamientoY;

	/**
	 * Referencia al {@link Tile} exacto que el jugador está pisando en el tick
	 * actual.
	 */
	protected Tile tilePisado;

	// =========================================================================
	// === COMBATE, ESTADÍSTICAS Y TEMPORIZADORES
	// =========================================================================

	/** Daño base infligido en ataques físicos cuerpo a cuerpo. */
	private double damage;

	/** Temporizador para controlar el tiempo de recarga entre ataques sucesivos. */
	private final GestorTiempo GT_ULTIMO_ATAQUE;

	/**
	 * Temporizador para gestionar el retardo previo a la regeneración de estamina.
	 */
	private final GestorTiempo GT_RECUPERACION_ESTAMINA;

	/** Tiempo mínimo de espera en milisegundos entre ataques. */
	private static final int TIEMPO_MS_ESPERA_POR_ATAQUE = 600;

	/** Duración visual en milisegundos de la animación del ataque. */
	private static final int TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE = TIEMPO_MS_ESPERA_POR_ATAQUE / 2;

	/**
	 * Tiempo de espera sin recibir daño para activar la regeneración pasiva de
	 * vida.
	 */
	private static final int TIEMPO_MS_ESPERA_REGEN_VIDA = 5000;

	/** Tiempo de espera sin correr para iniciar la recuperación de estamina. */
	private static final int TIEMPO_MS_ESPERA_REGEN_ESTAMINA = 2500;

	/**
	 * Bandera que indica si la animación de ataque debe permanecer visible este
	 * frame.
	 */
	private boolean dibujarAtaque;

	// =========================================================================
	// === RECOLECCIÓN Y SENSORES ESPACIALES
	// =========================================================================

	/**
	 * Área elíptica reutilizable para la detección y recogida automática de ítems
	 * en el suelo.
	 */
	protected final Shape areaRecoleccion = new Ellipse2D.Double();

	/** Diámetro en píxeles del radio de recolección de ítems. */
	protected final int recoleccionLado = 50;

	/** Vida máxima base del personaje. */
	protected final double PTS_VIDAMAX_BASE = 100;

	/** Daño base desarmado. */
	protected final double PTS_DAMAGE_BASE = 5;

	/** Puntos de estamina actuales. */
	protected double estamina;

	/** Capacidad máxima de estamina. */
	protected double maxEstamina;

	/** Tasa de recuperación de estamina por segundo. */
	protected double puntoRecuperarEstaminaXseg;

	/** Tasa de consumo de estamina por segundo al correr. */
	protected double puntoGastarEstaminaXseg;

	/** Costo mínimo de estamina consumido por ciclo al esprintar. */
	protected final float PTS_CONSUMIR_ESTAMINA = 0.5f;

	/** Margen horizontal de la caja de interacción con cofres. */
	protected final int ANCHO_INTERACCION_COFRE;

	/** Margen vertical de la caja de interacción con cofres. */
	protected final int ALTO_INTERACCION_COFRE;

	// =========================================================================
	// === NAVEGACIÓN POR RUTA (DIJKSTRA / A*)
	// =========================================================================

	/**
	 * Instancia local del algoritmo Dijkstra masivo adaptado a las dimensiones del
	 * jugador.
	 */
	protected DijkstraRework DIJKSTRA;

	/** Nodo destino actual en la navegación por Dijkstra. */
	protected NodoD nodoDDestino;

	/** Lista secuencial de nodos que componen el camino activo de Dijkstra. */
	protected Lista<NodoD> recorridoD;

	/**
	 * Bandera de espera mientras el hilo secundario de Dijkstra resuelve la ruta.
	 */
	private boolean generarRecorridoMoverMouse;

	/**
	 * Bandera que indica si el jugador se está desplazando automáticamente por una
	 * ruta.
	 */
	private boolean moviendoPorRecorrido;

	// =========================================================================
	// === ESTRUCTURAS AUXILIARES REUTILIZABLES (ZERO-GC)
	// =========================================================================

	private final HashSet<Ente> CHECK_LIST_DEBUG = new HashSet<Ente>();
	private final Rectangle2D AREA_INTERSECCION_MOVIMIENTO_AUXILIAR = new Rectangle2D.Double(0, 0, 0, 0);
	private final Rectangle RECTANGLE_AUXILIAR = new Rectangle();
	private final Point PUNTO_AUXILIAR = new Point();

	// =========================================================================
	// === CONSTRUCTOR
	// =========================================================================

	/**
	 * Construye e inicializa al jugador en las coordenadas especificadas del mundo.
	 *
	 * @param x Coordenada X inicial en píxeles.
	 * @param y Coordenada Y inicial en píxeles.
	 */
	public Jugador(final int x, final int y) {
		super(x, y, 12, 20, 50, 50);

		final int anchoSprite = 32;
		final int altoSprite = 32;
		this.MARGENX = Constantes.CENTROX - (anchoSprite / 2);
		this.MARGENY = Constantes.CENTROY - (altoSprite / 2);

		this.establecerVidaMaxima(this.PTS_VIDAMAX_BASE);
		this.damage = this.PTS_DAMAGE_BASE;
		this.velocidadEstandar = 0.5;

		this.GT_ULTIMO_ATAQUE = new GestorTiempo();
		this.GT_RECUPERACION_ESTAMINA = new GestorTiempo();
		this.dibujarAtaque = false;

		this.actualizarAreaRecoleccion();
		this.maxEstamina = 30;
		this.estamina = this.maxEstamina;
		this.puntoRecuperarEstaminaXseg = 5;
		this.puntoGastarEstaminaXseg = 5;

		this.ANCHO_INTERACCION_COFRE = this.ANCHO + 2;
		this.ALTO_INTERACCION_COFRE = this.ALTO + 2;
	}

	// =========================================================================
	// === BUCLE DE ACTUALIZACIÓN LÓGICA (TICK)
	// =========================================================================

	@Override
	public void actualizar() {
		// Interacción con ratón en modo debug (enfoque de cámara o curación rápida)
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if (Globales.RATON.presionadoClickIzqUnicaAct()) {
				Globales.CAMARA.setEntidadEnfocada(this);
			} else if (Globales.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Globales.JUGADOR.getDamage());
			}
		}

		if (this.eliminado) {
			return;
		}

		if (Globales.RATON.presionadoClickIzqUnicaAct()
				&& Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			this.curar();
		}
		// Detección del tile pisado en la base de los pies para modificadores de
		// velocidad
		if (this.mundo != null) {
			final Terreno terreno = this.mundo.getTerreno();
			final Shape s = this.getAreaInterseccionMovimiento();
			this.tilePisado = terreno.getTileReferenciado(s.getBounds().x + (s.getBounds().width / 2),
					s.getBounds().y + s.getBounds().height);
		}

		this.actualizarMovimientoMouseDijkstra();
		this.actualizarMovimientoMouseAEstrella();
		this.actualizarMovimientos();
		this.actualizarRecogidaItems();
		this.actualizarArrojar();
		this.actualizarAtaque();
	}

	// =========================================================================
	// === NAVEGACIÓN Y PATHFINDING POR RATÓN
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: NAVEGACIÓN POR RATÓN CON DIJKSTRA
	 * ------------------------------------------------------------------------- 1.
	 * Al presionar clic derecho en modo debug: - Proyecta la posición del ratón al
	 * mundo. - Verifica colisión contra sólidos en el destino usando
	 * RECTANGLE_AUXILIAR. - Si el hilo secundario de Dijkstra no está bloqueado,
	 * obtiene la lista de nodos. 2. En cada tick, interpola la posición hacia
	 * 'nodoDDestino'. Al alcanzarlo, avanza al siguiente nodo de 'recorridoD' hasta
	 * llegar a la meta.
	 * =========================================================================
	 */
	private void actualizarMovimientoMouseDijkstra() {
		if ((this.recorridoD == null) || Globales.RATON.presionadoClickDerUnicaAct()) {
			if (this.generarRecorridoMoverMouse && !Globales.RATON.presionadoClickDerUnicaAct()) {
				if (!this.DIJKSTRA.isActualizando()) {
					final NodoD nodoParado = this.DIJKSTRA.getNodoReferenciado(this.getPosicionXInt(),
							this.getPosicionYInt());
					if (nodoParado != null) {
						this.recorridoD = this.DIJKSTRA.getRecorrido(nodoParado);
						this.nodoDDestino = (this.recorridoD != null) ? this.recorridoD.getNext() : null;
						if ((this.nodoDDestino == null) || this.recorridoD.isEmpty()) {
							this.recorridoD = null;
							this.nodoDDestino = null;
							this.moviendoPorRecorrido = false;
						}
					}
					this.generarRecorridoMoverMouse = false;
				}
				return;
			}

			if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.RATON.presionadoClickDerUnicaAct()) {
				if (this.moviendoPorRecorrido) {
					this.nodoDDestino = null;
					this.recorridoD = null;
					this.moviendoPorRecorrido = false;
				}

				final Point p = Globales.RATON.getPuntoPosicionEscaladoConDesplazamientoCamara();
				if (!this.mundo.getTerreno()
						.areaDentroDelTerreno(Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara())) {
					return;
				}

				this.DIJKSTRA.actualizar(p);
				final NodoD n = this.DIJKSTRA.getNodoReferenciado(p.x, p.y);
				if (n == null) {
					return;
				}

				this.RECTANGLE_AUXILIAR.setBounds(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto());
				if (this.mundo.colisionaConZonaUObjetoSolido(this.RECTANGLE_AUXILIAR)) {
					return;
				}

				if (!this.DIJKSTRA.isActualizando()) {
					final NodoD nodoParado = this.DIJKSTRA.getNodoReferenciado(this.getPosicionXInt(),
							this.getPosicionYInt());
					if (nodoParado != null) {
						this.recorridoD = this.DIJKSTRA.getRecorrido(nodoParado);
						this.nodoDDestino = (this.recorridoD != null) ? this.recorridoD.getNext() : null;
						if ((this.nodoDDestino == null) || this.recorridoD.isEmpty()) {
							this.recorridoD = null;
							this.nodoDDestino = null;
							this.moviendoPorRecorrido = false;
						}
					}
				} else {
					this.generarRecorridoMoverMouse = true;
				}
			}
		} else {
			if (!this.moviendoPorRecorrido && (this.nodoDDestino == null)) {
				this.recorridoD = null;
				return;
			}

			this.establecerVelocidadStardar();
			if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					this.meterEstado(Estado.CORRIENDO);
					this.removerEstado(Estado.ESTANDAR);
					this.removerEstado(Estado.CAMINANDO);
				}
			} else {
				this.recuperarEstamina();
			}

			if (this.tilePisado != null) {
				this.velocidad = Math.max(0, this.velocidad
						+ ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
			}

			this.moverANodoDDestino();

			if ((this.nodoDDestino == this.recorridoD.getLast())
					&& (this.nodoDDestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt()))) {
				this.recorridoD = null;
				this.nodoDDestino = null;
				this.moviendoPorRecorrido = false;
				this.setEstadoEstandar();
			} else if (!this.moviendoPorRecorrido) {
				this.moviendoPorRecorrido = true;
				this.setEstadoUnico(Estado.CAMINANDO);
			}
		}
	}

	private void actualizarMovimientoMouseAEstrella() {
		if ((this.recorridoA == null) || Globales.RATON.presionadoClickDerUnicaAct()) {
			if (Globales.TECLADO.TECLA_DEBUG_TILE_INFO.presionado() && Globales.RATON.presionadoClickDerUnicaAct()) {
				final Point p = Globales.RATON.getPuntoPosicionEscaladoConDesplazamientoCamara();
				if (!this.mundo.getTerreno()
						.areaDentroDelTerreno(Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara())) {
					return;
				}

				final NodoA n = this.getMundo().getAEstrellaX12X20().getNodoRef(p.x, p.y);
				if (n == null) {
					return;
				}

				final int wNodo = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width;
				final int hNodo = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height;
				this.RECTANGLE_AUXILIAR.setBounds(n.getXNodo() * wNodo, n.getYNodo() * hNodo, wNodo, hNodo);

				if (this.mundo.colisionaConZonaUObjetoSolido(this.RECTANGLE_AUXILIAR)) {
					return;
				}

				this.getMundo().getAEstrellaX12X20().getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(), p.x,
						p.y, this.recorridoA);
				if ((this.recorridoA == null) || this.recorridoA.isEmpty()) {
					this.nodoADestino = null;
					return;
				}

				this.nodoADestino = this.recorridoA.poll();
				if ((this.nodoADestino == null) || this.recorridoA.isEmpty()) {
					this.nodoADestino = null;
					this.moviendoPorRecorrido = false;
				}
			}
		} else {
			if (!this.moviendoPorRecorrido && (this.nodoADestino == null)) {
				return;
			}

			if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
				return;
			}

			this.establecerVelocidadStardar();
			if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					this.setEstadoCorriendo();
				}
			} else {
				this.recuperarEstamina();
			}

			if (this.tilePisado != null) {
				this.velocidad = Math.max(0, this.velocidad
						+ ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
			}

			this.moverANodoADestino();

			if (!this.recorridoA.isEmpty() && (this.nodoADestino == this.recorridoA.getLast())
					&& (this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt()))) {
				this.moviendoPorRecorrido = false;
				this.nodoADestino = null;
				this.setEstadoEstandar();
				this.recorridoA.clear();
			} else if (!this.moviendoPorRecorrido) {
				this.moviendoPorRecorrido = true;
				this.setEstadoCaminando();
			}
		}
	}

	/**
	 * Interpola las coordenadas del jugador hacia el nodo de destino actual de
	 * Dijkstra.
	 */
	protected void moverANodoDDestino() {
		if (this.nodoDDestino == null) {
			return;
		}
		this.RECTANGLE_AUXILIAR.setBounds(this.nodoDDestino.getXMundo(), this.nodoDDestino.getYMundo(),
				this.nodoDDestino.getAncho(), this.nodoDDestino.getAlto());

		if (this.getPosicionYInt() < this.RECTANGLE_AUXILIAR.y) {
			final double dist = this.RECTANGLE_AUXILIAR.y - this.getPosicionYInt();
			this.y = (dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.y : this.y + Math.min(dist, this.velocidad);
			this.direccion = Direccion.SUR;
		} else if (this.getPosicionYInt() > this.RECTANGLE_AUXILIAR.y) {
			final double dist = this.getPosicionYInt() - this.RECTANGLE_AUXILIAR.y;
			this.y = (dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.y : this.y - Math.min(dist, this.velocidad);
			this.direccion = Direccion.NORTE;
		}

		if (this.getPosicionXInt() < this.RECTANGLE_AUXILIAR.x) {
			final double dist = this.RECTANGLE_AUXILIAR.x - this.getPosicionXInt();
			this.x = (dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.x : this.x + Math.min(dist, this.velocidad);
			this.direccion = Direccion.ESTE;
		} else if (this.getPosicionXInt() > this.RECTANGLE_AUXILIAR.x) {
			final double dist = this.getPosicionXInt() - this.RECTANGLE_AUXILIAR.x;
			this.x = (dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.x : this.x - Math.min(dist, this.velocidad);
			this.direccion = Direccion.OESTE;
		}

		if (this.nodoDDestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())
				&& (this.getPosicionXInt() == this.RECTANGLE_AUXILIAR.x)
				&& (this.getPosicionYInt() == this.RECTANGLE_AUXILIAR.y)) {
			if ((this.recorridoD != null) && this.recorridoD.hasNext()) {
				this.nodoDDestino = this.recorridoD.getNext();
			}
		}
	}

	// =========================================================================
	// === MOVIMIENTO MANUAL POR TECLADO Y FÍSICAS
	// =========================================================================

	/**
	 * Procesa la entrada continua del teclado (WASD / Flechas), comprobando
	 * colisiones proyectadas contra zonas sólidas y objetos del mapa antes de
	 * aplicar el desplazamiento.
	 */
	private void actualizarMovimientos() {
		boolean enMovimiento = false;
		boolean corriendo = false;

		this.establecerVelocidadStardar();

		if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
			if (Globales.TECLADO.TECLA_ARRIBA.presionado() || Globales.TECLADO.TECLA_ABAJO.presionado()
					|| Globales.TECLADO.TECLA_DERECHA.presionado() || Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 3.5;
					corriendo = true;
				}
			}
		} else {
			this.recuperarEstamina();
		}

		// Modificador de velocidad del terreno actual
		if (this.tilePisado != null) {
			this.velocidad = Math.max(0, this.velocidad
					+ ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
		}

		// Movimiento direccional con comprobación de colisión proyectada
		if (Globales.TECLADO.TECLA_ARRIBA.presionado()) {
			if ((((int) (this.y - this.velocidad)) >= 0) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 2))) {
				this.modificarPosicionY(-this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.NORTE;
		}

		if (Globales.TECLADO.TECLA_ABAJO.presionado()) {
			if (((this.y + this.velocidad) <= (this.mundo.getTerreno().getAlto() - this.ALTO)) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 3))) {
				this.modificarPosicionY(this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.SUR;
		}

		if (Globales.TECLADO.TECLA_IZQUIERDA.presionado()) {
			if (((this.x - this.velocidad) >= 0) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, -1))) {
				this.modificarPosicionX(-this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.OESTE;
		}

		if (Globales.TECLADO.TECLA_DERECHA.presionado()) {
			if (((this.x + this.velocidad) <= (this.mundo.getTerreno().getAncho() - this.ANCHO)) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 1))) {
				this.modificarPosicionX(this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.ESTE;
		}

		this.atrasDeComplemento = this.mundo
				.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getAreaInterseccionMovimiento());

		// Interrupción de ruta automática si el usuario presiona el teclado
		if (enMovimiento && this.moviendoPorRecorrido) {
			this.moviendoPorRecorrido = false;
			this.recorridoD = null;
			this.nodoDDestino = null;
			if (this.recorridoA.size() > 0) {
				this.recorridoA.clear();
			}
			this.nodoADestino = null;
		}

		// Actualización de estados visuales
		if (corriendo) {
			this.setEstadoCorriendo();
		} else {
			this.removerEstado(Estado.CORRIENDO);
		}

		if (!enMovimiento) {
			if (this.moviendoPorRecorrido) {
				if (!this.estaEstadoCorriendo()) {
					this.setEstadoCaminando();
				}
			} else {
				this.setEstadoEstandar();
			}
		} else if (!this.estaEstadoCorriendo()) {
			this.setEstadoCaminando();
		}
	}

	// =========================================================================
	// === GESTIÓN DE COMBATE, ACCIONES Y RECOLECCIÓN
	// =========================================================================

	/**
	 * Controla la lógica de ataque continuo o individual, verificando recarga y
	 * armas.
	 */
	private void actualizarAtaque() {
		if (!Globales.TECLADO.TECLA_ATACANDO.presionado() && this.estaEstadoAtacando()
				&& this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			this.removerEstado(Estado.ATACANDO);
		}

		if (!Globales.TECLADO.TECLA_ATACANDO.presionado() || this.tieneEstado(Estado.ARROJANDO)) {
			if (this.dibujarAtaque
					&& this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE)) {
				this.dibujarAtaque = false;
			}
			return;
		}

		if (this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			this.GT_ULTIMO_ATAQUE.establecerReferenciaTiempoActual();
			this.dibujarAtaque = true;
			this.meterEstado(Estado.ATACANDO);
			this.realizarAtaque(this.mundo);
		} else if (this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE)) {
			this.dibujarAtaque = false;
		}
	}

	/**
	 * Ejecuta el ataque físico o a distancia según el tipo de arma equipada.
	 *
	 * @param mundo Referencia al mundo activo donde spawnear proyectiles o
	 *              colisiones.
	 */
	private void realizarAtaque(final Mundo mundo) {
		final Arma armaEquipada = this.getArmaEquipada();

		if (armaEquipada instanceof Pistola) {
			final Pistola pistola = (Pistola) armaEquipada;
			final int offsetX = (this.direccion == Direccion.OESTE) ? -8 : 8;
			final int offsetY = (this.direccion == Direccion.NORTE) ? -8 : 8;
			pistola.disparar((int) this.x + offsetX, (int) this.y + offsetY, this.direccion, mundo, this, false);
		} else if (armaEquipada instanceof Desarmado) {
			this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
		}
	}

	/**
	 * Genera un proyectil cuerpo a cuerpo direccional con alcance corto.
	 */
	private void ataqueMele(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo mundo) {
		final int alcanceAtaque = 12;
		final int anchoAtaque = 4;

		switch (direccion) {
		case OESTE:
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - alcanceAtaque, yOrigen,
					alcanceAtaque, anchoAtaque, direccion, this));
			break;
		case ESTE:
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen, yOrigen, alcanceAtaque, anchoAtaque,
					direccion, this));
			break;
		case NORTE:
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - (anchoAtaque / 2),
					yOrigen - alcanceAtaque, anchoAtaque, alcanceAtaque, direccion, this));
			break;
		case SUR:
		default:
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - (anchoAtaque / 2), yOrigen,
					anchoAtaque, alcanceAtaque, direccion, this));
			break;
		}
	}

	/**
	 * Escanea el área elíptica de proximidad y recoge los ítems del suelo
	 * añadiéndolos al inventario.
	 */
	private void actualizarRecogidaItems() {
		if (!Globales.TECLADO.TECLA_RECOGIENDO.presionado() || (this.tilePisado == null)) {
			return;
		}
		if (!Globales.TECLEO_RECOGIDA.transcurrioMiliSegundos(300)) {
			return;
		}

		Globales.TECLEO_RECOGIDA.establecerReferenciaTiempoActual();
		this.actualizarAreaRecoleccion();

		for (final Item item : this.mundo.getItemsIntersectados(this.areaRecoleccion)) {
			if (Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(item)) {
				if (item instanceof Consumible) {
					if (((Consumible) item).getCantidad() == 0) {
						item.eliminar();
					}
				} else {
					item.eliminar();
				}
			}
		}
	}

	/**
	 * Administra el apuntado y lanzamiento de ítems arrojadizos (granadas, piedras,
	 * etc.).
	 */
	private void actualizarArrojar() {
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()) {
			this.meterEstado(Estado.ARROJANDO);
			if (Globales.RATON.presionadoClickIzqUnicaAct()) {
				final Rectangle areaRaton = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
				final Arrojadizo item = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo()
						.getItemArrojadizo();
				item.arrojar(areaRaton.x, areaRaton.y, this.direccion, this.mundo, this, false);
				Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().eliminarObjeto();
			} else if (Globales.RATON.presionadoClickDerUnicaAct()) {
				Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().eliminarObjeto();
			}
		}

		if (!Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()
				&& this.tieneEstado(Estado.ARROJANDO)) {
			this.removerEstado(Estado.ARROJANDO);
		}
	}

	private void curar() {
		// 1. Al explotar una granada o bola de fuego:
//		Globales.PARTICULAS.emitirExplosion(this.getPosicionX(), this.getPosicionY(), 40);

//		// 2. Al asestar un golpe crítico o recibir daño:
//		Globales.PARTICULAS.emitirSangre(this.getPosicionX(), this.getPosicionY(), -1, 1, 3);
//
		// 3. Al caminar o hacer un Dash:
//		Globales.PARTICULAS.emitirPolvoPaso(this.getPosicionX(), this.getPosicionY(), 5);
//
//		// 4. Al tomar una poción o usar magia:
		Globales.PARTICULAS.emitirMagia(this.getCentroX(), this.getCentroY(), 15);

		if (this.vida >= this.vidaMaxima) {
			return;
		}

		if (this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA)) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	// =========================================================================
	// === GESTIÓN DE ESTAMINA
	// =========================================================================

	/*
	 * =========================================================================
	 * EXPLICACIÓN TÉCNICA: CICLO DE ESTAMINA Y REGENERACIÓN POR ESTADOS
	 * ------------------------------------------------------------------------- -
	 * Gasto: Se valida si queda suficiente estamina para el tick actual. Al correr,
	 * reinicia el temporizador 'GT_RECUPERACION_ESTAMINA'. - Recuperación: Requiere
	 * un período de reposo de 2.5s sin esprintar. La tasa de regeneración varía: -
	 * Caminando: Tasa reducida al 50%. - Parado (>50% estamina): Tasa acelerada al
	 * 150%. - Parado (agotado): Tasa base 100%.
	 * =========================================================================
	 */
	private boolean gastarEstamina() {
		if ((this.estamina - this.PTS_CONSUMIR_ESTAMINA) > 0) {
			if (this.estamina < (this.puntoGastarEstaminaXseg / 60.0)) {
				this.estamina = 0;
				return false;
			}
			this.GT_RECUPERACION_ESTAMINA.establecerReferenciaTiempoActual();
			return true;
		}
		return false;
	}

	private void recuperarEstamina() {
		if (!this.estaEstadoCorriendo()
				&& this.GT_RECUPERACION_ESTAMINA.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_ESTAMINA)) {

			if ((this.estamina < this.maxEstamina)
					&& ((this.estamina + this.puntoRecuperarEstaminaXseg) <= this.maxEstamina)) {
				if (this.estaEstadoCaminando()) {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60.0) / 2.0;
				} else if (this.estamina >= (this.maxEstamina / 2.0)) {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60.0) * 1.5;
				} else {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60.0);
				}
			} else {
				this.estamina = this.maxEstamina;
			}
		}
	}

	// =========================================================================
	// === RENDERIZADO Y ELEMENTOS DEBUG
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		Animaciones.JUGADOR.pintar(g, Globales.getXDesplazamientoCamara(this.getPosicionXIntDibujado()),
				Globales.getYDesplazamientoCamara(this.getPosicionYIntDibujado()));

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			g.setColor(Color.BLUE);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionMovimiento().getBounds());
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.BLACK);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getPosicionXIntDibujado(),
					this.getPosicionYIntDibujado(), 32, 32, Color.RED);
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.estadoJuego) {
			this.pintarAreaRecoleccion(g);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, Globales.JUGADOR.getAreaInteraccionCofre(),
					Color.LIGHT_GRAY);
		}

		this.pintarDebugCaminos(g);
		this.pintarAreaDeteccion(g);
		this.pintarAreaArrojar(g);
	}

	private void pintarDebugCaminos(final Graphics2D g) {
		g.setFont(g.getFont().deriveFont(7f));

		if (this.recorridoD != null) {
			int pos = 1;
			for (final NodoD n : this.recorridoD) {
				final String txt = String.valueOf(pos++);
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, n.getXMundo(), n.getYMundo(), n.getAncho(),
						n.getAlto(), Color.RED);
				DibujoDebug.dibujarStringRefCamara(g, txt,
						(n.getXMundo() + (n.getAncho() / 2))
								- (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						n.getYMundo() + (n.getAlto() / 2)
								+ (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.BLACK);
			}
			if (this.nodoDDestino != null) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoDDestino.getXMundo(),
						this.nodoDDestino.getYMundo(), this.nodoDDestino.getAncho(), this.nodoDDestino.getAlto(),
						Color.YELLOW);
			}
		}

		if (this.recorridoA != null) {
			int pos = 1;
			int xNodoAux;
			int yNodoAux;
			final int wNodoAux = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width;
			final int hNodoAux = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height;
			for (final NodoA n : this.recorridoA) {
				xNodoAux = n.getXNodo() * wNodoAux;
				yNodoAux = n.getYNodo() * hNodoAux;
				final String txt = String.valueOf(pos++);
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, xNodoAux, yNodoAux, wNodoAux, hNodoAux, Color.BLUE);
				DibujoDebug.dibujarStringRefCamara(g, txt,
						(xNodoAux + (wNodoAux / 2)) - (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						yNodoAux + (hNodoAux / 2) + (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.BLACK);
			}
			if (this.nodoADestino != null) {
				final int w = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width;
				final int h = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height;
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getXNodo() * w,
						this.nodoADestino.getYNodo() * h, w, h, Color.YELLOW);
			}
		}
	}

	private void pintarAreaDeteccion(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_DEBUG.presionado() || !Globales.isEstadoJuego() || (this.mundo == null)) {
			return;
		}

		this.CHECK_LIST_DEBUG.clear();
		final Shape areaDeteccionJugador = this.getAreaDeteccion();
		final Rectangle areaInteraccionCofre = this.getAreaInteraccionCofre();

		final ArrayList<ZoneBox> zonasIntersectadas = this.mundo.getZonasIntersectadas(areaDeteccionJugador);
		for (final ZoneBox zb : zonasIntersectadas) {
			if (this.CHECK_LIST_DEBUG.add(zb)) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, zb.getArea(), Color.YELLOW);
			}

			for (final Item item : zb.getItems()) {
				if (this.CHECK_LIST_DEBUG.add(item) && areaDeteccionJugador.intersects(item.getArea())) {
					DibujoDebug.dibujarRectanguloContornoRefCamara(g, item.getArea(), Color.MAGENTA);
				}
			}

			for (final Objeto objeto : zb.getObjetos()) {
				if (this.CHECK_LIST_DEBUG.add(objeto) && areaInteraccionCofre.intersects(objeto.getArea())) {
					DibujoDebug.dibujarRectanguloContornoRefCamara(g, objeto.getArea(), Color.CYAN);
				}
			}
		}
	}

	/**
	 * Previsualiza el área circular de recolección sin generar nuevas instancias
	 * {@link Rectangle}.
	 */
	private void pintarAreaRecoleccion(final Graphics2D g) {
		this.actualizarAreaRecoleccion();
		this.RECTANGLE_AUXILIAR.setBounds((this.getPosicionXInt() - (this.recoleccionLado / 2)) + (this.ANCHO / 2),
				(this.getPosicionYInt() - (this.recoleccionLado / 2)) + (this.ALTO / 2), this.recoleccionLado,
				this.recoleccionLado);
		DibujoDebug.dibujarFiguraEllipseRefCamara(g, this.RECTANGLE_AUXILIAR, Color.CYAN);
	}

	/**
	 * Previsualiza el radio de impacto de arrojadizos sin generar nuevas instancias
	 * {@link Rectangle}.
	 */
	private void pintarAreaArrojar(final Graphics2D g) {
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()) {
			final Rectangle posRaton = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			final Arrojadizo item = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo()
					.getItemArrojadizo();
			final int diametro = item.getDiamentroAreaCaida();

			this.RECTANGLE_AUXILIAR.setBounds(posRaton.x - (diametro / 2), posRaton.y - (diametro / 2), diametro,
					diametro);
			DibujoDebug.dibujarFiguraEllipseRefCamara(g, this.RECTANGLE_AUXILIAR, Color.BLUE);
		}
	}

	@Override
	protected void pintarIndicadorVida(final Graphics2D g) {
		final int posX = this.MARGENX;
		final int posY = this.MARGENY;

		final int porcentajeVida = (int) ((this.vida * 100) / this.vidaMaxima);
		final int porcentajeBarraActual = (porcentajeVida * this.ANCHO) / 100;

		DibujoDebug.dibujarRectanguloRelleno(g, posX - 1, posY - 5, this.ANCHO + 2, 4, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, posX, posY - 4, porcentajeBarraActual, 2, Color.RED);

		g.setFont(g.getFont().deriveFont(4f));
		DibujoDebug.dibujarString(g, (int) this.vida + "/" + (int) this.vidaMaxima, posX, posY - 6, Color.WHITE);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	// =========================================================================
	// === SENSORES ESPACIALES Y DELIMITADORES (ZERO-GC)
	// =========================================================================

	private void actualizarAreaRecoleccion() {
		((Ellipse2D.Double) this.areaRecoleccion).setFrame((this.x - (this.recoleccionLado / 2.0)) + (this.ANCHO / 2.0),
				(this.y - (this.recoleccionLado / 2.0)) + (this.ALTO / 2.0), this.recoleccionLado,
				this.recoleccionLado);
	}

	public Rectangle getAreaInteraccionCofre() {
		this.RECTANGLE_AUXILIAR.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.ANCHO_INTERACCION_COFRE,
				this.ALTO_INTERACCION_COFRE);
		return this.RECTANGLE_AUXILIAR;
	}

	public Shape getAreaDeteccion() {
		return this.areaRecoleccion;
	}

	public Shape getAreaInterseccionMovimiento() {
		this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect(this.x + 2.0, this.y + 12.0, 8.0, 8.0);
		return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
	}

	public Shape getAreaInterseccionMovimiento(final double desplazamiento, final int direccion) {
		switch (direccion) {
		case -1: // OESTE
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect((this.x + 2.0) - desplazamiento, this.y + 12.0, 8.0,
					8.0);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 1: // ESTE
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect((this.x + 2.0) + desplazamiento, this.y + 12.0, 8.0,
					8.0);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 2: // NORTE
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect(this.x + 2.0, (this.y + 12.0) - desplazamiento, 8.0,
					8.0);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 3: // SUR
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect(this.x + 2.0, (this.y + 12.0) + desplazamiento, 8.0,
					8.0);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		default:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setRect(this.x + 2.0, this.y + 12.0, 8.0, 8.0);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		}
	}

	// =========================================================================
	// === ACCESORES Y GETTERS
	// =========================================================================

	public boolean pistolaEquipada() {
		return (Globales.GESTOR_INVENTARIO.getInventarioJugador().getArmaEquipada() instanceof Arma)
				&& !(Globales.GESTOR_INVENTARIO.getInventarioJugador().getArmaEquipada() instanceof Desarmado);
	}

	public Arma getArmaEquipada() {
		return (Arma) Globales.GESTOR_INVENTARIO.getInventarioJugador().getArmaEquipada();
	}

	public void solicitarActualizacionAreaRecoleccionSinRecoger() {
		this.actualizarAreaRecoleccion();
	}

	public double getEstamina() {
		return this.estamina;
	}

	public double getDamage() {
		return this.damage;
	}

	public double getLimiteEstamina() {
		return this.maxEstamina;
	}

	@Override
	public void modificarPosicionX(final double desplazamientoX) {
		this.x += desplazamientoX;
		this.desplazamientoX += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y += desplazamientoY;
		this.desplazamientoY += desplazamientoY;
	}

	public int getDesplazamientoX() {
		return (int) Math.round(this.desplazamientoX);
	}

	public int getDesplazamientoY() {
		return (int) Math.round(this.desplazamientoY);
	}

	public void establecerPosicion(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.moviendoPorRecorrido = false;
	}

	public int getPosicionXParado() {
		return (int) this.x + 3;
	}

	public int getPosicionYParado() {
		return ((int) this.y + this.ALTO) - 1;
	}

	public Point getPosicionParado() {
		this.PUNTO_AUXILIAR.setLocation((int) this.x + (this.ANCHO / 2), ((int) this.y + this.ALTO) - 10);
		return this.PUNTO_AUXILIAR;
	}

	public Point getPosicionTileParado() {
		this.PUNTO_AUXILIAR.setLocation((int) (this.x + 3) / Constantes.LADO_TILE,
				(int) ((this.y + this.ALTO) - 1) / Constantes.LADO_TILE);
		return this.PUNTO_AUXILIAR;
	}

	public int getMargenX() {
		return this.MARGENX;
	}

	public int getMargenY() {
		return this.MARGENY;
	}

	@Override
	public Rectangle getRectangulo() {
		return this.getArea();
	}

	@Override
	public Point getPosicionTile() {
		this.PUNTO_AUXILIAR.setLocation((int) this.x / Constantes.LADO_TILE, (int) this.y / Constantes.LADO_TILE);
		return this.PUNTO_AUXILIAR;
	}

	public String getVelocidad() {
		return String.format("%.2f", this.velocidad);
	}

	// =========================================================================
	// === GESTIÓN DE MUNDO Y REINICIO
	// =========================================================================

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {

		super.recibirAtaque(damage, causante);

	}

	@Override
	public JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Player";
	}

	/**
	 * Restablece los atributos vitales del jugador y traslada su contexto a un
	 * nuevo mundo.
	 *
	 * @param mundo Nuevo {@link Mundo} al que se traslada el jugador.
	 */
	public void restablecerYCambiarMundo(final Mundo mundo) {
		this.eliminado = false;
		this.establecerVidaMaxima(this.PTS_VIDAMAX_BASE);
		this.sanar();
		this.damage = this.PTS_DAMAGE_BASE;
		this.setMundo(mundo);
		Globales.GESTOR_INVENTARIO.getInventarioJugador().vaciar();
		if (this.mundo != null) {
			this.mundo.moverJugadorPuntoComienzo();
		}
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@Override
	public void setMundo(final Mundo mundo) {
		super.setMundo(mundo);
		if (this.mundo != null) {
			this.DIJKSTRA = new DijkstraRework(mundo, new Dimension(this.ANCHO, this.ALTO));
		}
		this.moviendoPorRecorrido = false;
	}
}