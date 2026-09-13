package principal.clima;

import principal.iluminacion.Estacion;
import principal.utilidades.Globales;

/**
 * Catálogo de perfiles climáticos por bioma que define las probabilidades y
 * transiciones meteorológicas estacionales mediante Cadenas de Markov (Zero-GC
 * / O(1)).
 * 
 * @version 4.0 (Vanilla Java 8 - Seasonal Markov Matrix Integration)
 */
public enum PerfilClima {

	TEMPLADO_BOSQUE("Bosque Templado", 18.0, 0.50), BOSQUE_BOREAL("Bosque Boreal", 12.0, 0.65),
	DESIERTO_CALIDO("Desierto", 34.0, 0.15), MONTANA_NEVADA("Montaña Helada", -4.0, 0.80),
	PANTANO_HUMEDO("Pantano Húmedo", 22.0, 0.90), VOLCANICO("Tierras Volcánicas", 40.0, 0.20),
	BOSQUE_MISTICO("Bosque Místico", 20.0, 0.70);

	private final String nombreVisible;
	private final double temperaturaBase;
	private final double humedadBase;

	PerfilClima(final String nombreVisible, final double temperaturaBase, final double humedadBase) {
		this.nombreVisible = nombreVisible;
		this.temperaturaBase = temperaturaBase;
		this.humedadBase = humedadBase;
	}

	/**
	 * Calcula el siguiente estado climático consultando automáticamente la estación
	 * activa en el ciclo solar del mundo.
	 * 
	 * @param actual Tipo de clima actualmente en curso.
	 * @return Siguiente TipoClima calculado.
	 */
	public TipoClima calcularSiguienteClima(final TipoClima actual) {
		Estacion estacion = Estacion.PRIMAVERA;
		if ((Globales.GESTOR_LUZ != null) && (Globales.GESTOR_LUZ.getCiclo() != null)) {
			estacion = Globales.GESTOR_LUZ.getCiclo().getEstacionActual();
		}
		return this.calcularSiguienteClima(actual, estacion);
	}

