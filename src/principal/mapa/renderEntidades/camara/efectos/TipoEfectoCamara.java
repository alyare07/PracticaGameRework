package principal.mapa.renderEntidades.camara.efectos;

/**
 * Catálogo central de identificadores para todos los efectos cinemáticos,
 * ambientales y de combate soportados por el motor de cámara.
 * <p>
 * <b>¿Por qué usamos un Enum para indexar arreglos? (Explicación para
 * novatos):</b><br>
 * En Java, cada constante de un {@code enum} tiene asignado un número entero
 * interno correlativo llamado {@link Enum#ordinal()} (el primero vale 0, el
 * segundo 1, etc.). <br>
 * En lugar de usar un {@code HashMap<TipoEfectoCamara, EfectoCamara>} (que es
 * más lento, calcula códigos Hash y crea objetos en memoria), el gestor utiliza
 * un arreglo primitivo directo: {@code poolEfectos[tipo.ordinal()]}. <br>
 * Esto permite acceder a cualquier efecto en <b>tiempo constante $O(1)$
 * absoluto</b> y a velocidad de caché de CPU con <b>0 bytes de sobrecarga</b>.
 * </p>
 * 
 * @author Copiloto Técnico
 * @version 2.0
 */
public enum TipoEfectoCamara {

	// =========================================================================
	// === 1. EFECTOS BASE DE ENTORNO, AMBIENTE Y ESTADOS ALTERADOS
	// =========================================================================

	/**
	 * Sacudida caótica de alta frecuencia con decaimiento cuadrático (Trauma
	 * Shake).
	 * <p>
	 * <b>Uso:</b> Terremotos, sismos de terreno, derrumbes y explosiones lejanas.
	 * </p>
	 */
	TERREMOTO,

	/**
	 * Salto instantáneo de zoom con retorno elástico oscilatorio (Zoom Punch).
	 * <p>
	 * <b>Uso:</b> Pisotones de gigantes, caídas desde plataformas y golpes de suelo
	 * (Ground Slam).
	 * </p>
	 */
	PISOTON,

	/**
	 * Pulso rítmico doble ("Lub-Dub") que simula el ciclo cardíaco con sístole y
	 * diástole.
	 * <p>
	 * <b>Uso:</b> Poca vida (HP crítico < 20%), sigilo extremo y terror
	 * psicológico.
	 * </p>
	 */
	LATIDO,

	/**
	 * Bamboleo pendular angular suave combinado con respiración lenta de zoom y
	 * desvío de ejes.
	 * <p>
	 * <b>Uso:</b> Consumo de alcohol, pociones extrañas, venenos y alucinaciones.
	 * </p>
	 */
	BORRACHO,

	/**
	 * Zoom focal cerrado permanente (+12%) acompañado de micro-vibración nerviosa
	 * constante.
	 * <p>
	 * <b>Uso:</b> Modo furia, sobrecarga de adrenalina y potenciadores temporales
	 * de combate.
	 * </p>
	 */
	BERSERK,

	/**
	 * Empuje violento a lo largo de un vector direccional con resorte amortiguado
	 * de retorno.
	 * <p>
	 * <b>Uso:</b> Disparos de escopeta, cañonazos, retroceso de armas pesadas y
	 * placajes.
	 * </p>
	 */
	RETROCESO_DIRECCIONAL,

	/**
	 * Dilatación y contracción armónica ultra-lenta del zoom (~4.2 s por ciclo).
	 * <p>
	 * <b>Uso:</b> Descanso en fogatas, posadas, meditación, lectura de lore o
	 * sigilo en espera.
	 * </p>
	 */
	RESPIRACION,

	/**
	 * Deriva lateral continua con ráfagas turbulentas compuestas de 3 octavas
	 * armónicas.
	 * <p>
	 * <b>Uso:</b> Biomas nevados, ventiscas, tormentas de arena en desiertos y
	 * puentes ventosos.
	 * </p>
	 */
	VIENTO_TORMENTA,

	/**
	 * Trayectoria espacial en forma de 8 (Curva paramétrica de Lissajous 1:2) con
	 * cabeceo desfasado.
	 * <p>
	 * <b>Uso:</b> Aturdimientos (Stun), granadas cegadoras (Flashbang) y golpes en
	 * la cabeza.
	 * </p>
	 */
	ATURDIMIENTO,

	// =========================================================================
	// === 2. EFECTOS AVANZADOS DE COMBATE Y CINEMÁTICA
	// =========================================================================

	/**
	 * Micro-zoom súbito instantáneo con vibración de altísima frecuencia (60-120
	 * ms).
	 * <p>
	 * <b>Uso:</b> Impactos críticos cuerpo a cuerpo, contraataques perfectos
	 * (Parry) y remates a jefes.
	 * </p>
	 */
	IMPACTO_CRITICO,

	/**
	 * Expansión súbita hacia afuera (Zoom-Out) con rebote elástico oscilatorio.
	 * <p>
	 * <b>Uso:</b> Detonación de bombas de área, caída de meteoritos y novás mágicas
	 * expansivas.
	 * </p>
	 */
	ONDA_EXPANSIVA,

	/**
	 * Retraso inercial elástico de la cámara en sentido opuesto al vector de
	 * esquiva rápida.
	 * <p>
	 * <b>Uso:</b> Dashes rápidos, rodadas evasivas y teletransportes cortos (estilo
	 * <i>Hades</i>).
	 * </p>
	 */
	INERCIA_DASH,

	/**
	 * Balanceo pendular angular continuo combinado con sube y baja vertical
	 * desfasado de olas.
	 * <p>
	 * <b>Uso:</b> Viajes en barco en altamar, balsas sobre ríos rápidos y
	 * dirigibles aéreos.
	 * </p>
	 */
	BARCO_NAVEGACION,

	/**
	 * Salto instantáneo en el eje vertical (-Y hacia arriba) con micro-zoom seco y
	 * retorno rápido.
	 * <p>
	 * <b>Uso:</b> Emboscadas enemigas sorpresa, activación de trampas o detección
	 * en sigilo ("!").
	 * </p>
	 */
	ALERTA_SOBRESALTO,

	/**
	 * Zoom táctico constante (+20%) con inclinación fija cinematográfica (Dutch
	 * Angle de 1.5°).
	 * <p>
	 * <b>Uso:</b> Tensar cuerda de arco, modo francotirador, apuntado de magia o
	 * tiempo bala.
	 * </p>
	 */
	CAMARA_LENTA_ENFOQUE,

	/**
	 * Giro rotacional continuo acelerado (Ease-In cuadrático) con contracción
	 * progresiva de zoom.
	 * <p>
	 * <b>Uso:</b> Caída en precipicios o fosos sin fondo, vórtices dimensionales y
	 * derrota cinemática.
	 * </p>
	 */
	CAIDA_ABISMO
}