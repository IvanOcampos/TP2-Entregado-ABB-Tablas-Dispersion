/** Excepción no chequeada: un índice inválido revela un error de programación del llamador. */
public class IndiceFueraDeRangoException extends RuntimeException {
    public IndiceFueraDeRangoException(String mensaje) {
        super(mensaje);
    }
}
