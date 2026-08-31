package principal.mapa.renderEntidades.camara;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.mapa.renderEntidades.camara.efectos.EfectoInerciaDash;
import principal.mapa.renderEntidades.camara.efectos.EfectoOndaExpansiva;
import principal.mapa.renderEntidades.camara.efectos.EfectoPisoton;
import principal.mapa.renderEntidades.camara.efectos.EfectoRetrocesoDireccional;
import principal.mapa.renderEntidades.camara.efectos.EfectoTerremoto;
import principal.mapa.renderEntidades.camara.efectos.GestorEfectosCamara;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.Constantes;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Gestiona el enfoque óptico, seguimiento predictivo (Look-Ahead), zoom por
 * velocidad (Speed FOV) y franjas cinemáticas (Letterbox).
 * 
 * @version 4.5
 */
public class Camara {

	private Ente entidadEnfocada;
	private int margenX;
	private int margenY;

	private final GestorDeLimites gestorLimite;
	private final GestorEfectosCamara gestorEfectos;

	private static final double ZOOM_BASE = 1.0;
	private double zoom = 1.0;

	private boolean lookAheadHabilitado = true;
	private final double lookAheadMaxDistancia = 35.0;
	private double lookAheadX = 0.0;
	private double lookAheadY = 0.0;

	private boolean speedZoomHabilitado = true;
	private double offsetSpeedZoom = 0.0;

	private static final double ALTURA_LETTERBOX_MAX = 38.0;
	private boolean cinematicaActiva = false;
	private double alturaLetterboxActual = 0.0;
	private double alturaLetterboxObjetivo = 0.0;

	public Camara(final Ente entidadEnfocada) {
		this.gestorLimite = new GestorDeLimites();
		this.gestorEfectos = new GestorEfectosCamara();

		if (entidadEnfocada != null) {
			this.setEntidadEnfocada(entidadEnfocada);
		}
	}

	public void actualizar() {
		final double dt = (Globales.delta > 0.0) ? Globales.delta : (1.0 / 60.0);

		this.actualizarLookAhead(dt);
		this.actualizarSpeedZoom(dt);

		if (!this.gestorLimite.estaEliminado()) {
			this.gestorLimite.actualizar(this.lookAheadX, this.lookAheadY);
		}
		this.gestorEfectos.actualizar();

		if (Math.abs(this.alturaLetterboxActual - this.alturaLetterboxObjetivo) > 0.1) {
			this.alturaLetterboxActual += (this.alturaLetterboxObjetivo - this.alturaLetterboxActual) * (dt * 4.0);
		} else {
			this.alturaLetterboxActual = this.alturaLetterboxObjetivo;
		}
	}

	private void actualizarLookAhead(final double dt) {
		if (this.lookAheadHabilitado && (this.entidadEnfocada != null) && (Globales.RATON != null)) {
			final double dx = Globales.RATON.getPosicionXEscalada() - Constantes.CENTROX;
			final double dy = Globales.RATON.getPosicionYEscalada() - Constantes.CENTROY;
			final double dist = Math.hypot(dx, dy);

			double targetX = 0.0;
			double targetY = 0.0;

			if (dist > 15.0) {
				final double factor = Math.min(1.0, dist / 160.0) * this.lookAheadMaxDistancia;
				targetX = (dx / dist) * factor;
				targetY = (dy / dist) * factor;
			}

			this.lookAheadX += (targetX - this.lookAheadX) * (dt * 3.5);
			this.lookAheadY += (targetY - this.lookAheadY) * (dt * 3.5);
		} else {
			this.lookAheadX += (0.0 - this.lookAheadX) * (dt * 5.0);
			this.lookAheadY += (0.0 - this.lookAheadY) * (dt * 5.0);
		}
	}

	private void actualizarSpeedZoom(final double dt) {
		if (this.speedZoomHabilitado && (this.entidadEnfocada instanceof Criatura)) {
			final Criatura c = (Criatura) this.entidadEnfocada;
			final double vel = c.getVelocidad();
			final double targetZoomOut = (vel > 1.3) ? -0.06 : 0.0;
			this.offsetSpeedZoom += (targetZoomOut - this.offsetSpeedZoom) * (dt * 3.0);
		} else {
			this.offsetSpeedZoom += (0.0 - this.offsetSpeedZoom) * (dt * 4.5);
		}
	}

