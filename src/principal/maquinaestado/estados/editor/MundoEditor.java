package principal.maquinaestado.estados.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.iluminacion.FuenteLuz;
import principal.iluminacion.TipoLuz;
import principal.iluminacion.ZonaAmbiente;
import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;
import principal.mapa.escenario.tps.PuertaArea;
import principal.mapa.escenario.tps.PuertaMapa;
import principal.mapa.escenario.tps.PuertaMundo;
import principal.mapa.escenario.tps.ZonaTP;
import principal.utilidades.Globales;
import principal.utilidades.Render2D;

/**
 * Especialización de Mundo para el entorno de edición. Permite registrar,
 * manipular, inspeccionar con 'E' y eliminar Triggers, Volúmenes de Ambiente y
 * Luces Estáticas en tiempo real.
 * 
 * @version 3.0 (Vanilla Java 8)
 */
public class MundoEditor extends Mundo {

	private final ArrayList<ZonaTP> triggersEditor = new ArrayList<ZonaTP>();
	private final ArrayList<ZonaAmbiente> zonasAmbienteEditor = new ArrayList<ZonaAmbiente>();
	private final ArrayList<FuenteLuz> lucesEstaticasEditor = new ArrayList<FuenteLuz>();

	private static final Font FUENTE_DEBUG = new Font(Font.SANS_SERIF, Font.BOLD, 6);

	public MundoEditor(final Terreno terreno) {
		super(terreno);
	}

	public MundoEditor(final Escenario esc) {
		super(esc, new Point(0, 0));
		if (esc != null) {
			esc.generarTriggers(this);
		}
	}

	@Override
	public void actualizar() {
		this.actualizarZonas();
		this.updateNextCodAct();
	}

	@Override
	public void pintar(final Graphics2D g) {
		this.pintarZonas(g);
		this.updateNextCodPintado();
	}

	/**
	 * Dibuja los elementos específicos de triggers y ambientes en el editor.
	 */
	public void pintarTriggersYAmbientes(final Graphics2D g) {
		final Font fontPrevia = g.getFont();
		g.setFont(FUENTE_DEBUG);

		// 1. Zonas de Ambiente / Biomas
		for (int i = 0; i < this.zonasAmbienteEditor.size(); i++) {
			final ZonaAmbiente z = this.zonasAmbienteEditor.get(i);
			final Rectangle lim = z.getLimites();
			final Color c = z.getColorAmbiente();

			Render2D.dibujarRectanguloRellenoRefCamara(g, lim, new Color(c.getRed(), c.getGreen(), c.getBlue(), 65));
			Render2D.dibujarRectanguloContornoRefCamara(g, lim, new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));

			final String label = "[ZONA: " + z.getNombre() + (z.isEsInterior() ? " (INT)]" : "]");
			Render2D.dibujarStringConSombraRefCamara(g, label, lim.x + 4, lim.y + 10, Color.WHITE, Color.BLACK);
		}

		// 2. Luces Estáticas
		for (int i = 0; i < this.lucesEstaticasEditor.size(); i++) {
			final FuenteLuz luz = this.lucesEstaticasEditor.get(i);
			final int x = (int) Math.round(luz.getPosX());
			final int y = (int) Math.round(luz.getPosY());
			final int r = (int) Math.round(luz.getRadioActual());

			Render2D.dibujarFiguraEllipseRefCamara(g, x - r, y - r, r * 2, r * 2, new Color(255, 180, 50, 90));
			Render2D.dibujarRectanguloRellenoRefCamara(g, x - 2, y - 2, 4, 4, Color.YELLOW);
			Render2D.dibujarStringConSombraRefCamara(g, "LUZ (" + r + "px)", x + 4, y + 2, Color.YELLOW, Color.BLACK);
		}

