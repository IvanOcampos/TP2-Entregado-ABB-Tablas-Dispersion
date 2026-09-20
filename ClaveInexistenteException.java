/** Excepción chequeada: una clave ausente es una situación normal que quien llama puede manejar. */
public class ClaveInexistenteException extends Exception {
    public ClaveInexistenteException() {
        super();
    }

    public ClaveInexistenteException(String mensaje) {
        super(mensaje);
    }
}
