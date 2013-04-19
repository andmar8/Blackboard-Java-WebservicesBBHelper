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

//blackboard - data
import bbws.util.Util;
import blackboard.data.registry.CourseRegistryEntry;

//blackboard - persist
import blackboard.persist.registry.CourseRegistryEntryDbLoader;
import blackboard.persist.registry.CourseRegistryEntryDbPersister;

/******** Documented API ********/

//bbws
import bbws.resource.course.BBCourse;
import bbws.resource.course.BBCourseQuota;
import bbws.entity.enums.verbosity.BBCourseVerbosity;
import bbws.resource.coursemembership.BBCourseMembershipRole;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;

//blackboard - base

//blackboard - data
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseQuota;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseDbPersister;
import blackboard.persist.Id;
import blackboard.persist.user.UserDbLoader;
import blackboard.persist.KeyNotFoundException;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

//javax
import javax.xml.ws.WebServiceException;

public class CourseHelper
{
    public static boolean courseCreate(BBCourse course)
    {
        try
        {
            CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId());
            throw new WebServiceException("Error: Course may already exist");
        }
        catch(KeyNotFoundException knfe){}
        catch(Exception e)
        {
            throw new WebServiceException("Error while trying to check if course already exists: "+e.toString()+" "+e.getMessage());
        }

