package fr.proline.studio.dam.tasks.data;


public class DRawFile {

  private String identifier;
  private String rawFileDirectory;
  private java.sql.Timestamp creationTimestamp;
  private String serializedProperties;
  private String mzdbFileDirectory;
  private String rawFileName;
  private String mzdbFileName;
  private int projectsCount;
  private String project_ids;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }


  public String getRawFileDirectory() {
    return rawFileDirectory;
  }

  public void setRawFileDirectory(String rawFileDirectory) {
    this.rawFileDirectory = rawFileDirectory;
  }


  public java.sql.Timestamp getCreationTimestamp() {
    return creationTimestamp;
  }

  public void setCreationTimestamp(java.sql.Timestamp creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }


  public String getSerializedProperties() {
    return serializedProperties;
  }

  public void setSerializedProperties(String serializedProperties) {
    this.serializedProperties = serializedProperties;
  }


  public String getMzdbFileDirectory() {
    return mzdbFileDirectory;
  }

  public void setMzdbFileDirectory(String mzdbFileDirectory) {
    this.mzdbFileDirectory = mzdbFileDirectory;
  }


  public String getRawFileName() {
    return rawFileName;
  }

  public void setRawFileName(String rawFileName) {
    this.rawFileName = rawFileName;
  }


  public String getMzdbFileName() {
    return mzdbFileName;
  }

  public void setMzdbFileName(String mzdbFileName) {
    this.mzdbFileName = mzdbFileName;
  }

  public int getProjectsCount() {
    return projectsCount;
  }

  public void setProjectsCount(int projectsCount) {
    this.projectsCount = projectsCount;
  }

  public String getProjectIds() {
    return project_ids;
  }

  public void setProjectIds(String project_ids) {
    this.project_ids = project_ids;
  }

  
}
