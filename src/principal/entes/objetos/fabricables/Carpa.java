package principal.entes.objetos.fabricables;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.configuracion.Dificultad;
import principal.entes.Ente;
import principal.entes.criaturas.jugador.GestorMetabolismoJugador;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.desplegables.KitCarpa;
import principal.interaccion.Interactuable;
import principal.inventario.Inventario;
import principal.maquinaestado.estados.GestorTransicionSueno;
import principal.recursos.TexturaObjetos;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Tienda de campaña de supervivencia que hereda de ObjetoFabricable. Ofrece
 * refugio bajo techo, combate asediable y cálculo de siesta Zero-GC.
 * 
 * @version 3.0 (Vanilla Java 8 - ObjetoFabricable Architecture)
 */
public class Carpa extends ObjetoFabricable implements Interactuable {

	private static final long serialVersionUID = 1L;

	public static final int LADO_CARPA = 32;
	public static final double VIDA_CARPA = 40.0;

	private final Rectangle areaInteriorNoSolida = new Rectangle();
	private boolean jugadorAdentro = false;

	// Caché Zero-GC para el prompt
	private int lastDecimasHoras = -1;
	private String cachedTextoSiesta = "Tomar Siesta";

	public Carpa(final int x, final int y, final Ente propietario) {
		super(x, y, VIDA_CARPA, propietario);
	}

	public Carpa(final int x, final int y) {
		this(x, y, Globales.JUGADOR);
	}

	public Carpa(final int x, final int y, final double vida, final Ente propietario) {
		super(x, y, VIDA_CARPA, propietario);
		this.setVida(vida);
	}

