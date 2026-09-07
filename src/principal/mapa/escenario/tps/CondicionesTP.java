package principal.mapa.escenario.tps;

import principal.entes.criaturas.Jugador;
import principal.inventario.Inventario;
import principal.utilidades.Globales;
import principal.utilidades.progreso.FlagProgreso;

/**
 * Fábrica de condiciones comunes para puertas y teletransportadores.
 */
public final class CondicionesTP {

	private CondicionesTP() {
	}

	// =========================================================================
	// 1. CONDICIONES DE ÍTEMS Y LLAVES
	// =========================================================================

	/**
	 * Requiere que el jugador tenga cierta cantidad de un ítem en su inventario.
	 * 
	 * @param codItem        Código del ítem (ej. "Llave de la Cripta").
	 * @param cantidad       Cantidad mínima necesaria.
	 * @param consumirAlUsar true si la llave se gasta al cruzar; false si es
	 *                       permanente.
	 */
	public static CondicionTP requiereItem(final String codItem, final int cantidad, final boolean consumirAlUsar) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();
				return (inv != null) && (inv.contarMunicionTotal(codItem) >= cantidad);
			}

			@Override
			public String getMensajeRechazo() {
				return "¡Requiere: " + codItem + " (x" + cantidad + ")!";
			}

			@Override
			public void alCruzar(final Jugador jugador) {
				if (consumirAlUsar) {
					final Inventario inv = Globales.GESTOR_INVENTARIO.getInventarioJugador();
					if (inv != null) {
						inv.extraerMunicion(codItem, cantidad);
					}
				}
			}
		};
	}

	// =========================================================================
	// 2. CONDICIONES DE ATRIBUTOS RPG
	// =========================================================================

	public static CondicionTP requiereFuerzaMinima(final int fuerzaMinima) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				return (jugador != null) && (jugador.getFuerzaTotal() >= fuerzaMinima);
			}

			@Override
			public String getMensajeRechazo() {
				return "¡Fuerza insuficiente! (Requiere " + fuerzaMinima + " FUE)";
			}
		};
	}

	public static CondicionTP requiereAgilidadMinima(final int agilidadMinima) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				return (jugador != null) && (jugador.getAgilidadTotal() >= agilidadMinima);
			}

			@Override
			public String getMensajeRechazo() {
				return "¡Demasiado pesado! (Requiere " + agilidadMinima + " AGI)";
			}
		};
	}

	public static CondicionTP requiereInteligenciaMinima(final int inteligenciaMinima) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				return (jugador != null) && (jugador.getInteligenciaTotal() >= inteligenciaMinima);
			}

			@Override
			public String getMensajeRechazo() {
				return "¡Sello Arcano! (Requiere " + inteligenciaMinima + " INT)";
			}
		};
	}

	// =========================================================================
	// 3. CONDICIONES DE HISTORIA / JEFES / FLAGS
	// =========================================================================

	/**
	 * Requiere que un evento o jefe haya sido superado en la partida.
	 * 
	 * @param claveFlag          Nombre de la bandera (ej. "JEFE_CUEVA_DERROTADO").
	 * @param mensajeSiBloqueado Texto a mostrar si aún no se derrotó.
	 */
	public static CondicionTP requiereFlag(final FlagProgreso flag, final String mensajeSiBloqueado) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				return (Globales.GESTOR_PROGRESO != null) && Globales.GESTOR_PROGRESO.isActivo(flag);
			}

			@Override
			public String getMensajeRechazo() {
				return mensajeSiBloqueado != null ? mensajeSiBloqueado : "¡Zona sellada!";
			}
		};
	}

	/**
	 * Sobrecarga que permite pasar el nombre del flag en String (útil para JSON o
	 * consola).
	 */
	public static CondicionTP requiereFlag(final String claveFlag, final String mensajeSiBloqueado) {
		try {
			final FlagProgreso flag = FlagProgreso.valueOf(claveFlag.toUpperCase().trim());
			return requiereFlag(flag, mensajeSiBloqueado);
		} catch (final Exception e) {
			// Si el String no coincide con ningún flag del enum, deniega el paso por
			// seguridad
			return new CondicionTP() {
				@Override
				public boolean seCumple(final Jugador jugador) {
					return false;
				}

				@Override
				public String getMensajeRechazo() {
					return "¡Flag desconocido: " + claveFlag + "!";
				}
			};
		}
	}

	// =========================================================================
	// 4. COMBINACIÓN DE CONDICIONES (AND / OR)
	// =========================================================================

	/**
	 * Combina dos condiciones (debe cumplir AMBAS).
	 */
	public static CondicionTP todas(final CondicionTP c1, final CondicionTP c2) {
		return new CondicionTP() {
			@Override
			public boolean seCumple(final Jugador jugador) {
				return c1.seCumple(jugador) && c2.seCumple(jugador);
			}

			@Override
			public String getMensajeRechazo() {
				if ((Globales.JUGADOR != null) && !c1.seCumple(Globales.JUGADOR)) {
					return c1.getMensajeRechazo();
				}
				return c2.getMensajeRechazo();
			}

			@Override
			public void alCruzar(final Jugador jugador) {
				c1.alCruzar(jugador);
				c2.alCruzar(jugador);
			}
		};
	}
}