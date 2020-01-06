import java.util.List;

public class Driver {

  public static void main(String[] args) throws Exception {
    GoogleCalendarClient googleCalendarClient = new GoogleCalendarClient();
    Parser parser = new Parser("/Users/ivanzhou/Downloads/test_input.html");
    List<CalendarEvent> events = parser.parse("Bay Area Rebels");
    for (CalendarEvent event : events) {
      googleCalendarClient.addEvent(event);
    }
  }
}
