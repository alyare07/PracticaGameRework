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
 * @version 2.0 (Vanilla Java 8 - Dedicated World Atmosphere Presets)
 */
public class MetadatosEscenario implements Serializable {

	private static final long serialVersionUID = 2L;

	public enum TipoAmbiente {
		EXTERIOR("Exterior (Ciclo Solar 24h y Clima)"), CUEVA_OSCURA("Cueva / Mazmorra (Oscuridad Total - Blackout)"),
		INTERIOR_TENUE("Interior Abandonado / Oscuro (Requiere Linterna)"),
		INTERIOR_HOGARENO("Interior Hogareño (Luz Cálida y Acogedora)"),
		INTERIOR_ILUMINADO("Interior Plenamente Iluminado");

		private final String descripcion;

		TipoAmbiente(final String descripcion) {
			this.descripcion = descripcion;
		}

		public String getDescripcion() {
			return this.descripcion;
		}
	}

	private IDMusica musicaFondo;
	private PerfilClima perfilBioma;
	private TipoClima climaInicial;
	private TipoAmbiente tipoAmbiente;
	private Color colorLuzPersonalizado;

	public MetadatosEscenario() {
		this(IDMusica.FONDO_RELAX, PerfilClima.TEMPLADO_BOSQUE, TipoClima.DESPEJADO, TipoAmbiente.EXTERIOR,
				new Color(0, 0, 0, 255));
	}

	public MetadatosEscenario(final IDMusica musicaFondo, final PerfilClima perfilBioma, final TipoClima climaInicial,
			final TipoAmbiente tipoAmbiente, final Color colorLuzPersonalizado) {
		this.musicaFondo = (musicaFondo != null) ? musicaFondo : IDMusica.FONDO_FOREST;
		this.perfilBioma = (perfilBioma != null) ? perfilBioma : PerfilClima.TEMPLADO_BOSQUE;
		this.climaInicial = (climaInicial != null) ? climaInicial : TipoClima.DESPEJADO;
		this.tipoAmbiente = (tipoAmbiente != null) ? tipoAmbiente : TipoAmbiente.EXTERIOR;
		this.colorLuzPersonalizado = (colorLuzPersonalizado != null) ? colorLuzPersonalizado : new Color(0, 0, 0, 255);
	}

	public boolean esEspacioInterior() {
		return this.tipoAmbiente != TipoAmbiente.EXTERIOR;
	}

	public Color resolverColorLuzEfectivo() {
		switch (this.tipoAmbiente) {
		case CUEVA_OSCURA:
			return new Color(0, 0, 0, 255); // Oscuridad absoluta (Blackout)
		case INTERIOR_TENUE:
			return new Color(10, 15, 30, 235); // Penumbra azulada
		case INTERIOR_HOGARENO:
			return new Color(255, 220, 150, 45); // Tinte cálido y relajante
		case INTERIOR_ILUMINADO:
			return new Color(0, 0, 0, 0); // Sin sombra ambiental
		case EXTERIOR:
		default:
			return null; // Deja que el ciclo solar de 24h controle la luz
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("musicaFondo", this.musicaFondo.name());
		json.put("perfilBioma", this.perfilBioma.name());
		json.put("climaInicial", this.climaInicial.name());
		json.put("tipoAmbiente", this.tipoAmbiente.name());
		json.put("luzR", Integer.valueOf(this.colorLuzPersonalizado.getRed()));
		json.put("luzG", Integer.valueOf(this.colorLuzPersonalizado.getGreen()));
		json.put("luzB", Integer.valueOf(this.colorLuzPersonalizado.getBlue()));
		json.put("luzA", Integer.valueOf(this.colorLuzPersonalizado.getAlpha()));
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

		TipoAmbiente ambiente = TipoAmbiente.EXTERIOR;
		if (json.get("tipoAmbiente") != null) {
			try {
				ambiente = TipoAmbiente.valueOf(json.get("tipoAmbiente").toString());
			} catch (final Exception ignored) {
			}
		} else if ((json.get("esInterior") != null) && Boolean.parseBoolean(json.get("esInterior").toString())) {
			ambiente = TipoAmbiente.CUEVA_OSCURA;
		}

		final int r = (json.get("luzR") != null) ? ((Number) json.get("luzR")).intValue() : 0;
		final int g = (json.get("luzG") != null) ? ((Number) json.get("luzG")).intValue() : 0;
		final int b = (json.get("luzB") != null) ? ((Number) json.get("luzB")).intValue() : 0;
		final int a = (json.get("luzA") != null) ? ((Number) json.get("luzA")).intValue() : 255;

		return new MetadatosEscenario(musica, bioma, clima, ambiente, new Color(r, g, b, a));
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

	public TipoAmbiente getTipoAmbiente() {
		return this.tipoAmbiente;
	}

	public void setTipoAmbiente(final TipoAmbiente tipoAmbiente) {
		if (tipoAmbiente != null) {
			this.tipoAmbiente = tipoAmbiente;
		}
	}

	public Color getColorLuzPersonalizado() {
		return this.colorLuzPersonalizado;
	}

	public void setColorLuzPersonalizado(final Color colorLuzPersonalizado) {
		if (colorLuzPersonalizado != null) {
			this.colorLuzPersonalizado = colorLuzPersonalizado;
		}
	}

	@Deprecated
	public boolean isEsInteriorCueva() {
		return this.esEspacioInterior();
	}

	@Deprecated
	public void setEsInteriorCueva(final boolean esInterior) {
		this.tipoAmbiente = esInterior ? TipoAmbiente.CUEVA_OSCURA : TipoAmbiente.EXTERIOR;
	}

	@Deprecated
	public Color getColorLuzInterior() {
		return this.colorLuzPersonalizado;
	}
}