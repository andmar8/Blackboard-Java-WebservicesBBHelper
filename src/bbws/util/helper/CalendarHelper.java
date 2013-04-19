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
import bbws.resource.calendar.BBCalendarEntry;
import bbws.resource.course.BBCourse;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;
import bbws.util.Util;

//blackboard - base
import blackboard.base.FormattedText;

//blackboard - data
import blackboard.data.calendar.CalendarEntry;
import blackboard.data.course.Course;
import blackboard.data.user.User;

//blackboard - persist
import blackboard.persist.calendar.CalendarEntryDbLoader;
import blackboard.persist.calendar.CalendarEntryDbPersister;
import blackboard.persist.KeyNotFoundException;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.util.Calendar;
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class CalendarHelper
{
    public static Boolean calendarEntryCreate(BBCalendarEntry bbce, BBCalendarEntry.BBCalendarEntryType bbceType) throws WebServiceException
                                    /*(String courseId, String userId, String description, String title,
                                    BBCalendarEntry.BBCalendarEntryType type, int startDay, int startMonth, int startYear,
                                    int startHour, int startMinute, int startSecond, int endDay,
                                    int endMonth, int endYear, int endHour, int endMinute, int endSecond) throws WebServiceException*/
    {
        //course - course,user,desc,title,type
        //inst - user,desc,title,type (course must not be set? is this system wide calendar?)
        //personal - user,desc,title,type (course must not be set)

        CalendarEntry.Type ceType = null;
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = null;

        ceType = calenderEntryTypeFactory(bbceType);
        startCal = Util.getCalendarObjFromDateTimeString(bbce.getStartDateTime());
        endCal = Util.getCalendarObjFromDateTimeString(bbce.getEndDateTime());

        try
        {
            CalendarEntry ce = new CalendarEntry();
            ce.setCourseId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, bbce.getCourseBbId()));
            //Changed 9.1.40071.3 on
            //ce.setCreatorUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, bbce.getUserBbId()));
            ce.setDescription(new FormattedText(bbce.getDescription(),FormattedText.Type.PLAIN_TEXT));
            if(endCal!=null)
            {
                ce.setEndDate(endCal);
            }
            //ce.setId();
            //ce.setModifiedDate();
            if(startCal!=null)
            {
                ce.setStartDate(startCal);
            }
            ce.setTitle(bbce.getTitle());
            //ce.setType(CalendarEntry.Type.fromExternalString(type));
            ce.setType(ceType);
            //Course Institution personal
            CalendarEntryDbPersister.Default.getInstance().persist(ce);
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while trying to add calendar entry: "+e.toString());
        }

        return true;
    }

    public static boolean calendarEntryDelete(BBCalendarEntry ce) throws WebServiceException
    {
        try
        {
            CalendarEntryDbPersister.Default.getInstance().deleteById(CalendarEntryDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CalendarEntry.DATA_TYPE,ce.getCalendarEntryBbId())).getId());
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Please provide a CalendarEntry object with a valid Id");//+e.toString();
        }
        return true;
    }

    public static BBCalendarEntry calendarEntryRead(BBCalendarEntry ce) throws WebServiceException
    {
        String error = "";
        try
        {
            if(ce.getCalendarEntryBbId()!=null && !ce.getCalendarEntryBbId().equalsIgnoreCase(""))
            {
                //return new BBCalendarEntry(CalendarEntryDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CalendarEntry.DATA_TYPE, ce.getCalendarEntryBbId())));
                return ObjectConverter.getCalendarEntry(CalendarEntryDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CalendarEntry.DATA_TYPE, ce.getCalendarEntryBbId())));
            }
            error = "You must specify a calendar BbId";
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching calender entry";
        }
        catch(Exception e)
        {
            error = "Error whilst searching to see if a calendar entry exists: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenCourse(BBCourse c) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByCourseId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, c.getCourseBbId())));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenCourseAndUserWithinDates(BBCourse c, BBUser u, String start, String end) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByCourseIdAndUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, c.getCourseBbId()),PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, u.getBbId()),Util.getCalendarObjFromDateTimeString(start),Util.getCalendarObjFromDateTimeString(end)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenCourseWithinDates(BBCourse c, String start, String end) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByCourseId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, c.getCourseBbId()),Util.getCalendarObjFromDateTimeString(start),Util.getCalendarObjFromDateTimeString(end)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenType(BBCalendarEntry.BBCalendarEntryType ceType) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByType(calenderEntryTypeFactory(ceType)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenTypeWithinDates(BBCalendarEntry.BBCalendarEntryType ceType, String start, String end) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByType(calenderEntryTypeFactory(ceType),Util.getCalendarObjFromDateTimeString(start),Util.getCalendarObjFromDateTimeString(end)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenUser(BBUser u) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, u.getBbId())));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllForGivenUserWithinDates(BBUser u, String start, String end) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadByUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, u.getBbId()),Util.getCalendarObjFromDateTimeString(start),Util.getCalendarObjFromDateTimeString(end)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBCalendarEntry> calendarEntryReadAllPersonalForGivenUserWithinDates(BBUser u, String start, String end) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(CalendarEntryDbLoader.Default.getInstance().loadPersonalByUserId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, u.getBbId()),Util.getCalendarObjFromDateTimeString(start),Util.getCalendarObjFromDateTimeString(end)));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No calendar entries found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    private static CalendarEntry.Type calenderEntryTypeFactory(BBCalendarEntry.BBCalendarEntryType ceType)
    {
        CalendarEntry.Type type;
        switch(ceType)
        {
            case COURSE: type = CalendarEntry.Type.COURSE; break;
            case INSTITUTION: type = CalendarEntry.Type.INSTITUTION; break;
            case PERSONAL: type = CalendarEntry.Type.PERSONAL; break;
            default: throw new WebServiceException("Invalid CalendarType");
        }
        return type;
    }
}
