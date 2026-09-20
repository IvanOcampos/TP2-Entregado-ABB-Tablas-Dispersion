import java.util.Iterator;

/**
 * ABB genérico aumentado: cada nodo almacena el tamaño de su subárbol para
 * resolver consultas de orden y rango sin recorrer todos los elementos.
 */
public class ABBAumentado<K extends Comparable<? super K>, V> implements Iterable<K> {
    /** Nodo público para permitir que el índice hash mantenga la misma referencia. */
    public static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> izq;
        Nodo<K, V> der;
        Nodo<K, V> padre;
        int tamano;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.tamano = 1;
        }

        public K clave() {
            return clave;
        }

        public V valor() {
            return valor;
        }

        public int tamano() {
            return tamano;
        }
    }

    private Nodo<K, V> raiz;
    private long visitas;

    private int tamano(Nodo<K, V> n) {
        return n == null ? 0 : n.tamano;
    }

    private void actualizar(Nodo<K, V> n) {
        // Se invoca solo con nodos reales; el tamaño de un hijo ausente es cero.
        n.tamano = 1 + tamano(n.izq) + tamano(n.der);
    }

    private void validarClave(K clave) {
        if (clave == null) {
            throw new ClaveNulaException("La clave no puede ser null");
        }
    }

    public void agregar(K clave, V valor) {
        validarClave(clave);
        if (raiz == null) {
            raiz = new Nodo<K, V>(clave, valor);
            return;
        }
        Nodo<K, V> actual = raiz;
        // Guardar el camino permite recalcular tamaños al volver hacia la raíz.
        Nodo<K, V>[] camino = new Nodo[8];
        int usados = 0;
        while (true) {
            visitas++;
            if (usados == camino.length) {
                Nodo<K, V>[] nuevo = new Nodo[camino.length * 2];
                for (int i = 0; i < usados; i++) {
                    nuevo[i] = camino[i];
                }
                camino = nuevo;
            }
            camino[usados++] = actual;
            int cmp = clave.compareTo(actual.clave);
            if (cmp == 0) {
                // Una clave existente conserva su posición y solo cambia el valor.
                actual.valor = valor;
                return;
            }
            if (cmp < 0) {
                if (actual.izq == null) {
                    actual.izq = new Nodo<K, V>(clave, valor);
                    actual.izq.padre = actual;
                    break;
                }
                actual = actual.izq;
            } else {
                if (actual.der == null) {
                    actual.der = new Nodo<K, V>(clave, valor);
                    actual.der.padre = actual;
                    break;
                }
                actual = actual.der;
            }
        }
        for (int i = usados - 1; i >= 0; i--) actualizar(camino[i]);
    }

    /* Acceso de paquete: evita una segunda búsqueda al registrar el nodo en IndiceDoble. */
    Nodo<K, V> nodoDe(K clave) {
        validarClave(clave);
        Nodo<K, V> n = raiz;
        while (n != null) {
            visitas++;
            int c = clave.compareTo(n.clave);
            if (c == 0) {
                return n;
            }
            n = c < 0 ? n.izq : n.der;
        }
        return null;
    }
    /* Variante para operaciones auxiliares que no deben afectar la métrica. */
    Nodo<K, V> nodoDeSinContar(K clave) {
        validarClave(clave);
        return buscarSinContar(clave);
    }

    public V obtener(K clave) throws ClaveInexistenteException {
        Nodo<K, V> n = nodoDe(clave);
        if (n == null) {
            throw new ClaveInexistenteException("Clave inexistente: " + clave);
        }
        return n.valor;
    }
    public boolean contiene(K clave) {
        return nodoDe(clave) != null;
    }

    public V eliminar(K clave) throws ClaveInexistenteException {
        validarClave(clave);
        Nodo<K, V> n = raiz;
        while (n != null) {
            visitas++;
            int c = clave.compareTo(n.clave);
            if (c == 0) {
                break;
            }
            n = c < 0 ? n.izq : n.der;
        }
        if (n == null) {
            throw new ClaveInexistenteException("Clave inexistente: " + clave);
        }
        V respuesta = n.valor;
        if (n.izq == null || n.der == null) {
            Nodo<K, V> hijo = n.izq != null ? n.izq : n.der;
            reemplazarEnPadre(n, hijo);
            actualizarHaciaArriba(n.padre);
        } else {
            // Se trasplanta el sucesor en vez de copiar datos, preservando referencias externas al nodo.
            Nodo<K, V> padreSuc = n;
            Nodo<K, V> suc = n.der;
            visitas++;
            while (suc.izq != null) {
                padreSuc = suc;
                suc = suc.izq;
                visitas++;
            }
            if (padreSuc != n) {
                padreSuc.izq = suc.der;
                if (suc.der != null) {
                    suc.der.padre = padreSuc;
                }
                actualizarHaciaArriba(padreSuc);
                suc.der = n.der;
                suc.der.padre = suc;
            }
            suc.izq = n.izq;
            suc.izq.padre = suc;
            reemplazarEnPadre(n, suc);
            actualizar(suc);
            actualizarHaciaArriba(suc.padre);
        }
        return respuesta;
    }
    private void reemplazarEnPadre(Nodo<K, V> viejo, Nodo<K, V> nuevo) {
        Nodo<K, V> padre = viejo.padre;
        if (padre == null) {
            raiz = nuevo;
        } else if (padre.izq == viejo) {
            padre.izq = nuevo;
        } else {
            padre.der = nuevo;
        }
        if (nuevo != null) {
            nuevo.padre = padre;
        }
    }

    private void actualizarHaciaArriba(Nodo<K, V> n) {
        while (n != null) {
            actualizar(n);
            n = n.padre;
        }
    }

    public K kEsimo(int k) {
        if (k < 1 || k > size()) {
            throw new IndiceFueraDeRangoException("k fuera de rango");
        }
        Nodo<K, V> n = raiz;
        while (true) {
            visitas++;
            // El tamaño del hijo izquierdo determina el rango que ocupa el nodo actual.
            int izquierdo = tamano(n.izq);
            if (k == izquierdo + 1) {
                return n.clave;
            }
            if (k <= izquierdo) {
                n = n.izq;
            } else {
                k -= izquierdo + 1;
                n = n.der;
            }
        }
    }
    public int cuantosMenores(K clave) {
        validarClave(clave);
        int resultado = 0;
        Nodo<K, V> n = raiz;
        while (n != null) {
            visitas++;
            if (clave.compareTo(n.clave) <= 0) {
                n = n.izq;
            } else {
                resultado += 1 + tamano(n.izq);
                n = n.der;
            }
        }
        return resultado;
    }

    private int menoresOIguales(K clave) {
        int resultado = 0;
        Nodo<K, V> n = raiz;
        while (n != null) {
            visitas++;
            if (clave.compareTo(n.clave) < 0) {
                n = n.izq;
            } else {
                resultado += 1 + tamano(n.izq);
                n = n.der;
            }
        }
        return resultado;
    }

    public int consultarRango(K a, K b) {
        validarClave(a);
        validarClave(b);
        if (a.compareTo(b) > 0) {
            throw new RangoInvalidoException("a debe ser <= b");
        }
        // Cuenta [a, b] como los <= b menos los estrictamente menores que a.
        return menoresOIguales(b) - cuantosMenores(a);
    }

    public int consultarRangoIngenuo(K a, K b) {
        validarClave(a);
        validarClave(b);
        if (a.compareTo(b) > 0) {
            throw new RangoInvalidoException("a debe ser <= b");
        }
        return contarIngenuo(raiz, a, b);
    }

    private int contarIngenuo(Nodo<K, V> n, K a, K b) {
        if (n == null) {
            return 0;
        }
        int resultado = contarIngenuo(n.izq, a, b);
        visitas++;
        if (n.clave.compareTo(a) >= 0 && n.clave.compareTo(b) <= 0) {
            resultado++;
        }
        return resultado + contarIngenuo(n.der, a, b);
    }
    public int rango(K clave) throws ClaveInexistenteException {
        validarClave(clave);
        int rango = 0;
        Nodo<K, V> nodo = raiz;
        while (nodo != null) {
            visitas++;
            int comparacion = clave.compareTo(nodo.clave);
            if (comparacion == 0) {
                return rango + tamano(nodo.izq) + 1;
            }
            if (comparacion < 0) {
                nodo = nodo.izq;
            } else {
                // Todo el subárbol izquierdo y el nodo actual preceden a la clave buscada.
                rango += tamano(nodo.izq) + 1;
                nodo = nodo.der;
            }
        }
        throw new ClaveInexistenteException("Clave inexistente: " + clave);
    }

    public K sucesor(K clave) throws ClaveInexistenteException {
        validarClave(clave);
        Nodo<K, V> nodo = raiz;
        Nodo<K, V> candidato = null;
        while (nodo != null) {
            visitas++;
            int comparacion = clave.compareTo(nodo.clave);
            if (comparacion == 0) {
                if (nodo.der != null) {
                    // El sucesor es el mínimo del subárbol derecho.
                    nodo = nodo.der;
                    visitas++;
                    while (nodo.izq != null) {
                        nodo = nodo.izq;
                        visitas++;
                    }
                    return nodo.clave;
                }
                // Sin hijo derecho, el ancestro candidato más cercano es el sucesor.
                return candidato == null ? null : candidato.clave;
            }
            if (comparacion < 0) {
                candidato = nodo;
                nodo = nodo.izq;
            } else {
                nodo = nodo.der;
            }
        }
        throw new ClaveInexistenteException("Clave inexistente: " + clave);
    }

    public K predecesor(K clave) throws ClaveInexistenteException {
        validarClave(clave);
        Nodo<K, V> nodo = raiz;
        Nodo<K, V> candidato = null;
        while (nodo != null) {
            visitas++;
            int comparacion = clave.compareTo(nodo.clave);
            if (comparacion == 0) {
                if (nodo.izq != null) {
                    // El predecesor es el máximo del subárbol izquierdo.
                    nodo = nodo.izq;
                    visitas++;
                    while (nodo.der != null) {
                        nodo = nodo.der;
                        visitas++;
                    }
                    return nodo.clave;
                }
                // Sin hijo izquierdo, se utiliza el ancestro menor registrado durante la búsqueda.
                return candidato == null ? null : candidato.clave;
            }
            if (comparacion > 0) {
                candidato = nodo;
                nodo = nodo.der;
            } else {
                nodo = nodo.izq;
            }
        }
        throw new ClaveInexistenteException("Clave inexistente: " + clave);
    }

    public int size() {
        return tamano(raiz);
    }

    public int altura() {
        return alturaDe(raiz);
    }

    private int alturaDe(Nodo<K, V> n) {
        return n == null ? -1 : 1 + Math.max(alturaDe(n.izq), alturaDe(n.der));
    }

    public long visitas() {
        return visitas;
    }

    public void reiniciarVisitas() {
        visitas = 0;
    }

    public boolean tamanosConsistentes() {
        return consistente(raiz) >= 0;
    }

    private int consistente(Nodo<K, V> n) {
        if (n == null) {
            return 0;
        }
        int izquierdo = consistente(n.izq);
        int derecho = consistente(n.der);
        // -1 se propaga para detener la validación ante cualquier tamaño incorrecto.
        if (izquierdo < 0 || derecho < 0 || n.tamano != 1 + izquierdo + derecho) {
            return -1;
        }
        return n.tamano;
    }
    public Iterator<K> iterator() {
        return new IteradorInorden(raiz);
    }

    private class IteradorInorden implements Iterator<K> {
        private Nodo<K, V>[] pila = new Nodo[8];
        private int tope;

        IteradorInorden(Nodo<K, V> nodo) {
            apilarIzq(nodo);
        }

        private void apilarIzq(Nodo<K, V> nodo) {
            // La pila mantiene el próximo ancestro pendiente del recorrido en orden.
            while (nodo != null) {
                if (tope == pila.length) {
                    Nodo<K, V>[] nuevaPila = new Nodo[pila.length * 2];
                    for (int i = 0; i < tope; i++) {
                        nuevaPila[i] = pila[i];
                    }
                    pila = nuevaPila;
                }
                pila[tope++] = nodo;
                nodo = nodo.izq;
            }
        }

        public boolean hasNext() {
            return tope > 0;
        }

        public K next() {
            if (!hasNext()) {
                throw new IllegalStateException("No hay más elementos");
            }
            Nodo<K, V> nodo = pila[--tope];
            apilarIzq(nodo.der);
            return nodo.clave;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    public String toString() {
        StringBuilder texto = new StringBuilder();
        for (K clave : this) {
            if (texto.length() > 0) {
                texto.append(' ');
            }
            // No contabiliza visitas: la representación no altera las métricas del árbol.
            Nodo<K, V> n = buscarSinContar(clave);
            texto.append(clave).append('(').append(n.tamano).append(')');
        }
        return texto.toString();
    }

    private Nodo<K, V> buscarSinContar(K clave) {
        Nodo<K, V> n = raiz;
        while (n != null) {
            int comparacion = clave.compareTo(n.clave);
            if (comparacion == 0) {
                return n;
            }
            n = comparacion < 0 ? n.izq : n.der;
        }
        return null;
    }
}
