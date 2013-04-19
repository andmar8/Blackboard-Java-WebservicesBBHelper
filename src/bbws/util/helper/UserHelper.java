/*
    Blackboard WebServices Helper
    Copyright (C) 2011-2013 Andrew Martin, Newcastle University

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bbws.util.helper;

/******** Undocumented API ********/

//blackboard - admin
import blackboard.admin.persist.user.impl.PersonDbLoader;
import blackboard.admin.persist.user.impl.PersonDbPersister;

//blackboard - db
import blackboard.db.ConstraintViolationException;

/******** Documented API ********/

//bbws
import bbws.entity.enums.verbosity.BBUserVerbosity;
import bbws.resource.course.BBCourse;
import bbws.resource.coursemembership.BBCourseMembershipRole;
import bbws.resource.user.BBRole;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;
import bbws.util.Util;

//blackboard - admin
import blackboard.admin.data.IAdminObject.RecStatus;
import blackboard.admin.data.IAdminObject.RowStatus;
import blackboard.admin.data.user.Person;

//blackboard - data
import blackboard.data.course.CourseMembership;
import blackboard.data.role.PortalRole;
import blackboard.data.user.User;
import blackboard.data.user.User.EducationLevel;
import blackboard.data.user.User.Gender;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.role.PortalRoleDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.user.UserDbPersister;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;
import blackboard.platform.security.SecurityUtil;

