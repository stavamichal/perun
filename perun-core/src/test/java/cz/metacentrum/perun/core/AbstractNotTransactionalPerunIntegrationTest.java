package cz.metacentrum.perun.core;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/perun-beans.xml", "/perun-datasources.xml" })
@TransactionConfiguration(transactionManager = "perunTransactionManager", defaultRollback = false)
@Transactional(rollbackFor = InternalErrorRuntimeException.class)
public abstract class AbstractNotTransactionalPerunIntegrationTest {

	@Autowired
	protected PerunBl perun;

	protected PerunSession sess;

	public void setPerun(PerunBl p) {
		this.perun = p;
	}

	@Before
	public void setUpSess() throws Exception {
		final PerunPrincipal pp = new PerunPrincipal("perunNonTransactionalTests", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		sess = perun.getPerunSession(pp);
	}
}
