/** Excepción no chequeada: una clave nula viola la precondición del TAD. */
public class ClaveNulaException extends RuntimeException {
    public ClaveNulaException(String mensaje) {
        super(mensaje);
    }
}
