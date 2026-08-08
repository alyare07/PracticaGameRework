package principal.mapa.renderEntidades;

import java.util.ArrayList;

import principal.entes.Ente;
import principal.mapa.Mundo;

public class RenderEntidad{
    protected Mundo mundo;
    protected ArrayList<ZoneBox> lista;
    protected final Ente ENTIDAD;
    protected int codAct;
    protected int codPaint;

    public RenderEntidad(final Ente e, final Mundo mundo) {
	this.ENTIDAD = e;
	this.mundo = mundo;
	this.lista = new ArrayList<ZoneBox>();
    }

    public void renderizado() {
	this.codAct = this.mundo.getCodAct();
    }

    public boolean estaRenderizado() {
	return this.codAct == this.mundo.getCodAct();
    }

    public boolean estaPintado() {
	return this.codPaint == this.mundo.getCodPintado();
//		return this.codPaint == this.mundo.getCodAct();

    }

    public void pintado() {
	this.codPaint = this.mundo.getCodPintado();
//		this.codPaint = this.mundo.getCodAct();
    }

    private void verificarZoneBox() {
	// eliminamos las zonebox que no contengan mas a la entidad

	for (int pos = 0; pos < this.lista.size(); pos++) {
	    if (!this.lista.get(pos).getArea().intersects(this.ENTIDAD.getArea())) {
		this.lista.get(pos).eliminarEntidad(this.ENTIDAD);
		this.lista.remove(pos);
	    }
	}

	this.mundo.getZonasIntersectadas(this.ENTIDAD).forEach(zb -> {
	    if (!this.contieneZona(zb)) {
		this.lista.add(zb);
		zb.addEntidad(this.ENTIDAD);
	    }
	});

	if (this.lista.size() == 0) {
	    this.ENTIDAD.eliminar();
	    this.eliminarEntidad();
	    System.out.println("Sin mapa, se ha eliminado entidad " + this.ENTIDAD + " / rest: " + this.mundo.getRenders().getCantEntidades());
	}
    }

    public void update() {
	if (this.ENTIDAD.estaEliminado()) {
	    this.eliminarEntidad();
	    return;
	}
	this.verificarZoneBox();
    }

    private void eliminarEntidad() {
	this.limpiarZonas();
	this.mundo.getRenders().eliminarEntidad(this.ENTIDAD);
    }

    public void meterZoneBox(final ZoneBox zona) {
	this.lista.add(zona);
    }

    public boolean contieneZona(final ZoneBox zona) {
	return this.lista.contains(zona);
    }

    protected void limpiarZonas() {
	for (final ZoneBox z : this.lista) {
	    z.eliminarEntidad(this.ENTIDAD);
	}
	this.lista.clear();
    }

    public Ente getEntidad() {
	return this.ENTIDAD;
    }

}
