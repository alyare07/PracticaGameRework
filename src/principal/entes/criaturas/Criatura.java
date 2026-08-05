package principal.entes.criaturas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.json.simple.JSONObject;
import principal.entes.Ente;
import principal.entes.objetos.particulas.Sangre;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.HojaSprite;

public abstract class Criatura extends Ente {

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

	protected final int ANCHO;
	protected final int ALTO;
	protected final HojaSprite hoja;
	protected double velocidad = 1;
	protected double x;
	protected double y;
	protected BufferedImage actualPerfil;
	protected double vida;
	protected double vidaMaxima;
	protected double velocidadEstandar = 0.5;
	protected final GestorTiempo GT_ESPERA;// tiempo de la accion esperar
	protected final GestorTiempo GT_ATACADO; // tiempo del ultimo ataque recibido
	protected final GestorTiempo GT_CURACION;
	protected double vidaRegen;
	protected Direccion direccion;
	protected boolean atrasDeComplemento;
	
	public Criatura(final double x, final double y, final int ancho, final int alto, BufferedImage hoja) {
		ANCHO = ancho;
		ALTO = alto;
		this.x = x;
		this.y = y;
		establecerVelocidadStardar();
		this.hoja = new HojaSprite(hoja, ancho, false);
		this.actualPerfil = this.hoja.getSprite(0);
		this.vidaMaxima = 100;
		this.vida = vidaMaxima;
		this.GT_ESPERA = new GestorTiempo();
		this.GT_ATACADO = new GestorTiempo();
		this.GT_CURACION = new GestorTiempo();
		this.vidaRegen = 1;
		this.direccion = Direccion.ESTE;
	}

	public Criatura(final double x, final double y, final int ancho, final int alto, final double vida, final double vidaMaxima, BufferedImage hoja) {
		ANCHO = ancho;
		ALTO = alto;
		this.x = x;
		this.y = y;
		establecerVelocidadStardar();
		this.hoja = new HojaSprite(hoja, ancho, false);
		this.actualPerfil = this.hoja.getSprite(0);
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
	
	public Criatura(final double x, final double y, final int ancho, final int alto, final double velocidad, final Color color) {
		ANCHO = ancho;
		ALTO = alto;
		this.x = x;
		this.y = y;
		this.velocidadEstandar = velocidad;
		establecerVelocidadStardar();
		BufferedImage hoja = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		{
			final Graphics2D g = (Graphics2D) hoja.getGraphics();
			g.setColor(color);
			g.fillRect(0, 0, ancho, alto);
			g.dispose();
		}
		this.hoja = new HojaSprite(hoja, ancho, false);
		this.actualPerfil = this.hoja.getSprite(0);
		this.vidaMaxima = 100;
		this.vida = vidaMaxima;
		this.GT_ESPERA = new GestorTiempo();
		this.GT_ATACADO = new GestorTiempo();
		this.GT_CURACION = new GestorTiempo();
		this.vidaRegen = 1;
		this.direccion = Direccion.ESTE;
	}

	public Rectangle getRectangulo() {
		return new Rectangle((int) x, (int) y, ANCHO, ALTO);
	}

	@Override
	public void pintar(Graphics2D g) {
		pintarIndicadorVida(g);
		if(this.atrasDeComplemento) {
			DibujoDebug.dibujarImagenConTransparenciaRefCamara(g, actualPerfil, getPosicionXInt(), getPosicionYInt(), 0.5f);
		}else {
			DibujoDebug.dibujarImagenRefCamara(g, actualPerfil, getPosicionXInt(), getPosicionYInt());
		}
		if(Constantes.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			DibujoDebug.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.cyan);
		}
	}


	public BufferedImage getSpriteActual() {
		return actualPerfil;
	}

