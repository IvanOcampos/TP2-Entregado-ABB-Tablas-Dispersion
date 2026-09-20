/** Excepción no chequeada: un límite inferior mayor que el superior es un error del llamador. */
public class RangoInvalidoException extends RuntimeException {
    public RangoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
