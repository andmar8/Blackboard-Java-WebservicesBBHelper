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

/******* Undocumented API *******/

//blackboard - platform
import blackboard.platform.security.DomainManagerFactory;
import blackboard.platform.security.SystemRole;

/******** Documented API ********/

//bbws
import bbws.resource.course.BBCourse;
import bbws.resource.user.BBRole;
import bbws.resource.user.BBUser;
import bbws.util.factory.object.ObjectConverter;

//blackboard - data
import blackboard.data.role.PortalRole;
import blackboard.data.user.User;
import blackboard.data.user.UserRole;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.role.PortalRoleDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.user.UserRoleDbPersister;

//java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class RoleHelper
{
    public static List<PortalRole> parseSecondaryPortalRoles(List<BBRole> roles) throws WebServiceException
    {
        List<PortalRole> pRoles = new ArrayList<PortalRole>();
        try
        {
            PortalRoleDbLoader prl = PortalRoleDbLoader.Default.getInstance();
            Iterator<BBRole> i = roles.iterator();
            BBRole r = null;
            while(i.hasNext())
            {
                r = i.next();
                if(r.getRoleId()!=null && !r.getRoleId().trim().equalsIgnoreCase(""))
                {
                    pRoles.add(prl.loadByRoleId(r.getRoleId().trim()));
                }
            }
        }
        catch(Exception e)
        {
            throw new WebServiceException("Invalid role specified "+e.toString());
        }
        return pRoles;
    }

    public static List<BBRole> roleSecondaryPortalReadByUserId(BBUser user) throws WebServiceException
    {
        List<BBRole> prl = new ArrayList<BBRole>();
        try
        {
            List<PortalRole> rl = PortalRoleDbLoader.Default.getInstance().loadSecondaryRolesByUserId(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId());
            Iterator<PortalRole> i = rl.iterator();

            while(i.hasNext())
            {
                prl.add(ObjectConverter.getRole(i.next()));
            }
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return prl;
    }

    public static Boolean roleSecondaryPortalUpdate(BBUser user, List<BBRole> roles) throws WebServiceException
    {
        //if roles.Length == 0 it will simply delete any existing roles
        try
        {
            User u = UserDbLoader.Default.getInstance().loadByUserName(user.getUserName(),null,true);
            PortalRole priPR = u.getPortalRole();
            UserRoleDbPersister prstr = UserRoleDbPersister.Default.getInstance();
            prstr.deleteAllByUserId(u.getId());

            Iterator<PortalRole> i = parseSecondaryPortalRoles(roles).iterator();
            PortalRole pr = null;
            UserRole ur = null;
            while(i.hasNext())
            {
                ur = new UserRole();
                ur.setUser(u);
                pr = i.next();

                if(!pr.getRoleID().equalsIgnoreCase(priPR.getRoleID()))
                {
                    ur.setPortalRoleId(pr.getId());
                    prstr.persist(ur);
                }
            }
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return true;
    }

    public static List<BBRole> roleSecondarySystemReadByUserId(BBUser user) throws WebServiceException
    {
        List<BBRole> srl = new ArrayList<BBRole>();
        try
        {
	    List<SystemRole> rl = DomainManagerFactory.getInstance().getDefaultDomainRolesForUser(user.getUserName());
            Iterator<SystemRole> i = rl.iterator();

            while(i.hasNext())
            {
                srl.add(ObjectConverter.getRole(i.next()));
            }
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return srl;
    }

    public static BBRole roleUserReadByUserIdAndCourseId(BBUser user, BBCourse course) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getRole(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId()));
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static Boolean setOrModifySecondaryPortalRolesForGivenUserId(String userId, List<PortalRole> roles) throws WebServiceException
    {
   	    //if roles.Length == 0 it will simply delete any existing roles
	    try
	    {
                User u = UserDbLoader.Default.getInstance().loadByUserName(userId,null,true);
                PortalRole priPR = u.getPortalRole();
                UserRoleDbPersister prstr = UserRoleDbPersister.Default.getInstance();
                prstr.deleteAllByUserId(u.getId());
                PortalRole pr = null;
                UserRole ur = null;

                for(int i=0; i<roles.size();i++)
                {
                    ur = new UserRole();
                    ur.setUser(u);
                    pr = roles.get(i);

                    if(!pr.getRoleID().equalsIgnoreCase(priPR.getRoleID()))
                    {
                        ur.setPortalRoleId(pr.getId());
                        prstr.persist(ur);
                    }
                }
	    }
	    catch(Exception e)
	    {
                //return "Error: could not set secondary roles for user "+e.toString();
                throw new WebServiceException(e.toString()+": "+e.getMessage());
	    }
	    return true;
    }
}
