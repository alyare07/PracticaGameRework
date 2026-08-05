package principal.maquinaestado.estados.editor;

import java.awt.Graphics2D;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import principal.entes.Ente;
import principal.entes.criaturas.Criatura;
import principal.entes.criaturas.enemigos.Enemigo;
import principal.entes.criaturas.neutrales.CosaNeutral;
import principal.entes.objetos.Complemento;
import principal.entes.objetos.Objeto;
import principal.entes.objetos.items.Item;
import principal.mapa.Mapa;
import principal.mapa.Mundo;
import principal.utilidades.Constantes;

public class MundoEditor extends Mundo {

	public MundoEditor(final Mapa mapa) {
		super(mapa);
	}
	
	
	public void actualizar() {
		this.getMapa().actualizarZonas(ZONAS, LADO_ZONEBOX);
		this.updateNextCodAct();
	}

	public void pintar(final Graphics2D g) {
		this.getMapa().pintarZonas(g, ZONAS, LADO_ZONEBOX);
		this.getMapa().pintarZonas(g, ZONAS, LADO_ZONEBOX);		
		this.updateNextCodPintado();
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getEntesInJson() {
		JSONObject listas = new JSONObject();
		JSONArray listaComplementos = new JSONArray();
		JSONArray listaCriaturas = new JSONArray();
		JSONArray listaItems = new JSONArray();
		JSONArray listaObjetos = new JSONArray();
		Complemento complemento = null;
		CosaNeutral cosaNeutral = null;
		Enemigo enemigo = null;
		Item item = null;
		JSONObject jsonAux = null;
		for(Ente e : this.getEntes()) {
			if(e instanceof Criatura) {
				if(e instanceof Enemigo) {
					enemigo = (Enemigo)e;
					jsonAux = new JSONObject();
					jsonAux.put("tipo", enemigo.exportarTipoCriatura());
					jsonAux.put("entiti", enemigo.exportarParaJSON());
					listaCriaturas.add(jsonAux);
				}else if(e instanceof CosaNeutral) {
					cosaNeutral = (CosaNeutral)e;
					jsonAux = new JSONObject();
					jsonAux.put("tipo", cosaNeutral.exportarTipoCriatura());
					jsonAux.put("entiti", cosaNeutral.exportarParaJSON());
					listaCriaturas.add(jsonAux);
				}
			}else if(e instanceof Complemento) {
				complemento = (Complemento) e;
				listaComplementos.add(complemento.exportarParaJSON());
			}else if(e instanceof Objeto) {
				if(e instanceof Item) {
					item = (Item)e;
					listaItems.add(item.getJsonItem());
				}else {
					/*
					 * COMPLETAR EL CODIGO PARA EXPORTAR LOS DEMAS TIPOS DE OBJETOS QUE NO SEAN ITEMS, COMO EL COFRE POR EJEMPLO
					 */
				}
			}
		}
		listas.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Complemento.class), listaComplementos);
		listas.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Criatura.class), listaCriaturas);
		listas.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Item.class), listaItems);
		listas.put(Constantes.FUNCIONES.GESTOR_TIPOS_EN_CARGA.getTipo(Objeto.class), listaObjetos);
		return listas;
	}
	
	

}
