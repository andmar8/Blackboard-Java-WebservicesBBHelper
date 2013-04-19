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
package bbws.util.factory.list;

//bbws
import bbws.entity.enums.verbosity.BBAttemptVerbosity;
import bbws.entity.enums.verbosity.BBCourseMembershipVerbosity;
import bbws.entity.enums.verbosity.BBCourseVerbosity;
import bbws.entity.enums.verbosity.BBLineitemVerbosity;
import bbws.entity.enums.verbosity.BBScoreVerbosity;
import bbws.entity.enums.verbosity.BBUserVerbosity;
import bbws.resource.course.BBCourse;
import bbws.resource.coursemembership.BBCourseMembership;
import bbws.resource.gradecentre.attempt.BBAttempt;
import bbws.resource.gradecentre.column.BBLineitem;
import bbws.resource.gradecentre.grade.BBScore;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.object.BBObjectFactory;
import bbws.util.factory.object.ObjectConverter;

//blackboard - admin
import blackboard.admin.data.user.Person;

//blackboard - data
import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.gradebook.impl.Attempt;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;
import blackboard.data.user.User;

//java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BBListFactory
{
    private static void checkListSize(List l) throws EmptyListException
    {
        if(l==null || l.size()<1)
        {
            throw new EmptyListException();
        }
    }

    public static List getNonVerboseBBList(List l) throws EmptyListException, Exception
    {
        checkListSize(l);
        List rl = new ArrayList();
        Iterator i = l.iterator();
        String type = l.get(0).getClass().getSimpleName();
        while(i.hasNext())
        {
            rl.add(BBObjectFactory.getBBObject(i.next(),type));
        }
        return rl;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBAttempt> getBBAttemptListFromList(List<Attempt> al, BBAttemptVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(al);
        List<BBAttempt> l = new ArrayList<BBAttempt>();
        Iterator<Attempt> i = al.iterator();
        while(i.hasNext())
        {
            //l.add(new BBAttempt(i.next(),verbosity));
            l.add(ObjectConverter.getAttempt(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBCourse> getBBCourseListFromList(List<Course> cl, BBCourseVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(cl);
        List<BBCourse> l = new ArrayList<BBCourse>();
        Iterator<Course> i = cl.iterator();
        while(i.hasNext())
        {
            //l.add(new BBCourse(i.next(),verbosity));
            l.add(ObjectConverter.getCourse(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBCourseMembership> getBBCourseMembershipListFromList(List<CourseMembership> cml, BBCourseMembershipVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(cml);
        List<BBCourseMembership> l = new ArrayList<BBCourseMembership>();
        Iterator<CourseMembership> i = cml.iterator();
        while(i.hasNext())
        {
            //l.add(new BBCourseMembership(i.next(),verbosity));
            l.add(ObjectConverter.getCourseMembership(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBLineitem> getBBLineitemListFromList(List<Lineitem> lil, BBLineitemVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(lil);
        List<BBLineitem> l = new ArrayList<BBLineitem>();
        Iterator<Lineitem> i = lil.iterator();
        while(i.hasNext())
        {
            //l.add(new BBLineitem(i.next(),verbosity));
            l.add(ObjectConverter.getLineitem(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBUser> getBBUserListFromPersonList(List<Person> pl, BBUserVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(pl);
        List<BBUser> l = new ArrayList<BBUser>();
        Iterator<Person> i = pl.iterator();
        while(i.hasNext())
        {
            //l.add(new BBUser((User)i.next(),verbosity));
            l.add(ObjectConverter.getUser(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBUser> getBBUserListFromList(List<User> ul, BBUserVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(ul);
        List<BBUser> l = new ArrayList<BBUser>();
        Iterator<User> i = ul.iterator();
        while(i.hasNext())
        {
            //l.add(new BBUser(i.next(),verbosity));
            l.add(ObjectConverter.getUser(i.next(),verbosity));
        }
        return l;
    }

    /** Change this so it's one method that gets Verbose BBLists, will probably need a "verbosity factory" ***/
    public static List<BBScore> getBBScoreListFromList(List<Score> sl, BBScoreVerbosity verbosity) throws EmptyListException, Exception
    {
        checkListSize(sl);
        List<BBScore> l = new ArrayList<BBScore>();
        Iterator<Score> i = sl.iterator();
        while(i.hasNext())
        {
            //l.add(new BBScore(i.next(),verbosity));
            l.add(ObjectConverter.getScore(i.next(),verbosity));
        }
        return l;
    }
}
