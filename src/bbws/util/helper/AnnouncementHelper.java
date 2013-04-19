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
import bbws.util.Util;
import bbws.resource.announcement.BBAnnouncement;
import bbws.resource.course.BBCourse;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;

//blackboard - base
import blackboard.base.FormattedText;
import blackboard.base.FormattedText.Type;

//blackboard - data
import blackboard.data.announcement.Announcement;

//blackboard - persist
import blackboard.persist.announcement.AnnouncementDbLoader;
import blackboard.persist.announcement.AnnouncementDbPersister;
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.user.UserDbLoader;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class AnnouncementHelper
{
    /*********************
     * Announcement posting really should have userID's passed
     * so you can check if user is allowed to post where they
     * want to but this would require authentication, passing
     * the userid and trust of that authentication.
     *
     * textType(null) = HTML / TEXT - DEFAULT TEXT
     * courseID(null) = e.g. bbd510
     * permanent(null) = true / false - DEFAULT false
     * type(null) = COURSE / SYSTEM - DEFAULT COURSE - what happens if -
     *					  no courseid? user not allowed to post system ann.?
     * title(!null) = Title of announcement
     * body(!null) = message to announce
     * startDay/Month/Year(null) = Date to make available - DEFAULT Today -
     *						    startDay 1-31, startMonth 1-12
     * endDay/Month/Year(null) = Date to make unavailable - DEFAULT Always available -
     *						    startDay 1-31, startMonth 1-12
     *******************/
    public static Boolean announcementCreate(BBAnnouncement announcement, BBCourse course, String textType) throws WebServiceException
    {
        try
        {
            Announcement a = new Announcement();
            //We can't use checkAnnouncementDetail as this mustn't throw an error as null or ""
            //is valid when posting message, but not when modifying them.
            if(announcement.getAnnouncementBbId()!=null && !announcement.getAnnouncementBbId().trim().equalsIgnoreCase(""))
            {
                announcement.setAnnouncementBbId(announcement.getAnnouncementBbId().trim());
                //We are modifying an announcement
                a.setId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Announcement.DATA_TYPE,announcement.getAnnouncementBbId()));
            }
            //else we are creating an announcement
            a.setTitle(Util.checkAndTrimParam(announcement.getTitle()));
            announcement.setBody(Util.checkAndTrimParam(announcement.getBody()));
            FormattedText ft = null;
            if(textType.equalsIgnoreCase("HTML"))
            {
                ft = new FormattedText(announcement.getBody(),Type.HTML);
            }
            else
            {
               ft = new FormattedText(announcement.getBody(),Type.PLAIN_TEXT);
            }
            a.setBody(ft);
            //Assume type is course unless specifically set as SYSTEM
            a.setType(blackboard.data.announcement.Announcement.Type.COURSE);
            try
            {
                announcement.setType(Util.checkAndTrimParam(announcement.getType()));
                //may be course or system
                if(announcement.getType().equalsIgnoreCase("SYSTEM"))
                {
                    //it's def a system
                    course.setCourseId("SYSTEM");
                    a.setType(blackboard.data.announcement.Announcement.Type.SYSTEM);
                }
                //it's def a course
            }catch(Exception e){/*it's def a course*/}
            course.setCourseId(Util.checkAndTrimParam(course.getCourseId()));
            a.setCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId());
            a.setCreatorUserId(UserDbLoader.Default.getInstance().loadByUserName("administrator").getId());
            if (announcement.getPermanent()==null)
            {
                announcement.setPermanent(Boolean.FALSE);
            }
            a.setIsPermanent(announcement.getPermanent());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(sdf.parse(announcement.getStartDate()));
            try{a.setRestrictionStartDate(gc);}catch(Exception e){}
            //else don't set a start date at all.
            gc.setTime(sdf.parse(announcement.getEndDate()));
            try{a.setRestrictionEndDate(gc);}catch(Exception e){}
            //else don't set an end date at all.
            AnnouncementDbPersister.Default.getInstance().persist(a);
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        return true;
    }

    public static boolean announcementDelete(BBAnnouncement announcement) throws WebServiceException
    {
        try
        {
            AnnouncementDbPersister.Default.getInstance().deleteById(AnnouncementDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Announcement.DATA_TYPE,announcement.getAnnouncementBbId())).getId());
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Please provide a valid dbid "+e.getMessage());
        }
        return true;
    }

    public static List<BBAnnouncement> announcementReadByAvailableAnnouncementAndUserId(BBUser user) throws WebServiceException
    {
        List<AnnouncementHelper> al = null;
        try
        {
            return BBListFactory.getNonVerboseBBList(AnnouncementDbLoader.Default.getInstance().loadAvailableByUserId(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No announcements found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBAnnouncement> announcementReadByCourseId(BBCourse course) throws WebServiceException
    {
        try
        {
             return BBListFactory.getNonVerboseBBList(AnnouncementDbLoader.Default.getInstance().loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No announcements found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static boolean announcementUpdate(BBAnnouncement announcement, BBCourse course, String textType) throws WebServiceException
    {
        try
        {
            announcement.setAnnouncementBbId(Util.checkAndTrimParam(announcement.getAnnouncementBbId()));
            announcement.setType("COURSE");
            try
            {
                course.setCourseId(Util.checkAndTrimParam(course.getCourseId()));
            }
            catch(Exception e)
            {
                course = null;
                announcement.setType("SYSTEM");
            }
            return announcementCreate(announcement,course,textType);
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: "+e.toString()+" "+e.getMessage());
        }
    }
}
