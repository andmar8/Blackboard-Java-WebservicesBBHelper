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
import bbws.entity.enums.BBAggregationModel;
import bbws.entity.enums.verbosity.BBAttemptVerbosity;
import bbws.entity.enums.verbosity.BBCourseVerbosity;
import bbws.entity.enums.verbosity.BBCourseMembershipVerbosity;
import bbws.entity.enums.verbosity.BBLineitemVerbosity;
import bbws.entity.enums.verbosity.BBScoreVerbosity;
import bbws.entity.enums.verbosity.BBUserVerbosity;
import bbws.util.Util;
import bbws.resource.announcement.BBAnnouncement;
import bbws.resource.calendar.BBCalendarEntry;
import bbws.resource.content.BBContent;
import bbws.resource.content.BBContentFile;
import bbws.resource.content.BBCourseToc;
import bbws.resource.course.BBCourse;
import bbws.resource.course.BBCourseQuota;
import bbws.resource.course.BBEnrollment;
import bbws.resource.coursemembership.BBCourseMembership;
import bbws.resource.coursemembership.BBCourseMembershipRole;
import bbws.resource.discussionboard.BBConference;
import bbws.resource.discussionboard.BBForum;
import bbws.resource.discussionboard.BBMessage;
import bbws.resource.gradecentre.BBGradeCentreSettings;
import bbws.resource.gradecentre.attempt.BBAttempt;
import bbws.resource.gradecentre.attempt.BBAttemptDetail;
import bbws.resource.gradecentre.column.BBGradableItem;
import bbws.resource.gradecentre.column.BBLineitem;
import bbws.resource.gradecentre.grade.BBGradeDetail;
import bbws.resource.gradecentre.grade.BBGradingSchema;
import bbws.resource.gradecentre.grade.BBScore;
import bbws.resource.gradecentre.outcome.BBOutcome;
import bbws.resource.gradecentre.outcome.BBOutcomeDefinition;
import bbws.resource.groups.BBGroup;
import bbws.resource.groups.BBGroupMembership;
import bbws.resource.user.BBRole;
import bbws.resource.user.BBUser;

//blackboard
import blackboard.admin.data.course.Enrollment;
import blackboard.data.announcement.Announcement;
import blackboard.data.calendar.CalendarEntry;
import blackboard.data.content.Content;
import blackboard.data.content.ContentFile;
import blackboard.data.course.Course;
import blackboard.data.course.CourseQuota;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.discussionboard.Conference;
import blackboard.data.discussionboard.Forum;
import blackboard.data.discussionboard.Message;
import blackboard.data.gradebook.impl.Attempt;
import blackboard.data.gradebook.impl.GradeBookSettings;
import blackboard.data.gradebook.impl.Outcome;
import blackboard.data.gradebook.impl.OutcomeDefinition;
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Lineitem.AssessmentLocation;
import blackboard.data.gradebook.Score;
import blackboard.data.gradebook.Score.AttemptLocation;
import blackboard.data.navigation.CourseToc;
import blackboard.data.role.PortalRole;
import blackboard.data.user.User;

//blackboard - persist
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.role.PortalRoleDbLoader;

//blackboard - platform
import blackboard.platform.gradebook2.AttemptDetail;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradeDetail;
import blackboard.platform.gradebook2.GradingSchema;
import blackboard.platform.security.SystemRole;

public class ObjectConverter
{
    public static BBAnnouncement getAnnouncement(Announcement a)
    {
        BBAnnouncement bba = new BBAnnouncement();
        bba.setAnnouncementBbId(a.getId().toExternalString());
        bba.setBody(a.getBody().getText());
        bba.setEndDate(Util.getDateTimeFromCalendar(a.getRestrictionEndDate()));
        bba.setPermanent(a.getIsPermanent());
        bba.setStartDate(Util.getDateTimeFromCalendar(a.getRestrictionStartDate()));
        bba.setTitle(a.getTitle());
        bba.setType(a.getType().toFieldName());
        return bba;
    }

    /**
     * Creates a BBAttempt from Attempt Object with defaulted BBAttemptVerbosity.EXTENDED
     * @param a
     * @return
     * @throws Exception
     */
    public static BBAttempt getAttempt(Attempt a) throws Exception
    {
        return getAttempt(a,BBAttemptVerbosity.extended);
    }

    public static BBAttempt getAttempt(Attempt a,BBAttemptVerbosity verbosity) throws Exception
    {
        BBAttempt bba = new BBAttempt();
        switch(verbosity)
        {
            case extended:
                //bba.setDataType(a.getDataType().getName());
                bba.setInstructorComments(a.getInstructorComments());
                bba.setInstructorNotes(a.getInstructorNotes());
                bba.setLinkRefBbId(a.getLinkRefId());
                bba.setPublicComments(a.getCommentIsPublic());
                try{bba.setResultObjectBbId(a.getResultObjectId().getExternalString());}catch(Exception e){bba.setResultObjectBbId("");}
                bba.setStudentComments(a.getStudentComments());
            case standard:
                bba.setAttemptBbId(a.getId().getExternalString());
                bba.setAttemptedDate(Util.getDateTimeFromCalendar(a.getAttemptedDate()));
                bba.setDateCreated(Util.getDateTimeFromCalendar(a.getDateCreated()));
                bba.setDateModified(Util.getDateTimeFromCalendar(a.getDateModified()));
                bba.setGrade(a.getGrade());
                bba.setOutcomeBbId(a.getOutcomeId().getExternalString());
                bba.setScore(a.getScore());
                bba.setStatus(a.getStatus().getDisplayName());
                break;
            default: throw new Exception("Undefined verbosity of Attempt");
        }
        return bba;
    }