	@Override
	public void actualizar() {
		super.actualizar();

		// Si el jugador salió caminando con WASD, sincroniza el estado de la carpa
		if (this.jugadorAdentro && (Globales.JUGADOR != null) && !Globales.JUGADOR.isRefugiadoEnCarpa()) {
			this.jugadorAdentro = false;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage tex = this.getTextura();
		if (tex != null) {
			Render2D.dibujarImagenRefCamara(g, tex, this.getPosicionXInt(), this.getPosicionYInt());
		} else {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), LADO_CARPA,
					LADO_CARPA, new Color(55, 90, 60));
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt() + 8, this.getPosicionYInt() + 12, 16,
					18, new Color(25, 30, 25));
		}

		// Barra de durabilidad táctica
		this.pintarIndicadorVida(g);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getAreaInterior(), Color.CYAN);
		}
	}

	public Rectangle getAreaInterior() {
		this.areaInteriorNoSolida.setBounds(this.getPosicionXInt() + 6, this.getPosicionYInt() + 8, LADO_CARPA - 12,
				LADO_CARPA - 10);
		return this.areaInteriorNoSolida;
	}

	@Override
	protected void alDestruir(final Ente causante) {
		// Válvula de daño por aplastamiento si el jugador estaba refugiado adentro
		if (this.jugadorAdentro && (Globales.JUGADOR != null) && !Globales.JUGADOR.estaEliminado()) {
			Globales.JUGADOR.salirDeCarpa();

			final double danioColapso;
			if (Globales.dificultad == Dificultad.FACIL) {
				danioColapso = 5.0;
			} else if (Globales.dificultad == Dificultad.NORMAL) {
				danioColapso = 15.0;
			} else {
				danioColapso = 25.0;
			}

			Globales.JUGADOR.recibirDanioDirecto(danioColapso);
			if (Globales.CAMARA != null) {
				Globales.CAMARA.aplicarTemblor(500, 3.5);
			}
			Globales.GESTOR_TEXTOS.agregarTexto("¡Refugio Destruido!", Globales.JUGADOR.getCentroX(),
					Globales.JUGADOR.getPosicionYInt() - 10, principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
		}
	}

	// =========================================================================
	// INTERACCIÓN Y DESCANSO INTELIGENTE
	// =========================================================================

	public double calcularHorasDescanso() {
		final GestorMetabolismoJugador meta = Globales.GESTOR_METABOLISMO;
		if (meta == null) {
			return 8.0;
		}
		final double suenio = meta.getSuenio();
		if (suenio >= 80.0) {
			return 0.0;
		}
		if (suenio < 50.0) {
			return 8.0;
		}
		final double falta = 100.0 - suenio;
		final double horas = falta / 12.5;
		return Math.max(1.0, Math.round(horas * 10.0) / 10.0);
	}

	@Override
	public String getTextoPrompt() {
		final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);
		final Jugador j = Globales.JUGADOR;

		if ((j == null) || !this.jugadorAdentro) {
			return shift ? "Desarmar Carpa" : "Entrar al Refugio";
		}

		if (shift) {
			return "Salir del Refugio";
		}

		final double horas = this.calcularHorasDescanso();
		if (horas <= 0.0) {
			return "Salir (Sin sueño)";
		}
		if (horas >= 8.0) {
			return "Dormir (8.0 h)";
		}

		final int decimasActuales = (int) Math.round(horas * 10.0);
		if (decimasActuales != this.lastDecimasHoras) {
			this.lastDecimasHoras = decimasActuales;
			this.cachedTextoSiesta = "Tomar Siesta (" + (decimasActuales / 10) + "." + (decimasActuales % 10) + " h)";
		}
		return this.cachedTextoSiesta;
	}

	@Override
	public void interactuar(final Jugador jugador) {
		final boolean shift = Globales.TECLADO.presionaTeclaEnLista(KeyEvent.VK_SHIFT);

		if (!this.jugadorAdentro) {
			if (shift) {
				this.desarmar(jugador);
			} else {
				this.jugadorAdentro = true;
				jugador.setPosicion(this.getCentroX() - (jugador.getAncho() / 2.0), this.getPosicionYInt() + 10);
				jugador.setRefugiadoEnCarpa(true);
				jugador.detenerMovimiento();
				GestorSonido.reproducir(IDSonido.SELECT);
				Globales.GESTOR_TEXTOS.agregarTexto("Bajo Refugio", this.getCentroX(), this.getPosicionYInt() - 8,
						principal.igu.textos.TipoTextoFlotante.ORO_EXP);
			}
			return;
		}

		if (shift) {
			this.jugadorAdentro = false;
			jugador.salirDeCarpa();
			return;
		}

		final double horas = this.calcularHorasDescanso();
		if (horas <= 0.0) {
			this.jugadorAdentro = false;
			jugador.salirDeCarpa();
			return;
		}

		GestorTransicionSueno.getInstancia().iniciar(horas, false);
	}

	private void desarmar(final Jugador jugador) {
		final Inventario inv = (Globales.GESTOR_INVENTARIO != null) ? Globales.GESTOR_INVENTARIO.getInventarioJugador()
				: null;
		final KitCarpa kit = new KitCarpa(1);

		if ((inv != null) && inv.agregarObjeto(kit)) {
			GestorSonido.reproducir(IDSonido.GOLPE_1);
			Globales.GESTOR_TEXTOS.agregarTexto("+1 Kit de Carpa", this.getCentroX(), this.getPosicionYInt() - 8,
					principal.igu.textos.TipoTextoFlotante.ORO_EXP);
		} else if (this.mundo != null) {
			kit.setPosicion(this.getPosicionXInt(), this.getPosicionYInt());
			this.mundo.meterEntidad(kit);
		}

		if ((Globales.GESTOR_DELTAS != null) && (this.mundo != null)) {
			Globales.GESTOR_DELTAS.registrarDestruccion(this.mundo, this.getPosicionXInt(), this.getPosicionYInt());
		}

		if (this.mundo != null) {
			this.mundo.notificarModificacionEstructura();
		}
		this.eliminar();
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.eliminado;
	}

	@Override
	public int getCentroX() {
		return this.getPosicionXInt() + (LADO_CARPA / 2);
	}

	@Override
	public int getAlto() {
		return LADO_CARPA;
	}

	@Override
	public int getAncho() {
		return LADO_CARPA;
	}

	@Override
	public BufferedImage getTextura() {
		return Globales.GESTOR_TEXTURAS.get(TexturaObjetos.CARPA_X32);
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		return new Carpa(this.getPosicionXInt(), this.getPosicionYInt(), this.vida, this.propietario);
	}

	public boolean isJugadorAdentro() {
		return this.jugadorAdentro;
	}

	// =========================================================================
	// PERSISTENCIA JSON
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("vida", Double.valueOf(this.vida));
		json.put("tipo", "Carpa");
		return json;
	}

	public static Carpa crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Carpa(0, 0);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final double vida = (json.get("vida") != null) ? ((Number) json.get("vida")).doubleValue() : VIDA_CARPA;
		return new Carpa(x, y, vida, Globales.JUGADOR);
	}
}