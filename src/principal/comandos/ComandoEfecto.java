package principal.comandos;

import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.efectos.TipoEfectoEstado;
import principal.mapa.Mundo;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;

/**
 * Comando de desarrollo para aplicar, consultar o remover efectos de estado
 * (Buffs, Debuffs de combate y ambientales) al jugador o a la criatura que esté
 * bajo el cursor del ratón.
 * 
 * @version 1.0 (Vanilla Java 8 - Zero-GC Target Picking)
 */
public class ComandoEfecto extends Comando {

	// Variable auxiliar para capturar el target en el visitor sin micro-asignaciones
	private Criatura objetivoDetectado;

	public ComandoEfecto() {
		super("efecto",
				"efecto <jugador | criatura> <tipo_efecto | clear [tipo|all]> [duracion_seg] [potencia] [stacks]",
				"Aplica o remueve efectos de estado al jugador o a la criatura apuntada con el raton.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (args.length < 2) {
			this.enviarError(emisor, "Sintaxis insuficiente.\nUso: " + this.getSintaxis()
					+ "\nEjemplos:\n -> efecto jugador veneno 10 5 3\n -> efecto criatura celeridad 15 1\n -> efecto jugador clear all");
			return;
		}

		final String targetParam = args[0].toLowerCase().trim();
		Criatura objetivo = null;

		// 1. RESOLUCIÓN DEL TARGET
		if (targetParam.equals("jugador") || targetParam.equals("player") || targetParam.equals("p")
				|| targetParam.equals("yo")) {
			if (Globales.JUGADOR == null || Globales.JUGADOR.estaEliminado()) {
				this.enviarError(emisor, "El jugador no esta inicializado en memoria.");
				return;
			}
			objetivo = Globales.JUGADOR;

		} else if (targetParam.equals("criatura") || targetParam.equals("mouse") || targetParam.equals("raton")
				|| targetParam.equals("npc") || targetParam.equals("c")) {

			objetivo = this.obtenerCriaturaBajoRaton();

			if (objetivo == null) {
				this.enviarError(emisor,
						"No se detecto ninguna criatura valida bajo el cursor del raton en coordenadas de mundo.");
				return;
			}
		} else {
			this.enviarError(emisor, "Target invalido: '" + args[0] + "'. Usa 'jugador' o 'criatura'.");
			return;
		}

		final String accionOEfecto = args[1].toLowerCase().trim();

		// 2. LIMPIEZA DE EFECTOS (CLEAR / REMOVE)
		if (accionOEfecto.equals("clear") || accionOEfecto.equals("limpiar") || accionOEfecto.equals("remove")
				|| accionOEfecto.equals("quitar")) {
			if (args.length >= 3 && !args[2].equalsIgnoreCase("all") && !args[2].equalsIgnoreCase("todos")) {
				final TipoEfectoEstado tipo = this.parsearTipoEfecto(args[2]);
				if (tipo != null) {
					objetivo.removerEfecto(tipo);
					this.enviarInfo(emisor, "Efecto " + tipo.getNombre() + " removido de " + objetivo.getNombre() + ".");
				} else {
					this.enviarError(emisor, "Efecto desconocido para remover: '" + args[2] + "'.");
				}
			} else {
				for (final TipoEfectoEstado t : TipoEfectoEstado.values()) {
					objetivo.removerEfecto(t);
				}
				this.enviarInfo(emisor, "Todos los efectos de estado han sido removidos de " + objetivo.getNombre() + ".");
			}
			return;
		}

		// 3. PARSEO DEL EFECTO
		final TipoEfectoEstado tipoEfecto = this.parsearTipoEfecto(accionOEfecto);
		if (tipoEfecto == null) {
			this.enviarError(emisor, "Tipo de efecto desconocido: '" + args[1]
					+ "'.\nOpciones: REGENERACION, CELERIDAD, FUERZA, RESISTENCIA, VENENO, SANGRADO, QUEMADURA, ATURDIMIENTO, HIPOTERMIA, HIPERTERMIA");
			return;
		}

		// 4. PARSEO DE PARÁMETROS OPCIONALES
		final double duracion = (args.length >= 3) ? this.parsearDouble(args[2], 10.0) : 10.0;
		final double potencia = (args.length >= 4) ? this.parsearDouble(args[3], 1.0) : 1.0;
		final int stacks = (args.length >= 5) ? this.parsearEntero(args[4], 1) : 1;

		// 5. APLICACIÓN DEL EFECTO
		objetivo.aplicarEfecto(tipoEfecto, duracion, potencia, stacks);

		this.enviarInfo(emisor, "Efecto aplicado con exito a [" + objetivo.getNombre() + "]:\n -> Tipo    : "
				+ tipoEfecto.getNombre() + "\n -> Duracion: " + duracion + "s\n -> Potencia: " + potencia
				+ (tipoEfecto.isAcumulable() ? "\n -> Cargas  : " + stacks : ""));
	}

	/**
	 * Realiza el picking espacial en O(1) de la criatura apuntada con el ratón.
	 */
	private Criatura obtenerCriaturaBajoRaton() {
		final Mundo mundo = (Globales.JUGADOR != null) ? Globales.JUGADOR.getMundo() : null;
		if (mundo == null || Globales.RATON == null) {
			return null;
		}

		final Rectangle areaMouse = Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara();
		this.objetivoDetectado = null;

		mundo.paraCadaCriaturaEn(areaMouse, false, new AccionEntidad<Criatura>() {
			@Override
			public void ejecutar(final Criatura entidad) {
				if (ComandoEfecto.this.objetivoDetectado == null && !(entidad instanceof Jugador)
						&& !entidad.estaEliminado()) {
					ComandoEfecto.this.objetivoDetectado = entidad;
				}
			}
		});

		final Criatura resultado = this.objetivoDetectado;
		this.objetivoDetectado = null;
		return resultado;
	}

	/**
	 * Mapeo tolerante a mayúsculas, minúsculas y alias comunes.
	 */
	private TipoEfectoEstado parsearTipoEfecto(final String texto) {
		if (texto == null) {
			return null;
		}
		final String clean = texto.toUpperCase().trim().replace(" ", "_");

		try {
			return TipoEfectoEstado.valueOf(clean);
		} catch (final IllegalArgumentException ignored) {
		}

		switch (clean) {
		case "REGEN":
		case "CURACION":
		case "VIDA":
			return TipoEfectoEstado.REGENERACION;
		case "SPEED":
		case "VELOCIDAD":
		case "RAPIDEZ":
			return TipoEfectoEstado.CELERIDAD;
		case "STRENGTH":
		case "DAMAGE":
		case "DANIO":
		case "DAÑO":
			return TipoEfectoEstado.FUERZA;
		case "DEFENSA":
		case "ARMADURA":
		case "DEF":
			return TipoEfectoEstado.RESISTENCIA;
		case "POISON":
		case "TOXICO":
			return TipoEfectoEstado.VENENO;
		case "BLEED":
		case "CORTE":
			return TipoEfectoEstado.SANGRADO;
		case "BURN":
		case "FUEGO":
		case "FIRE":
			return TipoEfectoEstado.QUEMADURA;
		case "STUN":
		case "MAREO":
		case "PARALISIS":
			return TipoEfectoEstado.ATURDIMIENTO;
		case "FRIO":
		case "COLD":
		case "FREEZE":
			return TipoEfectoEstado.HIPOTERMIA;
		case "CALOR":
		case "HEAT":
		case "HOT":
			return TipoEfectoEstado.HIPERTERMIA;
		default:
			return null;
		}
	}
}