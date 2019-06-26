package org.swift.jira.cot.functions;

import com.appfire.common.analytics.util.AnalyticEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.license.LicenseHandler;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.HostLicenseInformation;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.datacentercompatibility.HostLicenseInformation;
import com.atlassian.upm.datacentercompatibility.impl.CompatibilityHostLicenseInformation;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.swift.jira.cot.service.AnalyticsConfigService;
import org.swift.jira.cot.utilities.COTAnalyticsHelper;
import org.swift.jira.cot.utilities.MigrationUtility;
import org.swift.jira.library.Utilities;



















public class CreateIssueFunctionPluginFactory
  extends AbstractCreateFunctionPluginFactory
  implements WorkflowPluginFunctionFactory
{
  protected final ProjectManager projectManager;
  private final HostLicenseInformation hostLicenseInformation;
  
  public CreateIssueFunctionPluginFactory(IssueManager issueManager, SubTaskManager subTaskManager, IssueTypeManager issueTypeManager, ProjectManager projectManager, ConstantsManager constantsManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService, AnalyticsConfigService analyticsConfigService, HostLicenseInformation upmHostLicenseInformation, LicenseHandler licenseHandler) {
    super(issueManager, subTaskManager, issueTypeManager, constantsManager, applicationProperties, authenticationContext, i18nBeanFactory, eventPublisher, i18nResolver, licenseManager, jiraLicenseService, analyticsConfigService);
    
    this.projectManager = projectManager;
    this.hostLicenseInformation = new CompatibilityHostLicenseInformation(upmHostLicenseInformation, licenseHandler);
  }








  
  public void addDescriptorParams(Map<String, String> params, Map<String, Object> conditionParams) {
    params.put("field.projectKey", extractSingleParam(conditionParams, "projectKey"));
    params.put("field.specificProjectKey", extractSingleParam(conditionParams, "specificProjectKey"));

    
    params.put("field.issueTypeId", extractSingleParam(conditionParams, "issueTypeId"));

    
    params.put("field.issueSummary", extractSingleParam(conditionParams, "issueSummary"));

    
    params.put("field.issueDescription", extractSingleParam(conditionParams, "issueDescription"));

    
    params.put("field.issuePriorityId", extractSingleParam(conditionParams, "issuePriorityId"));

    
    params.put("field.issueReporter", extractSingleParam(conditionParams, "issueReporter"));

    
    params.put("field.issueAssignee", extractSingleParam(conditionParams, "issueAssignee"));

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificAffectedVersions"))) {
      params.put("field.issueAffectedVersions", "3");
    } else {
      params.put("field.issueAffectedVersions", extractSingleParam(conditionParams, "issueAffectedVersions"));
    } 

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificFixedVersions"))) {
      params.put("field.issueFixedVersions", "3");
    } else {
      params.put("field.issueFixedVersions", extractSingleParam(conditionParams, "issueFixedVersions"));
    } 

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificComponents"))) {
      params.put("field.issueComponents", "3");
    } else {
      params.put("field.issueComponents", extractSingleParam(conditionParams, "issueComponents"));
    } 

    
    params.put("field.issueDueDate", extractSingleParam(conditionParams, "issueDueDate"));

    
    params.put("field.issueOriginalEstimate", extractSingleParam(conditionParams, "issueOriginalEstimate"));

    
    params.put("field.issueRemainingEstimate", extractSingleParam(conditionParams, "issueRemainingEstimate"));

    
    params.put("field.securityLevel", extractSingleParam(conditionParams, "securityLevel"));

    
    try {
      if (this.analyticConfigService.isAnalyticsConfigure()) {
        COTAnalyticsHelper helper = new COTAnalyticsHelper(this.licenseManager, this.hostLicenseInformation);
        helper.capturePostFunctionAnalytics(params, AnalyticEvent.SubEvent.CREATEISSUE);
      } 
    } catch (Exception ex) {
      this.log.debug("Error while posting analytics for PostFunction", ex);
    } 
  }








  
  protected boolean continueWithView(FunctionDescriptor functionDescriptor) { return (functionDescriptor.getArgs().get("field.issueTypeId") != null); }










  
  protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor inDescriptor) {
    super.getVelocityParamsForView(velocityParams, inDescriptor);
    
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;
    if (continueWithView(descriptor)) {

      
      String projectKey = (String)descriptor.getArgs().get("field.projectKey");
      velocityParams.put("projectOriginal", "0");
      velocityParams.put("projectSpecific", "1");
      velocityParams.put("projectKey", projectKey);
      
      String specificProjectKey = (String)descriptor.getArgs().get("field.specificProjectKey");
      velocityParams.put("specificProjectKey", (specificProjectKey == null) ? "" : specificProjectKey);
      
      velocityParams.put("projectNameForView", isDefaultProjectKey(projectKey) ? "0" : ("1".equals(projectKey) ? ("(" + specificProjectKey + ")") : ("[" + projectKey + "] " + this.projectManager
          .getProjectObjByKey(projectKey).getName())));

      
      String issueTypeId = (String)descriptor.getArgs().get("field.issueTypeId");
      velocityParams.put("issueTypeId", (issueTypeId == null) ? "0" : issueTypeId);
      IssueType issueType = this.issueTypeManager.getIssueType(issueTypeId);
      velocityParams.put("issueTypeName", (issueType == null) ? "" : issueType.getName());

      
      velocityParams.put("issuePriorityId", descriptor.getArgs().get("field.issuePriorityId"));
      Priority priority = this.constantsManager.getPriorityObject((String)descriptor.getArgs().get("field.issuePriorityId"));
      velocityParams.put("issuePriorityName", (priority != null) ? priority.getName() : "");

      
      velocityParams.put("issueSummary", descriptor.getArgs().get("field.issueSummary"));

      
      velocityParams.put("issueDescription", descriptor.getArgs().get("field.issueDescription"));

      
      int reporter = 5;
      int assignee = 1;
      try {
        reporter = Integer.parseInt((String)descriptor.getArgs().get("field.issueReporter"));
      } catch (Exception exception) {}
      
      try {
        assignee = Integer.parseInt((String)descriptor.getArgs().get("field.issueAssignee"));
      } catch (Exception exception) {}
      
      velocityParams.put("issueReporterName", this.choices.getUserText(reporter));
      velocityParams.put("issueAssigneeName", this.choices.getUserText(assignee));

      
      int affectedVersions = 0;
      int fixedVersions = 0;
      try {
        affectedVersions = Integer.parseInt((String)descriptor.getArgs().get("field.issueAffectedVersions"));
      } catch (Exception exception) {}
      
      try {
        fixedVersions = Integer.parseInt((String)descriptor.getArgs().get("field.issueFixedVersions"));
      } catch (Exception exception) {}
      
      velocityParams.put("issueAffectedVersionsName", this.choices.getVersionText(affectedVersions));
      velocityParams.put("issueFixedVersionsName", this.choices.getVersionText(fixedVersions));

      
      int components = 1;
      try {
        components = Integer.parseInt((String)descriptor.getArgs().get("field.issueComponents"));
      } catch (Exception exception) {}
      
      velocityParams.put("issueComponentsName", this.choices.getComponentText(components));

      
      int dueDate = 0;
      try {
        dueDate = Integer.parseInt((String)descriptor.getArgs().get("field.issueDueDate"));
      } catch (Exception exception) {}
      
      velocityParams.put("issueDueDate", this.choices.getDueDateText(dueDate));

      
      String issueOriginalEstimate = (String)descriptor.getArgs().get("field.issueOriginalEstimate");
      velocityParams.put("issueOriginalEstimate", (issueOriginalEstimate == null) ? "" : issueOriginalEstimate);

      
      String issueRemainingEstimate = (String)descriptor.getArgs().get("field.issueRemainingEstimate");
      velocityParams.put("issueRemainingEstimate", (issueRemainingEstimate == null) ? "" : issueRemainingEstimate);

      
      String securityLevel = (String)descriptor.getArgs().get("field.securityLevel");
      velocityParams.put("securityLevel", (securityLevel == null) ? "" : securityLevel);
    } 
  }








  
  protected void getVelocityParamsForInput(Map velocityParams) {
    super.getVelocityParamsForInput(velocityParams);

    
    velocityParams.put("currentProjectKey", "0");
    velocityParams.put("currentSpecificProjectKey", "");

    
    velocityParams.put("currentIssueReporter", Integer.valueOf(5));
    velocityParams.put("currentIssueAssignee", Integer.valueOf(1));

    
    velocityParams.put("currentIssueAffectedVersions", Integer.valueOf(0));
    velocityParams.put("currentIssueFixedVersions", Integer.valueOf(0));

    
    velocityParams.put("currentIssueComponents", Integer.valueOf(1));

    
    velocityParams.put("currentIssueSummary", "%parent_summary%");

    
    velocityParams.put("currentIssueDueDate", Integer.valueOf(0));
  }









  
  protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor inDescriptor) {
    super.getVelocityParamsForEdit(velocityParams, inDescriptor);
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;

    
    velocityParams.put("projects", this.projectManager.getProjectObjects());
    String projectKey = (String)descriptor.getArgs().get("field.projectKey");
    velocityParams.put("currentProjectKey", isDefaultProjectKey(projectKey) ? "0" : (
        "1".equals(projectKey) ? "1" : projectKey));
    
    Project specificProjectKey = MigrationUtility.getProjectName((String)descriptor.getArgs().get("field.specificProjectKey"));
    velocityParams.put("currentSpecificProjectKey", (specificProjectKey == null) ? (String)descriptor.getArgs().get("field.specificProjectKey") : specificProjectKey.getKey());

    
    IssueType issueType = MigrationUtility.getIssueTypeName((String)descriptor.getArgs().get("field.issueTypeId"));
    velocityParams.put("issueTypes", this.constantsManager.getRegularIssueTypeObjects());
    velocityParams.put("currentIssueTypeId", descriptor.getArgs().get("field.issueTypeId"));
    velocityParams.put("currentIssueTypeName", (issueType != null) ? issueType.getName() : "");

    
    velocityParams.put("currentIssueSummary", descriptor.getArgs().get("field.issueSummary"));

    
    velocityParams.put("currentIssueDescription", descriptor.getArgs().get("field.issueDescription"));

    
    Priority priorityName = MigrationUtility.getPriorityName((String)descriptor.getArgs().get("field.issuePriorityId"));
    velocityParams.put("currentIssuePriorityId", descriptor.getArgs().get("field.issuePriorityId"));
    velocityParams.put("currentIssuePriorityName", (priorityName != null) ? priorityName.getName() : "");
    velocityParams.put("issuePriorities", this.constantsManager.getPriorities());

    
    int reporter = Utilities.getInt((String)descriptor.getArgs().get("field.issueReporter"), 5);
    velocityParams.put("currentIssueReporter", Integer.valueOf(reporter));

    
    int assignee = Utilities.getInt((String)descriptor.getArgs().get("field.issueAssignee"), 1);
    velocityParams.put("currentIssueAssignee", Integer.valueOf(assignee));

    
    int affectedVersions = Utilities.getInt((String)descriptor.getArgs().get("field.issueAffectedVersions"), 0);
    velocityParams.put("currentIssueAffectedVersions", Integer.valueOf(affectedVersions));

    
    int fixedVersions = Utilities.getInt((String)descriptor.getArgs().get("field.issueFixedVersions"), 0);
    velocityParams.put("currentIssueFixedVersions", Integer.valueOf(fixedVersions));

    
    int components = Utilities.getInt((String)descriptor.getArgs().get("field.issueComponents"), 1);
    velocityParams.put("currentIssueComponents", Integer.valueOf(components));

    
    int dueDate = Utilities.getInt((String)descriptor.getArgs().get("field.issueDueDate"), 0);
    velocityParams.put("currentIssueDueDate", Integer.valueOf(dueDate));


    
    String issueOriginalEstimate = (String)descriptor.getArgs().get("field.issueOriginalEstimate");
    velocityParams.put("currentIssueOriginalEstimate", (issueOriginalEstimate == null) ? "" : issueOriginalEstimate);

    
    String issueRemainingEstimate = (String)descriptor.getArgs().get("field.issueRemainingEstimate");
    velocityParams.put("currentIssueRemainingEstimate", (issueRemainingEstimate == null) ? "" : issueRemainingEstimate);

    
    String securityLevel = (String)descriptor.getArgs().get("field.securityLevel");
    velocityParams.put("currentSecurityLevel", (securityLevel == null) ? "" : securityLevel);
  }









  
  protected boolean isDefaultProjectKey(String key) { return (key == null || key.equals("0")); }
}
