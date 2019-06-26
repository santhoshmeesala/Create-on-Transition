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
import org.apache.commons.lang.StringUtils;
import org.swift.jira.cot.service.AnalyticsConfigService;
import org.swift.jira.cot.utilities.COTAnalyticsHelper;
import org.swift.jira.cot.utilities.MigrationUtility;
import org.swift.jira.library.Utilities;


















public class CreateSubIssueFunctionPluginFactory
  extends AbstractCreateFunctionPluginFactory
  implements WorkflowPluginFunctionFactory
{
  private final HostLicenseInformation hostLicenseInformation;
  
  public CreateSubIssueFunctionPluginFactory(IssueManager issueManager, SubTaskManager subTaskManager, IssueTypeManager issueTypeManager, ConstantsManager constantsManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService, AnalyticsConfigService analyticsConfigService, HostLicenseInformation upmHostLicenseInformation, LicenseHandler licenseHandler) {
    super(issueManager, subTaskManager, issueTypeManager, constantsManager, applicationProperties, authenticationContext, i18nBeanFactory, eventPublisher, i18nResolver, licenseManager, jiraLicenseService, analyticsConfigService);
    
    this.hostLicenseInformation = new CompatibilityHostLicenseInformation(upmHostLicenseInformation, licenseHandler);
  }








  
  public void addDescriptorParams(Map<String, String> params, Map<String, Object> conditionParams) {
    putParameterWithCheck("createSibling", params, conditionParams);

    
    params.put("field.subIssueSummary", extractSingleParam(conditionParams, "subIssueSummary"));

    
    params.put("field.subIssueDescription", extractSingleParam(conditionParams, "subIssueDescription"));

    
    params.put("field.entryParent", extractSingleParam(conditionParams, "entryParent"));

    
    params.put("field.subIssueTypeId", extractSingleParam(conditionParams, "subIssueTypeId"));

    
    params.put("field.subIssuePriorityId", extractSingleParam(conditionParams, "subIssuePriorityId"));

    
    params.put("field.subIssueReporter", extractSingleParam(conditionParams, "subIssueReporter"));

    
    params.put("field.subIssueAssignee", extractSingleParam(conditionParams, "subIssueAssignee"));

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificAffectedVersions"))) {
      params.put("field.subIssueAffectedVersions", "3");
    } else {
      params.put("field.subIssueAffectedVersions", extractSingleParam(conditionParams, "subIssueAffectedVersions"));
    } 

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificFixedVersions"))) {
      params.put("field.subIssueFixedVersions", "3");
    } else {
      params.put("field.subIssueFixedVersions", extractSingleParam(conditionParams, "subIssueFixedVersions"));
    } 

    
    if (StringUtils.isNotBlank(extractSingleParam(conditionParams, "specificComponents"))) {
      params.put("field.subIssueComponents", "3");
    } else {
      params.put("field.subIssueComponents", extractSingleParam(conditionParams, "subIssueComponents"));
    } 

    
    params.put("field.subIssueDueDate", extractSingleParam(conditionParams, "subIssueDueDate"));

    
    params.put("field.subIssueOriginalEstimate", extractSingleParam(conditionParams, "subIssueOriginalEstimate"));

    
    params.put("field.subIssueRemainingEstimate", extractSingleParam(conditionParams, "subIssueRemainingEstimate"));

    
    putParameterWithCheck("subtaskPreCreate", params, conditionParams);

    
    try {
      if (this.analyticConfigService.isAnalyticsConfigure()) {
        COTAnalyticsHelper helper = new COTAnalyticsHelper(this.licenseManager, this.hostLicenseInformation);
        helper.capturePostFunctionAnalytics(params, AnalyticEvent.SubEvent.CREATESUBTASK);
      } 
    } catch (Exception ex) {
      this.log.debug("Error while posting analytics for PostFunction", ex);
    } 
  }







  
  protected void putParameterWithCheck(String name, Map<String, String> params, Map<String, Object> conditionParams) {
    if (conditionParams.containsKey(name)) {
      if (extractSingleParam(conditionParams, name).equals("on")) {
        params.put("field." + name, "1");
      } else {
        params.put("field." + name, extractSingleParam(conditionParams, name));
      } 
    } else {
      params.put("field." + name, "0");
    } 
  }








  
  protected boolean continueWithView(FunctionDescriptor functionDescriptor) { return (functionDescriptor.getArgs().get("field.subIssueTypeId") != null); }










  
  protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor inDescriptor) {
    super.getVelocityParamsForView(velocityParams, inDescriptor);
    
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;
    if (continueWithView(descriptor)) {

      
      velocityParams.put("createSibling", descriptor.getArgs().get("field.createSibling"));

      
      String entryParent = (String)descriptor.getArgs().get("field.entryParent");
      velocityParams.put("entryParent", StringUtils.isBlank(entryParent) ? "%parent_key%" : entryParent);

      
      String issueTypeId = (String)descriptor.getArgs().get("field.subIssueTypeId");
      velocityParams.put("subIssueTypeId", issueTypeId);
      IssueType issueType = this.subTaskManager.getSubTaskIssueType(issueTypeId);
      velocityParams.put("issueTypeName", (issueType == null) ? "" : issueType.getName());

      
      velocityParams.put("subIssuePriorityId", descriptor.getArgs().get("field.subIssuePriorityId"));
      Priority priority = this.constantsManager.getPriorityObject((String)descriptor.getArgs().get("field.subIssuePriorityId"));
      velocityParams.put("issuePriorityName", (priority != null) ? priority.getName() : "");

      
      velocityParams.put("subIssueSummary", descriptor.getArgs().get("field.subIssueSummary"));

      
      velocityParams.put("subIssueDescription", descriptor.getArgs().get("field.subIssueDescription"));

      
      int reporter = 5;
      int assignee = 1;
      try {
        reporter = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueReporter"));
      } catch (Exception exception) {}
      
      try {
        assignee = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueAssignee"));
      } catch (Exception exception) {}
      
      velocityParams.put("subIssueReporterName", this.choices.getUserText(reporter));
      velocityParams.put("subIssueAssigneeName", this.choices.getUserText(assignee));

      
      int affectedVersions = 0;
      int fixedVersions = 0;
      try {
        affectedVersions = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueAffectedVersions"));
      } catch (Exception exception) {}
      
      try {
        fixedVersions = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueFixedVersions"));
      } catch (Exception exception) {}
      
      velocityParams.put("subIssueAffectedVersionsName", this.choices.getVersionText(affectedVersions));
      velocityParams.put("subIssueFixedVersionsName", this.choices.getVersionText(fixedVersions));

      
      int components = 1;
      try {
        components = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueComponents"));
      } catch (Exception exception) {}
      
      velocityParams.put("subIssueComponentsName", this.choices.getComponentText(components));

      
      int dueDate = 0;
      try {
        dueDate = Integer.parseInt((String)descriptor.getArgs().get("field.subIssueDueDate"));
      } catch (Exception exception) {}
      
      velocityParams.put("subIssueDueDate", this.choices.getDueDateText(dueDate));

      
      String subIssueOriginalEstimate = (String)descriptor.getArgs().get("field.subIssueOriginalEstimate");
      velocityParams.put("subIssueOriginalEstimate", (subIssueOriginalEstimate == null) ? "" : subIssueOriginalEstimate);

      
      String subIssueRemainingEstimate = (String)descriptor.getArgs().get("field.subIssueRemainingEstimate");
      velocityParams.put("subIssueRemainingEstimate", (subIssueRemainingEstimate == null) ? "" : subIssueRemainingEstimate);

      
      velocityParams.put("subtaskPreCreate", descriptor.getArgs().get("field.subtaskPreCreate"));
    } 
  }








  
  protected void getVelocityParamsForInput(Map velocityParams) {
    super.getVelocityParamsForInput(velocityParams);

    
    velocityParams.put("entryParent", "%parent_key%");

    
    velocityParams.put("currentSubIssueReporter", Integer.valueOf(5));
    velocityParams.put("currentSubIssueAssignee", Integer.valueOf(1));

    
    velocityParams.put("currentSubIssueAffectedVersions", Integer.valueOf(0));
    velocityParams.put("currentSubIssueFixedVersions", Integer.valueOf(0));

    
    velocityParams.put("currentSubIssueComponents", Integer.valueOf(1));

    
    velocityParams.put("currentSubIssueSummary", "%parent_summary%");

    
    velocityParams.put("currentSubIssueDueDate", Integer.valueOf(0));
  }









  
  protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor inDescriptor) {
    super.getVelocityParamsForEdit(velocityParams, inDescriptor);
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;

    
    String entryParent = (String)descriptor.getArgs().get("field.entryParent");
    velocityParams.put("currentEntryParent", StringUtils.isBlank(entryParent) ? "%parent_key%" : entryParent);

    
    IssueType issueType = MigrationUtility.getIssueTypeName((String)descriptor.getArgs().get("field.subIssueTypeId"));
    velocityParams.put("currentSubIssueTypeId", descriptor.getArgs().get("field.subIssueTypeId"));
    velocityParams.put("currentSubIssueTypeName", (issueType != null) ? issueType.getName() : "");

    
    String currentCreateSibling = (String)descriptor.getArgs().get("field.createSibling");
    velocityParams.put("currentCreateSibling", (currentCreateSibling == null) ? "" : currentCreateSibling);

    
    velocityParams.put("currentSubIssueSummary", descriptor.getArgs().get("field.subIssueSummary"));

    
    velocityParams.put("currentSubIssueDescription", descriptor.getArgs().get("field.subIssueDescription"));

    
    Priority priorityName = MigrationUtility.getPriorityName((String)descriptor.getArgs().get("field.subIssuePriorityId"));
    velocityParams.put("currentSubIssuePriorityId", descriptor.getArgs().get("field.subIssuePriorityId"));
    velocityParams.put("currentSubIssuePriorityName", (priorityName != null) ? priorityName.getName() : "");
    velocityParams.put("subIssuePriorities", this.constantsManager.getPriorities());

    
    int reporter = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueReporter"), 5);
    velocityParams.put("currentSubIssueReporter", Integer.valueOf(reporter));

    
    int assignee = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueAssignee"), 1);
    velocityParams.put("currentSubIssueAssignee", Integer.valueOf(assignee));

    
    int affectedVersions = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueAffectedVersions"), 0);
    velocityParams.put("currentSubIssueAffectedVersions", Integer.valueOf(affectedVersions));

    
    int fixedVersions = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueFixedVersions"), 0);
    velocityParams.put("currentSubIssueFixedVersions", Integer.valueOf(fixedVersions));

    
    int components = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueComponents"), 1);
    velocityParams.put("currentSubIssueComponents", Integer.valueOf(components));

    
    int dueDate = Utilities.getInt((String)descriptor.getArgs().get("field.subIssueDueDate"), 0);
    velocityParams.put("currentSubIssueDueDate", Integer.valueOf(dueDate));

    
    String subIssueOriginalEstimate = (String)descriptor.getArgs().get("field.subIssueOriginalEstimate");
    velocityParams.put("currentSubIssueOriginalEstimate", (subIssueOriginalEstimate == null) ? "" : subIssueOriginalEstimate);

    
    String subIssueRemainingEstimate = (String)descriptor.getArgs().get("field.subIssueRemainingEstimate");
    velocityParams.put("currentSubIssueRemainingEstimate", (subIssueRemainingEstimate == null) ? "" : subIssueRemainingEstimate);

    
    String currentSubtaskPreCreate = (String)descriptor.getArgs().get("field.subtaskPreCreate");
    velocityParams.put("currentSubtaskPreCreate", (currentSubtaskPreCreate == null) ? "" : currentSubtaskPreCreate);
  }
}
