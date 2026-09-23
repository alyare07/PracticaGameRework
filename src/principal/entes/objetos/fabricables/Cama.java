package principal.entes.objetos.fabricables;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.jugador.GestorMetabolismoJugador;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.Objeto;
import principal.interaccion.Interactuable;
import principal.maquinaestado.estados.GestorTransicionSueno;
import principal.recursos.TexturaObjetos;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Mueble interactuable de descanso en interiores que hereda de
 * ObjetoFabricable.
 * 
 * @version 2.0 (Vanilla Java 8 - ObjetoFabricable Architecture)
 */
public class Cama extends ObjetoFabricable implements Interactuable {

	private static final long serialVersionUID = 1L;

	public static final int ANCHO_CAMA = 12;
	public static final int ALTO_CAMA = 20;
	public static final double VIDA_CAMA = 30.0;

	private int lastDecimasHoras = -1;
	private String cachedTextoSiesta = "Tomar Siesta";

	public Cama(final int x, final int y, final Ente propietario) {
		super(x, y, VIDA_CAMA, propietario);
	}

	public Cama(final int x, final int y) {
		this(x, y, Globales.JUGADOR);
	}

	@Override
	public void pintar(final Graphics2D g) {
		final BufferedImage tex = this.getTextura();
		if (tex != null) {
			Render2D.dibujarImagenRefCamara(g, tex, this.getPosicionXInt(), this.getPosicionYInt());
		} else {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), ANCHO_CAMA,
					ALTO_CAMA, new Color(110, 45, 30));
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt() + 2, this.getPosicionYInt() + 2,
					ANCHO_CAMA - 4, 8, Color.WHITE);
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt() + 2, this.getPosicionYInt() + 10,
					ANCHO_CAMA - 4, ALTO_CAMA - 12, new Color(180, 50, 40));
		}

		// Barra de durabilidad táctica
		this.pintarIndicadorVida(g);

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.isEstadoJuego()) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	@Override
	protected void alDestruir(final Ente causante) {
		Globales.GESTOR_PARTICULAS.emitirPolvo(this.getCentroX(), this.getPosicionYInt() + 16, 6);
	}

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
		final double horas = this.calcularHorasDescanso();
		if (horas <= 0.0) {
			return "Sin cansancio";
		}
		if (horas >= 8.0) {
			return "Dormir (8.0 h)";
		}

		final int decimas = (int) Math.round(horas * 10.0);
		if (decimas != this.lastDecimasHoras) {
			this.lastDecimasHoras = decimas;
			this.cachedTextoSiesta = "Tomar Siesta (" + (decimas / 10) + "." + (decimas % 10) + " h)";
		}
		return this.cachedTextoSiesta;
	}

	@Override
	public void interactuar(final Jugador jugador) {
		final double horas = this.calcularHorasDescanso();
		if (horas <= 0.0) {
			GestorSonido.reproducir(IDSonido.SIN_MUNICION);
			if (this.mundo != null) {
				Globales.GESTOR_TEXTOS.agregarTexto("No tienes suficiente sueño para dormir", this.getCentroX(),
						this.getPosicionYInt() - 8, principal.igu.textos.TipoTextoFlotante.DANIO_NORMAL);
			}
			return;
		}

		// Registra esta cama como punto de respawn
		if (Globales.GESTOR_RESURRECCION != null) {
			Globales.GESTOR_RESURRECCION.registrarLecho(this);
		}

		GestorTransicionSueno.getInstancia().iniciar(horas, true);
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.eliminado;
	}

	@Override
	public int getCentroX() {
		return this.getPosicionXInt() + (ANCHO_CAMA / 2);
	}

	@Override
	public int getAlto() {
		return ALTO_CAMA;
	}

	@Override
	public int getAncho() {
		return ANCHO_CAMA;
	}

	@Override
	public BufferedImage getTextura() {
		return Globales.GESTOR_TEXTURAS.get(TexturaObjetos.CAMA_X32);
	}

	@Override
	public boolean esSolido() {
		return true;
	}

	@Override
	public Objeto copiar() {
		return new Cama(this.getPosicionXInt(), this.getPosicionYInt(), this.propietario);
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("vida", Double.valueOf(this.vida));
		json.put("tipo", "Cama");
		return json;
	}

	public static Cama crearDesdeJson(final JSONObject json) {
		if (json == null) {
			return new Cama(0, 0);
		}
		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final double vida = (json.get("vida") != null) ? ((Number) json.get("vida")).doubleValue() : VIDA_CAMA;
		final Cama c = new Cama(x, y, Globales.JUGADOR);
		c.setVida(vida);
		return c;
	}
}