    /**
     * Creates a BBAttemptDetail from AttemptDetail Object with defaulted BBAttemptVerbosity.EXTENDED
     * @param a
     * @return
     * @throws Exception
     */
    public static BBAttemptDetail getAttemptDetail(AttemptDetail a) throws Exception
    {
        return getAttemptDetail(a,BBAttemptVerbosity.extended);
    }

    public static BBAttemptDetail getAttemptDetail(AttemptDetail a,BBAttemptVerbosity verbosity) throws Exception
    {
        BBAttemptDetail bbad = new BBAttemptDetail();
        switch(verbosity)
        {
            case extended:
                bbad.setAttemptedDate(Util.getDateTimeFromCalendar(a.getAttemptDate()));
                //try{a.setAttemptedDate(getDateTimeFromCalendar((Calendar)a.getClass().getDeclaredMethod("getAttemptDate",new Class[]{}).invoke(a, new Object[]{}));}//Version 9.1 call
                //9.1.40071.3 on
                //catch(Exception e){*/bba.setAttemptedDate(Util.getDateTimeFromCalendar(a.getAttemptDate()));}//User version 8 call
                bbad.setDisplayGrade(a.getDisplayGrade());
                bbad.setFormattedAttemptedDate(a.getFormattedAttemptDate());
                bbad.setFormattedDateCreated(a.getFormattedCreateDate());
                bbad.setPublicComments(a.isPublicFeedbackToUser());
                bbad.setShortFeedBackToUser(a.getShortFeedbackToUser());
            case standard:
                bbad.setAttemptBbId(a.getId().getExternalString());
                bbad.setId(a.getId().getExternalString());
                bbad.setDateCreated(Util.getDateTimeFromCalendar(a.getCreationDate()));
                bbad.setExempt(a.isExempt());
                //9.1.40071.3 on
                bbad.setFeedBackToUser(a.getFeedBackToUser().getFormattedText());
                bbad.setFeedBackToUserHidden(a.isFeedbackToUserHidden());
                bbad.setGrade(a.getGrade());
                bbad.setGradeId(a.getGradeId().getExternalString());
                //9.1.40071.3 on
                bbad.setInstructorNotes(a.getInstructorNotes().getFormattedText());
                bbad.setScore(a.getScore());
                bbad.setStatus(a.getStatus().name());
                break;
            default: throw new Exception("Undefined verbosity of AttemptDetail");
        }
        return bbad;
    }

    public static BBCalendarEntry getCalendarEntry(CalendarEntry ce)
    {
        BBCalendarEntry bbce = new BBCalendarEntry();
	bbce.setCalendarEntryBbId(ce.getId().toExternalString());
        bbce.setCourseBbId(ce.getCourseId().toExternalString());
	bbce.setDescription(ce.getDescription().getText());
	bbce.setEndDateTime(Util.getDateTimeFromCalendar(ce.getEndDate()));
	bbce.setExternalType(ce.getType().toFieldName());
	bbce.setStartDateTime(Util.getDateTimeFromCalendar(ce.getStartDate()));
        bbce.setTitle(ce.getTitle());
        return bbce;
    }

    public static BBConference getConference(Conference c)
    {
        BBConference bbc = new BBConference();
	bbc.setAvailable(c.getIsAvailable());
	bbc.setConferenceBbId(c.getId().toExternalString());
	bbc.setDescription(c.getDescription().getText());
	bbc.setPosition(c.getPosition());
	bbc.setTitle(c.getTitle());
        return bbc;
    }

    public static BBContent getContent(Content c)
    {
        BBContent bbc = new BBContent();
	bbc.setAvailable(c.getIsAvailable());
	bbc.setBody(c.getBody().getText());
	bbc.setContentBbId(c.getId().toExternalString());
	bbc.setContentHandler(c.getContentHandler());
	bbc.setDataType(c.getDataType().getName());
	bbc.setDescribed(c.getIsDescribed());
	bbc.setEndDate(Util.getDateTimeFromCalendar(c.getEndDate()));
	bbc.setFolder(c.getIsFolder());
	bbc.setLesson(c.getIsLesson());
	bbc.setModifiedDate(Util.getDateTimeFromCalendar(c.getModifiedDate()));
	bbc.setNumOfFiles(c.getContentFiles().size());
	bbc.setOfflineName(c.getOfflineName());
	bbc.setOfflinePath(c.getOfflinePath());
	bbc.setParentContentBbId(c.getParentId().toExternalString());
	bbc.setPersistentTitle(c.getPersistentTitle());
	bbc.setPosition(c.getPosition());
	bbc.setRenderType(c.getRenderType().toFieldName());
	bbc.setStartDate(Util.getDateTimeFromCalendar(c.getStartDate()));
	bbc.setTitle(c.getTitle());
	bbc.setTitleColour(c.getTitleColor());
	bbc.setUrl(c.getUrl());
        return bbc;
    }

    public static BBContentFile getContentFile(ContentFile cf)
    {
        BBContentFile bbcf = new BBContentFile();
	bbcf.setAction(cf.getAction().toFieldName());
	bbcf.setContentFileBbId(cf.getId().toExternalString());
	bbcf.setDataType(cf.getDataType().getName());
	bbcf.setLinkName(cf.getLinkName());
	bbcf.setModifiedDate(Util.getDateTimeFromCalendar(cf.getModifiedDate()));
	bbcf.setName(cf.getName());
	bbcf.setSize(cf.getSize());
	bbcf.setStorageType(cf.getStorageType().toFieldName());
        return bbcf;
    }

