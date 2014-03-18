package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_def_basicDefaultGid extends FacilityUserAttributesModuleAbstract implements FacilityUserAttributesModuleImplApi {

    public void checkAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws WrongAttributeValueException, WrongReferenceAttributeValueException, InternalErrorException, WrongAttributeAssignmentException {
        Attribute namespaceAttribute;
        try {
            namespaceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        } catch (AttributeNotExistsException ex) {
            throw new ConsistencyErrorException(ex);
        }
        if (namespaceAttribute.getValue() == null) {
            throw new WrongReferenceAttributeValueException(attribute, namespaceAttribute, "Reference attribute is null");
        }
        String namespaceName = (String) namespaceAttribute.getValue();

        Attribute resourceGidAttribute;
        try {
            resourceGidAttribute = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespaceName));
        } catch (AttributeNotExistsException ex) {
            throw new ConsistencyErrorException("Namespace from value of " + namespaceAttribute + " doesn't exists. (Resource attribute " + AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespaceName + " doesn't exists", ex);
        }

        resourceGidAttribute.setValue(attribute.getValue());
        List<Resource> allowedResources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
        List<Resource> allowedResourcesWithSameGid = sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, resourceGidAttribute);
        if (allowedResourcesWithSameGid.isEmpty() && allowedResources.isEmpty() && resourceGidAttribute.getValue() == null) return;
        if (allowedResourcesWithSameGid.isEmpty() && resourceGidAttribute.getValue() != null) throw new WrongAttributeValueException(attribute, user, "Resource with requiered group id doesnt exist");
        if (allowedResources.isEmpty()) throw new WrongAttributeValueException(attribute, user, "User has not access to requiered resource");
        allowedResourcesWithSameGid.retainAll(allowedResources);
        
        if (!allowedResourcesWithSameGid.isEmpty()) {
            return; //We found at least one allowed resource with same gid as the user have => attribute is OK
        } else {
            throw new WrongAttributeValueException(attribute, user, "User has not access to resource with required group id");
        }
    }

    public Attribute fillAttribute(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
        Attribute attribute = new Attribute(attributeDefinition);

        List<Resource> allowedResources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
        try {
            for (Resource resource : allowedResources) {
                List<AttributeDefinition> resourceRequiredAttributesDefinitions = sess.getPerunBl().getAttributesManagerBl().getResourceRequiredAttributesDefinition(sess, resource);

                //if this attribute is not required by the services on the resource, skip the resource
                if (!resourceRequiredAttributesDefinitions.contains(new AttributeDefinition(attributeDefinition))) {
                    continue;
                }

                Attribute unixGidAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_VIRT + ":unixGID");
                if (unixGidAttribute.getValue() != null) {
                    attribute.setValue(unixGidAttribute.getValue());
                    return attribute;
                }
            }
        } catch (AttributeNotExistsException ex) {
            throw new ConsistencyErrorException(ex);
        }

        return attribute;
    }

    @Override
    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<String>();
        dependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        dependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace" + ":*");
        dependencies.add(AttributesManager.NS_RESOURCE_ATTR_VIRT + ":unixGID");
        return dependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_DEF);
        attr.setFriendlyName("basicDefaultGid");
        attr.setType(Integer.class.getName());
        attr.setDescription("basic Default Unix Group ID.");
        return attr;
    }
}
