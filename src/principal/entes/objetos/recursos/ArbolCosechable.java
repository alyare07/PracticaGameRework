package principal.entes.objetos.recursos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.recursos.arboles.DispensadorBotinArbol;
import principal.entes.objetos.recursos.arboles.TipoArbol;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;

public class ArbolCosechable extends RecursoCosechable {

	private static final long serialVersionUID = 1L;

	public static final int ANCHO_BASE = 32;
	public static final int ALTO_BASE = 48;

	private final TipoArbol tipoArbol;
	private boolean esTocon = false;
	private DispensadorBotinArbol dispensadorBotin;

	public ArbolCosechable(final int x, final int y, final TipoArbol tipoArbol) {
		super(x, y, (tipoArbol != null ? tipoArbol.getDurabilidadBase() : 100.0), TipoHerramienta.HACHA);
		this.tipoArbol = (tipoArbol != null) ? tipoArbol : TipoArbol.ROBLE;
		this.dispensadorBotin = this.tipoArbol.getDispensadorPorDefecto();
	}

	public ArbolCosechable(final int x, final int y) {
		this(x, y, TipoArbol.ROBLE);
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		final int px = this.getPosicionXInt() + this.shakeOffsetX;
		final int py = this.getPosicionYInt();

		// Solo se mece con el viento si conserva la copa (el tocón es rígido)
		if (!this.esTocon && (Globales.GESTOR_CLIMA != null)) {
			final double balanceo = Globales.GESTOR_CLIMA.getFactorBalanceoVegetacion(px, py);
			if (balanceo != 0.0) {
				Render2D.dibujarImagenConBalanceoRefCamara(g, img, px, py, balanceo);
			} else {
				Render2D.dibujarImagenRefCamara(g, img, px, py);
			}
		} else {
			Render2D.dibujarImagenRefCamara(g, img, px, py);
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt() + this.tipoArbol.getColX(),
				this.getPosicionYInt() + this.tipoArbol.getColY(), this.tipoArbol.getColAncho(),
				this.tipoArbol.getColAlto());
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public void destruir(final Ente causante) {
		if (!this.esTocon) {
			this.soltarBotin();
			this.esTocon = true;
			this.durabilidadMaxima = Math.max(20.0, this.tipoArbol.getDurabilidadBase() * 0.40);
			this.durabilidad = this.durabilidadMaxima;
			this.activarShake();
			if (this.mundo != null) {
				this.mundo.notificarModificacionEstructura();
			}
		} else {
			this.soltarBotin();
			super.destruir(causante);
		}
	}

	@Override
	protected void soltarBotin() {
		if ((this.mundo != null) && (this.dispensadorBotin != null)) {
			this.dispensadorBotin.soltar(this, this.mundo, this.esTocon);
		}
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY() + 10, 6);
	}

	@Override
	public BufferedImage getTextura() {
		if (Globales.GESTOR_TEXTURAS == null) {
			return null;
		}
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBOLES_32x48);
		if (hoja == null) {
			return Globales.GESTOR_TEXTURAS.getTexturaError();
		}
		// Si es tocón, usa siempre el sprite 19
		final int idx = this.esTocon ? TipoArbol.SPRITE_TOCON : this.tipoArbol.getSpriteIndex();
		return hoja.getSprite(idx);
	}

	@Override
	public int getAncho() {
		return ANCHO_BASE;
	}

	@Override
	public int getAlto() {
		return ALTO_BASE;
	}

	@Override
	public Objeto copiar() {
		final ArbolCosechable copia = new ArbolCosechable(this.getPosicionXInt(), this.getPosicionYInt(),
				this.tipoArbol);
		copia.esTocon = this.esTocon;
		copia.durabilidad = this.durabilidad;
		copia.durabilidadMaxima = this.durabilidadMaxima;
		copia.dispensadorBotin = this.dispensadorBotin;
		return copia;
	}

	public TipoArbol getTipoArbol() {
		return this.tipoArbol;
	}

	public boolean isEsTocon() {
		return this.esTocon;
	}

	public void setEsTocon(final boolean esTocon) {
		this.esTocon = esTocon;
	}

	public void setDispensadorBotin(final DispensadorBotinArbol dispensador) {
		if (dispensador != null) {
			this.dispensadorBotin = dispensador;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void exportarDatosEspecificos(final JSONObject json) {
		json.put("esTocon", Boolean.valueOf(this.esTocon));
	}

	@Override
	protected void importarDatosEspecificos(final JSONObject json) {
		if (json.get("esTocon") != null) {
			this.esTocon = Boolean.parseBoolean(json.get("esTocon").toString());
		}
	}
}