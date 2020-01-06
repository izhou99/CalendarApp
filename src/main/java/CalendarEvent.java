import com.google.api.client.util.DateTime;

public class CalendarEvent {

  private String summary;
  private String location;
  private String description;
  private DateTime startTime;
  private DateTime endTime;


  public CalendarEvent(String summary, String location, String description,
      DateTime startTime, DateTime endTime) {
    this.summary = summary;
    this.location = location;
    this.description = description;
    this.startTime = startTime;
    this.endTime = endTime;
  }


  public String getSummary() {
    return summary;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  @Override
  public String toString() {
    return "CalendarEvent{" +
        "summary='" + summary + '\'' +
        ", location='" + location + '\'' +
        ", description='" + description + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        '}';
  }
}
