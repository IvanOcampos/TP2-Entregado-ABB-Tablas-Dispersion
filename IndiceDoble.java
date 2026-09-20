/** Combina orden por ABB y acceso exacto por hash, compartiendo los mismos objetos Nodo. */
public class IndiceDoble<K extends Comparable<? super K>, V> {
    // Ambas estructuras guardan las mismas referencias a Nodo, no copias de los valores.
    private final ABBAumentado<K,V> arbol = new ABBAumentado<K,V>();
    private final TablaEncadenada<K,ABBAumentado.Nodo<K,V>> tabla = new TablaEncadenada<K,ABBAumentado.Nodo<K,V>>();
    public void agregar(K clave, V valor) {
        arbol.agregar(clave, valor);
        // Si la clave ya existía, se registra igualmente el nodo cuyo valor acaba de actualizarse.
        tabla.insertar(clave, arbol.nodoDeSinContar(clave));
    }

    public V obtener(K clave) throws ClaveInexistenteException {
        ABBAumentado.Nodo<K, V> nodo = tabla.obtener(clave);
        if (nodo == null) {
            throw new ClaveInexistenteException("Clave inexistente: " + clave);
        }
        return nodo.valor();
    }

    public V eliminar(K clave) throws ClaveInexistenteException {
        // Primero valida y elimina en el árbol; luego retira la entrada de acceso directo.
        V valor = arbol.eliminar(clave);
        tabla.eliminar(clave);
        return valor;
    }

    public boolean contiene(K clave) {
        return tabla.obtener(clave) != null;
    }

    public K kEsimo(int k) {
        // Las consultas que dependen del orden se delegan al ABB aumentado.
        return arbol.kEsimo(k);
    }

    public int consultarRango(K a, K b) {
        return arbol.consultarRango(a, b);
    }

    public int size() {
        return arbol.size();
    }

    public ABBAumentado<K, V> arbol() {
        return arbol;
    }

    public TablaEncadenada<K, ABBAumentado.Nodo<K, V>> tabla() {
        return tabla;
    }
}
