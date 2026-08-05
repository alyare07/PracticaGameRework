package principal.utilidades.funciones;

import java.util.HashMap;

import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.criaturas.neutrales.CosaNeutral;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.cofres.Cofre;
import principal.entes.objetos.cofres.CofreMediano;
import principal.entes.objetos.cofres.CofrePequeño;
import principal.entes.objetos.items.Consumible;
import principal.entes.objetos.items.Item;
import principal.entes.objetos.items.armas.distancia.fuego.Pistola;
import principal.mapa.Tile;
/**
 * Clase encargada de proporcionar los tipos de entes.
 * Haciendo mas practico definir en los json al importar o exportar.
 */
public class GestorTipoEnCarga {
	private final HashMap<String, String> TIPOS = new HashMap<String, String>();
	protected GestorTipoEnCarga() {
		this.llenar();
	}
	
	public String getTipo(final Class<?> c) {
		return this.TIPOS.get(c.getName());
	}
	
	
	private void llenar() {
		//Lista de generalizaciones
		this.TIPOS.put(Complemento.class.getName(), "complementos");
		this.TIPOS.put(Criatura.class.getName(), "criaturas");
		this.TIPOS.put(Item.class.getName(), "items");
		this.TIPOS.put(Tile.class.getName(), "terreno");
		this.TIPOS.put(Objeto.class.getName(), "objetos");
		//Fin lista
		this.TIPOS.put(CosaNeutral.class.getName(), "CosaNeutral");
		this.TIPOS.put(Enemigo.class.getName(), "Enemigo");
		this.TIPOS.put(Consumible.class.getName(), "Consumible");
		this.TIPOS.put(Pistola.class.getName(), "Pistola");
		this.TIPOS.put(Cofre.class.getName(), "Cofre");
		this.TIPOS.put(CofrePequeño.class.getName(), "Cofre Pequeño");
		this.TIPOS.put(CofreMediano.class.getName(), "Cofre Mediano");
	}
	
}
