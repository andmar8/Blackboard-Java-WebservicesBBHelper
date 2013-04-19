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
import bbws.resource.course.BBEnrollment;
import bbws.resource.coursemembership.BBCourseMembership;
import bbws.entity.enums.verbosity.BBCourseMembershipVerbosity;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;
import bbws.util.Util;

//blackboard - admin
import blackboard.admin.data.course.Enrollment;
import blackboard.admin.persist.course.EnrollmentLoader;

//blackboard - data
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.CourseMembership.Role;
import blackboard.data.user.User;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.CourseMembershipDbPersister;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.user.UserDbLoader;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class CourseMembershipHelper
{
    public static Boolean courseMembershipCreate(BBCourseMembership courseMembership) throws WebServiceException
    {
        Id uid = null;
        Id cid = null;

        try
        {
            uid = UserDbLoader.Default.getInstance().loadByUserName(courseMembership.getUser().getUserName()).getId();
        }
        catch(Exception e)
        {
            throw new WebServiceException("Please provide a valid username "+e.toString());
        }

        try
        {
            cid = CourseDbLoader.Default.getInstance().loadByCourseId(courseMembership.getCourse().getCourseId()).getId();
        }
        catch(Exception e)
        {
            throw new WebServiceException("Please provide a valid courseId "+e.toString());
        }

        try
        {
            CourseMembership cm = new CourseMembership();
            cm.setCourseId(cid);
            cm.setUserId(uid);
            cm.setIsAvailable(courseMembership.getAvailable());
            cm.setRole(Role.fromFieldName(courseMembership.getRole().name()));
            CourseMembershipDbPersister.Default.getInstance().persist(cm);
        }
        catch(IllegalArgumentException iae)
        {
            throw new WebServiceException("Problem while trying to set role, does role exist? "+iae.toString());
        }
        catch(PersistenceException e)
        {
            throw new WebServiceException("Enrollment may already exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Problem while trying to update coursemembership details "+e.toString());
        }
        return true;
    }

    public static Boolean courseMembershipDelete(BBCourseMembership courseMembership) throws WebServiceException
    {
        Course c = null;
        User u = null;
        CourseMembership cm = null;

        if(Util.checkParam(courseMembership.getCourseMembershipBbId()))
        {
            try
            {
                //get enrollment id
                cm = CourseMembershipDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CourseMembership.DATA_TYPE, courseMembership.getCourseMembershipBbId()));
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: Given courseMembership doesn't seem to exist "+e.getMessage());
            }
        }
        else
        {
            try
            {
                c = CourseDbLoader.Default.getInstance().loadByCourseId(courseMembership.getCourse().getCourseBbId());
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: Course Id is invalid or does not exist "+e.getMessage());
            }

            try
            {
                u = UserDbLoader.Default.getInstance().loadByUserName(courseMembership.getUser().getUserName());
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: User Id is invalid or does not exist "+e.getMessage());
            }

            try
            {
                //get enrollment id
                cm = CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(c.getId(),u.getId());
            }
            catch(Exception e)
            {
                throw new WebServiceException("Error: Given user does not appear to be enrolled on given course "+e.getMessage());
            }
        }

        try
        {
            //then delete
            CourseMembershipDbPersister.Default.getInstance().deleteById(cm.getId());
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error whilst trying to unenroll given user from give course "+e.toString());
        }
        return true;
    }

    public static BBCourseMembership courseMembershipRead(BBCourseMembership courseMembership, BBCourseMembershipVerbosity verbosity, Boolean loadUser) throws WebServiceException
    {
        String error = "";
        try
        {
            if(courseMembership.getUser()!=null && courseMembership.getCourse()!=null)
            {
                if(Util.checkParam(courseMembership.getUser().getBbId()) && Util.checkParam(courseMembership.getCourse().getCourseBbId()))
                {
                    try
                    {
                        //return new BBCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(CourseDbLoader.Default.getInstance().loadByCourseId(courseMembership.getCourse().getCourseBbId()).getId(),UserDbLoader.Default.getInstance().loadByUserName(courseMembership.getUser().getBbId()).getId(),null,true),verbosity);
                        return ObjectConverter.getCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(CourseDbLoader.Default.getInstance().loadByCourseId(courseMembership.getCourse().getCourseBbId()).getId(),UserDbLoader.Default.getInstance().loadByUserName(courseMembership.getUser().getBbId()).getId(),null,true),verbosity);
                    }
                    catch(Exception e)
                    {
                        throw new Exception("null in user or course?");
                    }
                }
            }
            else if(Util.checkParam(courseMembership.getCourseMembershipBbId()))
            {
                try
                {
                    //return new BBCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CourseMembership.DATA_TYPE, courseMembership.getCourseMembershipBbId()),null,true),verbosity);
                    return ObjectConverter.getCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CourseMembership.DATA_TYPE, courseMembership.getCourseMembershipBbId()),null,true),verbosity);

                }
                catch(Exception e)
                {
                    throw new Exception("null in coursemem?");
                }
            }
            error = "You must specify either user with userId and course with courseId, or, a courseMembershipBbId";
        }
        catch(NullPointerException npe)
        {
            throw new WebServiceException("null somewhere else");
        }
        catch(KeyNotFoundException knfe)
        {
            error = "No matching course";
        }
        catch(Exception e)
        {
            error = "Error whilst searching to see if courseMembership exists: "+e.toString();
        }
        throw new WebServiceException(error);
    }

    public static List<BBCourseMembership> courseMembershipReadByCourseId(BBCourse course,BBCourseMembershipVerbosity verbosity, Boolean loadUser)
    {
        try
        {
            if(verbosity == null)
            {
                throw new Exception("You must specify a verbosity level");
            }
            return BBListFactory.getBBCourseMembershipListFromList(CourseMembershipDbLoader.Default.getInstance().loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),null,loadUser),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No course memberships found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBCourseMembership courseMembershipReadByUserIdAndCourseId(BBUser user, BBCourse course,BBCourseMembershipVerbosity verbosity, Boolean loadUser)
    {
        try
        {
            if(verbosity == null)
            {
                throw new Exception("You must specify a verbosity level");
            }
            //return new BBCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId(),null,loadUser),verbosity);
            return ObjectConverter.getCourseMembership(CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId(),UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId(),null,loadUser),verbosity);
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBEnrollment> enrollmentReadByUserId(BBUser user) throws WebServiceException
    {
        try
        {
            Enrollment enrollment = new Enrollment();
            enrollment.setPersonBatchUid(user.getUserName());
            return BBListFactory.getNonVerboseBBList(EnrollmentLoader.Default.getInstance().load(enrollment));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No enrollments found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }
}
