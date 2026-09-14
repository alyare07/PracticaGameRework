package principal.entes.criaturas.enemigos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.facciones.GestorFacciones;
import principal.ia.arbol.FabricaArbolesIA;
import principal.mapa.Mundo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Base abstracta para enemigos con integración de alertas de facción (Zero-GC).
 * 
 * @version 6.0 (Vanilla Java 8 - Pack Alerting Integration)
 */
public abstract class Enemigo extends Criatura {

	protected double areaDeteccionAncho = 160.0;
	protected double areaDeteccionAlto = 160.0;
	protected double ataque = 25.0;

	private final Ellipse2D.Double AREA_DETECCION_AUXILIAR = new Ellipse2D.Double();

	public Enemigo(final double x, final double y, final int ancho, final int alto, final double vida,
			final double vidaMaxima, final Mundo mundo) {
		super(x, y, ancho, alto, vida, vidaMaxima);

		this.setFaccion(GestorFacciones.FACCION_MONSTRUOS);
		this.mundo = mundo;
		this.velocidad = 0.5;

		this.arbolComportamiento = FabricaArbolesIA.ARBOL_BANDIDO_MELE;
	}

	@Override
	public void actualizar() {
		super.actualizar();
		this.curarFueraDeCombate();

		if (Globales.RATON.getRectanguloPosicionEscaladoConDesplazamientoCamara().intersects(this.getArea())
				&& Globales.RATON.presionadoClickDerUnicaAct()) {
			this.curar(Globales.JUGADOR.getDamage());
		}

		this.atrasDeComplemento = (this.mundo != null)
				&& this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
	}

	protected void curarFueraDeCombate() {
		if ((this.vida >= this.vidaMaxima) || (this.blackboard.getObjetivoActual() != null)) {
			return;
		}

		if (this.GT_CURACION.transcurrioMiliSegundos(8000)) {
			this.curar(this.vidaRegen);
			this.GT_CURACION.establecerReferenciaTiempoActual();
		}
	}

	@Override
	public void recibirAtaque(final double damage, final Ente causante) {
		if (causante instanceof Criatura) {
			this.fijarObjetivo((Criatura) causante);
			this.GT_ATACADO.establecerReferenciaTiempoActual();
		}
		// Delega en Criatura, la cual propaga el pulso de socorro a toda la manada
		super.recibirAtaque(damage, causante);
	}

	public void fijarObjetivo(final Criatura objetivo) {
		if (objetivo != null) {
			this.blackboard.setObjetivoActual(objetivo);
			this.meterEstado(Estado.PERSIGUIENDO);
			this.removerEstado(Estado.ESTANDAR);
		}
	}

	public void desactivarModoAgresivo() {
		this.blackboard.setObjetivoActual(null);
		this.blackboard.olvidarPosicionObjetivo();
		this.removerEstado(Estado.ATACANDO);
		this.removerEstado(Estado.PERSIGUIENDO);
		this.setEstadoEstandar();
	}

	public Criatura getObjetivoActual() {
		final Ente obj = this.blackboard.getObjetivoActual();
		return (obj instanceof Criatura) ? (Criatura) obj : null;
	}

	public double getAtaque() {
		return this.ataque;
	}

	public void setAtaque(final double ataque) {
		this.ataque = Math.max(1.0, ataque);
	}

	public double getAreaDeteccionAncho() {
		return this.areaDeteccionAncho;
	}

	public Ellipse2D getAreaDeteccionLogica() {
		this.AREA_DETECCION_AUXILIAR.setFrame(
				(this.getPosicionX() - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0),
				(this.getPosicionY() - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
		return this.AREA_DETECCION_AUXILIAR;
	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);

		if (Globales.TECLADO.TECLA_DEBUG.presionado() && Globales.estadoJuego) {
			Render2D.dibujarFiguraEllipseRefCamara(g,
					(int) ((this.getPosicionX() - (this.areaDeteccionAncho / 2.0)) + (this.ANCHO / 2.0)),
					(int) ((this.getPosicionY() - (this.areaDeteccionAlto / 2.0)) + (this.ALTO / 2.0)),
					(int) this.areaDeteccionAncho, (int) this.areaDeteccionAlto, Color.RED);
		}
	}

	@Override
	public void eliminar() {
		GestorSonido.reproducir(IDSonido.CRIATURA_MUERTA);
		this.desactivarModoAgresivo();

		if (this.mundo != null) {
			final long loot = 5L + (long) (Math.random() * 20.0);
			this.mundo.meterEntidad(principal.entes.objetos.items.monedas.ItemMoneda.crearPlata(this.getCentroX(),
					this.getCentroY(), loot));

			if (Globales.GESTOR_DELTAS != null) {
				Globales.GESTOR_DELTAS.registrarDestruccion(this.mundo, this.getPosicionXInicial(),
						this.getPosicionYInicial());
			}
		}
		super.eliminar();
	}
}