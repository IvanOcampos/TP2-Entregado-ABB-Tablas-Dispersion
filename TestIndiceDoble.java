import java.util.Random;

/** Traza obligatoria y experimentos del Ejercicio 2. */
public class TestIndiceDoble {
    private static int[] permutacion(int n) {
        int[] numeros = new int[n];
        for (int i = 0; i < n; i++) {
            numeros[i] = i + 1;
        }
        // La semilla fija permite repetir exactamente las mismas inserciones.
        Random aleatorio = new Random(2026);
        for (int i = n - 1; i > 0; i--) {
            int j = aleatorio.nextInt(i + 1);
            int temporal = numeros[i];
            numeros[i] = numeros[j];
            numeros[j] = temporal;
        }
        return numeros;
    }

    private static void mostrar(IndiceDoble<Integer, String> indice) {
        // Expone ambas vistas para comprobar que el árbol y la tabla siguen sincronizados.
        System.out.println("arbol: " + indice.arbol());
        System.out.println("size arbol=" + indice.arbol().size()
                + " tabla=" + indice.tabla().size()
                + " visitas=" + indice.arbol().visitas()
                + " sondas=" + indice.tabla().sondas());
        System.out.println(indice.tabla().dump());
    }
    public static void main(String[] args) throws Exception {
        IndiceDoble<Integer, String> indice = new IndiceDoble<Integer, String>();
        int[] base = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int clave : base) {
            indice.agregar(clave, "P" + clave);
        }
        System.out.println("FASE A");
        mostrar(indice);

        indice.agregar(61, "P61");
        indice.agregar(41, "P41");
        System.out.println("FASE B");
        mostrar(indice);

        // El acceso por hash no recorre el ABB; se contrastan ambos contadores.
        indice.arbol().reiniciarVisitas();
        indice.tabla().reiniciarSondas();
        System.out.println("FASE C: indice.obtener(70)=" + indice.obtener(70)
                + " visitas=" + indice.arbol().visitas()
                + " sondas=" + indice.tabla().sondas());
        indice.arbol().reiniciarVisitas();
        System.out.println("arbol.obtener(70)=" + indice.arbol().obtener(70)
                + " visitas=" + indice.arbol().visitas());

        indice.eliminar(30);
        System.out.println("FASE D: eliminar(30)");
        mostrar(indice);
        System.out.println("contiene(30)=" + indice.contiene(30)
                + ", kEsimo(2)=" + indice.kEsimo(2)
                + ", obtener(35)=" + indice.obtener(35));
        System.out.println("\nTABLA 2: N vis_ABB_get sondas_hash_get alfa m");
        // Mide búsqueda ordenada frente a búsqueda exacta mediante la tabla hash.
        for (int n = 2000; n <= 10000; n += 2000) {
            IndiceDoble<Integer, Integer> experimento = new IndiceDoble<Integer, Integer>();
            int[] permutacion = permutacion(n);
            for (int clave : permutacion) {
                experimento.agregar(clave, clave);
            }
            experimento.arbol().reiniciarVisitas();
            experimento.tabla().reiniciarSondas();
            for (int clave : permutacion) {
                experimento.arbol().obtener(clave);
            }
            long visitasArbol = experimento.arbol().visitas();
            for (int clave : permutacion) {
                experimento.obtener(clave);
            }
            System.out.println(n + " " + visitasArbol
                    + " " + experimento.tabla().sondas()
                    + " " + experimento.tabla().factorCarga()
                    + " " + experimento.tabla().capacidad());
        }
        System.out.println("\nTABLA 3: N alfa sondas/N");
        // Sin rehash, el experimento aísla el efecto del factor de carga en las sondas.
        for (int n = 2000; n <= 10000; n += 2000) {
            TablaEncadenada<Integer, Integer> tabla = new TablaEncadenada<Integer, Integer>(
                    97, Double.POSITIVE_INFINITY);
            for (int clave = 1; clave <= n; clave++) {
                tabla.insertar(clave, clave);
            }
            tabla.reiniciarSondas();
            for (int clave = 1; clave <= n; clave++) {
                tabla.obtener(clave);
            }
            System.out.println(n + " " + tabla.factorCarga()
                    + " " + ((double) tabla.sondas() / n));
        }
    }
}
