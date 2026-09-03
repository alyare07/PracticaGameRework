package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONObject;

import principal.animaciones.Animaciones;
import principal.entes.Ente;
import principal.entes.facciones.GestorFacciones;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.Portable;
import principal.entes.objetos.items.armas.Arma;
import principal.entes.objetos.items.arrojadizos.Arrojadizo;
import principal.entes.objetos.items.equipamiento.PiezaEquipo;
import principal.entes.proyectil.GolpeMele;
import principal.ia.Lista;
import principal.ia.aEstrella.NodoA;
import principal.ia.dijkstra.DijkstraRework;
import principal.ia.dijkstra.NodoD;
import principal.inventario.equipamiento.SlotEquipamiento;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.Tile;
import principal.mapa.renderEntidades.ZoneBox;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Constantes;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Representa al personaje jugable con física diagonal normalizada, redondeo
 * simétrico con la cámara, gestión de equipamiento y estamina (Zero-GC).
 * 
 * @version 4.0 (Vanilla Java 8 - Anti-Jitter Subpixel Physics)
 */
public class Jugador extends Criatura {

	private static final String NOMBRE = "Alyare";
	protected final int MARGENX;
	protected final int MARGENY;
	protected double desplazamientoX;
	protected double desplazamientoY;
	protected Tile tilePisado;
	private double damage;
	private final GestorTiempo GT_ULTIMO_ATAQUE;
	private final GestorTiempo GT_RECUPERACION_ESTAMINA;

	private static final int TIEMPO_MS_ESPERA_POR_ATAQUE_BASE = 500;
	private static final int TIEMPO_MS_ESPERA_REGEN_VIDA = 5000;
	private static final int TIEMPO_MS_ESPERA_REGEN_ESTAMINA = 2500;

	private boolean dibujarAtaque;

	protected final Shape areaRecoleccion = new Ellipse2D.Double();
	protected final int recoleccionLado = 50;
	protected final double PTS_VIDAMAX_BASE = 20;
	protected final double PTS_DAMAGE_BASE = 5;

	protected int modFuerzaEquipo = 0;
	protected int modAgilidadEquipo = 0;
	protected int modInteligenciaEquipo = 0;
	protected int defensaTotal = 0;

	protected double estamina;
	protected double maxEstamina;
	protected double puntoRecuperarEstaminaXseg;
	protected double puntoGastarEstaminaXseg;

	protected final int ANCHO_INTERACCION_COFRE;
	protected final int ALTO_INTERACCION_COFRE;

	protected DijkstraRework DIJKSTRA;
	protected NodoD nodoDDestino;
	protected Lista<NodoD> recorridoD;
	private boolean generarRecorridoMoverMouse;
	private boolean moviendoPorRecorrido;

	private final HashSet<Ente> CHECK_LIST_DEBUG = new HashSet<Ente>();
	private final Rectangle AREA_INTERSECCION_MOVIMIENTO_AUXILIAR = new Rectangle(0, 0, 8, 8);
	private final Rectangle RECTANGLE_AUXILIAR = new Rectangle();
	private final Point PUNTO_AUXILIAR = new Point();

	private final AccionEntidad<Item> accionRecogidaItem = new AccionEntidad<Item>() {
		@Override
		public void ejecutar(final Item item) {
			Jugador.this.procesarAbsorcionItem(item);
		}
	};

	public Jugador(final int x, final int y) {
		super(x, y, 12, 20, 50, 50);

		this.setFaccion(GestorFacciones.FACCION_JUGADOR);
		final int anchoSprite = 32;
		final int altoSprite = 32;
		this.MARGENX = Constantes.CENTROX - (anchoSprite / 2);
		this.MARGENY = Constantes.CENTROY - (altoSprite / 2);

		this.fuerzaBase = 10;
		this.agilidadBase = 10;
		this.inteligenciaBase = 10;

		this.recalcularAtributos();
		this.sanar();

		this.GT_ULTIMO_ATAQUE = new GestorTiempo();
		this.GT_RECUPERACION_ESTAMINA = new GestorTiempo();
		this.dibujarAtaque = false;

		this.actualizarAreaRecoleccion();
		this.puntoRecuperarEstaminaXseg = 5;
		this.puntoGastarEstaminaXseg = 5;

		this.ANCHO_INTERACCION_COFRE = this.ANCHO + 2;
		this.ALTO_INTERACCION_COFRE = this.ALTO + 2;
	}

