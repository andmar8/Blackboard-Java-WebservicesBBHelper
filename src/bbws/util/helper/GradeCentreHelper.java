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
import blackboard.data.gradebook.impl.Attempt;
import blackboard.data.gradebook.impl.Outcome;
import blackboard.data.gradebook.impl.OutcomeDefinition;

//blackboard - persist
import blackboard.persist.gradebook.impl.AttemptDbLoader;
//Changed 9.1.40071.3 on
import blackboard.persist.gradebook.ext.AttemptDbPersister;
import blackboard.persist.gradebook.ext.GradeBookSettingsDbLoader;
import blackboard.persist.gradebook.impl.OutcomeDbLoader;
import blackboard.persist.gradebook.impl.OutcomeDefinitionDbLoader;
import blackboard.persist.gradebook.impl.OutcomeDefinitionDbPersister;

//blackboard - platform
import blackboard.platform.gradebook2.AttemptDetail;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradeDetail;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.gradebook2.impl.AttemptDAO;
import blackboard.platform.gradebook2.impl.GradeDetailDAO;
import blackboard.platform.gradebook2.impl.GradableItemDAO;

/******** Documented API ********/

//bbws
import bbws.entity.enums.verbosity.BBAttemptVerbosity;
import bbws.entity.enums.verbosity.BBCourseVerbosity;
import bbws.entity.enums.verbosity.BBCourseMembershipVerbosity;
import bbws.entity.enums.verbosity.BBLineitemVerbosity;
import bbws.entity.enums.verbosity.BBScoreVerbosity;
import bbws.resource.course.BBCourse;
import bbws.resource.coursemembership.BBCourseMembership;
import bbws.resource.gradecentre.BBGradeCentreSettings;
import bbws.resource.gradecentre.attempt.BBAttempt;
import bbws.resource.gradecentre.attempt.BBAttemptDetail;
import bbws.resource.gradecentre.grade.BBGradeDetail;
import bbws.resource.gradecentre.grade.BBGradingSchema;
import bbws.resource.gradecentre.grade.BBScore;
import bbws.resource.gradecentre.column.BBGradableItem;
import bbws.resource.gradecentre.column.BBLineitem;
import bbws.resource.gradecentre.outcome.BBOutcome;
import bbws.resource.gradecentre.outcome.BBOutcomeDefinition;
import bbws.resource.user.BBUser;
import bbws.util.exception.EmptyListException;
import bbws.util.factory.list.BBListFactory;
import bbws.util.factory.object.ObjectConverter;

//blackboard - base

//blackboard - data
import blackboard.data.course.CourseMembership;
//Changed 9.1.40071.3 on
import blackboard.data.gradebook.Lineitem;
import blackboard.data.gradebook.Score;

//blackboard - persist
import blackboard.persist.course.CourseDbLoader;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.gradebook.LineitemDbLoader;
import blackboard.persist.gradebook.LineitemDbPersister;
import blackboard.persist.gradebook.ScoreDbLoader;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.user.UserDbLoader;

//blackboard - platform
import blackboard.platform.persistence.PersistenceServiceFactory;

//java
import java.util.Iterator;
import java.util.List;

//javax
import javax.xml.ws.WebServiceException;

