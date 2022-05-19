package com.daimler.data.dto.usernotificationpref;

import java.util.Objects;
import com.daimler.data.dto.usernotificationpref.NotificationPreferenceVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * UserNotificationPrefVO
 */
@Validated


public class UserNotificationPrefVO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("userId")
  private String userId = null;

  @JsonProperty("solutionNotificationPref")
  private NotificationPreferenceVO solutionNotificationPref = null;

  @JsonProperty("notebookNotificationPref")
  private NotificationPreferenceVO notebookNotificationPref = null;

  @JsonProperty("persistenceNotificationPref")
  private NotificationPreferenceVO persistenceNotificationPref = null;

  @JsonProperty("dashboardNotificationPref")
  private NotificationPreferenceVO dashboardNotificationPref = null;

  public UserNotificationPrefVO id(String id) {
    this.id = id;
    return this;
  }

  /**
   * ID of notification
   * @return id
  **/
  @ApiModelProperty(value = "ID of notification")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserNotificationPrefVO userId(String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * short Identifier of user
   * @return userId
  **/
  @ApiModelProperty(required = true, value = "short Identifier of user")
  @NotNull


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public UserNotificationPrefVO solutionNotificationPref(NotificationPreferenceVO solutionNotificationPref) {
    this.solutionNotificationPref = solutionNotificationPref;
    return this;
  }

  /**
   * Get solutionNotificationPref
   * @return solutionNotificationPref
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public NotificationPreferenceVO getSolutionNotificationPref() {
    return solutionNotificationPref;
  }

  public void setSolutionNotificationPref(NotificationPreferenceVO solutionNotificationPref) {
    this.solutionNotificationPref = solutionNotificationPref;
  }

  public UserNotificationPrefVO notebookNotificationPref(NotificationPreferenceVO notebookNotificationPref) {
    this.notebookNotificationPref = notebookNotificationPref;
    return this;
  }

  /**
   * Get notebookNotificationPref
   * @return notebookNotificationPref
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public NotificationPreferenceVO getNotebookNotificationPref() {
    return notebookNotificationPref;
  }

  public void setNotebookNotificationPref(NotificationPreferenceVO notebookNotificationPref) {
    this.notebookNotificationPref = notebookNotificationPref;
  }

  public UserNotificationPrefVO persistenceNotificationPref(NotificationPreferenceVO persistenceNotificationPref) {
    this.persistenceNotificationPref = persistenceNotificationPref;
    return this;
  }

  /**
   * Get persistenceNotificationPref
   * @return persistenceNotificationPref
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public NotificationPreferenceVO getPersistenceNotificationPref() {
    return persistenceNotificationPref;
  }

  public void setPersistenceNotificationPref(NotificationPreferenceVO persistenceNotificationPref) {
    this.persistenceNotificationPref = persistenceNotificationPref;
  }

  public UserNotificationPrefVO dashboardNotificationPref(NotificationPreferenceVO dashboardNotificationPref) {
    this.dashboardNotificationPref = dashboardNotificationPref;
    return this;
  }

  /**
   * Get dashboardNotificationPref
   * @return dashboardNotificationPref
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public NotificationPreferenceVO getDashboardNotificationPref() {
    return dashboardNotificationPref;
  }

  public void setDashboardNotificationPref(NotificationPreferenceVO dashboardNotificationPref) {
    this.dashboardNotificationPref = dashboardNotificationPref;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserNotificationPrefVO userNotificationPrefVO = (UserNotificationPrefVO) o;
    return Objects.equals(this.id, userNotificationPrefVO.id) &&
        Objects.equals(this.userId, userNotificationPrefVO.userId) &&
        Objects.equals(this.solutionNotificationPref, userNotificationPrefVO.solutionNotificationPref) &&
        Objects.equals(this.notebookNotificationPref, userNotificationPrefVO.notebookNotificationPref) &&
        Objects.equals(this.persistenceNotificationPref, userNotificationPrefVO.persistenceNotificationPref) &&
        Objects.equals(this.dashboardNotificationPref, userNotificationPrefVO.dashboardNotificationPref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, solutionNotificationPref, notebookNotificationPref, persistenceNotificationPref, dashboardNotificationPref);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserNotificationPrefVO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    solutionNotificationPref: ").append(toIndentedString(solutionNotificationPref)).append("\n");
    sb.append("    notebookNotificationPref: ").append(toIndentedString(notebookNotificationPref)).append("\n");
    sb.append("    persistenceNotificationPref: ").append(toIndentedString(persistenceNotificationPref)).append("\n");
    sb.append("    dashboardNotificationPref: ").append(toIndentedString(dashboardNotificationPref)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

