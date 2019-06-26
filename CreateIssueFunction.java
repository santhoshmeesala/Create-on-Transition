package org.swift.jira.cot.functions;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.JiraHome;
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
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.scheme.Scheme;
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
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.swift.jira.cot.utilities.Helper;
import org.swift.jira.library.Utilities;




























public class CreateIssueFunction
  extends AbstractCreateFunction
{
  protected final IssueSecuritySchemeManager issueSecuritySchemeManager;
  
  public CreateIssueFunction(IssueSecuritySchemeManager issueSecuritySchemeManager, CustomFieldManager customFieldManager, SubTaskManager subTaskManager, IssueManager issueManager, IssueFactory issueFactory, ConstantsManager constantsManager, ApplicationProperties applicationProperties, UserUtil userUtil, GroupManager groupManager, ProjectRoleManager projectRoleManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, BuildUtilsInfo buildInfo, OptionsManager optionsManager, WatcherManager watcherManager, IssueLinkManager issueLinkManager, IssueLinkTypeManager issueLinkTypeManager, IssueSecurityLevelManager issueSecurityLevelManager, IssueTypeSchemeManager issueTypeSchemeManager, AttachmentManager attachmentManager, AttachmentPathManager attachmentPathManager, CommentManager commentManager, VersionManager versionManager, ProjectManager projectManager, SearchService searchService, JiraHome jiraHome, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService) {
    super(customFieldManager, subTaskManager, issueManager, issueFactory, constantsManager, applicationProperties, userUtil, groupManager, projectRoleManager, permissionManager, authenticationContext, i18nBeanFactory, eventPublisher, buildInfo, optionsManager, watcherManager, issueLinkManager, issueLinkTypeManager, issueSecurityLevelManager, issueTypeSchemeManager, attachmentManager, attachmentPathManager, commentManager, versionManager, projectManager, searchService, jiraHome, i18nResolver, licenseManager, jiraLicenseService);


    
    this.issueSecuritySchemeManager = issueSecuritySchemeManager;
  }











  
  protected void setFields(Helper helper, MutableIssue newIssue) {
    Issue originalIssue = helper.getReplaceHelper().getOriginalIssue();
    Issue parentIssue = helper.getReplaceHelper().getParentIssue();
    Map<String, String> args = helper.getReplaceHelper().getArgs();
    this.log.debug("Set field values for parent issue: {}, initiating issue was: {}", parentIssue.getKey(), originalIssue.getKey());
    
    try {
      Project project;
      String projectKey = (String)args.get("field.projectKey");
      if (projectKey != null && projectKey.equals("1")) {
        projectKey = helper.getReplaceHelper().findReplace((String)args.get("field.specificProjectKey"));
      }

      
      if (projectKey == null || projectKey.equals("0")) {
        project = originalIssue.getProjectObject();
      } else {
        project = lookupProject(projectKey);
      } 
      
      if (project == null) {
        this.log.error("No project found for: {}, originating issue was {}.", projectKey, originalIssue.getKey());
      } else {
        newIssue.setProjectId(project.getId());

        
        String type = Utilities.getString((String)args.get("field.issueTypeId"), "0");
        if (type.equals("-1")) {
          type = helper.getReplaceHelper().findReplace((String)args.get("field.specificIssueType"));
        }
        
        IssueType issueType = lookupIssueType(project, type, false, null);
        if (issueType != null) {
          newIssue.setIssueTypeObject(issueType);


          
          setCommonFields(helper, newIssue);

          
          String securityLevel = (String)args.get("field.securityLevel");
          if (!StringUtils.isBlank(securityLevel) && hasIssueSecurityPermission(project, helper.getReplaceHelper())) {
            setIssueSecurityLevel(newIssue, helper.getReplaceHelper().findReplace(securityLevel));
          }

          
          int priority = Utilities.getInt((String)args.get("field.issuePriorityId"), 0);
          Priority priorityObject = null;
          if (priority == -2) {
            priorityObject = lookupPriority(helper.getReplaceHelper().findReplace((String)args.get("field.specificPriority")), null);
          } else if (priority == 0) {
            priorityObject = parentIssue.getPriority();
          } else {
            priorityObject = this.constantsManager.getPriorityObject((String)args.get("field.issuePriorityId"));
          } 
          if (priorityObject != null) {
            newIssue.setPriorityObject(priorityObject);
          }



          
          String issueSummary = helper.getReplaceHelper().findReplace((String)args.get("field.issueSummary"));
          if (issueSummary.length() > 255) {
            issueSummary = issueSummary.substring(0, 255);
            this.log.info("Summary was truncated to 255 characters while creating the issue to avoid failure.");
          } 
          newIssue.setSummary(issueSummary);

          
          newIssue.setDescription(helper.getReplaceHelper().findReplace((String)args.get("field.issueDescription")));

          
          int affectedVersions = Utilities.getInt((String)args.get("field.issueAffectedVersions"), 0);
          newIssue.setAffectedVersions(getVersions(helper, affectedVersions, (String)args.get("field.specificAffectedVersions"), newIssue.getProjectObject()));

          
          int fixedVersions = Utilities.getInt((String)args.get("field.issueFixedVersions"), 0);
          newIssue.setFixVersions(getVersions(helper, fixedVersions, (String)args.get("field.specificFixedVersions"), newIssue.getProjectObject()));

          
          int components = Utilities.getInt((String)args.get("field.issueComponents"), 1);
          newIssue.setComponent(getComponents(helper, components, (String)args.get("field.specificComponents"), newIssue.getProjectObject()));

          
          int reporter = Utilities.getInt((String)args.get("field.issueReporter"), 5);
          ApplicationUser userReporter = getUser(helper, reporter, (String)args.get("field.specificReporter"));
          if (userReporter != null && isUserHasProjectPermission(newIssue.getProjectObject(), userReporter, 0)) {
            newIssue.setReporter(userReporter);
          }

          
          int assignee = Utilities.getInt((String)args.get("field.issueAssignee"), 1);
          if (assignee == 6) {
            newIssue.setAssignee(this.projectManager.getDefaultAssignee(newIssue.getProjectObject(), newIssue.getComponents()));
          } else {
            ApplicationUser user = getUser(helper, assignee, (String)args.get("field.specificAssignee"));
            if (user != null && isUserHasProjectPermission(newIssue.getProjectObject(), user, 1)) {
              newIssue.setAssignee(user);
            }
          } 

          
          boolean excludeNonWorkingDays = excludeNonWorkingDays((String)args.get("field.dueDateOffset"));

          
          int dueDateOffset = excludeNonWorkingDays ? Utilities.getInt(((String)args.get("field.dueDateOffset")).replaceAll("[^0-9-]", ""), 0) : Utilities.getInt((String)args.get("field.dueDateOffset"), 0);
          int dueDate = Utilities.getInt((String)args.get("field.issueDueDate"), 0);
          newIssue.setDueDate(getDueDate(helper, dueDate, (String)args.get("field.specificDueDate"), dueDateOffset, excludeNonWorkingDays));

          
          Long originalEstimate = getTimeDuration(helper, (String)args.get("field.issueOriginalEstimate"));
          Long remainingEstimate = getTimeDuration(helper, (String)args.get("field.issueRemainingEstimate"));
          this.log.debug("set original estimate: {}", originalEstimate);
          newIssue.setOriginalEstimate(originalEstimate);
          this.log.debug("get original estimate: {}", newIssue.getOriginalEstimate());
          if (remainingEstimate != null) {
            newIssue.setEstimate(remainingEstimate);
          } else {
            newIssue.setEstimate(originalEstimate);
          } 
        } 
      } 
    } catch (Exception exception) {
      this.log.error("Unexpected exception: {}", exception.toString(), exception);
    } 
  }






  
  protected void setIssueSecurityLevel(MutableIssue issue, String securityLevel) {
    Scheme scheme = this.issueSecuritySchemeManager.getSchemeFor(issue.getProjectObject());
    
    this.log.debug("set issue security: {}", securityLevel);
    
    if (scheme != null && securityLevel != null) {
      Collection<IssueSecurityLevel> issueSecurityList = this.issueSecurityLevelManager.getIssueSecurityLevels(scheme.getId().longValue());
      IssueSecurityLevel newSecurityLevel = Utilities.findIssueSecurityLevel(securityLevel.trim(), issueSecurityList);
      if (newSecurityLevel != null) {
        issue.setSecurityLevelId(newSecurityLevel.getId());
        this.log.debug("set security level to id: " + issue.getSecurityLevelId());
      } else {
        this.log.error("Security level could not be set. Security level not found: " + securityLevel);
      } 
    } else {
      this.log.debug("Attempt to set issue security to " + securityLevel + " ignored, no issue security on " + issue.getProjectObject().getKey());
    } 
  }
}
