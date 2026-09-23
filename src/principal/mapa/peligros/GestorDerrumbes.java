package principal.mapa.peligros;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

import principal.configuracion.Dificultad;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.EntradaCueva;
import principal.mapa.Mundo;
import principal.mapa.mapas.Mapa;
import principal.mapa.mapas.Spawn;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Gestor maestro de peligros subterráneos, desprendimientos telegrafiados y
 * secuencia de escape por colapso catastrófico (Zero-GC / O(1)).
 * 
 * @version 2.1 (Vanilla Java 8 - Headless Subterranean Hazards)
 */
public class GestorDerrumbes {

	private static final int MAX_DERRUMBES = 8;
	private final DerrumbeTelegrafiado[] pool = new DerrumbeTelegrafiado[MAX_DERRUMBES];
	private final Random random = new Random();

	private double temporizadorSpawn = 0.0;

	// Secuencia de escape por colapso total
	private boolean secuenciaEscapeActiva = false;
	private double tiempoRestanteEscape = 0.0;
	private double duracionTotalEscape = 45.0;

	// Anclaje dinámico de retorno
	private double timestampColapsoCueva = -1.0;
	private double duracionTotalCueva = 72.0;
	private EntradaCueva entradaActiva = null;
	private String nombreMundoOrigen = "exterior";

	private final Rectangle rectValidacionAux = new Rectangle();

	public GestorDerrumbes() {
		for (int i = 0; i < MAX_DERRUMBES; i++) {
			this.pool[i] = new DerrumbeTelegrafiado();
		}
	}

	public void armarCueva(final double timestampColapso, final double duracionTotal, final EntradaCueva entrada) {
		this.timestampColapsoCueva = timestampColapso;
		this.duracionTotalCueva = Math.max(1.0, duracionTotal);
		this.entradaActiva = entrada;

		if ((entrada != null) && (entrada.getMundo() != null)) {
			this.nombreMundoOrigen = entrada.getMundo().getNombreMundo();
		} else {
			this.nombreMundoOrigen = "exterior";
		}
	}

	public void retornarAlExterior(final Criatura c) {

		if (!(c instanceof Jugador) || (c.getMundo() == null)) {
			return;
		}

		final Mapa mapa = c.getMundo().getMapa();
		if (mapa == null) {
			return;
		}

		final Mundo mundoDestino = mapa.getMundo(this.nombreMundoOrigen);
		if (mundoDestino == null) {
			return;
		}

		final String nombreSpawnRetorno = "retorno_" + c.getMundo().getNombreMundo();

		// Auto-registrar el spawn de retorno en el exterior si no existe
		if (this.entradaActiva != null) {
			final int spawnX = this.entradaActiva.getPosicionXInt() + 8;
			final int spawnY = this.entradaActiva.getPosicionYInt() + 36;
			mundoDestino.agregarSpawn(new Spawn(new Point(spawnX, spawnY), nombreSpawnRetorno));
		}

		final double liderX = c.getCentroX();
		final double liderY = c.getCentroY();
		final Mundo mundoOrigen = c.getMundo();

		// 1. Transición del jugador al exterior
		mapa.cambiarMundoInterno(this.nombreMundoOrigen, nombreSpawnRetorno);

		// 2. Migración atómica del séquito
		if (Globales.GESTOR_GRUPO != null) {
			Globales.GESTOR_GRUPO.migrarEscoltaSubmundo(mundoOrigen, mundoDestino, c, liderX, liderY, 48.0);
		}

		// 3. Resolución de la secuencia de escape si estaba activa
		if (this.secuenciaEscapeActiva) {
			this.cancelarSecuenciaEscape();
			if (this.entradaActiva != null) {
				this.entradaActiva.colapsarEntrada();
			}
			Globales.GESTOR_TEXTOS.agregarTexto("¡Escapaste del derrumbe!", c.getCentroX(), c.getPosicionYInt() - 8,
					principal.igu.textos.TipoTextoFlotante.ORO_EXP);
		}
	}

	public void actualizar(final Mundo mundo) {
		if ((mundo == null) || (mundo.getEscenario() == null) || (mundo.getEscenario().getMetadatos() == null)) {
			return;
		}

		final boolean esCueva = mundo.getEscenario().getMetadatos().esCueva();

		if (!esCueva) {
			if (this.secuenciaEscapeActiva) {
				this.cancelarSecuenciaEscape();
			}
			return;
		}

		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		// Cálculo astronómico de horas restantes
		double horasRestantes = 0.0;
		if (Globales.GESTOR_ASTRONOMICO != null) {
			horasRestantes = Math.max(0.0,
					this.timestampColapsoCueva - Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego());
		}

		// 1. Activación de la Secuencia de Escape si expira el tiempo geológico
		if (!this.secuenciaEscapeActiva && (this.timestampColapsoCueva > 0.0) && (horasRestantes <= 0.0)) {
			this.iniciarSecuenciaEscape(45.0);
			Globales.GESTOR_TEXTOS.agregarTexto("¡LA CAVERNA ESTÁ COLAPSANDO!", Globales.JUGADOR.getCentroX(),
					Globales.JUGADOR.getPosicionYInt() - 12, principal.igu.textos.TipoTextoFlotante.QUEMADURA);
		}

		// 2. Gestión de la Secuencia de Escape
		if (this.secuenciaEscapeActiva) {
			this.tiempoRestanteEscape -= dt;
			Globales.CAMARA.getGestorEfectos().conmutarEfectoInfinito(TipoEfectoCamara.TERREMOTO, true, 0.40);

			if (this.tiempoRestanteEscape <= 0.0) {
				this.ejecutarColapsoCatastrofico(mundo);
				return;
			}
		}

		// 3. Curva de Fases Geológicas (Regulación de frecuencia de caída de rocas)
		final double estabilidadPct = (horasRestantes / this.duracionTotalCueva) * 100.0;

		double intervaloObjetivo = -1.0; // -1 = 0% de rocas cayendo (Fase Estable)
		if (this.secuenciaEscapeActiva) {
			intervaloObjetivo = 2.5;
		} else if (estabilidadPct <= 20.0) {
			intervaloObjetivo = 14.0; // Peligro Crítico
		} else if (estabilidadPct <= 50.0) {
			intervaloObjetivo = 40.0; // Inestable
		}

		if (intervaloObjetivo > 0.0) {
			this.temporizadorSpawn += dt;
			if (this.temporizadorSpawn >= intervaloObjetivo) {
				this.temporizadorSpawn = 0.0;
				this.intentarSpawnDerrumbe(mundo);
			}
		}

		// 4. Actualización del pool de telégrafos activos
		for (int i = 0; i < MAX_DERRUMBES; i++) {
			if (this.pool[i].isActivo()) {
				this.pool[i].actualizar(mundo, dt);
			}
		}
	}

