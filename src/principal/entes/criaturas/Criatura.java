package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.particulas.Sangre;
import principal.ia.Lista;
import principal.ia.aEstrella.AEstrella;
import principal.ia.aEstrella.NodoA;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;

public abstract class Criatura extends Ente{

    public enum Direccion {
	NORTE("N"), SUR("S"), ESTE("E"), OESTE("W");

	private Direccion(final String descripcion) {
	    this.DESCRIPCION = descripcion;
	}

	private final String DESCRIPCION;

	@Override
	public String toString() {
	    return this.DESCRIPCION;
	}
    }

    public enum Estado {
	ESTANDAR("Estandar"), CAMINANDO("Caminando"), CORRIENDO("Corriendo"), ATACANDO("Atacando"), ARROJANDO("Arrojando"), PERSIGUIENDO("Persiguiendo");

	private Estado(final String descripcion) {
	    this.DESCRIPCION = descripcion;
	}

	private final String DESCRIPCION;

	@Override
	public String toString() {
	    return this.DESCRIPCION;
	}
    }

    protected final int ANCHO;
    protected final int ALTO;
    protected double velocidad = 1;
    protected double x;
    protected double y;
    protected double vida;
    protected double vidaMaxima;
    protected double velocidadEstandar = 0.5;
    protected final GestorTiempo GT_ESPERA;// tiempo de la accion esperar
    protected final GestorTiempo GT_ATACADO; // tiempo del ultimo ataque recibido
    protected final GestorTiempo GT_CURACION;
    protected double vidaRegen;
    protected Direccion direccion;
    protected boolean atrasDeComplemento;
    protected int margenXInicialSprite;
    protected int margenYInicialSprite;
    protected int margenXFinalSprite;
    protected int margenYFinalSprite;
    protected Lista<NodoA> recorridoA;
    protected AEstrella aEstrella;
    protected NodoA nodoADestino;
    protected int destinoX;
    protected int destinoY;
    protected final static Random ALEATORIO = new Random(System.currentTimeMillis());
    protected final HashMap<Estado, Estado> ESTADO = new HashMap<Estado, Estado>();

    public Criatura(final double x, final double y, final int ancho, final int alto) {
	this.establecerMargenesSprite();
	this.ANCHO = ancho;
	this.ALTO = alto;
	this.x = x;
	this.y = y;
	this.establecerVelocidadStardar();
	this.vidaMaxima = 100;
	this.vida = this.vidaMaxima;
	this.GT_ESPERA = new GestorTiempo();
	this.GT_ATACADO = new GestorTiempo();
	this.GT_CURACION = new GestorTiempo();
	this.vidaRegen = 1;
	this.direccion = Direccion.ESTE;
    }

    public Criatura(final double x, final double y, final int ancho, final int alto, final double vida, final double vidaMaxima) {
	this.establecerMargenesSprite();
	this.ANCHO = ancho;
	this.ALTO = alto;
	this.x = x;
	this.y = y;
	this.establecerVelocidadStardar();

	this.vidaMaxima = vidaMaxima;
	if (vida > vidaMaxima) {
	    this.vida = this.vidaMaxima;
	} else {
	    this.vida = vida;
	}
	this.GT_ESPERA = new GestorTiempo();
	this.GT_ATACADO = new GestorTiempo();
	this.GT_CURACION = new GestorTiempo();
	this.vidaRegen = 1;
	this.direccion = Direccion.ESTE;
    }

    public Criatura(final double x, final double y, final int ancho, final int alto, final double velocidad) {
	this.establecerMargenesSprite();
	this.ANCHO = ancho;
	this.ALTO = alto;
	this.x = x;
	this.y = y;
	this.velocidadEstandar = velocidad;
	this.establecerVelocidadStardar();
	this.vidaMaxima = 100;
	this.vida = this.vidaMaxima;
	this.GT_ESPERA = new GestorTiempo();
	this.GT_ATACADO = new GestorTiempo();
	this.GT_CURACION = new GestorTiempo();
	this.vidaRegen = 1;
	this.direccion = Direccion.ESTE;
    }

    public Rectangle getRectangulo() {
	return new Rectangle((int) this.x, (int) this.y, this.ANCHO, this.ALTO);
    }