	public void activarModoCinematico(final boolean activar) {
		this.cinematicaActiva = activar;
		this.alturaLetterboxObjetivo = activar ? ALTURA_LETTERBOX_MAX : 0.0;
	}

	public boolean isModoCinematico() {
		return this.cinematicaActiva;
	}

	public void pintarLetterbox(final Graphics2D g) {
		if (this.alturaLetterboxActual <= 0.5) {
			return;
		}

		final int h = (int) Math.round(this.alturaLetterboxActual);
		Render2D.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, h, Color.BLACK);
		Render2D.dibujarRectanguloRelleno(g, 0, Constantes.ALTO_JUEGO - h, Constantes.ANCHO_JUEGO, h, Color.BLACK);
	}

	public void aplicarTemblor(final double duracionMs, final double amplitudPx) {
		this.gestorEfectos.<EfectoTerremoto>getEfecto(TipoEfectoCamara.TERREMOTO).configurar(amplitudPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.TERREMOTO, duracionMs, 1.0);
	}

	public void aplicarPisoton(final double fuerzaZoom, final double duracionMs) {
		this.gestorEfectos.<EfectoPisoton>getEfecto(TipoEfectoCamara.PISOTON).configurar(fuerzaZoom);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.PISOTON, duracionMs, 1.0);
	}

	public void aplicarRetroceso(final double dirX, final double dirY, final double fuerzaPx, final double duracionMs) {
		this.gestorEfectos.<EfectoRetrocesoDireccional>getEfecto(TipoEfectoCamara.RETROCESO_DIRECCIONAL)
				.configurarDireccion(dirX, dirY, fuerzaPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.RETROCESO_DIRECCIONAL, duracionMs, 1.0);
	}

	public void activarModoBorracho(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BORRACHO, activar, 1.0);
	}

	public void activarLatido(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.LATIDO, activar, 1.0);
	}

	public void activarModoBerserk(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BERSERK, activar, 1.0);
	}

	public void activarRespiracion(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.RESPIRACION, activar, 1.0);
	}

	public void activarVientoTormenta(final boolean activar, final double intensidad) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.VIENTO_TORMENTA, activar, intensidad);
	}

	public void aplicarAturdimiento(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ATURDIMIENTO, duracionMs, 1.0);
	}

	public void aplicarImpactoCritico(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.IMPACTO_CRITICO, duracionMs, 1.0);
	}

	public void aplicarOndaExpansiva(final double zoomOutMax, final double duracionMs) {
		this.gestorEfectos.<EfectoOndaExpansiva>getEfecto(TipoEfectoCamara.ONDA_EXPANSIVA).configurar(zoomOutMax);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ONDA_EXPANSIVA, duracionMs, 1.0);
	}

	public void aplicarInerciaDash(final double dirX, final double dirY, final double distanciaLagPx,
			final double duracionMs) {
		this.gestorEfectos.<EfectoInerciaDash>getEfecto(TipoEfectoCamara.INERCIA_DASH).configurarDireccion(dirX, dirY,
				distanciaLagPx);
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.INERCIA_DASH, duracionMs, 1.0);
	}

	public void activarNavegacionBarco(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.BARCO_NAVEGACION, activar, 1.0);
	}

	public void aplicarSobresaltoAlerta(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.ALERTA_SOBRESALTO, duracionMs, 1.0);
	}

	public void activarModoApuntado(final boolean activar) {
		this.gestorEfectos.conmutarEfectoInfinito(TipoEfectoCamara.CAMARA_LENTA_ENFOQUE, activar, 1.0);
	}

	public void aplicarCaidaAbismo(final double duracionMs) {
		this.gestorEfectos.reproducirEfectoTemporal(TipoEfectoCamara.CAIDA_ABISMO, duracionMs, 1.0);
	}

	public double getZoomFinal() {
		final double zoomBaseConEfectos = Math.max(0.2,
				this.zoom + this.gestorEfectos.getOffsetZoom() + this.offsetSpeedZoom);
		final double rotacion = this.gestorEfectos.getAnguloRotacion();

		if (rotacion == 0.0) {
			return zoomBaseConEfectos;
		}

		final double aspecto = (double) Constantes.ANCHO_JUEGO / Constantes.ALTO_JUEGO;
		final double rotAbs = Math.abs(rotacion);
		final double factorCompensacionRotacion = Math.cos(rotAbs) + (aspecto * Math.sin(rotAbs));

		return zoomBaseConEfectos * factorCompensacionRotacion;
	}

	public void setEntidadEnfocada(final Ente e) {
		if (e == null) {
			return;
		}

		this.entidadEnfocada = e;
		final int enteAncho = (e.getArea() != null) ? e.getArea().width : 0;
		final int enteAlto = (e.getArea() != null) ? e.getArea().height : 0;

		this.margenX = Constantes.CENTROX - (enteAncho / 2);
		this.margenY = Constantes.CENTROY - (enteAlto / 2);

		if (this.entidadEnfocada != this.gestorLimite.getEntidadEnfocada()) {
			this.gestorLimite.eliminar();
		}
	}

	public Ente getEntidadEnfocada() {
		return this.entidadEnfocada;
	}

	public void habilitarGestorLimite() {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada);
	}

	public void habilitarGestorLimite(final int limiteMaximoX, final int limiteMinimoX, final int limiteMaximoY,
			final int limiteMinimoY, final boolean contarDimensionEnte) {
		this.gestorLimite.restituir();
		this.gestorLimite.setEntidadEnfocada(this.entidadEnfocada, limiteMaximoX, limiteMinimoX, limiteMaximoY,
				limiteMinimoY, contarDimensionEnte);
	}

	public void deshabilitarGestorLimite() {
		this.gestorLimite.eliminar();
	}

	public double getZoom() {
		return this.zoom;
	}

	public void setZoom(final double nuevoZoom) {
		final double zoomClampeado = Math.max(0.5, Math.min(2.5, nuevoZoom));
		this.zoom = Math.round(zoomClampeado * 4.0) / 4.0;
	}

	public void reiniciarZoom() {
		this.zoom = ZOOM_BASE;
	}

	public void aumentarZoom() {
		this.setZoom(this.zoom + 0.25);
	}

	public void reducirZoom() {
		this.setZoom(this.zoom - 0.25);
	}

	public void setLookAheadHabilitado(final boolean habilitado) {
		this.lookAheadHabilitado = habilitado;
	}

	public boolean isLookAheadHabilitado() {
		return this.lookAheadHabilitado;
	}

	public void setSpeedZoomHabilitado(final boolean habilitado) {
		this.speedZoomHabilitado = habilitado;
	}

	// =========================================================================
	// === MÉTODOS DE CONMUTACIÓN RÁPIDA (TOGGLES)
	// =========================================================================

	/**
	 * Conmuta el seguimiento predictivo del ratón (Look-Ahead). Si estaba activo lo
	 * apaga, y si estaba apagado lo enciende.
	 * 
	 * @return El nuevo estado (true = encendido, false = apagado).
	 */
	public boolean conmutarLookAhead() {
		this.lookAheadHabilitado = !this.lookAheadHabilitado;
		return this.lookAheadHabilitado;
	}

	/**
	 * Conmuta el zoom dinámico por velocidad al correr (Speed FOV). Si estaba
	 * activo lo apaga, y si estaba apagado lo enciende.
	 * 
	 * @return El nuevo estado (true = encendido, false = apagado).
	 */
	public boolean conmutarSpeedZoom() {
		this.speedZoomHabilitado = !this.speedZoomHabilitado;
		return this.speedZoomHabilitado;
	}

	public boolean isSpeedZoomHabilitado() {
		return this.speedZoomHabilitado;
	}

	public GestorEfectosCamara getGestorEfectos() {
		return this.gestorEfectos;
	}

	public double getPosicionX() {
		if (this.entidadEnfocada == null) {
			return 0.0;
		}
		return (this.gestorLimite.estaEliminado()) ? (this.entidadEnfocada.getPosicionX() + this.lookAheadX)
				: this.gestorLimite.getPosicionX();
	}

	public double getPosicionY() {
		if (this.entidadEnfocada == null) {
			return 0.0;
		}
		return (this.gestorLimite.estaEliminado()) ? (this.entidadEnfocada.getPosicionY() + this.lookAheadY)
				: this.gestorLimite.getPosicionY();
	}

	public int getPosicionXInt() {
		return (int) Math.round(this.getPosicionX());
	}

	public int getPosicionYInt() {
		return (int) Math.round(this.getPosicionY());
	}

	public int getMargenX() {
		return this.margenX;
	}

	public int getMargenY() {
		return this.margenY;
	}
}