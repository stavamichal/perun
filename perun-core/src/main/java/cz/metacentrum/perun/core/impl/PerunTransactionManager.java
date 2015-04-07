package cz.metacentrum.perun.core.impl;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.AuditerMessage;
import org.springframework.transaction.TransactionDefinition;

public class PerunTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private Auditer auditer;

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		System.out.println("TRANSACTION: -- doSetRollbackOnly");
		super.doSetRollbackOnly(status);
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		System.out.println("TRANSACTION: -- doBegin");
		super.doBegin(transaction, definition);
	}

	@Override
	protected Object doGetTransaction() {
		System.out.println("TRANSACTION: -- doGetTransaction");
		return super.doGetTransaction();
	}

	@Override
	public void afterPropertiesSet() {
		System.out.println("TRANSACTION: -- afterPropertiesSet");
		super.afterPropertiesSet();
	}

	@Override
	protected Object doSuspend(Object transaction) {
		System.out.println("TRANSACTION: -- doSuspend");
		if(TransactionSynchronizationManager.hasResource(this.getAuditer())) {
			List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(getAuditer());
			logger.trace("Storing audit messages while suspending transaction. Number of messages " + messages.size());
		}

		return super.doSuspend(transaction);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		System.out.println("TRANSACTION: -- doResume");
		if(TransactionSynchronizationManager.hasResource(this.getAuditer())) {
			List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(transaction);
			logger.trace("Retrieving audit messages while rusuming transaction. Number of messages " + messages.size());
		}

		super.doResume(transaction, suspendedResources);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		System.out.println("TRANSACTION: -- doCommit");
		super.doCommit(status);
		this.getAuditer().flush();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		System.out.println("TRANSACTION: -- doRollback");
		super.doRollback(status);
		this.getAuditer().clean();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		System.out.println("TRANSACTION: -- doCleanupAfterCompletion");
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
