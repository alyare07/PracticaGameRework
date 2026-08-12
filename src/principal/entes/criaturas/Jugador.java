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
import principal.entes.objetos.cofres.Cofre;
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

/**
 * Representa al personaje controlado por el usuario. Gestiona controles por
 * teclado/ratón, estamina, ataques meclé/distancia e interacciones.
 */
public class Jugador extends Criatura {

	protected final int MARGENX;
	protected final int MARGENY;
	protected int desplazamientoX;
	protected int desplazamientoY;
	protected Tile tilePisado;

	private double damage;
	private final GestorTiempo GT_ULTIMO_ATAQUE;
	private final GestorTiempo GT_RECUPERACION_ESTAMINA;

	private static final int TIEMPO_MS_ESPERA_POR_ATAQUE = 600;
	private static final int TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE = TIEMPO_MS_ESPERA_POR_ATAQUE / 2;
	private static final int TIEMPO_MS_ESPERA_REGEN_VIDA = 5000;
	private static final int TIEMPO_MS_ESPERA_REGEN_ESTAMINA = 2500;

	private boolean dibujarAtaque;

	protected Shape areaRecoleccion;
	protected final int recoleccionLado = 50;

	protected final double PTS_VIDAMAX_BASE = 100;
	protected final double PTS_DAMAGE_BASE = 5;
	protected double estamina;
	protected double maxEstamina;
	protected double puntoRecuperarEstaminaXseg;
	protected double puntoGastarEstaminaXseg;
	protected final float PTS_CONSUMIR_ESTAMINA = 0.5f;

	protected final int ANCHO_INTERACCION_COFRE;
	protected final int ALTO_INTERACCION_COFRE;

	protected DijkstraRework DIJKSTRA;
	protected NodoD nodoDDestino;
	protected Lista<NodoD> recorridoD;

	private boolean generarRecorridoMoverMouse;
	private boolean moviendoPorRecorrido;
	private final HashSet<Ente> CHECK_LIST_DEBUG = new HashSet<>();

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

