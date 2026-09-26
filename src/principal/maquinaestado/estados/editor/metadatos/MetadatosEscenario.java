package principal.maquinaestado.estados.editor.metadatos;

import java.awt.Color;
import java.io.Serializable;

import org.json.simple.JSONObject;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.utilidades.audio.musica.IDMusica;

/**
 * Contenedor maestro de configuración atmosférica, ambiental y narrativa del
 * submundo. Clasifica los espacios en EXTERIOR, INTERIOR y CUEVA, e incorpora
 * el Nombre Visible narrativo para HUD, banners cinemáticos y pantallas de
 * carga.
 * 
 * @version 3.1 (Vanilla Java 8 - Narrative Display Name Support)
 */
public class MetadatosEscenario implements Serializable {

	private static final long serialVersionUID = 4L;

	public enum TipoAmbiente {
		EXTERIOR("Exterior (Ciclo Solar 24h y Clima)"), INTERIOR("Interior (Hogares, Casas, Tabernas)"),
		CUEVA("Cueva / Mazmorra (Blackout y Oscuridad Total)");

		private final String descripcion;

		TipoAmbiente(final String descripcion) {
			this.descripcion = descripcion;
		}

		public String getDescripcion() {
			return this.descripcion;
		}
	}

	private String nombreVisible;
	private IDMusica musicaFondo;
	private PerfilClima perfilBioma;
	private TipoClima climaInicial;
	private TipoAmbiente tipoAmbiente;
	private TipoIluminacionInterior iluminacionInterior;
	private Color colorLuzPersonalizado;

	public MetadatosEscenario() {
		this(IDMusica.FONDO_RELAX, PerfilClima.TEMPLADO_BOSQUE, TipoClima.DESPEJADO, TipoAmbiente.EXTERIOR,
				TipoIluminacionInterior.HOGARENA, new Color(255, 215, 140, 40), "Submundo");
	}

	public MetadatosEscenario(final IDMusica musicaFondo, final PerfilClima perfilBioma, final TipoClima climaInicial,
			final TipoAmbiente tipoAmbiente, final TipoIluminacionInterior iluminacionInterior,
			final Color colorLuzPersonalizado) {
		this(musicaFondo, perfilBioma, climaInicial, tipoAmbiente, iluminacionInterior, colorLuzPersonalizado,
				"Submundo");
	}

	public MetadatosEscenario(final IDMusica musicaFondo, final PerfilClima perfilBioma, final TipoClima climaInicial,
			final TipoAmbiente tipoAmbiente, final TipoIluminacionInterior iluminacionInterior,
			final Color colorLuzPersonalizado, final String nombreVisible) {
		this.musicaFondo = (musicaFondo != null) ? musicaFondo : IDMusica.FONDO_FOREST;
		this.perfilBioma = (perfilBioma != null) ? perfilBioma : PerfilClima.TEMPLADO_BOSQUE;
		this.climaInicial = (climaInicial != null) ? climaInicial : TipoClima.DESPEJADO;
		this.tipoAmbiente = (tipoAmbiente != null) ? tipoAmbiente : TipoAmbiente.EXTERIOR;
		this.iluminacionInterior = (iluminacionInterior != null) ? iluminacionInterior
				: TipoIluminacionInterior.HOGARENA;
		this.colorLuzPersonalizado = (colorLuzPersonalizado != null) ? colorLuzPersonalizado
				: new Color(255, 215, 140, 40);
		this.nombreVisible = ((nombreVisible != null) && !nombreVisible.trim().isEmpty()) ? nombreVisible.trim()
				: "Submundo";
	}

	public boolean esExterior() {
		return this.tipoAmbiente == TipoAmbiente.EXTERIOR;
	}

	public boolean esInterior() {
		return this.tipoAmbiente == TipoAmbiente.INTERIOR;
	}

	public boolean esCueva() {
		return this.tipoAmbiente == TipoAmbiente.CUEVA;
	}

