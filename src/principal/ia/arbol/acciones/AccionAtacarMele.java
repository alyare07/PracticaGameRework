package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Estado;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Acción de ataque cuerpo a cuerpo con alerta social inmediata a aliados en la
 * trifulca.
 * 
 * @version 3.0 (Vanilla Java 8 - Melee Brawl Alert Integration)
 */
public class AccionAtacarMele implements NodoBT {

	private final double damage;
	private final double distanciaAlcanzableSq;
	private final double cadenciaSegundos;

	public AccionAtacarMele(final double damage, final double distanciaAlcanzable, final double cadenciaSegundos) {
		this.damage = Math.max(1.0, damage);
		this.distanciaAlcanzableSq = distanciaAlcanzable * distanciaAlcanzable;
		this.cadenciaSegundos = Math.max(0.1, cadenciaSegundos);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Ente objetivo = bb.getObjetivoActual();
		if ((objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final double dx = objetivo.getCentroX() - criatura.getCentroX();
		final double dy = objetivo.getCentroY() - criatura.getCentroY();
		final double distSq = (dx * dx) + (dy * dy);

		if (distSq > this.distanciaAlcanzableSq) {
			return EstadoBT.FRACASO;
		}

		if (objetivo instanceof Criatura) {
			criatura.setDireccionMirandoCriatura((Criatura) objetivo);
		}

		if (objetivo instanceof Criatura) {
			((Criatura) objetivo).recibirAtaque(this.damage, criatura);
			GestorSonido.reproducir(IDSonido.GOLPE_1);

			// Al conectar el golpe, alerta a los aliados cercanos que estén a menos de 90
			// px
			criatura.alertarAliadosCercanos(objetivo, 90.0);
		}

		criatura.meterEstado(Estado.ATACANDO);
		bb.iniciarCooldown(BlackboardIA.CD_ATAQUE, this.cadenciaSegundos);

		return EstadoBT.EXITO;
	}
}