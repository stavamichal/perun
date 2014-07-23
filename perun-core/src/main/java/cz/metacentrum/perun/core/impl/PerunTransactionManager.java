package cz.metacentrum.perun.core.impl;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.AuditerMessage;

public class PerunTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private Auditer auditer;



	@Override
	protected Object doSuspend(Object transaction) {
		if(TransactionSynchronizationManager.hasResource(this.getAuditer())) {
			List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(getAuditer());
			logger.trace("Storing audit messages while suspending transaction. Number of messages " + messages.size());
		}

		return super.doSuspend(transaction);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		if(TransactionSynchronizationManager.hasResource(this.getAuditer())) {
			List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(getAuditer());
			logger.trace("Retrieving audit messages while rusuming transaction. Number of messages " + messages.size());
		}

		super.doResume(transaction, suspendedResources);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {

		logger.info("commit");
		logger.info("rollback only:" + status.isRollbackOnly() + "gro: " + status.isGlobalRollbackOnly());

		super.doCommit(status);

		//System.out.println("map " + TransactionSynchronizationManager.getResourceMap());

		for(Object o: TransactionSynchronizationManager.getResourceMap().keySet()) {
			if(o.getClass().equals(getAuditer().getClass())) System.out.println("YES");
			//System.out.println(o.getClass());
		}
		//System.out.println(getAuditer().getClass());
		
		List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(getAuditer());
		if(messages != null) {
			System.out.println("COMMIT. Number of messages " + messages.size());
		} else {
			System.out.println("COMMIT - NO MSG");
		}

		System.out.println(getAuditer());

		this.getAuditer().flush();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		super.doRollback(status);

		if(TransactionSynchronizationManager.hasResource(this.getAuditer())) {
			List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(getAuditer());
			System.out.println("ROLLBACK. Number of messages " + messages.size());
		} else {
			System.out.println("ROLLBACK - NO MSG");
		}

		this.getAuditer().clean();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		super.doCleanupAfterCompletion(transaction);
		this.getAuditer().clean();
	}

	public Auditer getAuditer() {
		return this.auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

}