    /**
     * Creates a BBCourse from Course Object with defaulted BBCourseVerbosity.EXTENDED
     * @param course
     * @return
     * @throws Exception
     */
    public static BBCourse getCourse(Course course) throws Exception
    {
        return getCourse(course,BBCourseVerbosity.extended);
    }

    public static BBCourse getCourse(Course course,BBCourseVerbosity verbosity) throws Exception
    {
        BBCourse bbc = new BBCourse();

        switch(verbosity)
        {
            case extended:
                bbc.setAbsoluteLimit(Long.toString(course.getAbsoluteLimit()));
                bbc.setAllowGuests(course.getAllowGuests());
                bbc.setAllowObservers(course.getAllowObservers());
                try{bbc.setBannerImageFile(course.getBannerImageFile().getPath());}catch(Exception e){bbc.setBannerImageFile("");}
                bbc.setBatchUId(course.getBatchUid());
                try{bbc.setButtonStyle(course.getButtonStyle().getDescription());}catch(Exception e){bbc.setButtonStyle("");}
                try{bbc.setCartridgeDescription(course.getCartridge().getDescription());}catch(Exception e){bbc.setCartridgeDescription("");}
                try{bbc.setClassification(course.getClassification().getTitle());}catch(Exception e){bbc.setClassification("");}
                bbc.setDurationType(course.getDurationType().toFieldName());
                bbc.setEndDate(Util.getDateTimeFromCalendar(course.getEndDate()));
                bbc.setEnrollment(course.getEnrollmentType().toFieldName());
                bbc.setInstitution(course.getInstitutionName());
                bbc.setLocaleEnforced(course.getIsLocaleEnforced());
                bbc.setLockedOut(course.getIsLockedOut());
                bbc.setNavigationCollapsible(course.getIsNavCollapsible());
                bbc.setLocale(course.getLocale());
                bbc.setNavigationBackgroundColour(course.getNavColorBg());
                bbc.setNavigationForegroundColour(course.getNavColorFg());
                bbc.setNavigationStyle(course.getNavStyle().toFieldName());
                bbc.setNumberOfDaysOfUse(course.getNumDaysOfUse());
                bbc.setPaceType(course.getPaceType().toFieldName());
                bbc.setServiceLevelType(course.getServiceLevelType().toFieldName());
                bbc.setSoftLimit(Long.toString(course.getSoftLimit()));
                bbc.setStartDate(Util.getDateTimeFromCalendar(course.getStartDate()));
                bbc.setUploadLimit(Long.toString(course.getUploadLimit()));
            case standard:
                bbc.setCourseBbId(course.getId().toExternalString());
                bbc.setTitle(course.getTitle());
                bbc.setDescription(course.getDescription());
                bbc.setCreationDate(Util.getDateTimeFromCalendar(course.getCreatedDate()));
                bbc.setModifiedDate(Util.getDateTimeFromCalendar(course.getModifiedDate()));
                bbc.setAvailable(course.getIsAvailable());
            case minimal:
                bbc.setCourseId(course.getCourseId());
                break;
            default: throw new Exception("Undefined verbosity of course");
        }
        return bbc;
    }

    public static BBCourseMembership getCourseMembership(CourseMembership cm) throws Exception
    {
        return getCourseMembership(cm,BBCourseMembershipVerbosity.standard);
    }

    public static BBCourseMembership getCourseMembership(CourseMembership cm, BBCourseMembershipVerbosity verbosity) throws Exception
    {
        BBCourseMembership bbcm = new BBCourseMembership();
        switch(verbosity)
        {
            case standard:
                bbcm.setAvailable(cm.getIsAvailable());
                bbcm.setCartridgeAccess(cm.getHasCartridgeAccess());
                bbcm.setCourseBbId(cm.getCourseId().toExternalString());
                bbcm.setDataSourceBbId(cm.getDataSourceId().toExternalString());
                bbcm.setEnrollmentDate(Util.getDateTimeFromCalendar(cm.getEnrollmentDate()));
                bbcm.setIntroduction(cm.getIntroduction());
                bbcm.setLastAccessDate(Util.getDateTimeFromCalendar(cm.getLastAccessDate()));
                bbcm.setModifiedDate(Util.getDateTimeFromCalendar(cm.getModifiedDate()));
                bbcm.setNotes(cm.getNotes());
                bbcm.setPersonalInfo(cm.getPersonalInfo());
                bbcm.setRole(BBCourseMembershipRole.valueOfSafe(cm.getRole().toFieldName()));
                bbcm.setUserBbId(cm.getUserId().toExternalString());
                if(cm.getUser()!=null)
                {
                    //bbcm.setUser(new BBUser(cm.getUser(),BBUserVerbosity.extended));
                    bbcm.setUser(getUser(cm.getUser()));
                }
            case minimal:
                bbcm.setCourseMembershipBbId(cm.getId().toExternalString());
                break;
            default: throw new Exception("Undefined verbosity of course membership");
        }
        return bbcm;
    }

