package principal.entes.criaturas.neutrales;

import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.inventario.Contenedor;
import principal.inventario.vault.InventarioVault;

public class Comerciante extends Criatura implements Contenedor {
	private final InventarioVault INVENTARIO;

	public Comerciante(final double x, final double y, final int ancho, final int alto, final double vidaMaxima) {
		super(x, y, ancho, alto, vidaMaxima, vidaMaxima);
		this.INVENTARIO = new InventarioVault(this, 8, 3, "Inventario Comerciante");
	}

	@Override
	public void actualizar() {
		super.actualizar();

	}

	@Override
	public void pintar(final Graphics2D g) {
		super.pintar(g);
	}

	@Override
	public void establecerMargenesSprite() {
		this.margenXInicialSprite = 10;
		this.margenYInicialSprite = 6;
		this.margenXFinalSprite = 9;
		this.margenYFinalSprite = 3;
	}

	@Override
	protected JSONObject exportarParaJSON() {
		return null;
	}

	@Override
	public String exportarTipoCriatura() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNombreContenedor() {
		// TODO Auto-generated method stub
		return "Comerciante";
	}

	@Override
	public InventarioVault getInventario() {
		return this.INVENTARIO;
	}

	@Override
	public Ente getEntePropietario() {
		return this;
	}

}
