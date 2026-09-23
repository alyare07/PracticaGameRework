package principal.mapa.peligros;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.jugador.Jugador;
import principal.entes.objetos.recursos.RocaCosechable;
import principal.mapa.Mundo;
import principal.mapa.renderEntidades.camara.efectos.TipoEfectoCamara;
import principal.utilidades.AccionEntidad;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * FSM individual de un peligro telegrafiado por desprendimiento de techo
 * (Zero-GC / O(1)).
 * <p>
 * Fases: 1. Telégrafo: Sombra elíptica 2.5D que se expande hacia el radio de
 * impacto. 2. Detonación: Daño físico + Knockback radial a criaturas y jugador.
 * 3. Materialización: Inyección de una RocaCosechable sólida en la grilla.
 * </p>
 * 
 * @version 1.1 (Vanilla Java 8 - AWT Verified API)
 */
public class DerrumbeTelegrafiado {

	private boolean activo = false;
	private double xCentro = 0.0;
	private double yCentro = 0.0;
	private int snapX = 0;
	private int snapY = 0;

	private double duracionTotal = 1.4; // Segundos de advertencia
	private double tiempoTranscurrido = 0.0;
	private double radioImpacto = 20.0;
	private double danio = 25.0;

	private final Rectangle areaImpactoAux = new Rectangle();

	private final AccionEntidad<Criatura> accionDanioCriatura = new AccionEntidad<Criatura>() {
		@Override
		public void ejecutar(final Criatura c) {
			if ((c == null) || c.estaEliminado()) {
				return;
			}
			final double dx = c.getCentroX() - DerrumbeTelegrafiado.this.xCentro;
			final double dy = c.getPieY() - DerrumbeTelegrafiado.this.yCentro;
			final double distSq = (dx * dx) + (dy * dy);

			if (distSq <= (DerrumbeTelegrafiado.this.radioImpacto * DerrumbeTelegrafiado.this.radioImpacto)) {
				c.recibirDanioDirecto(DerrumbeTelegrafiado.this.danio);
			}
		}
	};

	public DerrumbeTelegrafiado() {
	}

	public void activar(final double x, final double y, final int tileX, final int tileY, final double duracionSegundos,
			final double radio, final double danio) {
		this.xCentro = x;
		this.yCentro = y;
		this.snapX = tileX;
		this.snapY = tileY;
		this.duracionTotal = Math.max(0.5, duracionSegundos);
		this.tiempoTranscurrido = 0.0;
		this.radioImpacto = Math.max(8.0, radio);
		this.danio = Math.max(1.0, danio);
		this.activo = true;
	}

	public void actualizar(final Mundo mundo, final double dt) {
		if (!this.activo || (mundo == null)) {
			return;
		}

		this.tiempoTranscurrido += dt;

		// Micro-temblor de advertencia si el jugador está a corta distancia
		if ((Globales.JUGADOR != null) && (this.tiempoTranscurrido >= (this.duracionTotal * 0.70))) {
			final double dx = Globales.JUGADOR.getCentroX() - this.xCentro;
			final double dy = Globales.JUGADOR.getCentroY() - this.yCentro;
			if (((dx * dx) + (dy * dy)) < (80.0 * 80.0)) {
				Globales.CAMARA.getGestorEfectos().reproducirEfectoTemporal(TipoEfectoCamara.TERREMOTO, 120, 0.4);
			}
		}

		// Momento exacto del impacto
		if (this.tiempoTranscurrido >= this.duracionTotal) {
			this.detonar(mundo);
		}
	}

	private void detonar(final Mundo mundo) {
		this.activo = false;

		// 1. Efecto sísmico contundente en cámara
		Globales.CAMARA.getGestorEfectos().reproducirEfectoTemporal(TipoEfectoCamara.TERREMOTO, 320, 1.25);
		GestorSonido.reproducir(IDSonido.GOLPE_1);

		// 2. Partículas balísticas de escombros y polvo
		Globales.GESTOR_PARTICULAS.emitirPolvoPaso((int) Math.round(this.xCentro), (int) Math.round(this.yCentro), 24);

		// 3. Daño y retroceso al Jugador si está dentro del radio
		final Jugador j = Globales.JUGADOR;
		if ((j != null) && !j.estaEliminado()) {
			final double dx = j.getCentroX() - this.xCentro;
			final double dy = j.getPieY() - this.yCentro;
			final double dist = Math.sqrt((dx * dx) + (dy * dy));

			if (dist <= this.radioImpacto) {
				j.recibirAtaque(this.danio, null);

				// Vector de empuje hacia afuera
				if (dist > 0.001) {
					final double pushX = (dx / dist) * 7.0;
					final double pushY = (dy / dist) * 7.0;
					if (!mundo.colisionaConZonaUObjetoSolido(j.getAreaInterseccionMovimiento(pushX, 0.0))) {
						j.modificarPosicionX(pushX);
					}
					if (!mundo.colisionaConZonaUObjetoSolido(j.getAreaInterseccionMovimiento(0.0, pushY))) {
						j.modificarPosicionY(pushY);
					}
				}
			}
		}

		// 4. Daño en área a otras criaturas presentes
		final int rInt = (int) Math.ceil(this.radioImpacto);
		this.areaImpactoAux.setBounds((int) Math.round(this.xCentro) - rInt, (int) Math.round(this.yCentro) - rInt,
				rInt * 2, rInt * 2);
		mundo.paraCadaCriaturaEn(this.areaImpactoAux, false, this.accionDanioCriatura);

		// 5. Inyección limpia de la Roca Minable en la grilla espacial
		if (!mundo.colisionaConZonaUObjetoSolido(new Rectangle(this.snapX, this.snapY, 16, 16))) {
			final RocaCosechable roca = new RocaCosechable(this.snapX, this.snapY);
			mundo.meterEntidad(roca);
			mundo.notificarModificacionEstructura();
		}
	}

	public void pintar(final Graphics2D g) {
		if (!this.activo) {
			return;
		}

		// Progreso normalizado (0.0 a 1.0)
		final double progreso = Math.min(1.0, this.tiempoTranscurrido / this.duracionTotal);

		// Interpolación cuadrática suave (Ease-Out)
		final double factorCrecimiento = Math.sin(progreso * (Math.PI / 2.0));
		final double radioActual = this.radioImpacto * (0.25 + (0.75 * factorCrecimiento));

		final int diametroX = (int) Math.round(radioActual * 2.0);
		final int diametroY = (int) Math.round(radioActual * 1.3); // Perspectiva 2.5D elíptica

		final int xDibujo = (int) Math.round(this.xCentro - (diametroX / 2.0));
		final int yDibujo = (int) Math.round(this.yCentro - (diametroY / 2.0));

		// Modulación de alpha usando el pool de composites de Render2D (Zero-GC)
		final float alpha = Math.max(0.10f, Math.min(0.85f, (float) (progreso * 0.85)));
		final Composite comOriginal = g.getComposite();
		g.setComposite(Render2D.obtenerComposite(alpha));

		// Relleno elíptico de la sombra
		Render2D.dibujarFiguraEllipseRellenoRefCamara(g, xDibujo, yDibujo, diametroX, diametroY, Color.BLACK);

		// Anillo indicador concéntrico al acercarse a la detonación
		if (progreso > 0.60) {
			Render2D.dibujarFiguraEllipseRefCamara(g, xDibujo, yDibujo, diametroX, diametroY, Color.DARK_GRAY);
		}

		g.setComposite(comOriginal);
	}

	public boolean isActivo() {
		return this.activo;
	}

	public void desactivar() {
		this.activo = false;
	}
}