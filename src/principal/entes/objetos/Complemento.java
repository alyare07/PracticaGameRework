package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.TipoModeloComplemento;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Representa elementos escénicos del mapa (árboles, casas, rocas) respaldados
 * por el catálogo Flyweight {@link TipoModeloComplemento} con deformación
 * eólica reactiva para vegetación (Zero-GC / O(1)).
 */
public class Complemento extends Objeto {

	private static final long serialVersionUID = -2759528530038714828L;

	private final TipoModeloComplemento MODELO;
	private final Rectangle AREA_MARGENES_INTERSECCION_AUXILIAR = new Rectangle();

	public Complemento(final int x, final int y, final TipoModeloComplemento modelo) {
		super(x, y);
		this.MODELO = (modelo != null) ? modelo : TipoModeloComplemento.ARBOL_ROBLE;
	}

	public Complemento(final int x, final int y, final int codModeloComplemento) {
		this(x, y, TipoModeloComplemento.desdeId(codModeloComplemento));
	}

	public TipoModeloComplemento getModelo() {
		return this.MODELO;
	}

	public int getCodigoModelo() {
		return this.MODELO.getId();
	}

	public boolean compararModelos(final Complemento c) {
		return (c != null) && (c.MODELO == this.MODELO);
	}

	public void pintarAreaInterseccion(final Graphics2D g) {
		Render2D.dibujarRectanguloContornoRefCamara(g,
				this.getAreaInterseccionEnBaseMargen(this.MODELO.getMargenesInterseccion()), Color.ORANGE);
	}

	public Rectangle getAreaInterseccionEnBaseMargen(final Rectangle margen) {
		this.AREA_MARGENES_INTERSECCION_AUXILIAR.setBounds(this.getPosicionXInt() + margen.x,
				this.getPosicionYInt() + margen.y, this.getAncho() - margen.width - margen.x,
				(this.getAlto() - margen.height - margen.y));
		return this.AREA_MARGENES_INTERSECCION_AUXILIAR;
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado()) {
			final BufferedImage tex = this.getTextura();

			if (this.MODELO.esVegetacion() && (Globales.GESTOR_CLIMA != null)) {
				final double balanceo = Globales.GESTOR_CLIMA.getFactorBalanceoVegetacion(this.getPosicionX(),
						this.getPosicionY());
				if (balanceo != 0.0) {
					Render2D.dibujarImagenConBalanceoRefCamara(g, tex, this.getPosicionXInt(), this.getPosicionYInt(),
							balanceo, 40);
				} else {
					Render2D.dibujarImagenRefCamara(g, tex, this.getPosicionXInt(), this.getPosicionYInt());
				}
			} else {
				Render2D.dibujarImagenRefCamara(g, tex, this.getPosicionXInt(), this.getPosicionYInt());
			}
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			this.pintarAreaInterseccion(g);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("codModelo", Integer.valueOf(this.getCodigoModelo()));
		return json;
	}

	public static Complemento crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int codModelo = Integer.parseInt(json.get("codModelo").toString());
		return new Complemento(x, y, codModelo);
	}

	public boolean intersectaAreaNoSolida(final Shape area) {
		if (area.intersects(this.getArea()) && this.MODELO.contieneZonaNoSolida()) {
			return !this.intersecta(area);
		}
		return false;
	}

	@Override
	public boolean intersecta(final Shape s) {
		return this.MODELO.intersecta(s, this);
	}

	@Override
	public boolean esSolido() {
		return this.MODELO.esSolido();
	}

	@Override
	public Objeto copiar() {
		return new Complemento(this.getPosicionXInt(), this.getPosicionYInt(), this.MODELO);
	}

	@Override
	public BufferedImage getTextura() {
		return this.MODELO.getTextura();
	}

	@Override
	public void eliminar() {
		this.eliminado = true;
	}

	@Override
	public boolean estaEliminado() {
		return this.eliminado;
	}

	@Override
	public int getAncho() {
		return this.MODELO.getAncho();
	}

	@Override
	public int getAlto() {
		return this.MODELO.getAlto();
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}
}