	@Override
	public void pintar(final Graphics2D g) {
		Animaciones.JUGADOR.pintar(g, Constantes.getXDesplazamientoCamara(this.getPosicionXIntDibujado()),
				Constantes.getYDesplazamientoCamara(this.getPosicionYIntDibujado()));

		if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado() && Constantes.GLOBALES.estadoJuego) {
			g.setColor(Color.BLUE);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionMovimiento().getBounds());
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.BLACK);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getPosicionXIntDibujado(),
					this.getPosicionYIntDibujado(), 32, 32, Color.RED);
		}

		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
			this.pintarAreaRecoleccion(g);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, Constantes.JUGADOR.getAreaInteraccionCofre(),
					Color.LIGHT_GRAY);
		}

		// Debug de caminos Dijkstra y A*
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
								- (Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						n.getYMundo() + (n.getAlto() / 2)
								+ (Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
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
						(xNodoAux + (wNodoAux / 2))
								- (Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						yNodoAux + (hNodoAux / 2) + (Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.BLACK);
			}
			if (this.nodoADestino != null) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g,
						this.nodoADestino.getXNodo() * this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width,
						this.nodoADestino.getYNodo() * this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height,
						this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width,
						this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height, Color.YELLOW);
			}
		}
	}

	private void pintarAreaDeteccion(final Graphics2D g) {
		if (!Constantes.TECLADO.TECLA_DEBUG.presionado() || !Constantes.isEstadoJuego() || (this.mundo == null)) {
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

			for (final Cofre cofre : zb.getCofres()) {
				if (this.CHECK_LIST_DEBUG.add(cofre) && areaInteraccionCofre.intersects(cofre.getArea())) {
					DibujoDebug.dibujarRectanguloContornoRefCamara(g, cofre.getArea(), Color.CYAN);
				}
			}
		}
	}

	@Override
	public void actualizar() {
		if (Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())) {
			if (Constantes.RATON.presionadoClickIzqUnicaAct()) {
				Constantes.CAMARA.setEntidadEnfocada(this);
			} else if (Constantes.RATON.presionadoClickDerUnicaAct()) {
				this.curar(Constantes.JUGADOR.getDamage());
			}
		}

		if (this.eliminado) {
			return;
		}

		this.curar();

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

	private void actualizarMovimientoMouseDijkstra() {
		if ((this.recorridoD == null) || Constantes.RATON.presionadoClickDerUnicaAct()) {
			if (this.generarRecorridoMoverMouse && !Constantes.RATON.presionadoClickDerUnicaAct()) {
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

			if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.RATON.presionadoClickDerUnicaAct()) {
				if (this.moviendoPorRecorrido) {
					this.nodoDDestino = null;
					this.recorridoD = null;
					this.moviendoPorRecorrido = false;
				}

				final Point p = Constantes.RATON.getPuntoPosicionEscaladoConDesplazamientoCamara();
				if (!this.mundo.getTerreno().AreaDentroDelTerreno(
						Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara())) {
					return;
				}

				this.DIJKSTRA.actualizar(p);
				final NodoD n = this.DIJKSTRA.getNodoReferenciado(p.x, p.y);
				if ((n == null) || this.mundo.colisionaConZonaUObjetoSolido(
						new Rectangle(n.getXMundo(), n.getYMundo(), n.getAncho(), n.getAlto()))) {
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
			if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
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
		if ((this.recorridoA == null) || Constantes.RATON.presionadoClickDerUnicaAct()) {
			if (Constantes.TECLADO.TECLA_DEBUG_TILE_INFO.presionado()
					&& Constantes.RATON.presionadoClickDerUnicaAct()) {
				final Point p = Constantes.RATON.getPuntoPosicionEscaladoConDesplazamientoCamara();
				if (!this.mundo.getTerreno().AreaDentroDelTerreno(
						Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara())) {
					return;
				}

				final NodoA n = this.getMundo().getAEstrellaX12X20().getNodoRef(p.x, p.y);
				if ((n == null) || this.mundo.colisionaConZonaUObjetoSolido(
						new Rectangle(n.getXNodo() * this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width,
								n.getYNodo() * this.getMundo().getAEstrellaX12X20().getDimensionNodoA().height,
								this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width,
								this.getMundo().getAEstrellaX12X20().getDimensionNodoA().width))) {
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

			if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
				return;
			}

			this.establecerVelocidadStardar();
			if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
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

			if ((this.nodoADestino == this.recorridoA.getLast())
					&& (this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt(),
							this.getMundo().getAEstrellaX12X20().getDimensionNodoA()))) {

				this.moviendoPorRecorrido = false;
				this.nodoADestino = null;
				this.setEstadoEstandar();
				if (this.recorridoA.size() > 0) {
					this.recorridoA.clear();
				}
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

		final Rectangle areaNodo = this.nodoDDestino.getArea();

		if (this.getPosicionYInt() < areaNodo.y) {
			final double dist = areaNodo.y - this.getPosicionYInt();
			this.y = (dist < this.velocidad) ? areaNodo.y : this.y + Math.min(dist, this.velocidad);
			this.direccion = Direccion.SUR;
		} else if (this.getPosicionYInt() > areaNodo.y) {
			final double dist = this.getPosicionYInt() - areaNodo.y;
			this.y = (dist < this.velocidad) ? areaNodo.y : this.y - Math.min(dist, this.velocidad);
			this.direccion = Direccion.NORTE;
		}

		if (this.getPosicionXInt() < areaNodo.x) {
			final double dist = areaNodo.x - this.getPosicionXInt();
			this.x = (dist < this.velocidad) ? areaNodo.x : this.x + Math.min(dist, this.velocidad);
			this.direccion = Direccion.ESTE;
		} else if (this.getPosicionXInt() > areaNodo.x) {
			final double dist = this.getPosicionXInt() - areaNodo.x;
			this.x = (dist < this.velocidad) ? areaNodo.x : this.x - Math.min(dist, this.velocidad);
			this.direccion = Direccion.OESTE;
		}

		if (this.nodoDDestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())
				&& (this.getPosicionXInt() == areaNodo.x) && (this.getPosicionYInt() == areaNodo.y)) {
			if ((this.recorridoD != null) && this.recorridoD.hasNext()) {
				this.nodoDDestino = this.recorridoD.getNext();
			}
		}
	}

	private void actualizarAtaque() {
		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() && this.estaEstadoAtacando()
				&& this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			this.removerEstado(Estado.ATACANDO);
		}

		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() || this.tieneEstado(Estado.ARROJANDO)) {
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

	private void curar() {
		if (this.vida >= this.vidaMaxima) {
			return;
		}

		if (this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA)) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	private void realizarAtaque(final Mundo mundo) {
		final Arma armaEquipada = this.getArmaEquipada();

		if (armaEquipada instanceof Pistola) {
			final Pistola pistola = (Pistola) armaEquipada;
			final int offsetX = (this.direccion == Direccion.OESTE) ? -8 : 8;
			final int offsetY = (this.direccion == Direccion.NORTE) ? -8 : 8;
			pistola.disparar((int) this.x + offsetX, (int) this.y + offsetY, this.direccion, mundo, this, false);
		} else if (armaEquipada instanceof Desarmado) {
			// Se simplificó la verificación ya que todas las direcciones ejecutaban la
			// misma invocación
			this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
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
		if (!Constantes.TECLADO.TECLA_RECOGIENDO.presionado() || (this.tilePisado == null)) {
			return;
		}
		if (!Constantes.TECLEO_RECOGIDA.transcurrioMiliSegundos(300)) {
			return;
		}

		Constantes.TECLEO_RECOGIDA.establecerReferenciaTiempoActual();
		this.actualizarAreaRecoleccion();

		final ArrayList<Item> listaItems = new ArrayList<>(this.mundo.getItemsIntersectados(this.areaRecoleccion));
		for (final Item item : listaItems) {
			if (Constantes.INVENTARIO.agregarObjeto(item)) {
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

	private void actualizarArrojar() {
		if (Constantes.INVENTARIO.getSlotArrojadizo().contieneItem()) {
			this.meterEstado(Estado.ARROJANDO);
			if (Constantes.RATON.presionadoClickIzqUnicaAct()) {
				final Rectangle areaRaton = Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
				final Arrojadizo item = Constantes.INVENTARIO.getSlotArrojadizo().getItemArrojadizo();
				item.arrojar(areaRaton.x, areaRaton.y, this.direccion, this.mundo, this, false);
				Constantes.INVENTARIO.getSlotArrojadizo().eliminarObjeto();
			} else if (Constantes.RATON.presionadoClickDerUnicaAct()) {
				Constantes.INVENTARIO.getSlotArrojadizo().eliminarObjeto();
			}
		}

		if (!Constantes.INVENTARIO.getSlotArrojadizo().contieneItem() && this.tieneEstado(Estado.ARROJANDO)) {
			this.removerEstado(Estado.ARROJANDO);
		}
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

	private boolean gastarEstamina() {
		if ((this.estamina - this.PTS_CONSUMIR_ESTAMINA) > 0) {
			if (this.estamina < (this.puntoGastarEstaminaXseg / 60)) {
				this.estamina = 0;
				return false;
			}
//			this.estamina -= (this.puntoGastarEstaminaXseg / 60);
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
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60) / 2;
				} else if (this.estamina >= (this.maxEstamina / 2)) {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60) * 1.5;
				} else {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60);
				}
			} else {
				this.estamina = this.maxEstamina;
			}
		}
	}

	public Rectangle getAreaInteraccionCofre() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.ANCHO_INTERACCION_COFRE,
				this.ALTO_INTERACCION_COFRE);
	}

	private void actualizarAreaRecoleccion() {
		this.areaRecoleccion = new Ellipse2D.Double((this.x - (this.recoleccionLado / 2.0)) + (this.ANCHO / 2.0),
				(this.y - (this.recoleccionLado / 2.0)) + (this.ALTO / 2.0), this.recoleccionLado,
				this.recoleccionLado);
	}

	private void pintarAreaRecoleccion(final Graphics2D g) {
		this.actualizarAreaRecoleccion();
		DibujoDebug.dibujarFiguraEllipseRefCamara(g,
				new Rectangle((this.getPosicionXInt() - (this.recoleccionLado / 2)) + (this.ANCHO / 2),
						(this.getPosicionYInt() - (this.recoleccionLado / 2)) + (this.ALTO / 2), this.recoleccionLado,
						this.recoleccionLado),
				Color.CYAN);
	}

	private void pintarAreaArrojar(final Graphics2D g) {
		if (Constantes.INVENTARIO.getSlotArrojadizo().contieneItem()) {
			final Rectangle posRaton = Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			final Arrojadizo item = Constantes.INVENTARIO.getSlotArrojadizo().getItemArrojadizo();
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle(posRaton.x - (item.getDiamentroAreaCaida() / 2),
							posRaton.y - (item.getDiamentroAreaCaida() / 2), item.getDiamentroAreaCaida(),
							item.getDiamentroAreaCaida()),
					Color.BLUE);
		}
	}

	public Shape getAreaDeteccion() {
		return this.areaRecoleccion;
	}

	private void actualizarMovimientos() {
		boolean enMovimiento = false;
		boolean corriendo = false;

		this.establecerVelocidadStardar();

		if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
			if (Constantes.TECLADO.TECLA_ARRIBA.presionado() || Constantes.TECLADO.TECLA_ABAJO.presionado()
					|| Constantes.TECLADO.TECLA_DERECHA.presionado()
					|| Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					corriendo = true;
				}
			}
		} else {
			this.recuperarEstamina();
		}

		if (this.tilePisado != null) {
			this.velocidad = Math.max(0, this.velocidad
					+ ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
		}

		// Movimiento por teclado
		if (Constantes.TECLADO.TECLA_ARRIBA.presionado()) {
			if ((((int) (this.y - this.velocidad)) >= 0) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 2))) {
				this.modificarPosicionY(-this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.NORTE;
		}

		if (Constantes.TECLADO.TECLA_ABAJO.presionado()) {
			if (((this.y + this.velocidad) <= (this.mundo.getTerreno().getAlto() - this.ALTO)) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 3))) {
				this.modificarPosicionY(this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.SUR;
		}

		if (Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {
			if (((this.x - this.velocidad) >= 0) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, -1))) {
				this.modificarPosicionX(-this.velocidad);
			}
			enMovimiento = true;
			this.direccion = Direccion.OESTE;
		}

		if (Constantes.TECLADO.TECLA_DERECHA.presionado()) {
			if (((this.x + this.velocidad) <= (this.mundo.getTerreno().getAncho() - this.ANCHO)) && !this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 1))) {
				this.modificarPosicionX(this.velocidad);
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

		// Actualización de estados según el movimiento
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

	public boolean pistolaEquipada() {
		return (Constantes.INVENTARIO.getArmaEquipada() instanceof Arma)
				&& !(Constantes.INVENTARIO.getArmaEquipada() instanceof Desarmado);
	}

	public Arma getArmaEquipada() {
		return (Arma) Constantes.INVENTARIO.getArmaEquipada();
	}

	@Override
	protected void pintarIndicadorVida(final Graphics2D g) {
		final int posX = this.MARGENX;
		final int posY = this.MARGENY;

		final Rectangle indicador = new Rectangle(posX - 1, posY - 5, this.ANCHO + 2, 4);
		final int porcentajeVida = (int) ((this.vida * 100) / this.vidaMaxima);
		final int porcentajeBarraActual = (porcentajeVida * this.ANCHO) / 100;
		final Rectangle barraVidaActual = new Rectangle(posX, posY - 4, porcentajeBarraActual, 2);

		DibujoDebug.dibujarRectanguloRelleno(g, indicador, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, barraVidaActual, Color.RED);

		g.setFont(g.getFont().deriveFont(4f));
		DibujoDebug.dibujarString(g, (int) this.vida + "/" + (int) this.vidaMaxima, posX, posY - 6, Color.WHITE);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	public int getDesplazamientoX() {
		return this.desplazamientoX;
	}

	public int getDesplazamientoY() {
		return this.desplazamientoY;
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
		return new Point((int) this.x + (this.ANCHO / 2), ((int) this.y + this.ALTO) - 10);
	}

	public Point getPosicionTileParado() {
		return new Point((int) (this.x + 3) / Constantes.GLOBALES.ladoTile,
				(int) ((this.y + this.ALTO) - 1) / Constantes.GLOBALES.ladoTile);
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

	public Shape getAreaInterseccionMovimiento() {
		return new Rectangle2D.Double((int) this.x + 2, (int) this.y + 12, 8, 8);
	}

	public Shape getAreaInterseccionMovimiento(final double desplazamiento, final int direccion) {
		switch (direccion) {
		case -1:
			return new Rectangle2D.Double(((int) this.x + 2) - desplazamiento, (int) this.y + 12, 8, 8);
		case 1:
			return new Rectangle2D.Double((int) this.x + 2 + desplazamiento, (int) this.y + 12, 8, 8);
		case 2:
			return new Rectangle2D.Double((int) this.x + 2, ((int) this.y + 12) - desplazamiento, 8, 8);
		case 3:
			return new Rectangle2D.Double((int) this.x + 2, (int) this.y + 12 + desplazamiento, 8, 8);
		default:
			return new Rectangle2D.Double((int) this.x + 2, (int) this.y + 12, 8, 8);
		}
	}

	@Override
	public Point getPosicionTile() {
		return new Point((int) this.x / Constantes.GLOBALES.ladoTile, (int) this.y / Constantes.GLOBALES.ladoTile);
	}

	public String getVelocidad() {
		return String.format("%.2f", this.velocidad);
	}

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
//		this.reducirVida(damage);
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

	@Override
	public Rectangle getArea() {
		return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.ANCHO, this.ALTO);
	}

	public void restablecerYCambiarMundo(final Mundo mundo) {
		this.eliminado = false;
		this.establecerVidaMaxima(this.PTS_VIDAMAX_BASE);
		this.sanar();
		this.damage = this.PTS_DAMAGE_BASE;
		this.setMundo(mundo);
		Constantes.INVENTARIO.vaciar();
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