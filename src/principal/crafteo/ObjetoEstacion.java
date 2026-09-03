package principal.crafteo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.objetos.Objeto;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

/**
 * Objeto físico del mapa que implementa EstacionInteractiva para habilitar
 * recetas de crafteo avanzadas cuando el jugador está en su rango.
 * 
 * @version 2.0 (Vanilla Java 8 - GestorTexturas Pipeline)
 */
public class ObjetoEstacion extends Objeto implements EstacionInteractiva {

	private static final long serialVersionUID = 1L;

	private final EstacionCrafteo tipoEstacion;
	private final BufferedImage textura;
	private final int ancho;
	private final int alto;
	private final boolean solido;

	public ObjetoEstacion(final int x, final int y, final EstacionCrafteo tipoEstacion, final BufferedImage textura,
			final int ancho, final int alto, final boolean solido) {
		super(x, y);
		this.tipoEstacion = (tipoEstacion != null) ? tipoEstacion : EstacionCrafteo.MESA_TRABAJO;
		this.textura = textura;
		this.ancho = Math.max(8, ancho);
		this.alto = Math.max(8, alto);
		this.solido = solido;
	}

	public ObjetoEstacion(final int x, final int y, final EstacionCrafteo tipoEstacion, final ClaveHoja hoja,
			final int spriteIndex, final int ancho, final int alto, final boolean solido) {
		this(x, y, tipoEstacion, resolverTextura(hoja, spriteIndex), ancho, alto, solido);
	}

	@Override
	public EstacionCrafteo getTipoEstacion() {
		return this.tipoEstacion;
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		if (img != null) {
			Render2D.dibujarImagenRefCamara(g, img, this.getPosicionXInt(), this.getPosicionYInt());
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.CYAN);
		}
	}

	@Override
	public BufferedImage getTextura() {
		return (this.textura != null) ? this.textura : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public int getAncho() {
		return this.ancho;
	}

	@Override
	public int getAlto() {
		return this.alto;
	}

	@Override
	public boolean esSolido() {
		return this.solido;
	}

	@Override
	public Objeto copiar() {
		return new ObjetoEstacion(this.getPosicionXInt(), this.getPosicionYInt(), this.tipoEstacion, this.textura,
				this.ancho, this.alto, this.solido);
	}

	private static BufferedImage resolverTextura(final ClaveHoja hoja, final int index) {
		final HojaSprite h = Globales.GESTOR_TEXTURAS.getHoja(hoja);
		return (h != null) ? h.getSprite(index) : Globales.GESTOR_TEXTURAS.getTexturaError();
	}
}