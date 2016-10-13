package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Integration tests of GroupsManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "GroupsManager.";

	// these must be setUp"type" before every method to be in DB
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final Group group = new Group("GroupsManagerTestGroup1","testovaci1");
	final Group group2 = new Group("GroupsManagerTestGroup2","testovaci2");
	final Group group21 = new Group("GroupsManagerTestGroup21","testovaci21");
	final Group group3 = new Group("GroupsManagerTestGroup3","testovaci3");
	final Group group4 = new Group("GroupsManagerTestGroup4","testovaci4");
	final Group group5 = new Group("GroupsManagerTestGroup5","testovaci5");
	final Group group6 = new Group("GroupsManagerTestGroup6","testovaci6");
	final Group group7 = new Group("GroupsManagerTestGroup7","testovaci7");

	private Vo vo;
	private Group membersGroupOfVo;
	private Map<String, String> syncType = new HashMap<>();
	private List<Attribute> attributesList = new ArrayList<>();

	// exists before every method
	private GroupsManager groupsManager;
	private GroupsManagerBl groupsManagerBl;
	private AttributesManager attributesManager;

	@Before
	public void setUpBeforeEveryMethod() throws Exception {

		groupsManager = perun.getGroupsManager();
		groupsManagerBl = perun.getGroupsManagerBl();
		attributesManager = perun.getAttributesManager();
		vo = setUpVo();
		membersGroupOfVo = perun.getGroupsManagerBl().getGroupByName(sess, vo, "members");
		ExtSource extSource = perun.getExtSourcesManagerBl().getExtSourceByName(sess, "TESTING-XML");
		perun.getExtSourcesManagerBl().addExtSource(sess, vo, extSource);
		perun.getExtSourcesManagerBl().addExtSource(sess, membersGroupOfVo, extSource);
		//Prepare needed attributes for members group
		Attribute groupExternalSource = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":groupExtSource"));
		Attribute groupMembersQuery = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersQuery"));
		Attribute synchronizationEnabled = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationEnabled"));
		Attribute synchronizationInterval = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationInterval"));
		//Set needed attributes
		groupExternalSource.setValue("TESTING-XML");
		groupMembersQuery.setValue("//user");
		synchronizationEnabled.setValue("true");
		synchronizationInterval.setValue("100");
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, groupExternalSource);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, groupMembersQuery);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, synchronizationEnabled);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, synchronizationInterval);

		syncType.put("10", "10-testing-data.xml");
		syncType.put("100", "100-testing-data.xml");
		syncType.put("1000", "1000-testing-data.xml");
		syncType.put("10000", "10000-testing-data.xml");
		syncType.put("100000", "100000-testing-data.xml");
		syncType.put("10A", "10-A-testing-data.xml");
		syncType.put("100A", "100-A-testing-data.xml");
		syncType.put("1000A", "1000-A-testing-data.xml");
		syncType.put("10000A", "10000-A-testing-data.xml");
		syncType.put("100000A", "100000-A-testing-data.xml");
		syncType.put("R", "results.csv");
	}

	//Merit nekolikrat kazdy test, zaroven byt schopen merit prvotni naliti a pote i opakovane synchronizace
	// mozna i ruzne mnozstvi attributu

	@Test
	public void synchronize10UsersInRow() throws Exception {
		String choosedType = "10";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize10AUsersInRow() throws Exception {
		String choosedType = "10A";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize100UsersInRow() throws Exception {
		String choosedType = "100";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize100AUsersInRow() throws Exception {
		String choosedType = "100A";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize1000UsersInRow() throws Exception {
		String choosedType = "1000";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize1000AUsersInRow() throws Exception {
		String choosedType = "1000A";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize10000UsersInRow() throws Exception {
		String choosedType = "10000";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize10000AUsersInRow() throws Exception {
		String choosedType = "10000A";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize100000UsersInRow() throws Exception {
		String choosedType = "100000";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}

	@Test
	public void synchronize100000AUsersInRow() throws Exception {
		String choosedType = "100000A";
		addToResultFile(choosedType, synchronizeUsers(choosedType), true);
		addToResultFile(choosedType, synchronizeUsers(choosedType), false);
		Attribute lightweightSynchronization = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization"));
		lightweightSynchronization.setValue(true);
		perun.getAttributesManagerBl().setAttribute(sess, membersGroupOfVo, lightweightSynchronization);
		addToResultFile(choosedType + "L", synchronizeUsers(choosedType), false);
	}



	//------------------------- WORK METHODS ---------------------------

	private long synchronizeUsers(String type) throws Exception {
		copyFile(syncType.get(type));
		return perun.getGroupsManagerBl().synchronizeGroup(sess, membersGroupOfVo);
	}

	// -------------------------SPECIAL METHODS---------------------------
	private long getTimeNanos() {
		return System.nanoTime();
	}

	private long getResultTime(long start, long end) {
		return end-start;
	}

	private void printResults(long time, String file) {
		long seconds = time/1000/1000/1000;
		System.out.println(file + " -- " + time + " ns " + "(" + seconds + " s)");
	}

	private void copyFile(String sourceFileName) throws Exception {
		Path sourcePath = Paths.get("/home", "perun", "stipendijni_zprava", sourceFileName);
		Path targetPath = Paths.get("/home", "perun", "stipendijni_zprava", "testing-data.xml");
		Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private void addToResultFile(String type, long resultTime, boolean firstSynchronizaton) throws InternalError {
		double timeSeconds = (double) resultTime / 1000000000.0;
		String fileName = type + "-" + syncType.get("R");
		if(firstSynchronizaton) fileName = "first-" + fileName;

		Path resultPath = Paths.get("/home", "perun", "stipendijni_zprava", "results", fileName);
		File resultFile = resultPath.toFile();
		try(FileWriter fw = new FileWriter(resultFile, true);
			   BufferedWriter bw = new BufferedWriter(fw);
			   PrintWriter out = new PrintWriter(bw)) {
			out.print(timeSeconds + ",");

		} catch (IOException e) {
			throw new InternalError("");
		}
	}
	
	// PRIVATE METHODS -------------------------------------------------------------

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);

		//ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, "LDAPMETA");
		// get real external source from DB
		//perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		return returnedVo;

	}

	private Member setUpMember(Vo vo) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate(0);
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Candidate setUpCandidate(int i) {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0+i);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private void setUpGroup(Vo vo) throws Exception {

		Group returnedGroup = groupsManager.createGroup(sess, vo, group);
		assertNotNull("unable to create a group",returnedGroup);
		assertEquals("created group should be same as returned group",group,returnedGroup);
		
	}

	private List<Group> setUpGroupsWithSubgroups(Vo vo) throws Exception {
		Group groupA = new Group("A", "A");
		Group groupB = new Group("B", "B");
		Group groupC = new Group("C", "C");
		Group groupD = new Group("D", "D");
		Group groupE = new Group("E", "E");
		Group groupF = new Group("F", "F");
		Group groupG = new Group("G", "G");

		groupA = this.groupsManagerBl.createGroup(sess, vo, groupA);
		groupD = this.groupsManagerBl.createGroup(sess, vo, groupD);

		groupB = this.groupsManagerBl.createGroup(sess, groupA, groupB);
		groupG = this.groupsManagerBl.createGroup(sess, groupB, groupG);

		groupC = this.groupsManagerBl.createGroup(sess, groupD, groupC);
		groupE = this.groupsManagerBl.createGroup(sess, groupC, groupE);

		groupF = this.groupsManagerBl.createGroup(sess, groupE, groupF);

		List<Group> groups = new ArrayList<>();
		groups.add(groupC);
		groups.add(groupA);
		groups.add(groupF);
		groups.add(groupG);
		groups.add(groupB);
		groups.add(groupD);
		groups.add(groupE);

		return groups;
	}

	private Member setUpMemberWithDifferentParam(Vo vo, int i) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate(i);
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private List<Attribute> setUpGroupAttributes() throws Exception {

		List<Attribute> attributes = new ArrayList<Attribute>();

		// attribute1
		Attribute attr = new Attribute();
		String namespace = "group-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");
		assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr));

		attributes.add(attr);

		// attribute2
		Attribute attr2 = new Attribute(attr);
		attr2.setFriendlyName(namespace + "2");
		attr2.setValue("next2");
		assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr2));

		attributes.add(attr2);

		return attributes;
	}

}
