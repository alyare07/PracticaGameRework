package principal.comandos;

import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Criatura.Estado;
import principal.entes.criaturas.Jugador;
import principal.iluminacion.CalculadorSigilo;
import principal.utilidades.Globales;

/**
 * Comando integral para la manipulación y diagnóstico del jugador principal
 * (Salud, Modo Dios, Estamina, Velocidad, Daño, Teletransporte y Estados).
 * 
 * @version 2.0
 */
public class ComandoJugador extends Comando {

	public ComandoJugador() {
		super("player",
				"player <god [on/off] | heal [cant] | damage <cant> | hp <val> | maxhp <val> | stamina <val> | speed <val> | dmg <val> | tp <x y> | dir <dir> | estado <add/remove/clear> | reset | stats | ayuda>",
				"Manipula la vida, modo dios, estamina, velocidad, combate y estados del jugador.");
	}

	@Override
	public void ejecutar(final String[] args) {
		this.ejecutar(args, null);
	}

	@Override
	public void ejecutar(final String[] args, final EmisorRespuesta emisor) {
		if (Globales.JUGADOR == null) {
			this.enviarError(emisor, "El jugador no esta inicializado en memoria.");
			return;
		}

		final Jugador p = Globales.JUGADOR;

		// 1. Tarjeta de Diagnóstico y Estado (sin argumentos o con 'stats')
		if ((args.length == 0) || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("info")) {
			this.mostrarEstadisticas(p, emisor);
			return;
		}

		final String sub = args[0].toLowerCase().trim();

		// 2. Menú de Ayuda
		if (sub.equals("ayuda") || sub.equals("help") || sub.equals("?")) {
			this.mostrarMenuAyuda(emisor);
			return;
		}

		// 3. Modo Dios / Inmortalidad (player god [on/off])
		if (sub.equals("god") || sub.equals("godmode") || sub.equals("inmortal") || sub.equals("invencible")) {
			if (args.length < 2) {
				final boolean nuevoEstado = p.conmutarModoDios();
				this.enviarInfo(emisor,
						"Modo Dios (Vida y Estamina infinitas): " + (nuevoEstado ? "ACTIVADO" : "DESACTIVADO"));
				return;
			}
			final boolean activar = this.parsearBooleano(args[1]);
			p.setModoDios(activar);
			this.enviarInfo(emisor, "Modo Dios (Vida y Estamina infinitas): " + (activar ? "ACTIVADO" : "DESACTIVADO"));
			return;
		}

		// 4. Curación y Sanación
		if (sub.equals("heal") || sub.equals("curar") || sub.equals("vida")) {
			if (args.length >= 2) {
				final double cant = this.parsearDouble(args[1], 10.0);
				p.curar(cant);
				this.enviarInfo(emisor,
						"Curados +" + (int) cant + " HP -> Vida: " + (int) p.getVida() + "/" + (int) p.getVidaMaxima());
			} else {
				p.sanar();
				this.enviarInfo(emisor, "Jugador sanado al 100% (" + (int) p.getVidaMaxima() + " HP).");
			}
			return;
		}

		// 5. Daño y Ataques de Prueba
		if (sub.equals("damage") || sub.equals("dmg") || sub.equals("hit") || sub.equals("danio")
				|| sub.equals("daño")) {
			if (args.length < 2) {
				this.enviarError(emisor, "Indica la cantidad de daño. Uso: 'player damage 25'");
				return;
			}
			final double danio = this.parsearDouble(args[1], 10.0);
			p.recibirAtaque(danio, null);
			this.enviarInfo(emisor, "Aplicados -" + (int) danio + " puntos de daño -> Vida restante: "
					+ (int) p.getVida() + "/" + (int) p.getVidaMaxima());
			return;
		}

		// 6. Ajuste Directo de Vida (HP) y Vida Máxima
		if (sub.equals("hp") || sub.equals("sethp")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Vida actual: " + (int) p.getVida() + "/" + (int) p.getVidaMaxima());
				return;
			}
			final double hp = this.parsearDouble(args[1], p.getVidaMaxima());
			p.establecerVida(hp);
			this.enviarInfo(emisor, "Vida fijada en: " + (int) p.getVida() + "/" + (int) p.getVidaMaxima());
			return;
		}

