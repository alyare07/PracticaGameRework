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
import java.util.HashMap;
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
import principal.utilidades.SonidoMP3;

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
	private final SonidoMP3 SONIDO_HIT_GOLPE;
	protected Shape areaRecoleccion;
	protected final int recoleccionLado = 50;
	/*
	 * RECOMIENDO CREAR UNA CLASE GESTORA DE LOS PTS DEL JUGADOR TALES COMO LA
	 * ESTAMINA Y LA VIDA. LIMPIAR CODIGO!
	 */
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
		this.MARGENX = Constantes.CENTROX - ((anchoSprite) / 2);
		this.MARGENY = Constantes.CENTROY - (altoSprite / 2);
		this.establecerVidaMaxima(this.PTS_VIDAMAX_BASE);
		this.damage = this.PTS_DAMAGE_BASE;
		this.velocidadEstandar = 0.5;
		this.GT_ULTIMO_ATAQUE = new GestorTiempo();
		this.GT_RECUPERACION_ESTAMINA = new GestorTiempo();
		this.dibujarAtaque = false;
		this.SONIDO_HIT_GOLPE = new SonidoMP3("sonidos/hit_punch.mp3");
		this.actualizarAreaRecoleccion();
		this.maxEstamina = 30; // 150 - 200
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
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.black);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getPosicionXIntDibujado(),
					this.getPosicionYIntDibujado(), 32, 32, Color.red);

		}
		if (Constantes.TECLADO.TECLA_DEBUG.presionado() && Constantes.GLOBALES.estadoJuego) {
			this.pintarAreaRecoleccion(g);
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, Constantes.JUGADOR.getAreaInteraccionCofre(),
					Color.lightGray);
		}

		if (this.recorridoD != null) {
			g.setFont(g.getFont().deriveFont(7f));
			int pos = 1;
			String txt = String.valueOf(pos);
			for (final NodoD n : this.recorridoD) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, n.AREA, Color.red);
				DibujoDebug.dibujarStringRefCamara(g, txt,
						(n.AREA.x + (n.AREA.width / 2))
								- (Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						n.AREA.y + (n.AREA.height / 2)
								+ (Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.black);
				pos++;
				txt = String.valueOf(pos);
			}
			if (this.nodoADestino != null) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoDDestino.AREA, Color.yellow);
			}
		}

		if (this.recorridoA != null) {
			g.setFont(g.getFont().deriveFont(7f));
			int pos = 1;
			String txt = String.valueOf(pos);
			for (final NodoA n : this.recorridoA) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, n.getAreaEnMundo(), Color.blue);
				DibujoDebug.dibujarStringRefCamara(g, txt,
						(n.getAreaEnMundo().x + (n.getAreaEnMundo().width / 2))
								- (Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2),
						n.getAreaEnMundo().y + (n.getAreaEnMundo().height / 2)
								+ (Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2),
						Color.black);
				pos++;
				txt = String.valueOf(pos);
			}
			if (this.nodoADestino != null) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getAreaEnMundo(), Color.yellow);
			}
		}

		this.pintarAreaDeteccion(g);

		this.pintarAreaArrojar(g);
	}

	private void pintarAreaDeteccion(final Graphics2D g) {
		if (!Constantes.TECLADO.TECLA_DEBUG.presionado() || !Constantes.isEstadoJuego()) {
			return;
		}

		this.CHECK_LIST_DEBUG.clear();

		final Shape areaDeteccionJugador = this.getAreaDeteccion();
		final Rectangle areaInteraccionCofre = this.getAreaInteraccionCofre();

		final ArrayList<ZoneBox> zonasIntersectadas = this.getMundo().getZonasIntersectadas(areaDeteccionJugador);
		for (final ZoneBox zb : zonasIntersectadas) {
			if (this.CHECK_LIST_DEBUG.add(zb)) {
				DibujoDebug.dibujarRectanguloContornoRefCamara(g, zb.getArea(), Color.YELLOW);
			}

			for (final Item item : zb.getItems()) {
				if (this.CHECK_LIST_DEBUG.add(item)) {
					if (areaDeteccionJugador.intersects(item.getArea())) {
						DibujoDebug.dibujarRectanguloContornoRefCamara(g, item.getArea(), Color.MAGENTA);
					}
				}
			}

			for (final Cofre cofre : zb.getCofres()) {
				if (this.CHECK_LIST_DEBUG.add(cofre)) {
					if (areaInteraccionCofre.intersects(cofre.getArea())) {
						DibujoDebug.dibujarRectanguloContornoRefCamara(g, cofre.getArea(), Color.CYAN);
					}
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

		final Terreno terreno = this.mundo.getTerreno();
		final Shape s = this.getAreaInterseccionMovimiento();
		this.tilePisado = terreno.getTileReferenciado(s.getBounds().x + (s.getBounds().width / 2),
				s.getBounds().y + s.getBounds().height);
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
				if (!this.DIJKSTRA.actualizando()) {
					final NodoD nodoParado = this.DIJKSTRA.getNodoReferenciado(this.getPosicionXInt(),
							this.getPosicionYInt());
					if (nodoParado != null) {
						this.recorridoD = this.DIJKSTRA.getRecorrido(nodoParado);
						this.nodoDDestino = this.recorridoD.getNext();
						if ((this.nodoDDestino == null) || this.recorridoD.isEmpty()) {
							this.recorridoD = null;
							this.nodoDDestino = null;
							if (this.moviendoPorRecorrido) {
								this.moviendoPorRecorrido = false;
							}
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
				if ((n == null) || this.mundo.colisionaConZonaUObjetoSolido(n.AREA)) {
					return;
				}
				if (!this.DIJKSTRA.actualizando()) {
					final NodoD nodoParado = this.DIJKSTRA.getNodoReferenciado(this.getPosicionXInt(),
							this.getPosicionYInt());
					if (nodoParado != null) {
						this.recorridoD = this.DIJKSTRA.getRecorrido(nodoParado);
						this.nodoDDestino = this.recorridoD.getNext();
						if ((this.nodoDDestino == null) || this.recorridoD.isEmpty()) {
							this.recorridoD = null;
							this.nodoDDestino = null;
							if (this.moviendoPorRecorrido) {
								this.moviendoPorRecorrido = false;
							}
						}
					}
				} else {
					this.generarRecorridoMoverMouse = true;
				}

			}
		} else {
			if (!this.moviendoPorRecorrido && ((this.recorridoD == null) || (this.nodoDDestino == null))) {
				this.recorridoD = null;
				this.nodoDDestino = null;
				return;
			}
			this.establecerVelocidadStardar();
			if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					if (!this.ESTADO.containsKey(Estado.CORRIENDO)) {
						this.meterEstado(Estado.CORRIENDO);
						this.sacarEstado(Estado.ESTANDAR);
						this.sacarEstado(Estado.CAMINANDO);
					}
				}
				if (this.tilePisado != null) {
					this.velocidad += (ListaModeloTile.getModelo(this.tilePisado.getCodModelo())
							.getAlteracionVelocidad());
					if (this.velocidad < 0) {
						this.velocidad = 0;
					}

				}
			} else {
				this.recuperarEstamina();
			}
			this.moverANodoDDestino();
			if ((this.nodoDDestino == this.recorridoD.getLast())
					&& ((this.getPosicionXInt() == this.nodoDDestino.AREA.x)
							&& (this.getPosicionYInt() == this.nodoDDestino.AREA.y))) {
				this.recorridoD = null;
				this.nodoDDestino = null;
				this.moviendoPorRecorrido = false;
				this.meterEstado(Estado.ESTANDAR);
				this.sacarEstado(Estado.CAMINANDO);
				this.sacarEstado(Estado.CORRIENDO);
			} else if (!this.moviendoPorRecorrido) {
				this.moviendoPorRecorrido = true;
				this.meterEstado(Estado.CAMINANDO);
				this.sacarEstado(Estado.ESTANDAR);
				this.sacarEstado(Estado.CORRIENDO);
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
				final NodoA n = this.aEstrella.getNodoRef(p.x, p.y);
				if ((n == null) || this.mundo.colisionaConZonaUObjetoSolido(n.getAreaEnMundo())) {
					return;
				}
				this.recorridoA = this.aEstrella.getRecorrido(this.getPosicionXInt(), this.getPosicionYInt(), p.x, p.y);
				if ((this.recorridoA == null) || this.recorridoA.isEmpty()) {
					this.recorridoA = null;
					this.nodoADestino = null;
					return;
				}
				this.nodoADestino = this.recorridoA.getNext();
				if ((this.nodoADestino == null) || this.recorridoA.isEmpty()) {
					this.recorridoA = null;
					this.nodoADestino = null;
					if (this.moviendoPorRecorrido) {
						this.moviendoPorRecorrido = false;
					}
				}

			}
		} else {
			if (!this.moviendoPorRecorrido && ((this.recorridoA == null) || (this.nodoADestino == null))) {
				this.recorridoA = null;
				this.nodoADestino = null;
				return;
			}
			if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
				return;
			}
			this.establecerVelocidadStardar();
			if (Constantes.TECLADO.TECLA_CORRIENDO.presionado()) {
				if (this.gastarEstamina()) {
					this.velocidad = this.velocidadEstandar * 1.5;
					if (!this.ESTADO.containsKey(Estado.CORRIENDO)) {
						this.meterEstado(Estado.CORRIENDO);
						this.sacarEstado(Estado.ESTANDAR);
						this.sacarEstado(Estado.CAMINANDO);
					}
				}
			} else {
				this.recuperarEstamina();
			}
			if (this.tilePisado != null) {
				this.velocidad += (ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
				if (this.velocidad < 0) {
					this.velocidad = 0;
				}

			}
			this.moverANodoADestino();
			if ((this.nodoADestino == this.recorridoA.getLast())
					&& ((this.getPosicionXInt() == this.nodoADestino.getAreaEnMundo().x)
							&& (this.getPosicionYInt() == this.nodoADestino.getAreaEnMundo().y))) {
				System.out.println("fin recorrido");
				this.moviendoPorRecorrido = false;
				this.recorridoA = null;
				this.nodoADestino = null;
				this.meterEstado(Estado.ESTANDAR);
				this.sacarEstado(Estado.CAMINANDO);
				this.sacarEstado(Estado.CORRIENDO);
			} else if (!this.moviendoPorRecorrido) {
				this.moviendoPorRecorrido = true;
				this.meterEstado(Estado.CAMINANDO);
				this.sacarEstado(Estado.ESTANDAR);
				this.sacarEstado(Estado.CORRIENDO);
			}
		}
	}

	protected void moverANodoDDestino() {

		if (this.getPosicionYInt() < this.nodoDDestino.AREA.y) {
			if ((this.nodoDDestino.AREA.y - this.getPosicionYInt()) < this.velocidad) {
				this.y = this.nodoDDestino.AREA.y;
			} else {
				this.modificarPosicionY(this.velocidad);
				this.direccion = Direccion.SUR;
//					this.y += this.velocidad;
			}
		} else if (this.getPosicionYInt() > this.nodoDDestino.AREA.y) {
			if ((this.getPosicionYInt() - this.nodoDDestino.AREA.y) < this.velocidad) {
				this.y = this.nodoDDestino.AREA.y;
			} else {
//					this.y -= this.velocidad;
				this.modificarPosicionY(-this.velocidad);
				this.direccion = Direccion.NORTE;
			}
		}

		if (this.getPosicionXInt() < this.nodoDDestino.AREA.x) {
			if ((this.nodoDDestino.AREA.x - this.getPosicionXInt()) < this.velocidad) {
				this.x = this.nodoDDestino.AREA.x;
			} else {
//					this.x += this.velocidad;
				this.modificarPosicionX(this.velocidad);
				this.direccion = Direccion.ESTE;
			}

		} else if (this.getPosicionXInt() > this.nodoDDestino.AREA.x) {
			if ((this.getPosicionXInt() - this.nodoDDestino.AREA.x) < this.velocidad) {
				this.x = this.nodoDDestino.AREA.x;
			} else {
				this.modificarPosicionX(-this.velocidad);
				this.direccion = Direccion.OESTE;
//					this.x -= this.velocidad;
			}
		}

		if (this.nodoDDestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())
				&& ((this.getPosicionXInt() == this.nodoDDestino.AREA.x)
						&& (this.getPosicionYInt() == this.nodoDDestino.AREA.y))) {
			if (this.recorridoD.hasNext()) {
				this.nodoDDestino = this.recorridoD.getNext();
			}

		}
	}

	private void actualizarAtaque() {
		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() && this.ESTADO.containsKey(Estado.ATACANDO)
				&& this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_POR_ATAQUE)) {
			this.sacarEstado(Estado.ATACANDO);
		}
		if (!Constantes.TECLADO.TECLA_ATACANDO.presionado() || this.ESTADO.containsKey(Estado.ARROJANDO)) {
			if (this.dibujarAtaque) {
				if (this.GT_ULTIMO_ATAQUE.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_DIBUJADO_POR_ATAQUE)) {
					this.dibujarAtaque = false;
				}
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
		if ((this.GT_CURACION.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_VIDA))) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	private void realizarAtaque(final Mundo mundo) {
		final Arma armaEquipada = this.getArmaEquipada();
		if (armaEquipada instanceof Pistola) {
			final Pistola pistola = (Pistola) armaEquipada;
			if (this.direccion == Direccion.OESTE) {
				pistola.disparar((int) this.x - 8, (int) this.y + 8, this.direccion, mundo, this, false);
			} else if (this.direccion == Direccion.NORTE) {
				pistola.disparar((int) this.x + 8, (int) this.y - 8, this.direccion, mundo, this, false);
			} else if (this.direccion == Direccion.ESTE) {
				pistola.disparar((int) this.x + 8, (int) this.y + 8, this.direccion, mundo, this, false);
			} else if (this.direccion == Direccion.SUR) {
				pistola.disparar((int) this.x + 8, (int) this.y + 8, this.direccion, mundo, this, false);
			}
		} else if (armaEquipada instanceof Desarmado) {

//			final Pistola pistola = (Pistola) armaEquipada;
			if (this.direccion == Direccion.OESTE) {
				this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
			} else if (this.direccion == Direccion.NORTE) {
				this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
			} else if (this.direccion == Direccion.ESTE) {
				this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
			} else if (this.direccion == Direccion.SUR) {
				this.ataqueMele((int) this.x + 8, (int) this.y + 8, this.direccion, mundo);
			}
		}
	}

	private void ataqueMele(final int xOrigen, final int yOrigen, final Direccion direccion, final Mundo mundo) {
		final int alcanceAtaque = 12;
		final int anchoAtaque = 4;
		if (direccion == Direccion.OESTE) {
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - alcanceAtaque, yOrigen,
					alcanceAtaque, anchoAtaque, direccion, this));
		} else if (direccion == Direccion.ESTE) {
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen, yOrigen, alcanceAtaque, anchoAtaque,
					direccion, this));
		} else if (direccion == Direccion.NORTE) {
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - (anchoAtaque / 2),
					yOrigen - alcanceAtaque, anchoAtaque, alcanceAtaque, direccion, this));
		} else {
			mundo.crearProyectil(new GolpeMele(this.damage, false, mundo, xOrigen - (anchoAtaque / 2), yOrigen,
					anchoAtaque, alcanceAtaque, direccion, this));
		}

	}