    public static BBCourseToc getCourseToc(CourseToc ct)
    {
        BBCourseToc bbct = new BBCourseToc();
	bbct.setAvailable(ct.getIsEnabled());
	bbct.setContentBbId(ct.getContentId().toExternalString());
	bbct.setDataType(ct.getDataType().toString());
	bbct.setEntryPoint(ct.getIsEntryPoint());
	bbct.setInternalHandle(ct.getInternalHandle());
	bbct.setLabel(ct.getLabel());
	bbct.setPosition(ct.getPosition());
	bbct.setTargetType(ct.getTargetType().toFieldName());
	bbct.setUrl(ct.getUrl());
        return bbct;
    }

    public static BBCourseQuota getCourseQuota(CourseQuota courseQuota)
    {
        BBCourseQuota bbcq = new BBCourseQuota();
	bbcq.setCourseAbsoluteLimit(courseQuota.getCourseAbsoluteLimit());
	bbcq.setCourseAbsoluteLimit(courseQuota.getCourseAbsoluteLimitRemainingSize());
	bbcq.setCourseSize(courseQuota.getCourseSize());
	bbcq.setCourseSoftLimit(courseQuota.getCourseSoftLimit());
	bbcq.setCourseUploadLimit(courseQuota.getCourseUploadLimit());
	bbcq.setEnforceQuota(courseQuota.getEnforceQuota());
	bbcq.setEnforceUploadLimit(courseQuota.getEnforceUploadLimit());
	bbcq.setSystemAbsoluteLimit(courseQuota.getSystemAbsoluteLimit());
	bbcq.setSystemSoftLimit(courseQuota.getSystemSoftLimit());
	bbcq.setSystemUploadLimit(courseQuota.getSystemUploadLimit());
        return bbcq;
    }

    public static BBEnrollment getEnrollment(Enrollment enr)
    {
        BBEnrollment bbe = new BBEnrollment();
        bbe.setAvailable(enr.getIsAvailable());
        bbe.setCreationDate(Util.getDateTimeFromCalendar(enr.getEnrollmentDate()));
        bbe.setCourseId(enr.getCourseSiteBatchUid());
        bbe.setUserRole(enr.getRole().toFieldName());
        return bbe;
    }

    public static BBForum getForum(Forum f)
    {
        BBForum bbf = new BBForum();
	bbf.setAvailable(f.getIsAvailable());
	bbf.setDescription(f.getDescription().getText());
	bbf.setForumBbId(f.getId().toExternalString());
	bbf.setPosition(f.getPosition());
	bbf.setTitle(f.getTitle());
        return bbf;
    }

    public static BBGradableItem getGradableItem(GradableItem gi)
    {
        BBGradableItem bbgi = new BBGradableItem();
        try{bbgi.setAggregationModel(BBAggregationModel.valueOfSafe(gi.getAggregationModel().name()));}catch(Exception e){bbgi.setAggregationModel(null);}
        try
        {
            Object o = gi.getAssessmentId();
            if(o!=null)
            {
                bbgi.setAssessmentId(o.getClass().getName());
                if(bbgi.getAssessmentId().equalsIgnoreCase("java.lang.String")){bbgi.setAssessmentId(o.toString());}
                else if(bbgi.getAssessmentId().equalsIgnoreCase("blackboard.persist.Id")){bbgi.setAssessmentId(((blackboard.persist.Id)o).getExternalString());}
                else if(bbgi.getAssessmentId().equalsIgnoreCase("blackboard.persist.PkId")){bbgi.setAssessmentId(((blackboard.persist.PkId)o).getExternalString());}
            }
        }
        catch(Exception e)
        {
            bbgi.setAssessmentId("");
        }
        bbgi.setCalculated(gi.isCalculated());
        bbgi.setCalculatedInd(gi.getCalculatedInd().toString());
        bbgi.setCategory(gi.getCategory());
        try{bbgi.setCategoryId(gi.getCategoryId().toExternalString());}catch(Exception e){bbgi.setCategoryId("");}
        try{bbgi.setCourseContentId(gi.getCourseContentId().toExternalString());}catch(NullPointerException npe){bbgi.setCourseContentId("");}
        bbgi.setCourseId(gi.getCourseId().toExternalString());
        bbgi.setDateAdded(Util.getDateTimeFromCalendar(gi.getDateAdded()));
        bbgi.setDateModified(Util.getDateTimeFromCalendar(gi.getDateModified()));
        bbgi.setDeleted(gi.isDeleted());
        try{bbgi.setDescription(gi.getDescription().getText());}catch(NullPointerException npe){bbgi.setDescription("");}
        bbgi.setDescriptionForDisplay(gi.getDescriptionForDisplay().getText());
        bbgi.setDisplayColumnName(gi.getDisplayColumnName());
        bbgi.setDisplayPoints(gi.getDisplayPoints());
        bbgi.setDisplayTitle(gi.getDisplayTitle());
        bbgi.setDueDate(Util.getDateTimeFromCalendar(gi.getDueDate()));
        bbgi.setExternalAnalysisUrl(gi.getExternalAnalysisUrl());
        bbgi.setExternalAttemptHandlerUrl(gi.getExternalAttemptHandlerUrl());
        bbgi.setExternalId(gi.getExternalId());
        bbgi.setFormattedDateAdded(gi.getFormattedDateAdded());
        bbgi.setFormattedDueDate(gi.getFormattedDueDate());
        //V9.1 doesn't like this call?
        //bbgi.setgradeItem(gi.isGradeItem());
        try{bbgi.setGradingPeriodId(gi.getGradingPeriodId().toExternalString());}catch(NullPointerException npe){bbgi.setGradingPeriodId("");}
        try{bbgi.setGradingSchema(gi.getGradingSchema().getTitle());}catch(NullPointerException npe){bbgi.setGradingSchema("");}
        bbgi.setGradingSchemaId(gi.getGradingSchemaId().toExternalString());
        bbgi.setHideAttempt(gi.isHideAttempt());
        bbgi.setId(gi.getId().toExternalString());
        bbgi.setLinkId(gi.getLinkId());
        bbgi.setManual(gi.isManual());
        bbgi.setPoints(gi.getPoints());
        bbgi.setPosition(gi.getPosition());
        //bbgi.setschemaValue(gi.getSchemaValue(score); //WTH is the parameter for?
        bbgi.setScorable(gi.isScorable());
        //bbgi.setscore(gi.getScore(""); //Another weird parameter?
        try{bbgi.setScoreProvider(gi.getScoreProvider().getName());}catch(NullPointerException npe){bbgi.setScoreProvider("");}
        bbgi.setScoreProviderHandle(gi.getScoreProviderHandle());
        try{bbgi.setSecondaryGradingSchemaId(gi.getSecondaryGradingSchemaId().toExternalString());}catch(NullPointerException npe){bbgi.setSecondaryGradingSchemaId("");}
        bbgi.setShowStatsToStudent(gi.isShowStatsToStudent());
        bbgi.setSingleAttempt(gi.isSingleAttempt());
        bbgi.setTitle(gi.getTitle());
        bbgi.setUsedInCalculation(gi.isUsedInCalculation());
        bbgi.setVersion(gi.getVersion());
        bbgi.setVisibleInAllTerms(gi.isVisibleInAllTerms());
        bbgi.setVisibleInBook(gi.isVisibleInBook());
        bbgi.setVisibleToStudents(gi.isVisibleToStudents());
        //V9.1 doesn't like this call?
        //bbgi.setWeight(gi.getWeight());
        return bbgi;
    }

