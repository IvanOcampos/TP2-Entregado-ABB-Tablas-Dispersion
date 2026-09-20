/** Tabla hash con arreglos de cubetas y cadenas propias; no usa colecciones del API. */
public class TablaEncadenada<K, E> {
    public static final int TAM_INICIAL = 11;
    private static class Entrada<K, E> {
        K clave;
        E dato;
        Entrada<K, E> sig;

        Entrada(K clave, E dato, Entrada<K, E> siguiente) {
            this.clave = clave;
            this.dato = dato;
            this.sig = siguiente;
        }
    }
    // Cada posición del arreglo es la cabecera de una cadena de colisiones.
    private Entrada<K,E>[] cubetas;
    private int cantidad;
    private final double alfaMax;
    private long sondas;

    public TablaEncadenada() {
        this(TAM_INICIAL, 1.0);
    }

    public TablaEncadenada(int m, double alfaMax) {
        if (m <= 0 || alfaMax <= 0) {
            throw new IllegalArgumentException("capacidad y alfaMax deben ser positivos");
        }
        cubetas = new Entrada[m];
        this.alfaMax = alfaMax;
    }

    private int indice(K clave, int m) {
        if (clave == null) {
            throw new ClaveNulaException("La clave no puede ser null");
        }
        // La máscara evita que un hash negativo produzca un índice inválido.
        return (clave.hashCode() & 0x7fffffff) % m;
    }

    public void insertar(K clave, E dato) {
        int i = indice(clave, cubetas.length);
        for (Entrada<K, E> entrada = cubetas[i]; entrada != null; entrada = entrada.sig) {
            sondas++;
            if (entrada.clave.equals(clave)) {
                entrada.dato = dato;
                return;
            }
        }
        cubetas[i] = new Entrada<K, E>(clave, dato, cubetas[i]);
        cantidad++;
        // Se redimensiona después de insertar para respetar el umbral configurado.
        if (factorCarga() > alfaMax) {
            rehash();
        }
    }

    public E obtener(K clave) {
        int i = indice(clave, cubetas.length);
        for (Entrada<K, E> entrada = cubetas[i]; entrada != null; entrada = entrada.sig) {
            sondas++;
            if (entrada.clave.equals(clave)) {
                return entrada.dato;
            }
        }
        return null;
    }
    public E eliminar(K clave) {
        int i = indice(clave, cubetas.length);
        Entrada<K, E> anterior = null;
        Entrada<K, E> entrada = cubetas[i];
        while (entrada != null) {
            sondas++;
            if (entrada.clave.equals(clave)) {
                // Se distingue la cabecera de los demás nodos para conservar la cadena.
                if (anterior == null) {
                    cubetas[i] = entrada.sig;
                } else {
                    anterior.sig = entrada.sig;
                }
                cantidad--;
                return entrada.dato;
            }
            anterior = entrada;
            entrada = entrada.sig;
        }
        return null;
    }
    private void rehash() {
        Entrada<K, E>[] vieja = cubetas;
        // Duplicar la capacidad baja el factor de carga antes de redistribuir las entradas.
        cubetas = new Entrada[vieja.length * 2];

        for (int i = 0; i < vieja.length; i++) {
            for (Entrada<K, E> entrada = vieja[i]; entrada != null;) {
                // Se guarda el enlace antes de reutilizar la entrada en su nueva cubeta.
                Entrada<K, E> siguiente = entrada.sig;
                int indice = indice(entrada.clave, cubetas.length);
                entrada.sig = cubetas[indice];
                cubetas[indice] = entrada;
                entrada = siguiente;
            }
        }
    }

    public int capacidad() {
        return cubetas.length;
    }

    public int size() {
        return cantidad;
    }

    public double factorCarga() {
        return (double) cantidad / cubetas.length;
    }

    public long sondas() {
        return sondas;
    }

    public void reiniciarSondas() {
        sondas = 0;
    }

    public String dump() {
        StringBuilder texto = new StringBuilder();

        // Muestra la distribución real de claves por cubeta, útil para las trazas.
        for (int i = 0; i < cubetas.length; i++) {
            texto.append(i).append(":");
            for (Entrada<K, E> entrada = cubetas[i]; entrada != null; entrada = entrada.sig) {
                texto.append(" ").append(entrada.clave);
            }
            if (i + 1 < cubetas.length) {
                texto.append('\n');
            }
        }
        return texto.toString();
    }
}
