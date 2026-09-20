import java.util.Random;

/** Traza y experimento del Ejercicio 1. */
public class TestABBAumentado {
    private static void estado(ABBAumentado<Integer, String> arbol) {
        // Resume las invariantes que deben mantenerse tras cada modificación.
        System.out.println(arbol + " | h=" + arbol.altura()
                + " size=" + arbol.size()
                + " consistente=" + arbol.tamanosConsistentes());
    }

    private static int[] permutacion(int n) {
        int[] numeros = new int[n];
        for (int i = 0; i < n; i++) {
            numeros[i] = i + 1;
        }
        // Semilla fija: los resultados de los experimentos son reproducibles.
        Random aleatorio = new Random(2026);
        for (int i = n - 1; i > 0; i--) {
            int j = aleatorio.nextInt(i + 1);
            int temporal = numeros[i];
            numeros[i] = numeros[j];
            numeros[j] = temporal;
        }
        return numeros;
    }
    public static void main(String[] args) throws Exception {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<Integer, String>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        System.out.println("TRAZA ABB");
        estado(arbol);
        for (int clave : claves) {
            arbol.agregar(clave, "P" + clave);
            System.out.print("agregar(" + clave + "): ");
            estado(arbol);
        }

        // Cada medición reinicia el contador para no mezclar operaciones previas.
        arbol.reiniciarVisitas();
        System.out.println("kEsimo(6)=" + arbol.kEsimo(6) + " visitas=" + arbol.visitas());
        arbol.reiniciarVisitas();
        System.out.println("cuantosMenores(65)=" + arbol.cuantosMenores(65) + " visitas=" + arbol.visitas());
        arbol.reiniciarVisitas();
        System.out.println("cuantosMenores(35)=" + arbol.cuantosMenores(35) + " visitas=" + arbol.visitas());
        arbol.reiniciarVisitas();
        System.out.println("consultarRango(35,65)=" + arbol.consultarRango(35, 65)
                + " visitas=" + arbol.visitas());
        arbol.reiniciarVisitas();
        System.out.println("consultarRangoIngenuo(35,65)=" + arbol.consultarRangoIngenuo(35, 65)
                + " visitas=" + arbol.visitas());
        System.out.println("sucesor(40)=" + arbol.sucesor(40)
                + ", sucesor(80)=" + arbol.sucesor(80)
                + ", predecesor(35)=" + arbol.predecesor(35)
                + ", rango(50)=" + arbol.rango(50));
        System.out.print("inorden:");
        for (Integer clave : arbol) {
            System.out.print(" " + clave);
        }
        System.out.println();
        System.out.println("eliminar(30)=" + arbol.eliminar(30));
        estado(arbol);
        System.out.println("\nTABLA 1: N h_aleat h_ord vis_kEsimo_aleat vis_kEsimo_ord vis_rango_aum vis_rango_ing");
        // Compara el comportamiento de inserciones aleatorias contra el peor caso ordenado.
        for (int n = 2000; n <= 10000; n += 2000) {
            ABBAumentado<Integer, Integer> aleatorio = new ABBAumentado<Integer, Integer>();
            ABBAumentado<Integer, Integer> ordenado = new ABBAumentado<Integer, Integer>();
            int[] permutacion = permutacion(n);
            for (int valor : permutacion) {
                aleatorio.agregar(valor, valor);
            }
            for (int valor = 1; valor <= n; valor++) {
                ordenado.agregar(valor, valor);
            }
            aleatorio.reiniciarVisitas();
            aleatorio.kEsimo(n / 2);
            long visitasAleatorio = aleatorio.visitas();
            ordenado.reiniciarVisitas();
            ordenado.kEsimo(n / 2);
            long visitasOrdenado = ordenado.visitas();
            Integer inferior = aleatorio.kEsimo(n / 4);
            Integer superior = aleatorio.kEsimo(3 * n / 4);
            aleatorio.reiniciarVisitas();
            aleatorio.consultarRango(inferior, superior);
            long visitasRangoAumentado = aleatorio.visitas();
            aleatorio.reiniciarVisitas();
            aleatorio.consultarRangoIngenuo(inferior, superior);
            System.out.println(n + " " + aleatorio.altura() + " " + ordenado.altura()
                    + " " + visitasAleatorio + " " + visitasOrdenado
                    + " " + visitasRangoAumentado + " " + aleatorio.visitas());
        }
        // Ordenado degenera: h=N-1 y kEsimo(N/2) baja N/2 nodos. El rango aumentado baja dos caminos; el ingenuo visita N.
    }
}
