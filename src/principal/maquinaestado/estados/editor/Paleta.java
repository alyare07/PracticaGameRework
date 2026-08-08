package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;
import principal.controles.Raton;
import principal.mapa.Tile;

public abstract class Paleta {
	public abstract int getLado();

	public abstract int getPosicionX();

	public abstract int getPosicionY();

	public abstract int getAncho();

	public abstract int getAlto();

	public abstract void pintar(final Graphics2D g);

	public abstract void actualizar(final Raton raton);

	public abstract boolean valoresYaEstalecidosPreviamente(final Tile tileEvaluar);

}