//    private void atacar(final Criatura c, final Mundo esc) {
//	c.recibirAtaque(this.damage, this);
//	final int x = c.getPosicionXInt() + (c.getRectangulo().width / 2);
//	final int y = this.getPosicionYInt() + (c.getRectangulo().height - 4);
//	this.SONIDO_HIT_GOLPE.reproducir();
//	esc.agregarParticula(new Sangre(x, y));
//    }

	private void actualizarRecogidaItems() {
		if (!Constantes.TECLADO.TECLA_RECOGIENDO.presionado()) {
			return;
		}
		if (this.tilePisado == null) {
			return;
		}
		if (!Constantes.TECLEO_RECOGIDA.transcurrioMiliSegundos(300)) {
			return;
		}
		Constantes.TECLEO_RECOGIDA.establecerReferenciaTiempoActual();
		this.actualizarAreaRecoleccion();
		final ArrayList<Item> listaItems = new ArrayList<Item>(this.mundo.getItemsIntersectados(this.areaRecoleccion));
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
		if (!Constantes.INVENTARIO.getSlotArrojadizo().contieneItem() && this.ESTADO.containsKey(Estado.ARROJANDO)) {
			this.sacarEstado(Estado.ARROJANDO);
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
		boolean puedeCorrer = false;
		if ((this.estamina - this.PTS_CONSUMIR_ESTAMINA) > 0) {
			if (this.estamina < (this.puntoGastarEstaminaXseg / 60)) {
				this.estamina = 0;
				puedeCorrer = false;
			} else {
				this.estamina -= (this.puntoGastarEstaminaXseg / 60);
				puedeCorrer = true;
			}

			this.GT_RECUPERACION_ESTAMINA.establecerReferenciaTiempoActual();
		}
		return puedeCorrer;
	}

	private void recuperarEstamina() {
		if (!this.ESTADO.containsKey(Estado.CORRIENDO)
				&& this.GT_RECUPERACION_ESTAMINA.transcurrioMiliSegundos(TIEMPO_MS_ESPERA_REGEN_ESTAMINA)) {
			if ((this.estamina < this.maxEstamina)
					&& ((this.estamina + this.puntoRecuperarEstaminaXseg) <= this.maxEstamina)) {
				// EN ESTOS CASOS TAMBIEN SE PODRIA USAR UN GESTOR_TIEMPO PARA TENER UN MEJOR
				// CONTROL DE ESTAS COSAS COMO LA VELOCIDAD DE RECUPERACION.
				if (this.ESTADO.containsKey(Estado.CAMINANDO)) {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60) / 2;
				} else if (this.estamina >= (this.maxEstamina / 2)) {
					this.estamina += (this.puntoRecuperarEstaminaXseg / 60)
							+ ((this.puntoRecuperarEstaminaXseg / 60) / 2);
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
		this.areaRecoleccion = new Ellipse2D.Double((this.x - (this.recoleccionLado / 2)) + (this.ANCHO / 2),
				(this.y - (this.recoleccionLado / 2)) + (this.ALTO / 2), this.recoleccionLado, this.recoleccionLado);
	}

	private void pintarAreaRecoleccion(final Graphics2D g) {
		this.actualizarAreaRecoleccion();
		DibujoDebug.dibujarFiguraEllipseRefCamara(g,
				new Rectangle((this.getPosicionXInt() - (this.recoleccionLado / 2)) + (this.ANCHO / 2),
						(this.getPosicionYInt() - (this.recoleccionLado / 2)) + (this.ALTO / 2), this.recoleccionLado,
						this.recoleccionLado),
				Color.cyan);
	}

	private void pintarAreaArrojar(final Graphics2D g) {
		if (Constantes.INVENTARIO.getSlotArrojadizo().contieneItem()) {
			final Rectangle posRaton = Constantes.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
			final Arrojadizo item = Constantes.INVENTARIO.getSlotArrojadizo().getItemArrojadizo();
			DibujoDebug.dibujarFiguraEllipseRefCamara(g,
					new Rectangle(posRaton.x - (item.getDiamentroAreaCaida() / 2),
							posRaton.y - (item.getDiamentroAreaCaida() / 2), item.getDiamentroAreaCaida(),
							item.getDiamentroAreaCaida()),
					Color.blue);
		}
	}

	public Shape getAreaDeteccion() {
		return this.areaRecoleccion;
	}

	private void actualizarMovimientos() {

		boolean enMovimiento = false;
		boolean corriendo = false;

		// movimiento en eje x e y
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
			this.velocidad += (ListaModeloTile.getModelo(this.tilePisado.getCodModelo()).getAlteracionVelocidad());
			if (this.velocidad < 0) {
				this.velocidad = 0;
			}

		}

		if (Constantes.TECLADO.TECLA_ARRIBA.presionado()) {

			if (!(((int) (this.y - this.velocidad)) < 0) && !(this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 2)))) {
				this.modificarPosicionY(-this.velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.NORTE;

		}
		if (Constantes.TECLADO.TECLA_ABAJO.presionado()) {

			if (!((this.y + this.velocidad) > (this.mundo.getTerreno().getAlto() - this.ALTO)) && !(this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 3)))) {
				this.modificarPosicionY(this.velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.SUR;
		}

		if (Constantes.TECLADO.TECLA_IZQUIERDA.presionado()) {

			if (!((this.x - this.velocidad) < 0) && !(this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, -1)))) {
				this.modificarPosicionX(-this.velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.OESTE;
		}
		if (Constantes.TECLADO.TECLA_DERECHA.presionado()) {

			if (!((this.x + this.velocidad) > (this.mundo.getTerreno().getAncho() - this.ANCHO)) && !(this.mundo
					.colisionaConZonaUObjetoSolido(this.getAreaInterseccionMovimiento(this.velocidad, 1)))) {
				this.modificarPosicionX(this.velocidad);
			}
			if (!enMovimiento) {
				enMovimiento = true;
			}
			this.direccion = Direccion.ESTE;
		}

		if (this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getAreaInterseccionMovimiento())) {
			if (!this.atrasDeComplemento) {
				this.atrasDeComplemento = true;
			}
		} else if (this.atrasDeComplemento) {
			this.atrasDeComplemento = false;
		}

		if (enMovimiento && this.moviendoPorRecorrido) {
			this.moviendoPorRecorrido = false;
			this.recorridoD = null;
			this.nodoDDestino = null;
			this.recorridoA = null;
			this.nodoADestino = null;
		}

		if (corriendo) {
			this.meterEstado(Estado.CORRIENDO);
			this.sacarEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.ESTANDAR);
		} else {
			this.sacarEstado(Estado.CORRIENDO);
		}

		if (!enMovimiento && this.moviendoPorRecorrido) {

			if (!this.ESTADO.containsKey(Estado.CORRIENDO)) {
				this.meterEstado(Estado.CAMINANDO);
				this.sacarEstado(Estado.ESTANDAR);
				this.sacarEstado(Estado.CORRIENDO);
			}
			return;
		}

		if (!enMovimiento) {
			this.meterEstado(Estado.ESTANDAR);
			this.sacarEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.CORRIENDO);
		} else if (!this.ESTADO.containsKey(Estado.CORRIENDO)) {
			this.meterEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.ESTANDAR);
			this.sacarEstado(Estado.CORRIENDO);
		}

	}

	public boolean pistolaEquipada() {
		return (Constantes.INVENTARIO.getArmaEquipada() instanceof Arma)
				&& !(Constantes.INVENTARIO.getArmaEquipada() instanceof Desarmado); // cambiar Arma por pistola en un
		// futuro
	}

	public Arma getArmaEquipada() {
		return (Arma) Constantes.INVENTARIO.getArmaEquipada();
	}

	@Override
	protected void pintarIndicadorVida(final Graphics2D g) {
		final int posicionX = this.MARGENX;
		final int posicionY = this.MARGENY;
		final Rectangle indicador = new Rectangle(posicionX - 1, posicionY - 5, this.ANCHO + 2, 4);
		final int porcentajeVida = (int) ((this.vida * 100) / this.vidaMaxima);
		final int pocentajeBarraVidaActual = (porcentajeVida * this.ANCHO) / 100;
		final Rectangle barraVidaActual = new Rectangle(posicionX, posicionY - 4, pocentajeBarraVidaActual, 2);
		DibujoDebug.dibujarRectanguloRelleno(g, indicador, Color.BLACK);
		DibujoDebug.dibujarRectanguloRelleno(g, barraVidaActual, Color.RED);

		g.setFont(g.getFont().deriveFont(4f));
		DibujoDebug.dibujarString(g, String.valueOf(this.vida) + "/" + String.valueOf(this.vidaMaxima), posicionX,
				posicionY - 6, Color.white);
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
		if (this.moviendoPorRecorrido) {
			this.moviendoPorRecorrido = false;
		}
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

	/**
	 * Obtiene el shape del area de interseccion del jugador en base a un
	 * desplazamiento.
	 * 
	 * @param desplazamiento El valor del desplazamiento.
	 * @param direccion      El sentido del desplazamiento -> -1 = izquierda. 1 =
	 *                       Derecha. 2 = Norte. 3 = Sur.
	 * @return El shape de interseccion del jugador.
	 */
	public Shape getAreaInterseccionMovimiento(final double desplazamiento, final int direccion) {
		if (direccion == -1) {
			return new Rectangle2D.Double(((int) this.x + 2) - desplazamiento, (int) this.y + 12, 8, 8);
		}
		if (direccion == 1) {
			return new Rectangle2D.Double((int) this.x + 2 + desplazamiento, (int) this.y + 12, 8, 8);
		}
		if (direccion == 2) {
			return new Rectangle2D.Double((int) this.x + 2, ((int) this.y + 12) - desplazamiento, 8, 8);
		}
		if (direccion == 3) {
			return new Rectangle2D.Double((int) this.x + 2, (int) this.y + 12 + desplazamiento, 8, 8);
		}
		return new Rectangle2D.Double((int) this.x + 2, (int) this.y + 12, 8, 8);
	}

	@Override
	public Point getPosicionTile() {
		return new Point((int) this.x / Constantes.GLOBALES.ladoTile, (int) this.y / Constantes.GLOBALES.ladoTile);
	}

	public String getVelocidad() {
		return String.format("%.2f", this.velocidad);
	}

	public HashMap<Estado, Estado> getEstado() {
		return this.ESTADO;
	}

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		this.reducirVida(damage);
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
	public void reducirVida(final double damage) {
		super.reducirVida(damage);
		if (this.eliminado) {

		}
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
		this.mundo.moverJugadorPuntoComienzo();
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
		this.DIJKSTRA = new DijkstraRework(mundo, new Dimension(this.ANCHO, this.ALTO));
		if (this.moviendoPorRecorrido) {
			this.moviendoPorRecorrido = false;
		}
	}

}
