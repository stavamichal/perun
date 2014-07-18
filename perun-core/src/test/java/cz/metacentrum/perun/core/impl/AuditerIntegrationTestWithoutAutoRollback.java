package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.AbstractNotTransactionalPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

public class AuditerIntegrationTestWithoutAutoRollback extends AbstractNotTransactionalPerunIntegrationTest {

	private Facility facility;      // uses creation of facility to store same system message in Auditer
	private String consumerName = "testConsumer";

	@Autowired
	private AuditerConsumer auditerConsumer;

	@Before
	public void checkAuditerExists() throws Exception {

		assertNotNull("unable to get auditer",perun.getAuditer());

	}
	
	@Test
	public void testingMethodTransactionAllOk() throws Exception {
		perun.getAttributesManager().testingMethodTransactionAllOk(sess);
	}

	@Test
	public void testingMethodTransactionParentBad() throws Exception {
		perun.getAttributesManager().testingMethodTransactionParentBad(sess);
	}

	@Test
	public void testingMethodTransactionNestedBad() throws Exception {
		perun.getAttributesManager().testingMethodTransactionNestedBad(sess);
	}
}
