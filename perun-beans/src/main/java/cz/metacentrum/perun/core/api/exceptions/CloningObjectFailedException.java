package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of CloningObjectFailedException.
 *
 * This exception is thrown when cloning of some object failed.
 *
 * @author Michal Stava
 */
public class CloningObjectFailedException extends PerunException {
	static final long serialVersionUID = 0;
	Object clonedObject;

	public CloningObjectFailedException(String message, Object clonedObject) {
		super(message);
		this.clonedObject=clonedObject;
	}

	public CloningObjectFailedException(String message, Object clonedObject, Throwable cause) {
		super(message, cause);
		this.clonedObject=clonedObject;
	}

	public CloningObjectFailedException(Object clonedObject, Throwable cause) {
		super(cause);
		this.clonedObject=clonedObject;
	}

	public CloningObjectFailedException(Throwable cause) {
		super(cause);
	}

	public Object getClonedObject() {
		return this.clonedObject;
	}
}
