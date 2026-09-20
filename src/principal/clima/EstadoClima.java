package principal.clima;

import org.json.simple.JSONObject;

/**
 * Contenedor de estado meteorológico y termodinámico individual por Mundo.
 * Mantiene la memoria climática cuando el mundo está inactivo (Zero-GC / O(1)).
 * 
 * @version 1.0 (Vanilla Java 8 - Dedicated World Climate State)
 */
public class EstadoClima {

	private TipoClima climaActual;
	private TipoClima climaPronosticado;

	private double duracionEstadoClimaSegundos = 360.0;
	private double tiempoRestanteEstadoClima = 360.0;
	private double duracionTransicionClima = 5.0;

	private double temperaturaCelsius = 20.0;
	private double humedadRelativa = 0.50;
	private double presionBarometricaHPa = 1013.25;

	private double anguloVientoRadianes = Math.toRadians(45.0);
	private double fuerzaViento = 1.0;
	private double vectorVientoX = 0.0;
	private double vectorVientoY = 0.0;
	private double timestampUltimaVisitaHoras = 0.0;

	public EstadoClima(final PerfilClima perfilInicial, final TipoClima climaInicial) {
		final PerfilClima perfil = (perfilInicial != null) ? perfilInicial : PerfilClima.TEMPLADO_BOSQUE;
		this.climaActual = (climaInicial != null) ? climaInicial : TipoClima.DESPEJADO;
		this.climaPronosticado = perfil.calcularSiguienteClima(this.climaActual);

		this.temperaturaCelsius = perfil.getTemperaturaBase();
		this.humedadRelativa = perfil.getHumedadBase();
		this.setViento(this.climaActual.getAnguloVientoGrados(), this.climaActual.getFuerzaViento());
	}

	public void setViento(final double gradosDireccion, final double fuerza) {
		this.anguloVientoRadianes = Math.toRadians(gradosDireccion);
		this.fuerzaViento = Math.max(0.0, fuerza);
		this.vectorVientoX = Math.cos(this.anguloVientoRadianes) * this.fuerzaViento;
		this.vectorVientoY = Math.sin(this.anguloVientoRadianes) * this.fuerzaViento;
	}

	// =========================================================================
	// GETTERS Y SETTERS ESCALARES (ZERO-GC)
	// =========================================================================
	public TipoClima getClimaActual() {
		return this.climaActual;
	}

	public void setClimaActual(final TipoClima climaActual) {
		if (climaActual != null) {
			this.climaActual = climaActual;
		}
	}

	public TipoClima getClimaPronosticado() {
		return this.climaPronosticado;
	}

	public void setClimaPronosticado(final TipoClima climaPronosticado) {
		if (climaPronosticado != null) {
			this.climaPronosticado = climaPronosticado;
		}
	}

	public double getDuracionEstadoClimaSegundos() {
		return this.duracionEstadoClimaSegundos;
	}

	public void setDuracionEstadoClimaSegundos(final double duracion) {
		this.duracionEstadoClimaSegundos = Math.max(1.0, duracion);
	}

	public double getTiempoRestanteEstadoClima() {
		return this.tiempoRestanteEstadoClima;
	}

	public void setTiempoRestanteEstadoClima(final double tiempo) {
		this.tiempoRestanteEstadoClima = Math.max(0.0, tiempo);
	}

	public double getDuracionTransicionClima() {
		return this.duracionTransicionClima;
	}

	public void setDuracionTransicionClima(final double duracion) {
		this.duracionTransicionClima = Math.max(0.5, duracion);
	}

	public double getTemperaturaCelsius() {
		return this.temperaturaCelsius;
	}

	public void setTemperaturaCelsius(final double temp) {
		this.temperaturaCelsius = temp;
	}

	public double getHumedadRelativa() {
		return this.humedadRelativa;
	}

	public void setHumedadRelativa(final double hum) {
		this.humedadRelativa = Math.max(0.0, Math.min(1.0, hum));
	}

	public double getPresionBarometricaHPa() {
		return this.presionBarometricaHPa;
	}

	public void setPresionBarometricaHPa(final double pres) {
		this.presionBarometricaHPa = pres;
	}

	public double getAnguloVientoRadianes() {
		return this.anguloVientoRadianes;
	}

	public double getFuerzaViento() {
		return this.fuerzaViento;
	}

	public double getVectorVientoX() {
		return this.vectorVientoX;
	}

	public double getVectorVientoY() {
		return this.vectorVientoY;
	}

	public double getTimestampUltimaVisitaHoras() {
		return this.timestampUltimaVisitaHoras;
	}

	public void setTimestampUltimaVisitaHoras(final double ts) {
		this.timestampUltimaVisitaHoras = ts;
	}

	// =========================================================================
	// PERSISTENCIA JSON (org.json.simple)
	// =========================================================================
	// =========================================================================
	// PERSISTENCIA JSON (org.json.simple)
	// =========================================================================
	@SuppressWarnings("unchecked")
	public JSONObject exportarJSON() {
		final JSONObject json = new JSONObject();
		json.put("climaActual", this.climaActual.name());
		json.put("climaPronosticado", this.climaPronosticado.name());
		json.put("duracionEstado", Double.valueOf(this.duracionEstadoClimaSegundos));
		json.put("tiempoRestante", Double.valueOf(this.tiempoRestanteEstadoClima));
		json.put("temperatura", Double.valueOf(this.temperaturaCelsius));
		json.put("humedad", Double.valueOf(this.humedadRelativa));
		json.put("presion", Double.valueOf(this.presionBarometricaHPa));
		json.put("fuerzaViento", Double.valueOf(this.fuerzaViento));
		json.put("anguloViento", Double.valueOf(Math.toDegrees(this.anguloVientoRadianes)));
		json.put("timestampUltimaVisita", Double.valueOf(this.timestampUltimaVisitaHoras));
		return json;
	}

	public void importarJSON(final JSONObject json) {
		if (json == null) {
			return;
		}
		if (json.get("climaActual") != null) {
			try {
				this.climaActual = TipoClima.valueOf(json.get("climaActual").toString());
			} catch (final Exception ignored) {
			}
		}
		if (json.get("climaPronosticado") != null) {
			try {
				this.climaPronosticado = TipoClima.valueOf(json.get("climaPronosticado").toString());
			} catch (final Exception ignored) {
			}
		}
		if (json.get("duracionEstado") != null) {
			this.duracionEstadoClimaSegundos = ((Number) json.get("duracionEstado")).doubleValue();
		}
		if (json.get("tiempoRestante") != null) {
			this.tiempoRestanteEstadoClima = ((Number) json.get("tiempoRestante")).doubleValue();
		}
		if (json.get("temperatura") != null) {
			this.temperaturaCelsius = ((Number) json.get("temperatura")).doubleValue();
		}
		if (json.get("humedad") != null) {
			this.humedadRelativa = ((Number) json.get("humedad")).doubleValue();
		}
		if (json.get("presion") != null) {
			this.presionBarometricaHPa = ((Number) json.get("presion")).doubleValue();
		}
		if ((json.get("fuerzaViento") != null) && (json.get("anguloViento") != null)) {
			final double fv = ((Number) json.get("fuerzaViento")).doubleValue();
			final double av = ((Number) json.get("anguloViento")).doubleValue();
			this.setViento(av, fv);
		}
		if (json.get("timestampUltimaVisita") != null) {
			this.timestampUltimaVisitaHoras = ((Number) json.get("timestampUltimaVisita")).doubleValue();
		}
	}
}