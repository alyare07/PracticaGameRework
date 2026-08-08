package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

import org.json.simple.JSONObject;

import principal.animaciones.Animacion;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.modelos.item.ListaModelosItem;
import principal.entes.modelos.item.ModeloGranada;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.HojaSprite;
import principal.utilidades.Sonidos;
import principal.utilidades.Textura;

public class ProyectilGranada extends ProyectilGeneral{

    private static final long serialVersionUID = 9083899449947637545L;
    protected final Ellipse2D.Double AREA_DESTINO;
    protected final Point DIRECCION_DESTINO;
    protected boolean realizoImpacto;
    protected int[][] trayectoria; // trayectoria[arrayDeX][arrayDeY]
    protected boolean causanteMasCercaAlPunto00;
    private int posTrayectoria;
    private final Granada GRANADA;
    private final Animacion ANIMACION_EXPLOSION;
    private final int TIEMPO_MS_FRAMES_EXPLOSION = 35;

    public ProyectilGranada(final int xDestino, final int yDestino, final Mundo mundo, final Ente causante, final Granada granada, final boolean soloContraJugador) {
	super(granada.getDamage(), 1.5, false, 0, mundo, causante.getPosicionXInt() + causante.getArea().width / 2, causante.getPosicionYInt() + causante.getArea().height / 2, 10, 10, null, causante,
		soloContraJugador);
	this.AREA_DESTINO = new Ellipse2D.Double(xDestino - granada.getDiamentroAreaCaida() / 2, yDestino - granada.getDiamentroAreaCaida() / 2, granada.getDiamentroAreaCaida(),
		granada.getDiamentroAreaCaida());
	this.GRANADA = granada;
	this.ANIMACION_EXPLOSION = new Animacion(new HojaSprite(((ModeloGranada) ListaModelosItem.getModeloConsumible(this.GRANADA.getCodigoModelo())).getTexturaExplosion(), 50, false), false,
		this.TIEMPO_MS_FRAMES_EXPLOSION);
	this.DIRECCION_DESTINO = new Point();
	if (this.AREA_DESTINO.getCenterX() > this.x) {
	    this.DIRECCION_DESTINO.x = 1;
	} else if (this.AREA_DESTINO.getCenterX() < this.x) {
	    this.DIRECCION_DESTINO.x = -1;
	} else {
	    this.DIRECCION_DESTINO.x = 0;
	}
	if (this.AREA_DESTINO.getCenterY() > this.y) {
	    this.DIRECCION_DESTINO.y = 1;
	} else if (this.AREA_DESTINO.getCenterY() < this.y) {
	    this.DIRECCION_DESTINO.y = -1;
	} else {
	    this.DIRECCION_DESTINO.y = 0;
	}
	this.generarTrayectoria();
    }

    public ProyectilGranada(final double damage, final Ellipse2D.Double areaDestino, final Mundo mundo, final int xOrigen, final int yOrigen, final String codModelo, final boolean soloContraJugador) {
	super(damage, 1, false, 0, mundo, xOrigen, yOrigen, 10, 10, null, null, soloContraJugador);
	this.GRANADA = new Granada(1, (int) areaDestino.width, damage, codModelo) {

	    private static final long serialVersionUID = -6508234289982231948L;

	    @Override
	    public Objeto copiar() {
		return null;
	    }

	    @Override
	    protected JSONObject exportarParaJSON() {
		return null;
	    }
	};
	this.ANIMACION_EXPLOSION = new Animacion(new HojaSprite(((ModeloGranada) ListaModelosItem.getModeloConsumible(this.GRANADA.getCodigoModelo())).getTexturaExplosion(), 50, false), false,
		this.TIEMPO_MS_FRAMES_EXPLOSION);
	this.AREA_DESTINO = areaDestino;
	this.DIRECCION_DESTINO = new Point();
	if (this.AREA_DESTINO.getCenterX() > this.x) {
	    this.DIRECCION_DESTINO.x = 1;
	} else if (this.AREA_DESTINO.getCenterX() < this.x) {
	    this.DIRECCION_DESTINO.x = -1;
	} else {
	    this.DIRECCION_DESTINO.x = 0;
	}
	if (this.AREA_DESTINO.getCenterY() > this.y) {
	    this.DIRECCION_DESTINO.y = 1;
	} else if (this.AREA_DESTINO.getCenterY() < this.y) {
	    this.DIRECCION_DESTINO.y = -1;
	} else {
	    this.DIRECCION_DESTINO.y = 0;
	}
	this.generarTrayectoria();
    }