    public static BBGradeCentreSettings getGradeCentreSettings(GradeBookSettings gbs)
    {
        BBGradeCentreSettings bbgcs = new BBGradeCentreSettings();
        bbgcs.setAverageDisplayed(gbs.isAverageDisplayed());
        bbgcs.setCommentsDisplayed(gbs.areCommentsDisplayed());
        bbgcs.setFirstLastDisplayed(gbs.isFirstLastDisplayed());
        bbgcs.setGradeBookSettingBbId(gbs.getId().toExternalString());
        bbgcs.setLastFirstDisplayed(gbs.isLastFirstDisplayed());
        bbgcs.setStudentIdDisplayed(gbs.isStudentIdDisplayed());
        bbgcs.setUserIdDisplayed(gbs.isUserIdDisplayed());
        bbgcs.setWeightType(gbs.getWeightType().toFieldName());
        return bbgcs;
    }

    public static BBGradeDetail getGradeDetail(GradeDetail gd)
    {
        BBGradeDetail bbgd = new BBGradeDetail();
        //bbgd.setAttempts = BBListFactory --> gd.getAttempts());
        bbgd.setAverageScore(gd.getAverageScore());
        bbgd.setCalculatedGrade(gd.getCalculatedGrade());
        //Is this the courseMembership Id?
        try{bbgd.setCourseUserId(gd.getCourseUserId().toExternalString());}catch(Exception e){bbgd.setCourseUserId("");}
        bbgd.setExempt(gd.isExempt());
        try{bbgd.setFirstAttemptId(gd.getFirstAttemptId().toExternalString());}catch(Exception e){bbgd.setFirstAttemptId("");}
        //Not neccesary surely?
        //bbgd.setgradableItem(new BBGradableItem(gd.getGradableItem());
        try{bbgd.setGradableItemId(gd.getGradableItemId().toExternalString());}catch(Exception e){bbgd.setGradableItemId("");}
        bbgd.setGrade(gd.getGrade());
        bbgd.setGradeStatusKey(gd.getGradeStatusKey());
        bbgd.setGradingRequired(gd.isGradingRequired());
        try{bbgd.setHighestAttemptId(gd.getHighestAttemptId().toExternalString());}catch(Exception e){bbgd.setHighestAttemptId("");}
        //Could be possible, but is it needed? - maaaybeeeeeee... :-/
        //bbgd.sethistory(gd.getHistory());
        try{bbgd.setId(gd.getId().toExternalString());}catch(Exception e){bbgd.setId("");}
        //9.1.40071.3 on
        bbgd.setInstructorComments(gd.getInstructorComments().getFormattedText());
        try{bbgd.setLastAttemptId(gd.getLastAttemptId().toExternalString());}catch(Exception e){bbgd.setLastAttemptId("");}
        try{bbgd.setLowestAttemptId(gd.getLowestAttemptId().toExternalString());}catch(Exception e){bbgd.setLowestAttemptId("");}
        //This is not in v9.1
        //bbgd.setmanualChangeIndicator(gd.isManualChangeIndicator());
        bbgd.setManualGrade(gd.getManualGrade());
        bbgd.setManualScore(gd.getManualScore());
        bbgd.setNullGrade(gd.isNullGrade());
        bbgd.setShortInstructorComments(gd.getShortInstructorComments());
        bbgd.setShortStudentComments(gd.getShortStudentComments());
        //9.1.40071.3 on
        bbgd.setStudentComments(gd.getStudentComments().getFormattedText());
        return bbgd;
    }