        Course c = new Course();
        try
        {
            c.setBatchUid(course.getBatchUId());
            c.setCourseId(course.getCourseId());
            c.setDescription(course.getDescription());
            c.setTitle(course.getTitle());
            c.setAllowGuests(course.getAllowGuests());
            c.setAllowObservers(course.getAllowObservers());
            c.setIsAvailable(course.getAvailable());
            CourseDbPersister.Default.getInstance().persist(c);
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while trying to add course: "+e.getMessage());
        }
        return true;
    }

    public static boolean courseDelete(BBCourse course)
    {
        String error = "";
        try
        {
            if(Util.checkParam(course.getCourseId()))
            {
                CourseDbPersister.Default.getInstance().deleteById(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId());
                return true;
            }
            else if(Util.checkParam(course.getCourseBbId()))
            {
                CourseDbPersister.Default.getInstance().deleteById(CourseDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE,course.getCourseBbId())).getId());
                return true;
            }
            error = "You must specify either courseId or courseBBId";
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching course";
        }
        catch(Exception e)
        {
            error = "Error whilst deleting course: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    public static BBCourse courseRead(BBCourse course, BBCourseVerbosity verbosity) throws WebServiceException
    {
        String error = "";
        try
        {
            if(verbosity == null)
            {
                throw new Exception("You must specify a verbosity level");
            }
            if(course.getCourseId()!=null && !course.getCourseId().equalsIgnoreCase(""))
            {
                //return new BBCourse(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()),verbosity);
                return ObjectConverter.getCourse(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()), verbosity);
            }
            else if(course.getCourseBbId()!=null && !course.getCourseBbId().equalsIgnoreCase(""))
            {
                //return new BBCourse(CourseDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, course.getCourseBbId())),verbosity);
                return ObjectConverter.getCourse(CourseDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Course.DATA_TYPE, course.getCourseBbId())), verbosity);
            }
            error = "You must specify either courseId or courseBBId";
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching course";
        }
        catch(Exception e)
        {
            error = "Error whilst searching to see if course exists: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    public static List<BBCourse> courseReadAll(BBCourseVerbosity verbosity)
    {
        try
        {
            if(verbosity == null)
            {
                throw new Exception("You must specify a verbosity level");
            }
            //return getBBCourseListFromList(CourseDbLoader.Default.getInstance().loadAllCourses(),verbosity);
            return BBListFactory.getBBCourseListFromList(CourseDbLoader.Default.getInstance().loadAllCourses(),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No courses found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error whilst searching to see if course exists: "+e.toString());
        }
    }

    public static List<BBCourse> courseReadByUserIdAndCMRole(BBUser user, BBCourseMembershipRole cmRole, BBCourseVerbosity verbosity) throws WebServiceException
    {
        try
        {
            if(verbosity == null)
            {
                throw new Exception("You must specify a verbosity level");
            }
            //return getBBCourseListFromList(CourseDbLoader.Default.getInstance().loadByUserIdAndCourseMembershipRole(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId(),CourseMembership.Role.fromExternalString(cmRole.name())),verbosity);
            return BBListFactory.getBBCourseListFromList(CourseDbLoader.Default.getInstance().loadByUserIdAndCourseMembershipRole(UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId(),CourseMembership.Role.fromExternalString(cmRole.name())),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No courses found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error whilst searching to see if course exists: "+e.toString());
        }
    }

    public static List<BBCourse> courseReadSearchByRegex(String regex, BBCourseVerbosity verbosity) throws WebServiceException
    {
        List<BBCourse> rl = new ArrayList<BBCourse>();
        List<Course> cl = null;
        try
        {
            cl = CourseDbLoader.Default.getInstance().loadAllCourses();
        }
        catch(Exception e)
        {
            throw new WebServiceException("Could not load all courses to search: "+e.getMessage());
        }

        if(cl!=null && cl.size()>0)
        {
            Pattern pattern =  Pattern.compile(regex);
            Course c = null;
            Iterator i = cl.iterator();

            while(i.hasNext())
            {
                c = ((Course)i.next());
                if(Util.isAMatch(pattern,c.getCourseId()))
                {
                    //The verbosity exception here "should" NEVER happen
                    //try{rl.add(new BBCourse(c,verbosity));}catch(Exception e){System.err.println("Error while instantiating course "+c.getCourseId()+": "+e.getMessage());}
                    try{rl.add(ObjectConverter.getCourse(c,verbosity));}catch(Exception e){System.err.println("Error while instantiating course "+c.getCourseId()+": "+e.getMessage());}
                }
            }

            if(rl.size()<1)
            {
                throw new WebServiceException("No matches found");
            }
            return rl;
        }
        throw new WebServiceException("No courses found");
    }

    public static BBCourseQuota courseQuotaRead(BBCourse course) throws WebServiceException
    {
        try
        {
            //return new BBCourseQuota(CourseQuota.createInstance(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId())));
            return ObjectConverter.getCourseQuota(CourseQuota.createInstance(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId())));
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Could not find course with that Id "+e.getMessage()+" "+e.toString());
        }
    }

    public static Boolean courseQuotaUpdate(BBCourse course, BBCourseQuota courseQuota) throws WebServiceException
    {
        if(courseQuota.getEnforceQuota()!=null || courseQuota.getEnforceUploadLimit()!=null || courseQuota.getSystemUploadLimit()!=null || courseQuota.getSystemSoftLimit()!=null || courseQuota.getSystemAbsoluteLimit()!=null)
        {
            Course c = null;
            try
            {
                c = CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId());
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: Could not load course to modify quota for, does it exist? "+e.getMessage());
            }

            try
            {
                setOrModifyCourseRegistryValue(c.getId(),"quota_override",courseQuota.getEnforceQuota()?"Y":"N");
                setOrModifyCourseRegistryValue(c.getId(),"quota_upload_override",courseQuota.getEnforceUploadLimit()?"Y":"N");

                if(courseQuota.getCourseAbsoluteLimit()!=null)
                {
                    c.setAbsoluteLimit(courseQuota.getCourseAbsoluteLimit());
                }

                if(courseQuota.getCourseSoftLimit()!=null)
                {
                    c.setSoftLimit(courseQuota.getCourseSoftLimit());
                }

                if(courseQuota.getCourseUploadLimit()!=null)
                {
                    c.setUploadLimit(courseQuota.getCourseUploadLimit());
                }

                CourseDbPersister.Default.getInstance().persist(c);
            }
            catch(Exception e)
            {
                throw new WebServiceException( "Error: Could not modify course quota settings - "+e.getMessage());
            }
            return true;
        }
        return false;
    }

    private static Boolean setOrModifyCourseRegistryValue(Id crsId, String regKey, String value) throws Exception
    {
        CourseRegistryEntry entry = null;
        try
        {
            entry = CourseRegistryEntryDbLoader.Default.getInstance().loadByKeyAndCourseId(regKey,crsId);
        }
        catch(Exception e)
        {
            //ignore this error. if there is an exception, it means entry not found for this course,
            entry = new CourseRegistryEntry();
            entry.setCourseId(crsId);
            entry.setKey(regKey);
            entry.validate();
        }
        entry.setValue(value);
        CourseRegistryEntryDbPersister.Default.getInstance().persist(entry);
        return true;
    }
}
