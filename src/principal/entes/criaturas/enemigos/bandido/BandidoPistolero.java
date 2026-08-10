package principal.entes.criaturas.enemigos.bandido;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class BandidoPistolero extends Bandido {

	private static final byte RECTANGULO_INTERSECCION_PISTOLA_1 = -1;
	private static final byte RECTANGULO_INTERSECCION_PISTOLA_3 = 1;
	private static final byte RECTANGULO_INTERSECCION_PISTOLA_NONE = Byte.MIN_VALUE;

	private final int rangoDisparo = 248;
	private final Pistola pistola;

	// ==========================================
	// SCRATCHPADS: INSTANCIAS DE REUSO (0 GC)
	// ==========================================
	private final Rectangle rPistola1 = new Rectangle();
	private final Rectangle rPistola2 = new Rectangle();
	private final Rectangle rPistola3 = new Rectangle();
	private final Rectangle rPistolaDistancia = new Rectangle();
	private final Point puntoNodoAuxiliar = new Point();

	public BandidoPistolero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.pistola = new Pistola(ListaModelosItem.COD_EQUIPABLE_ARMA, new Municion(100));
		this.areaDeteccionAncho = this.rangoDisparo * 2;
		this.areaDeteccionAlto = this.rangoDisparo * 2;
	}

	@Override
	public void actualizar() {
		if (this.pistola.getMunicion().getCantidad() <= 1) {
			this.pistola.getMunicion().restablecer();
		}
		super.actualizar();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);

		if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola1(this.rangoDisparo),
					Color.red);
			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola2(this.rangoDisparo),
					Color.red);
			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola3(this.rangoDisparo),
					Color.red);
		}
	}

	private void pintarSprite(final Graphics2D g) {
		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_ESTANDAR, this.atrasDeComplemento, true);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.PISTOLA_CAMINANDO, this.atrasDeComplemento, true);
		}
	}

	@Override
	protected void actualizarAtaque() {
		if (this.realizandoAtaque
				&& this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			if (this.enAccion) {
				this.enAccion = false;
			}
			this.pistola.disparar(this.getCentroX(), this.getCentroY(), this.direccion, this.mundo, this, true);
			this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
			this.realizandoAtaque = false;
			return;
		}

		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		if (this.atacando) {
			this.meterEstado(Estado.ATACANDO);

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo()) && this
					.getRectanguloInterseccionPistola2(this.rangoDisparo).intersects(Constantes.JUGADOR.getArea())) {

				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {
					if (!this.realizandoAtaque) {
						this.realizandoAtaque = true;
						this.removerEstado(Estado.CAMINANDO);
						this.removerEstado(Estado.PERSIGUIENDO);
					}
				} else if (!this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea())) {
					this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
				}
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();

			} else if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea())
					|| !this.GE_FUERA_DE_RANGO.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango())) {

				final byte codR = this.getCodRectanguloInterseccionPistolaDiferenteNodo();

				if (codR != RECTANGULO_INTERSECCION_PISTOLA_NONE) {
					final Point p = this.getPuntoNodoInterseccionDeLosRectangulos(codR);
					if ((p != null) && (this.mundo != null) && (this.mundo.getDijkstra() != null)) {
						final NodoD n = this.mundo.getDijkstra().getNodoReferenciado(p.x, p.y);

						if ((n != null) && (n.AREA != null)) {
							final int posNodoX = n.AREA.x;
							final int posNodoY = n.AREA.y;

							// Corrección del movimiento sin instanciar objetos
							if (this.y < posNodoY) {
								this.modificarPosicionY(this.velocidad);
								if ((posNodoY - this.y) <= 0.25) {
									this.y = posNodoY;
								}
							} else if (this.y > posNodoY) {
								this.modificarPosicionY(-this.velocidad);
								if ((this.y - posNodoY) <= 0.25) {
									this.y = posNodoY;
								}
							}

							if (this.x < posNodoX) {
								this.modificarPosicionX(this.velocidad);
								if ((posNodoX - this.x) <= 0.25) {
									this.x = posNodoX;
								}
							} else if (this.x > posNodoX) {
								this.modificarPosicionX(-this.velocidad);
								if ((this.x - posNodoX) <= 0.25) {
									this.x = posNodoX;
								}
							}
						}
					}
				} else {
					this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
					this.meterEstado(Estado.CAMINANDO);
					this.meterEstado(Estado.PERSIGUIENDO);
				}

			} else {
				this.atacando = false;
				this.pendienteADijkstra = false;
				if ((this.mundo != null) && (this.mundo.getDijkstra() != null)) {
					this.mundo.getDijkstra().reducirEntidadesPendientes();
				}
			}
		} else {
			if (!this.tieneEstado(Estado.ESTANDAR)) {
				this.setEstadoUnico(Estado.ESTANDAR);
			}

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.atacando = true;
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}
	}

	private byte getCodRectanguloInterseccionPistolaDiferenteNodo() {
		final Jugador jugador = Constantes.JUGADOR;

		// CORREGIDO: Ahora asigna r1, r2 y r3 correctamente sin copy-paste bug
		final Rectangle r1 = this.getRectanguloInterseccionPistola1(this.rangoDisparo);
		final Rectangle r3 = this.getRectanguloInterseccionPistola3(this.rangoDisparo);

		if (jugador.getArea().intersects(r1)) {
			if (!this.mundo.colisionaConZonaUObjetoSolido(r1) || !this.mundo.colisionaConZonaUObjetoSolido(
					this.getRectanguloInterseccionPistolaDistaciaHastaJugador(RECTANGULO_INTERSECCION_PISTOLA_1))) {
				return RECTANGULO_INTERSECCION_PISTOLA_1;
			}
		} else if (jugador.getArea().intersects(r3)) {
			if (!this.mundo.colisionaConZonaUObjetoSolido(r3) || !this.mundo.colisionaConZonaUObjetoSolido(
					this.getRectanguloInterseccionPistolaDistaciaHastaJugador(RECTANGULO_INTERSECCION_PISTOLA_3))) {
				return RECTANGULO_INTERSECCION_PISTOLA_3;
			}
		}
		return RECTANGULO_INTERSECCION_PISTOLA_NONE;
	}

	private Point getPuntoNodoInterseccionDeLosRectangulos(final byte codRect) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		if (codRect == RECTANGULO_INTERSECCION_PISTOLA_1) {
			if ((this.direccion == Direccion.OESTE) || (this.direccion == Direccion.ESTE)) {
				this.puntoNodoAuxiliar.setLocation(posX, posY - (this.ALTO / 2) - 1);
			} else { // NORTE o SUR
				this.puntoNodoAuxiliar.setLocation(posX - (this.ANCHO / 2) - 1, posY);
			}
			return this.puntoNodoAuxiliar;
		}
		if (codRect == RECTANGULO_INTERSECCION_PISTOLA_3) {
			if ((this.direccion == Direccion.OESTE) || (this.direccion == Direccion.ESTE)) {
				this.puntoNodoAuxiliar.setLocation(posX, posY + this.ALTO + 1);
			} else { // NORTE o SUR
				this.puntoNodoAuxiliar.setLocation(posX + this.ANCHO + 1, posY);
			}
			return this.puntoNodoAuxiliar;
		}
		return null;
	}

	private Rectangle getRectanguloInterseccionPistola1(final int rango) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		switch (this.direccion) {
		case OESTE:
			this.rPistola1.setBounds(posX - rango, (posY + (this.ALTO / 2)) - this.ALTO - 1, rango + (this.ANCHO / 2),
					1);
			break;
		case NORTE:
			this.rPistola1.setBounds((posX + (this.ANCHO / 2)) - this.ANCHO - 1, posY - rango, 1,
					rango + (this.ALTO / 2));
			break;
		case ESTE:
			this.rPistola1.setBounds(posX + (this.ANCHO / 2), (posY + (this.ALTO / 2)) - this.ALTO - 1,
					rango + (this.ANCHO / 2), 1);
			break;
		case SUR:
			this.rPistola1.setBounds((posX + (this.ANCHO / 2)) - this.ANCHO - 1, posY + (this.ALTO / 2), 1,
					rango + (this.ALTO / 2));
			break;
		}
		return this.rPistola1;
	}

	private Rectangle getRectanguloInterseccionPistola2(final int rango) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		switch (this.direccion) {
		case OESTE:
			this.rPistola2.setBounds(posX - rango, posY + (this.ALTO / 2), rango + (this.ANCHO / 2), 1);
			break;
		case NORTE:
			this.rPistola2.setBounds(posX + (this.ANCHO / 2), posY - rango, 1, rango + (this.ALTO / 2));
			break;
		case ESTE:
			this.rPistola2.setBounds(posX + (this.ANCHO / 2), posY + (this.ALTO / 2), rango + (this.ANCHO / 2), 1);
			break;
		case SUR:
			this.rPistola2.setBounds(posX + (this.ANCHO / 2), posY + (this.ALTO / 2), 1, rango + (this.ALTO / 2));
			break;
		}
		return this.rPistola2;
	}

	private Rectangle getRectanguloInterseccionPistola3(final int rango) {
		final int posX = this.getPosicionXInt();
		final int posY = this.getPosicionYInt();

		switch (this.direccion) {
		case OESTE:
			this.rPistola3.setBounds(posX - rango, posY + (this.ALTO / 2) + this.ALTO + 1, rango + (this.ANCHO / 2), 1);
			break;
		case NORTE:
			this.rPistola3.setBounds(posX + (this.ANCHO / 2) + this.ANCHO + 1, posY - rango, 1,
					rango + (this.ALTO / 2));
			break;
		case ESTE:
			this.rPistola3.setBounds(posX + (this.ANCHO / 2), posY + (this.ALTO / 2) + this.ALTO + 1,
					rango + (this.ANCHO / 2), 1);
			break;
		case SUR:
			this.rPistola3.setBounds(posX + (this.ANCHO / 2) + this.ANCHO + 1, posY + (this.ALTO / 2), 1,
					rango + (this.ALTO / 2));
			break;
		}
		return this.rPistola3;
	}

	private Rectangle getRectanguloInterseccionPistolaDistaciaHastaJugador(final byte codRecct) {
		int dist = 0;
		final Jugador jugador = Constantes.JUGADOR;

		if (codRecct == RECTANGULO_INTERSECCION_PISTOLA_1) {
			switch (this.direccion) {
			case OESTE:
				dist = this.getCentroX() - jugador.getCentroX();
				break;
			case NORTE:
				dist = this.getCentroY() - jugador.getCentroY();
				break;
			case ESTE:
				dist = jugador.getCentroX() - this.getCentroX();
				break;
			case SUR:
				dist = jugador.getCentroY() - this.getCentroY();
				break;
			}
			return this.getRectanguloInterseccionPistola1(dist);

		}
		if (codRecct == RECTANGULO_INTERSECCION_PISTOLA_3) {
			switch (this.direccion) {
			case OESTE:
				dist = this.getCentroX() - jugador.getCentroX();
				break;
			case NORTE:
				dist = this.getCentroY() - jugador.getCentroY();
				break;
			case ESTE:
				dist = jugador.getCentroX() - this.getCentroX();
				break;
			case SUR:
				dist = jugador.getCentroY() - this.getCentroY();
				break;
			}
			return this.getRectanguloInterseccionPistola3(dist);
		}

		// Reutilizamos el objeto rPistolaDistancia para retornar un vacio seguro en
		// caso extremo
		this.rPistolaDistancia.setBounds(0, 0, 0, 0);
		return this.rPistolaDistancia;
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getXRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return this.getTiempoMsEsperaRetomarAtaque();
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1350;
	}
}