    public static BBGradingSchema getGradingSchema(GradingSchema gs)
    {
        BBGradingSchema bbgs = new BBGradingSchema();
        bbgs.setCanRemove(gs.getCanRemove());
        bbgs.setCourseId(gs.getCourseId().toExternalString());
        bbgs.setGradingSchemaId(gs.getGradingSchemaId().toExternalString());
        //bbg.setid(gs.getId().toExternalString());
        bbgs.setInUse(gs.isInuse());
        bbgs.setLocalisedTitle(gs.getLocalizedTitle());
        bbgs.setNumeric(gs.isNumeric());
        bbgs.setPercentage(gs.isPercentage());
        bbgs.setScaleType(gs.getScaleType().name());
        //gs.getSchemaValue(Double.NaN, pointsPossible); //?
        //gs.getScore(courseId, pointsPossible); //?
        //gs.getSymbols()); //?
        bbgs.setTabular(gs.isTabular());
        bbgs.setTitle(gs.getTitle());
        bbgs.setUserDefined(gs.isUserDefined());
        bbgs.setVersion(gs.getVersion());
        return bbgs;
    }

    public static BBGroup getGroup(Group g)
    {
        BBGroup bbg = new BBGroup();
	bbg.setAvailable(g.getIsAvailable());
	bbg.setChatRoomsAvailable(g.getIsChatRoomAvailable());
	bbg.setDescription(g.getDescription().getText());
	bbg.setDiscussionBoardsAvailable(g.getIsDiscussionBoardAvailable());
	bbg.setEmailAvailable(g.getIsEmailAvailable());
	bbg.setGroupBbId(g.getId().toExternalString());
	bbg.setModifiedDate(Util.getDateTimeFromCalendar(g.getModifiedDate()));
	bbg.setTitle(g.getTitle());
	bbg.setTransferAreaAvailable(g.getIsTransferAreaAvailable());
        return bbg;
    }

    public static BBGroupMembership getGroupMembership(GroupMembership gm) throws Exception
    {
        BBGroupMembership bbgm = new BBGroupMembership();
        bbgm.setUserId(CourseMembershipDbLoader.Default.getInstance().loadById(gm.getCourseMembershipId(),null,true).getUser().getUserName());
        return bbgm;
    }

    public static BBLineitem getLineitem(Lineitem li) throws Exception
    {
        return getLineitem(li,BBLineitemVerbosity.WithoutScores);
    }

    public static BBLineitem getLineitem(Lineitem li,BBLineitemVerbosity verbosity) throws Exception
    {
        BBLineitem bbli = new BBLineitem();
        switch(verbosity)
        {
            case WithScores:
                /*try
                {
                    bbli.setScores(bbws.util.factory.list.BBListFactory.getBBScoreListFromList(li.getScores(),bbws.gradecentre.grade.BBScore.BBScoreVerbosity.extended);
                }
                catch(Exception e)
                {
                    bbli.setScores(null;
                }*/
            case WithoutScores:
                Object o = li.getAssessmentId();
                if(o!=null)
                {
                    bbli.setAssessmentBbId(o.getClass().getName());
                    if(bbli.getAssessmentBbId().equalsIgnoreCase("java.lang.String"))
                    {
                        bbli.setAssessmentBbId(o.toString());
                    }
                    else if(bbli.getAssessmentBbId().equalsIgnoreCase("blackboard.persist.Id"))
                    {
                        bbli.setAssessmentBbId(((blackboard.persist.Id)o).getExternalString());
                    }
                }
                if(li.getAssessmentLocation().equals(AssessmentLocation.EXTERNAL)){bbli.setAssessmentLocation("EXTERNAL");}
                else if(li.getAssessmentLocation().equals(AssessmentLocation.INTERNAL)){bbli.setAssessmentLocation("INTERNAL");}
                else if(li.getAssessmentLocation().equals(AssessmentLocation.UNSET)){bbli.setAssessmentLocation("UNSET");}
                bbli.setAvailable(li.getIsAvailable());
                bbli.setColumnPosition(li.getColumnOrder());
                bbli.setDateAdded(Util.getDateTimeFromCalendar(li.getDateAdded()));
                bbli.setDateChanged(Util.getDateTimeFromCalendar(li.getDateChanged()));
                bbli.setLineItemBbId(li.getId().toExternalString());
                bbli.setName(li.getName());
                bbli.setOutcomeDefBbId(li.getOutcomeDefinition().getId().toExternalString());
                bbli.setPointsPossible(li.getPointsPossible());
                bbli.setType(li.getType());
                bbli.setWeight(li.getWeight());
                break;
            default: throw new Exception("Undefined verbosity of line item");
        }
        return bbli;
    }

