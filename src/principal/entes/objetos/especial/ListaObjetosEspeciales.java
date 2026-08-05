package principal.entes.objetos.especial;

import java.awt.Color;
import java.util.HashMap;

import principal.entes.objetos.especial.modelos.ModeloCuadradoInvisible;

public abstract class ListaObjetosEspeciales {
	private static int idSiguiente = 1;

	private static int getSiguienteId() {
		return idSiguiente++;
	}

	// LISTAS DE MODELOS PARA CADA OBJETO ESPECIAL
	public static final HashMap<Integer, ModeloCuadradoInvisible> LISTA_CUADRADOS_INVISIBLES = new HashMap<Integer, ModeloCuadradoInvisible>();

	// CODIGO DE LOS MODELOS
	public static int COD_CUADRADO_INVISIBLE_X32 = getSiguienteId();

	// ASIGNACION DE CADA MODELO NUEVO A SU RESPECTIVA LISTA
	static {
		LISTA_CUADRADOS_INVISIBLES.put(COD_CUADRADO_INVISIBLE_X32, new ModeloCuadradoInvisible(32, true, new Color(255, 0, 0, 76)));
	}

	// GETTERS PARA CADA TIPO DE MODELO

	public static ModeloCuadradoInvisible getModeloCuadrado(final int cod) {
		return LISTA_CUADRADOS_INVISIBLES.get(cod);
	}

	private ListaObjetosEspeciales() {

	}

}
