package principal.entes.criaturas.mascotas;

import java.awt.Color;
import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesComerciante;
import principal.dialogos.MensajeDialogo;
import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.criaturas.grupo.TipoVinculo;
import principal.entes.facciones.GestorFacciones;
import principal.ia.arbol.FabricaArbolesIA;
import principal.igu.textos.TipoTextoFlotante;
import principal.interaccion.Interactuable;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Mascota acompañante con menú de órdenes tácticas limpio (sin opciones de
 * debug/dificultad), vinculación al grupo (TipoVinculo) y huida programada a
 * 1.5h (Zero-GC).
 * 
 * @version 5.1 (Vanilla Java 8 - Clean Roleplay Interaction Menu)
 */
public class Mascota extends Criatura implements Interactuable {

	private final String nombre;
	private final AnimacionesComerciante ANIMACION;

	private boolean siguiendo = false;
	private boolean agresivo = false;
	private boolean puedeTparseAlLider = false;

	public Mascota(final double x, final double y, final String nombre, final double vidaMaxima,
			final TipoVinculo vinculoInicial) {
		super(x, y, 12, 16, vidaMaxima, vidaMaxima, 0.95);

		this.nombre = (nombre != null) ? nombre : "Compañero";
		this.vinculo = (vinculoInicial != null) ? vinculoInicial : TipoVinculo.NINGUNO;

		if (this.tieneVinculoConJugador()) {
			this.setFaccion(GestorFacciones.FACCION_JUGADOR);
		} else {
			this.setFaccion(GestorFacciones.FACCION_NEUTRAL);
		}

		this.ANIMACION = new AnimacionesComerciante();
		this.direccion = Direccion.SUR;
		this.setEstadoUnico(Estado.ESTANDAR);

		this.configurarFootprint(8, 8, 0);

		this.arbolComportamiento = FabricaArbolesIA.ARBOL_MASCOTA_ACOMPANANTE;
		this.blackboard.setSiguiendoLider(this.siguiendo);
		this.blackboard.setModoAgresivo(this.agresivo);
		this.blackboard.setPuedeTparseAlLider(this.puedeTparseAlLider);

		if (this.tieneVinculoConJugador() && (Globales.GESTOR_GRUPO != null)) {
			Globales.GESTOR_GRUPO.registrarEnRoster(this);
			if (!Globales.GESTOR_GRUPO.estaLlenoElGrupo()) {
				this.setSiguiendo(true);
			}
		}
	}

	public Mascota(final double x, final double y, final String nombre, final double vidaMaxima) {
		this(x, y, nombre, vidaMaxima, TipoVinculo.NINGUNO);
	}

	public Mascota(final double x, final double y) {
		this(x, y, "Compañero", 80.0, TipoVinculo.NINGUNO);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.eliminado) {
			return;
		}