    public static BBOutcome getOutcome(Outcome o)
    {
        BBOutcome bbo = new BBOutcome();
        bbo.setAverageGrade(o.getAverageGrade(false));
        try{bbo.setAverageScore(o.getAverageScore());}catch(Exception e){bbo.setAverageScore(new Float(0));}
        bbo.setCourseMembershipBbId(o.getCourseMembershipId().toExternalString());
        try{bbo.setFirstAttemptBbId(o.getFirstAttemptId().toExternalString());}catch(Exception e){bbo.setFirstAttemptBbId("");}
        bbo.setGrade(o.getGrade());
        bbo.setGradebookStatus(o.getGradebookStatus().getDisplayName());
        try{bbo.setHighestAttemptBbId(o.getHighestAttemptId().toExternalString());}catch(Exception e){bbo.setHighestAttemptBbId("");}
        bbo.setInstructorComments(o.getInstructorComments());
        try{bbo.setLastAttemptBbId(o.getLastAttemptId().toExternalString());}catch(Exception e){bbo.setLastAttemptBbId("");}
        try{bbo.setLowestAttemptBbId(o.getLowestAttemptId().toExternalString());}catch(Exception e){bbo.setLowestAttemptBbId("");}
        bbo.setManualGrade(o.getManualGrade());
        try{bbo.setManualScore(o.getManualScore());}catch(Exception e){bbo.setManualScore(new Float(0));}
        bbo.setOutcomeBbId(o.getId().toExternalString());
        bbo.setOutcomeDefinitionBbId(o.getOutcomeDefinitionId().toExternalString());
        try{bbo.setScore(o.getScore());}catch(Exception e){bbo.setScore(new Float(0));}
        bbo.setStudentComments(o.getStudentComments());
        try{bbo.setTotalScore(o.totalScore());}catch(Exception e){bbo.setTotalScore(new Float(0));}
        return bbo;
    }

    public static BBOutcomeDefinition getOutcomeDefinition(OutcomeDefinition od)
    {
        BBOutcomeDefinition bbod = new BBOutcomeDefinition();
        try{bbod.setAggregationModel(BBAggregationModel.valueOfSafe(od.getAggregationModel().toFieldName()));}catch(Exception e){bbod.setAggregationModel(null);}
        bbod.setAnalysisUrl(od.getAnalysisUrl());
        try{bbod.setAsiDataBbId(od.getAsiDataId().toExternalString());}catch(Exception e){bbod.setAsiDataBbId("");}
        bbod.setCalculated(od.isCalculated());
        try{bbod.setCalculationType(od.getCalculationType().toFieldName());}catch(Exception e){bbod.setCalculationType("");}
        try{bbod.setCategory(od.getCategory().getDescription());}catch(Exception e){bbod.setCategory("");}
        try{bbod.setCategoryId(od.getCategoryId().toExternalString());}catch(Exception e){bbod.setCategoryId("");}
        try{bbod.setContentId(od.getContentId().toExternalString());}catch(Exception e){bbod.setContentId("");}
        bbod.setCourseId(od.getCourseId().toExternalString());
        bbod.setDateAdded(Util.getDateTimeFromCalendar(od.getDateAdded()));
        bbod.setDateCreated(Util.getDateTimeFromCalendar(od.getDateCreated()));
        bbod.setDateModified(Util.getDateTimeFromCalendar(od.getDateModified()));
        bbod.setDescription(od.getDescription());
        bbod.setDueDate(Util.getDateTimeFromCalendar(od.getDueDate()));
        bbod.setDueDateInUse(od.isDueDateInUse());
        bbod.setHandlerUrl(od.getHandlerUrl());
        bbod.setHideAttempt(od.getHideAttempt());
        bbod.setId(od.getId().toExternalString());
        bbod.setIgnoreUnscoredAttempts(od.isIgnoreUnscoredAttempts());
        bbod.setLinkId(od.getLinkId());
        bbod.setNumOfOutcomes(od.getOutcomeCount());
        //od.getOutcomes());
        //od.getOutcomes(onlyStudents);
        bbod.setOutcomeDefinitionBbId(od.getId().getExternalString());
        bbod.setPersistentDescription(od.getPersistentDescription());
        bbod.setPersistentTitle(od.getPersistentTitle());
        bbod.setPosition(od.getPosition());
        bbod.setPossible(od.getPossible());
        //od.getScale());
        //od.getScaleId());
        bbod.setScorable(od.isScorable());
        bbod.setScoreProviderHandle(od.getScoreProviderHandle());
        bbod.setSimpleDateCreated(od.getSimpleDateCreated());
        bbod.setSimpleDueDate(od.getSimpleDueDate());
        bbod.setTitle(od.getTitle());
        bbod.setTotal(od.isTotal());
        bbod.setVisible(od.isVisible());
        bbod.setWeight(od.getWeight());
        bbod.setWeightedTotal(od.isWeightedTotal());
        return bbod;
    }

    public static BBMessage getMessage(Message m)
    {
        BBMessage bbm = new BBMessage();
	bbm.setForumBbId(m.getForumId().toExternalString());
	bbm.setMessageBbId(m.getId().toExternalString());
	bbm.setParentMessageBbId(m.getParentId().toExternalString());
	bbm.setSubject(m.getSubject());
	bbm.setText(m.getBody().getText());
        return bbm;
    }

    public static BBRole getRole(PortalRole pr)
    {
        BBRole bbr = new BBRole();
	bbr.setRoleId(pr.getRoleID());
	bbr.setRoleName(pr.getRoleName());
        return bbr;
    }

    public static BBRole getRole(SystemRole sr)
    {
        BBRole bbr = new BBRole();
        bbr.setRoleId(sr.getIdentifier());
	bbr.setRoleName(sr.getName());
        return bbr;
    }

    public static BBRole getRole(CourseMembership cm)
    {
        BBRole bbr = new BBRole();
        bbr.setRoleName(cm.getRole().toFieldName());
        return bbr;
    }

    public static BBScore getScore(Score s) throws Exception
    {
        return getScore(s,BBScoreVerbosity.extended);
    }

