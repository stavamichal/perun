package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

/**
 * <p>VOs manager can create, delete, update and find VO.</p>
 * <p/>
 * <p>You must get an instance of VosManager from Perun:</p>
 * <pre>
 *    PerunSession ps;
 *    //...
 *    VosManager vm = ps.getPerun().getVosManager();
 * </pre>
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @see PerunSession
 */
public interface VosManager {

	static final String MEMBERS_GROUP = "members";
	static final String MEMBERS_GROUP_DESCRIPTION = "Group containing VO members";

	void mainTransaction(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get list of Vos by Access Righs:
	 * If User is:
	 * - PERUNADMIN : get all Vos
	 * - VoAdmin : Vo where user is Admin
	 * - GroupAdmin: Vo where user is GroupAdmin
	 *
	 * @param perunSession
	 * @return List of VOs or empty ArrayList<Vo>
	 *
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	List<Vo> getVos(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

	/**
	 * Get list of Vos without any privilege.
	 *
	 * @param perunSession
	 * @return List of VOs or empty ArrayList<Vo>
	 *
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	List<Vo> getAllVos(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 */
	void deleteVo(PerunSession perunSession, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param forceDelete force the deletion of the VO, regardless there are any existing entities associated with the VO (they will be deleted)
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException exception is thrown when forceDelete is false and there are some entities associated with the VO
	 */
	void deleteVo(PerunSession perunSession, Vo vo, boolean forceDelete) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException;


	/**
	 * Create new VO.
	 *
	 * @param perunSession
	 * @param vo vo object with prefilled voShortName and voName
	 * @return newly created VO
	 * @throws VoExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Vo createVo(PerunSession perunSession, Vo vo) throws VoExistsException, PrivilegeException, InternalErrorException;

	/**
	 * Updates VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return returns updated VO
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	Vo updateVo(PerunSession perunSession, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Find existing VO by short name (short name is unique).
	 *
	 * @param perunSession
	 * @param shortName short name of VO which you find (for example "KZCU")
	 * @return VO with requested shortName or throws VoNotExistsException if the VO with specified shortName doesn't exist
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	Vo getVoByShortName(PerunSession perunSession, String shortName) throws VoNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Finds existing VO by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return VO with requested id or throws VoNotExistsException if the VO with specified id doesn't exist
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Vo getVoById(PerunSession perunSession, int id) throws VoNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @param maxNumOfResults limit the maximum number of returned entries
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException, VoNotExistsException, PrivilegeException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo vo to be used
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException;

	/**
	 * Add a user administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will became an VO administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, User user) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, UserNotExistsException;

	/**
	 * Add a group administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group that will become a VO administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 * @throws GroupNotExistsException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException;


	/**
	 * Removes a user administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will lose an VO administrator role
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, User user) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotAdminException, UserNotExistsException;

	/**
	 * Removes a group administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group group that will lose a VO administrator role
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws GroupNotAdminException
	 * @throws GroupNotExistsException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException, PrivilegeException, VoNotExistsException, GroupNotAdminException, GroupNotExistsException;


	/**
	 * Get list of Vo administrators.
	 * If some group is administrator of the VO, all members are included in the list.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<User> getAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Gets list of direct user administrators of the VO.
	 * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<User> getDirectAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get list of group administrators of the given VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;


	/**
	 * Get list of Vo administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Get list of Vo administrators with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Get list of Vo administrators like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Get list of Vo administrators like RichUsers with attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException;
}
