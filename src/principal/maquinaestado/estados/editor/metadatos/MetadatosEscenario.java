package principal.maquinaestado.estados.editor.metadatos;

import java.awt.Color;
import java.io.Serializable;

import org.json.simple.JSONObject;

import principal.clima.PerfilClima;
import principal.clima.TipoClima;
import principal.utilidades.audio.musica.IDMusica;

/**
 * Contenedor maestro de configuración atmosférica y ambiental del mapa.
 * Clasifica los espacios en EXTERIOR, INTERIOR (Casas/Tabernas) y CUEVA
 * (Blackout).
 * 
 * @version 3.0 (Vanilla Java 8 - Dedicated 3-Tier Environment Architecture)
 */
public class MetadatosEscenario implements Serializable {

	private static final long serialVersionUID = 3L;

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

	private IDMusica musicaFondo;
	private PerfilClima perfilBioma;
	private TipoClima climaInicial;
	private TipoAmbiente tipoAmbiente;
	private TipoIluminacionInterior iluminacionInterior;
	private Color colorLuzPersonalizado;

	public MetadatosEscenario() {
		this(IDMusica.FONDO_RELAX, PerfilClima.TEMPLADO_BOSQUE, TipoClima.DESPEJADO, TipoAmbiente.EXTERIOR,
				TipoIluminacionInterior.HOGARENA, new Color(255, 215, 140, 40));
	}

	public MetadatosEscenario(final IDMusica musicaFondo, final PerfilClima perfilBioma, final TipoClima climaInicial,
			final TipoAmbiente tipoAmbiente, final TipoIluminacionInterior iluminacionInterior,
			final Color colorLuzPersonalizado) {
		this.musicaFondo = (musicaFondo != null) ? musicaFondo : IDMusica.FONDO_FOREST;
		this.perfilBioma = (perfilBioma != null) ? perfilBioma : PerfilClima.TEMPLADO_BOSQUE;
		this.climaInicial = (climaInicial != null) ? climaInicial : TipoClima.DESPEJADO;
		this.tipoAmbiente = (tipoAmbiente != null) ? tipoAmbiente : TipoAmbiente.EXTERIOR;
		this.iluminacionInterior = (iluminacionInterior != null) ? iluminacionInterior
				: TipoIluminacionInterior.HOGARENA;
		this.colorLuzPersonalizado = (colorLuzPersonalizado != null) ? colorLuzPersonalizado
				: new Color(255, 215, 140, 40);
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

	/**
	 * Retorna true para cualquier espacio cerrado bajo techo (Interiores o Cuevas)
	 * para evitar que las partículas de clima exterior (lluvia, nieve, tormentas)
	 * se rendericen.
	 */
	public boolean esEspacioInterior() {
		return this.tipoAmbiente != TipoAmbiente.EXTERIOR;
	}

	/**
	 * Resuelve la capa de luz ambiental fija que baña uniformemente todo el mapa.
	 */
	public Color resolverColorLuzEfectivo() {
		switch (this.tipoAmbiente) {
		case CUEVA:
			return new Color(0, 0, 0, 255); // Oscuridad absoluta (Blackout completo)

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
			return null; // El ciclo solar de 24 horas controla la iluminación dinámicamente
		}
	}

	// =========================================================================
	// PERSISTENCIA JSON (SERIALIZACIÓN Y DESERIALIZACIÓN COMPATIBLE)
	// =========================================================================

	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
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
			// Retrocompatibilidad con nombres anteriores
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

		return new MetadatosEscenario(musica, bioma, clima, ambiente, ilumInterior, new Color(r, g, b, a));
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