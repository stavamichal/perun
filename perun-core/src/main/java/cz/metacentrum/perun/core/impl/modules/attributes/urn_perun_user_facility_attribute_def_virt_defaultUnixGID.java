package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGID extends FacilityUserVirtualAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi {

    public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
        Attribute attr = new Attribute(attributeDefinition);
        try {
            //first phase: if attribute UF:D:defaultUnixGID is set, it has top priority
            Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
            if (attribute.getValue() != null) {
                Utils.copyAttributeToVirtualAttributeWithValue(attribute, attr);
                return attr;
            }
            //second phase: UF:D:defaultUnixGID is not set, module will select unix group name from preffered list
            String namespace = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace").getValue();
            if (namespace == null) {
                return attr;
            }

            Attribute userPrefferedGroupNames = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" + namespace);
            List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
            if (userPrefferedGroupNames.getValue() != null) {
                Set<String> resourcesGroupNames = new HashSet<>();
                Map<String,Resource> resourcesWithName = new HashMap<>();
                for (Resource resource : resources) {
                    String groupNameForTest = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:" + namespace).getValue();
                    if (groupNameForTest != null) {
                        resourcesGroupNames.add(groupNameForTest);
                        resourcesWithName.put(groupNameForTest, resource);
                    }
                }

                List<Member> userMembers = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
                Set<String> UnixGroupNames = new HashSet<>();
                Map<String, Group> GroupsWithName = new HashMap<>();
                for (Resource resource : resources) {
                    List<Group> groupsFromResource = sess.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource);
                    for (Group group : groupsFromResource) {
                        List<Member> groupMembers = sess.getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
                        groupMembers.retainAll(userMembers);
                        if (!groupMembers.isEmpty()) {
                            String groupNamesForTest = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:" + namespace).getValue();
                            if (groupNamesForTest != null) {
                                UnixGroupNames.add(groupNamesForTest);
                                GroupsWithName.put(groupNamesForTest, group);
                            }
                        }
                    }
                }

                for (String pUGID : (List<String>) userPrefferedGroupNames.getValue()) {
                    if (resourcesGroupNames.contains(pUGID)) {
                        Utils.copyAttributeToViAttributeWithoutValue(userPrefferedGroupNames, attr);
                        Resource resource = resourcesWithName.get(pUGID);
                        attr.setValue(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace).getValue());
                        return attr;
                    }
                    if (UnixGroupNames.contains(pUGID)) {
                        Utils.copyAttributeToViAttributeWithoutValue(userPrefferedGroupNames, attr);
                        Group group = GroupsWithName.get(pUGID);
                        attr.setValue(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace).getValue());
                        return attr;
                    }
                }
            }
            //third phase: Preffered unix name is not on the facility and it is choosen basicDefaultGID
            Attribute basicGid = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGid");
            Utils.copyAttributeToVirtualAttributeWithValue(basicGid, attr);
            return attr;


        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
        try {
            Attribute attributeToSet = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
            return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, facility, user, attributeToSet);


        } catch (WrongAttributeAssignmentException ex) {
            throw new ConsistencyErrorException(ex);
        } catch (WrongAttributeValueException ex) {
            throw new InternalErrorException(ex);
        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> getStrongDependencies() {
        List<String> strongDependencies = new ArrayList<String>();
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
        strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":preferredUnixGroupName-namespace:*");
        strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:*");
        strongDependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:*");
        strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:*");
        strongDependencies.add(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:*");
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":basicDefaultGid");
        return strongDependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
        attr.setFriendlyName("defaultUnixGID");
        attr.setType(String.class.getName());
        attr.setDescription("Computed unix group id from user preferrences");
        return attr;
    }
}