	protected void pintarIndicadorVida(final Graphics2D g) {

		final Rectangle indicador = new Rectangle(getPosicionXInt() - 1, getPosicionYInt() - 5, this.ANCHO + 2, 4);
		final int porcentajeVida = (int) (this.vida * 100 / this.vidaMaxima);
		final int pocentajeBarraVidaActual = porcentajeVida * this.ANCHO / 100;
		final Rectangle barraVidaActual = new Rectangle(getPosicionXInt(), getPosicionYInt() - 4, pocentajeBarraVidaActual, 2);
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, indicador, Color.BLACK);
		DibujoDebug.dibujarRectanguloRellenoRefCamara(g, barraVidaActual, Color.RED);
		g.setFont(g.getFont().deriveFont(4f));
		final String texto = String.valueOf(this.vida) + "/" + String.valueOf(this.vidaMaxima);
		final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		int xTexto = getPosicionXInt();
		if(anchoTexto > this.ANCHO) {
			xTexto -= (anchoTexto - this.ANCHO)/2;
		}else if(anchoTexto < this.ANCHO) {
			xTexto += (this.ANCHO-anchoTexto)/2;
		}
		DibujoDebug.dibujarStringRefCamara(g, texto, xTexto, getPosicionYInt() - 6, Color.white);
		g.setFont(g.getFont().deriveFont(Constantes.TAMANO_FUENTE));
	}

	protected void establecerVelocidadStardar() {
		this.velocidad = velocidadEstandar;
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
	 * Reduce la vida actual de la criatura (NO la vida maxima).
	 * En caso de que la vida baje de 0 puntos se establecera el 
	 * valor de la vida a 0 y se establecera en TRUE al atributo
	 * eliminado.
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
	 * Establece la vda maxima de la criatura.
	 * La vida actual tambien tomara este valor.
	 * @param puntos
	 */
	public void establecerVidaMaxima(final double puntos) {
		this.vidaMaxima = puntos;
		this.vida = puntos;
	}
	/**
	 * Aumenta la vida maxima a la criatura. Por ende a los puntos
	 * de vida actual tambien se le sumaran estos puntos.
	 * @param puntos Los puntos a aumentar.
	 */
	public void aumentarVidaMaxima(final double puntos) {
		this.vidaMaxima += puntos;
		this.vida += puntos;
	}
	/**
	 * Reduce la vida maxima a la criatura. Por ende los puntos
	 * de vida actual se estableceran al mismo valor en caso
	 * de que haya estado la vida al 100%.
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
	 * Cura a la criatura los puntos mencionados. Si se 
	 * supera la vida maxima al curar se limitara a este
	 * mismo la curacion.
	 * @param puntos Los puntos de vida a curar.
	 */
	public void curar(final double puntos) {
		if (this.vida + puntos > this.vidaMaxima) {
			this.vida = this.vidaMaxima;
		} else {
			this.vida += puntos;
		}
	}
	/**
	 * Establece la vida de la criatura a la mencionada.
	 * Si este valor supera la vida maxima se establecera
	 * el valor de la vida maxima para la vida.
	 * @param puntos La vida a establecer
	 */
	public void establecerVida(final double puntos) {
		if(puntos > this.vidaMaxima) {
			this.vida = this.vidaMaxima;
		}else if(puntos <= 0) {
			this.vida = 0;
			this.eliminar();
		}else {
			this.vida = puntos;
		}
	}
	/**
	 * Cura completamente a la entidad.
	 */
	public void sanar() {
		this.vida = vidaMaxima;
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
		this.x += desplazamientoX;
	}

	@Override
	public void modificarPosicionY(final double desplazamientoY) {
		this.y += desplazamientoY;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}
	

	public void recibirAtaque(final double damage, final Ente causante) {
		this.mundo.agregarParticula(new Sangre(this.getPosicionXInt()+this.getArea().width/2, this.getPosicionYInt()+this.getArea().height/2));
	}
	
	protected abstract  JSONObject exportarParaJSON();
	public abstract String exportarTipoCriatura();
	@SuppressWarnings("unchecked")
	public JSONObject getJsonCriatura() {
		JSONObject datosCriatura = exportarParaJSON();
		JSONObject criatura = new JSONObject();
		criatura.put("tipo", exportarTipoCriatura());
		criatura.put("entiti", datosCriatura);
		return criatura;
	}
	
	public int getAncho() {
		return this.ANCHO;
	}
	
	public int getAlto() {
		return this.ALTO;
	}
	
	public Rectangle getArea() {
		return new Rectangle(getPosicionXInt(), getPosicionYInt(), ANCHO, ALTO);
	}

}
