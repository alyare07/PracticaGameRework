package principal.mapa.renderEntidades;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.mapa.Mundo;

/**
 * Gestor global y registro centralizado de estados de renderizado para todas
 * las entidades activas.
 * <p>
 * Se encarga del ciclo de vida de los envoltorios {@link RenderEntidad},
 * asignación de códigos de grupo e integración directa con la estructura del
 * mapa.
 * </p>
 */
public class MapRender {

	/** Identificador único progresivo para el agrupamiento de renders. */
	private long codRenders = Long.MIN_VALUE;

	/** Contador interno para agrupar asignaciones de código por lotes. */
	private int contadorGrupo;

	/** Límite de entidades asignadas por cada lote de código de render. */
	private final int LIMITE_POR_GRUPOS = 200;

	/**
	 * Registro central de entidades y sus respectivos gestores de renderizado.
	 * <p>
	 * <b>Optimización de Memoria y GC:</b> Se utiliza {@link IdentityHashMap} en
	 * lugar de un {@code HashMap} convencional. Al comparar claves mediante
	 * igualdad de referencia ({@code ==}) y no por {@code equals()}, elimina el
	 * costo de cómputo y previene la sobrecarga de instancias envolventes
	 * (Autoboxing), logrando un acceso puro $O(1)$.
	 * </p>
	 */
	private final Map<Ente, RenderEntidad> REGISTRO_RENDERS;

	/** Referencia al escenario/mundo principal. */
	private final Mundo ESCENARIO;

	/**
	 * Construye un nuevo gestor de renderizado asignado a un mapa específico.
	 *
	 * @param esc Referencia a la instancia activa de {@link Mundo}.
	 */
	public MapRender(final Mundo esc) {
		this.REGISTRO_RENDERS = new IdentityHashMap<>();
		this.ESCENARIO = esc;
	}

	/**
	 * Calcula y retorna el siguiente código de grupo de renderizado.
	 * <p>
	 * Incrementa el código una vez alcanzado el límite por grupo
	 * ({@link #LIMITE_POR_GRUPOS}). Incluye control de desbordamiento de bits para
	 * reciclar el valor en caso de alcanzar {@link Long#MAX_VALUE}.
	 * </p>
	 *
	 * @return El identificador de código de render actual.
	 */
	public long getNextcodRenders() {
		if (this.nextContadorGrupo() == this.LIMITE_POR_GRUPOS) {
			if (this.codRenders < Long.MAX_VALUE) {
				this.codRenders++;
			} else {
				// Previene desbordamiento reiniciando en el límite inferior
				this.codRenders = Long.MIN_VALUE + 1;
			}
		}
		return this.codRenders;
	}

	/**
	 * Incrementa el contador interno del grupo actual y lo reinicia si supera el
	 * límite establecido.
	 *
	 * @return El valor del contador del grupo tras la evaluación.
	 */
	private int nextContadorGrupo() {
		if (this.contadorGrupo <= this.LIMITE_POR_GRUPOS) {
			return this.contadorGrupo++;
		}
		this.contadorGrupo = 0;
		return this.contadorGrupo;
	}

	/**
	 * Obtiene el gestor de renderizado asociado a una entidad específica.
	 *
	 * @param e Instancia de {@link Ente} a consultar.
	 * @return Su objeto {@link RenderEntidad} asociado, o {@code null} si no está
	 *         registrada.
	 */
	public RenderEntidad getRender(final Ente e) {
		return this.REGISTRO_RENDERS.get(e);
	}

	/**
	 * Instancia, vincula y registra un nuevo {@link RenderEntidad} para una entidad
	 * dada.
	 *
	 * @param e La entidad a ingresar en el sistema de renderizado.
	 * @return La instancia de {@link RenderEntidad} recién creada y vinculada.
	 */
	public RenderEntidad meterEntidad(final Ente e) {
		final long cod = this.getNextcodRenders();
		e.setCodRender(cod);
		final RenderEntidad render = new RenderEntidad(e, this.ESCENARIO);

		this.REGISTRO_RENDERS.put(e, render);
		return render;
	}

	/**
	 * Registra directamente una instancia previa de {@link RenderEntidad}
	 * asignándole un nuevo código.
	 *
	 * @param re El gestor de renderizado a incorporar.
	 */
	public void meterEntidad(final RenderEntidad re) {
		final long cod = this.getNextcodRenders();
		re.getEntidad().setCodRender(cod);

		this.REGISTRO_RENDERS.put(re.getEntidad(), re);
	}

	/**
	 * Remueve una entidad del mapa de renders y limpia sus asociaciones con las
	 * zonas espaciales ({@code ZoneBox}).
	 *
	 * @param e La entidad a desvincular.
	 */
	public void eliminarEntidad(final Ente e) {
		final RenderEntidad re = this.REGISTRO_RENDERS.remove(e);
		if (re != null) {
			// Desvincula la entidad de todas las ZoneBox que ocupaba
			re.limpiarZonas();
		}
	}

	/**
	 * Obtiene la cantidad total de entidades activas registradas actualmente.
	 *
	 * @return Número total de renders administrados.
	 */
	public long getCantEntidades() {
		return this.REGISTRO_RENDERS.size();
	}

	/**
	 * Comprueba si una entidad se encuentra registrada actualmente en el sistema.
	 *
	 * @param e Entidad a verificar.
	 * @return {@code true} si está registrada; {@code false} en caso contrario.
	 */
	public boolean containsKey(final Ente e) {
		return this.REGISTRO_RENDERS.containsKey(e);
	}

	/**
	 * Retorna el conjunto de todas las entidades registradas.
	 *
	 * @return Un {@link Set} con las claves de entidad activas.
	 */
	public Set<Ente> getEntes() {
		return this.REGISTRO_RENDERS.keySet();
	}

	/**
	 * Recolecta y remueve de manera segura todas las instancias de {@link Criatura}
	 * registradas.
	 * <p>
	 * <b>Nota de Concurrencia:</b> Utiliza una lista intermedia de recolección para
	 * prevenir fallos de modificación concurrente al remover elementos mientras se
	 * itera sobre la colección principal.
	 * </p>
	 */
	public void eliminarCriaturas() {
		final ArrayList<Ente> criaturasAEliminar = new ArrayList<>();

		// Fase 1: Identificación y recolección
		for (final Ente ente : this.REGISTRO_RENDERS.keySet()) {
			if (ente instanceof Criatura) {
				criaturasAEliminar.add(ente);
			}
		}

		// Fase 2: Remoción e inmunización estructural
		for (int i = 0; i < criaturasAEliminar.size(); i++) {
			this.eliminarEntidad(criaturasAEliminar.get(i));
		}
	}
}