package principal.ia.arbol;

import principal.ia.arbol.acciones.AccionAproximarse;
import principal.ia.arbol.acciones.AccionArrojarGranada;
import principal.ia.arbol.acciones.AccionAtacarMele;
import principal.ia.arbol.acciones.AccionDisparar;
import principal.ia.arbol.acciones.AccionFlanquearTiro;
import principal.ia.arbol.acciones.AccionHuir;
import principal.ia.arbol.acciones.AccionInvestigarPunto;
import principal.ia.arbol.acciones.AccionMantenerDistancia;
import principal.ia.arbol.acciones.AccionMirarAlrededor;
import principal.ia.arbol.acciones.AccionRegresarASpawn;
import principal.ia.arbol.acciones.AccionSeguirLider;
import principal.ia.arbol.acciones.AccionVagar;
import principal.ia.arbol.condiciones.CondicionAmenazaCercana;
import principal.ia.arbol.condiciones.CondicionDistanciaObjetivo;
import principal.ia.arbol.condiciones.CondicionEsperaRetornoActiva;
import principal.ia.arbol.condiciones.CondicionLineaDeTiroLimpia;
import principal.ia.arbol.condiciones.CondicionObjetivoVisible;
import principal.ia.arbol.condiciones.CondicionSaludBaja;
import principal.ia.arbol.condiciones.CondicionTieneSospecha;

/**
 * Catálogo central de Arquetipos de Comportamiento con sincronización de rangos
 * melee (anti-overlap) y topología limpia.
 * 
 * @version 13.1 (Vanilla Java 8 - Melee Combat Range Synchronization)
 */
public final class FabricaArbolesIA {

	private static final NodoBT ACCION_DISPARO_CON_CADENCIA = new Secuencia(new CondicionLineaDeTiroLimpia(),
			new DecoradorCooldown(BlackboardIA.CD_DISPARO, 1.1, new AccionDisparar(12, 4.5, 1.1)));

	private static final NodoBT ACCION_LANZAR_GRANADA_CON_CADENCIA = new DecoradorCooldown(BlackboardIA.CD_DISPARO, 2.2,
			new AccionArrojarGranada(2.2));

	/**
	 * Arquetipo Bandido Melee (Distancia de acercamiento sincronizada con el golpe
	 * a 26.0 px)
	 */
	public static final NodoBT ARBOL_BANDIDO_MELE = new SelectorPrioridad(
			// 1. Huida por salud crítica
			new Secuencia(new CondicionSaludBaja(0.20), new AccionHuir(220.0)),

			// 2. Combate Melee: aproximación y ataque comparten exactamente el mismo rango
			// de 26.0 px
			new Secuencia(new DecoradorInvertir(new CondicionSaludBaja(0.20)), new CondicionObjetivoVisible(180.0, 1.4),
					new SelectorPrioridad(new Secuencia(CondicionDistanciaObjetivo.menorQue(26.0),
							new DecoradorCooldown(BlackboardIA.CD_ATAQUE, 0.75,
									new AccionAtacarMele(15.0, 26.0, 0.75))),
							new AccionAproximarse(26.0))),

			// 3. Inspección de ruidos
			new Secuencia(new CondicionTieneSospecha(6.0), new AccionInvestigarPunto(3.5)),

			// 4. Merodeo pasivo
			new SelectorPrioridad(new AccionVagar(48.0, 2.5, 5.0), new AccionMirarAlrededor(2.0, 4.0)));

	/**
	 * Arquetipo Bandido Pistolero
	 */
	public static final NodoBT ARBOL_BANDIDO_PISTOLERO = new SelectorPrioridad(
			// 1. Huida por salud crítica
			new Secuencia(new CondicionSaludBaja(0.20), new AccionHuir(220.0)),

			// 2. Combate a distancia con Kiting
			new Secuencia(new DecoradorInvertir(new CondicionSaludBaja(0.20)), new CondicionObjetivoVisible(240.0, 1.4),
					new SelectorPrioridad(
							new Secuencia(CondicionDistanciaObjetivo.menorQue(65.0),
									new AccionMantenerDistancia(65.0, 115.0, ACCION_DISPARO_CON_CADENCIA)),
							new Secuencia(CondicionDistanciaObjetivo.entre(45.0, 240.0), ACCION_DISPARO_CON_CADENCIA),
							new AccionFlanquearTiro(40.0), new AccionAproximarse(90.0))),

			// 3. Inspección
			new Secuencia(new CondicionTieneSospecha(7.0), new AccionInvestigarPunto(4.0)),

			// 4. Merodeo
			new SelectorPrioridad(new AccionVagar(40.0, 3.0, 6.0), new AccionMirarAlrededor(2.0, 4.0)));

	/**
	 * Arquetipo Bandido Granadero
	 */
	public static final NodoBT ARBOL_BANDIDO_GRANADERO = new SelectorPrioridad(
			// 1. Huida por salud crítica
			new Secuencia(new CondicionSaludBaja(0.20), new AccionHuir(220.0)),

			// 2. Combate con Granadas
			new Secuencia(new DecoradorInvertir(new CondicionSaludBaja(0.20)), new CondicionObjetivoVisible(260.0, 1.4),
					new SelectorPrioridad(
							new Secuencia(CondicionDistanciaObjetivo.menorQue(75.0),
									new AccionMantenerDistancia(75.0, 130.0,
											new Secuencia(CondicionDistanciaObjetivo.mayorQue(55.0),
													ACCION_LANZAR_GRANADA_CON_CADENCIA))),
							new Secuencia(CondicionDistanciaObjetivo.entre(75.0, 220.0),
									ACCION_LANZAR_GRANADA_CON_CADENCIA),
							new AccionFlanquearTiro(40.0), new AccionAproximarse(120.0))),

			// 3. Inspección
			new Secuencia(new CondicionTieneSospecha(6.0), new AccionInvestigarPunto(3.5)),

			// 4. Merodeo
			new SelectorPrioridad(new AccionVagar(44.0, 3.0, 6.0), new AccionMirarAlrededor(2.0, 4.0)));

	/**
	 * Arquetipo Mascota / Compañero (Rango melee y aproximación unificados a 26.0
	 * px)
	 */
	public static final NodoBT ARBOL_MASCOTA_ACOMPANANTE = new SelectorPrioridad(
			new Secuencia(new CondicionObjetivoVisible(48.0, 1.7),
					new SelectorPrioridad(new Secuencia(CondicionDistanciaObjetivo.menorQue(26.0),
							new DecoradorCooldown(BlackboardIA.CD_ATAQUE, 0.6, new AccionAtacarMele(12.0, 26.0, 0.6))),
							new AccionAproximarse(26.0))),
			new AccionSeguirLider(), new AccionMirarAlrededor(2.5, 5.0));

	/**
	 * Arquetipo Comerciante / Aldeano Pasivo
	 */
	public static final NodoBT ARBOL_NPC_COMERCIANTE = new SelectorPrioridad(
			new Secuencia(new CondicionAmenazaCercana(140.0, 1.5), new AccionHuir(190.0)),
			new Secuencia(new CondicionEsperaRetornoActiva(), new AccionMirarAlrededor(2.0, 4.5)),
			new AccionRegresarASpawn(20.0), new AccionMirarAlrededor(3.0, 6.5));

	private FabricaArbolesIA() {
	}
}