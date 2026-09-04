package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;
import java.awt.Point;

import principal.mapa.Mundo;
import principal.mapa.Terreno;
import principal.mapa.escenario.Escenario;

public class MundoEditor extends Mundo {

	public MundoEditor(final Terreno terreno) {
		super(terreno);
	}

	public MundoEditor(final Escenario esc) {
		super(esc, new Point(0, 0));
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
}