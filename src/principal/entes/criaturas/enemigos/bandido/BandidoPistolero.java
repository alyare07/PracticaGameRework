package principal.entes.criaturas.enemigos.bandido;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.entes.objetos.items.armas.distancia.fuego.municiones.Municion;
import principal.ia.dijkstra.NodoD;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class BandidoPistolero extends Bandido{
    private static final byte RECTANGULO_INTERSECCION_PISTOLA_1 = -1;
//    private static final byte RECTANGULO_INTERSECCION_PISTOLA_2 = 0;
    private static final byte RECTANGULO_INTERSECCION_PISTOLA_3 = 1;
    private static final byte RECTANGULO_INTERSECCION_PISTOLA_NONE = Byte.MIN_VALUE;
    private final int rangoDisparo = 248; // el alcance del la bala lo determina igualmente la clase pistola
    private final Pistola pistola;

    public BandidoPistolero(final double x, final double y, final double vida, final double vidaMaxima, final Mundo mundo) {
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
	    DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola1(this.rangoDisparo), Color.red);
	    DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola2(this.rangoDisparo), Color.red);
	    DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getRectanguloInterseccionPistola3(this.rangoDisparo), Color.red);
	}

    }

    private void pintarSprite(final Graphics2D g) {
	if (!this.ESTADO.containsKey(Estado.CAMINANDO)) {
	    this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion, AnimacionesBandido.PISTOLA_ESTANDAR, this.atrasDeComplemento, true);
	} else {
	    this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion, AnimacionesBandido.PISTOLA_CAMINANDO, this.atrasDeComplemento, true);
	}
    }

    @Override
    protected void actualizarAtaque() {
	if (this.realizandoAtaque && this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
	    if (this.enAccion) {
		this.enAccion = false;
	    }
//	    this.setDireccionMirandoCriatura(Constantes.JUGADOR);
	    this.pistola.disparar(this.getCentroX(), this.getCentroY(), this.direccion, this.mundo, this, true);
//	    this.granada.arrojar(Constantes.JUGADOR.getPosicionXInt() + Constantes.JUGADOR.getAncho() / 2, Constantes.JUGADOR.getPosicionYInt() + Constantes.JUGADOR.getAlto() / 2, this.direccion,
//		    this.mundo, this);

	    this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
	    this.realizandoAtaque = false;
	    return;
	} else if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
	    return;
	}

	if (this.atacando) {

	    this.meterEstado(Estado.ATACANDO);
	    if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo()) && this.getRectanguloInterseccionPistola2(this.rangoDisparo).intersects(Constantes.JUGADOR.getArea())) {
		if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {

		    // realiza la carga del ataque
		    if (!this.realizandoAtaque) {
			this.realizandoAtaque = true;
			this.sacarEstado(Estado.CAMINANDO);
			this.sacarEstado(Estado.PERSIGUIENDO);
		    }

		} else if (!this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea())) {
		    this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
		}
		this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
	    } else if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea()) || !this.GE_FUERA_DE_RANGO.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango())) {
		final byte codR = this.getCodRectanguloInterseccionPistolaDiferenteNodo();
		if (codR != RECTANGULO_INTERSECCION_PISTOLA_NONE) {
		    final Point p = this.getPuntoNodoInterseccionDeLosRectangulos(codR);
		    final NodoD n = this.mundo.getDijkstra().getNodoReferenciado(p.x, p.y);
		    final Point posNodo = new Point(n.AREA.x, n.AREA.y);

		    if (this.y < posNodo.y) {
//				y += velocidad;
			this.modificarPosicionY(this.velocidad);
			if ((posNodo.y - this.y) <= 0.25) {
			    this.y = posNodo.y;
			}
		    } else {
			if (this.y > posNodo.y) {
//					y -= velocidad;
			    this.modificarPosicionY(-this.velocidad);
			    if ((this.y - posNodo.y) <= 0.25) {
				this.y = posNodo.y;
			    }
			}
		    }

		    if (this.x < posNodo.x) {
//			x += velocidad;
			this.modificarPosicionX(this.velocidad);
			if ((posNodo.x - this.x) <= 0.25) {
			    this.x = posNodo.x;
			}
		    } else {
			if (this.x > posNodo.x) {
//				x -= velocidad;
			    this.modificarPosicionX(-this.velocidad);
			    if ((this.x - posNodo.x) <= 0.25) {
				this.x = posNodo.x;
			    }
			}
		    }
		} else {
		    this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
		    this.setEstadoCaminando();
		    this.meterEstado(Estado.PERSIGUIENDO);
		}

	    } else {
		this.atacando = false;
		this.pendienteADijkstra = false;
		this.mundo.getDijkstra().reducirEntidadesPendientes();
	    }
	} else {
	    if (!this.ESTADO.containsKey(Estado.ESTANDAR)) {
		this.setEstadoEstandar();
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
	final Rectangle r1 = this.getRectanguloInterseccionPistola1(this.rangoDisparo);
	final Rectangle r2 = this.getRectanguloInterseccionPistola1(this.rangoDisparo);
	final Rectangle r3 = this.getRectanguloInterseccionPistola1(this.rangoDisparo);
	if (jugador.getArea().intersects(r1)) {
	    if (!this.mundo.colisionaConZonaUObjetoSolido(r1)
		    || !this.mundo.colisionaConZonaUObjetoSolido(this.getRectanguloInterseccionPistolaDistaciaHastaJugador(RECTANGULO_INTERSECCION_PISTOLA_1))) {
		return RECTANGULO_INTERSECCION_PISTOLA_1;
	    }
	} else if (jugador.getArea().intersects(r3)) {
	    if (!this.mundo.colisionaConZonaUObjetoSolido(r3)
		    || !this.mundo.colisionaConZonaUObjetoSolido(this.getRectanguloInterseccionPistolaDistaciaHastaJugador(RECTANGULO_INTERSECCION_PISTOLA_3))) {
		return RECTANGULO_INTERSECCION_PISTOLA_3;
	    }
	}
//	if (jugador.getArea().intersects(r2)) {
//	    if (!this.mundo.colisionaConZonaUObjetoSolido(r2) || jugador.getArea().intersects(this.getRectanguloInterseccionPistolaDistaciaHastaJugador(RECTANGULO_INTERSECCION_PISTOLA_2))) {
//		return RECTANGULO_INTERSECCION_PISTOLA_2;
//	    }
//	}
	return RECTANGULO_INTERSECCION_PISTOLA_NONE;
    }

    private Point getPuntoNodoInterseccionDeLosRectangulos(final byte codRect) {

	if (codRect == RECTANGULO_INTERSECCION_PISTOLA_1) {
	    if (this.direccion == Direccion.OESTE) {
		return new Point(this.getPosicionXInt(), this.getPosicionYInt() - this.ALTO / 2 - 1);
	    } else if (this.direccion == Direccion.NORTE) {
		return new Point(this.getPosicionXInt() - this.ANCHO / 2 - 1, this.getPosicionYInt());
	    } else if (this.direccion == Direccion.ESTE) {
		return new Point(this.getPosicionXInt(), this.getPosicionYInt() - this.ALTO / 2 - 1);
	    } else if (this.direccion == Direccion.SUR) {
		return new Point(this.getPosicionXInt() - this.ANCHO / 2 - 1, this.getPosicionYInt());
	    }
	} else if (codRect == RECTANGULO_INTERSECCION_PISTOLA_3) {
	    if (this.direccion == Direccion.OESTE) {
		return new Point(this.getPosicionXInt(), this.getPosicionYInt() + this.ALTO + 1);
	    } else if (this.direccion == Direccion.NORTE) {
		return new Point(this.getPosicionXInt() + this.ANCHO + 1, this.getPosicionYInt());
	    } else if (this.direccion == Direccion.ESTE) {
		return new Point(this.getPosicionXInt(), this.getPosicionYInt() + this.ALTO + 1);
	    } else if (this.direccion == Direccion.SUR) {
		return new Point(this.getPosicionXInt() + this.ANCHO + 1, this.getPosicionYInt());
	    }
	}

//	 if (codRect == RECTANGULO_INTERSECCION_PISTOLA_2) {
//		    if (this.direccion == Direccion.OESTE) {
//			return new Point(this.getPosicionXInt(), this.getPosicionYInt() + this.ALTO / 2);
//		    } else if (this.direccion == Direccion.NORTE) {
//			return new Point(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt());
//		    } else if (this.direccion == Direccion.ESTE) {
//			return new Point(this.getPosicionXInt(), this.getPosicionYInt() + this.ALTO / 2);
//		    } else if (this.direccion == Direccion.SUR) {
//			return new Point(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt());
//		    }
//		} else
	return null;
    }

    private Rectangle getRectanguloInterseccionPistola1(final int rangoDisparo) {
	if (this.direccion == Direccion.OESTE) {
	    return new Rectangle(this.getPosicionXInt() - rangoDisparo, this.getPosicionYInt() + this.ALTO / 2 - this.ALTO - 1, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.NORTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2 - this.ANCHO - 1, this.getPosicionYInt() - rangoDisparo, 1, rangoDisparo + this.ALTO / 2);
	} else if (this.direccion == Direccion.ESTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt() + this.ALTO / 2 - this.ALTO - 1, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.SUR) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2 - this.ANCHO - 1, this.getPosicionYInt() + this.ALTO / 2, 1, rangoDisparo + this.ALTO / 2);
	}
	return null;
    }

    private Rectangle getRectanguloInterseccionPistola2(final int rangoDisparo) {
	if (this.direccion == Direccion.OESTE) {
	    return new Rectangle(this.getPosicionXInt() - rangoDisparo, this.getPosicionYInt() + this.ALTO / 2, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.NORTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt() - rangoDisparo, 1, rangoDisparo + this.ALTO / 2);
	} else if (this.direccion == Direccion.ESTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt() + this.ALTO / 2, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.SUR) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt() + this.ALTO / 2, 1, rangoDisparo + this.ALTO / 2);
	}
	return null;
    }

    private Rectangle getRectanguloInterseccionPistola3(final int rangoDisparo) {
	if (this.direccion == Direccion.OESTE) {
	    return new Rectangle(this.getPosicionXInt() - rangoDisparo, this.getPosicionYInt() + this.ALTO / 2 + this.ALTO + 1, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.NORTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2 + this.ANCHO + 1, this.getPosicionYInt() - rangoDisparo, 1, rangoDisparo + this.ALTO / 2);
	} else if (this.direccion == Direccion.ESTE) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2, this.getPosicionYInt() + this.ALTO / 2 + this.ALTO + 1, rangoDisparo + this.ANCHO / 2, 1);
	} else if (this.direccion == Direccion.SUR) {
	    return new Rectangle(this.getPosicionXInt() + this.ANCHO / 2 + this.ANCHO + 1, this.getPosicionYInt() + this.ALTO / 2, 1, rangoDisparo + this.ALTO / 2);
	}
	return null;
    }

    private Rectangle getRectanguloInterseccionPistolaDistaciaHastaJugador(final byte codRecct) {
	if (codRecct == RECTANGULO_INTERSECCION_PISTOLA_1) {
	    if (this.direccion == Direccion.OESTE) {
		return this.getRectanguloInterseccionPistola1(this.getCentroX() - Constantes.JUGADOR.getCentroX());
	    } else if (this.direccion == Direccion.NORTE) {
		return this.getRectanguloInterseccionPistola1(this.getCentroY() - Constantes.JUGADOR.getCentroY());
	    } else if (this.direccion == Direccion.ESTE) {
		return this.getRectanguloInterseccionPistola1(Constantes.JUGADOR.getCentroX() - this.getCentroX());
	    } else if (this.direccion == Direccion.SUR) {
		return this.getRectanguloInterseccionPistola1(Constantes.JUGADOR.getCentroY() - this.getCentroY());
	    }
	} else if (codRecct == RECTANGULO_INTERSECCION_PISTOLA_3) {
	    if (this.direccion == Direccion.OESTE) {
		return this.getRectanguloInterseccionPistola3(this.getCentroX() - Constantes.JUGADOR.getCentroX());
	    } else if (this.direccion == Direccion.NORTE) {
		return this.getRectanguloInterseccionPistola3(this.getCentroY() - Constantes.JUGADOR.getCentroY());
	    } else if (this.direccion == Direccion.ESTE) {
		return this.getRectanguloInterseccionPistola3(Constantes.JUGADOR.getCentroX() - this.getCentroX());
	    } else if (this.direccion == Direccion.SUR) {
		return this.getRectanguloInterseccionPistola3(Constantes.JUGADOR.getCentroY() - this.getCentroY());
	    }
	}
//	if (codRecct == RECTANGULO_INTERSECCION_PISTOLA_2) {
//	    if (this.direccion == Direccion.OESTE) {
//		return this.getRectanguloInterseccionPistola2(this.getCentroX() - Constantes.JUGADOR.getCentroX());
//	    } else if (this.direccion == Direccion.NORTE) {
//		return this.getRectanguloInterseccionPistola2(this.getCentroY() - Constantes.JUGADOR.getCentroY());
//	    } else if (this.direccion == Direccion.ESTE) {
//		return this.getRectanguloInterseccionPistola2(Constantes.JUGADOR.getCentroX() - this.getCentroX());
//	    } else if (this.direccion == Direccion.SUR) {
//		return this.getRectanguloInterseccionPistola2(Constantes.JUGADOR.getCentroY() - this.getCentroY());
//	    }
//	} else
	return null;
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

class AreaDisparoYDireccion{
    public final Direccion direccion;
    public final Rectangle area;
    public byte codRect;

    public AreaDisparoYDireccion(final Direccion d, final Rectangle r) {
	this.direccion = d;
	this.area = r;
    }

}
