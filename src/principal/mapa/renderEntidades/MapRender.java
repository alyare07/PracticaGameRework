package principal.mapa.renderEntidades;

import java.util.HashMap;
import java.util.HashSet;

import principal.entes.Ente;
import principal.mapa.Mundo;


public class MapRender {
	private long codRenders = Long.MIN_VALUE;
	private final HashMap<Long, HashMap<Ente, RenderEntidad>> DIRECCIONES;
	private int contadorGrupo;
	private final int LIMITE_POR_GRUPOS = 200;
	private final Mundo ESCENARIO;
	private long cantRenders;

	public MapRender(final Mundo esc) {
		this.DIRECCIONES = new HashMap<Long, HashMap<Ente, RenderEntidad>>();
		this.ESCENARIO = esc;
		this.cantRenders = 0;
	}

	public long getNextcodRenders() {
		if (nextContadorGrupo() == LIMITE_POR_GRUPOS) {
			if (this.codRenders < Long.MAX_VALUE) {
				this.codRenders++;
			} else {
				this.codRenders = Long.MIN_VALUE + 1;
			}
		}

		return this.codRenders;
	}

	private int nextContadorGrupo() {
		if (contadorGrupo <= LIMITE_POR_GRUPOS) {
			return contadorGrupo++;
		}
		contadorGrupo = 0;
		return contadorGrupo;
	}

	public RenderEntidad getRender(final Ente e) {
		return this.DIRECCIONES.get(e.getCodRender()).get(e);
	}

	public RenderEntidad meterEntidad(final Ente e) {
		long cod = this.getNextcodRenders();
		e.setCodRender(cod);
		RenderEntidad render = new RenderEntidad(e, ESCENARIO);
		if (!this.DIRECCIONES.containsKey(cod)) {
			this.DIRECCIONES.put(cod, new HashMap<Ente, RenderEntidad>());
		}
		this.DIRECCIONES.get(cod).put(e, render);
		return render;

	}

	public void meterEntidad(final RenderEntidad re) {
		long cod = this.getNextcodRenders();

		re.getEntidad().setCodRender(cod);
		if (!this.DIRECCIONES.containsKey(cod)) {
			this.DIRECCIONES.put(cod, new HashMap<Ente, RenderEntidad>());
		}
		this.DIRECCIONES.get(cod).put(re.getEntidad(), re);
		this.cantRenders++;

	}

	public void eliminarEntidad(final Ente e) {
		if (this.DIRECCIONES.containsKey(e.getCodRender()) && this.DIRECCIONES.get(e.getCodRender()).containsKey(e)) {
			this.DIRECCIONES.get(e.getCodRender()).remove(e);
			this.cantRenders--;
		}
	}

	// revisar
	public long getCantEntidades() {
		return this.cantRenders;
	}

	public boolean containsKey(final Ente e) {
		return this.DIRECCIONES.containsKey(e.getCodRender()) && this.DIRECCIONES.get(e.getCodRender()).containsKey(e);
	}
	
	public HashSet<Ente> getEntes() {
		final HashSet<Ente> entes = new HashSet<Ente>();
		for(HashMap<Ente, RenderEntidad> direccion : this.DIRECCIONES.values()) {
			for(RenderEntidad re : direccion.values()) {
				entes.add(re.ENTIDAD);
			}
		}
		return entes;
	}
}
