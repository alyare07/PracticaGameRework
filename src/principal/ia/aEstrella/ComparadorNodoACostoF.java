package principal.ia.aEstrella;

import java.util.Comparator;

public class ComparadorNodoACostoF implements Comparator<NodoA>{

    @Override
    public int compare(final NodoA nodo1, final NodoA nodo2) {
	return Double.compare(nodo1.getCostoF(), nodo2.getCostoF());
    }

}
