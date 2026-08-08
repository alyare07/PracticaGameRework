package principal.mapa.renderEntidades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.mapa.Mundo;

public class MapRender{
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
	if (this.nextContadorGrupo() == this.LIMITE_POR_GRUPOS) {
	    if (this.codRenders < Long.MAX_VALUE) {
		this.codRenders++;
	    } else {
		this.codRenders = Long.MIN_VALUE + 1;
	    }
	}

	return this.codRenders;
    }

    private int nextContadorGrupo() {
	if (this.contadorGrupo <= this.LIMITE_POR_GRUPOS) {
	    return this.contadorGrupo++;
	}
	this.contadorGrupo = 0;
	return this.contadorGrupo;
    }

    public RenderEntidad getRender(final Ente e) {
	return this.DIRECCIONES.get(e.getCodRender()).get(e);
    }

    public RenderEntidad meterEntidad(final Ente e) {
	final long cod = this.getNextcodRenders();
	e.setCodRender(cod);
	final RenderEntidad render = new RenderEntidad(e, this.ESCENARIO);
	if (!this.DIRECCIONES.containsKey(cod)) {
	    this.DIRECCIONES.put(cod, new HashMap<Ente, RenderEntidad>());
	}
	this.DIRECCIONES.get(cod).put(e, render);
	return render;

    }

    public void meterEntidad(final RenderEntidad re) {
	final long cod = this.getNextcodRenders();

	re.getEntidad().setCodRender(cod);
	if (!this.DIRECCIONES.containsKey(cod)) {
	    this.DIRECCIONES.put(cod, new HashMap<Ente, RenderEntidad>());
	}
	this.DIRECCIONES.get(cod).put(re.getEntidad(), re);
	this.cantRenders++;

    }

    public void eliminarEntidad(final Ente e) {
	if (this.DIRECCIONES.containsKey(e.getCodRender()) && this.DIRECCIONES.get(e.getCodRender()).containsKey(e)) {
	    final RenderEntidad re = this.DIRECCIONES.get(e.getCodRender()).get(e);
	    re.limpiarZonas();
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
	for (final HashMap<Ente, RenderEntidad> direccion : this.DIRECCIONES.values()) {
	    for (final RenderEntidad re : direccion.values()) {
		entes.add(re.ENTIDAD);
	    }
	}
	return entes;
    }

    public void eliminarCriaturas() {
	final HashSet<Ente> lista = new HashSet<Ente>();
	for (final HashMap<Ente, RenderEntidad> direccion : this.DIRECCIONES.values()) {
	    for (final RenderEntidad re : direccion.values()) {
		if (re.ENTIDAD instanceof Criatura) {
		    lista.add(re.ENTIDAD);
		}
	    }
	}
	final ArrayList<Ente> entes = new ArrayList<Ente>(lista);
	for (int i = 0; i < entes.size(); i++) {
	    this.eliminarEntidad(entes.get(i));
	}
    }
}
