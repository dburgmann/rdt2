package rdt.essentials;

/**
 * Class for a exception which is caused by this framework.
 * 
 * @author MK
 */
public class RDTException extends Exception {
	private static final long serialVersionUID = -3502461066153940223L;
	
	/**
	 * The message of the exception.
	 */
	private String message;
	
	/**
	 * Creates a new RDTException with the given message.
	 * 
	 * @param message the message
	 */
	public RDTException(String message) {
		this.message = message;
	}
	
	/**
	 * Returns the message of the exception.
	 * 
	 * @return the message of the exception
	 */
	public String toString(){
		return message;
	}
	
}
