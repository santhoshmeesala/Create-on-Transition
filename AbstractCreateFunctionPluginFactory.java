package org.swift.jira.cot.functions;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swift.jira.cot.service.AnalyticsConfigService;
import org.swift.jira.cot.utilities.Choices;
import org.swift.jira.cot.utilities.MigrationUtility;
import org.swift.jira.library.CsvUtilities;
import org.swift.jira.library.LicenseUtilities;
import org.swift.jira.library.Utilities;

public abstract class AbstractCreateFunctionPluginFactory
  extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
  protected final Logger log;
  protected final IssueManager issueManager;
  protected final SubTaskManager subTaskManager;
  protected final IssueTypeManager issueTypeManager;
  protected final ConstantsManager constantsManager;
  protected final ApplicationProperties applicationProperties;
  protected final JiraAuthenticationContext authenticationContext;
  protected final I18nHelper.BeanFactory i18nBeanFactory;
  protected final EventPublisher eventPublisher;
  protected final I18nResolver i18nResolver;
  protected final PluginLicenseManager licenseManager;
  protected final JiraLicenseService jiraLicenseService;
  protected final AnalyticsConfigService analyticConfigService;
  protected final Choices choices;
  
  protected AbstractCreateFunctionPluginFactory(IssueManager issueManager, SubTaskManager subTaskManager, IssueTypeManager issueTypeManager, ConstantsManager constantsManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService, AnalyticsConfigService analyticConfigService) {
    this.log = LoggerFactory.getLogger(getClass());






















    
    this.issueManager = issueManager;
    this.subTaskManager = subTaskManager;
    this.issueTypeManager = issueTypeManager;
    this.constantsManager = constantsManager;
    this.applicationProperties = applicationProperties;
    this.authenticationContext = authenticationContext;
    this.i18nBeanFactory = i18nBeanFactory;
    this.eventPublisher = eventPublisher;
    this.i18nResolver = i18nResolver;
    this.licenseManager = licenseManager;
    this.jiraLicenseService = jiraLicenseService;
    this.analyticConfigService = analyticConfigService;
    
    this.choices = new Choices(i18nResolver);
  }



  
  protected abstract void addDescriptorParams(Map<String, String> paramMap1, Map<String, Object> paramMap2);



  
  public Map<String, String> getDescriptorParams(Map<String, Object> conditionParams) {
    Map<String, String> params = new HashMap<String, String>();
    
    addDescriptorParams(params, conditionParams);

    
    params.put("field.conditionPattern1", extractSingleParam(conditionParams, "conditionPattern1"));
    params.put("field.conditionValue1", extractSingleParam(conditionParams, "conditionValue1"));
    putParameterWithCheck("conditionExact1", params, conditionParams);
    putParameterWithCheck("conditionLiteral1", params, conditionParams);
    putParameterWithCheck("conditionReverse1", params, conditionParams);
    
    params.put("field.conditionPattern2", extractSingleParam(conditionParams, "conditionPattern2"));
    params.put("field.conditionValue2", extractSingleParam(conditionParams, "conditionValue2"));
    putParameterWithCheck("conditionExact2", params, conditionParams);
    putParameterWithCheck("conditionLiteral2", params, conditionParams);
    putParameterWithCheck("conditionReverse2", params, conditionParams);

    
    params.put("field.labels", extractSingleParam(conditionParams, "labels"));

    
    params.put("field.watchers", extractSingleParam(conditionParams, "watchers"));

    
    params.put("field.linkKey", extractSingleParam(conditionParams, "linkKey"));
    params.put("field.linkType", extractSingleParam(conditionParams, "linkType"));
    params.put("field.linkDirection", extractSingleParam(conditionParams, "linkDirection"));

    
    params.put("field.copyLinksFrom", extractSingleParam(conditionParams, "copyLinksFrom"));
    params.put("field.copyLinksTypes", extractSingleParam(conditionParams, "copyLinksTypes"));

    
    params.put("field.copyRemoteLinks", extractSingleParam(conditionParams, "copyRemoteLinks"));
    params.put("field.copyRemoteLinksType", extractSingleParam(conditionParams, "copyRemoteLinksType"));

    
    params.put("field.copyAttachments", extractSingleParam(conditionParams, "copyAttachments"));
    putParameterWithCheck("copyTransitionAttachments", params, conditionParams);

    
    params.put("field.comment", extractSingleParam(conditionParams, "comment"));
    params.put("field.commentSecurity", extractSingleParam(conditionParams, "commentSecurity"));
    putParameterWithCheck("copyParentComments", params, conditionParams);
    putParameterWithCheck("copyOriginalComments", params, conditionParams);

    
    params.put("field.specificIssueType", extractSingleParam(conditionParams, "specificIssueType"));

    
    params.put("field.specificPriority", extractSingleParam(conditionParams, "specificPriority"));

    
    params.put("field.specificReporter", extractSingleParam(conditionParams, "specificReporter"));

    
    params.put("field.specificAssignee", extractSingleParam(conditionParams, "specificAssignee"));

    
    params.put("field.specificAffectedVersions", extractSingleParam(conditionParams, "specificAffectedVersions"));

    
    params.put("field.specificFixedVersions", extractSingleParam(conditionParams, "specificFixedVersions"));

    
    params.put("field.specificComponents", extractSingleParam(conditionParams, "specificComponents"));

    
    params.put("field.specificDueDate", extractSingleParam(conditionParams, "specificDueDate"));

    
    params.put("field.dueDateOffset", extractSingleParam(conditionParams, "dueDateOffset"));

    
    params.put("field.copyParentFields", extractSingleParam(conditionParams, "copyParentFields"));
    params.put("field.copyOriginalFields", extractSingleParam(conditionParams, "copyOriginalFields"));
    params.put("field.customFieldIds", extractSingleParam(conditionParams, "customFieldIds"));

    
    if (conditionParams.get("customField1Name") != null) {
      params.put("field.customField1Name", extractSingleParam(conditionParams, "customField1Name"));
      params.put("field.customField1Value1", extractSingleParam(conditionParams, "customField1Value1"));
      params.put("field.customField1Value2", extractSingleParam(conditionParams, "customField1Value2"));
    } 

    
    if (conditionParams.get("customField2Name") != null) {
      params.put("field.customField2Name", extractSingleParam(conditionParams, "customField2Name"));
      params.put("field.customField2Value1", extractSingleParam(conditionParams, "customField2Value1"));
      params.put("field.customField2Value2", extractSingleParam(conditionParams, "customField2Value2"));
    } 

    
    if (conditionParams.get("customField3Name") != null) {
      params.put("field.customField3Name", extractSingleParam(conditionParams, "customField3Name"));
      params.put("field.customField3Value1", extractSingleParam(conditionParams, "customField3Value1"));
      params.put("field.customField3Value2", extractSingleParam(conditionParams, "customField3Value2"));
    } 

    
    if (conditionParams.get("customField4Name") != null) {
      params.put("field.customField4Name", extractSingleParam(conditionParams, "customField4Name"));
      params.put("field.customField4Value1", extractSingleParam(conditionParams, "customField4Value1"));
      params.put("field.customField4Value2", extractSingleParam(conditionParams, "customField4Value2"));
    } 

    
    if (conditionParams.get("customField5Name") != null) {
      params.put("field.customField5Name", extractSingleParam(conditionParams, "customField5Name"));
      params.put("field.customField5Value1", extractSingleParam(conditionParams, "customField5Value1"));
      params.put("field.customField5Value2", extractSingleParam(conditionParams, "customField5Value2"));
    } 

    
    if (conditionParams.get("customField10Name") != null) {
      params.put("field.customField10Name", extractSingleParam(conditionParams, "customField10Name"));
      params.put("field.customField10Value1", extractSingleParam(conditionParams, "customField10Value1"));
      params.put("field.customField10Value2", extractSingleParam(conditionParams, "customField10Value2"));
    } 
    
    if (conditionParams.get("customField11Name") != null) {
      params.put("field.customField11Name", extractSingleParam(conditionParams, "customField11Name"));
      params.put("field.customField11Value1", extractSingleParam(conditionParams, "customField11Value1"));
      params.put("field.customField11Value2", extractSingleParam(conditionParams, "customField11Value2"));
    } 
    
    if (conditionParams.get("customField12Name") != null) {
      params.put("field.customField12Name", extractSingleParam(conditionParams, "customField12Name"));
      params.put("field.customField12Value1", extractSingleParam(conditionParams, "customField12Value1"));
      params.put("field.customField12Value2", extractSingleParam(conditionParams, "customField12Value2"));
    } 
    
    if (conditionParams.get("customField13Name") != null) {
      params.put("field.customField13Name", extractSingleParam(conditionParams, "customField13Name"));
      params.put("field.customField13Value1", extractSingleParam(conditionParams, "customField13Value1"));
      params.put("field.customField13Value2", extractSingleParam(conditionParams, "customField13Value2"));
    } 
    
    if (conditionParams.get("customField14Name") != null) {
      params.put("field.customField14Name", extractSingleParam(conditionParams, "customField14Name"));
      params.put("field.customField14Value1", extractSingleParam(conditionParams, "customField14Value1"));
      params.put("field.customField14Value2", extractSingleParam(conditionParams, "customField14Value2"));
    } 
    
    if (conditionParams.get("customField15Name") != null) {
      params.put("field.customField15Name", extractSingleParam(conditionParams, "customField15Name"));
      params.put("field.customField15Value1", extractSingleParam(conditionParams, "customField15Value1"));
      params.put("field.customField15Value2", extractSingleParam(conditionParams, "customField15Value2"));
    } 
    
    if (conditionParams.get("customField16Name") != null) {
      params.put("field.customField16Name", extractSingleParam(conditionParams, "customField16Name"));
      params.put("field.customField16Value1", extractSingleParam(conditionParams, "customField16Value1"));
      params.put("field.customField16Value2", extractSingleParam(conditionParams, "customField16Value2"));
    } 
    
    if (conditionParams.get("customField17Name") != null) {
      params.put("field.customField17Name", extractSingleParam(conditionParams, "customField17Name"));
      params.put("field.customField17Value1", extractSingleParam(conditionParams, "customField17Value1"));
      params.put("field.customField17Value2", extractSingleParam(conditionParams, "customField17Value2"));
    } 
    
    if (conditionParams.get("customField18Name") != null) {
      params.put("field.customField18Name", extractSingleParam(conditionParams, "customField18Name"));
      params.put("field.customField18Value1", extractSingleParam(conditionParams, "customField18Value1"));
      params.put("field.customField18Value2", extractSingleParam(conditionParams, "customField18Value2"));
    } 
    
    if (conditionParams.get("customField19Name") != null) {
      params.put("field.customField19Name", extractSingleParam(conditionParams, "customField19Name"));
      params.put("field.customField19Value1", extractSingleParam(conditionParams, "customField19Value1"));
      params.put("field.customField19Value2", extractSingleParam(conditionParams, "customField19Value2"));
    } 

    
    params.put("field.jqlQuery", extractSingleParam(conditionParams, "jqlQuery"));

    
    params.put("field.multipleIssuesPattern", extractSingleParam(conditionParams, "multipleIssuesPattern"));
    params.put("field.multipleIssuesValue", extractSingleParam(conditionParams, "multipleIssuesValue"));
    putParameterWithCheck("multipleIssuesExact", params, conditionParams);
    putParameterWithCheck("multipleIssuesLiteral", params, conditionParams);
    putParameterWithCheck("multipleIssuesReverse", params, conditionParams);

    
    params.put("field.acting", extractSingleParam(conditionParams, "acting"));

    
    params.put("field.environment", extractSingleParam(conditionParams, "environment"));

    
    params.put("field.notes", extractSingleParam(conditionParams, "notes"));
    
    return params;
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








  
  protected abstract boolean continueWithView(FunctionDescriptor paramFunctionDescriptor);







  
  protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor inDescriptor) {
    velocityParams.put("licenseErrorMessage", 
        LicenseUtilities.validateLicense(this.licenseManager, this.jiraLicenseService, this.i18nResolver, "org.swift.jira.cot", true));
    
    if (!(inDescriptor instanceof FunctionDescriptor)) {
      throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
    }
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;
    
    if (continueWithView(descriptor)) {

      
      velocityParams.put("conditionPattern1", descriptor.getArgs().get("field.conditionPattern1"));
      velocityParams.put("conditionValue1", descriptor.getArgs().get("field.conditionValue1"));
      velocityParams.put("conditionExact1", descriptor.getArgs().get("field.conditionExact1"));
      velocityParams.put("conditionLiteral1", descriptor.getArgs().get("field.conditionLiteral1"));
      velocityParams.put("conditionReverse1", descriptor.getArgs().get("field.conditionReverse1"));
      
      velocityParams.put("conditionPattern2", descriptor.getArgs().get("field.conditionPattern2"));
      velocityParams.put("conditionValue2", descriptor.getArgs().get("field.conditionValue2"));
      velocityParams.put("conditionExact2", descriptor.getArgs().get("field.conditionExact2"));
      velocityParams.put("conditionLiteral2", descriptor.getArgs().get("field.conditionLiteral2"));
      velocityParams.put("conditionReverse2", descriptor.getArgs().get("field.conditionReverse2"));

      
      velocityParams.put("labels", descriptor.getArgs().get("field.labels"));

      
      velocityParams.put("watchers", descriptor.getArgs().get("field.watchers"));

      
      velocityParams.put("linkKey", descriptor.getArgs().get("field.linkKey"));
      velocityParams.put("linkType", descriptor.getArgs().get("field.linkType"));
      velocityParams.put("linkDirection", descriptor.getArgs().get("field.linkDirection"));

      
      velocityParams.put("copyLinksFrom", descriptor.getArgs().get("field.copyLinksFrom"));
      velocityParams.put("copyLinksTypes", descriptor.getArgs().get("field.copyLinksTypes"));
      velocityParams.put("copyRemoteLinks", descriptor.getArgs().get("field.copyRemoteLinks"));
      velocityParams.put("copyRemoteLinksType", descriptor.getArgs().get("field.copyRemoteLinksType"));

      
      velocityParams.put("copyAttachments", descriptor.getArgs().get("field.copyAttachments"));
      velocityParams.put("copyTransitionAttachments", descriptor.getArgs().get("field.copyTransitionAttachments"));

      
      velocityParams.put("comment", descriptor.getArgs().get("field.comment"));
      velocityParams.put("commentSecurity", descriptor.getArgs().get("field.commentSecurity"));
      velocityParams.put("copyParentComments", descriptor.getArgs().get("field.copyParentComments"));
      velocityParams.put("copyOriginalComments", descriptor.getArgs().get("field.copyOriginalComments"));

      
      String specificIssueType = (String)descriptor.getArgs().get("field.specificIssueType");
      velocityParams.put("specificIssueType", (specificIssueType == null) ? "" : specificIssueType);

      
      String specificPriority = (String)descriptor.getArgs().get("field.specificPriority");
      velocityParams.put("specificPriority", (specificIssueType == null) ? "" : specificPriority);

      
      String specificReporter = (String)descriptor.getArgs().get("field.specificReporter");
      String specificAssignee = (String)descriptor.getArgs().get("field.specificAssignee");
      velocityParams.put("specificReporter", (specificReporter == null) ? "" : specificReporter);
      velocityParams.put("specificAssignee", (specificAssignee == null) ? "" : specificAssignee);

      
      String specificAffectedVersions = (String)descriptor.getArgs().get("field.specificAffectedVersions");
      String specificFixedVersions = (String)descriptor.getArgs().get("field.specificFixedVersions");
      velocityParams.put("specificAffectedVersions", (specificAffectedVersions == null) ? "" : specificAffectedVersions);
      velocityParams.put("specificFixedVersions", (specificFixedVersions == null) ? "" : specificFixedVersions);

      
      String specificComponents = (String)descriptor.getArgs().get("field.specificComponents");
      velocityParams.put("specificComponents", (specificComponents == null) ? "" : specificComponents);

      
      String specificDueDate = (String)descriptor.getArgs().get("field.specificDueDate");
      velocityParams.put("specificDueDate", (specificDueDate == null) ? "" : specificDueDate);

      
      String dueDateOffset = (String)descriptor.getArgs().get("field.dueDateOffset");
      velocityParams.put("dueDateOffset", (dueDateOffset == null) ? "" : dueDateOffset);


      
      String copyParentFields = (String)descriptor.getArgs().get("field.copyParentFields");
      String copyParentFieldsUpdated = getCustomFieldNamesUpdated(copyParentFields);
      velocityParams.put("copyParentFields", copyParentFieldsUpdated);
      String copyOriginalFields = (String)descriptor.getArgs().get("field.copyOriginalFields");
      String copyOriginalFieldsUpdated = getCustomFieldNamesUpdated(copyOriginalFields);
      velocityParams.put("copyOriginalFields", copyOriginalFieldsUpdated);


      
      String customField1Name = (String)descriptor.getArgs().get("field.customField1Name");
      CustomField customField = MigrationUtility.getCustomFieldName(customField1Name);
      if (customField != null) {
        velocityParams.put("customField1Name", customField.getName());
      } else {
        velocityParams.put("customField1Name", (customField1Name == null) ? "" : customField1Name);
      } 
      String customField1Value1 = (String)descriptor.getArgs().get("field.customField1Value1");
      velocityParams.put("customField1Value1", (customField1Value1 == null) ? "" : customField1Value1);
      String customField1Value2 = (String)descriptor.getArgs().get("field.customField1Value2");
      velocityParams.put("customField1Value2", (customField1Value2 == null) ? "" : customField1Value2);

      
      String customField2Name = (String)descriptor.getArgs().get("field.customField2Name");
      customField = MigrationUtility.getCustomFieldName(customField2Name);
      if (customField != null) {
        velocityParams.put("customField2Name", customField.getName());
      } else {
        velocityParams.put("customField2Name", (customField2Name == null) ? "" : customField2Name);
      } 
      String customField2Value1 = (String)descriptor.getArgs().get("field.customField2Value1");
      velocityParams.put("customField2Value1", (customField2Value1 == null) ? "" : customField2Value1);
      String customField2Value2 = (String)descriptor.getArgs().get("field.customField2Value2");
      velocityParams.put("customField2Value2", (customField2Value2 == null) ? "" : customField2Value2);

      
      String customField3Name = (String)descriptor.getArgs().get("field.customField3Name");
      customField = MigrationUtility.getCustomFieldName(customField3Name);
      if (customField != null) {
        velocityParams.put("customField3Name", customField.getName());
      } else {
        velocityParams.put("customField3Name", (customField3Name == null) ? "" : customField3Name);
      } 
      String customField3Value1 = (String)descriptor.getArgs().get("field.customField3Value1");
      velocityParams.put("customField3Value1", (customField3Value1 == null) ? "" : customField3Value1);
      String customField3Value2 = (String)descriptor.getArgs().get("field.customField3Value2");
      velocityParams.put("customField3Value2", (customField3Value2 == null) ? "" : customField3Value2);

      
      String customField4Name = (String)descriptor.getArgs().get("field.customField4Name");
      customField = MigrationUtility.getCustomFieldName(customField4Name);
      if (customField != null) {
        velocityParams.put("customField4Name", customField.getName());
      } else {
        velocityParams.put("customField4Name", (customField4Name == null) ? "" : customField4Name);
      } 
      String customField4Value1 = (String)descriptor.getArgs().get("field.customField4Value1");
      velocityParams.put("customField4Value1", (customField4Value1 == null) ? "" : customField4Value1);
      String customField4Value2 = (String)descriptor.getArgs().get("field.customField4Value2");
      velocityParams.put("customField4Value2", (customField4Value2 == null) ? "" : customField4Value2);

      
      String customField5Name = (String)descriptor.getArgs().get("field.customField5Name");
      customField = MigrationUtility.getCustomFieldName(customField5Name);
      if (customField != null) {
        velocityParams.put("customField5Name", customField.getName());
      } else {
        velocityParams.put("customField5Name", (customField5Name == null) ? "" : customField5Name);
      } 
      String customField5Value1 = (String)descriptor.getArgs().get("field.customField5Value1");
      velocityParams.put("customField5Value1", (customField5Value1 == null) ? "" : customField5Value1);
      String customField5Value2 = (String)descriptor.getArgs().get("field.customField5Value2");
      velocityParams.put("customField5Value2", (customField5Value2 == null) ? "" : customField5Value2);

      
      String name = (String)descriptor.getArgs().get("field.customField10Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField10Name", customField.getName());
      } else {
        velocityParams.put("customField10Name", (name == null) ? "" : name);
      } 
      String value = (String)descriptor.getArgs().get("field.customField10Value1");
      velocityParams.put("customField10Value1", (value == null) ? "" : value);
      String value2 = (String)descriptor.getArgs().get("field.customField10Value2");
      velocityParams.put("customField10Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField11Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField11Name", customField.getName());
      } else {
        velocityParams.put("customField11Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField11Value1");
      velocityParams.put("customField11Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField11Value2");
      velocityParams.put("customField11Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField12Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField12Name", customField.getName());
      } else {
        velocityParams.put("customField12Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField12Value1");
      velocityParams.put("customField12Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField12Value2");
      velocityParams.put("customField12Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField13Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField13Name", customField.getName());
      } else {
        velocityParams.put("customField13Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField13Value1");
      velocityParams.put("customField13Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField13Value2");
      velocityParams.put("customField13Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField14Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField14Name", customField.getName());
      } else {
        velocityParams.put("customField14Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField14Value1");
      velocityParams.put("customField14Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField14Value2");
      velocityParams.put("customField14Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField15Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField15Name", customField.getName());
      } else {
        velocityParams.put("customField15Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField15Value1");
      velocityParams.put("customField15Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField15Value2");
      velocityParams.put("customField15Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField16Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField16Name", customField.getName());
      } else {
        velocityParams.put("customField16Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField16Value1");
      velocityParams.put("customField16Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField16Value2");
      velocityParams.put("customField16Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField17Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField17Name", customField.getName());
      } else {
        velocityParams.put("customField17Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField17Value1");
      velocityParams.put("customField17Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField17Value2");
      velocityParams.put("customField17Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField18Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField18Name", customField.getName());
      } else {
        velocityParams.put("customField18Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField18Value1");
      velocityParams.put("customField18Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField18Value2");
      velocityParams.put("customField18Value2", (value2 == null) ? "" : value2);
      
      name = (String)descriptor.getArgs().get("field.customField19Name");
      customField = MigrationUtility.getCustomFieldName(name);
      if (customField != null) {
        velocityParams.put("customField19Name", customField.getName());
      } else {
        velocityParams.put("customField19Name", (name == null) ? "" : name);
      } 
      value = (String)descriptor.getArgs().get("field.customField19Value1");
      velocityParams.put("customField19Value1", (value == null) ? "" : value);
      value2 = (String)descriptor.getArgs().get("field.customField19Value2");
      velocityParams.put("customField19Value2", (value2 == null) ? "" : value2);

      
      String jqlQuery = (String)descriptor.getArgs().get("field.jqlQuery");
      velocityParams.put("jqlQuery", (jqlQuery == null) ? "" : jqlQuery);

      
      velocityParams.put("multipleIssuesPattern", descriptor.getArgs().get("field.multipleIssuesPattern"));
      velocityParams.put("multipleIssuesValue", descriptor.getArgs().get("field.multipleIssuesValue"));
      velocityParams.put("multipleIssuesExact", descriptor.getArgs().get("field.multipleIssuesExact"));
      velocityParams.put("multipleIssuesLiteral", descriptor.getArgs().get("field.multipleIssuesLiteral"));
      velocityParams.put("multipleIssuesReverse", descriptor.getArgs().get("field.multipleIssuesReverse"));

      
      velocityParams.put("acting", descriptor.getArgs().get("field.acting"));

      
      velocityParams.put("environment", descriptor.getArgs().get("field.environment"));

      
      velocityParams.put("notes", descriptor.getArgs().get("field.notes"));
    } 
  }








  
  protected void getVelocityParamsForInput(Map velocityParams) {
    velocityParams.put("licenseErrorMessage", 
        LicenseUtilities.validateLicense(this.licenseManager, this.jiraLicenseService, this.i18nResolver, "org.swift.jira.cot", true));
    
    velocityParams.put("conditionPattern1", "");
    velocityParams.put("conditionValue1", "");
    velocityParams.put("conditionPattern2", "");
    velocityParams.put("conditionValue2", "");

    
    velocityParams.put("labels", "");
    velocityParams.put("currentLabels", "");

    
    velocityParams.put("watchers", "");
    velocityParams.put("currentWatchers", "");

    
    velocityParams.put("linkKey", "");
    velocityParams.put("currentLinkKey", "");
    velocityParams.put("linkType", "");
    velocityParams.put("currentLinkType", "");
    velocityParams.put("linkDirection", "");
    velocityParams.put("currentLinkType", "");

    
    velocityParams.put("copyLinksFrom", "");
    velocityParams.put("currentCopyLinksFrom", "");
    velocityParams.put("copyLinksTypes", "");
    velocityParams.put("currentCopyLinksTypes", "");
    velocityParams.put("copyRemoteLinks", "");
    velocityParams.put("currentCopyRemoteLinks", "");
    velocityParams.put("copyRemoteLinksType", "");
    velocityParams.put("currentCopyRemoteLinksType", "");


    
    velocityParams.put("copyAttachments", "");

    
    velocityParams.put("comment", "");
    velocityParams.put("currentComment", "");
    velocityParams.put("commentSecurity", "");
    velocityParams.put("currentCommentSecurity", "");

    
    velocityParams.put("dateFormat", getExampleDateFormat());

    
    velocityParams.put("timeDurationFormat", getExampleTimeDurationFormat());

    
    velocityParams.put("currentCopyParentFields", "");
    velocityParams.put("currentCopyOriginalFields", "");
    velocityParams.put("customFieldIds", "customField1,customField2,customField3,customField4,customField5,customField10,customField11,customField12,customField13,customField14,customField15,customField16,customField17,customField18,customField19");



    
    velocityParams.put("customField1Name", "");
    velocityParams.put("customField1Value1", "");
    velocityParams.put("customField1Value2", "");

    
    velocityParams.put("customField2Name", "");
    velocityParams.put("customField2Value1", "");
    velocityParams.put("customField2Value2", "");

    
    velocityParams.put("customField3Name", "");
    velocityParams.put("customField3Value1", "");
    velocityParams.put("customField3Value2", "");

    
    velocityParams.put("customField4Name", "");
    velocityParams.put("customField4Value1", "");
    velocityParams.put("customField4Value2", "");

    
    velocityParams.put("customField5Name", "");
    velocityParams.put("customField5Value1", "");
    velocityParams.put("customField5Value2", "");

    
    velocityParams.put("customField10Name", "");
    velocityParams.put("customField10Value1", "");
    velocityParams.put("customField10Value2", "");
    
    velocityParams.put("customField11Name", "");
    velocityParams.put("customField11Value1", "");
    velocityParams.put("customField11Value2", "");
    
    velocityParams.put("customField12Name", "");
    velocityParams.put("customField12Value1", "");
    velocityParams.put("customField12Value2", "");
    
    velocityParams.put("customField13Name", "");
    velocityParams.put("customField13Value1", "");
    velocityParams.put("customField13Value2", "");
    
    velocityParams.put("customField14Name", "");
    velocityParams.put("customField14Value1", "");
    velocityParams.put("customField14Value2", "");
    
    velocityParams.put("customField15Name", "");
    velocityParams.put("customField15Value1", "");
    velocityParams.put("customField15Value2", "");
    
    velocityParams.put("customField16Name", "");
    velocityParams.put("customField16Value1", "");
    velocityParams.put("customField16Value2", "");
    
    velocityParams.put("customField17Name", "");
    velocityParams.put("customField17Value1", "");
    velocityParams.put("customField17Value2", "");
    
    velocityParams.put("customField18Name", "");
    velocityParams.put("customField18Value1", "");
    velocityParams.put("customField18Value2", "");
    
    velocityParams.put("customField19Name", "");
    velocityParams.put("customField19Value1", "");
    velocityParams.put("customField19Value2", "");

    
    velocityParams.put("currentJqlQuery", "");

    
    velocityParams.put("multipleIssuesPattern", "");
    velocityParams.put("multipleIssuesValue", "");

    
    velocityParams.put("acting", "");

    
    velocityParams.put("environment", "");

    
    velocityParams.put("notes", "");
  }









  
  protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor inDescriptor) {
    velocityParams.put("licenseErrorMessage", 
        LicenseUtilities.validateLicense(this.licenseManager, this.jiraLicenseService, this.i18nResolver, "org.swift.jira.cot", true));
    
    if (!(inDescriptor instanceof FunctionDescriptor)) {
      throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
    }
    
    FunctionDescriptor descriptor = (FunctionDescriptor)inDescriptor;

    
    String currentConditionPattern1 = (String)descriptor.getArgs().get("field.conditionPattern1");
    velocityParams.put("currentConditionPattern1", (currentConditionPattern1 == null) ? "" : currentConditionPattern1);
    String currentConditionValue1 = (String)descriptor.getArgs().get("field.conditionValue1");
    velocityParams.put("currentConditionValue1", (currentConditionValue1 == null) ? "" : currentConditionValue1);
    String currentConditionExact1 = (String)descriptor.getArgs().get("field.conditionExact1");
    velocityParams.put("currentConditionExact1", (currentConditionExact1 == null) ? "" : currentConditionExact1);
    String currentConditionLiteral1 = (String)descriptor.getArgs().get("field.conditionLiteral1");
    velocityParams.put("currentConditionLiteral1", (currentConditionLiteral1 == null) ? "" : currentConditionLiteral1);
    String currentConditionReverse1 = (String)descriptor.getArgs().get("field.conditionReverse1");
    velocityParams.put("currentConditionReverse1", (currentConditionReverse1 == null) ? "" : currentConditionReverse1);
    
    String currentConditionPattern2 = (String)descriptor.getArgs().get("field.conditionPattern2");
    velocityParams.put("currentConditionPattern2", (currentConditionPattern2 == null) ? "" : currentConditionPattern2);
    String currentConditionValue2 = (String)descriptor.getArgs().get("field.conditionValue2");
    velocityParams.put("currentConditionValue2", (currentConditionValue2 == null) ? "" : currentConditionValue2);
    String currentConditionExact2 = (String)descriptor.getArgs().get("field.conditionExact2");
    velocityParams.put("currentConditionExact2", (currentConditionExact2 == null) ? "" : currentConditionExact2);
    String currentConditionLiteral2 = (String)descriptor.getArgs().get("field.conditionLiteral2");
    velocityParams.put("currentConditionLiteral2", (currentConditionLiteral2 == null) ? "" : currentConditionLiteral2);
    String currentConditionReverse2 = (String)descriptor.getArgs().get("field.conditionReverse2");
    velocityParams.put("currentConditionReverse2", (currentConditionReverse2 == null) ? "" : currentConditionReverse2);

    
    String labels = (String)descriptor.getArgs().get("field.labels");
    velocityParams.put("currentLabels", (labels == null) ? "" : labels);

    
    String watchers = (String)descriptor.getArgs().get("field.watchers");
    velocityParams.put("currentWatchers", (watchers == null) ? "" : watchers);

    
    String linkKey = (String)descriptor.getArgs().get("field.linkKey");
    velocityParams.put("currentLinkKey", (linkKey == null) ? "" : linkKey);
    String linkType = (String)descriptor.getArgs().get("field.linkType");
    velocityParams.put("currentLinkType", (linkType == null) ? "" : linkType);
    String linkDirection = (String)descriptor.getArgs().get("field.linkDirection");
    velocityParams.put("currentLinkDirection", (linkDirection == null) ? "" : linkDirection);

    
    String copyLinksFrom = (String)descriptor.getArgs().get("field.copyLinksFrom");
    velocityParams.put("currentCopyLinksFrom", (copyLinksFrom == null) ? "" : copyLinksFrom);
    String copyLinksTypes = (String)descriptor.getArgs().get("field.copyLinksTypes");
    velocityParams.put("currentCopyLinksTypes", (copyLinksTypes == null) ? "" : copyLinksTypes);
    String copyRemoteLinks = (String)descriptor.getArgs().get("field.copyRemoteLinks");
    velocityParams.put("currentCopyRemoteLinks", (copyRemoteLinks == null) ? "" : copyRemoteLinks);
    String copyRemoteLinksType = (String)descriptor.getArgs().get("field.copyRemoteLinksType");
    velocityParams.put("currentCopyRemoteLinksType", (copyRemoteLinksType == null) ? "" : copyRemoteLinksType);

    
    String copyAttachments = (String)descriptor.getArgs().get("field.copyAttachments");
    velocityParams.put("currentCopyAttachments", (copyAttachments == null) ? "" : copyAttachments);
    String currentCopyTransitionAttachments = (String)descriptor.getArgs().get("field.copyTransitionAttachments");
    velocityParams.put("currentCopyTransitionAttachments", (currentCopyTransitionAttachments == null) ? "" : currentCopyTransitionAttachments);

    
    String comment = (String)descriptor.getArgs().get("field.comment");
    velocityParams.put("currentComment", (comment == null) ? "" : comment);
    String commentSecurity = (String)descriptor.getArgs().get("field.commentSecurity");
    velocityParams.put("currentCommentSecurity", (commentSecurity == null) ? "" : commentSecurity);
    String currentCopyParentComments = (String)descriptor.getArgs().get("field.copyParentComments");
    velocityParams.put("currentCopyParentComments", (currentCopyParentComments == null) ? "" : currentCopyParentComments);
    String currentCopyOriginalComments = (String)descriptor.getArgs().get("field.copyOriginalComments");
    velocityParams.put("currentCopyOriginalComments", (currentCopyOriginalComments == null) ? "" : currentCopyOriginalComments);

    
    String specificIssueType = (String)descriptor.getArgs().get("field.specificIssueType");
    velocityParams.put("currentSpecificIssueType", (specificIssueType == null) ? "" : specificIssueType);

    
    String specificPriority = (String)descriptor.getArgs().get("field.specificPriority");
    velocityParams.put("currentSpecificPriority", (specificPriority == null) ? "" : specificPriority);

    
    String specificReporter = (String)descriptor.getArgs().get("field.specificReporter");
    velocityParams.put("currentSpecificReporter", (specificReporter == null) ? "" : specificReporter);

    
    String specificAssignee = (String)descriptor.getArgs().get("field.specificAssignee");
    velocityParams.put("currentSpecificAssignee", (specificAssignee == null) ? "" : specificAssignee);

    
    String specificAffectedVersions = (String)descriptor.getArgs().get("field.specificAffectedVersions");
    velocityParams.put("currentSpecificAffectedVersions", (specificAffectedVersions == null) ? "" : specificAffectedVersions);

    
    String specificFixedVersions = (String)descriptor.getArgs().get("field.specificFixedVersions");
    velocityParams.put("currentSpecificFixedVersions", (specificFixedVersions == null) ? "" : specificFixedVersions);

    
    String specificComponents = (String)descriptor.getArgs().get("field.specificComponents");
    velocityParams.put("currentSpecificComponents", (specificComponents == null) ? "" : specificComponents);

    
    String specificDueDate = (String)descriptor.getArgs().get("field.specificDueDate");
    velocityParams.put("currentSpecificDueDate", (specificDueDate == null) ? "" : specificDueDate);
    velocityParams.put("dateFormat", getExampleDateFormat());

    
    velocityParams.put("dateFormat", getExampleDateFormat());
    
    String dueDateOffset = (String)descriptor.getArgs().get("field.dueDateOffset");
    velocityParams.put("currentDueDateOffset", (dueDateOffset == null) ? "" : dueDateOffset);

    
    velocityParams.put("timeDurationFormat", getExampleTimeDurationFormat());


    
    String copyParentFields = (String)descriptor.getArgs().get("field.copyParentFields");
    String copyParentFieldsUpdated = getCustomFieldNamesUpdated(copyParentFields);
    velocityParams.put("currentCopyParentFields", copyParentFieldsUpdated);
    String copyOriginalFields = (String)descriptor.getArgs().get("field.copyOriginalFields");
    String copyOriginalFieldsUpdated = getCustomFieldNamesUpdated(copyOriginalFields);
    velocityParams.put("currentCopyOriginalFields", copyOriginalFieldsUpdated);

    
    String customFieldIdsCsv = "";
    
    String customField1Name = (String)descriptor.getArgs().get("field.customField1Name");
    if (StringUtils.isBlank(customField1Name)) {
      customFieldIdsCsv = "customField1,";
    }
    CustomField customField = MigrationUtility.getCustomFieldName(customField1Name);
    if (customField != null) {
      velocityParams.put("currentCustomField1Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField1Name", (customField1Name == null) ? "" : customField1Name);
    } 
    
    String customField1Value1 = (String)descriptor.getArgs().get("field.customField1Value1");
    velocityParams.put("currentCustomField1Value1", (customField1Name == null) ? "" : customField1Value1);
    String customField1Value2 = (String)descriptor.getArgs().get("field.customField1Value2");
    velocityParams.put("currentCustomField1Value2", (customField1Name == null) ? "" : customField1Value2);

    
    String customField2Name = (String)descriptor.getArgs().get("field.customField2Name");
    if (StringUtils.isBlank(customField2Name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField2,";
    }
    customField = MigrationUtility.getCustomFieldName(customField2Name);
    if (customField != null) {
      velocityParams.put("currentCustomField2Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField2Name", (customField2Name == null) ? "" : customField2Name);
    } 
    String customField2Value1 = (String)descriptor.getArgs().get("field.customField2Value1");
    velocityParams.put("currentCustomField2Value1", (customField2Name == null) ? "" : customField2Value1);
    String customField2Value2 = (String)descriptor.getArgs().get("field.customField2Value2");
    velocityParams.put("currentCustomField2Value2", (customField2Name == null) ? "" : customField2Value2);

    
    String customField3Name = (String)descriptor.getArgs().get("field.customField3Name");
    if (StringUtils.isBlank(customField3Name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField3,";
    }
    customField = MigrationUtility.getCustomFieldName(customField3Name);
    if (customField != null) {
      velocityParams.put("currentCustomField3Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField3Name", (customField3Name == null) ? "" : customField3Name);
    } 
    String customField3Value1 = (String)descriptor.getArgs().get("field.customField3Value1");
    velocityParams.put("currentCustomField3Value1", (customField3Name == null) ? "" : customField3Value1);
    String customField3Value2 = (String)descriptor.getArgs().get("field.customField3Value2");
    velocityParams.put("currentCustomField3Value2", (customField3Name == null) ? "" : customField3Value2);

    
    String customField4Name = (String)descriptor.getArgs().get("field.customField4Name");
    if (StringUtils.isBlank(customField4Name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField4,";
    }
    customField = MigrationUtility.getCustomFieldName(customField4Name);
    if (customField != null) {
      velocityParams.put("currentCustomField4Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField4Name", (customField4Name == null) ? "" : customField4Name);
    } 
    String customField4Value1 = (String)descriptor.getArgs().get("field.customField4Value1");
    velocityParams.put("currentCustomField4Value1", (customField4Name == null) ? "" : customField4Value1);
    String customField4Value2 = (String)descriptor.getArgs().get("field.customField4Value2");
    velocityParams.put("currentCustomField4Value2", (customField4Name == null) ? "" : customField4Value2);

    
    String customField5Name = (String)descriptor.getArgs().get("field.customField5Name");
    if (StringUtils.isBlank(customField5Name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField5,";
    }
    customField = MigrationUtility.getCustomFieldName(customField5Name);
    if (customField != null) {
      velocityParams.put("currentCustomField5Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField5Name", (customField5Name == null) ? "" : customField5Name);
    } 
    String customField5Value1 = (String)descriptor.getArgs().get("field.customField5Value1");
    velocityParams.put("currentCustomField5Value1", (customField5Name == null) ? "" : customField5Value1);
    String customField5Value2 = (String)descriptor.getArgs().get("field.customField5Value2");
    velocityParams.put("currentCustomField5Value2", (customField5Name == null) ? "" : customField5Value2);

    
    String name = (String)descriptor.getArgs().get("field.customField10Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField10,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField10Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField10Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField10Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField10Value1"));
    velocityParams.put("currentCustomField10Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField10Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField11Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField11,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField11Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField11Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField11Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField11Value1"));
    velocityParams.put("currentCustomField11Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField11Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField12Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField12,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField12Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField12Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField12Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField12Value1"));
    velocityParams.put("currentCustomField12Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField12Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField13Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField13,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField13Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField13Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField13Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField13Value1"));
    velocityParams.put("currentCustomField13Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField13Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField14Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField14,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField14Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField14Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField14Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField14Value1"));
    velocityParams.put("currentCustomField13Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField13Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField15Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField15,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField15Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField15Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField15Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField15Value1"));
    velocityParams.put("currentCustomField15Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField15Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField16Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField16,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField16Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField16Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField16Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField16Value1"));
    velocityParams.put("currentCustomField16Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField16Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField17Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField17,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField17Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField17Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField17Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField17Value1"));
    velocityParams.put("currentCustomField17Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField17Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField18Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField18,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField18Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField18Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField18Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField18Value1"));
    velocityParams.put("currentCustomField18Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField18Value2"));
    
    name = (String)descriptor.getArgs().get("field.customField19Name");
    if (StringUtils.isBlank(name)) {
      customFieldIdsCsv = customFieldIdsCsv + "customField19,";
    }
    customField = MigrationUtility.getCustomFieldName(name);
    if (customField != null) {
      velocityParams.put("currentCustomField19Name", customField.getName());
    } else {
      velocityParams.put("currentCustomField19Name", (name == null) ? "" : name);
    } 
    velocityParams.put("currentCustomField19Value1", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField19Value1"));
    velocityParams.put("currentCustomField19Value2", (name == null) ? "" : (String)descriptor.getArgs().get("field.customField19Value2"));
    
    String customFieldIds = (String)descriptor.getArgs().get("field.copyCustomFieldIds");
    velocityParams.put("currentCustomFieldIds", (customFieldIds == null || customFieldIds.equals("")) ? customFieldIdsCsv : customFieldIds);

    
    String jqlQuery = (String)descriptor.getArgs().get("field.jqlQuery");
    velocityParams.put("currentJqlQuery", (jqlQuery == null) ? "" : jqlQuery);

    
    String currentMultipleIssuesPattern = (String)descriptor.getArgs().get("field.multipleIssuesPattern");
    velocityParams.put("currentMultipleIssuesPattern", (currentMultipleIssuesPattern == null) ? "" : currentMultipleIssuesPattern);
    String currentMultipleIssuesValue = (String)descriptor.getArgs().get("field.multipleIssuesValue");
    velocityParams.put("currentMultipleIssuesValue", (currentMultipleIssuesValue == null) ? "" : currentMultipleIssuesValue);
    String currentMultipleIssuesExact = (String)descriptor.getArgs().get("field.multipleIssuesExact");
    velocityParams.put("currentMultipleIssuesExact", (currentMultipleIssuesExact == null) ? "" : currentMultipleIssuesExact);
    String currentMultipleIssuesLiteral = (String)descriptor.getArgs().get("field.multipleIssuesLiteral");
    velocityParams.put("currentMultipleIssuesLiteral", (currentMultipleIssuesLiteral == null) ? "" : currentMultipleIssuesLiteral);
    String currentMultipleIssuesReverse = (String)descriptor.getArgs().get("field.multipleIssuesReverse");
    velocityParams.put("currentMultipleIssuesReverse", (currentMultipleIssuesReverse == null) ? "" : currentMultipleIssuesReverse);

    
    String acting = (String)descriptor.getArgs().get("field.acting");
    velocityParams.put("currentActing", (acting == null) ? "" : acting);

    
    String environment = (String)descriptor.getArgs().get("field.environment");
    velocityParams.put("currentEnvironment", (environment == null) ? "" : environment);

    
    String notes = (String)descriptor.getArgs().get("field.notes");
    velocityParams.put("currentNotes", (notes == null) ? "" : notes);
  }









  
  protected String getExampleDateFormat() { return this.applicationProperties.getDefaultBackedString("jira.lf.date.dmy"); }







  
  protected String getExampleTimeDurationFormat() {
    JiraDurationUtils jiraDurationUtils = (JiraDurationUtils)ComponentLocator.getComponent(JiraDurationUtils.class);
    
    Locale defaultLocale = LocaleParser.parseLocale(this.applicationProperties.getString("jira.i18n.default.locale"));
    if (defaultLocale == null) {
      defaultLocale = Locale.ENGLISH;
    }
    
    return jiraDurationUtils.getShortFormattedDuration(Long.valueOf(94500L), defaultLocale);
  }
  
  protected String getCustomFieldNamesUpdated(String fieldNamesCsv) {
    Collection<String> parentFieldList = CsvUtilities.csvDataAsList(fieldNamesCsv, false, ',', '\'', '\n');
    Collection<String> parentFieldListNames = new ArrayList<String>();
    for (String eachParentFieldList : parentFieldList) {
      if (!eachParentFieldList.startsWith("'") || eachParentFieldList.endsWith("'"));

      
      CustomField field = MigrationUtility.getCustomFieldName(eachParentFieldList);
      if (field != null) {
        parentFieldListNames.add(field.getName()); continue;
      } 
      parentFieldListNames.add(eachParentFieldList);
    } 
    
    return Utilities.collectionToSeparatedString(parentFieldListNames, ",");
  }
}