	public void recalcularAtributos() {
		int f = 0;
		int a = 0;
		int i = 0;
		int def = 0;

		if ((Globales.GESTOR_INVENTARIO != null) && (Globales.GESTOR_INVENTARIO.getInventarioJugador() != null)) {
			final ArrayList<SlotEquipamiento> slots = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotManager()
					.getSlotsEquipamiento();

			for (int idx = 0; idx < slots.size(); idx++) {
				final SlotEquipamiento s = slots.get(idx);
				if (s.contieneItem() && (s.getItem() instanceof PiezaEquipo)) {
					final PiezaEquipo p = (PiezaEquipo) s.getItem();
					f += p.getBonifFuerza();
					a += p.getBonifAgilidad();
					i += p.getBonifInteligencia();
					def += p.getArmaduraDefensa();
				}
			}
		}

		this.modFuerzaEquipo = f;
		this.modAgilidadEquipo = a;
		this.modInteligenciaEquipo = i;
		this.defensaTotal = def;

		final double nuevaVidaMax = this.PTS_VIDAMAX_BASE + (this.getFuerzaTotal() * 2.0);
		final double ratioVida = (this.vidaMaxima > 0) ? (this.vida / this.vidaMaxima) : 1.0;
		this.vidaMaxima = nuevaVidaMax;
		this.vida = Math.min(this.vidaMaxima, nuevaVidaMax * ratioVida);
		this.vidaLag = this.vida;

		this.damage = this.PTS_DAMAGE_BASE + (this.getFuerzaTotal() * 0.5);

		this.velocidadEstandar = 1 + (this.getAgilidadTotal() * 0.01);
		this.establecerVelocidadStardar();

		this.maxEstamina = 20.0 + (this.getAgilidadTotal() * 0.5) + (this.getInteligenciaTotal() * 0.5);
		this.estamina = Math.min(this.maxEstamina, this.estamina);
	}

	@Override
	public int getFuerzaTotal() {
		return this.fuerzaBase + this.modFuerzaEquipo;
	}

	@Override
	public int getAgilidadTotal() {
		return this.agilidadBase + this.modAgilidadEquipo;
	}

	@Override
	public int getInteligenciaTotal() {
		return this.inteligenciaBase + this.modInteligenciaEquipo;
	}

	public int getDefensaTotal() {
		return this.defensaTotal;
	}

	@Override
	public void recibirAtaque(final double damageRecibido, final Ente causante) {
		final double factorReduccion = 100.0 / (100.0 + Math.max(0, this.defensaTotal));
		final double danioEfectivo = Math.max(1.0, damageRecibido * factorReduccion);

		super.recibirAtaque(danioEfectivo, causante);
	}