    @Override
    public void actualizar() {
	if (!this.eliminado) {
	    this.mover();
	    this.verificarImpacto();
	}
    }

    @Override
    public void pintar(final Graphics2D g) {
	if (this.realizoImpacto) {
	    this.ANIMACION_EXPLOSION.pintar(g, this.AREA_DESTINO.getX(), this.AREA_DESTINO.getY() - 16, true);
	} else {
	    DibujoDebug.dibujarImagenRefCamara(g, Textura.getTextura(Textura.TEXTURA_X10_GRANADA_1), (int) this.x, (int) this.y);
	    DibujoDebug.dibujarFiguraEllipseRefCamara(g, this.AREA_DESTINO.getBounds(), Color.cyan);
	    super.pintar(g);
	}

    }

    @Override
    protected void verificarImpacto() {
	if (!this.realizoImpacto && this.x == this.AREA_DESTINO.getCenterX() && this.y == this.AREA_DESTINO.getCenterY()) {
	    if (!this.SOLO_CONTRA_JUGADOR) {
		for (final Criatura c : this.mundo.getCriaturasIntersectadas(this.AREA_DESTINO, !(this.CAUSANTE instanceof Jugador))) {
		    this.impactar(c);
		}
	    } else {
		if (Constantes.JUGADOR != this.CAUSANTE) {
		    if (this.AREA_DESTINO.intersects(Constantes.JUGADOR.getRectangulo())) {

			this.impactar(Constantes.JUGADOR);

			if (!this.PENETRANTE) {
			    this.eliminar();
			    return;
			}
		    }
		}
	    }
	    Sonidos.crearSonido("sonidos/explosion1.mp3").reproducir();
	    this.realizoImpacto = true;
	}

	if (this.realizoImpacto && !this.eliminado && this.ANIMACION_EXPLOSION.animacionFinalizada()) {
	    this.eliminar();
	}
    }

    @Override
    protected void mover() {
	if (this.x == this.AREA_DESTINO.getCenterX() && this.y == this.AREA_DESTINO.getCenterY()) {
	    return;
	}
	if (this.causanteMasCercaAlPunto00) {
	    if (this.posTrayectoria == this.trayectoria[0].length) {
		this.x = this.AREA_DESTINO.getCenterX();
		this.y = this.AREA_DESTINO.getCenterY();
		return;
	    }
	    this.x = this.trayectoria[0][this.posTrayectoria];
	    this.y = this.trayectoria[1][this.posTrayectoria];
	    this.posTrayectoria++;
	} else {
	    if (this.posTrayectoria == -1) {
		this.x = this.AREA_DESTINO.getCenterX();
		this.y = this.AREA_DESTINO.getCenterY();
		return;
	    }
	    this.x = this.trayectoria[0][this.posTrayectoria];
	    this.y = this.trayectoria[1][this.posTrayectoria];
	    this.posTrayectoria--;
	}

    }

    @Override
    protected void impactar(final Criatura c) {
	if (this.perforados.containsKey(c)) {
	    return;
	}
	this.perforados.put(c, c);
	c.recibirAtaque(this.DAMAGE, this.CAUSANTE);
    }

    private void generarTrayectoria() {
	final Point puntoOrigen = new Point(this.getPosicionXInt(), this.getPosicionYInt());
	final Point puntoDestino = new Point((int) this.AREA_DESTINO.getCenterX(), (int) this.AREA_DESTINO.getCenterY());
	final Point punto00 = new Point(0, 0);

	if (puntoOrigen.distance(punto00) < puntoDestino.distance(punto00)) {
	    this.causanteMasCercaAlPunto00 = true;
	    this.trayectoria = Constantes.FUNCIONES.GENERADOR_TRAYECTORIAS.getTrayectoiaBezier(puntoOrigen.x, puntoOrigen.y, puntoDestino.x, puntoDestino.y,
		    this.GRANADA.getTiempoMsCaidaEnAnchoPantalla());
	    this.posTrayectoria = 0;
	} else {
	    this.causanteMasCercaAlPunto00 = false;
	    this.trayectoria = Constantes.FUNCIONES.GENERADOR_TRAYECTORIAS.getTrayectoiaBezier(puntoOrigen.x, puntoOrigen.y, puntoDestino.x, puntoDestino.y,
		    this.GRANADA.getTiempoMsCaidaEnAnchoPantalla());
	    this.posTrayectoria = this.trayectoria[0].length - 1;
	}

    }
}