		g.setFont(fontPrevia);
	}

	// =========================================================================
	// REGISTRO Y GESTIÓN DE ELEMENTOS EN EDITOR
	// =========================================================================

	public void agregarTrigger(final ZonaTP trigger) {
		if ((trigger != null) && !this.triggersEditor.contains(trigger)) {
			this.triggersEditor.add(trigger);
			this.meterEntidad(trigger);
		}
	}

	public void agregarZonaAmbiente(final ZonaAmbiente zona) {
		if ((zona != null) && !this.zonasAmbienteEditor.contains(zona)) {
			this.zonasAmbienteEditor.add(zona);
			if (Globales.GESTOR_ZONAS_AMBIENTE != null) {
				Globales.GESTOR_ZONAS_AMBIENTE.registrarZona(zona);
			}
		}
	}

	public void agregarLuzEstatica(final FuenteLuz luz) {
		if ((luz != null) && !this.lucesEstaticasEditor.contains(luz)) {
			this.lucesEstaticasEditor.add(luz);
		}
	}

	public void eliminarTrigger(final ZonaTP trigger) {
		if (trigger != null) {
			this.triggersEditor.remove(trigger);
			this.eliminarEntidad(trigger);
		}
	}

	public void eliminarZonaAmbiente(final ZonaAmbiente zona) {
		if (zona != null) {
			this.zonasAmbienteEditor.remove(zona);
		}
	}

	public void eliminarLuzEstatica(final FuenteLuz luz) {
		if (luz != null) {
			this.lucesEstaticasEditor.remove(luz);
			luz.apagar();
		}
	}

	/**
	 * Borra el trigger, zona o luz ubicado en las coordenadas especificadas con
	 * tolerancia de 16px.
	 */
	public Object eliminarTriggerOAmbienteEn(final int x, final int y, final int radio) {
		final int radioTolerancia = Math.max(16, radio);
		final Rectangle r = new Rectangle(x - radioTolerancia, y - radioTolerancia, radioTolerancia * 2,
				radioTolerancia * 2);

		// 1. Triggers TP
		for (int i = this.triggersEditor.size() - 1; i >= 0; i--) {
			final ZonaTP tp = this.triggersEditor.get(i);
			if (tp.getArea().intersects(r)) {
				this.eliminarTrigger(tp);
				return tp;
			}
		}

		// 2. Zonas de Ambiente
		for (int i = this.zonasAmbienteEditor.size() - 1; i >= 0; i--) {
			final ZonaAmbiente z = this.zonasAmbienteEditor.get(i);
			if (z.getLimites().intersects(r)) {
				this.eliminarZonaAmbiente(z);
				return z;
			}
		}

		// 3. Luces Estáticas (Tolerancia ampliada a 16px)
		for (int i = this.lucesEstaticasEditor.size() - 1; i >= 0; i--) {
			final FuenteLuz luz = this.lucesEstaticasEditor.get(i);
			final double dx = x - luz.getPosX();
			final double dy = y - luz.getPosY();
			if (((dx * dx) + (dy * dy)) <= (radioTolerancia * radioTolerancia)) {
				this.eliminarLuzEstatica(luz);
				return luz;
			}
		}

		return null;
	}

	/**
	 * Obtiene la fuente de luz ubicada en las coordenadas dadas (utilizado por el
	 * inspector 'E').
	 */
	public FuenteLuz getLuzEn(final int x, final int y, final int radioTolerancia) {
		for (int i = 0; i < this.lucesEstaticasEditor.size(); i++) {
			final FuenteLuz luz = this.lucesEstaticasEditor.get(i);
			final double dx = x - luz.getPosX();
			final double dy = y - luz.getPosY();
			if (((dx * dx) + (dy * dy)) <= (radioTolerancia * radioTolerancia)) {
				return luz;
			}
		}
		return null;
	}

	public ArrayList<ZonaTP> getTriggersEditor() {
		return this.triggersEditor;
	}

	public ArrayList<ZonaAmbiente> getZonasAmbienteEditor() {
		return this.zonasAmbienteEditor;
	}

	public ArrayList<FuenteLuz> getLucesEstaticasEditor() {
		return this.lucesEstaticasEditor;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getTriggersEnJson() {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < this.triggersEditor.size(); i++) {
			final ZonaTP tp = this.triggersEditor.get(i);
			final JSONObject jo = new JSONObject();
			jo.put("x", Integer.valueOf(tp.getPosicionXInt()));
			jo.put("y", Integer.valueOf(tp.getPosicionYInt()));
			jo.put("w", Integer.valueOf(tp.getAncho()));
			jo.put("h", Integer.valueOf(tp.getAlto()));
			if (tp.getPuertaTP() instanceof PuertaMapa) {
				jo.put("tipo", "PuertaMapa");
				jo.put("mapa", ((PuertaMapa) tp.getPuertaTP()).getRutaMapaDestino());
				jo.put("mundo", ((PuertaMapa) tp.getPuertaTP()).getNombreMundoDestino());
				jo.put("spawn", ((PuertaMapa) tp.getPuertaTP()).getNombreSpawnDelMundoDestino());

			} else if (tp.getPuertaTP() instanceof PuertaMundo) {
				jo.put("tipo", "PuertaMundo");
				jo.put("mundo", ((PuertaMundo) tp.getPuertaTP()).getNombreMundoDestino());
				jo.put("spawn", ((PuertaMundo) tp.getPuertaTP()).getNombreSpawnDestino());

			} else if (tp.getPuertaTP() instanceof PuertaArea) { // puerta area
				jo.put("tipo", "PuertaArea");
				jo.put("destX", ((PuertaArea) tp.getPuertaTP()).getXDestino());
				jo.put("destY", ((PuertaArea) tp.getPuertaTP()).getYDestino());
				jo.put("destW", ((PuertaArea) tp.getPuertaTP()).getWDestino());
				jo.put("destH", ((PuertaArea) tp.getPuertaTP()).getHDestino());

			}
			lista.add(jo);
		}
		return lista;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getZonasAmbienteEnJson() {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < this.zonasAmbienteEditor.size(); i++) {
			final ZonaAmbiente z = this.zonasAmbienteEditor.get(i);
			final JSONObject jo = new JSONObject();
			final Rectangle r = z.getLimites();
			jo.put("x", Integer.valueOf(r.x));
			jo.put("y", Integer.valueOf(r.y));
			jo.put("w", Integer.valueOf(r.width));
			jo.put("h", Integer.valueOf(r.height));
			jo.put("nombre", z.getNombre());
			jo.put("interior", Boolean.valueOf(z.isEsInterior()));
			jo.put("niebla", z.getNivelNiebla().name());
			jo.put("r", Integer.valueOf(z.getColorAmbiente().getRed()));
			jo.put("g", Integer.valueOf(z.getColorAmbiente().getGreen()));
			jo.put("b", Integer.valueOf(z.getColorAmbiente().getBlue()));
			jo.put("a", Integer.valueOf(z.getColorAmbiente().getAlpha()));
			lista.add(jo);
		}
		return lista;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getLucesEnJson() {
		final JSONArray lista = new JSONArray();
		for (int i = 0; i < this.lucesEstaticasEditor.size(); i++) {
			final FuenteLuz luz = this.lucesEstaticasEditor.get(i);
			final JSONObject jo = new JSONObject();
			jo.put("x", Double.valueOf(luz.getPosX()));
			jo.put("y", Double.valueOf(luz.getPosY()));
			jo.put("radio", Double.valueOf(luz.getRadioActual()));
			jo.put("tipo", (luz.getTipo() != null ? luz.getTipo().name() : TipoLuz.ANTORCHA.name()));
			lista.add(jo);
		}
		return lista;
	}
}