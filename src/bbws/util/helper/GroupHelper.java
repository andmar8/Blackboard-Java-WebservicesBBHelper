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

/******** Documented API ********/

//bbws
import bbws.resource.course.BBCourse;
import bbws.resource.groups.BBGroup;
import bbws.resource.groups.BBGroupMembership;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;

//blackboard - base
import blackboard.base.FormattedText;
import blackboard.base.FormattedText.Type;

//blackboard - data
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupMembershipDbLoader;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.user.UserDbLoader;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class GroupHelper
{
    public static boolean groupAdd(BBGroup group, BBCourse course, String descType) throws WebServiceException
    {
        try
        {
            Group g = new Group();
            g.setCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseBbId()).getId());
            g.setDescription(new FormattedText(group.getDescription(),Type.fromFieldName(descType.trim().toUpperCase())));
            g.setIsAvailable(group.getAvailable());
            g.setIsChatRoomAvailable(group.getChatRoomsAvailable());
            g.setIsDiscussionBoardAvailable(group.getDiscussionBoardsAvailable());
            g.setIsEmailAvailable(group.getEmailAvailable());
            g.setIsTransferAreaAvailable(group.getTransferAreaAvailable());
            g.setTitle(group.getTitle());
            GroupDbPersister.Default.getInstance().persist(g);
        }
        catch(Exception e)
        {
            //return "Invalid description formatted text type, try: HTML/PLAIN_TEXT/SMART_TEXT";
            throw new WebServiceException("Error while trying to add group "+e.toString());
        }
        return true;
    }

    public static boolean groupDelete(BBGroup group) throws WebServiceException
    {
        try
        {
            GroupDbPersister.Default.getInstance().deleteById(GroupDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId())).getId());
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Please provide a valid Id for a group");
        }
        return true;
    }

    public static List<BBGroup> groupReadByCourseId(BBCourse course) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(GroupDbLoader.Default.getInstance().loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No groups found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBGroup groupRead(BBGroup group) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getGroup(GroupDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId())));
        }
        catch(KeyNotFoundException knfe)
        {
            throw new WebServiceException("Error: The given group does not exist");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Could not retrieve group for given Id "+e.toString());
        }
    }

    public static boolean groupUpdate(BBGroup group, String descType) throws WebServiceException
    {
        try
        {
            Group g = GroupDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId()));
            g.setDescription(new FormattedText(group.getDescription(),Type.fromFieldName(descType)));
            g.setIsAvailable(group.getAvailable());
            g.setIsChatRoomAvailable(group.getChatRoomsAvailable());
            g.setIsDiscussionBoardAvailable(group.getDiscussionBoardsAvailable());
            g.setIsEmailAvailable(group.getEmailAvailable());
            g.setIsTransferAreaAvailable(group.getTransferAreaAvailable());
            g.setTitle(group.getTitle());
            GroupDbPersister.Default.getInstance().persist(g);
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return true;
    }

    public static List<BBGroupMembership> groupMembershipReadByGroupId(BBGroup group) throws WebServiceException
    {
        try
        {
           return BBListFactory.getNonVerboseBBList(GroupMembershipDbLoader.Default.getInstance().loadByGroupId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId())));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No group memberships found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: "+e.toString());
        }
    }

    public static boolean groupMembershipCreateByUserIdAndGroupId(BBUser user, BBGroup group) throws WebServiceException
    {
        try
        {
            Group g = GroupDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId()));
            GroupMembership gm = new GroupMembership();
            gm.setCourseMembershipId(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(g.getCourseId(),UserDbLoader.Default.getInstance().loadByUserName(user.getBbId()).getId()).getId());
            gm.setGroupId(g.getId());
            GroupMembershipDbPersister.Default.getInstance().persist(gm);
        }
        catch(PersistenceException pe)
        {
            throw new WebServiceException("Error: Is user already part of this group?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while trying to add user to group: "+e.toString());
        }
        return true;
    }

    public static boolean groupMembershipDeleteByUserIdAndGroupId(BBUser user, BBGroup group) throws WebServiceException
    {
        try
        {
            GroupMembershipDbPersister.Default.getInstance().deleteById(GroupMembershipDbLoader.Default.getInstance().loadByGroupAndUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Group.DATA_TYPE,group.getGroupBbId()),UserDbLoader.Default.getInstance().loadByUserName(user.getBbId()).getId()).getId());
        }
        catch(KeyNotFoundException knfe)
        {
            throw new WebServiceException("Error: User is not a member of this group");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while attempting to delete group membership: "+e.toString());
        }
        return true;
    }
}