	private void intentarSpawnDerrumbe(final Mundo mundo) {
		final Jugador j = Globales.JUGADOR;
		if ((j == null) || j.estaEliminado()) {
			return;
		}

		DerrumbeTelegrafiado libre = null;
		for (int i = 0; i < MAX_DERRUMBES; i++) {
			if (!this.pool[i].isActivo()) {
				libre = this.pool[i];
				break;
			}
		}
		if (libre == null) {
			return;
		}

		final int offX = (this.random.nextInt(144) - 72);
		final int offY = (this.random.nextInt(144) - 72);

		final int targetX = j.getCentroX() + offX;
		final int targetY = j.getPieYInt() + offY;

		final int snapX = Math.floorDiv(targetX, 16) * 16;
		final int snapY = Math.floorDiv(targetY, 16) * 16;

		this.rectValidacionAux.setBounds(snapX, snapY, 16, 16);
		if (mundo.colisionaConZonaUObjetoSolido(this.rectValidacionAux)) {
			return;
		}

		// Protección de salida (48 px)
		final Spawn spawnSalida = mundo.getSpawn(Mundo.CLAVE_PUNTO_SPAWN_COMIENZO);
		if (spawnSalida != null) {
			final double dxSpawn = snapX - spawnSalida.getX();
			final double dySpawn = snapY - spawnSalida.getY();
			if (((dxSpawn * dxSpawn) + (dySpawn * dySpawn)) < (48.0 * 48.0)) {
				return;
			}
		}

		final double danioBase = (Globales.dificultad == Dificultad.FACIL) ? 15.0
				: (Globales.dificultad == Dificultad.NORMAL ? 25.0 : 35.0);

		final double duracionAdvertencia = this.secuenciaEscapeActiva ? 1.15 : 1.5;
		libre.activar(snapX + 8.0, snapY + 8.0, snapX, snapY, duracionAdvertencia, 22.0, danioBase);
	}

	private void ejecutarColapsoCatastrofico(final Mundo mundo) {
		this.cancelarSecuenciaEscape();

		if (this.entradaActiva != null) {
			this.entradaActiva.colapsarEntrada();
		}

		Globales.CAMARA.getGestorEfectos().reproducirEfectoTemporal(TipoEfectoCamara.TERREMOTO, 1500, 3.0);
		GestorSonido.reproducir(IDSonido.CRIATURA_MUERTA);

		if ((Globales.JUGADOR != null) && !Globales.JUGADOR.estaEliminado()) {
			Globales.JUGADOR.recibirDanioDirecto(9999.0);
		}

		mundo.eliminarCriaturas();
	}

	public void pintar(final Graphics2D g) {
		// Dibuja exclusivamente las sombras de rocas cayendo en el mundo
		for (int i = 0; i < MAX_DERRUMBES; i++) {
			if (this.pool[i].isActivo()) {
				this.pool[i].pintar(g);
			}
		}
	}

	public void iniciarSecuenciaEscape(final double segundos) {
		this.secuenciaEscapeActiva = true;
		this.duracionTotalEscape = Math.max(15.0, segundos);
		this.tiempoRestanteEscape = this.duracionTotalEscape;
		this.temporizadorSpawn = 0.0;
	}

	public void cancelarSecuenciaEscape() {
		this.secuenciaEscapeActiva = false;
		this.tiempoRestanteEscape = 0.0;
		Globales.CAMARA.getGestorEfectos().conmutarEfectoInfinito(TipoEfectoCamara.TERREMOTO, false, 0.0);
		for (int i = 0; i < MAX_DERRUMBES; i++) {
			this.pool[i].desactivar();
		}
	}

	public boolean isSecuenciaEscapeActiva() {
		return this.secuenciaEscapeActiva;
	}

	public double getHorasRestantes() {
		if ((this.timestampColapsoCueva <= 0.0) || (Globales.GESTOR_ASTRONOMICO == null)) {
			return this.duracionTotalCueva;
		}
		return Math.max(0.0, this.timestampColapsoCueva - Globales.GESTOR_ASTRONOMICO.getHorasTotalesJuego());
	}

	public double getDuracionTotalCueva() {
		return this.duracionTotalCueva;
	}

	public double getTiempoRestanteEscape() {
		return this.tiempoRestanteEscape;
	}
}