package principal.ia.arbol.acciones;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Criatura.Estado;
import principal.entes.criaturas.enemigos.bandido.BandidoPistolero;
import principal.ia.arbol.BlackboardIA;
import principal.ia.arbol.EstadoBT;
import principal.ia.arbol.NodoBT;
import principal.mapa.Mundo;
import principal.utilidades.Globales;

/**
 * Disparo balístico con emisión acústica de detonación y alerta de manada
 * (Zero-GC).
 * 
 * @version 5.0 (Vanilla Java 8 - Gunshot Acoustic Pulse Integration)
 */
public class AccionDisparar implements NodoBT {

	private final int damage;
	private final double velocidadBala;
	private final double cadenciaSegundos;

	public AccionDisparar(final int damage, final double velocidadBala, final double cadenciaSegundos) {
		this.damage = Math.max(1, damage);
		this.velocidadBala = Math.max(1.0, velocidadBala);
		this.cadenciaSegundos = Math.max(0.1, cadenciaSegundos);
	}

	@Override
	public EstadoBT ejecutar(final Criatura criatura, final BlackboardIA bb, final double dt) {
		final Mundo mundo = criatura.getMundo();
		final Ente objetivo = bb.getObjetivoActual();

		if ((mundo == null) || (objetivo == null) || objetivo.estaEliminado()) {
			return EstadoBT.FRACASO;
		}

		final int origenX = criatura.getCentroX();
		final int origenY = criatura.getCentroY();
		final int targetX = objetivo.getCentroX();
		final int targetY = objetivo.getCentroY();

		if (!criatura.estaEnMovimientoFisico()) {
			criatura.detenerMovimiento();
			criatura.removerEstado(Estado.CAMINANDO);
		}

		criatura.setDireccion(Globales.FUNCIONES.getDireccionMirando(origenX, origenY, targetX, targetY));
		criatura.meterEstado(Estado.ATACANDO);

		// Disparo físico del arma
		if (criatura instanceof BandidoPistolero) {
			final BandidoPistolero bp = (BandidoPistolero) criatura;
			if (bp.getPistola() != null) {
				if (bp.getPistola().isRecargando()) {
					return EstadoBT.EN_PROCESO;
				}
				bp.getPistola().disparar(origenX, origenY, targetX, targetY, mundo, bp);
			}
		} else {
			mundo.crearProyectil(this.damage, this.velocidadBala, false, 320, origenX, origenY, 4, 4,
					criatura.getDireccion(), criatura);
		}

		// PROPAGACIÓN ACÚSTICA: El estruendo del disparo viaja a 280 px y alerta a
		// aliados y centinelas
		mundo.emitirPulsoSonido(origenX, origenY, 280.0, criatura);
		criatura.alertarAliadosCercanos(objetivo, 200.0);

		bb.iniciarCooldown(BlackboardIA.CD_DISPARO, this.cadenciaSegundos);
		return EstadoBT.EXITO;
	}
}