		if (sub.equals("maxhp") || sub.equals("vidamax")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Vida maxima actual: " + (int) p.getVidaMaxima());
				return;
			}
			final double maxHp = this.parsearDouble(args[1], 100.0);
			p.establecerVidaMaxima(maxHp);
			this.enviarInfo(emisor, "Vida maxima establecida en: " + (int) p.getVidaMaxima());
			return;
		}

		// 7. Eliminar / Revivir
		if (sub.equals("kill") || sub.equals("morir") || sub.equals("suicidio")) {
			p.establecerVida(0);
			this.enviarInfo(emisor, "El jugador ha sido eliminado.");
			return;
		}

		if (sub.equals("revive") || sub.equals("revivir") || sub.equals("resucitar")) {
			if (p.getMundo() != null) {
				p.restablecerYCambiarMundo(p.getMundo());
				this.enviarInfo(emisor, "Jugador resucitado con exito y reubicado en spawn.");
			} else {
				p.sanar();
				this.enviarInfo(emisor, "Jugador sanado al maximo.");
			}
			return;
		}

		// 8. Control de Estamina
		if (sub.equals("stamina") || sub.equals("st") || sub.equals("estamina")) {
			if (args.length < 2) {
				this.enviarInfo(emisor,
						"Estamina actual: " + (int) p.getEstamina() + "/" + (int) p.getLimiteEstamina());
				return;
			}
			final double st = this.parsearDouble(args[1], p.getLimiteEstamina());
			p.setEstamina(st);
			this.enviarInfo(emisor, "Estamina fijada en: " + (int) p.getEstamina() + "/" + (int) p.getLimiteEstamina());
			return;
		}

		if (sub.equals("maxstamina") || sub.equals("maxst")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Limite de estamina actual: " + (int) p.getLimiteEstamina());
				return;
			}
			final double maxSt = this.parsearDouble(args[1], 30.0);
			p.setMaxEstamina(maxSt);
			this.enviarInfo(emisor, "Estamina maxima fijada en: " + (int) p.getLimiteEstamina());
			return;
		}

		// 9. Velocidad Base (player speed <val>)
		if (sub.equals("speed") || sub.equals("vel") || sub.equals("velocidad")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Velocidad actual: " + p.getVelocidad() + " (Base: "
						+ String.format("%.2f", p.getVelocidad()) + ")");
				return;
			}
			final double vel = this.parsearDouble(args[1], 0.5);
			p.setVelocidadBase(vel);
			this.enviarInfo(emisor, "Velocidad base establecida en: " + vel);
			return;
		}

		// 10. Daño Base de Combate (player dmg <val>)
		if (sub.equals("damagebase") || sub.equals("dmgbase") || sub.equals("fuerza") || sub.equals("atk")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Daño base actual: " + (int) p.getDamage() + " pts");
				return;
			}
			final double atk = this.parsearDouble(args[1], 5.0);
			p.setDamage(atk);
			this.enviarInfo(emisor, "Puntos de daño base fijados en: " + (int) p.getDamage() + " pts");
			return;
		}

		// 11. Teletransporte (player tp <x> <y> / player spawn)
		if (sub.equals("tp") || sub.equals("teleport") || sub.equals("mover")) {
			if (args.length < 3) {
				this.enviarError(emisor, "Uso: 'player tp <x> <y>'\nEjemplo: 'player tp 500 350'");
				return;
			}
			final double x = this.parsearDouble(args[1], p.getPosicionX());
			final double y = this.parsearDouble(args[2], p.getPosicionY());
			p.setPosicion(x, y);
			this.enviarInfo(emisor, "Jugador teletransportado a: (" + (int) x + ", " + (int) y + ")");
			return;
		}

		if (sub.equals("spawn") || sub.equals("inicio") || sub.equals("home")) {
			if (p.getMundo() != null) {
				p.getMundo().moverJugadorPuntoComienzo();
				this.enviarInfo(emisor, "Jugador enviado al punto de spawn del mapa.");
			} else {
				this.enviarError(emisor, "El mundo actual no esta disponible para reubicar en spawn.");
			}
			return;
		}

		// 12. Dirección de Mirada (player dir <norte/sur/este/oeste>)
		if (sub.equals("dir") || sub.equals("direccion") || sub.equals("mirar")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Direccion actual: " + p.getDireccion());
				return;
			}
			final Direccion d = this.parsearDireccion(args[1]);
			if (d != null) {
				switch (d) {
				case NORTE:
					p.modificarPosicionY(-0.0001);
					break;
				case SUR:
					p.modificarPosicionY(0.0001);
					break;
				case OESTE:
					p.modificarPosicionX(-0.0001);
					break;
				case ESTE:
				default:
					p.modificarPosicionX(0.0001);
					break;
				}
				this.enviarInfo(emisor, "Direccion orientada a: " + d);
			} else {
				this.enviarError(emisor, "Direccion desconocida: '" + args[1] + "'. Opciones: NORTE, SUR, ESTE, OESTE");
			}
			return;
		}

		// 13. Máquina de Estados (player estado <add/remove/clear> [nombre])
		if (sub.equals("estado") || sub.equals("estados") || sub.equals("state")) {
			if (args.length < 2) {
				this.enviarInfo(emisor, "Estados activos: [ " + p.getStringEstados() + "]");
				return;
			}

			final String op = args[1].toLowerCase().trim();
			if (op.equals("clear") || op.equals("limpiar") || op.equals("reset")) {
				p.limpiarEstados();
				p.meterEstado(Estado.ESTANDAR);
				this.enviarInfo(emisor, "Estados limpiados. Estado restablecido a ESTANDAR.");
				return;
			}

			if (args.length >= 3) {
				final Estado e = this.parsearEstado(args[2]);
				if (e == null) {
					this.enviarError(emisor, "Estado no valido: '" + args[2]
							+ "'. Opciones: ESTANDAR, CAMINANDO, CORRIENDO, ATACANDO, ARROJANDO, PERSIGUIENDO");
					return;
				}

				if (op.equals("add") || op.equals("meter") || op.equals("+")) {
					p.meterEstado(e);
					this.enviarInfo(emisor,
							"Estado '" + e.name() + "' agregado. Estados: [ " + p.getStringEstados() + "]");
				} else if (op.equals("remove") || op.equals("quitar") || op.equals("-")) {
					p.removerEstado(e);
					this.enviarInfo(emisor,
							"Estado '" + e.name() + "' removido. Estados: [ " + p.getStringEstados() + "]");
				}
				return;
			}
		}

		// 14. Restaurar Valores Iniciales (player reset)
		if (sub.equals("reset") || sub.equals("default")) {
			p.setModoDios(false);
			p.sanar();
			p.setVelocidadBase(0.5);
			p.setDamage(5.0);
			p.setMaxEstamina(30.0);
			p.setEstamina(30.0);
			p.limpiarEstados();
			p.meterEstado(Estado.ESTANDAR);
			this.enviarInfo(emisor, "Jugador restablecido a los parametros iniciales por defecto.");
			return;
		}

		this.enviarError(emisor,
				"Subcomando desconocido: '" + args[0] + "'.\nEscribe 'player ayuda' para ver todas las opciones.");
	}

	private boolean parsearBooleano(final String str) {
		final String clean = str.toLowerCase().trim();
		return clean.equals("on") || clean.equals("true") || clean.equals("1") || clean.equals("si")
				|| clean.equals("activar");
	}

	private void mostrarEstadisticas(final Jugador p, final EmisorRespuesta emisor) {
		final float visibilidad = CalculadorSigilo.calcularFactorVisibilidad(p);
		final String luz = (p.getLuzAsignada() != null)
				? p.getLuzAsignada().getTipo().name() + " (" + (int) p.getLuzAsignada().getRadioActual() + "px)"
				: "Ninguna";

		final StringBuilder sb = new StringBuilder();
		sb.append("=== ESTADISTICAS DEL JUGADOR ===\n");
		sb.append(String.format(" -> Modo Dios   : %s\n",
				(p.isModoDios() ? "ACTIVADO (Inmune a daño / Sprint infinito)" : "DESACTIVADO")));
		sb.append(String.format(" -> Salud       : %d / %d HP (Lag: %d)\n", (int) p.getVida(), (int) p.getVidaMaxima(),
				(int) p.getVidaLag()));
		sb.append(String.format(" -> Estamina    : %d / %d pts\n", (int) p.getEstamina(), (int) p.getLimiteEstamina()));
		sb.append(String.format(" -> Velocidad   : %.2f (Base: %.2f)\n", p.getVelocidad(), p.getVelocidad()));
		sb.append(String.format(" -> Daño Mele   : %d pts de ataque\n", (int) p.getDamage()));
		sb.append(String.format(" -> Posicion    : (%d, %d) | Direccion: %s\n", p.getPosicionXInt(),
				p.getPosicionYInt(), p.getDireccion()));
		sb.append(String.format(" -> Visibilidad : %d%% (Rango Alerta IA)\n", (int) (visibilidad * 100)));
		sb.append(String.format(" -> Luz Anclada : %s\n", luz));
		sb.append(String.format(" -> Estados     : [ %s]\n", p.getStringEstados()));
		sb.append(" (Escribe 'player ayuda' para ver todos los comandos disponibles)");

		this.enviarInfo(emisor, sb.toString());
	}

	private Direccion parsearDireccion(final String str) {
		final String clean = str.toUpperCase().trim();
		switch (clean) {
		case "N":
		case "NORTE":
		case "NORTH":
		case "UP":
		case "ARRIBA":
			return Direccion.NORTE;
		case "S":
		case "SUR":
		case "SOUTH":
		case "DOWN":
		case "ABAJO":
			return Direccion.SUR;
		case "E":
		case "ESTE":
		case "EAST":
		case "RIGHT":
		case "DERECHA":
			return Direccion.ESTE;
		case "W":
		case "O":
		case "OESTE":
		case "WEST":
		case "LEFT":
		case "IZQUIERDA":
			return Direccion.OESTE;
		default:
			return null;
		}
	}

	private Estado parsearEstado(final String str) {
		final String clean = str.toUpperCase().trim();
		try {
			return Estado.valueOf(clean);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	private void mostrarMenuAyuda(final EmisorRespuesta emisor) {
		final String ayuda = "=== AYUDA: COMANDO PLAYER / JUGADOR ===" + "\n1. Modo Dios & Salud:"
				+ "\n   - player god [on/off]     -> Inmunidad total y estamina infinita"
				+ "\n   - player heal [cant]      -> Sana al jugador (total o cantidad)"
				+ "\n   - player damage <cant>    -> Aplica daño con destello y sangre"
				+ "\n   - player hp <val>         -> Fija la vida actual"
				+ "\n   - player maxhp <val>      -> Modifica la vida maxima"
				+ "\n   - player kill | revivir   -> Elimina o resucita al jugador" + "\n2. Estamina y Combate:"
				+ "\n   - player stamina <val>    -> Fija estamina actual"
				+ "\n   - player maxstamina <val> -> Fija límite de estamina"
				+ "\n   - player dmg <val>        -> Fija daño base de ataque cuerpo a cuerpo"
				+ "\n   - player speed <val>      -> Modifica velocidad base (ej: 2.0)" + "\n3. Movimiento y Posicion:"
				+ "\n   - player tp <x> <y>       -> Teletransporta a coordenadas"
				+ "\n   - player spawn            -> Envia al punto de inicio"
				+ "\n   - player dir <N|S|E|O>    -> Orienta la mirada" + "\n4. Maquina de Estados:"
				+ "\n   - player estado add <nombre>    -> Agrega estado (ej: CORRIENDO)"
				+ "\n   - player estado remove <nombre> -> Remueve estado"
				+ "\n   - player estado clear           -> Vuelve a estado ESTANDAR" + "\n5. Utilidades:"
				+ "\n   - player stats            -> Muestra ficha completa de atributos"
				+ "\n   - player reset            -> Restaura todos los valores de fabrica";
		this.enviarInfo(emisor, ayuda);
	}
}