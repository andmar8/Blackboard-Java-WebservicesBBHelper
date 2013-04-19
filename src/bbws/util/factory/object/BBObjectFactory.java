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
package bbws.util.factory.object;

//bbws
import bbws.util.factory.BBObjectsWithoutVerbosity;

//blackboard - admin
import blackboard.admin.data.course.Enrollment;

//blackboard - data
import blackboard.data.announcement.Announcement;
import blackboard.data.calendar.CalendarEntry;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.gradebook.impl.Outcome;
import blackboard.data.gradebook.impl.OutcomeDefinition;

//blackboard - platform
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradeDetail;
import blackboard.platform.gradebook2.GradingSchema;

public class BBObjectFactory
{
    public static Object getBBObject(Object o, String type) throws Exception
    {
        switch(BBObjectsWithoutVerbosity.valueOfSafe(type))
        {
            case Announcement: return ObjectConverter.getAnnouncement((Announcement)o);
            case CalendarEntry: return ObjectConverter.getCalendarEntry((CalendarEntry)o);
            case Enrollment: return ObjectConverter.getEnrollment((Enrollment)o);
            case GradableItem: return ObjectConverter.getGradableItem((GradableItem)o);
            case GradeDetail: return ObjectConverter.getGradeDetail((GradeDetail)o);
            case GradingSchema: return ObjectConverter.getGradingSchema((GradingSchema)o);
            case Group: return ObjectConverter.getGroup((Group)o);
            case GroupMembership: return ObjectConverter.getGroupMembership((GroupMembership)o);
            case Outcome: return ObjectConverter.getOutcome((Outcome)o);
            case OutcomeDefinition: return ObjectConverter.getOutcomeDefinition((OutcomeDefinition)o);
            default: throw new Exception("Type '"+type+"' not found");
        }
    }
}
