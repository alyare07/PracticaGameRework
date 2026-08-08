package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;

import principal.mapa.Mundo;
import principal.mapa.Terreno;

public class MundoEditor extends Mundo{

    public MundoEditor(final Terreno terreno) {
	super(terreno);
    }

    @Override
    public void actualizar() {
	this.getTerreno().actualizarZonas(this.ZONAS, this.LADO_ZONEBOX);
	this.updateNextCodAct();
    }

    @Override
    public void pintar(final Graphics2D g) {
	this.getTerreno().pintarZonas(g, this.ZONAS, this.LADO_ZONEBOX);
	this.getTerreno().pintarZonas(g, this.ZONAS, this.LADO_ZONEBOX);
	this.updateNextCodPintado();
    }

}
