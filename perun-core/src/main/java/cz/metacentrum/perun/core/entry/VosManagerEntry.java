package cz.metacentrum.perun.core.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * VosManager entry logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerEntry implements VosManager {

    private PerunBl perunBl;
    private VosManagerBl vosManagerBl;

    /**
     * Constructor.
     */
    public VosManagerEntry(PerunBl perunBl) {
        this.perunBl = perunBl;
        this.vosManagerBl = this.perunBl.getVosManagerBl();
    }

    public VosManagerEntry() {
    }

    public List<Vo> getVos(PerunSession sess) throws InternalErrorException, PrivilegeException {
        getPerunBl().getAuditer().log(sess, "TESTING- This message comes BEFORE nested transaction begin.");
        
        Vo v = new Vo(98999, "interniTest01", "interniTest01");
        try {
            getPerunBl().getVosManagerBl().createVo(sess, v);
        } catch (VoExistsException ex) {
            throw new InternalErrorException(ex);
        }
        
        //Return fictive vos
        List<Vo> vos = new ArrayList<Vo>();
        try {
            vos = getPerunBl().getVosManagerBl().getVos(sess);
        } catch (InternalErrorException ex) {
            //this is ok, we excpected this exception
        }
        
        try {
            //Waiting for 15 seconds
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            //nothing to do
        }
        
        return vos;
    }

    public List<Vo> getAllVos(PerunSession perunSession) throws InternalErrorException, PrivilegeException {
        
        return new ArrayList<Vo>();
    }

    public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException {
        Utils.notNull(sess, "sess");

        // Authorization - only Perun admin can delete the VO
        if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
            throw new PrivilegeException(sess, "deleteVo");
        }

        vosManagerBl.checkVoExists(sess, vo);

        vosManagerBl.deleteVo(sess, vo, forceDelete);
    }

    public void deleteVo(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException {
        Utils.notNull(sess, "sess");

        // Authorization - only Perun admin can delete the VO
        if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
            throw new PrivilegeException(sess, "deleteVo");
        }

        vosManagerBl.checkVoExists(sess, vo);

        vosManagerBl.deleteVo(sess, vo);
    }

    public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, PrivilegeException, InternalErrorException {
        Utils.notNull(sess, "sess");
        Utils.notNull(vo, "vo");

        // Authorization - Perun admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN)) {
            throw new PrivilegeException(sess, "createVo");
        }


        if (vo.getName().length() > 128) {
            throw new InternalErrorException("VO name is too long, >128 characters");
        }

        if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,16}$")) {
            throw new InternalErrorException("Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 16 characters.");
        }

        return vosManagerBl.createVo(sess, vo);
    }

    public Vo updateVo(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
            throw new PrivilegeException(sess, "updateVo");
        }

        if (vo.getName().length() > 128) {
            throw new InternalErrorException("VO name is too long, >128 characters");
        }

        if (!vo.getShortName().matches("^[-_a-zA-z0-9.]{1,16}$")) {
            throw new InternalErrorException("Wrong VO short name - must matches [-_a-zA-z0-9.]+ and not be longer than 16 characters.");
        }

        return vosManagerBl.updateVo(sess, vo);
    }

    public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException, InternalErrorException, PrivilegeException {
        Utils.notNull(shortName, "shortName");
        Utils.notNull(sess, "sess");
        Vo vo = vosManagerBl.getVoByShortName(sess, shortName);

        // Authorization
        //TODO Any groupAdmin can get anyVo
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
                !AuthzResolver.isAuthorized(sess, Role.SERVICE)) {
            throw new PrivilegeException(sess, "getVoByShortName");
        }

        return vo;
    }

    public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException, InternalErrorException, PrivilegeException {
        Utils.notNull(sess, "sess");
        Vo vo = vosManagerBl.getVoById(sess, id);

        // Authorization
        //TODO Any groupAdmin can get anyVo
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.SERVICE) &&
                !AuthzResolver.isAuthorized(sess, Role.RPC) &&
                !AuthzResolver.isAuthorized(sess, Role.SELF)) {
            throw new PrivilegeException(sess, "getVoById");
        }

        return vo;
    }

    public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException, VoNotExistsException, PrivilegeException {
        Utils.notNull(searchString, "searchString");
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "findCandidates");
        }

        return vosManagerBl.findCandidates(sess, vo, searchString, maxNumOfResults);
    }

    public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, VoNotExistsException, PrivilegeException {
        Utils.notNull(searchString, "searchString");
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "findCandidates");
        }

        return vosManagerBl.findCandidates(sess, vo, searchString);
    }

    public void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, UserNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);
        perunBl.getUsersManagerBl().checkUserExists(sess, user);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
            throw new PrivilegeException(sess, "addAdmin");
        }

        vosManagerBl.addAdmin(sess, vo, user);
    }


    @Override
    public void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, PrivilegeException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);
        perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
            throw new PrivilegeException(sess, "addAdmin");
        }

        vosManagerBl.addAdmin(sess, vo, group);
    }

    public void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotAdminException, UserNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);
        perunBl.getUsersManagerBl().checkUserExists(sess, user);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
            throw new PrivilegeException(sess, "deleteAdmin");
        }

        vosManagerBl.removeAdmin(sess, vo, user);
    }

    @Override
    public void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, PrivilegeException, VoNotExistsException, GroupNotAdminException, GroupNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);
        perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

        // Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
            throw new PrivilegeException(sess, "deleteAdmin");
        }

        vosManagerBl.removeAdmin(sess, vo, group);
    }

    public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getAdmins");
        }

        return vosManagerBl.getAdmins(sess, vo);
    }

    @Override
    public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getDirectAdmins");
        }

        return vosManagerBl.getDirectAdmins(sess, vo);
    }

    @Override
    public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getAdminGroups");
        }

        return vosManagerBl.getAdminGroups(sess, vo);
    }

    public List<RichUser> getRichAdmins(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && 
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getRichAdmins");
        }

        return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdmins(sess, vo));
    }

    public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && 
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getRichAdminsWithAttributes");
        }

        return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithAttributes(sess, vo));
    }

    public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession sess, Vo vo, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, VoNotExistsException, UserNotExistsException {
        Utils.notNull(sess, "sess");
        vosManagerBl.checkVoExists(sess, vo);

        //  Authorization - Vo admin required
        if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
                !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
            throw new PrivilegeException(sess, "getRichAdminsWithSpecificAttributes");
        }

        return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, vosManagerBl.getRichAdminsWithSpecificAttributes(sess, vo, specificAttributes));
    }

    /**
     * Gets the perunBl for this instance.
     *
     * @return The perunBl.
     */
    public PerunBl getPerunBl() {
        return this.perunBl;
    }

    /**
     * Sets the perunBl for this instance.
     *
     * @param perunBl The perunBl.
     */
    public void setPerunBl(PerunBl perunBl)
    {
        this.perunBl = perunBl;
    }

    /**
     * Sets the vosManagerBl for this instance.
     *
     * @param vosManagerBl The vosManagerBl.
     */
    public void setVosManagerBl(VosManagerBl vosManagerBl)
    {
        this.vosManagerBl = vosManagerBl;
    }
}