public class GradeCentreHelper
{
    public static Score getScoreObjForGivenCourseMembershipBbIdAndLineItemBbId(String courseMembershipBbId, String lineItemBbId) throws Exception
    {
	    return ((ScoreDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(ScoreDbLoader.TYPE)).loadByCourseMembershipIdAndLineitemId
		(
		    CourseMembershipDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(CourseMembership.DATA_TYPE,courseMembershipBbId)).getId(),
		    ((LineitemDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(LineitemDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Lineitem.LINEITEM_DATA_TYPE,lineItemBbId)).getId()
		);
    }

    //Changed in 9.1.40071.3
    public static boolean gradeCentreAttemptDelete(BBAttempt attempt) throws WebServiceException
    {
        try
        {
            AttemptDbPersister.Default.getInstance().deleteById(AttemptDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Attempt.DATA_TYPE,attempt.getAttemptBbId())).getId());
        }
        catch(Exception e)
        {
            throw new WebServiceException( "Error: Could not delete attempt - "+e.getMessage());
        }
        return true;
    }

    public static BBAttemptDetail gradeCentreAttemptDetailRead(BBAttemptDetail ad, BBAttemptVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getAttemptDetail(AttemptDAO.get().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(AttemptDetail.DATA_TYPE, ad.getId())),verbosity);
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBAttemptDetail gradeCentreAttemptDetailReadLastAttempByGradeDetail(BBGradeDetail gd, BBAttemptVerbosity verbosity) throws WebServiceException
    {
        BBAttemptDetail bbad = new BBAttemptDetail();
        bbad.setId(gd.getLastAttemptId());
        return gradeCentreAttemptDetailRead(bbad,verbosity);
    }

    public static List<BBAttempt> gradeCentreAttemptReadByOutcomeDefinitionId(BBOutcomeDefinition outcomeDef, BBAttemptVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return BBListFactory.getBBAttemptListFromList(AttemptDbLoader.Default.getInstance().loadByOutcomeDefinitionId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No attempts found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static List<BBAttempt> gradeCentreAttemptReadByOutcomeId(BBOutcome outcome, BBAttemptVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return BBListFactory.getBBAttemptListFromList(((AttemptDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(AttemptDbLoader.TYPE)).loadByOutcomeId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Outcome.DATA_TYPE, outcome.getOutcomeBbId())),verbosity);
            //return BBListFactory.getBBAttemptListFromList(AttemptDbLoader.Default.getInstance().loadByOutcomeId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Outcome.DATA_TYPE, outcome.getOutcomeBbId())),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No attempts found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBGradeDetail gradeCentreGradeDetailRead(BBGradeDetail gd) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getGradeDetail(GradeDetailDAO.get().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(GradeDetail.DATA_TYPE, gd.getId())));
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while retrieving GradeDetail: "+e.toString());
        }
    }

    public static List<BBGradeDetail> gradeCentreGradeDetailReadByGradableItem(BBGradableItem bbgi) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(GradeDetailDAO.get().getGradeDetails(GradableItemDAO.get().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(GradableItem.DATA_TYPE, bbgi.getId())).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No grade details found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while retrieving GradeDetails: "+e.toString());
        }
    }

    public static BBGradeDetail gradeCentreGradeDetailReadByGradableItemAndCourseMembership(BBGradableItem gi, BBCourseMembership cm) throws WebServiceException
    {
        Iterator<BBGradeDetail> gdi = gradeCentreGradeDetailReadByGradableItem(gi).iterator();
        BBGradeDetail gd;
        while(gdi.hasNext())
        {
            gd = gdi.next();
            if(gdi.next().getCourseUserId().equalsIgnoreCase(cm.getCourseMembershipBbId()))
            {
                return gd;
            }
        }
        throw new WebServiceException("Grade Detail not found");
    }

    public static BBGradeDetail gradeCentreGradeDetailReadByGradableItemAndUserId(BBGradableItem gi, BBUser u) throws WebServiceException
    {
        BBCourse c = new BBCourse();
        c.setCourseBbId(gi.getCourseId());
        return gradeCentreGradeDetailReadByGradableItemAndCourseMembership(gi,CourseMembershipHelper.courseMembershipReadByUserIdAndCourseId(u, CourseHelper.courseRead(c, BBCourseVerbosity.minimal), BBCourseMembershipVerbosity.minimal, false));
    }

    /*public static boolean gradeCentreGradableItemAdd(BBGradableItem bbgi) throws WebServiceException
    {
        try
        {
            blackboard.persist.BbPersistenceManager bbpm = PersistenceServiceFactory.getInstance().getDbPersistenceManager();
            GradableItem gi = new GradableItem();
            gi.setAggregationModel(GradableItem.AttemptAggregationModel.valueOf(bbgi.getAggregationModel().name()));
            try{gi.setAssessmentId(bbpm.generateId(blackboard.data.assessment.CourseAssessment, bbgi.getAssessmentId()));}catch(Exception e){}
            try{gi.setCalculatedInd(GradableItem.CalculationType.valueOf(bbgi.getCalculatedInd()));}catch(Exception e){}
            gi.setCategory(bbgi.getCategory());
            try{gi.setCategoryId(bbpm.generateId(blackboard.data.category.Category.DATA_TYPE, bbgi.getCategoryId()));}catch(Exception e){}
            gi.setCourseContentId(Id.UNSET_ID);
            gi.setCourseId(Id.UNSET_ID);
            gi.setDateAdded(bbgi.getDateAdded());
            gi.setDateModified(bbgi.getDateModified());
            gi.setDescription(bbgi.getDescription());
            gi.setDisplayTitle(bbgi.getDisplayTitle());
            gi.setDueDate(bbgi.getDueDate());
            gi.setExternalAnalysisUrl(bbgi.getExternalAnalysisUrl());
            gi.setExternalAttemptHandlerUrl(bbgi.getExternalAttemptHandlerUrl());
            gi.setGradingPeriodId(Id.UNSET_ID);
            gi.setGradingSchema(bbgi.getGradingSchema());
            gi.setGradingSchemaId(Id.UNSET_ID);
            gi.setHideAttempt(bbgi.getHideAttempt());
            //gi.setId(Id.UNSET_ID); //You shouldn't need to set this for creating a new item as blackboard should assign one
            gi.setLinkId(bbgi.getLinkId());
            gi.setPoints(bbgi.getPoints());
            //gi.setPosition(bbgi.getPosition()); //Not sure if bb will work this out too, probs better used for updating when changing position
            gi.setScorable(bbgi.getScorable());
            gi.setScoreProviderHandle(bbgi.getScoreProviderHandle());
            gi.setSecondaryGradingSchemaId(Id.UNSET_ID);
            gi.setShowStatsToStudent(bbgi.getShowStatsToStudent());
            gi.setSingleAttempt(bbgi.getSingleAttempt());
            gi.setTitle(bbgi.getTitle());
            try{gi.setVersion(new blackboard.persist.RowVersion(bbgi.getVersion()));}catch(Exception e){}
            gi.setVisibleInAllTerms(bbgi.getVisibleInAllTerms());
            gi.setVisibleInBook(bbgi.getVisibleInBook());
            gi.setVisibleToStudents(bbgi.getVisibleToStudents());
            try{gi.setWeight(Float.parseFloat(""+bbgi.getWeight()));}catch(Exception e){}
            GradebookManagerFactory.getInstanceWithoutSecurityCheck().persistGradebookItem(gi);
            return true;
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error whilst adding GradableItem: "+e.toString());
        }
    }*/

    public static BBGradableItem gradeCentreGradableItemRead(BBGradableItem gradableItem) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getGradableItem(GradableItemDAO.get().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(GradableItem.DATA_TYPE, gradableItem.getId())));
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while retrieving GradableItem: "+e.toString());
        }
    }

    public static List<BBGradableItem> gradeCentreGradableItemReadByCourseId(BBCourse course) throws WebServiceException
    {
        try
        {
            //return BBListFactory.getNonVerboseBBList(GradebookManagerFactory.getInstanceWithoutSecurityCheck().getGradebookItems(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
            return BBListFactory.getNonVerboseBBList(GradableItemDAO.get().getGradableItemByCourse(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No gradable items found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error while retrieving GradableItems: "+e.toString());
        }
    }

    /*public static BBGradeDetail gradeCentreGradeDetailReadByGradableItemIdAndUserId(BBGradableItem gradableItem, BBUser user) throws WebServiceException
    {
        try
        {
            blackboard.platform.gradebook2.GradeDetail gd = blackboard.platform.gradebook2.impl.GradeDetailDAO.get().getGradeDetail(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(GradableItem.DATA_TYPE, gradableItem.getId()), PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, user.getBbId()));
            //blackboard.platform.gradebook2.GradeWithAttemptScore gWAS = blackboard.platform.gradebook2.impl.GradeDAO.get().getGrade(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(GradableItem.DATA_TYPE, gradableItem), PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(User.DATA_TYPE, user.getBbId()));
            return new BBGradeDetail(gd);
            //gWAS.getAttemptScore();
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No grade detail found, has user taken test and has it been marked? Is user on course? Does gradable item exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: While retrieving grade detail... "+e.toString());
        }
    }*/

    public static List<BBGradingSchema> gradeCentreGradingSchemaReadByCourseId(BBCourse course) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(GradebookManagerFactory.getInstanceWithoutSecurityCheck().getGradingSchemaForCourse(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No gradable items found");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: "+e.getMessage());
        }
    }

    public static boolean gradeCentreLineitemAdd(BBLineitem lineitem, BBCourse course) throws WebServiceException
    {
        try
        {
            Lineitem li = new Lineitem();
            li.setCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId());
            //li.setAssessmentLocation(Lineitem.AssessmentLocation.INTERNAL);
            li.setName(lineitem.getName());
            li.setIsAvailable(lineitem.getAvailable());
            li.setPointsPossible(lineitem.getPointsPossible());
            li.setType(lineitem.getType());
            li.setWeight(lineitem.getWeight());
            ((LineitemDbPersister)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getPersister(LineitemDbPersister.TYPE)).persist(li);
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Could not add lineitem "+e.toString());
        }
        return true;
    }

    public static boolean gradeCentreLineitemOrOutcomeDefinitionDelete(String Id)
    {
    	try
        {
            OutcomeDefinitionDbPersister.Default.getInstance().deleteById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,Id));
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Does that item/outcome exist? "+e.toString());
        }
        return true;
    }

    public static BBLineitem gradeCentreLineitemRead(BBLineitem lineitem, BBLineitemVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getLineitem(((LineitemDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(LineitemDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Lineitem.LINEITEM_DATA_TYPE, lineitem.getLineItemBbId())),verbosity);
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Could not retrieve lineitem details "+e.toString());
        }
    }

    public static List<BBLineitem> gradeCentreLineitemReadByCourseId(BBCourse course, BBLineitemVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return BBListFactory.getBBLineitemListFromList((((LineitemDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(LineitemDbLoader.TYPE)).loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId())),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No line items found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBOutcomeDefinition gradeCentreOutcomeDefinitionRead(BBOutcomeDefinition outcomeDef) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getOutcomeDefinition(((OutcomeDefinitionDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(OutcomeDefinitionDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())));
            //return new BBOutcomeDefinition(OutcomeDefinitionDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())));
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No outcomeDef found. Does outcomeDef exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: Could not retrieve outcomeDefinition details "+e.toString());
        }
    }

    public static List<BBOutcomeDefinition> gradeCentreOutcomeDefinitionReadByCourseId(BBCourse course) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(((OutcomeDefinitionDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(OutcomeDefinitionDbLoader.TYPE)).loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseId()).getId()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No outcome definitions found");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBOutcome gradeCentreOutcomeRead(BBOutcome outcome) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getOutcome(((OutcomeDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(OutcomeDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Outcome.DATA_TYPE, outcome.getOutcomeBbId())));
            //return new BBOutcome(OutcomeDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Outcome.DATA_TYPE, outcome.getOutcomeBbId())));
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No outcome found. Does outcome exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: While retrieving outcome... "+e.toString());
        }
    }

    public static List<BBOutcome> gradeCentreOutcomeReadByOutcomeDefinitionId(BBOutcomeDefinition outcomeDef) throws WebServiceException
    {
        try
        {
            return BBListFactory.getNonVerboseBBList(((OutcomeDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(OutcomeDbLoader.TYPE)).loadByOutcomeDefinitionId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())));
            //return BBListFactory.getNonVerboseBBList(OutcomeDbLoader.Default.getInstance().loadByOutcomeDefinitionId(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())));
            //return BBListFactory.getNonVerboseBBList(Arrays.asList(((OutcomeDefinitionDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(OutcomeDefinitionDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())).getOutcomes()));
            //return BBListFactory.getNonVerboseBBList(Arrays.asList(OutcomeDefinitionDbLoader.Default.getInstance().loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(OutcomeDefinition.DATA_TYPE,outcomeDef.getOutcomeDefinitionBbId())).getOutcomes()));
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No outcomes found for outcomeDefBbId");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBScore gradeCentreScoreRead(BBScore score, BBScoreVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getScore(((ScoreDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(ScoreDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Score.SCORE_DATA_TYPE,score.getScoreBbId())),verbosity);
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No score found, has user taken test and has it been marked? Is user on course? Does lineitem exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: While retrieving score... "+e.toString());
        }
    }

    public static List<BBScore> gradeCentreScoreReadByLineitemId(BBLineitem lineitem, BBScoreVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return BBListFactory.getBBScoreListFromList(((LineitemDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(LineitemDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Lineitem.LINEITEM_DATA_TYPE,lineitem.getLineItemBbId())).getScores(),verbosity);
        }
        catch(EmptyListException ele)
        {
            throw new WebServiceException("No scores found for line item");
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }

    public static BBScore gradeCentreScoreReadByLineitemIdAndUserId(BBLineitem lineitem, BBUser user, BBScoreVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getScore
            (
                getScoreObjForGivenCourseMembershipBbIdAndLineItemBbId
                (
                    CourseMembershipDbLoader.Default.getInstance().loadByCourseAndUserId
                    (
                        ((LineitemDbLoader)PersistenceServiceFactory.getInstance().getDbPersistenceManager().getLoader(LineitemDbLoader.TYPE)).loadById(PersistenceServiceFactory.getInstance().getDbPersistenceManager().generateId(Lineitem.LINEITEM_DATA_TYPE,lineitem.getLineItemBbId())).getCourseId(),
                        UserDbLoader.Default.getInstance().loadByUserName(user.getUserName()).getId()
                    ).getId().toExternalString(),
                    lineitem.getLineItemBbId()
                ),
                verbosity
            );
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No score found, has user taken test and has it been marked? Is user on course? Does lineitem exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: While retrieving score... "+e.toString());
        }
    }

    public static BBScore gradeCentreScoreReadByLineitemIdAndCourseMembershipId(BBLineitem lineitem, BBCourseMembership courseMembership, BBScoreVerbosity verbosity) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getScore(getScoreObjForGivenCourseMembershipBbIdAndLineItemBbId(courseMembership.getCourseMembershipBbId(), lineitem.getLineItemBbId()),verbosity);
        }
        catch(KeyNotFoundException e)
        {
            throw new WebServiceException("Error: No score found, has user taken test and has it been marked? Is user on course? Does lineitem exist?");
        }
        catch(Exception e)
        {
            throw new WebServiceException("Error: While retrieving score... "+e.toString());
        }
    }

    //Changed 9.1.40071.3 on
    public static BBGradeCentreSettings gradeCentreSettingsRead(BBCourse course) throws WebServiceException
    {
        try
        {
            return ObjectConverter.getGradeCentreSettings(GradeBookSettingsDbLoader.Default.getInstance().loadByCourseId(CourseDbLoader.Default.getInstance().loadByCourseId(course.getCourseBbId()).getId()));
        }
        catch(Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
    }
}
