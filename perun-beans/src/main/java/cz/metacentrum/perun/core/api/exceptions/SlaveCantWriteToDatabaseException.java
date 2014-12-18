package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this exception is thrown if slave server try to write to the database
 *
 * @author Michal Stava
 */
public class SlaveCantWriteToDatabaseException extends PerunException {
	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(SlaveCantWriteToDatabaseException.class);

	public SlaveCantWriteToDatabaseException(String message) {
		super(message);

		log.error("SlaveCantWriteToDatabaseException:", this);
	}

	public SlaveCantWriteToDatabaseException(String message, Throwable cause) {
		super(message, cause);

		log.error("SlaveCantWriteToDatabaseException:", this);
	}

	public SlaveCantWriteToDatabaseException(Throwable cause) {
		super(cause);

		log.error("SlaveCantWriteToDatabaseException:", this);
	}
}