	@Override
	public void actualizar() {
		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if (Globales.RATON.presionadoClickIzqUnicaAct()) {
				Globales.CAMARA.setEntidadEnfocada(this);
			} else if (Globales.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Globales.JUGADOR.getDamage());
			}
		}

		super.actualizar();
		if (this.eliminado) {
			return;
		}

		if (Globales.RATON.presionadoClickIzqUnicaAct()
				&& Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			this.curar();
		}

		if (this.mundo != null) {
			final Terreno terreno = this.mundo.getTerreno();
			final Rectangle s = this.getAreaInterseccionMovimiento();
			this.tilePisado = terreno.getTileReferenciado(s.x + (s.width / 2), s.y + s.height);
		}

		this.actualizarMovimientoMouseDijkstra();
		this.actualizarMovimientoMouseAEstrella();
		this.actualizarMovimientos();
		this.actualizarRecogidaItems();
		this.actualizarArrojar();
		this.actualizarRecarga();
		this.actualizarAtaque();

		if (Animaciones.JUGADOR != null) {
			Animaciones.JUGADOR.actualizar(this);
		}
	}

	private void actualizarRecarga() {
		final Arma armaEquipada = this.getArmaEquipada();
		if ((armaEquipada == null) || !armaEquipada.esArmaDistancia()) {
			return;
		}

		armaEquipada.actualizarCicloRecarga(this);

		if (Globales.TECLADO.TECLA_RECARGAR.presionadoUnicaActualizacion()) {
			armaEquipada.iniciarRecarga(this);
		}
	}

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
				this.velocidad = Math.max(0, this.velocidad + this.tilePisado.getAlteracionVelocidad());
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
				this.velocidad = Math.max(0, this.velocidad + this.tilePisado.getAlteracionVelocidad());
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

	protected void moverANodoDDestino() {
		if (this.nodoDDestino == null) {
			return;
		}
		this.RECTANGLE_AUXILIAR.setBounds(this.nodoDDestino.getXMundo(), this.nodoDDestino.getYMundo(),
				this.nodoDDestino.getAncho(), this.nodoDDestino.getAlto());

		if (this.getPosicionYInt() < this.RECTANGLE_AUXILIAR.y) {
			final double dist = this.RECTANGLE_AUXILIAR.y - this.getPosicionYInt();
			this.setPosicionY((dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.y
					: this.getPosicionY() + Math.min(dist, this.velocidad));
			this.direccion = Direccion.SUR;
		} else if (this.getPosicionYInt() > this.RECTANGLE_AUXILIAR.y) {
			final double dist = this.getPosicionYInt() - this.RECTANGLE_AUXILIAR.y;
			this.setPosicionY((dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.y
					: this.getPosicionY() - Math.min(dist, this.velocidad));
			this.direccion = Direccion.NORTE;
		}

		if (this.getPosicionXInt() < this.RECTANGLE_AUXILIAR.x) {
			final double dist = this.RECTANGLE_AUXILIAR.x - this.getPosicionXInt();
			this.setPosicionX((dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.x
					: this.getPosicionX() + Math.min(dist, this.velocidad));
			this.direccion = Direccion.ESTE;
		} else if (this.getPosicionXInt() > this.RECTANGLE_AUXILIAR.x) {
			final double dist = this.getPosicionXInt() - this.RECTANGLE_AUXILIAR.x;
			this.setPosicionX((dist < this.velocidad) ? this.RECTANGLE_AUXILIAR.x
					: this.getPosicionX() - Math.min(dist, this.velocidad));
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

	private void actualizarMovimientos() {
		boolean enMovimiento = false;
		boolean corriendo = false;

		this.establecerVelocidadStardar();

		final boolean arr = Globales.TECLADO.TECLA_ARRIBA.presionado();
		final boolean abj = Globales.TECLADO.TECLA_ABAJO.presionado();
		final boolean izq = Globales.TECLADO.TECLA_IZQUIERDA.presionado();
		final boolean der = Globales.TECLADO.TECLA_DERECHA.presionado();

		if (Globales.TECLADO.TECLA_CORRIENDO.presionado()) {
			if (arr || abj || der || izq) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					corriendo = true;
				}
			}
		} else {
			this.recuperarEstamina();
		}

		if (this.tilePisado != null) {
			this.velocidad = Math.max(0, this.velocidad + this.tilePisado.getAlteracionVelocidad());
		}

		// =====================================================================
		// NORMALIZACIÓN DE VELOCIDAD DIAGONAL (1 / sqrt(2) ≈ 0.70710678)
		// =====================================================================
		final boolean movVertical = arr ^ abj;
		final boolean movHorizontal = izq ^ der;

		double paso = this.velocidad;
		if (movVertical && movHorizontal) {
			paso *= 0.7071067811865475;
		}

		if (arr) {
			if ((((int) Math.round(this.getPosicionY() - paso)) >= 0)
					&& !this.mundo.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(paso, 2))) {
				this.modificarPosicionY(-paso);
			}
			enMovimiento = true;
			this.direccion = Direccion.NORTE;
		}

		if (abj) {
			if (((this.getPosicionY() + paso) <= (this.mundo.getTerreno().getAlto() - this.ALTO))
					&& !this.mundo.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(paso, 3))) {
				this.modificarPosicionY(paso);
			}
			enMovimiento = true;
			this.direccion = Direccion.SUR;
		}

		if (izq) {
			if (((this.getPosicionX() - paso) >= 0)
					&& !this.mundo.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(paso, -1))) {
				this.modificarPosicionX(-paso);
			}
			enMovimiento = true;
			this.direccion = Direccion.OESTE;
		}

		if (der) {
			if (((this.getPosicionX() + paso) <= (this.mundo.getTerreno().getAncho() - this.ANCHO))
					&& !this.mundo.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(paso, 1))) {
				this.modificarPosicionX(paso);
			}
			enMovimiento = true;
			this.direccion = Direccion.ESTE;
		}

		this.atrasDeComplemento = this.mundo
				.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getAreaInterseccionMovimiento());

		if (enMovimiento && this.moviendoPorRecorrido) {
			this.moviendoPorRecorrido = false;
			this.recorridoD = null;
			this.nodoDDestino = null;
			if (this.recorridoA.size() > 0) {
				this.recorridoA.clear();
			}
			this.nodoADestino = null;
		}

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

	private void actualizarAtaque() {
		final Arma armaEquipada = this.getArmaEquipada();
		final int tiempoEsperaAtaque = (armaEquipada != null) ? armaEquipada.getCadenciaMs()
				: TIEMPO_MS_ESPERA_POR_ATAQUE_BASE;
		final int tiempoEsperaDibujado = Math.max(50, tiempoEsperaAtaque / 2);

		final boolean clickDisparo = ((armaEquipada != null) && armaEquipada.esArmaDistancia())
				&& Globales.RATON.presionadoClickIzqUnicaAct()
				&& !Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()
				&& !Globales.viendoContenedor;

		final boolean intentandoAtacar = Globales.TECLADO.TECLA_ATACANDO.presionado() || clickDisparo;

		if (!intentandoAtacar && this.estaEstadoAtacando()
				&& this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(tiempoEsperaAtaque)) {
			this.removerEstado(Estado.ATACANDO);
		}

		if (!intentandoAtacar || this.tieneEstado(Estado.ARROJANDO)) {
			if (this.dibujarAtaque && this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(tiempoEsperaDibujado)) {
				this.dibujarAtaque = false;
			}
			return;
		}

		if (this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(tiempoEsperaAtaque)) {
			this.GT_ULTIMO_ATAQUE.establecerReferenciaTiempoActual();
			this.dibujarAtaque = true;
			this.meterEstado(Estado.ATACANDO);
			this.realizarAtaque(this.mundo);
		} else if (this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(tiempoEsperaDibujado)) {
			this.dibujarAtaque = false;
		}
	}

	private void realizarAtaque(final Mundo mundo) {
		final Arma armaEquipada = this.getArmaEquipada();
		if ((armaEquipada == null) || (mundo == null)) {
			return;
		}

		if (armaEquipada.esArmaDistancia()) {
			final Point pRaton = Globales.RATON.getPuntoPosicionEscaladoConDesplazamientoCamara();
			final int xOrigen = this.getCentroX();
			final int yOrigen = this.getCentroY();

			this.direccion = Globales.FUNCIONES.getDireccionMirando(xOrigen, yOrigen, pRaton.x, pRaton.y);
			armaEquipada.disparar(xOrigen, yOrigen, pRaton.x, pRaton.y, mundo, this);

		} else {
			this.ataqueMele((int) Math.round(this.getPosicionX()) + 8, (int) Math.round(this.getPosicionY()) + 8,
					this.direccion, mundo);
		}
	}

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

	private void actualizarRecogidaItems() {
		if (!Globales.TECLADO.TECLA_RECOGIENDO.presionado() || (this.tilePisado == null) || (this.mundo == null)) {
			return;
		}
		if (!Globales.TECLEO_RECOGIDA.transcurrioMiliSegundos(300)) {
			return;
		}

		Globales.TECLEO_RECOGIDA.establecerReferenciaTiempoActual();
		this.actualizarAreaRecoleccion();

		this.mundo.paraCadaItemEn(this.areaRecoleccion, this.accionRecogidaItem);
	}

	private void procesarAbsorcionItem(final Item item) {
		if (item.estaEliminado()) {
			return;
		}

		if (item instanceof Consumible) {
			final Consumible cons = (Consumible) item;
			final int cantInicial = cons.getCantidad();

			Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(cons);

			final int absorbidos = cantInicial - cons.getCantidad();

			if (absorbidos > 0) {
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				Globales.GESTOR_TEXTOS.agregarTexto("+" + absorbidos + " " + cons.getNombre(), this.getCentroX(),
						this.getPosicionYInt() - 6, principal.igu.textos.TipoTextoFlotante.ORO_EXP);

				if (cons.getCantidad() <= 0) {
					cons.eliminar();
					if (Globales.GESTOR_DELTAS != null) {
						Globales.GESTOR_DELTAS.obtenerOCrearDelta(this.mundo.getNombreMundo(), 0)
								.registrarDestruccion(cons.getPosicionXInt(), cons.getPosicionYInt());
					}
				}
			}
		} else if (item instanceof Portable) {
			if (Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(item)) {
				item.eliminar();
				GestorSonido.reproducir(IDSonido.GOLPE_1);
				Globales.GESTOR_TEXTOS.agregarTexto("+" + item.getNombre(), this.getCentroX(),
						this.getPosicionYInt() - 6, principal.igu.textos.TipoTextoFlotante.ORO_EXP);

				if (Globales.GESTOR_DELTAS != null) {
					Globales.GESTOR_DELTAS.obtenerOCrearDelta(this.mundo.getNombreMundo(), 0)
							.registrarDestruccion(item.getPosicionXInt(), item.getPosicionYInt());
				}
			}
		}
	}

	private void actualizarArrojar() {
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()) {
			this.meterEstado(Estado.ARROJANDO);
			if (Globales.RATON.presionadoClickIzqUnicaAct()) {
				final Rectangle areaRaton = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
				final Arrojadizo item = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo()
						.getItemArrojadizo();
				item.arrojar(areaRaton.x, areaRaton.y, this.direccion, this.mundo, this);
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
		Globales.GESTOR_PARTICULAS.emitirMagia(this.getCentroX(), this.getCentroY(), 15);

		if (this.vida >= this.vidaMaxima) {
			return;
		}

		if (this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA)) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	private boolean gastarEstamina() {
		if (this.modoDios) {
			return true;
		}
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
		final double gastoPorTick = this.puntoGastarEstaminaXseg * dt;

		if (this.estamina >= gastoPorTick) {
			this.estamina -= gastoPorTick;
			this.GT_RECUPERACION_ESTAMINA.establecerReferenciaTiempoActual();
			return true;
		}
		this.estamina = 0.0;
		return false;
	}

	private void recuperarEstamina() {
		if (!this.estaEstadoCorriendo()
				&& this.GT_RECUPERACION_ESTAMINA.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_ESTAMINA)) {

			final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);
			double recuperacionPorTick = this.puntoRecuperarEstaminaXseg * dt;

			if (this.estaEstadoCaminando()) {
				recuperacionPorTick *= 0.5;
			} else if (this.estamina >= (this.maxEstamina / 2.0)) {
				recuperacionPorTick *= 1.5;
			}

			this.estamina = Math.min(this.maxEstamina, this.estamina + recuperacionPorTick);
		}
	}

	@Override
	public void setModoDios(final boolean modoDios) {
		this.modoDios = modoDios;
		if (this.modoDios) {
			this.sanar();
			this.estamina = this.maxEstamina;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		Animaciones.JUGADOR.pintar(g, Globales.getXDesplazamientoCamara(this.getPosicionXIntDibujado()),
				Globales.getYDesplazamientoCamara(this.getPosicionYIntDibujado()));

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			g.setColor(Color.BLUE);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionMovimiento());
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.BLACK);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getPosicionXIntDibujado(),
					this.getPosicionYIntDibujado(), 32, 32, Color.RED);
		}

		if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.estadoJuego) {
			this.pintarAreaRecoleccion(g);
			Render2D.dibujarRectanguloContornoRefCamara(g, Globales.JUGADOR.getAreaInteraccionCofre(),
					Color.LIGHT_GRAY);
		}

		this.pintarDebugCaminos(g);
		this.pintarAreaDeteccion(g);
		this.pintarAreaArrojar(g);
	}

	private void pintarDebugCaminos(final Graphics2D g) {
		g.setFont(Globales.GESTOR_FUENTES.getFuente(7f));

		if (this.recorridoD != null) {
			int pos = 1;
			for (final NodoD n : this.recorridoD) {
				final String txt = String.valueOf(pos++);
				Render2D.dibujarRectanguloContornoRefCamara(g, n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto(),
						Color.RED);
				Render2D.dibujarStringRefCamara(g, txt,
						(n.getXMundo() + (n.getAncho() / 2))
								- (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						n.getYMundo() + (n.getAlto() / 2)
								+ (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.BLACK);
			}
			if (this.nodoDDestino != null) {
				Render2D.dibujarRectanguloContornoRefCamara(g, this.nodoDDestino.getXMundo(),
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
				Render2D.dibujarRectanguloContornoRefCamara(g, xNodoAux, yNodoAux, wNodoAux, hNodoAux, Color.BLUE);
				Render2D.dibujarStringRefCamara(g, txt,
						(xNodoAux + (wNodoAux / 2)) - (Globales.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						yNodoAux + (hNodoAux / 2) + (Globales.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.BLACK);
			}
			if (this.nodoADestino != null) {
				final int w = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width;
				final int h = this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height;
				Render2D.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getXNodo() * w,
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
				Render2D.dibujarRectanguloContornoRefCamara(g, zb.getArea(), Color.YELLOW);
			}

			for (final Item item : zb.getItems()) {
				if (this.CHECK_LIST_DEBUG.add(item) && areaDeteccionJugador.intersects(item.getArea())) {
					Render2D.dibujarRectanguloContornoRefCamara(g, item.getArea(), Color.MAGENTA);
				}
			}

			for (final Objeto objeto : zb.getObjetos()) {
				if (this.CHECK_LIST_DEBUG.add(objeto) && areaInteraccionCofre.intersects(objeto.getArea())) {
					Render2D.dibujarRectanguloContornoRefCamara(g, objeto.getArea(), Color.CYAN);
				}
			}
		}
	}

	private void pintarAreaRecoleccion(final Graphics2D g) {
		this.actualizarAreaRecoleccion();
		this.RECTANGLE_AUXILIAR.setBounds((this.getPosicionXInt() - (this.recoleccionLado / 2)) + (this.ANCHO / 2),
				(this.getPosicionYInt() - (this.recoleccionLado / 2)) + (this.ALTO / 2), this.recoleccionLado,
				this.recoleccionLado);
		Render2D.dibujarFiguraEllipseRefCamara(g, this.RECTANGLE_AUXILIAR, Color.CYAN);
	}

	private void pintarAreaArrojar(final Graphics2D g) {
		if (Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo().contieneItem()) {
			final Rectangle posRaton = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			final Arrojadizo item = Globales.GESTOR_INVENTARIO.getInventarioJugador().getSlotArrojadizo()
					.getItemArrojadizo();
			final int diametro = item.getDiamentroAreaCaida();

			this.RECTANGLE_AUXILIAR.setBounds(posRaton.x - (diametro / 2), posRaton.y - (diametro / 2), diametro,
					diametro);
			Render2D.dibujarFiguraEllipseRefCamara(g, this.RECTANGLE_AUXILIAR, Color.BLUE);
		}
	}

	@Override
	protected void pintarIndicadorVida(final Graphics2D g) {
		final int posX = this.MARGENX;
		final int posY = this.MARGENY;

		final int porcentajeVida = (int) ((this.vida * 100) / this.vidaMaxima);
		final int porcentajeBarraActual = (porcentajeVida * this.ANCHO) / 100;

		Render2D.dibujarRectanguloRelleno(g, posX - 1, posY - 5, this.ANCHO + 2, 4, Color.BLACK);
		Render2D.dibujarRectanguloRelleno(g, posX, posY - 4, porcentajeBarraActual, 2, Color.RED);

		g.setFont(Globales.GESTOR_FUENTES.getFuente(4f));
		Render2D.dibujarString(g, (int) this.vida + "/" + (int) this.vidaMaxima, posX, posY - 6, Color.WHITE);
		g.setFont(Globales.GESTOR_FUENTES.getFuente(Constantes.TAMANO_FUENTE));
	}

	private void actualizarAreaRecoleccion() {
		((Ellipse2D.Double) this.areaRecoleccion).setFrame(
				(this.getPosicionX() - (this.recoleccionLado / 2.0)) + (this.ANCHO / 2.0),
				(this.getPosicionY() - (this.recoleccionLado / 2.0)) + (this.ALTO / 2.0), this.recoleccionLado,
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

	public Rectangle getAreaInterseccionMovimiento() {
		this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds((int) Math.round(this.getPosicionX()) + 2,
				(int) Math.round(this.getPosicionY()) + 12, 8, 8);
		return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
	}

	public Rectangle getAreaInterseccionMovimiento(final double desplazamiento, final int direccion) {
		final int xBase = (int) Math.round(this.getPosicionX()) + 2;
		final int yBase = (int) Math.round(this.getPosicionY()) + 12;
		final int despInt = (int) Math.round(desplazamiento);

		switch (direccion) {
		case -1:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds(xBase - despInt, yBase, 8, 8);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 1:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds(xBase + despInt, yBase, 8, 8);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 2:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds(xBase, yBase - despInt, 8, 8);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		case 3:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds(xBase, yBase + despInt, 8, 8);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		default:
			this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR.setBounds(xBase, yBase, 8, 8);
			return this.AREA_INTERSECCION_MOVIMIENTO_AUXILIAR;
		}
	}

	public boolean tieneArmaDistanciaEquipada() {
		final Arma arma = this.getArmaEquipada();
		return (arma != null) && arma.esArmaDistancia();
	}

	public boolean pistolaEquipada() {
		return this.tieneArmaDistanciaEquipada();
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
		if (desplazamientoX > 0) {
			this.direccion = Direccion.ESTE;
		} else if (desplazamientoX < 0) {
			this.direccion = Direccion.OESTE;
		}
		this.setPosicionXSinVerificarZonebox(this.getPosicionX() + desplazamientoX);
		this.desplazamientoX += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		if (desplazamientoY > 0) {
			this.direccion = Direccion.SUR;
		} else if (desplazamientoY < 0) {
			this.direccion = Direccion.NORTE;
		}
		this.setPosicionYSinVerificarZonebox(this.getPosicionY() + desplazamientoY);
		this.desplazamientoY += desplazamientoY;
	}

	public void setDamage(final double damage) {
		this.damage = Math.max(0, damage);
	}

	public void setEstamina(final double estamina) {
		this.estamina = Math.max(0, Math.min(this.maxEstamina, estamina));
	}

	public void setMaxEstamina(final double maxEstamina) {
		this.maxEstamina = Math.max(1, maxEstamina);
		this.estamina = Math.min(this.estamina, this.maxEstamina);
	}

	@Override
	public void setPosicion(final double x, final double y) {
		this.setPosicionXSinVerificarZonebox(x);
		this.setPosicionYSinVerificarZonebox(y);
		this.moviendoPorRecorrido = false;
	}

	@Override
	public void setPosicionX(final double x) {
		super.setPosicionXSinVerificarZonebox(x);
	}

	@Override
	public void setPosicionY(final double y) {
		super.setPosicionYSinVerificarZonebox(y);
	}

	public int getDesplazamientoX() {
		return (int) Math.round(this.desplazamientoX);
	}

	public int getDesplazamientoY() {
		return (int) Math.round(this.desplazamientoY);
	}

	public int getPosicionXParado() {
		return this.getPosicionXInt() + 3;
	}

	public int getPosicionYParado() {
		return (this.getPosicionYInt() + this.ALTO) - 1;
	}

	public Point getPosicionParado() {
		this.PUNTO_AUXILIAR.setLocation(this.getPosicionXInt() + (this.ANCHO / 2),
				(this.getPosicionYInt() + this.ALTO) - 10);
		return this.PUNTO_AUXILIAR;
	}

	public Point getPosicionTileParado() {
		this.PUNTO_AUXILIAR.setLocation(Math.floorDiv(this.getPosicionXInt() + 3, Constantes.LADO_TILE),
				Math.floorDiv((this.getPosicionYInt() + this.ALTO) - 1, Constantes.LADO_TILE));
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
		this.PUNTO_AUXILIAR.setLocation(Math.floorDiv(this.getPosicionXInt(), Constantes.LADO_TILE),
				Math.floorDiv(this.getPosicionYInt(), Constantes.LADO_TILE));
		return this.PUNTO_AUXILIAR;
	}

	public String getStringVelocidad() {
		return String.format("%.2f", this.velocidad);
	}

	@Override
	public JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Player";
	}

	public void restablecerYCambiarMundo(final Mundo mundo) {
		this.eliminado = false;
		this.fuerzaBase = 10;
		this.agilidadBase = 10;
		this.inteligenciaBase = 10;
		this.recalcularAtributos();
		this.sanar();
		this.setFaccion(GestorFacciones.FACCION_JUGADOR);
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

	@Override
	public String getNombre() {
		return NOMBRE;
	}
}