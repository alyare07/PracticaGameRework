package principal.igu.textos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Partícula de texto flotante individual con física balística parabólica y
 * soporte dual de renderizado: Espacio de Mundo vs. Espacio de Pantalla Fijo
 * (Zero-GC).
 * 
 * @version 2.0 (Vanilla Java 8 - Dual Screen/World Support)
 */
public class TextoFlotante {

	private static final double GRAVEDAD = 180.0;

	private boolean activo;
	private String texto;
	private double posX;
	private double posY;
	private double velX;
	private double velY;

	private double duracionSegundos;
	private double tiempoTranscurrido;
	private Color colorTexto;
	private Font fuente;
	private boolean esCritico;

	public TextoFlotante() {
		this.activo = false;
		this.texto = "";
		this.colorTexto = Color.WHITE;
		this.fuente = new Font(Font.SANS_SERIF, Font.BOLD, (int) Constantes.TAMANO_FUENTE);
	}

	public void activar(final String texto, final double x, final double y, final TipoTextoFlotante tipo,
			final double dispersionX) {
		this.texto = texto;
		this.posX = x;
		this.posY = y;
		this.colorTexto = tipo.getColor();
		this.duracionSegundos = tipo.getDuracionMs() / 1000.0;
		this.tiempoTranscurrido = 0.0;
		this.esCritico = tipo.isCritico();

		final int estilo = this.esCritico ? Font.BOLD : Font.PLAIN;
		this.fuente = Globales.GESTOR_FUENTES.getFuente(Font.SANS_SERIF, estilo, (int) tipo.getTamanoFuente());

		this.velY = this.esCritico ? -110.0 : -75.0;
		this.velX = dispersionX * (this.esCritico ? 35.0 : 20.0);

		this.activo = true;
	}

	public void actualizar(final double dt) {
		if (!this.activo) {
			return;
		}

		this.tiempoTranscurrido += dt;

		if (this.tiempoTranscurrido >= this.duracionSegundos) {
			this.activo = false;
			return;
		}

		this.posX += this.velX * dt;
		this.posY += this.velY * dt;
		this.velY += GRAVEDAD * dt;
		this.velX *= (1.0 - (0.8 * dt));
	}

	/**
	 * Renderizado para el mundo: Proyectado con la cámara.
	 */
	public void pintar(final Graphics2D g) {
		if (!this.activo || this.texto.isEmpty()) {
			return;
		}

		final Font fuentePrevia = g.getFont();
		g.setFont(this.fuente);

		final int renderX = (int) Math.round(this.posX);
		final int renderY = (int) Math.round(this.posY);

		Render2D.dibujarStringConSombraRefCamara(g, this.texto, renderX, renderY, this.colorTexto, Color.BLACK);
		g.setFont(fuentePrevia);
	}

	/**
	 * Renderizado para la interfaz: Coordenadas fijas de pantalla (640x360).
	 */
	public void pintarFijo(final Graphics2D g) {
		if (!this.activo || this.texto.isEmpty()) {
			return;
		}

		final Font fuentePrevia = g.getFont();
		g.setFont(this.fuente);

		final int renderX = (int) Math.round(this.posX);
		final int renderY = (int) Math.round(this.posY);

		Render2D.dibujarStringConSombra(g, this.texto, renderX, renderY, this.colorTexto, Color.BLACK);
		g.setFont(fuentePrevia);
	}

	public boolean isActivo() {
		return this.activo;
	}
}