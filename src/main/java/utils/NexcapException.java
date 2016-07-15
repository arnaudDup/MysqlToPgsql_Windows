package utils;

@SuppressWarnings("serial")
public class NexcapException extends Exception {
	public NexcapException() {
		super();
	}
	
    public NexcapException(String message) {
        super(message);
    }
	
    public NexcapException(String message, Throwable cause) {
        super(message, cause);
    }
}
