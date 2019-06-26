package org.swift.jira.cot.functions;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
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
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.opensymphony.module.propertyset.PropertySet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swift.jira.cot.utilities.Choices;
import org.swift.jira.cot.utilities.Helper;
import org.swift.jira.cot.utilities.JSDPermissionHelper;
import org.swift.jira.library.AbstractPostFunction;
import org.swift.jira.library.CsvUtilities;
import org.swift.jira.library.DefaultReplaceHelper;
import org.swift.jira.library.LabelHelper;
import org.swift.jira.library.Utilities;





















public abstract class AbstractCreateFunction
  extends AbstractPostFunction
{
  protected final Logger log = LoggerFactory.getLogger(getClass());
  
  public static final String COT_PACKAGE = "org.swift.jira.cot.";
  
  public static final String COT_LAST_CREATED_NON_SUBTASK_KEY = "org.swift.jira.cot.last_created_non_subtask_key";
  public static final String COT_LAST_CREATED_KEY = "org.swift.jira.cot.last_created_key";
  public static final String COT_CREATED_KEY_LIST = "org.swift.jira.cot.created_key_list";
  public static final String PARENT_PREFIX = "parent_";
  public static final String ORIGINAL_PREFIX = "original_";
  public static final int MAX_CHAR = 255;
  public static final String REGEX_NON_NUMBERS = "[^0-9-]";
  public static final String PARENT_KEY = null;



  
  public static final String CHILD_KEY = "1";



  
  protected final Choices choices;




  
  protected AbstractCreateFunction(CustomFieldManager customFieldManager, SubTaskManager subTaskManager, IssueManager issueManager, IssueFactory issueFactory, ConstantsManager constantsManager, ApplicationProperties applicationProperties, UserUtil userUtil, GroupManager groupManager, ProjectRoleManager projectRoleManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, I18nHelper.BeanFactory i18nBeanFactory, EventPublisher eventPublisher, BuildUtilsInfo buildInfo, OptionsManager optionsManager, WatcherManager watcherManager, IssueLinkManager issueLinkManager, IssueLinkTypeManager issueLinkTypeManager, IssueSecurityLevelManager issueSecurityLevelManager, IssueTypeSchemeManager issueTypeSchemeManager, AttachmentManager attachmentManager, AttachmentPathManager attachmentPathManager, CommentManager commentManager, VersionManager versionManager, ProjectManager projectManager, SearchService searchService, JiraHome jiraHome, I18nResolver i18nResolver, PluginLicenseManager licenseManager, JiraLicenseService jiraLicenseService) {
    super(customFieldManager, subTaskManager, issueManager, issueFactory, constantsManager, applicationProperties, userUtil, groupManager, projectRoleManager, permissionManager, authenticationContext, i18nBeanFactory, eventPublisher, buildInfo, optionsManager, watcherManager, issueLinkManager, issueLinkTypeManager, issueSecurityLevelManager, issueTypeSchemeManager, attachmentManager, attachmentPathManager, commentManager, versionManager, projectManager, searchService, jiraHome, i18nResolver, licenseManager, jiraLicenseService);



    
    this.choices = new Choices(i18nResolver);
  }


  
  protected String getI18nPrefix() { return "org.swift.jira.cot"; }









  
  protected void process(Map<String, Object> transientVariables, Map<String, String> args, PropertySet propertySet) {
    Helper helper = new Helper(this.issueManager, this.customFieldManager, this.applicationProperties, this.watcherManager, this.issueSecurityLevelManager, this.groupManager, this.versionManager, this.projectManager, this.projectRoleManager, this.optionsManager, this.authenticationContext, this.searchService, this.i18nResolver, this.issueLinkManager);

    
    helper.getReplaceHelper().setTransientVariables(transientVariables);
    helper.getReplaceHelper().setArgs(args);
    helper.getReplaceHelper().setPropertySet(propertySet);
    helper.getReplaceHelper().setBlankPrefixIssue(helper.getReplaceHelper().getParentIssue());
    helper.getReplaceHelper().setAllowMethods(true);
    
    try {
      if (verifyPreConditions(helper)) {
        helper.getJqlHelper().defineJqlSubstitutionVariables(Boolean.valueOf(false));
        if (verifyPreConditionsAfterJql(helper)) {
          createIssues(helper);
        }
      } 
    } catch (Exception exception) {
      this.log.error("Unexpected exception: {}", exception.toString(), exception);
    } 
  }









  
  protected void createIssues(Helper helper) {
    List<String> entryList = getEntryList(helper);
    
    if (entryList == null) {
      createIssue(helper);
    } else {
      for (String entry : entryList) {
        helper.getReplaceHelper().setEntry(entry);
        helper.getJqlHelper().defineJqlSubstitutionVariables(Boolean.valueOf(true));
        createIssue(helper);
      } 
    } 
  }








  
  protected void createIssue(Helper helper) {
    this.log.debug("Create issue based on original issue: {}, parent: {}", helper.getReplaceHelper().getOriginalIssue().getKey(), helper.getReplaceHelper()
        .getParentIssue().getKey());
    
    try {
      if (helper.getConditionHelper().verifyConditions()) {
        
        MutableIssue newIssue = this.issueFactory.getIssue();
        
        helper.getReplaceHelper().setEntryIssue(newIssue);
        setFields(helper, newIssue);
        
        if (newIssue.getProjectObject() != null) {
          if (newIssue.getIssueType() != null) {
            if (hasCreateIssuePermission(newIssue.getProjectObject(), helper.getReplaceHelper())) {

              
              Map<String, Object> params = new HashMap<String, Object>();
              params.put("issue", newIssue);
              
              try {
                ApplicationUser user = helper.getReplaceHelper().getTransitionUser();
                if (user == null)
                {
                  user = ComponentAccessor.getUserManager().getUserByKey(helper.getReplaceHelper().getOriginalIssue().getReporterId());
                }
                
                if (user == null) {
                  this.log.error("No user could be determined. Could not create issue.");
                } else {
                  Issue issue = this.issueManager.createIssueObject(user.getName(), params);
                  this.log.debug("Issue has been created with key: {}", newIssue.getKey());
                  doPostTasks(helper, issue);
                  this.log.debug("Post tasks complete for: {}", newIssue.getKey());
                  setCreatedKey(helper.getReplaceHelper().getPropertySet(), newIssue.getKey(), newIssue.isSubTask());
                } 
              } catch (CreateException exception) {
                this.log.error("Could not create issue due to exception: {}", exception.toString(), exception);
              } catch (Exception exception) {
                this.log.error("Unexpected exception: {}", exception.toString(), exception);
              } 
            } else {
              this.log.error("Could not create issue during workflow transition for {} since '{}' does not have 'Create Issue' permission.", helper
                  .getReplaceHelper().getOriginalIssue().getKey(), Utilities.getTransitionUserName(helper.getReplaceHelper()));
            } 
          } else {
            this.log.error("Cound not create issue during workflow transition for {} due to error finding configured issue type.", helper
                .getReplaceHelper().getOriginalIssue().getKey());
          } 
        } else {
          this.log.error("Could not create issue during workflow transition for {} due to error finding configured project.", helper.getReplaceHelper()
              .getOriginalIssue().getKey());
        } 
      } 
    } catch (Exception exception) {
      this.log.error("Unexpected exception: {}", exception.toString(), exception);
    } 
  }










  
  protected abstract void setFields(Helper paramHelper, MutableIssue paramMutableIssue);









  
  protected void doPostTasks(Helper helper, Issue newIssue) {
    Map<String, String> args = helper.getReplaceHelper().getArgs();

    
    addLink(newIssue, helper.getReplaceHelper().findReplace((String)args.get("field.linkKey")), helper
        .getReplaceHelper().findReplace((String)args.get("field.linkType")), helper
        .getReplaceHelper().findReplace((String)args.get("field.linkDirection")), helper.getReplaceHelper());

    
    copyLinks(newIssue, helper.getReplaceHelper().findReplace((String)args.get("field.copyLinksFrom")), helper
        .getReplaceHelper().findReplace((String)args.get("field.copyLinksTypes")), helper
        .getReplaceHelper(), true, true);

    
    if (StringUtils.isNotBlank(helper.getReplaceHelper().findReplace((String)args.get("field.copyRemoteLinksType")))) {
      for (String linkType : helper.getReplaceHelper().findReplace((String)args.get("field.copyRemoteLinksType")).split(",")) {
        linkType = linkType.trim();
        copyRemoteIssueLinks(newIssue, helper.getReplaceHelper().findReplace((String)args.get("field.copyRemoteLinks")), linkType);
      } 
    } else {
      copyRemoteIssueLinks(newIssue, helper.getReplaceHelper().findReplace((String)args.get("field.copyRemoteLinks")), "");
    } 

    
    String attachmentIds = helper.getReplaceHelper().findReplace("%transition_attachment%");
    if ("1".equals(args.get("field.copyTransitionAttachments"))) {
      Issue fromIssue = helper.getReplaceHelper().getOriginalIssue();
      if (fromIssue == null) {
        this.log.error("Attachments {} not found for copy. Ignore.", attachmentIds);
      } else {
        copyTransitionAttachments(fromIssue, newIssue, attachmentIds, helper.getReplaceHelper());
      } 
    } 

    
    String fromKey = helper.getReplaceHelper().findReplace((String)args.get("field.copyAttachments"));
    if (!StringUtils.isBlank(fromKey)) {
      MutableIssue mutableIssue = this.issueManager.getIssueObject(fromKey);
      if (mutableIssue == null) {
        this.log.error("{} not found for copy. Ignore.", fromKey);
      }
      else if (mutableIssue.getKey().equals(helper.getReplaceHelper().getParentIssue().getKey()) || mutableIssue
        .getKey().equals(helper.getReplaceHelper().getOriginalIssue().getKey())) {
        List<String> finalAttachmentIds = new ArrayList<String>();
        for (Attachment attachment : this.attachmentManager.getAttachments(mutableIssue)) {
          if (!attachmentIds.contains(attachment.getId().toString())) {
            finalAttachmentIds.add(attachment.getId().toString());
          }
        } 
        copyTransitionAttachments(mutableIssue, newIssue, Utilities.collectionToSeparatedString(finalAttachmentIds, ","), helper.getReplaceHelper());
      } else {
        copyAttachments(mutableIssue, newIssue, helper.getReplaceHelper());
      } 
    } 



    
    String watchers = (String)args.get("field.watchers");
    if (StringUtils.isNotBlank(watchers)) {
      setWatchers(newIssue, helper.getReplaceHelper().findReplace(watchers), helper.getReplaceHelper());
    }

    
    if ("1".equals(args.get("field.copyOriginalComments")) || "1"
      .equals(args.get("field.copyParentComments"))) {
      Issue fromOriginalIssue = null;
      Issue fromParentIssue = null;
      if ("1".equals(args.get("field.copyOriginalComments"))) {
        fromOriginalIssue = helper.getReplaceHelper().getOriginalIssue();
      }
      
      if ("1".equals(args.get("field.copyParentComments"))) {
        fromParentIssue = helper.getReplaceHelper().getParentIssue();
      }
      
      if (fromOriginalIssue == null && fromParentIssue == null) {
        this.log.error("Parent and original issue not found for copy. Ignore.");
      } else {
        CommentManager commentManager = ComponentAccessor.getCommentManager();
        List<Comment> commentsOfOriginalAndParentIssue = new ArrayList<Comment>();
        if (fromOriginalIssue != null) {
          commentsOfOriginalAndParentIssue.addAll(commentManager.getComments(fromOriginalIssue));
        }
        if (fromParentIssue != null && (
          fromOriginalIssue == null || (fromOriginalIssue != null && 
          !fromOriginalIssue.getKey().equals(fromParentIssue.getKey())))) {
          commentsOfOriginalAndParentIssue.addAll(commentManager.getComments(fromParentIssue));
        }
        
        if (!commentsOfOriginalAndParentIssue.isEmpty()) {
          addCommentsFromParentOrOriginalIssue(commentsOfOriginalAndParentIssue, newIssue);
        }
      } 
    } 


    
    addCommentWithSecurity(newIssue, helper
        .getReplaceHelper().findReplace((String)args.get("field.comment")), helper
        .getReplaceHelper().findReplace((String)args.get("field.commentSecurity")), helper
        .getReplaceHelper());
  }











  
  protected void addCommentWithSecurity(Issue newIssue, String comment, String commentSecurity, DefaultReplaceHelper replaceHelper) {
    if (!StringUtils.isBlank(commentSecurity) && !StringUtils.isBlank(comment)) {
      
      try {
        if (this.applicationProperties.getOption("jira.comment.level.visibility.groups") && 
          isValidUserGroupName(commentSecurity, Utilities.getTransitionUserName(replaceHelper)))
        { this.commentManager.create(newIssue, replaceHelper.getTransitionUser(), comment, commentSecurity, null, true);
          this.log.debug("{} had comment added: {} with restriction: " + commentSecurity, newIssue.getKey(), comment); }
        else { Long roleId; if ((roleId = getValidUserProjectRoleId(commentSecurity, replaceHelper.getTransitionUser(), newIssue.getProjectObject())).longValue() != 0L)
          { this.commentManager.create(newIssue, replaceHelper.getTransitionUser(), comment, null, roleId, true);
            this.log.debug("{} had comment added: {} with restriction: " + commentSecurity, newIssue.getKey(), comment); }
          else
          { this.log.error("Invalid group name or role name/id provided : {}. Comment was skipped !", commentSecurity); }  }
      
      } catch (Exception exception) {
        this.log.error("Unable to create comment for {} with restriction" + commentSecurity + ". Exception: {}", newIssue.getKey(), exception.getMessage());
      } 
    } else {
      addComment(newIssue, comment, replaceHelper);
    } 
  }







  
  protected boolean isValidUserGroupName(String groupName, String username) {
    Group group = this.groupManager.getGroup(groupName);
    if (group != null && this.groupManager
      .isUserInGroup(ComponentAccessor.getUserManager().getUserByKey(username), group.getName())) {
      return true;
    }
    return false;
  }








  
  protected Long getValidUserProjectRoleId(String name, ApplicationUser user, Project project) {
    ProjectRole projectRole = null;
    
    projectRole = this.projectRoleManager.getProjectRole(Long.valueOf(Utilities.getLong(name, 0)));
    if (projectRole == null)
    {
      projectRole = this.projectRoleManager.getProjectRole(name);
    }
    if (projectRole != null && this.projectRoleManager
      .isUserInProjectRole(user, projectRole, project)) {
      return projectRole.getId();
    }
    return NumberUtils.LONG_ZERO;
  }







  
  protected void addCommentsFromParentOrOriginalIssue(List<Comment> commentsOfParentIssue, Issue newIssue) {
    for (Comment comment : commentsOfParentIssue) {

      
      try {
        
        if ((comment.getGroupLevel() == null && comment.getRoleLevel() == null) || this.groupManager
          .groupExists((comment.getGroupLevel() != null) ? comment.getGroupLevel() : "") || 
          !this.projectRoleManager.isRoleNameUnique((comment.getRoleLevel() != null) ? comment.getRoleLevel().getName() : "")) {
          if (null == this.commentManager.create(newIssue, comment
              .getAuthorApplicationUser(), comment
              .getUpdateAuthorApplicationUser(), comment
              .getBody(), comment
              .getGroupLevel(), comment
              .getRoleLevelId(), comment
              .getCreated(), comment
              .getUpdated(), true))
          {
            this.log.error("Unable to create comment for {}. Reason is unknown.", newIssue.getKey());
          }
          this.log.debug("{} had comment added: {}", newIssue.getKey(), comment); continue;
        } 
        this.log.error("Unable to create comment for {} with comment id {}. Group Level/Role Level is not valid.", newIssue.getKey(), comment.getId());
      }
      catch (Exception exception) {
        this.log.error("Unable to create comment for {}. Exception: {}", newIssue.getKey(), exception.getMessage());
      } 
    } 
  }










  
  protected boolean verifyPreConditions(Helper helper) { return true; }





  
  protected boolean verifyPreConditionsAfterJql(Helper helper) { return true; }












  
  protected boolean shouldIncludeEntry(Helper helper, String entry) {
    Map<String, String> args = helper.getReplaceHelper().getArgs();
    boolean shouldInclude = true;
    Pattern pattern = null;
    
    String patternString = (String)args.get("field.multipleIssuesPattern");
    
    boolean exact = "1".equals(args.get("field.multipleIssuesExact"));
    boolean literal = "1".equals(args.get("field.multipleIssuesLiteral"));
    boolean reverse = "1".equals(args.get("field.multipleIssuesReverse"));
    if (StringUtils.isNotEmpty(patternString)) {
      try {
        pattern = Pattern.compile(patternString, literal ? 16 : 0);
      } catch (IllegalArgumentException exception) {
        shouldInclude = false;
        this.log.error("Invalid multiple issues regex: {}, error: {}", patternString, exception.toString());
      } 
    }
    
    if (this.log.isDebugEnabled()) {
      this.log.debug("entry: " + entry + ", for pattern: " + patternString);
      this.log.debug("exact: " + exact + ", literal: " + literal + ", reverse: " + reverse);
    } 
    Matcher matcher = (pattern == null) ? null : pattern.matcher(entry);
    if (matcher != null) {
      if ((exact && matcher.matches()) || (!exact && matcher.find())) {
        this.log.debug("matcher find true, groups: " + matcher.groupCount());
        shouldInclude = !reverse;
      } else {
        this.log.debug("Entry: '{}' did not match for multiple issue pattern: {}", entry, pattern);
        shouldInclude = reverse;
      } 
    }
    this.log.debug("Entry: {}, should include: {}", entry, Boolean.valueOf(shouldInclude));
    return shouldInclude;
  }










  
  protected void setCommonFields(Helper helper, MutableIssue newIssue) {
    Map<String, String> args = helper.getReplaceHelper().getArgs();
    this.log.debug("Set common issue fields for new issue. Original issue: {}, parent issue: {}", helper.getReplaceHelper().getOriginalIssue().getKey(), helper
        .getReplaceHelper().getParentIssue().getKey());

    
    newIssue.setEnvironment(helper.getReplaceHelper().findReplace((String)args.get("field.environment")));

    
    setDefaultCustomFieldValues(newIssue);

    
    newIssue.setLabels(LabelParser.buildFromString(helper.getReplaceHelper().findReplace((String)args.get("field.labels"))));

    
    copyCustomFields(helper, newIssue, (String)args.get("field.copyParentFields"), true);
    copyCustomFields(helper, newIssue, (String)args.get("field.copyOriginalFields"), false);

    
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField1Name"), (String)args.get("field.customField1Value1"), (String)args.get("field.customField1Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField2Name"), (String)args.get("field.customField2Value1"), (String)args.get("field.customField2Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField3Name"), (String)args.get("field.customField3Value1"), (String)args.get("field.customField3Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField4Name"), (String)args.get("field.customField4Value1"), (String)args.get("field.customField4Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField5Name"), (String)args.get("field.customField5Value1"), (String)args.get("field.customField5Value2"));
    
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField10Name"), (String)args.get("field.customField10Value1"), (String)args.get("field.customField10Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField11Name"), (String)args.get("field.customField11Value1"), (String)args.get("field.customField11Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField12Name"), (String)args.get("field.customField12Value1"), (String)args.get("field.customField12Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField13Name"), (String)args.get("field.customField13Value1"), (String)args.get("field.customField13Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField14Name"), (String)args.get("field.customField14Value1"), (String)args.get("field.customField14Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField15Name"), (String)args.get("field.customField15Value1"), (String)args.get("field.customField15Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField16Name"), (String)args.get("field.customField16Value1"), (String)args.get("field.customField16Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField17Name"), (String)args.get("field.customField17Value1"), (String)args.get("field.customField17Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField18Name"), (String)args.get("field.customField18Value1"), (String)args.get("field.customField18Value2"));
    setCustomFieldValue(helper, newIssue, (String)args.get("field.customField19Name"), (String)args.get("field.customField19Value1"), (String)args.get("field.customField19Value2"));
  }






  
  protected void setDefaultCustomFieldValues(MutableIssue newIssue) {
    this.log.debug("set default custom field values: {}, id: {}", newIssue.getKey(), newIssue.getProjectObject().getId());
    
    List<CustomField> list = this.customFieldManager.getCustomFieldObjects(newIssue);
    if (list != null) {
      for (CustomField customField : list) {
        try {
          Object defaultValue = customField.getDefaultValue(newIssue);
          newIssue.setCustomFieldValue(customField, defaultValue);
        } catch (Exception exception) {
          
          this.log.debug("Unable to set default custom field value for field: {}, type: {}" + customField.getName(), customField.getCustomFieldType()
              .getKey());
          this.log.debug("Exception: {}", exception.toString());
        } 
      } 
    } else {
      this.log.debug("Unexpected error, custom field list is null. issue type: {}", (newIssue.getIssueType() == null) ? "null" : newIssue
          .getIssueType().getName());
    } 
  }






  
  protected void setWatchers(Issue issue, String watchers, DefaultReplaceHelper replaceHelper) {
    if (hasManageWatchersPermission(issue.getProjectObject(), replaceHelper)) {
      String[] list = watchers.split(",");
      
      for (String entry : list) {
        entry = entry.trim();
        if (!entry.equals("")) {
          ApplicationUser user = Utilities.lookupUser(entry);
          if (user != null) {
            if (this.permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, issue.getProjectObject(), user)) {
              this.watcherManager.startWatching(user, issue);
            } else {
              this.log.warn("Ignore adding watch for user: {}. User does not have browse permission to project: {}.", 
                  Utilities.getTransitionUserName(replaceHelper), issue.getProjectObject().getKey());
            } 
          } else {
            this.log.warn("Ignore invalid user entry: {}", entry);
          } 
        } 
      } 
    } else {
      this.log.warn("Not authorized to add watchers: {}", Utilities.getTransitionUserName(replaceHelper));
    } 
  }







  
  protected ApplicationUser getUser(Helper helper, int choice, String specificUser) {
    String specificUserAfterReplace;
    ApplicationUser user = null;
    
    switch (choice) {
      
      case 0:
        user = helper.getReplaceHelper().getParentIssue().getReporter();
        break;
      
      case 1:
        user = helper.getReplaceHelper().getParentIssue().getAssignee();
        this.log.debug("parent assignee: " + user);
        break;
      
      case 2:
        user = helper.getReplaceHelper().getParentIssue().getProjectObject().getProjectLead();
        break;



      
      case 4:
        specificUserAfterReplace = helper.getReplaceHelper().findReplace(specificUser);
        user = Utilities.lookupUser(specificUserAfterReplace);
        if (user == null) {
          this.log.error("User not found: {}. Field before replacement was: {}. Field will be unassigned.", specificUserAfterReplace, specificUser);
        }
        break;
      
      case 5:
        user = helper.getReplaceHelper().getTransitionUser();
        break;
    } 
    return user;
  }








  
  protected Collection<Version> getVersions(Helper helper, int choice, String specificVersions, Project targetProject) {
    Collection<Version> versions = null;
    switch (choice) {



      
      case 1:
        if (targetProject.equals(helper.getReplaceHelper().getParentIssue().getProjectObject())) {
          versions = helper.getReplaceHelper().getParentIssue().getAffectedVersions();
          break;
        } 
        versions = Utilities.getReplacementVersions(helper.getReplaceHelper().getParentIssue().getAffectedVersions(), targetProject.getVersions(), null);
        break;

      
      case 2:
        if (targetProject.equals(helper.getReplaceHelper().getParentIssue().getProjectObject())) {
          versions = helper.getReplaceHelper().getParentIssue().getFixVersions(); break;
        } 
        versions = Utilities.getReplacementVersions(helper.getReplaceHelper().getParentIssue().getFixVersions(), targetProject.getVersions(), null);
        break;

      
      case 3:
        versions = getVersionList(helper.getReplaceHelper().findReplace(specificVersions), targetProject);
        break;
    } 
    return versions;
  }







  
  protected List<Version> getVersionList(String versions, Project project) {
    List<Version> list = new ArrayList<Version>();
    Collection<Version> projectVersions = project.getVersions();
    Collection<String> versionList = CsvUtilities.csvDataAsList(versions, ',', '\'', '\n');
    for (String versionString : versionList) {
      versionString = versionString.trim();
      Version version = Utilities.findVersion(versionString, projectVersions);
      this.log.debug("String for version lookup: '" + versionString + "' for project: " + project.getName());
      if (version == null) {
        this.log.warn("Could not find version: '" + versionString + "' for project: " + project.getName()); continue;
      } 
      list.add(version);
    } 
    
    return list;
  }








  
  protected Collection<ProjectComponent> getComponents(Helper helper, int choice, String specificComponents, Project targetProject) {
    Collection<ProjectComponent> projectComponents, componentsList, components = null;
    
    this.log.debug("component choice: " + choice);
    
    switch (choice) {



      
      case 1:
        if (targetProject.equals(helper.getReplaceHelper().getParentIssue().getProjectObject())) {
          components = helper.getReplaceHelper().getParentIssue().getComponents(); break;
        } 
        components = Utilities.getReplacementComponents(helper.getReplaceHelper().getParentIssue().getComponents(), targetProject
            .getProjectComponents(), null);
        break;



      
      case 3:
        components = new ArrayList<ProjectComponent>();
        componentsList = CsvUtilities.csvDataAsList(helper.getReplaceHelper().findReplace(specificComponents), ',', '\'', '\n');
        projectComponents = targetProject.getProjectComponents();
        
        for (String string : componentsList) {
          string = string.trim();
          ProjectComponent component = Utilities.findComponent(string, projectComponents);
          if (component == null) {
            this.log.error("Ignore not finding component: '" + string + "' for project: " + targetProject.getName()); continue;
          } 
          components.add(component);
        } 
        break;
    } 
    
    return components;
  }
  
  protected List<ProjectComponent> getComponentList(String specificComponents, Project project) {
    List<ProjectComponent> components = new ArrayList<ProjectComponent>();
    Collection<String> componentsList = CsvUtilities.csvDataAsList(specificComponents, ',', '\'', '\n');
    Collection<ProjectComponent> projectComponents = project.getProjectComponents();
    
    for (String string : componentsList) {
      string = string.trim();
      ProjectComponent component = Utilities.findComponent(string, projectComponents);
      if (component == null) {
        this.log.error("Ignore not finding component: '" + string + "' for project: " + project.getName()); continue;
      } 
      components.add(component);
    } 

    
    return components;
  }









  
  protected Timestamp getDueDate(Helper helper, int choice, String specificValue, int offset, boolean excludeNonWorkingDays) {
    String string;
    this.log.debug("get due date choice: " + choice + ", specific: " + specificValue + ", parent due date: " + helper
        .getReplaceHelper().getParentIssue().getDueDate());
    
    Timestamp value = null;
    
    switch (choice) {



      
      case 1:
        value = helper.getReplaceHelper().getParentIssue().getDueDate();
        if (value != null && offset != 0) {
          if (excludeNonWorkingDays) {
            value = Utilities.handleNonWorkingDays(offset, helper.getReplaceHelper().getParentIssue().getDueDate()); break;
          } 
          value = new Timestamp(helper.getReplaceHelper().getParentIssue().getDueDate().getTime() + offset * 86400000L);
        } 
        break;



      
      case 2:
        string = helper.getReplaceHelper().findReplace(specificValue);
        value = Utilities.getTimestamp(string, offset, getDateFormats(), excludeNonWorkingDays);
        break;
    } 
    return value;
  }








  
  protected Long getTimeDuration(Helper helper, String value) {
    Long duration = null;
    if (value != null && !value.trim().isEmpty()) {
      String durationString = helper.getReplaceHelper().findReplace(value);
      this.log.debug("time duration input string: {}", durationString);
      
      JiraDurationUtils jiraDurationUtils = (JiraDurationUtils)ComponentLocator.getComponent(JiraDurationUtils.class);

      
      try {
        duration = jiraDurationUtils.parseDuration(durationString, Utilities.getDefaultLocale());
      } catch (InvalidDurationException e) {
        this.log.error("Invalid duration specified for original/remaining estimate: " + durationString + ", ignore. Issue is: " + helper
            .getReplaceHelper().getOriginalIssue().getKey() + ".");
      } 
    } 
    this.log.debug("time duration result: {}", duration);
    return duration;
  }









  
  protected void copyCustomFields(Helper helper, MutableIssue issue, String value, boolean useParentAsSource) {
    if (value != null && !value.trim().isEmpty()) {
      Collection<String> fields = CsvUtilities.csvDataAsList(helper.getReplaceHelper().findReplace(value), ',', '\'', '\n');
      for (String name : fields) {
        CustomField customField = getCustomField(name, false);
        CustomField targetField = customField;
        if (customField == null) {
          String[] values = name.split(":");
          if (values.length > 1) {
            customField = getCustomField(values[0], true);
            targetField = getCustomField(values[1], true);
            this.log.debug("map cf from: " + values[0] + ", to: " + values[1] + ", name: " + name);
          } else {
            getCustomField(name, true);
          } 
        } 
        if (customField != null && targetField != null && isValidCustomField(targetField, issue)) {
          copyCustomFieldValueProtected(issue, targetField, (useParentAsSource ? helper.getReplaceHelper().getParentIssue() : helper
              .getReplaceHelper().getOriginalIssue()).getCustomFieldValue(customField));
        }
      } 
    } 
  }








  
  protected void copyCustomFieldValueProtected(MutableIssue issue, CustomField customField, Object value) {
    try {
      CustomFieldType type = customField.getCustomFieldType();
      String key = type.getKey();

      
      if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect") || key
        .equals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes")) {
        if (value instanceof Collection) {
          String optionString = Utilities.collectionToSeparatedString((Collection)value, ",");
          setCustomFieldValueForOptionField(customField, issue, CsvUtilities.csvDataAsList(optionString, ',', '\'', '\n'));
        } else {
          this.log.warn("Could not set the value of custom field {}", customField.getName());
        }
      
      } else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect")) {
        if (value instanceof Map) {
          Map<String, Option> options = (Map)value;
          String childOption = null;
          String parentOption = null;
          if (options.get(PARENT_KEY) != null) {
            parentOption = ((Option)options.get(PARENT_KEY)).toString();
          }
          if (options.get("1") != null) {
            childOption = ((Option)options.get("1")).toString();
          }
          setCustomFieldValueForOptionField(customField, issue, parentOption, childOption);
        } else {
          this.log.warn("Could not set the value of custom field {}", customField.getName());
        }
      
      } else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:select") || key
        .equals("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")) {
        if (value instanceof Option) {
          setCustomFieldValueForOptionField(customField, issue, value.toString());
        } else {
          this.log.warn("Could not set the value of custom field {}", customField.getName());
        } 
      } else {
        issue.setCustomFieldValue(customField, value);
        this.log.debug("custom field: " + customField.getName() + ", id: " + customField.getId() + ", set to: " + value);
      }
    
    } catch (Exception exception) {
      this.log.error("Unable to set custom field: " + customField.getName() + ", id: " + customField.getId() + ", value: " + value + ". Exception was: " + exception
          .toString());
    } 
  }







  
  protected void setCustomFieldValueProtected(MutableIssue issue, CustomField customField, Object value) {
    try {
      issue.setCustomFieldValue(customField, value);
      this.log.debug("custom field: " + customField.getName() + ", id: " + customField.getId() + ", set to: " + value);
    } catch (Exception exception) {
      this.log.error("Unable to set custom field: " + customField.getName() + ", id: " + customField.getId() + ", value: " + value + ". Exception was: " + exception
          .toString());
    } 
  }










  
  protected void setCustomFieldValue(Helper helper, MutableIssue issue, String customFieldName, String inValue1, String inValue2) {
    if (customFieldName != null && !customFieldName.trim().isEmpty()) {
      String value1 = helper.getReplaceHelper().findReplace(inValue1);
      String value2 = helper.getReplaceHelper().findReplace(inValue2);
      
      if (this.log.isDebugEnabled()) {
        this.log.debug("inValue1: " + inValue1 + ", value1: " + value1 + ", original issue: " + helper.getReplaceHelper().getOriginalIssue().getKey());
        if (!"".equals(inValue2)) {
          this.log.debug("inValue2: " + inValue2 + ", value2: " + value2);
        }
      } 
      
      CustomField customField = getCustomField(customFieldName, true);
      if (customField != null) {
        
        CustomFieldType type = customField.getCustomFieldType();
        String key = type.getKey();
        if (this.log.isDebugEnabled()) {
          this.log.debug("set custom field: " + customField.getName() + ", type: " + key + ", description: " + type.getDescription() + ", class: " + type
              .getClass().getName());
        }

        
        if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:userpicker")) {
          setCustomFieldValueProtected(issue, customField, getApplicationUser(value1));
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker")) {
          
          List<Object> userList = new ArrayList<Object>();
          for (String userName : CsvUtilities.csvDataAsList(value1, ',', '\'', '\n')) {
            userList.add(getApplicationUser(userName));
          }
          setCustomFieldValueProtected(issue, customField, userList);
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker") || key
          .equals("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker")) {
          
          GroupManager groupManager = (GroupManager)ComponentLocator.getComponent(GroupManager.class);
          List<Object> list = new ArrayList<Object>();
          for (String groupName : CsvUtilities.csvDataAsList(value1, ',', '\'', '\n')) {
            if (groupManager.groupExists(groupName)) {
              list.add(groupManager.getGroup(groupName)); continue;
            } 
            this.log.error("Invalid group ignored: {}", groupName);
          } 
          
          if (list.size() > 0) {
            setCustomFieldValueProtected(issue, customField, list);
          
          }
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")) {
          boolean excludeNonWorkingDays = excludeNonWorkingDays(value2);

          
          int offset = excludeNonWorkingDays ? Utilities.getInt(value2.replaceAll("[^0-9-]", ""), 0) : Utilities.getInt(value2, 0);
          setCustomFieldValueProtected(issue, customField, Utilities.getTimestamp(value1, offset, getDateFormats(), excludeNonWorkingDays));
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:datetime")) {
          boolean excludeNonWorkingDays = excludeNonWorkingDays(value2);

          
          int offset = excludeNonWorkingDays ? Utilities.getInt(value2.replaceAll("[^0-9-]", ""), 0) : Utilities.getInt(value2, 0);
          setCustomFieldValueProtected(issue, customField, Utilities.getTimestamp(value1, offset, getDateFormats(), excludeNonWorkingDays));
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:version")) {
          
          List<Version> versions = getVersionList(value1, issue.getProjectObject());
          setCustomFieldValueProtected(issue, customField, (versions.size() == 0) ? null : versions);
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiversion")) {
          
          List<Version> versions = getVersionList(value1, issue.getProjectObject());
          setCustomFieldValueProtected(issue, customField, versions);
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect") || key
          .equals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes")) {
          setCustomFieldValueForOptionField(customField, issue, CsvUtilities.csvDataAsList(value1, ',', '\'', '\n'));






        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect") || key
          .equals("de.ics.cascading.show-hide-cascading-select:show-hide-cascading-select-field")) {
          setCustomFieldValueForOptionField(customField, issue, value1, value2);
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:select") || key
          .equals("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")) {
          setCustomFieldValueForOptionField(customField, issue, value1);
        
        }
        else if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:labels")) {
          
          setCustomFieldValueProtected(issue, customField, LabelParser.buildFromString(new LabelHelper(), value1));
        
        }
        else if (key.equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-multi") || key
          .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object") || key
          .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-reference") || key
          .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-reference-multi")) {
          handleThirdPartyCustomFields(value2, value1, inValue1, customField, helper, helper
              .getReplaceHelper().getOriginalIssue(), issue);
        }
        else {
          
          if (key.equals("com.atlassian.jira.plugin.system.customfieldtypes:textarea")) {
            value1 = StringUtils.replace(value1, " \\n ", "\n");
          }
          try {
            if (customField.getCustomFieldType().getSingularObjectFromString(value1) != null) {
              setCustomFieldValueProtected(issue, customField, customField.getCustomFieldType().getSingularObjectFromString(value1));
            } else {
              handleThirdPartyCustomFields(value2, value1, inValue1, customField, helper, helper
                  .getReplaceHelper().getOriginalIssue(), issue);
            } 
          } catch (Exception exception) {
            this.log.error("Custom field type: " + type.getName() + " may not be supported or there was some other error with the field. Error details follow.");
            
            this.log.error("Error treating custom field as a single value field. Exception: " + exception.toString());
          } 
        } 
      } 
    } 
  }











  
  protected void handleThirdPartyCustomFields(String originalCustomFieldName, String value, String inValue1, CustomField customField, Helper helper, Issue originalIssue, MutableIssue newIssue) {
    Object values = new Object();
    if (StringUtils.isNotBlank(originalCustomFieldName)) {
      values = getCustomFieldObjectValueList(originalCustomFieldName, originalIssue, value);
      if (values != null) {
        setCustomFieldValueProtected(newIssue, customField, values);
      } else {
        this.log.info("Custom field {} is not supported yet !", originalCustomFieldName);
      } 
    } else if (inValue1.contains("parent_")) {
      String nameNoPrefix = getCustomFieldNameFromSubstitutionVariable("parent_", inValue1);
      CustomField originalCustomField = getCustomField(nameNoPrefix, true);
      copyCustomFields(helper, newIssue, originalCustomField.getName() + ":" + customField, true);
    }
    else if (inValue1.contains("original_")) {
      String nameNoPrefix = getCustomFieldNameFromSubstitutionVariable("original_", inValue1);
      CustomField originalCustomField = getCustomField(nameNoPrefix, true);
      copyCustomFields(helper, newIssue, originalCustomField.getName() + ":" + customField, false);
    } else {
      this.log.info("No references was made for original Issue customfield. Skipping setting of Custom field");
    } 
  }







  
  protected String getCustomFieldNameFromSubstitutionVariable(String prefix, String substitutionVariable) {
    int index1 = 0;
    int index2 = substitutionVariable.indexOf("%", index1 + 1);
    String name = substitutionVariable.substring(index1 + 1, index2);
    return name.substring(prefix.length());
  }










  
  protected Object getCustomFieldObjectValueList(String originalCustomFieldName, Issue originalIssue, String value) {
    CustomField originalCustomField = getCustomField(originalCustomFieldName, true);
    if (originalCustomField == null) {
      this.log.error("Customfield {} was not found in the original issue {}", originalCustomFieldName, originalIssue.getKey());
    }
    CustomFieldType type = originalCustomField.getCustomFieldType();
    String key = type.getKey();
    if (key.equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-multi") || key
      .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object") || key
      .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-reference") || key
      .equals("com.riadalabs.jira.plugins.insight:rlabs-customfield-object-reference-multi")) {
      Collection<Object> valueObjects = new ArrayList<Object>();
      Collection<Object> values = new ArrayList<Object>();
      valueObjects = (ArrayList)originalCustomField.getValue(originalIssue);
      for (Object object : valueObjects) {
        if (object.toString().equals(value)) {
          values.add(object);
        }
      } 
      return values;
    } 
    return null;
  }






  
  protected boolean excludeNonWorkingDays(String dueDateOffsetString) {
    boolean excludeNonWorkingDays = false;
    if (dueDateOffsetString.toLowerCase().contains("work")) {
      excludeNonWorkingDays = true;
    }
    return excludeNonWorkingDays;
  }






  
  protected ApplicationUser getApplicationUser(String name) {
    ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(name);
    if (user == null) {
      user = ComponentAccessor.getUserManager().getUserByName(name);
    }
    return user;
  }







  
  protected void setCustomFieldValueForOptionField(CustomField customField, MutableIssue issue, Collection<String> values) {
    FieldConfig fieldConfig = customField.getRelevantConfig(issue);
    Options options = this.optionsManager.getOptions(fieldConfig);
    List<Option> optionList = new ArrayList<Option>();
    for (String value : values) {
      value = value.trim();
      if (StringUtils.isNotBlank(value)) {
        Option option = getOption(options, value, null);
        if (option == null) {
          this.log.error("Can't set custom field '{}' to value '{}' because it doesn't correspond to a valid custom field option or id.", customField
              .getName(), value); continue;
        } 
        optionList.add(option);
      } 
    } 
    
    setCustomFieldValueProtected(issue, customField, optionList);
  }







  
  protected void setCustomFieldValueForOptionField(CustomField customField, MutableIssue issue, String value) {
    if (StringUtils.isNotBlank(value)) {
      FieldConfig fieldConfig = customField.getRelevantConfig(issue);
      Options options = this.optionsManager.getOptions(fieldConfig);
      Option option = getOption(options, value, null);
      
      if (option == null) {
        this.log.error("Can't set custom field '{}' to value '{}' because it doesn't correspond to a valid custom field option or id.", customField
            .getName(), value);
      } else {
        setCustomFieldValueProtected(issue, customField, option);
      } 
    } else {
      setCustomFieldValueProtected(issue, customField, null);
    } 
  }








  
  protected void setCustomFieldValueForOptionField(CustomField customField, MutableIssue issue, String value1, String value2) {
    if (StringUtils.isNotBlank(value1)) {
      FieldConfig fieldConfig = customField.getRelevantConfig(issue);
      Options options = this.optionsManager.getOptions(fieldConfig);
      Option option = getOption(options, value1, null);
      this.log.debug("value1: {}, option: {}", value1, option);
      String[] valueList = null;
      String childOptionString = value2;
      if (option == null && 
        !StringUtils.isNotBlank(childOptionString)) {
        valueList = parseOtherFormats(value1);
        if (valueList != null) {
          option = getOption(options, valueList[0].trim(), null);
          childOptionString = valueList[1];
        } 
      } 

      
      if (option == null) {
        this.log.error("Can't set custom field '{}' to value '{}' because it doesn't correspond to a valid custom field option or id.", customField
            .getName(), value1);
        
        return;
      } 
      Map<Object, Option> optionMap = new HashMap<Object, Option>();
      optionMap.put(null, option);
      if (!StringUtils.isBlank(childOptionString)) {
        Option childOption = getOption(options, childOptionString.trim(), option);
        this.log.debug("value2: {}, option: {}", option, childOption);
        if (childOption != null) {
          optionMap.put("1", childOption);
        } else {
          this.log.error("Can't set custom field '{}' to child value '{}' because it doesn't correspond to a valid custom field option or id", customField
              .getName(), childOptionString);
        } 
      } 
      setCustomFieldValueProtected(issue, customField, optionMap);
    }
    else {
      
      setCustomFieldValueProtected(issue, customField, null);
    } 
  }
  
  private String[] parseOtherFormats(String optionValue) {
    String[] valueList = null;
    if (optionValue.contains(" - ")) {
      valueList = optionValue.split(" - ", 2);
    } else if (optionValue.contains(",")) {
      valueList = optionValue.split(",", 2);
    } 
    return (valueList != null && valueList.length > 1) ? valueList : null;
  }








  
  protected Option getOption(Options options, String value, Option parent) {
    this.log.debug("get option for: {}, with parent: {}", value, parent);
    Option option = null;
    if (options != null) {
      option = options.getOptionForValue(value, (parent == null) ? null : parent.getOptionId());
      if (option == null) {
        
        long optionId = -1L;
        try {
          optionId = Long.parseLong(value);
        } catch (NumberFormatException numberFormatException) {}
        
        if (optionId > 0L) {
          option = options.getOptionById(Long.valueOf(optionId));
        }
      } 
    } else {
      this.log.debug("options is null");
    } 
    return option;
  }






  
  protected CustomField getCustomField(String name, boolean logError) {
    CustomField customField = null;
    if (name != null) {
      name = name.trim();
      customField = this.customFieldManager.getCustomFieldObjectByName(name);
      if (customField == null) {
        customField = this.customFieldManager.getCustomFieldObject(name);
      }
      if (customField == null) {
        try {
          Long id = Long.valueOf(Long.parseLong(name));
          customField = this.customFieldManager.getCustomFieldObject(id);
        } catch (NumberFormatException numberFormatException) {}
      }
      
      if (customField == null && logError) {
        this.log.error("Custom field: '" + name + "' not found. Field was ignored.");
      }
    } 
    return customField;
  }








  
  protected boolean isValidCustomField(CustomField customField, Issue issue) {
    List<CustomField> list = this.customFieldManager.getCustomFieldObjects(issue);
    boolean result = list.contains(customField);
    if (!result) {
      this.log.error("Custom field " + customField.getId() + " is not valid for project " + issue.getProjectObject().getKey() + ", issue type " + issue
          .getIssueType().getName() + " . Ignore.");
    }
    return result;
  }










  
  protected List<String> getEntryList(Helper helper) {
    List<String> entryList = null;
    String multipleIssuesValueBeforeReplace = (String)helper.getReplaceHelper().getArgs().get("field.multipleIssuesValue");

    
    if (StringUtils.isNotBlank(multipleIssuesValueBeforeReplace)) {
      entryList = new ArrayList<String>();
      String multipleIssuesValue = helper.getReplaceHelper().findReplace(multipleIssuesValueBeforeReplace);
      
      this.log.debug("getting entry list for multipleIssues: {}", multipleIssuesValue);
      
      if (StringUtils.isNotBlank(multipleIssuesValue)) {
        Collection<String> entries = CsvUtilities.csvDataAsList(multipleIssuesValue, ',', '\'', '\n');
        
        for (String entry : entries) {
          entry = entry.trim();
          if (!entry.equals("") && shouldIncludeEntry(helper, entry)) {
            entryList.add(entry);
          }
        } 
        this.log.debug("Entry list size: {}, for multipleIssues: {}", Integer.valueOf(entryList.size()), multipleIssuesValue);
      } 
    } 
    return entryList;
  }




  
  protected void setCreatedKey(PropertySet propertySet, String createdKey, boolean isSubtask) {
    try {
      Utilities.setPropertySetString(propertySet, "org.swift.jira.cot.last_created_key", createdKey);
      if (!isSubtask) {
        Utilities.setPropertySetString(propertySet, "org.swift.jira.cot.last_created_non_subtask_key", createdKey);
      }
      String createdKeyList = propertySet.getString("org.swift.jira.cot.created_key_list");
      if (createdKeyList != null && createdKeyList.contains("-")) {
        createdKeyList = createdKey + "," + createdKeyList;
      } else {
        createdKeyList = createdKey;
      } 
      Utilities.setPropertySetString(propertySet, "org.swift.jira.cot.created_key_list", createdKeyList);
      Utilities.log(propertySet);
    } catch (Exception exception) {
      this.log.debug("Unexpected exception ignored: {}", exception.toString());
    } 
  }









  
  protected boolean isUserHasProjectPermission(Project project, ApplicationUser applicationUser, int choice) {
    boolean isProjectPermission = false;

    
    try {
      Class class1 = Class.forName("com.atlassian.servicedesk.api.customer.CustomerContextService");
      switch (choice) {
        case 0:
          isProjectPermission = JSDPermissionHelper.verifyCustomerPermission(this.permissionManager, ProjectPermissions.CREATE_ISSUES, project, applicationUser).booleanValue();
          showLogMessage(applicationUser, project, isProjectPermission, "Reporter");
          break;
        
        case 1:
          isProjectPermission = JSDPermissionHelper.verifyCustomerPermission(this.permissionManager, ProjectPermissions.ASSIGNABLE_USER, project, applicationUser).booleanValue();
          showLogMessage(applicationUser, project, isProjectPermission, "Assignee");
          break;
      } 
    } catch (ClassNotFoundException e) {

      
      switch (choice) {
        case 0:
          isProjectPermission = this.permissionManager.hasPermission(ProjectPermissions.CREATE_ISSUES, project, applicationUser);
          showLogMessage(applicationUser, project, isProjectPermission, "Reporter");
          break;
        
        case 1:
          isProjectPermission = this.permissionManager.hasPermission(ProjectPermissions.ASSIGNABLE_USER, project, applicationUser);
          showLogMessage(applicationUser, project, isProjectPermission, "Assignee");
          break;
      } 
    
    } 
    return isProjectPermission;
  }







  
  private void showLogMessage(ApplicationUser applicationUser, Project project, boolean isProjectPermission, String userField) {
    if (isProjectPermission) {
      this.log.debug("{} added as {} for the project {}", new Object[] { applicationUser.getName(), userField, project.getKey() });
    } else {
      this.log.error("Ignore adding {} for user: User does not have {} permission to  the project {}", new Object[] { applicationUser, userField, project
            .getKey() });
    } 
  }
}
