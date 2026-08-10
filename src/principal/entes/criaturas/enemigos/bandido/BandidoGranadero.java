package principal.entes.criaturas.enemigos.bandido;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import principal.animaciones.criaturas.AnimacionesBandido;
import principal.entes.objetos.items.arrojadizos.granadas.Granada;
import principal.entes.objetos.items.arrojadizos.granadas.GranadaT1;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;

public class BandidoGranadero extends Bandido {
	private final Granada granada;

	public BandidoGranadero(final double x, final double y, final double vida, final double vidaMaxima,
			final Mundo mundo) {
		super(x, y, vida, vidaMaxima, mundo);
		this.granada = new GranadaT1(100);
		this.areaDeteccionAncho = 300;
		this.areaDeteccionAlto = 300;
	}

	@Override
	public void actualizar() {
		if (this.granada.getCantidad() <= 1) {
			this.granada.establecerCantidad(100);
		}
		super.actualizar();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarSprite(g);
		super.pintar(g);
	}

	private void pintarSprite(final Graphics2D g) {
		if (!this.estaEstadoCaminando()) {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.ESTANDAR, this.atrasDeComplemento, true);
		} else {
			this.ANIMACION.pintar(g, this.getPosicionXIntDibujado(), this.getPosicionYIntDibujado(), this.direccion,
					AnimacionesBandido.CAMINANDO, this.atrasDeComplemento, true);
		}
	}

	@Override
	protected void actualizarAtaque() {
		if (this.realizandoAtaque
				&& this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			if (this.enAccion) {
				this.enAccion = false;
			}
			// Ataque del enemigo al jugador
			this.granada.arrojar(Constantes.JUGADOR.getPosicionXInt() + (Constantes.JUGADOR.getAncho() / 2),
					Constantes.JUGADOR.getPosicionYInt() + (Constantes.JUGADOR.getAlto() / 2), this.direccion,
					this.mundo, this, false);
			this.GT_RETOMAR_ATAQUE.establecerReferenciaTiempoActual();
			this.realizandoAtaque = false;
			return;
		}
		if (!this.GT_RETOMAR_ATAQUE.transcurrioMiliSegundos(this.getTiempoMsEsperaRetomarAtaque())) {
			return;
		}

		if (this.atacando) {

			this.meterEstado(Estado.ATACANDO);
			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				if (this.GT_ATAQUE_INICIAL_COOLDOWN.transcurrioMiliSegundos(this.getTiempoMsEsperaAtaqueInicial())) {

					// realiza la carga del ataque
					if (!this.realizandoAtaque) {
						this.realizandoAtaque = true;
						this.direccion = Constantes.FUNCIONES.getDireccionMirando(this.getPosicionXInt(),
								this.getPosicionYInt(), Constantes.JUGADOR.getPosicionXInt(),
								Constantes.JUGADOR.getPosicionYInt(), true);
						this.removerEstado(Estado.CAMINANDO);
						this.removerEstado(Estado.PERSIGUIENDO);
						this.removerEstado(Estado.CORRIENDO);
					}

				} else if (!this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea())) {
					this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
				}
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			} else if (!this.GE_FUERA_DE_RANGO.transcurrioMiliSegundos(this.getTiempoMsBusquedaFueraRango())) {
				if (!this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getArea())) {
					this.moverEnAtaque(this.mundo.getDijkstra(), this.mundo.getTerreno());
					this.setEstadoCaminando();
					this.meterEstado(Estado.PERSIGUIENDO);
				}
			} else {
				this.atacando = false;
				this.pendienteADijkstra = false;
				this.mundo.getDijkstra().reducirEntidadesPendientes();
			}
		} else {
			if (!this.estaEstadoEstandar()) {
				this.setEstadoEstandar();
			}

			if (this.getAreaDeteccionLogica().intersects(Constantes.JUGADOR.getRectangulo())) {
				this.GT_ATAQUE_INICIAL_COOLDOWN.establecerReferenciaTiempoActual();
				this.atacando = true;
				this.GE_FUERA_DE_RANGO.establecerReferenciaTiempoActual();
			}
		}
	}

	@Override
	public Ellipse2D getAreaDeteccionLogica() {
		return new Ellipse2D.Double((this.x - (this.areaDeteccionAncho / 2)) + (this.ANCHO / 2),
				(this.y - (this.areaDeteccionAlto / 2)) + (this.ALTO / 2), this.areaDeteccionAncho,
				this.areaDeteccionAlto);
	}

	@Override
	protected double getXRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getYRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getAlcanceRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected double getGrosorRangoAtaqueMele() {
		return 0;
	}

	@Override
	protected int getTiempoMsEsperaAtaqueInicial() {
		return this.getTiempoMsEsperaRetomarAtaque();
	}

	@Override
	protected int getTiempoMsEsperaRetomarAtaque() {
		return 1350;
	}

}
