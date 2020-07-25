package alemiz.bettersurvival.utils.exception;

public class CancelException extends Exception{

    private final Object value;

    public CancelException(Object value){
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}
