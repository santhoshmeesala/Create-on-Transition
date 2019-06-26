package org.swift.jira.cot.functions;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.swift.jira.cot.utilities.Helper;
import org.swift.jira.library.Utilities;



























public class CreateSubIssueFunction
  extends AbstractCreateFunction
{
  public CreateSubIssueFunction(CustomFieldManager customFieldManager, SubTaskManager subTaskManager, IssueManager issueManager, IssueFactory issueFactory, ConstantsManager constantsManager, ApplicationProperties applicationProperties, UserUtil userUtil, GroupManager groupManager, ProjectRoleManager projectRoleManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, BuildUtilsInfo buildInfo, OptionsManager optionsManager, WatcherManager watcherManager, IssueLinkManager issueLinkManager, IssueLinkTypeManager issueLinkTypeManager, IssueSecurityLevelManager issueSecurityLevelManager, IssueTypeSchemeManager issueTypeSchemeManager, AttachmentManager attachmentManager, AttachmentPathManager attachmentPathManager, CommentManager commentManager, VersionManager versionManager, ProjectManager projectManager, SearchService searchService, JiraHome jiraHome, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService) { super(customFieldManager, subTaskManager, issueManager, issueFactory, constantsManager, applicationProperties, userUtil, groupManager, projectRoleManager, permissionManager, authenticationContext, i18nBeanFactory, eventPublisher, buildInfo, optionsManager, watcherManager, issueLinkManager, issueLinkTypeManager, issueSecurityLevelManager, issueTypeSchemeManager, attachmentManager, attachmentPathManager, commentManager, versionManager, projectManager, searchService, jiraHome, i18nResolver, licenseManager, jiraLicenseService); }















  
  protected boolean verifyPreConditions(Helper helper) {
    boolean allowSiblingCreate = (1 == Utilities.getInt((String)helper.getReplaceHelper().getArgs().get("field.createSibling"), 0));

    
    return (allowSiblingCreate || !helper.getReplaceHelper().getOriginalIssue().getIssueType().isSubTask());
  }




  
  protected boolean verifyPreConditionsAfterJql(Helper helper) {
    boolean shouldContinue = true;
    String entryParent = (String)helper.getReplaceHelper().getArgs().get("field.entryParent");
    if (StringUtils.isNotBlank(entryParent)) {
      entryParent = helper.getReplaceHelper().findReplace(entryParent);
    }
    helper.getReplaceHelper().setEntryParentIssue((
        StringUtils.isBlank(entryParent) || entryParent.trim().equalsIgnoreCase("%parent_key%")) ? helper.getReplaceHelper()
        .getParentIssue() : this.issueManager.getIssueObject(entryParent.trim().toUpperCase()));
    if (helper.getReplaceHelper().getEntryParentIssue() == null) {
      this.log.error("Unable to find issue with key {}. Subtasks can not created.", entryParent);
      shouldContinue = false;
    } else if (helper.getReplaceHelper().getEntryParentIssue().isSubTask()) {
      this.log.error("Issue with key {} is a subtask. This is an invalid parent. Subtasks can not created.", entryParent);
      shouldContinue = false;
    } 
    return shouldContinue;
  }











  
  protected void doPostTasks(Helper helper, Issue newIssue) {
    Issue entryParentIssue = helper.getReplaceHelper().getEntryParentIssue();
    try {
      this.log.debug("Now link new issue: {}, as a subtask of parent: {}", newIssue.getKey(), entryParentIssue.getKey());
      this.subTaskManager.createSubTaskIssueLink(entryParentIssue, newIssue, helper.getReplaceHelper().getTransitionUser());
    } catch (CreateException exception) {
      this.log.debug("New issue: {}, parent issue: {}", newIssue, entryParentIssue);
      this.log.error("Unexpected create exception linking subtask to parent: {}", exception.toString(), exception);
    } catch (Exception exception) {
      this.log.debug("New issue: {}, parent issue: {}", newIssue, entryParentIssue);
      this.log.error("Unexpected exception linking subtask to parent: {}", exception.toString(), exception);
    } 
    super.doPostTasks(helper, newIssue);
  }











  
  protected void setFields(Helper helper, MutableIssue newIssue) {
    Issue originalIssue = helper.getReplaceHelper().getOriginalIssue();
    Issue parentIssue = helper.getReplaceHelper().getParentIssue();
    Issue entryParentIssue = helper.getReplaceHelper().getEntryParentIssue();
    Map<String, String> args = helper.getReplaceHelper().getArgs();
    this.log.debug("Set field values for parent issue: {}, initiating issue was: {}", parentIssue.getKey(), originalIssue.getKey());
    
    try {
      Project targetProject = entryParentIssue.getProjectObject();

      
      newIssue.setProjectId(targetProject.getId());



      
      boolean subtaskPreCreate = (1 == Utilities.getInt((String)helper.getReplaceHelper().getArgs().get("field.subtaskPreCreate"), 0));
      if (subtaskPreCreate) {
        this.log.debug("Subtask pre-create selected");
        newIssue.setParentObject(entryParentIssue);
      } 

      
      String type = Utilities.getString((String)args.get("field.subIssueTypeId"), "0");
      if (type.equals("-1")) {
        type = helper.getReplaceHelper().findReplace((String)args.get("field.specificIssueType"));
      }
      IssueType issueType = lookupIssueType(targetProject, type, true, null);
      if (issueType != null) {
        
        newIssue.setIssueTypeObject(issueType);

        
        setCommonFields(helper, newIssue);

        
        newIssue.setSecurityLevelId(entryParentIssue.getSecurityLevelId());

        
        int priority = Utilities.getInt((String)args.get("field.subIssuePriorityId"), 0);
        Priority priorityObject = null;
        if (priority == -2) {
          priorityObject = lookupPriority(helper.getReplaceHelper().findReplace((String)args.get("field.specificPriority")), null);
        } else if (priority == 0) {
          priorityObject = parentIssue.getPriority();
        } else {
          priorityObject = this.constantsManager.getPriorityObject((String)args.get("field.subIssuePriorityId"));
        } 
        newIssue.setPriorityObject(priorityObject);



        
        String issueSummary = helper.getReplaceHelper().findReplace((String)args.get("field.subIssueSummary"));
        if (issueSummary.length() > 255) {
          issueSummary = issueSummary.substring(0, 255);
          this.log.info("Summary was truncated to 255 characters while creating the subtask to avoid failure.");
        } 
        newIssue.setSummary(issueSummary);

        
        newIssue.setDescription(helper.getReplaceHelper().findReplace((String)args.get("field.subIssueDescription")));

        
        newIssue.setLabels(LabelParser.buildFromString(helper.getReplaceHelper().findReplace((String)args.get("field.labels"))));

        
        int affectedVersions = Utilities.getInt((String)args.get("field.subIssueAffectedVersions"), 0);
        newIssue.setAffectedVersions(getVersions(helper, affectedVersions, (String)args.get("field.specificAffectedVersions"), newIssue.getProjectObject()));

        
        int fixedVersions = Utilities.getInt((String)args.get("field.subIssueFixedVersions"), 0);
        newIssue.setFixVersions(getVersions(helper, fixedVersions, (String)args.get("field.specificFixedVersions"), newIssue.getProjectObject()));

        
        int components = Utilities.getInt((String)args.get("field.subIssueComponents"), 1);
        newIssue.setComponent(getComponents(helper, components, (String)args.get("field.specificComponents"), targetProject));

        
        int reporter = Utilities.getInt((String)args.get("field.subIssueReporter"), 5);
        ApplicationUser userReporter = getUser(helper, reporter, (String)args.get("field.specificReporter"));
        if (userReporter != null && isUserHasProjectPermission(newIssue.getProjectObject(), userReporter, 0)) {
          newIssue.setReporter(userReporter);
        }

        
        int assignee = Utilities.getInt((String)args.get("field.subIssueAssignee"), 1);
        if (assignee == 6) {
          newIssue.setAssignee(this.projectManager.getDefaultAssignee(targetProject, newIssue.getComponents()));
        } else {
          ApplicationUser user = getUser(helper, assignee, (String)args.get("field.specificAssignee"));
          if (user != null && isUserHasProjectPermission(newIssue.getProjectObject(), user, 1)) {
            newIssue.setAssignee(user);
          }
        } 

        
        boolean excludeNonWorkingDays = excludeNonWorkingDays((String)args.get("field.dueDateOffset"));

        
        int dueDateOffset = excludeNonWorkingDays ? Utilities.getInt(((String)args.get("field.dueDateOffset")).replaceAll("[^0-9-]", ""), 0) : Utilities.getInt((String)args.get("field.dueDateOffset"), 0);
        int dueDate = Utilities.getInt((String)args.get("field.subIssueDueDate"), 0);
        newIssue.setDueDate(getDueDate(helper, dueDate, (String)args.get("field.specificDueDate"), dueDateOffset, excludeNonWorkingDays));

        
        Long originalEstimate = getTimeDuration(helper, (String)args.get("field.subIssueOriginalEstimate"));
        Long remainingEstimate = getTimeDuration(helper, (String)args.get("field.subIssueRemainingEstimate"));
        newIssue.setOriginalEstimate(originalEstimate);
        if (remainingEstimate != null) {
          newIssue.setEstimate(remainingEstimate);
        } else {
          newIssue.setEstimate(originalEstimate);
        }
      
      } 
    } catch (Exception exception) {
      this.log.error("Unexpected exception: {}", exception.toString(), exception);
    } 
  }
}
