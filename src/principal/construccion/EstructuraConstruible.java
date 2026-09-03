package principal.construccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.pociones.PocionVidaMenor;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

public class EstructuraConstruible extends Objeto {

	private static final long serialVersionUID = 1L;

	protected final TipoEstructura tipo;
	protected double vida;
	protected double vidaMaxima;

	public EstructuraConstruible(final int x, final int y, final TipoEstructura tipo) {
		super(x, y);
		this.tipo = tipo;
		this.vidaMaxima = (tipo != null) ? tipo.getDurabilidadMaxima() : 100.0;
		this.vida = this.vidaMaxima;
	}

	public void recibirAtaque(final double danio, final Ente causante) {
		if (this.eliminado) {
			return;
		}

		this.vida -= Math.max(1.0, danio);
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 4);

		if (this.vida <= 0.0) {
			this.destruir();
		}
	}

	public void destruir() {
		if (this.mundo != null) {
			final int dropCant = Math.max(1, this.tipo.getCantidadMaterialRequerido() / 2);
			this.mundo.meterEntidad(new PocionVidaMenor(this.getCentroX() - 4, this.getCentroY() - 4, dropCant));
			Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 15);
			this.mundo.notificarModificacionEstructura();
		}
		this.eliminar();
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		if (img != null) {
			Render2D.dibujarImagenRefCamara(g, img, this.getPosicionXInt(), this.getPosicionYInt());
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.YELLOW);
		}
	}

	@Override
	public BufferedImage getTextura() {
		return (this.tipo != null) ? this.tipo.getTextura() : Globales.GESTOR_TEXTURAS.getTexturaError();
	}

	@Override
	public int getAncho() {
		return (this.tipo != null) ? this.tipo.getAncho() : 16;
	}

	@Override
	public int getAlto() {
		return (this.tipo != null) ? this.tipo.getAlto() : 16;
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		return new EstructuraConstruible(this.getPosicionXInt(), this.getPosicionYInt(), this.tipo);
	}

	public TipoEstructura getTipo() {
		return this.tipo;
	}

	public double getVida() {
		return this.vida;
	}

	public double getVidaMaxima() {
		return this.vidaMaxima;
	}
}