	/**
	 * Calcula el siguiente clima mediante la matriz estocástica de Markov sesgada
	 * por la estación meteorológica en curso.
	 * 
	 * @param actual   Tipo de clima actual.
	 * @param estacion Estación canónica del año (Primavera, Verano, Otoño,
	 *                 Invierno).
	 * @return Siguiente TipoClima calculado.
	 */
	public TipoClima calcularSiguienteClima(final TipoClima actual, final Estacion estacion) {
		final double azar = Math.random();
		final Estacion est = (estacion != null) ? estacion : Estacion.PRIMAVERA;

		switch (this) {

		// =====================================================================
		// 1. BOSQUE TEMPLADO (Bioma Principal con fuerte contraste de 4 estaciones)
		// =====================================================================
		case TEMPLADO_BOSQUE:
			if (actual == TipoClima.DESPEJADO) {
				switch (est) {
				case PRIMAVERA:
					if (azar < 0.25) {
						return TipoClima.VENTOSO;
					}
					if (azar < 0.55) {
						return TipoClima.PETALOS_CEREZO;
					}
					if (azar < 0.85) {
						return TipoClima.LLUVIA_LEVE;
					}
					return TipoClima.DESPEJADO;

				case VERANO:
					if (azar < 0.15) {
						return TipoClima.VENTOSO;
					}
					if (azar < 0.35) {
						return TipoClima.LLUVIA_TORMENTA;
					}
					if (azar < 0.40) {
						return TipoClima.ECLIPSE_SOLAR;
					}
					return TipoClima.DESPEJADO; // Veranos predominantemente soleados

				case OTONO:
					if (azar < 0.40) {
						return TipoClima.VENTOSO; // Vientos con hojas otoñales
					}
					if (azar < 0.70) {
						return TipoClima.LLUVIA_LEVE;
					}
					if (azar < 0.90) {
						return TipoClima.NIEBLA_CERRADA;
					}
					return TipoClima.DESPEJADO;

				case INVIERNO:
				default:
					if (azar < 0.50) {
						return TipoClima.NIEVE;
					}
					if (azar < 0.75) {
						return TipoClima.VENTISCA;
					}
					if (azar < 0.90) {
						return TipoClima.NIEBLA_CERRADA;
					}
					return TipoClima.DESPEJADO;
				}
			}
			if (actual == TipoClima.VENTOSO) {
				if (est == Estacion.INVIERNO) {
					return (azar < 0.60) ? TipoClima.NIEVE : TipoClima.DESPEJADO;
				}
				return (azar < 0.55) ? TipoClima.LLUVIA_LEVE : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				if (est == Estacion.VERANO) {
					return (azar < 0.60) ? TipoClima.LLUVIA_TORMENTA : TipoClima.DESPEJADO;
				}
				if (est == Estacion.INVIERNO) {
					return TipoClima.NIEVE;
				}
				return (azar < 0.35) ? TipoClima.LLUVIA_TORMENTA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.NIEVE) {
				return (azar < 0.40) ? TipoClima.VENTISCA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.VENTISCA) {
				return TipoClima.NIEVE;
			}
			return TipoClima.DESPEJADO;

		// =====================================================================
		// 2. BOSQUE BOREAL (Frío riguroso, nieblas y auroras invernales)
		// =====================================================================
		case BOSQUE_BOREAL:
			if (actual == TipoClima.DESPEJADO) {
				if (est == Estacion.INVIERNO) {
					if (azar < 0.50) {
						return TipoClima.NIEVE;
					}
					if (azar < 0.75) {
						return TipoClima.VENTISCA;
					}
					return TipoClima.AURORA_BOREAL; // Auroras boreales en invierno boreal
				}
				if (azar < 0.35) {
					return TipoClima.VENTOSO;
				}
				if (azar < 0.60) {
					return TipoClima.LLUVIA_LEVE;
				}
				if (azar < 0.85) {
					return TipoClima.NIEBLA_CERRADA;
				}
				return TipoClima.NIEVE;
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.50) ? TipoClima.NIEVE : TipoClima.LLUVIA_LEVE;
			}
			if (actual == TipoClima.LLUVIA_LEVE) {
				return (azar < 0.40) ? TipoClima.NIEBLA_CERRADA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.NIEVE) {
				return (azar < 0.50) ? TipoClima.VENTOSO : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.AURORA_BOREAL) {
				return TipoClima.DESPEJADO;
			}
			return TipoClima.DESPEJADO;

		// =====================================================================
		// 3. DESIERTO CÁLIDO (Tormentas de arena y radiación abrasadora)
		// =====================================================================
		case DESIERTO_CALIDO:
			if (actual == TipoClima.DESPEJADO) {
				final double pTormenta = (est == Estacion.VERANO) ? 0.55 : 0.35;
				if (azar < 0.20) {
					return TipoClima.VENTOSO;
				}
				if (azar < pTormenta) {
					return TipoClima.TORMENTA_ARENA;
				}
				if (azar < (pTormenta + 0.05)) {
					return TipoClima.ECLIPSE_SOLAR;
				}
				return TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.VENTOSO) {
				return (azar < 0.65) ? TipoClima.TORMENTA_ARENA : TipoClima.DESPEJADO;
			}
			return TipoClima.DESPEJADO;

		// =====================================================================
		// 4. MONTAÑA HELADA (Criósfera continua con ventiscas implacables)
		// =====================================================================
		case MONTANA_NEVADA:
			if (actual == TipoClima.DESPEJADO) {
				if (azar < 0.55) {
					return TipoClima.NIEVE;
				}
				if (azar < 0.80) {
					return TipoClima.VENTOSO;
				}
				return TipoClima.AURORA_BOREAL;
			}
			if (actual == TipoClima.NIEVE) {
				final double pVentisca = (est == Estacion.INVIERNO) ? 0.60 : 0.35;
				return (azar < pVentisca) ? TipoClima.VENTISCA : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.VENTISCA) {
				return TipoClima.NIEVE;
			}
			if (actual == TipoClima.AURORA_BOREAL) {
				return TipoClima.DESPEJADO;
			}
			return TipoClima.NIEVE;

		// =====================================================================
		// 5. PANTANO HÚMEDO (Nieblas densas y lluvias ácidas)
		// =====================================================================
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

		// =====================================================================
		// 6. VOLCÁNICO (Cenizas incandescentes permanentes)
		// =====================================================================
		case VOLCANICO:
			if (actual == TipoClima.DESPEJADO) {
				return (azar < 0.70) ? TipoClima.CENIZA_VOLCANICA
						: ((azar < 0.85) ? TipoClima.VENTOSO : TipoClima.ECLIPSE_SOLAR);
			}
			return (azar < 0.75) ? TipoClima.CENIZA_VOLCANICA : TipoClima.VENTOSO;

		// =====================================================================
		// 7. BOSQUE MÍSTICO (Magia astronómica y botánica estacional)
		// =====================================================================
		case BOSQUE_MISTICO:
		default:
			if (actual == TipoClima.DESPEJADO) {
				switch (est) {
				case PRIMAVERA:
					if (azar < 0.50) {
						return TipoClima.PETALOS_CEREZO;
					}
					return TipoClima.ESPORAS_MAGICAS;

				case VERANO:
					if (azar < 0.45) {
						return TipoClima.LLUVIA_ESTRELLAS;
					}
					return TipoClima.AURORA_BOREAL;

				case INVIERNO:
					if (azar < 0.60) {
						return TipoClima.AURORA_BOREAL;
					}
					return TipoClima.NIEVE;

				case OTONO:
				default:
					if (azar < 0.40) {
						return TipoClima.ESPORAS_MAGICAS;
					}
					if (azar < 0.70) {
						return TipoClima.NIEBLA_CERRADA;
					}
					return TipoClima.AURORA_BOREAL;
				}
			}
			if (actual == TipoClima.AURORA_BOREAL) {
				return (azar < 0.50) ? TipoClima.LLUVIA_ESTRELLAS : TipoClima.DESPEJADO;
			}
			if (actual == TipoClima.LLUVIA_ESTRELLAS) {
				return TipoClima.ESPORAS_MAGICAS;
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