	public boolean esEspacioInterior() {
		return this.tipoAmbiente != TipoAmbiente.EXTERIOR;
	}

	public Color resolverColorLuzEfectivo() {
		switch (this.tipoAmbiente) {
		case CUEVA:
			return new Color(0, 0, 0, 255);

		case INTERIOR:
			if ((this.iluminacionInterior == TipoIluminacionInterior.PERSONALIZADA)
					&& (this.colorLuzPersonalizado != null)) {
				return this.colorLuzPersonalizado;
			}
			return ((this.iluminacionInterior != null) && (this.iluminacionInterior.getColorAmbiente() != null))
					? this.iluminacionInterior.getColorAmbiente()
					: TipoIluminacionInterior.HOGARENA.getColorAmbiente();

		case EXTERIOR:
		default:
			return null;
		}
	}

	// =========================================================================
	// PERSISTENCIA JSON
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("nombreVisible", this.nombreVisible);
		json.put("musicaFondo", this.musicaFondo.name());
		json.put("perfilBioma", this.perfilBioma.name());
		json.put("climaInicial", this.climaInicial.name());
		json.put("tipoAmbiente", this.tipoAmbiente.name());
		json.put("iluminacionInterior", this.iluminacionInterior.name());
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

		final String nombreVisible = (json.get("nombreVisible") != null) ? json.get("nombreVisible").toString()
				: "Submundo";

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
		TipoIluminacionInterior ilumInterior = TipoIluminacionInterior.HOGARENA;

		if (json.get("tipoAmbiente") != null) {
			final String ambStr = json.get("tipoAmbiente").toString();
			if (ambStr.contains("CUEVA")) {
				ambiente = TipoAmbiente.CUEVA;
			} else if (ambStr.contains("INTERIOR")) {
				ambiente = TipoAmbiente.INTERIOR;
				if (ambStr.contains("TENUE")) {
					ilumInterior = TipoIluminacionInterior.TENUE;
				} else if (ambStr.contains("ILUMINADO")) {
					ilumInterior = TipoIluminacionInterior.CLARA;
				} else {
					ilumInterior = TipoIluminacionInterior.HOGARENA;
				}
			} else {
				try {
					ambiente = TipoAmbiente.valueOf(ambStr);
				} catch (final Exception ignored) {
				}
			}
		} else if ((json.get("esInterior") != null) && Boolean.parseBoolean(json.get("esInterior").toString())) {
			ambiente = TipoAmbiente.CUEVA;
		}

		if (json.get("iluminacionInterior") != null) {
			try {
				ilumInterior = TipoIluminacionInterior.valueOf(json.get("iluminacionInterior").toString());
			} catch (final Exception ignored) {
			}
		}

		final int r = (json.get("luzR") != null) ? ((Number) json.get("luzR")).intValue() : 255;
		final int g = (json.get("luzG") != null) ? ((Number) json.get("luzG")).intValue() : 215;
		final int b = (json.get("luzB") != null) ? ((Number) json.get("luzB")).intValue() : 140;
		final int a = (json.get("luzA") != null) ? ((Number) json.get("luzA")).intValue() : 40;

		return new MetadatosEscenario(musica, bioma, clima, ambiente, ilumInterior, new Color(r, g, b, a),
				nombreVisible);
	}

	// =========================================================================
	// GETTERS & SETTERS
	// =========================================================================

	public String getNombreVisible() {
		return (this.nombreVisible != null) ? this.nombreVisible : "";
	}

	public void setNombreVisible(final String nombreVisible) {
		this.nombreVisible = ((nombreVisible != null) && !nombreVisible.trim().isEmpty()) ? nombreVisible.trim()
				: "Submundo";
	}

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

	public TipoIluminacionInterior getIluminacionInterior() {
		return this.iluminacionInterior;
	}

	public void setIluminacionInterior(final TipoIluminacionInterior iluminacionInterior) {
		if (iluminacionInterior != null) {
			this.iluminacionInterior = iluminacionInterior;
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
}