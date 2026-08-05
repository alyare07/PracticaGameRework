package principal.maquinaestado.estados.pantallaCarga;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import principal.entes.AsistenteCamara;
import principal.mapa.Mundo;
import principal.mapa.escenario.EscenarioLoader;
import principal.maquinaestado.estados.EstadoJuego;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.Textura;

public class PantallaCarga implements EstadoJuego {
	protected final GestorCarga GC;
	protected final BufferedImage FONDO;
	protected final Dimension DIMENSION = new Dimension(Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO);
	protected final int MARGEN_MARCO = 50;
	protected final Rectangle MARCO_SUPERIOR = new Rectangle(0, 0, this.DIMENSION.width, this.MARGEN_MARCO);
	protected final Rectangle MARCO_INFERIOR = new Rectangle(0, this.DIMENSION.height - this.MARGEN_MARCO, this.DIMENSION.width, this.MARGEN_MARCO);
	protected final Rectangle MARCO_IZQUIERDA = new Rectangle(0, this.MARCO_SUPERIOR.y+this.MARCO_SUPERIOR.height, this.MARGEN_MARCO, this.DIMENSION.height-this.MARCO_INFERIOR.height-this.MARCO_SUPERIOR.height);
	protected final Rectangle MARCO_DERECHA = new Rectangle(this.DIMENSION.width-this.MARGEN_MARCO, this.MARCO_SUPERIOR.y+MARCO_SUPERIOR.height, this.MARGEN_MARCO,  this.DIMENSION.height-this.MARCO_INFERIOR.height-this.MARCO_SUPERIOR.height);
	protected Mundo mundo;
	protected final AsistenteCamara AC = new AsistenteCamara(this.DIMENSION.width/2 -1 ,this.DIMENSION.height/2 -1, 2,2);
	public PantallaCarga(final GestorCarga gc) {
		this.FONDO = Textura.crearTextura(new Color(61, 81, 61), this.DIMENSION.width, this.DIMENSION.height);
		this.GC = gc;
	}

	public PantallaCarga(final GestorCarga gc, final BufferedImage fondo) {
		this.FONDO = fondo;
		this.GC = gc;
	}

	public PantallaCarga(final GestorCarga gc, final Color fondo) {
		this.FONDO = Textura.crearTextura(fondo, this.DIMENSION.width, this.DIMENSION.height);
		this.GC = gc;
	}
	
	public PantallaCarga(final GestorCarga gc, final String ruta) {
		this.FONDO = Textura.crearTextura(Color.cyan,1,1);
		this.GC = gc;
		this.mundo = new Mundo(EscenarioLoader.importarEscenario(new File(ruta)), new Point(this.DIMENSION.width/2,this.DIMENSION.height/2));
		Constantes.CAMARA.setEntidadEnfocada(AC);
	}

	@Override
	public void actualizar() {
	}

	@Override
	public void pintar(Graphics2D g) {
		if(this.mundo != null) {
			this.mundo.pintar(g);
		}else {
			DibujoDebug.dibujarImagen(g, FONDO, 0, 0);
		}
		this.pintarMarcos(g);
		this.pintarTextoCarga(g);
	}
	
	
	private void pintarTextoCarga(final Graphics2D g) {
		g.setFont(g.getFont().deriveFont(10f));
		final int porcentajeCarga = this.GC.getPorcentaje();
		final String texto = this.GC.getDetalleCarga()+"..."+porcentajeCarga+"%";
		final int anchoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAnchoPixeles(g, texto);
		final int altoTexto = Constantes.FUNCIONES.MEDIDOR_STRING.medirAltoPixeles(g, texto);
		final int x = this.DIMENSION.width/2 - anchoTexto/2;
		final int y = (int) (this.DIMENSION.height * 0.90) + altoTexto;
		DibujoDebug.dibujarString(g, texto, x, y, Color.green);
	}
	
	private void pintarMarcos(final Graphics2D G) {
		DibujoDebug.dibujarRectanguloRelleno(G, MARCO_SUPERIOR, Color.black);
		DibujoDebug.dibujarRectanguloRelleno(G, MARCO_INFERIOR, Color.black);
		DibujoDebug.dibujarRectanguloRelleno(G, MARCO_IZQUIERDA, Color.black);
		DibujoDebug.dibujarRectanguloRelleno(G, MARCO_DERECHA, Color.black);
		
	}

}