    public static BBScore getScore(Score s,BBScoreVerbosity verbosity) throws Exception
    {
        BBScore bbs = new BBScore();
        switch(verbosity)
        {
            case extended:
                bbs.setAttemptBbId("");
                Object o = s.getAttemptId();
                if(o!=null)
                {
                    if(o.getClass().getName().equalsIgnoreCase("java.lang.String"))
                    {
                        bbs.setAttemptBbId(o.toString());
                    }
                    //Need to test
                    /*if(attmptId=="blackboard.persist.Id")
                    {
                    scr[5] = ((Id)o).getExternalString();
                    }*/
                }
                AttemptLocation al = s.getAttemptLocation();
                if(al.equals(AttemptLocation.EXTERNAL))	{bbs.setAttemptLocation("EXTERNAL");}
                else if(al.equals(AttemptLocation.INTERNAL)){bbs.setAttemptLocation("INTERNAL");}
                else{bbs.setAttemptLocation("UNSET");}
                bbs.setCourseMembershipBbId(s.getCourseMembershipId().toExternalString());
                bbs.setDataType(s.getDataType().getName());
                bbs.setLineItemBbId(s.getLineitemId().toExternalString());
            case standard:
                bbs.setDateAdded(Util.getDateTimeFromCalendar(s.getDateAdded()));
                bbs.setDateChanged(Util.getDateTimeFromCalendar(s.getDateChanged()));
                bbs.setDateModified(Util.getDateTimeFromCalendar(s.getModifiedDate()));
                bbs.setGrade(s.getGrade());
                bbs.setOutcomeDefBbId(s.getOutcome().getOutcomeDefinitionId().getExternalString());
                bbs.setScoreBbId(s.getId().getExternalString());
                break;
            default: throw new Exception("Undefined verbosity of score");
        }
        return bbs;
    }

    public static BBUser getUser(User u) throws Exception
    {
        return getUser(u,BBUserVerbosity.extended);
    }

    public static BBUser getUser(User u, BBUserVerbosity verbosity) throws Exception
    {
        BBUser bbu = new BBUser();
	switch(verbosity)
	{
	    case extended:
		bbu.setBatchUserBbId(u.getBatchUid());
		bbu.setDataSourceBbId(u.getDataSourceId().toExternalString());
		bbu.setUserName(u.getUserName());
		bbu.setStudentId(u.getStudentId());
		bbu.setTitle(u.getTitle());
		bbu.setSystemRoleId(u.getSystemRoleIdentifier());
		bbu.setPortalRoleId(u.getPortalRoleId().toExternalString());
		bbu.setGender(u.getGender().toFieldName());
		bbu.setBirthDate(Util.getDateTimeFromCalendar(u.getBirthDate()));
		bbu.setEducationLevel( u.getEducationLevel().toFieldName());
		bbu.setJobTitle(u.getJobTitle());
		bbu.setCompany(u.getCompany());
		bbu.setDepartment(u.getDepartment());
		bbu.setStreet1(u.getStreet1());
		bbu.setStreet2(u.getStreet2());
		bbu.setCity(u.getCity());
		bbu.setCounty(u.getState());
		bbu.setCountry(u.getCountry());
		bbu.setPostCode(u.getZipCode());
		bbu.setBusinessPhone1(u.getBusinessPhone1());
		bbu.setBusinessPhone2(u.getBusinessPhone2());
		bbu.setMobilePhone(u.getMobilePhone());
		bbu.setHomePhone1(u.getHomePhone1());
		bbu.setHomePhone2(u.getHomePhone2());
		bbu.setBusinessFax(u.getBusinessFax());
		bbu.setHomeFax(u.getHomeFax());
		bbu.setWebPage(u.getWebPage());
		bbu.setCardNumber(u.getCardNumber());
		bbu.setCdROMDriveMac(u.getCDRomDriveMac());
		bbu.setCdROMDrivePC(u.getCDRomDrivePC());
		//user.getCreatedDate()
		bbu.setShowAddContactInfo(u.getShowAddContactInfo());
		bbu.setShowAddressInfo(u.getShowAddressInfo());
		bbu.setShowEmailInfo(u.getShowEmailInfo());
		bbu.setShowWorkInfo(u.getShowWorkInfo());
		bbu.setIsAvailable(u.getIsAvailable());
		bbu.setIsInfoPublic(u.getIsInfoPublic());
		bbu.setModifiedDate(Util.getDateTimeFromCalendar(u.getModifiedDate()));
		bbu.setLocale(u.getLocale());
                bbu.setPassword(u.getPassword());
                bbu.setStateOrProvince(u.getState());
	    case standard:
		bbu.setBbId(u.getId().toExternalString());
		bbu.setGivenName(u.getGivenName());
		bbu.setMiddleName(u.getMiddleName());
		bbu.setFamilyName(u.getFamilyName());
		bbu.setEmailAddress(u.getEmailAddress());
		bbu.setLastLogin(Util.getDateTimeFromCalendar(u.getLastLoginDate()));
                //u.getPortalRole().getRoleName()???
                try{bbu.setRoleName(PortalRoleDbLoader.Default.getInstance().loadPrimaryRoleByUserId(u.getPortalRoleId()).getRoleName());}catch(Exception e){bbu.setRoleName("");}
		try{bbu.setPortalRoleId(u.getPortalRoleId().getExternalString());}catch(Exception e){bbu.setPortalRoleId(null);}
		try{bbu.setSystemRole(u.getSystemRole().toFieldName());}catch(Exception e){bbu.setSystemRole("");}
	    case minimal:
		bbu.setUserName(u.getUserName());
		break;
            default: throw new Exception("Undefined verbosity of user");
	}
        return bbu;
    }
}
