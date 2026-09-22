package principal.entes.objetos.recursos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.comidas.BayaSilvestre;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.items.materiales.RecursoMaterial;
import principal.interaccion.Interactuable;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Arbusto silvestre cosechable mediante [E] y destructible con herramientas
 * (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8)
 */
public class ArbustoCosechable extends RecursoCosechable implements Interactuable {

	private static final long serialVersionUID = 1L;
	private final int ANCHO = 6;
	private final int ALTO = 3;
	public static final double TIEMPO_REGENERACION_HORAS = 24.0;
	public static final double DURABILIDAD_BASE = 25.0;
	private static final int xMargen = 5;
	private static final int yMargen = 12;
	/** Timestamp absoluto de la última recolección (-999 = listo para cosechar) */
	private double horaUltimaCosecha = -999.0;

	public ArbustoCosechable(final int x, final int y) {
		super(x, y, DURABILIDAD_BASE, TipoHerramienta.HACHA);
	}

	public ArbustoCosechable(final int x, final int y, final double horaUltimaCosecha) {
		super(x, y, DURABILIDAD_BASE, TipoHerramienta.HACHA);
		this.horaUltimaCosecha = horaUltimaCosecha;
	}

	// =========================================================================
	// === EVALUACIÓN PEREZOSA DE ESTADO (ZERO-GC)
	// =========================================================================

	public boolean tieneFruto() {
		if (Globales.GESTOR_ASTRONOMICO == null) {
			return true;
		}
		return (Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego()
				- this.horaUltimaCosecha) >= TIEMPO_REGENERACION_HORAS;
	}

	@Override
	public boolean estaModificado() {
		return super.estaModificado() || !this.tieneFruto();
	}

	// =========================================================================
	// === RENDERIZADO Y FÍSICA ESPACIAL
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage img = this.getTextura();
		final int px = this.getPosicionXInt() + this.shakeOffsetX;
		final int py = this.getPosicionYInt();

		// Balanceo sutil reactivo al viento del GestorClima
		if (Globales.GESTOR_CLIMA != null) {
			// Multiplicador x2.2 para compensar la menor altura (16 px vs 32 px)
			final double balanceo = Globales.GESTOR_CLIMA.getFactorBalanceoVegetacion(px, py) * 2.2;
			if (balanceo != 0.0) {
				Render2D.dibujarImagenConBalanceoRefCamara(g, img, px - xMargen, py - yMargen, balanceo);
			} else {
				Render2D.dibujarImagenRefCamara(g, img, px - xMargen, py - yMargen);
			}
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@Override
	public Rectangle getArea() {
		this.AREA_ENTE_RETORNO.setBounds(this.getPosicionXInt(), this.getPosicionYInt(), this.ANCHO, this.ALTO);
		return this.AREA_ENTE_RETORNO;
	}

	@Override
	public BufferedImage getTextura() {
		final HojaSprite hoja = Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.ARBUSTO_COSECHABLES);
		if (hoja == null) {
			return Globales.GESTOR_TEXTURAS.getTexturaError();
		}
		// Sprite 1 = Con bayas azules | Sprite 0 = Arbusto limpio/seco
		final int spriteIndex = this.tieneFruto() ? 1 : 0;
		return hoja.getSprite(spriteIndex);
	}

	@Override
	public int getAncho() {
		return this.ANCHO;
	}

	@Override
	public int getAlto() {
		return this.ALTO;
	}

	// =========================================================================
	// === CONTRATO INTERACTUABLE ([E] A 28 PX)
	// =========================================================================

	@Override
	public String getTextoPrompt() {
		return "Recolectar";
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.estaEliminado() && this.tieneFruto();
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (!this.tieneFruto()) {
			return;
		}

		final double horaActual = (Globales.GESTOR_ASTRONOMICO != null)
				? Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego()
				: 0.0;
		this.horaUltimaCosecha = horaActual;

		// Entrega entre 2 y 4 bayas silvestres
		final int cantidadBayas = 2 + (int) (Math.random() * 3);
		final BayaSilvestre bayas = new BayaSilvestre(cantidadBayas);

		boolean entregado = false;
		if (Globales.GESTOR_INVENTARIO != null) {
			entregado = Globales.GESTOR_INVENTARIO.getInventarioJugador().agregarObjeto(bayas);
		}

		// Si el inventario no pudo alojar las bayas, se arrojan al suelo
		if (!entregado && (this.mundo != null)) {
			final int dropX = this.getCentroX() - 4;
			final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);
			this.mundo.meterEntidad(new BayaSilvestre(dropX, dropY, cantidadBayas));
			Globales.GESTOR_TEXTOS.agregarTexto("¡Inventario lleno!", this.getCentroX(), this.getPosicionYInt(),
					principal.igu.textos.TipoTextoFlotante.ORO_EXP);
		} else {
			Globales.GESTOR_TEXTOS.agregarTexto("+" + cantidadBayas + " Bayas", this.getCentroX(),
					this.getPosicionYInt(), principal.igu.textos.TipoTextoFlotante.CURACION);
		}

		// Efectos y notificación de estructura
		GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, this.getCentroX(), this.getCentroY(),
				jugador.getPosicionX(), jugador.getPosicionY());
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 6);

		if (this.mundo != null) {
			this.mundo.notificarModificacionEstructura();
		}
	}

	// =========================================================================
	// === TALA Y DESTRUCCIÓN FÍSICA
	// =========================================================================

	@Override
	protected void soltarBotin() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getPosicionYInt() + (this.getAlto() / 2);

		// Suelta 1-2 unidades de madera/ramas al talarlo
		final int madera = 1 + (int) (Math.random() * 2);
		this.mundo.meterEntidad(RecursoMaterial.crearMadera(dropX, dropY, madera));

		// Si se destruye mientras tenía bayas, caen al suelo
		if (this.tieneFruto()) {
			final int cantBayas = 2 + (int) (Math.random() * 3);
			this.mundo.meterEntidad(new BayaSilvestre(dropX, dropY, cantBayas));
		}

		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 10);
	}

	@Override
	protected void emitirParticulasImpacto() {
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 4);
	}

	@Override
	public Objeto copiar() {
		return new ArbustoCosechable(this.getPosicionXInt(), this.getPosicionYInt(), this.horaUltimaCosecha);
	}

	// =========================================================================
	// === PERSISTENCIA DIFERENCIAL (HOOKS EN DELTAMUNDO)
	// =========================================================================

	@SuppressWarnings("unchecked")
	@Override
	protected void exportarDatosEspecificos(final JSONObject json) {
		json.put("horaUltimaCosecha", Double.valueOf(this.horaUltimaCosecha));
	}

	@Override
	protected void importarDatosEspecificos(final JSONObject json) {
		if (json.get("horaUltimaCosecha") != null) {
			this.horaUltimaCosecha = ((Number) json.get("horaUltimaCosecha")).doubleValue();
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("horaUltimaCosecha", Double.valueOf(this.horaUltimaCosecha));
		return json;
	}

	public static ArbustoCosechable crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new ArbustoCosechable(0, 0);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final double horaCosecha = (json.get("horaUltimaCosecha") != null)
				? ((Number) json.get("horaUltimaCosecha")).doubleValue()
				: -999.0;

		return new ArbustoCosechable(x, y, horaCosecha);
	}
}