		this.actualizarAnimacion();
		this.actualizarOclusion();
	}

	private void actualizarAnimacion() {
		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;
		this.ANIMACION.actualizar(this.direccion, tipoAnim);
	}

	private void actualizarOclusion() {
		if (this.mundo != null) {
			this.atrasDeComplemento = this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
		}
	}

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		super.recibirAtaque(damage, causante);

		if (this.tieneVinculoConJugador() && !this.siguiendo && !this.agresivo && (causante != null)) {
			this.blackboard.setEnPanico(true);
			this.blackboard.setObjetivoActual(causante);
			this.blackboard.fijarAnclaRetorno(this.getPieX(), this.getPieY());

			if (Globales.GESTOR_ASTRONOMICO != null) {
				final double ahoraHoras = Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego();
				this.blackboard.setTimestampRetornoJuegoHoras(ahoraHoras + 1.5);
			}
		}
	}

	@Override
	public String getTextoPrompt() {
		return this.tieneVinculoConJugador() ? ("Órdenes para " + this.nombre) : ("Acercarse a " + this.nombre);
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		this.setDireccionMirandoCriatura(jugador);
		GestorSonido.reproducir(IDSonido.SELECT);

		// 1. CASO SILVESTRE: Ofrece adopción
		if (!this.tieneVinculoConJugador()) {
			final MensajeDialogo dialogoAdopcion = new MensajeDialogo(this.nombre, new Color(140, 220, 140),
					"El animal te observa con curiosidad y olfatea tu mano pacíficamente. ¿Deseas adoptarlo?", null);

			dialogoAdopcion.agregarOpcion("1. Adoptar como compañero", () -> {
				Mascota.this.vincularAlGrupo(TipoVinculo.MASCOTA);
			});

			dialogoAdopcion.agregarOpcion("2. Dejarlo en paz.", () -> {
			});

			Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogoAdopcion);
			return;
		}

		// 2. CASO VINCULADO: Menú de órdenes sin opciones de dificultad
		final String opcionSeguimiento = this.siguiendo ? "1. Quédate aquí y espera" : "1. Sígueme";
		final String opcionActitud = this.agresivo ? "2. Poner en modo Tranquilo (no atacar)"
				: "2. Poner en modo Agresivo (atacar enemigos)";

		final MensajeDialogo dialogo = new MensajeDialogo(this.nombre, new Color(100, 200, 255),
				"¿Cuáles son tus órdenes, camarada?", null);

		// Opción 1: Alternar Postura
		dialogo.agregarOpcion(opcionSeguimiento, () -> {
			if (!Mascota.this.siguiendo) {
				final int cupoMax = (Globales.GESTOR_GRUPO != null) ? Globales.GESTOR_GRUPO.getCapacidadMaximaActual()
						: 0;
				final int activos = (Globales.GESTOR_GRUPO != null)
						? Globales.GESTOR_GRUPO.getCantidadSeguidoresActivos()
						: 0;

				if ((Globales.GESTOR_GRUPO != null) && Globales.GESTOR_GRUPO.estaLlenoElGrupo()) {
					GestorSonido.reproducir(IDSonido.SIN_MUNICION);
					final String msg;
					if (cupoMax <= 0) {
						msg = "¡Requiere " + Jugador.PUNTOS_INT_POR_SEGUIDOR + " INT para liderar!";
					} else {
						final int siguienteIntRequerida = (activos + 1) * Jugador.PUNTOS_INT_POR_SEGUIDOR;
						msg = "¡Séquito lleno (" + activos + "/" + cupoMax + ")! Requiere " + siguienteIntRequerida
								+ " INT";
					}
					Globales.GESTOR_TEXTOS.agregarTexto(msg, Mascota.this.getCentroX(),
							Mascota.this.getPosicionYInt() - 8, TipoTextoFlotante.ESTADO);
					return;
				}

				Mascota.this.setSiguiendo(true);
				Globales.GESTOR_TEXTOS.agregarTexto("¡" + Mascota.this.nombre + " te sigue!", Mascota.this.getCentroX(),
						Mascota.this.getPosicionYInt() - 8, TipoTextoFlotante.ORO_EXP);
			} else {
				Mascota.this.setSiguiendo(false);
				Globales.GESTOR_TEXTOS.agregarTexto("¡" + Mascota.this.nombre + " se queda aquí!",
						Mascota.this.getCentroX(), Mascota.this.getPosicionYInt() - 8, TipoTextoFlotante.ORO_EXP);
			}
		});

		// Opción 2: Alternar Actitud
		dialogo.agregarOpcion(opcionActitud, () -> {
			Mascota.this.setAgresivo(!Mascota.this.agresivo);
			final String msg = Mascota.this.agresivo ? "¡" + Mascota.this.nombre + " atacará a los enemigos!"
					: "¡" + Mascota.this.nombre + " se mantendrá en paz!";
			Globales.GESTOR_TEXTOS.agregarTexto(msg, Mascota.this.getCentroX(), Mascota.this.getPosicionYInt() - 8,
					TipoTextoFlotante.ORO_EXP);
		});

		// Opción 3: Despedir / Dejar en libertad
		dialogo.agregarOpcion("3. Despedir del grupo (Liberar)", () -> {
			Mascota.this.desvincularDelGrupo();
		});

		// Opción 4: Salir
		dialogo.agregarOpcion("4. Nada por ahora.", () -> {
		});

		Globales.GESTOR_DIALOGOS.iniciarDialogo(dialogo);
	}

	public void vincularAlGrupo(final TipoVinculo tipo) {
		this.setVinculo(tipo);
		this.setFaccion(GestorFacciones.FACCION_JUGADOR);

		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.registrarEnRoster(this);

			if (!Globales.GESTOR_GRUPO.estaLlenoElGrupo()) {
				this.setSiguiendo(true);
				GestorSonido.reproducir(IDSonido.SELECT);
				Globales.GESTOR_TEXTOS.agregarTexto("¡Has adoptado a " + this.nombre + "!", this.getCentroX(),
						this.getPosicionYInt() - 8, TipoTextoFlotante.ORO_EXP);
			} else {
				this.setSiguiendo(false);
				GestorSonido.reproducir(IDSonido.SELECT);
				Globales.GESTOR_TEXTOS.agregarTexto("¡Adoptaste a " + this.nombre + "! (Esperará aquí por cupo)",
						this.getCentroX(), this.getPosicionYInt() - 8, TipoTextoFlotante.ESTADO);
			}
		}
	}

	public void desvincularDelGrupo() {
		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.desvincularDeRoster(this);
		}
		this.setVinculo(TipoVinculo.NINGUNO);
		this.setFaccion(GestorFacciones.FACCION_NEUTRAL);
		this.setSiguiendo(false);

		GestorSonido.reproducir(IDSonido.SIN_MUNICION);
		Globales.GESTOR_TEXTOS.agregarTexto("¡" + this.nombre + " vuelve a ser libre!", this.getCentroX(),
				this.getPosicionYInt() - 8, TipoTextoFlotante.ESTADO);
	}

	@Override
	public void eliminar() {
		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.desvincularDeRoster(this);
		}
		super.eliminar();
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.estaEliminado();
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int drawX = this.getPosicionXIntDibujado();
		final int drawY = this.getPosicionYIntDibujado();
		final boolean flash = this.estaEnFlashDanio();
		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;

		this.ANIMACION.pintar(g, drawX, drawY, this.direccion, tipoAnim, this.atrasDeComplemento, true, flash);
		super.pintar(g);
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		this.exportarDatosCriaturaBase(json);
		json.put("nombre", this.nombre);
		json.put("siguiendo", Boolean.valueOf(this.siguiendo));
		json.put("agresivo", Boolean.valueOf(this.agresivo));
		json.put("puedeTparse", Boolean.valueOf(this.puedeTparseAlLider));
		return json;
	}

	public static Mascota crearDesdeJSON(final JSONObject json) {
		if (json == null) {
			return new Mascota(0, 0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String nombre = (json.get("nombre") != null) ? json.get("nombre").toString() : "Compañero";
		final double vidaMax = (json.get("vidaMaxima") != null) ? ((Number) json.get("vidaMaxima")).doubleValue()
				: 80.0;

		TipoVinculo vinculoRecuperado = TipoVinculo.NINGUNO;
		if (json.get("vinculo") != null) {
			try {
				vinculoRecuperado = TipoVinculo.valueOf(json.get("vinculo").toString());
			} catch (final Exception ignored) {
			}
		}

		final Mascota mascota = new Mascota(x, y, nombre, vidaMax, vinculoRecuperado);
		mascota.importarDatosCriaturaBase(json);

		if (json.get("siguiendo") != null) {
			mascota.siguiendo = Boolean.parseBoolean(json.get("siguiendo").toString());
			mascota.getBlackboard().setSiguiendoLider(mascota.siguiendo);
		}
		if (json.get("agresivo") != null) {
			mascota.setAgresivo(Boolean.parseBoolean(json.get("agresivo").toString()));
		}
		if (json.get("puedeTparse") != null) {
			mascota.setPuedeTparseAlLider(Boolean.parseBoolean(json.get("puedeTparse").toString()));
		}

		return mascota;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Mascota";
	}

	@Override
	public String getNombre() {
		return this.nombre;
	}

	public boolean isSiguiendo() {
		return this.siguiendo;
	}

	public void setSiguiendo(final boolean siguiendo) {
		this.siguiendo = siguiendo;
		this.blackboard.setSiguiendoLider(siguiendo);

		if (Globales.GESTOR_GRUPO != null) {
			if (siguiendo && this.tieneVinculoConJugador()) {
				Globales.GESTOR_GRUPO.agregarSeguidor(this);
			} else {
				Globales.GESTOR_GRUPO.removerSeguidor(this);
			}
		}

		if (!siguiendo) {
			this.detenerMovimiento();
			this.setEstadoEstandar();
		} else {
			this.blackboard.limpiarAnclaRetorno();
			this.blackboard.setEnPanico(false);
			this.blackboard.setTimestampRetornoJuegoHoras(0.0);
		}
	}

	public boolean isAgresivo() {
		return this.agresivo;
	}

	public void setAgresivo(final boolean agresivo) {
		this.agresivo = agresivo;
		this.blackboard.setModoAgresivo(agresivo);

		if (!agresivo) {
			this.blackboard.setObjetivoActual(null);
			this.removerEstado(Estado.ATACANDO);
			this.removerEstado(Estado.PERSIGUIENDO);
		}
	}

	public boolean isPuedeTparseAlLider() {
		return this.puedeTparseAlLider;
	}

	public void setPuedeTparseAlLider(final boolean puedeTparseAlLider) {
		this.puedeTparseAlLider = puedeTparseAlLider;
		this.blackboard.setPuedeTparseAlLider(puedeTparseAlLider);
	}
}