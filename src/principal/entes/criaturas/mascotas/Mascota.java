package principal.entes.criaturas.mascotas;

import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import principal.animaciones.criaturas.AnimacionesComerciante;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.Jugador;
import principal.entes.facciones.GestorFacciones;
import principal.ia.arbol.FabricaArbolesIA;
import principal.igu.textos.TipoTextoFlotante;
import principal.interaccion.Interactuable;
import principal.utilidades.Globales;
import principal.utilidades.audio.sonido.GestorSonido;
import principal.utilidades.audio.sonido.IDSonido;

/**
 * Mascota acompañante con soporte de Behavior Tree y navegación por huellas
 * seguras y Flowfield (Zero-GC).
 * 
 * @version 3.1 (Vanilla Java 8 - Standard 8x8 Footprint & Flowfield
 *          Integration)
 */
public class Mascota extends Criatura implements Interactuable {

	private final String nombre;
	private final AnimacionesComerciante ANIMACION;
	private boolean siguiendo = true;

	public Mascota(final double x, final double y, final String nombre, final double vidaMaxima) {
		super(x, y, 12, 16, vidaMaxima, vidaMaxima, 0.95);

		this.nombre = (nombre != null) ? nombre : "Compañero";
		this.setFaccion(GestorFacciones.FACCION_JUGADOR);

		this.ANIMACION = new AnimacionesComerciante();
		this.direccion = Direccion.SUR;
		this.setEstadoUnico(Estado.ESTANDAR);

		// Footprint unificado a 8x8 para coincidir exactamente con el paso del jugador
		this.configurarFootprint(8, 8, 0);

		// Asignación del árbol cognitivo de acompañante
		this.arbolComportamiento = FabricaArbolesIA.ARBOL_MASCOTA_ACOMPANANTE;
		this.blackboard.setSiguiendoLider(this.siguiendo);
	}

	public Mascota(final double x, final double y) {
		this(x, y, "Compañero", 80.0);
	}

	@Override
	public void actualizar() {
		super.actualizar();
		if (this.eliminado) {
			return;
		}

		this.actualizarAnimacion();
		this.actualizarOclusion();
	}

	private void actualizarAnimacion() {
		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;
		this.ANIMACION.actualizar(this.direccion, tipoAnim);
	}

	private void actualizarOclusion() {
		if (this.mundo != null) {
			this.atrasDeComplemento = this.mundo.colisionaConObjetoSolidoPeroEnZonaNoSolida(this.getArea());
		}
	}

	@Override
	public String getTextoPrompt() {
		return this.siguiendo ? "Ordenar esperar" : "Ordenar seguirme";
	}

	@Override
	public void interactuar(final Jugador jugador) {
		if (jugador == null) {
			return;
		}

		this.setSiguiendo(!this.siguiendo);
		this.setDireccionMirandoCriatura(jugador);

		GestorSonido.reproducir(IDSonido.SELECT);

		final String msg = this.siguiendo ? "¡" + this.nombre + " te sigue!" : "¡" + this.nombre + " se queda aquí!";
		Globales.GESTOR_TEXTOS.agregarTexto(msg, this.getCentroX(), this.getPosicionYInt() - 8,
				TipoTextoFlotante.ORO_EXP);
	}

	@Override
	public boolean puedeInteractuar(final Jugador jugador) {
		return !this.estaEliminado();
	}

	@Override
	public void pintar(final Graphics2D g) {
		final int drawX = this.getPosicionXIntDibujado();
		final int drawY = this.getPosicionYIntDibujado();
		final boolean flash = this.estaEnFlashDanio();
		final int tipoAnim = this.estaEstadoCaminando() ? AnimacionesComerciante.CAMINANDO
				: AnimacionesComerciante.ESTANDAR;

		this.ANIMACION.pintar(g, drawX, drawY, this.direccion, tipoAnim, this.atrasDeComplemento, true, flash);
		super.pintar(g);
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONObject exportarParaJSON() {
		final JSONObject json = new JSONObject();
		json.put("x", Integer.valueOf(this.getPosicionXInt()));
		json.put("y", Integer.valueOf(this.getPosicionYInt()));
		json.put("nombre", this.nombre);
		json.put("vida", Double.valueOf(this.vida));
		json.put("vidaMaxima", Double.valueOf(this.vidaMaxima));
		json.put("siguiendo", Boolean.valueOf(this.siguiendo));
		return json;
	}

	public static Mascota crearDesdeJSON(final JSONObject json) {
		if (json == null) {
			return new Mascota(0, 0);
		}

		final int x = (json.get("x") != null) ? ((Number) json.get("x")).intValue() : 0;
		final int y = (json.get("y") != null) ? ((Number) json.get("y")).intValue() : 0;
		final String nombre = (json.get("nombre") != null) ? json.get("nombre").toString() : "Compañero";
		final double vidaMax = (json.get("vidaMaxima") != null) ? ((Number) json.get("vidaMaxima")).doubleValue()
				: 80.0;

		final Mascota mascota = new Mascota(x, y, nombre, vidaMax);

		if (json.get("vida") != null) {
			mascota.establecerVida(((Number) json.get("vida")).doubleValue());
		}
		if (json.get("siguiendo") != null) {
			mascota.setSiguiendo(Boolean.parseBoolean(json.get("siguiendo").toString()));
		}

		return mascota;
	}

	@Override
	public String exportarTipoCriatura() {
		return "Mascota";
	}

	@Override
	public String getNombre() {
		return this.nombre;
	}

	public boolean isSiguiendo() {
		return this.siguiendo;
	}

	public void setSiguiendo(final boolean siguiendo) {
		this.siguiendo = siguiendo;
		this.blackboard.setSiguiendoLider(siguiendo);
		if (!siguiendo) {
			this.setEstadoEstandar();
		}
	}
}