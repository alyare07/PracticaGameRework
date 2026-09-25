package principal.persistencia.json;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.mapa.Mundo;
import principal.persistencia.json.adaptadores.AdaptadoresContenedoresYRecursos;
import principal.persistencia.json.adaptadores.AdaptadoresCriaturas;
import principal.persistencia.json.adaptadores.AdaptadoresEstructuras;
import principal.persistencia.json.adaptadores.AdaptadoresItems;

/**
 * Service Locator y Registro Maestro de Adaptadores de Entidades. Centraliza la
 * serialización y deserialización polimórfica en O(1).
 */
public final class RegistroEntidades {

	public static final String CLAVE_TIPO = "tipo";
	public static final String CLAVE_DATOS = "datos";

	private static final Map<String, AdaptadorEntidad<?>> ADAPTADORES_POR_ID = new HashMap<String, AdaptadorEntidad<?>>();
	private static final Map<Class<?>, AdaptadorEntidad<?>> ADAPTADORES_POR_CLASE = new HashMap<Class<?>, AdaptadorEntidad<?>>();

	private static boolean inicializado = false;

	private RegistroEntidades() {
	}

	public static <T extends Ente> void registrar(final AdaptadorEntidad<T> adaptador) {
		if (adaptador == null) {
			return;
		}
		ADAPTADORES_POR_ID.put(adaptador.getId(), adaptador);
		ADAPTADORES_POR_CLASE.put(adaptador.getClaseEntidad(), adaptador);
	}

	public static synchronized void inicializar() {
		if (inicializado) {
			return;
		}
		inicializado = true;

		// 1. CRIATURAS
		registrar(new AdaptadoresCriaturas.AdaptadorJugador());
		registrar(new AdaptadoresCriaturas.AdaptadorBandidoPistolero());
		registrar(new AdaptadoresCriaturas.AdaptadorBandidoGarrote());
		registrar(new AdaptadoresCriaturas.AdaptadorBandidoGranadero());
		registrar(new AdaptadoresCriaturas.AdaptadorGallina());
		registrar(new AdaptadoresCriaturas.AdaptadorMascota());
		registrar(new AdaptadoresCriaturas.AdaptadorComerciante());

		// 2. ESTRUCTURAS, COMPLEMENTOS Y FABRICABLES
		registrar(new AdaptadoresEstructuras.AdaptadorComplemento());
		registrar(new AdaptadoresEstructuras.AdaptadorCuadradoInvisible());
		registrar(new AdaptadoresEstructuras.AdaptadorFogata());
		registrar(new AdaptadoresEstructuras.AdaptadorCarpa());
		registrar(new AdaptadoresEstructuras.AdaptadorCama());
		registrar(new AdaptadoresEstructuras.AdaptadorEntradaCueva());
		registrar(new AdaptadoresEstructuras.AdaptadorEdificio());
		registrar(new AdaptadoresEstructuras.AdaptadorZonaTP());

		// 3. CONTENEDORES Y RECURSOS
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorCofrePequeno());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorCofreMediano());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorArbolCofre());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorArbolCosechable());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorArbustoCosechable());

		// 4. MINERALES ESPECIALIZADOS
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralRoca());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralCobre());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralHierro());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralOro());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralCarbon());
		registrar(new AdaptadoresContenedoresYRecursos.AdaptadorMineralCristal());

		// 5. ÍTEMS
		registrar(new AdaptadoresItems.AdaptadorItemMoneda());
		registrar(new AdaptadoresItems.AdaptadorPistola());
		registrar(new AdaptadoresItems.AdaptadorEscopetaRecortada());
		registrar(new AdaptadoresItems.AdaptadorEscopetaTactica());
		registrar(new AdaptadoresItems.AdaptadorEscopetaAutomatica());
		registrar(new AdaptadoresItems.AdaptadorSubfusilLigero());
		registrar(new AdaptadoresItems.AdaptadorRifleAsalto());
		registrar(new AdaptadoresItems.AdaptadorAmetralladoraPesada());
		registrar(new AdaptadoresItems.AdaptadorHerramienta());
		registrar(new AdaptadoresItems.AdaptadorPiezaEquipo());
		registrar(new AdaptadoresItems.AdaptadorAntorcha());
		registrar(new AdaptadoresItems.AdaptadorGranadaT1());
		registrar(new AdaptadoresItems.AdaptadorPocionVidaMenor());
		registrar(new AdaptadoresItems.AdaptadorCajaMunicion());
		registrar(new AdaptadoresItems.AdaptadorRecursoMaterial());
		registrar(new AdaptadoresItems.AdaptadorBayaSilvestre());
		registrar(new AdaptadoresItems.AdaptadorCarnePolloCruda());
		registrar(new AdaptadoresItems.AdaptadorCarnePolloCocida());
		registrar(new AdaptadoresItems.AdaptadorCuencoVacio());
		registrar(new AdaptadoresItems.AdaptadorCuencoAguaSucia());
		registrar(new AdaptadoresItems.AdaptadorCuencoAguaHervida());
		registrar(new AdaptadoresItems.AdaptadorKitFogata());
		registrar(new AdaptadoresItems.AdaptadorKitFogataAzul());
		registrar(new AdaptadoresItems.AdaptadorKitCarpa());
		registrar(new AdaptadoresItems.AdaptadorKitCama());
	}

	public static boolean soporta(final Ente entidad) {
		if (entidad == null) {
			return false;
		}
		asegurarInicializacion();
		return ADAPTADORES_POR_CLASE.containsKey(entidad.getClass());
	}

	@SuppressWarnings("unchecked")
	public static JSONObject exportar(final Ente entidad) {
		if (entidad == null) {
			return null;
		}
		asegurarInicializacion();

		final AdaptadorEntidad adaptador = ADAPTADORES_POR_CLASE.get(entidad.getClass());
		if (adaptador == null) {
			System.err.println("[RegistroEntidades] Advertencia: No existe adaptador para la clase: "
					+ entidad.getClass().getName());
			return null;
		}

		final JSONObject datos = adaptador.serializar(entidad);
		final JSONObject sobre = new JSONObject();
		sobre.put(CLAVE_TIPO, adaptador.getId());
		sobre.put(CLAVE_DATOS, datos);
		return sobre;
	}

	@SuppressWarnings("unchecked")
	public static Ente importar(final JSONObject sobre, final Mundo mundo) {
		if (sobre == null) {
			return null;
		}
		asegurarInicializacion();

		final String tipoId = LectorJSON.getString(sobre, CLAVE_TIPO, null);
		if (tipoId == null) {
			System.err.println("[RegistroEntidades] Error: Sobre JSON no contiene campo '" + CLAVE_TIPO + "'.");
			return null;
		}

		final AdaptadorEntidad adaptador = ADAPTADORES_POR_ID.get(tipoId);
		if (adaptador == null) {
			System.err.println("[RegistroEntidades] Error: Tipo de entidad no reconocido en registro: " + tipoId);
			return null;
		}

		final JSONObject datos = LectorJSON.getObjeto(sobre, CLAVE_DATOS);
		final JSONObject payload = (datos != null) ? datos : sobre;

		return adaptador.deserializar(payload, mundo);
	}

	private static void asegurarInicializacion() {
		if (!inicializado) {
			inicializar();
		}
	}
}