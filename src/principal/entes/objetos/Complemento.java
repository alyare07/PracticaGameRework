package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Representa elementos escénicos del mapa (árboles, casas, rocas) con
 * deformación eólica reactiva para vegetación (Zero-GC / O(1)).
 * 
 * @version 2.2 (Vanilla Java 8 - Selective Wind Swaying)
 */
public class Complemento extends Objeto {

	private static final long serialVersionUID = -2759528530038714828L;
	private final int COD_MODELO_COMPLEMENTO;
	private final Rectangle AREA_MARGENES_INTERSECCION_AUXILIAR = new Rectangle();

	public Complemento(final int x, final int y, final int codModeloComplemento) {
		super(x, y);
		this.COD_MODELO_COMPLEMENTO = codModeloComplemento;
	}

	public void pintarAreaInterseccion(final Graphics2D g) {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		if (modelo == null) {
			return;
		}

		if (modelo instanceof ModeloComplementoT1) {
			Render2D.dibujarRectanguloContornoRefCamara(g,
					this.getAreaInterseccionEnBaseMargen(((ModeloComplementoT1) modelo).getMargenesInterseccion()),
					Color.ORANGE);
		} else if (modelo instanceof ModeloComplementoT2) {
			for (final Rectangle margen : ((ModeloComplementoT2) modelo).getMargenesInterseccion()) {
				Render2D.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionEnBaseMargen(margen),
						Color.ORANGE);
			}
		}
	}

	public int getCodigoModelo() {
		return this.COD_MODELO_COMPLEMENTO;
	}

	public boolean compararModelos(final Complemento c) {
		return (c != null) && (c.COD_MODELO_COMPLEMENTO == this.COD_MODELO_COMPLEMENTO);
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
			final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);

			if (modelo != null) {
				if (modelo.animar()) {
					modelo.getAnimacion().pintar(g, this.getPosicionXInt(), this.getPosicionYInt());
				} else if (modelo.esVegetacion() && (Globales.GESTOR_CLIMA != null)) {
					final double balanceo = Globales.GESTOR_CLIMA.getFactorBalanceoVegetacion(this.getPosicionX(),
							this.getPosicionY());
					if (balanceo != 0.0) {
						Render2D.dibujarImagenConBalanceoRefCamara(g, this.getTextura(), this.getPosicionXInt(),
								this.getPosicionYInt(), balanceo);
					} else {
						Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(),
								this.getPosicionYInt());
					}
				} else {
					Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(),
							this.getPosicionYInt());
				}
			}
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			this.pintarAreaInterseccion(g);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.getCodigoModelo());
		return json;
	}

	public boolean intersectaAreaNoSolida(final Shape area) {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		if ((modelo != null) && area.intersects(this.getArea()) && modelo.contieneZonaNoSolida()) {
			return !this.intersecta(area);
		}
		return false;
	}

	public static Complemento crearDesdeJson(final JSONObject json) {
		final int x = Integer.parseInt(json.get("x").toString());
		final int y = Integer.parseInt(json.get("y").toString());
		final int codModelo = Integer.parseInt(json.get("codModelo").toString());
		return new Complemento(x, y, codModelo);
	}

	@Override
	public boolean intersecta(final Shape s) {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		return (modelo != null) && modelo.intersecta(s, this);
	}

	@Override
	public boolean esSolido() {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		return (modelo != null) && modelo.esSolido();
	}

	@Override
	public Objeto copiar() {
		return new Complemento(this.getPosicionXInt(), this.getPosicionYInt(), this.COD_MODELO_COMPLEMENTO);
	}

	@Override
	public BufferedImage getTextura() {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		return (modelo != null) ? modelo.getTextura() : Globales.GESTOR_TEXTURAS.getTexturaError();
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
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		return (modelo != null) ? modelo.getAncho() : 32;
	}

	@Override
	public int getAlto() {
		final ModeloComplemento modelo = ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO);
		return (modelo != null) ? modelo.getAlto() : 32;
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}
}