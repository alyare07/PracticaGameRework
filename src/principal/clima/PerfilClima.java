package principal.clima;

/**
 * Catálogo de perfiles climáticos por bioma que define las probabilidades y
 * transiciones meteorológicas (Cadenas de Markov).
 * 
 * @version 3.0
 */
public enum PerfilClima {

	TEMPLADO_BOSQUE("Bosque Templado", 18.0, 0.50), DESIERTO_CALIDO("Desierto", 34.0, 0.15),
	MONTANA_NEVADA("Montaña Helada", -4.0, 0.80), PANTANO_HUMEDO("Pantano Húmedo", 22.0, 0.90),
	VOLCANICO("Tierras Volcánicas", 40.0, 0.20), BOSQUE_MISTICO("Bosque Místico", 20.0, 0.70);

	private final String nombreVisible;
	private final double temperaturaBase;
	private final double humedadBase;

	PerfilClima(final String nombreVisible, final double temperaturaBase, final double humedadBase) {
		this.nombreVisible = nombreVisible;
		this.temperaturaBase = temperaturaBase;
		this.humedadBase = humedadBase;
	}

	public TipoClima calcularSiguienteClima(final TipoClima actual) {
		final double azar = Math.random();

		switch (this) {
		case DESIERTO_CALIDO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.25) ? TipoClima.VENTOSO
						: ((azar < 0.40) ? TipoClima.TORMENTA_ARENA
								: ((azar < 0.45) ? TipoClima.ECLIPSE_SOLAR : TipoClima.DESPEJADO));
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.60) ? TipoClima.TORMENTA_ARENA : TipoClima.DESPEJADO;
			}
			return TipoClima.DESPEJADO;

		case MONTANA_NEVADA:
			if (actual == TipoClima.DESPEJADO) {
				if (azar < 0.55) {
					return TipoClima.NIEVE;
				}
				if (azar < 0.80) {
					return TipoClima.VENTOSO;
				}
				return TipoClima.AURORA_BOREAL; // Transición a Aurora Boreal
			}
			if (actual == TipoClima.NIEVE) {
				return (azar < 0.35) ? TipoClima.VENTISCA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.VENTISCA) {
				return TipoClima.NIEVE;
			}
			if (actual == TipoClima.AURORA_BOREAL) {
				return TipoClima.DESPEJADO;
			}
			return TipoClima.NIEVE;

		case PANTANO_HUMEDO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.45) ? TipoClima.NIEBLA_CERRADA : TipoClima.LLUVIA_LEVE;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				return (azar < 0.40) ? TipoClima.LLUVIA_ACIDA : TipoClima.NIEBLA_CERRADA;
			}
			if (actual == TipoClima.NIEBLA_CERRADA) {
				return (azar < 0.50) ? TipoClima.LLUVIA_ACIDA : TipoClima.DESPEJADO;
			}
			return TipoClima.NIEBLA_CERRADA;

		case VOLCANICO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.70) ? TipoClima.CENIZA_VOLCANICA
						: ((azar < 0.85) ? TipoClima.VENTOSO : TipoClima.ECLIPSE_SOLAR);
			}
			return (azar < 0.75) ? TipoClima.CENIZA_VOLCANICA : TipoClima.VENTOSO;

		case BOSQUE_MISTICO:
			if (actual == TipoClima.DESPEJADO) {
				if (azar < 0.30) {
					return TipoClima.ESPORAS_MAGICAS;
				}
				if (azar < 0.55) {
					return TipoClima.PETALOS_CEREZO;
				}
				if (azar < 0.80) {
					return TipoClima.AURORA_BOREAL;
				}
				return TipoClima.LLUVIA_ESTRELLAS; // Lluvia de estrellas cósmica
			}
			if (actual == TipoClima.AURORA_BOREAL) {
				return (azar < 0.50) ? TipoClima.LLUVIA_ESTRELLAS : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_ESTRELLAS) {
				return TipoClima.ESPORAS_MAGICAS;
			}
			return TipoClima.DESPEJADO;

		case TEMPLADO_BOSQUE:
		default:
			if (actual == TipoClima.DESPEJADO) {
				if (azar < 0.30) {
					return TipoClima.VENTOSO;
				}
				if (azar < 0.55) {
					return TipoClima.LLUVIA_LEVE;
				}
				if (azar < 0.75) {
					return TipoClima.PETALOS_CEREZO;
				}
				if (azar < 0.90) {
					return TipoClima.NIEBLA_CERRADA;
				}
				return TipoClima.LLUVIA_ESTRELLAS;
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.55) ? TipoClima.LLUVIA_LEVE : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				return (azar < 0.40) ? TipoClima.LLUVIA_TORMENTA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_TORMENTA) {
				return TipoClima.LLUVIA_LEVE;
			}
			if (actual == TipoClima.LLUVIA_ESTRELLAS) {
				return TipoClima.DESPEJADO;
			}
			return TipoClima.DESPEJADO;
		}
	}

	public String getNombreVisible() {
		return this.nombreVisible;
	}

	public double getTemperaturaBase() {
		return this.temperaturaBase;
	}

	public double getHumedadBase() {
		return this.humedadBase;
	}
}