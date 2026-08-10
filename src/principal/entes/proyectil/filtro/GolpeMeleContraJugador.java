package principal.entes.proyectil.filtro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;
import principal.utilidades.GestorTiempo;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class GolpeMeleContraJugador extends ProyectilContraJugador {

	private static final long serialVersionUID = 5631243201941847598L;

	private final GestorTiempo GT_DIBUJADO = new GestorTiempo();
	private final int TIEMPO_DIBUJADO_MS = 200;

	private boolean golpeRealizado;

	public GolpeMeleContraJugador(final double damage, final boolean penetrante, final Mundo escenario, final double x,
			final double y, final int ancho, final int alto, final Direccion direccion, final Ente causante) {
		super(damage, 0, penetrante, 0, escenario, x, y, ancho, alto, direccion, causante);
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Constantes.TECLADO.TECLA_DEBUG.presionado()) {
			DibujoDebug.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
					this.alto, Color.red);
		}
	}

	@Override
	public void actualizar() {
		if (!this.golpeRealizado) {
			this.GT_DIBUJADO.establecerReferenciaTiempoActual();
			this.verificarImpacto();
			this.golpeRealizado = true;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		} else if (this.GT_DIBUJADO.transcurrioMiliSegundos(this.TIEMPO_DIBUJADO_MS)) {
			this.eliminar();
		}

	}

	@Override
	protected void verificarImpacto() {
		final Rectangle area = this.getArea();
		if (area.intersects(Constantes.JUGADOR.getRectangulo())) {

			this.impactar(Constantes.JUGADOR);
		}
	}

}
