package principal.entes.proyectil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura.Direccion;
import principal.entes.criaturas.Jugador;
import principal.entes.objetos.items.herramientas.Herramienta;
import principal.entes.objetos.items.herramientas.TipoHerramienta;
import principal.entes.objetos.recursos.Cosechable;
import principal.mapa.Mundo;
import principal.utilidades.GestorTiempo;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

public class GolpeMele extends ProyectilGeneral {

	private static final long serialVersionUID = 5631243201941847598L;

	private final GestorTiempo GT_DIBUJADO = new GestorTiempo();
	private static final int TIEMPO_DIBUJADO_MS = 150;
	private boolean golpeRealizado = false;

	public GolpeMele(final double damage, final boolean penetrante, final Mundo escenario, final double x,
			final double y, final int ancho, final int alto, final Direccion direccion, final Ente causante) {
		super(damage, 0.0, penetrante, 0.0, escenario, x, y, ancho, alto, direccion, causante);
	}

	@Override
	public void actualizar() {
		if (!this.golpeRealizado) {
			this.GT_DIBUJADO.establecerReferenciaTiempoActual();
			this.verificarImpacto();
			this.golpeRealizado = true;
			GestorSonido.reproducir(IDSonido.GOLPE_1);
		} else if (this.GT_DIBUJADO.transcurrioMiliSegundos(TIEMPO_DIBUJADO_MS)) {
			this.eliminar();
		}
	}

	@Override
	protected void verificarImpacto() {
		super.verificarImpacto();

		if (this.mundo == null) {
			return;
		}

		final Rectangle area = this.getArea();

		// Detección y daño a recursos naturales (Árboles, Rocas, Minas)
		this.mundo.paraCadaObjetoEn(area, objeto -> {
			if ((objeto instanceof Cosechable) && !objeto.estaEliminado()) {
				final Cosechable recurso = (Cosechable) objeto;

				TipoHerramienta tipo = TipoHerramienta.DESARMADO;
				double potencia = this.DAMAGE;

				if (this.CAUSANTE instanceof Jugador) {
					final Jugador j = (Jugador) this.CAUSANTE;
					if (j.getArmaEquipada() instanceof Herramienta) {
						final Herramienta h = (Herramienta) j.getArmaEquipada();
						tipo = h.getTipoHerramienta();
						potencia = h.getPotenciaCosecha();
					}
				}

				recurso.golpear(tipo, potencia, this.CAUSANTE);
			}
		});
	}

	@Override
	public void pintar(final Graphics2D g) {
		if (Globales.TECLADO.TECLA_DEBUG.presionado()) {
			Render2D.dibujarRectanguloRellenoRefCamara(g, this.getPosicionXInt(), this.getPosicionYInt(), this.ancho,
					this.alto, Color.RED);
		}
	}
}