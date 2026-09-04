package principal.maquinaestado.estados.editor.metadatos;

import java.awt.Color;
import java.io.Serializable;

import org.json.simple.JSONObject;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.utilidades.audio.musica.IDMusica;

/**
 * Contenedor de configuración atmosférica y ambiental del mapa (Música BGM,
 * Bioma, Clima inicial e Iluminación interior/exterior).
 * 
 * @version 1.0 (Vanilla Java 8 - Data-Driven Map Config)
 */
public class MetadatosEscenario implements Serializable {

	private static final long serialVersionUID = 1L;

	private IDMusica musicaFondo;
	private PerfilClima perfilBioma;
	private TipoClima climaInicial;
	private boolean esInteriorCueva;
	private Color colorLuzInterior;

	public MetadatosEscenario() {
		this.musicaFondo = IDMusica.FONDO_FOREST;
		this.perfilBioma = PerfilClima.TEMPLADO_BOSQUE;
		this.climaInicial = TipoClima.DESPEJADO;
		this.esInteriorCueva = false;
		this.colorLuzInterior = new Color(0, 0, 0, 255);
	}

	public MetadatosEscenario(final IDMusica musicaFondo, final PerfilClima perfilBioma, final TipoClima climaInicial,
			final boolean esInteriorCueva, final Color colorLuzInterior) {
		this.musicaFondo = (musicaFondo != null) ? musicaFondo : IDMusica.FONDO_FOREST;
		this.perfilBioma = (perfilBioma != null) ? perfilBioma : PerfilClima.TEMPLADO_BOSQUE;
		this.climaInicial = (climaInicial != null) ? climaInicial : TipoClima.DESPEJADO;
		this.esInteriorCueva = esInteriorCueva;
		this.colorLuzInterior = (colorLuzInterior != null) ? colorLuzInterior : new Color(0, 0, 0, 255);
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("musicaFondo", this.musicaFondo.name());
		json.put("perfilBioma", this.perfilBioma.name());
		json.put("climaInicial", this.climaInicial.name());
		json.put("esInterior", Boolean.valueOf(this.esInteriorCueva));
		json.put("luzInteriorR", Integer.valueOf(this.colorLuzInterior.getRed()));
		json.put("luzInteriorG", Integer.valueOf(this.colorLuzInterior.getGreen()));
		json.put("luzInteriorB", Integer.valueOf(this.colorLuzInterior.getBlue()));
		json.put("luzInteriorA", Integer.valueOf(this.colorLuzInterior.getAlpha()));
		return json;
	}

	public static MetadatosEscenario crearDesdeJSON(final JSONObject json) {
		if (json == null) {
			return new MetadatosEscenario();
		}

		IDMusica musica = IDMusica.FONDO_FOREST;
		if (json.get("musicaFondo") != null) {
			try {
				musica = IDMusica.valueOf(json.get("musicaFondo").toString());
			} catch (final Exception ignored) {
			}
		}

		PerfilClima bioma = PerfilClima.TEMPLADO_BOSQUE;
		if (json.get("perfilBioma") != null) {
			try {
				bioma = PerfilClima.valueOf(json.get("perfilBioma").toString());
			} catch (final Exception ignored) {
			}
		}

		TipoClima clima = TipoClima.DESPEJADO;
		if (json.get("climaInicial") != null) {
			try {
				clima = TipoClima.valueOf(json.get("climaInicial").toString());
			} catch (final Exception ignored) {
			}
		}

		final boolean esInterior = (json.get("esInterior") != null) && Boolean.parseBoolean(json.get("esInterior").toString());

		final int r = (json.get("luzInteriorR") != null) ? ((Number) json.get("luzInteriorR")).intValue() : 0;
		final int g = (json.get("luzInteriorG") != null) ? ((Number) json.get("luzInteriorG")).intValue() : 0;
		final int b = (json.get("luzInteriorB") != null) ? ((Number) json.get("luzInteriorB")).intValue() : 0;
		final int a = (json.get("luzInteriorA") != null) ? ((Number) json.get("luzInteriorA")).intValue() : 255;

		return new MetadatosEscenario(musica, bioma, clima, esInterior, new Color(r, g, b, a));
	}

	// =========================================================================
	// GETTERS & SETTERS
	// =========================================================================

	public IDMusica getMusicaFondo() {
		return this.musicaFondo;
	}

	public void setMusicaFondo(final IDMusica musicaFondo) {
		if (musicaFondo != null) {
			this.musicaFondo = musicaFondo;
		}
	}

	public PerfilClima getPerfilBioma() {
		return this.perfilBioma;
	}

	public void setPerfilBioma(final PerfilClima perfilBioma) {
		if (perfilBioma != null) {
			this.perfilBioma = perfilBioma;
		}
	}

	public TipoClima getClimaInicial() {
		return this.climaInicial;
	}

	public void setClimaInicial(final TipoClima climaInicial) {
		if (climaInicial != null) {
			this.climaInicial = climaInicial;
		}
	}

	public boolean isEsInteriorCueva() {
		return this.esInteriorCueva;
	}

	public void setEsInteriorCueva(final boolean esInteriorCueva) {
		this.esInteriorCueva = esInteriorCueva;
	}

	public Color getColorLuzInterior() {
		return this.colorLuzInterior;
	}

	public void setColorLuzInterior(final Color colorLuzInterior) {
		if (colorLuzInterior != null) {
			this.colorLuzInterior = colorLuzInterior;
		}
	}
}