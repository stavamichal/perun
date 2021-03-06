package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;

public class AdminRemovedForVo extends AuditEvent implements EngineIgnoreEvent {

	private User user;
	private Vo vo;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminRemovedForVo() {
	}

	public AdminRemovedForVo(User user, Vo vo) {
		this.user = user;
		this.vo = vo;
		this.message = formatMessage("%s was removed from admins of %s.", user, vo);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public Vo getVo() {
		return vo;
	}

	@Override
	public String toString() {
		return message;
	}
}
