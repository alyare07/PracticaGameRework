package principal.entes.objetos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.modelos.complemento.ListaModeloComplemento;
import principal.entes.modelos.complemento.ModeloComplementoT1;
import principal.entes.modelos.complemento.ModeloComplementoT2;
import principal.utilidades.Render2D;
import principal.utilidades.Globales;

/**
 * Representa elementos escénicos del mapa como árboles, casas, rocas y muros.
 * <p>
 * <b>Optimizaciones de Memoria:</b>
 * <ul>
 * <li>Corrige la asignación oculta de {@code new Rectangle()} en
 * {@link #getArea()}, reutilizando la estructura fija
 * {@link #AREA_ENTE_RETORNO} de {@link principal.entes.Ente}.</li>
 * <li>Integra el pivote de profundidad {@link #getPosicionYBase()} para que las
 * copas de los árboles y techos de casas tapen al jugador correctamente al
 * pasar por detrás.</li>
 * </ul>
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public class Complemento extends Objeto {

	private static final long serialVersionUID = -2759528530038714828L;
	private final int COD_MODELO_COMPLEMENTO;

	public Complemento(final int x, final int y, final int codModeloComplemento) {
		super(x, y);
		this.COD_MODELO_COMPLEMENTO = codModeloComplemento;
	}

	public void pintarAreaInterseccion(final Graphics2D g) {
		if (ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO) instanceof ModeloComplementoT1) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getAreaInterseccionEnBaseMargen(
					((ModeloComplementoT1) ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO))
							.getMargenesInterseccion()),
					Color.ORANGE);
		} else if (ListaModeloComplemento
				.getModeloComplemento(this.COD_MODELO_COMPLEMENTO) instanceof ModeloComplementoT2) {
			for (final Rectangle margen : ((ModeloComplementoT2) ListaModeloComplemento
					.getModeloComplemento(this.COD_MODELO_COMPLEMENTO)).getMargenesInterseccion()) {
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
		return new Rectangle(this.getPosicionXInt() + margen.x, this.getPosicionYInt() + margen.y,
				this.getAncho() - margen.width - margen.x, (this.getAlto() - margen.height - margen.y));
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (!Globales.TECLADO.TECLA_OCULTAR_COMPLEMENTOS.presionado()) {
			if (ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).animar()) {
				ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAnimacion().pintar(g,
						this.getPosicionXInt(), this.getPosicionYInt());
			} else {
				Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt(),
						this.getPosicionYInt());
			}
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado()) {
			this.pintarAreaInterseccion(g);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.BLACK);
		}
	}

	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", this.getPosicionXInt());
		json.put("y", this.getPosicionYInt());
		json.put("codModelo", this.getCodigoModelo());
		return json;
	}

	public boolean intersectaAreaNoSolida(final Shape area) {
		if (area.intersects(this.getArea())
				&& ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).contieneZonaNoSolida()) {
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
		return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).intersecta(s, this);
	}

	@Override
	public boolean esSolido() {
		return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).esSolido();
	}

	@Override
	public Objeto copiar() {
		return new Complemento(this.getPosicionXInt(), this.getPosicionYInt(), this.COD_MODELO_COMPLEMENTO);
	}

	@Override
	public BufferedImage getTextura() {
		return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getTextura();
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
		return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAncho();
	}

	@Override
	public int getAlto() {
		return ListaModeloComplemento.getModeloComplemento(this.COD_MODELO_COMPLEMENTO).getAlto();
	}

	/**
	 * Retorna el delimitador rectangular reutilizando la estructura fija de Ente
	 * (Zero-GC).
	 */
	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.getAncho(),
				this.getAlto());
		return this.AREA_ENTE_RETORNO;
	}

}