    @Override
    public void pintar(final Graphics2D g) {
	this.pintarIndicadorVida(g);
	if (Constantes.TECLADO.TECLA_VER_COLISIONES.presionado()) {
	    DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.cyan);
	}

	if (Constantes.CAMARA.getEntidadEnfocada() == this && this.recorridoA != null) {

	    g.setFont(g.getFont().deriveFont(7f));
	    int pos = 1;
	    String txt = String.valueOf(pos);
	    for (final NodoA n : this.recorridoA) {
		DibujoDebug.dibujarRectanguloContornoRefCamara(g, n.getAreaEnMundo(), Color.magenta);
		DibujoDebug.dibujarStringRefCamara(g, txt, n.getAreaEnMundo().x + n.getAreaEnMundo().width / 2 - Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, txt) / 2,
			n.getAreaEnMundo().y + n.getAreaEnMundo().height / 2 + Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, txt) / 2, Color.black);
		pos++;
		txt = String.valueOf(pos);
	    }
	    if (this.nodoADestino != null) {
		DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.nodoADestino.getAreaEnMundo(), Color.yellow);
	    }
	}
    }

    protected void pintarIndicadorVida(final Graphics2D g) {

	final Rectangle indicador = new Rectangle(this.getPosicionXInt() - 1, this.getPosicionYInt() - 5, this.ANCHO + 2, 4);
	final int porcentajeVida = (int) (this.vida * 100 / this.vidaMaxima);
	final int pocentajeBarraVidaActual = porcentajeVida * this.ANCHO / 100;
	final Rectangle barraVidaActual = new Rectangle(this.getPosicionXInt(), this.getPosicionYInt() - 4, pocentajeBarraVidaActual, 2);
	DibujoDebug.dibujarRectanguloRellenoRefCamara(g, indicador, Color.BLACK);
	DibujoDebug.dibujarRectanguloRellenoRefCamara(g, barraVidaActual, Color.RED);
	g.setFont(g.getFont().deriveFont(4f));
	final String texto = String.valueOf(this.vida) + "/" + String.valueOf(this.vidaMaxima);
	final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
	int xTexto = this.getPosicionXInt();
	if (anchoTexto > this.ANCHO) {
	    xTexto -= (anchoTexto - this.ANCHO) / 2;
	} else if (anchoTexto < this.ANCHO) {
	    xTexto += (this.ANCHO - anchoTexto) / 2;
	}
	DibujoDebug.dibujarStringRefCamara(g, texto, xTexto, this.getPosicionYInt() - 6, Color.white);
	g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
    }

    protected void moverANodoADestino() {

	if (this.getPosicionYInt() < this.nodoADestino.getAreaEnMundo().y) {
	    if ((this.nodoADestino.getAreaEnMundo().y - this.getPosicionYInt()) < this.velocidad) {
		this.y = this.nodoADestino.getAreaEnMundo().y;
	    } else {
		this.modificarPosicionY(this.velocidad);
		this.direccion = Direccion.SUR;

//					this.y += this.velocidad;
	    }
	} else if (this.getPosicionYInt() > this.nodoADestino.getAreaEnMundo().y) {
	    if ((this.getPosicionYInt() - this.nodoADestino.getAreaEnMundo().y) < this.velocidad) {
		this.y = this.nodoADestino.getAreaEnMundo().y;
	    } else {
//					this.y -= this.velocidad;
		this.modificarPosicionY(-this.velocidad);

		this.direccion = Direccion.NORTE;
	    }
	}

	if (this.getPosicionXInt() < this.nodoADestino.getAreaEnMundo().x) {
	    if ((this.nodoADestino.getAreaEnMundo().x - this.getPosicionXInt()) < this.velocidad) {
		this.x = this.nodoADestino.getAreaEnMundo().x;
	    } else {
//					this.x += this.velocidad;
		this.modificarPosicionX(this.velocidad);
		this.direccion = Direccion.ESTE;
	    }

	} else if (this.getPosicionXInt() > this.nodoADestino.getAreaEnMundo().x) {
	    if ((this.getPosicionXInt() - this.nodoADestino.getAreaEnMundo().x) < this.velocidad) {
		this.x = this.nodoADestino.getAreaEnMundo().x;
	    } else {
		this.modificarPosicionX(-this.velocidad);
		this.direccion = Direccion.OESTE;
//					this.x -= this.velocidad;
	    }
	}

	if (this.nodoADestino.compararPosicionesMundo(this.getPosicionXInt(), this.getPosicionYInt())
		&& (this.getPosicionXInt() == this.nodoADestino.getAreaEnMundo().x && this.getPosicionYInt() == this.nodoADestino.getAreaEnMundo().y)) {
	    if (this.recorridoA.hasNext()) {
		this.nodoADestino = this.recorridoA.getNext();
	    }

	}
    }

    protected void establecerVelocidadStardar() {
	this.velocidad = this.velocidadEstandar;
    }

    public double getVida() {
	return this.vida;
    }

    public double getVidaMaxima() {
	return this.vidaMaxima;
    }

    public boolean vidaCompleta() {
	return this.vida == this.vidaMaxima;
    }

    /**
     * Reduce la vida actual de la criatura (NO la vida maxima). En caso de que la
     * vida baje de 0 puntos se establecera el valor de la vida a 0 y se establecera
     * en TRUE al atributo eliminado.
     * 
     * @param puntos
     */
    public void reducirVida(final double puntos) {
	if (this.vida - puntos < 0) {
	    this.vida = 0;
	} else {
	    this.vida -= puntos;
	}

	if (this.vida <= 0) {
	    this.eliminar();
	}
    }

    /**
     * Establece la vda maxima de la criatura. La vida actual tambien tomara este
     * valor.
     * 
     * @param puntos
     */
    public void establecerVidaMaxima(final double puntos) {
	this.vidaMaxima = puntos;
	this.vida = puntos;
    }

    /**
     * Aumenta la vida maxima a la criatura. Por ende a los puntos de vida actual
     * tambien se le sumaran estos puntos.
     * 
     * @param puntos Los puntos a aumentar.
     */
    public void aumentarVidaMaxima(final double puntos) {
	this.vidaMaxima += puntos;
	this.vida += puntos;
    }

    /**
     * Reduce la vida maxima a la criatura. Por ende los puntos de vida actual se
     * estableceran al mismo valor en caso de que haya estado la vida al 100%.
     * 
     * @param puntos Los puntos a reducir.
     */
    public void reducirVidaMaxima(final double puntos) {
	if (this.vidaMaxima - puntos < 50) {
	    this.vidaMaxima = 50;
	} else {
	    this.vidaMaxima -= puntos;
	}
	if (this.vida > this.vidaMaxima) {
	    this.vida = this.vidaMaxima;
	}
    }

    /**
     * Cura a la criatura los puntos mencionados. Si se supera la vida maxima al
     * curar se limitara a este mismo la curacion.
     * 
     * @param puntos Los puntos de vida a curar.
     */
    public void curar(final double puntos) {
	if (this.vida + puntos > this.vidaMaxima) {
	    this.vida = this.vidaMaxima;
	} else {
	    this.vida += puntos;
	}
    }

    protected void meterEstado(final Estado estado) {
	if (this.ESTADO.containsKey(estado)) {
	} else {
	    this.ESTADO.put(estado, estado);
	}
    }

    protected void sacarEstado(final Estado estado) {
	if (this.ESTADO.containsKey(estado)) {
	    this.ESTADO.remove(estado);
	}
    }

    protected void setEstadoEstandar() {
	this.ESTADO.clear();
	this.meterEstado(Estado.ESTANDAR);
    }

    protected void setEstadoCaminando() {
	this.sacarEstado(Estado.ESTANDAR);
	this.sacarEstado(Estado.CORRIENDO);
	this.meterEstado(Estado.CAMINANDO);
    }

    protected void setEstadoCorriendo() {
	this.sacarEstado(Estado.ESTANDAR);
	this.sacarEstado(Estado.CAMINANDO);
	this.meterEstado(Estado.CORRIENDO);
    }

    public String getStringEstados() {
	final StringBuilder sb = new StringBuilder();
	for (final Estado e : this.ESTADO.values()) {
	    sb.append(e.toString() + "  ");
	}
	return sb.toString();
    }

    /**
     * Establece la vida de la criatura a la mencionada. Si este valor supera la
     * vida maxima se establecera el valor de la vida maxima para la vida.
     * 
     * @param puntos La vida a establecer
     */
    public void establecerVida(final double puntos) {
	if (puntos > this.vidaMaxima) {
	    this.vida = this.vidaMaxima;
	} else if (puntos <= 0) {
	    this.vida = 0;
	    this.eliminar();
	} else {
	    this.vida = puntos;
	}
    }

    /**
     * Cura completamente a la entidad.
     */
    public void sanar() {
	this.vida = this.vidaMaxima;
    }

    public Point getPosicion() {
	return new Point((int) this.x, (int) this.y);
    }

    public Point getPosicionTile() {
	return new Point(((int) this.x) / Constantes.LADO_TILE, ((int) this.y) / Constantes.LADO_TILE);
    }

    public Direccion getDireccion() {
	return this.direccion;
    }

    public boolean atrasDeComplemento() {
	return this.atrasDeComplemento;
    }

    public void setPosicionX(final int x) {
	this.x = x;
    }

    public void setPosicionY(final int y) {
	this.y = y;
    }

    protected int getPosicionXIntDibujado() {
	return (int) this.x - this.margenXInicialSprite;
    }

    protected int getPosicionYIntDibujado() {
	return (int) this.y - this.margenYInicialSprite;
    }

    public int getMargenXSprite() {
	return this.margenXInicialSprite;
    }

    public int getMargenYSprite() {
	return this.margenYInicialSprite;
    }

    @Override
    public void eliminar() {
	this.eliminado = true;
    }

    @Override
    public int getPosicionXInt() {
	return (int) this.x;
    }

    @Override
    public int getPosicionYInt() {
	return (int) this.y;
    }

    @Override
    public double getPosicionX() {
	return this.x;
    }

    @Override
    public double getPosicionY() {
	return this.y;
    }

    @Override
    public void modificarPosicionX(final double desplazamientoX) {
	if (desplazamientoX > 0) {
	    this.direccion = Direccion.ESTE;
	} else if (desplazamientoX < 0) {
	    this.direccion = Direccion.OESTE;
	}
	this.x += desplazamientoX;
    }

    @Override
    public void modificarPosicionY(final double desplazamientoY) {
	if (desplazamientoY > 0) {
	    this.direccion = Direccion.SUR;
	} else if (desplazamientoY < 0) {
	    this.direccion = Direccion.NORTE;
	}
	this.y += desplazamientoY;
    }

    @Override
    public boolean estaEliminado() {
	return this.eliminado;
    }

    public void recibirAtaque(final double damage, final Ente causante) {
	this.mundo.agregarParticula(new Sangre(this.getPosicionXInt() + this.getArea().width / 2, this.getPosicionYInt() + this.getArea().height / 2));
    }

    public abstract void establecerMargenesSprite();

    protected abstract JSONObject exportarParaJSON();

    public abstract String exportarTipoCriatura();

    protected void setDireccionMirandoCriatura(final Criatura c) {

	this.direccion = Constantes.FUNCIONES.getDireccionMirando(this.getPosicionXInt(), this.getPosicionYInt(), c.getPosicionXInt(), c.getPosicionYInt());
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJsonCriatura() {
	final JSONObject datosCriatura = this.exportarParaJSON();
	final JSONObject criatura = new JSONObject();
	criatura.put("tipo", this.exportarTipoCriatura());
	criatura.put("entiti", datosCriatura);
	return criatura;
    }

    public int getAncho() {
	return this.ANCHO;
    }

    public int getAlto() {
	return this.ALTO;
    }

    @Override
    public Rectangle getArea() {
	return new Rectangle(this.getPosicionXInt(), this.getPosicionYInt(), this.ANCHO, this.ALTO);
    }

    @Override
    public void setMundo(final Mundo mundo) {
	super.setMundo(mundo);
	this.aEstrella = new AEstrella(this.mundo, new Dimension(this.ANCHO, this.ALTO));
    }

    public int getCentroX() {
	return this.getPosicionXInt() + this.ANCHO / 2;
    }

    public int getCentroY() {
	return this.getPosicionYInt() + this.ALTO / 2;
    }

}
