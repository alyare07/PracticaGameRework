package principal.entes.criaturas.animales;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.criaturas.Criatura;
import principal.entes.facciones.GestorFacciones;
import principal.entes.objetos.items.comidas.CarnePolloCruda;
import principal.ia.arbol.FabricaArbolesIA;
import principal.igu.textos.TipoTextoFlotante;
import principal.recursos.ClaveHoja;
import principal.utilidades.Globales;
import principal.utilidades.HojaSprite;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Fauna menor pasiva de caza (Presa). Huye ante la proximidad de amenazas y
 * rinde alimento cárnico al ser abatida (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8 - Prey AI & Dynamic Footprint)
 */
public class Gallina extends Criatura {

	private static final long serialVersionUID = 1L;

	public static final String NOMBRE_ESPECIE = "Gallina Silvestre";
	private static final double VIDA_BASE = 12.0;
	private static final double VEL_PASTOREO = 0.50;
	private static final double VEL_HUIDA = 1.35;

	public Gallina(final double x, final double y) {
		super(x, y, 16, 16, VIDA_BASE, VIDA_BASE, VEL_PASTOREO);

		this.setFaccion(GestorFacciones.FACCION_FAUNA_PASIVA);
		this.configurarFootprint(8, 6, 0);

		// Asignamos el Árbol de Comportamiento de Presa que preparamos en el Paso 2
		this.setArbolComportamiento(FabricaArbolesIA.ARBOL_PRESA_GALLINA);
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 0;
		this.margenYInicialSprite = 0;
		this.margenXFinalSprite = 0;
		this.margenYFinalSprite = 0;
	}

	// =========================================================================
	// === MODULACIÓN FÍSICA Y LOCOMOCIÓN
	// =========================================================================

	@Override
	protected void establecerVelocidadStardar() {
		// Si está huyendo o corriendo, multiplica su velocidad de escape
		if (this.tieneEstado(Estado.CORRIENDO) || this.tieneEstado(Estado.HUYENDO)) {
			this.velocidad = VEL_HUIDA * this.getMultiplicadorVelocidadEfectos();
		} else {
			this.velocidad = this.velocidadEstandar * this.getMultiplicadorVelocidadEfectos();
		}
	}

	// =========================================================================
	// === RENDERIZADO ANIMADO ZERO-GC
	// =========================================================================

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);

		final boolean mirandoOeste = (this.direccion == Direccion.OESTE);
		final HojaSprite hoja = mirandoOeste ? Globales.GESTOR_TEXTURAS.getHojaVolteadaH(ClaveHoja.GALLINA)
				: Globales.GESTOR_TEXTURAS.getHoja(ClaveHoja.GALLINA);

		if (hoja == null) {
			return;
		}

		// Modulación de fotogramas según esfuerzo físico
		final int frame;
		if (this.estaEnMovimientoFisico()) {
			final int cadencia = (this.tieneEstado(Estado.CORRIENDO) || this.tieneEstado(Estado.HUYENDO)) ? 5 : 10;
			frame = (Globales.animacion / cadencia) % 4;
		} else {
			// Ciclo pausado de picoteo y respiración en reposo
			frame = (Globales.animacion / 18) % 4;
		}

		final BufferedImage sprite = this.estaEnFlashDanio() ? hoja.getSpriteFlash(frame) : hoja.getSprite(frame);

		Render2D.dibujarImagenRefCamara(g, sprite, this.getPosicionXInt(), this.getPosicionYInt());
	}

	// =========================================================================
	// === MUERTE Y SUELTA DE BOTÍN
	// =========================================================================

	@Override
	public void eliminar() {
		if (!this.eliminado) {
			this.soltarBotinMuerte();
		}
		super.eliminar();
	}

	private void soltarBotinMuerte() {
		if (this.mundo == null) {
			return;
		}

		final int dropX = this.getCentroX() - 4;
		final int dropY = this.getCentroY() - 4;

		// Entrega de 1 a 2 patas de pollo crudas
		final int cantidadCarne = 1 + (int) (Math.random() * 2);
		this.mundo.meterEntidad(new CarnePolloCruda(dropX, dropY, cantidadCarne));

		// Audio, sangre y partículas de plumas
		GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, dropX, dropY, Globales.JUGADOR.getPosicionX(),
				Globales.JUGADOR.getPosicionY());
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso(this.getCentroX(), this.getCentroY(), 10);
		Globales.GESTOR_TEXTOS.agregarTexto("+" + cantidadCarne + " Pollo Crudo", this.getCentroX(),
				this.getPosicionYInt() - 8, TipoTextoFlotante.ORO_EXP);

		// Registro persistente en DeltaMundo para que la muerte perdure
		if (Globales.GESTOR_DELTAS != null) {
			Globales.GESTOR_DELTAS.obtenerOCrearDelta(this.mundo.getNombreMundo(), 0)
					.registrarDestruccion(this.xInicial, this.yInicial);
		}
	}

	// =========================================================================
	// === IDENTIDAD Y SERIALIZACIÓN
	// =========================================================================

	@Override
	public String getNombre() {
		return NOMBRE_ESPECIE;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Gallina";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		this.exportarDatosCriaturaBase(json);
		return json;
	}

	public static Gallina crearDesdeJSON(final JSONObject json) {
		if (json == null) {
			return new Gallina(0, 0);
		}

		final double x = (json.get("x") != null) ? ((Number) json.get("x")).doubleValue() : 0.0;
		final double y = (json.get("y") != null) ? ((Number) json.get("y")).doubleValue() : 0.0;

		final Gallina gallina = new Gallina(x, y);
		gallina.importarDatosCriaturaBase(json);
		return gallina;
	}
}