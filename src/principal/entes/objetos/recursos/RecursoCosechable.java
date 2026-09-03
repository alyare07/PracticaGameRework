package principal.entes.objetos.recursos;

import java.awt.Color;
import java.awt.Graphics2D;

import principal.entes.Ente;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.mapa.persistencia.DeltaMundo;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public abstract class RecursoCosechable extends Objeto implements Cosechable {

	private static final long serialVersionUID = 1L;

	protected double durabilidad;
	protected double durabilidadMaxima;
	protected TipoHerramienta herramientaRequerida;

	protected int shakeOffsetX = 0;
	protected final GestorTiempo GT_SHAKE = new GestorTiempo();
	protected static final int TIEMPO_MS_SHAKE = 120;

	public RecursoCosechable(final int x, final int y, final double durabilidadMaxima,
			final TipoHerramienta herramientaRequerida) {
		super(x, y);
		this.durabilidadMaxima = Math.max(1.0, durabilidadMaxima);
		this.durabilidad = this.durabilidadMaxima;
		this.herramientaRequerida = herramientaRequerida;
	}

	@Override
	public boolean golpear(final TipoHerramienta tipo, final double potencia, final Ente causante) {
		if (this.eliminado) {
			return false;
		}

		double danioEfectivo = potencia;

		if (tipo == this.herramientaRequerida) {
			danioEfectivo = Math.max(1.0, potencia);
		} else {
			danioEfectivo = Math.max(0.2, potencia * 0.20);
		}

		this.durabilidad -= danioEfectivo;
		this.activarShake();
		this.emitirParticulasImpacto();

		Globales.GESTOR_TEXTOS.agregarDanio((int) Math.ceil(danioEfectivo), this.getCentroX(), this.getPosicionYInt(),
				false);

		if ((Globales.CAMARA != null) && (Globales.CAMARA.getEntidadEnfocada() != null)) {
			GestorSonido.reproducirEnPosicion(IDSonido.GOLPE_1, this.getCentroX(), this.getCentroY(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionX(),
					Globales.CAMARA.getEntidadEnfocada().getPosicionY());
		}

		if (this.durabilidad <= 0.0) {
			this.destruir(causante);
			return true;
		}

		return false;
	}

	protected void activarShake() {
		this.shakeOffsetX = (Math.random() < 0.5) ? -2 : 2;
		this.GT_SHAKE.establecerReferenciaTiempoActual();
	}

	@Override
	public void actualizar() {
		super.actualizar();

		if ((this.shakeOffsetX != 0) && this.GT_SHAKE.transcurrioMiliSegundos(TIEMPO_MS_SHAKE)) {
			this.shakeOffsetX = 0;
		}
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (this.getTextura() != null) {
			Render2D.dibujarImagenRefCamara(g, this.getTextura(), this.getPosicionXInt() + this.shakeOffsetX,
					this.getPosicionYInt());
		}

		if (Globales.TECLADO.TECLA_VER_COLISIONES.presionado() && Globales.estadoJuego) {
			Render2D.dibujarRectanguloContornoRefCamara(g, this.getArea(), Color.ORANGE);
		}
	}

	public void destruir(final Ente causante) {
		if (this.mundo != null) {
			this.soltarBotin();

			// Registra la destrucción en el Delta con el nombre unificado del mundo
			if (Globales.GESTOR_DELTAS != null) {
				final String clave = this.mundo.getNombreMundo();
				final DeltaMundo delta = Globales.GESTOR_DELTAS.obtenerOCrearDelta(clave, 0);
				delta.registrarDestruccion(this.getPosicionXInt(), this.getPosicionYInt());
			}
			this.mundo.notificarModificacionEstructura();
		}
		this.eliminar();
	}

	protected abstract void soltarBotin();

	protected abstract void emitirParticulasImpacto();

	@Override
	public double getDurabilidad() {
		return this.durabilidad;
	}

	@Override
	public double getDurabilidadMaxima() {
		return this.durabilidadMaxima;
	}

	@Override
	public TipoHerramienta getHerramientaRequerida() {
		return this.herramientaRequerida;
	}

	@Override
	public boolean esSolido() {
		return true;
	}
}