//java
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class UserHelper
{
    public static Boolean userCreateOrUpdate(BBUser user,BBRole portalRole,List<BBRole> secPortalRoles,BBRole systemRole,Boolean isUpdate) throws WebServiceException
    {
        Person p = null;
        if(isUpdate)
        {
            try
            {
                p = PersonDbLoader.Default.getInstance().load(user.getUserName());
            }
            catch(KeyNotFoundException knfe)
            {
                //We need the user to exist
                throw new WebServiceException("User "+user.getUserName()+" does not exist");
            }
            catch(Exception e)
            {
                //return "Error while trying to check if user already exists: "+e;
                throw new WebServiceException(e.toString()+": "+e.getMessage());
            }

        }
        else
        {
            try
            {
                PersonDbLoader.Default.getInstance().load(user.getUserName());
                //return "Error: User may already exist";
                throw new WebServiceException("User may already exist");
            }
            catch(KeyNotFoundException knfe){} //We need the user to not exist
            catch(Exception e)
            {
                //return "Error while trying to check if user already exists: "+e;
                throw new WebServiceException(e.getMessage());
            }
            p = new Person();
        }
        List<PortalRole> secPRoles = null;
        String debug = "setting user name";
        try
	{
            p.setUserName(Util.checkAndTrimParam(user.getUserName()));//userid
            debug = "setting batch uid";
            p.setBatchUid(Util.checkAndTrimParam(user.getBatchUserBbId()));//batchuid
            debug = "setting given name";
            p.setGivenName(Util.checkAndTrimParam(user.getGivenName()));//firstname
            debug = "setting middle name";
            try{p.setMiddleName(Util.checkAndTrimParam(user.getMiddleName()));}catch(Exception e){}//middlename - Catch Exception as it's not a mandatory field
            debug = "setting family name";
            p.setFamilyName(Util.checkAndTrimParam(user.getFamilyName()));//lastname
            debug = "setting email address";
            p.setEmailAddress(user.getEmailAddress());//emailaddress
            debug = "setting student id";
            try{p.setStudentId(Util.checkAndTrimParam(user.getStudentId()));}catch(Exception e){}//studentid - Catch Exception as it's not a mandatory field
            debug = "setting password";
            p.setPassword(SecurityUtil.getHashValue(user.getPassword()));//password - The password in blackboard is irrelevant if you're using ldap
            debug = "setting gender";
            try//gender - Is this working?
            {
                p.setGender(Gender.fromExternalString(user.getGender().trim().toUpperCase()));
            }
            catch(Exception e)
            {
                p.setGender(Gender.UNKNOWN);
            }
            debug = "setting birthdate";
            try//birthdate
            {
                p.setBirthDate(new GregorianCalendar(Integer.parseInt(user.getBirthDate().substring(0,3)),Integer.parseInt(user.getBirthDate().substring(5, 7))-1,Integer.parseInt(user.getBirthDate().substring(9, 11))));
            }catch(Exception e){}
            debug = "setting education level";
            try//Education Level
            {
                p.setEducationLevel(EducationLevel.fromExternalString(user.getEducationLevel().trim().toUpperCase()));
            }
            catch(Exception e)
            {
                p.setEducationLevel(EducationLevel.UNKNOWN);
            }
            debug = "setting company";
            try{p.setCompany(Util.checkAndTrimParam(user.getCompany()));}catch(Exception e){}//Company - Catch Exception as it's not a mandatory field
            debug = "setting job title";
            try{p.setJobTitle(Util.checkAndTrimParam(user.getJobTitle()));}catch(Exception e){}//Job Title - Catch Exception as it's not a mandatory field
            debug = "setting department";
            try{p.setDepartment(Util.checkAndTrimParam(user.getDepartment()));}catch(Exception e){}//Department - Catch Exception as it's not a mandatory field
            debug = "setting street1";
            try{p.setStreet1(Util.checkAndTrimParam(user.getStreet1()));}catch(Exception e){}//Street 1 - Catch Exception as it's not a mandatory field
            debug = "setting street2";
            try{p.setStreet2(Util.checkAndTrimParam(user.getStreet2()));}catch(Exception e){}//Street 2 - Catch Exception as it's not a mandatory field
            debug = "setting city";
            try{p.setCity(Util.checkAndTrimParam(user.getCity()));}catch(Exception e){}//City - Catch Exception as it's not a mandatory field
            debug = "setting state or province";
            try{p.setState(Util.checkAndTrimParam(user.getStateOrProvince()));}catch(Exception e){}//State / Province - Catch Exception as it's not a mandatory field
            debug = "setting zip or post code";
            try{p.setZipCode(Util.checkAndTrimParam(user.getPostCode()));}catch(Exception e){}//Zip / Postal Code - Catch Exception as it's not a mandatory field
            debug = "setting country";
            try{p.setCountry(Util.checkAndTrimParam(user.getCountry()));}catch(Exception e){}//Country - Catch Exception as it's not a mandatory field
            debug = "setting website";
            try{p.setWebPage(Util.checkAndTrimParam(user.getWebPage()));}catch(Exception e){}//Website - Catch Exception as it's not a mandatory field
            debug = "setting home phone";
            try{p.setHomePhone1(Util.checkAndTrimParam(user.getHomePhone1()));}catch(Exception e){}//Home Phone - Catch Exception as it's not a mandatory field
            debug = "setting work phone";
            try{p.setHomePhone2(Util.checkAndTrimParam(user.getHomePhone2()));}catch(Exception e){}//Work Phone - Catch Exception as it's not a mandatory field
            debug = "setting work fax";
            try{p.setHomeFax(Util.checkAndTrimParam(user.getHomeFax()));}catch(Exception e){}//Work Fax - Catch Exception as it's not a mandatory field
            debug = "setting mobile phone";
            try{p.setMobilePhone(Util.checkAndTrimParam(user.getMobilePhone()));}catch(Exception e){}//Mobile Phone - Catch Exception as it's not a mandatory field
            debug = "setting portal role";
            //Portal Role
            PortalRole pr = null;
            if(portalRole.getRoleId()!=null && !portalRole.getRoleId().equalsIgnoreCase(""))
            {
                portalRole.setRoleId(portalRole.getRoleId().trim());
                pr = PortalRoleDbLoader.Default.getInstance().loadByRoleId(portalRole.getRoleId());

            }
            else
            {
                pr = PortalRoleDbLoader.Default.getInstance().loadByRoleId("STUDENT");
            }
            p.setPortalRole(pr);
            debug = "setting system role";
            //System Role
            if(systemRole.getRoleId()!=null && !systemRole.getRoleId().equalsIgnoreCase(""))
            {
                p.setSystemRole(blackboard.data.user.User.SystemRole.fromFieldName(systemRole.getRoleId().trim().toUpperCase()));
            }
            else
            {
                p.setSystemRole(blackboard.data.user.User.SystemRole.NONE);
            }
            debug = "setting available";
            if(user.getIsAvailable()!=null){p.setIsAvailable(user.getIsAvailable());}else{throw new Exception("Invalid availability");}//Available
            debug = "setting row status";
            p.setRowStatus(RowStatus.ENABLED);
            debug = "setting rec status";
            p.setRecStatus(RecStatus.ADD);
            //p.setReplacementBatchUid(userId);
            debug = "checking secondary portal roles, number specified="+secPortalRoles.size();
            Iterator<BBRole> i = secPortalRoles.iterator();
            while(i.hasNext())
            {
                debug += ", "+i.next();
            }
            //parse valid secondary roles, assuming any roles are specified then they
            //must be valid or the user is not added. No specified roles, null or blank roles are ignored.
            secPRoles = RoleHelper.parseSecondaryPortalRoles(secPortalRoles);
            debug = "exiting try to persist user, you really shouldn't see this!";
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.toString()+" (hint: code was in the process of... "+debug+")");
        }

        try
        {
            if (isUpdate)
            {
                PersonDbPersister.Default.getInstance().update(p);
            }
            else
            {
                PersonDbPersister.Default.getInstance().insert(p);
            }
        }
        catch(ConstraintViolationException cve)
        {
            throw new WebServiceException("The user you're trying to add may already exist");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while trying to add user: "+e.toString());
        }

        /******
         * Following must be set AFTER user is created as you need User.Id
         * in order to set their UserRoles
         *****/

        //Secondary Portal Roles
        if(secPRoles!=null && secPRoles.size()>0)
        {
            try
            {
                RoleHelper.setOrModifySecondaryPortalRolesForGivenUserId(user.getUserName(),secPRoles);
            }
            catch(Exception e)
            {
                throw new WebServiceException(e.toString());
            }
        }
        return true;
    }

    public static boolean userDelete(BBUser user)
    {
        String error = "";
        try
        {
            if(Util.checkParam(user.getUserName()))
            {
                UserDbPersister.Default.getInstance().deleteById(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId());
                return true;
            }
            else if(Util.checkParam(user.getBbId()))
            {
                UserDbPersister.Default.getInstance().deleteById(UserDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE,user.getBbId())).getId());
                return true;
            }
            error = "You must specify either userId or userBBId";
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching user";
        }
        catch(Exception e)
        {
            error = "Error whilst deleting user: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    public static BBUser userRead(BBUser user, BBUserVerbosity verbosity) throws WebServiceException
    {
        String error = "";
        try
        {
            if(user.getUserName()!=null && !user.getUserName().equalsIgnoreCase(""))
            {
                return ObjectConverter.getUser(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()),verbosity);
            }
            else if(user.getBbId()!=null && !user.getBbId().equalsIgnoreCase(""))
            {
                return ObjectConverter.getUser(UserDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, user.getBbId())),verbosity);
            }
            error = "You must specify either userId or userBBId";
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching user";
        }
        catch(Exception e)
        {
            error = "Error whilst finding user: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    //Changed 9.1.40071.3 on
    public static List<BBUser> userReadAll(BBUserVerbosity verbosity)
    {
        try
        {
	    Person p = new Person();
	    p.setBatchUid("%%");
            return BBListFactory.getBBUserListFromPersonList(PersonDbLoader.Default.getInstance().load(p),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No users found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error whilst getting all users: "+e.toString());
        }
    }

    public static List<BBUser> userReadByCourseIdAndCMRole(BBCourse course, BBCourseMembershipRole cmRole, BBUserVerbosity verbosity) throws WebServiceException
    {
        List<BBUser> rl = new ArrayList<BBUser>();
        try
        {
            //This may require heavy loading instead of lightweight
            List<CourseMembership> cml = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),CourseMembership.Role.fromExternalString(cmRole.name()),null,true);
	    if(cml.size()>0)
	    {
		Iterator<CourseMembership> i = cml.iterator();
		while(i.hasNext())
		{
		    rl.add(ObjectConverter.getUser(i.next().getUser(),verbosity));
		}
		return rl;
	    }
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return rl;
    }

    public static List<BBUser> userReadByCourseId(BBCourse course, BBUserVerbosity verbosity) throws WebServiceException
    {
        List<BBUser> al = new ArrayList<BBUser>();
        try
        {
            List<CourseMembership> membershipList = CourseMembershipDbLoader.Default.getInstance().loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),null,true);
            Iterator<CourseMembership> i = membershipList.iterator();
            User u = null;
            while(i.hasNext())
            {
                u = i.next().getUser();
                try{al.add(ObjectConverter.getUser(u,verbosity));}catch(Exception e){System.out.println("Error while instantiating user "+u.getUserName()+": "+e.getMessage());}
            }
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return al;
    }
}
