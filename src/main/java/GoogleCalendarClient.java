import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleCalendarClient {

  private static final String APPLICATION_NAME = "Quickstart";

  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".store/calendar_sample");
  private static final String CALENDAR_ID = "primary";
  private static final String START_TIME = "startTime";
  private static final int MAX_RESULTS = 200;

  private static FileDataStoreFactory dataStoreFactory;

  private static HttpTransport httpTransport;

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final String CREDENTIALS_FILE_PATH = "/Users/ivanzhou/Downloads/secrets.json";
  private static final String AMERICA_LOS_ANGELES = "America/Los_Angeles";

  private Calendar client;
  private List<CalendarEvent> eventsCache;

  public GoogleCalendarClient() throws Exception {
    httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

    Credential credential = authorize();

    client =
        new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    populateCache();
  }

  private Credential authorize() throws Exception {

    // load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new FileReader(CREDENTIALS_FILE_PATH));

    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar "
              + "into calendar-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR))
            .setDataStoreFactory(dataStoreFactory)
            .build();
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public void addEvent(CalendarEvent input) throws IOException {
    if (isDuplicate(input)) {
      System.out.println("Event already exists, skipping");
      return;
    }
    Event event =
        new Event()
            .setSummary(input.getSummary())
            .setLocation(input.getLocation())
            .setDescription(input.getDescription());

    DateTime startDateTime = input.getStartTime();
    EventDateTime start =
        new EventDateTime().setDateTime(startDateTime).setTimeZone(AMERICA_LOS_ANGELES);
    event.setStart(start);

    DateTime endDateTime = input.getEndTime();
    EventDateTime end =
        new EventDateTime().setDateTime(endDateTime).setTimeZone(AMERICA_LOS_ANGELES);
    event.setEnd(end);

    String calendarId = CALENDAR_ID;
    event = client.events().insert(calendarId, event).execute();
    System.out.printf("Event created: %s\n", event.getHtmlLink());
  }

  private void populateCache() throws IOException {
    Events events =
        client
            .events()
            .list(CALENDAR_ID)
            .setMaxResults(MAX_RESULTS)
            .setTimeMin(new DateTime(System.currentTimeMillis()))
            .setOrderBy(START_TIME)
            .setSingleEvents(true)
            .execute();
    this.eventsCache =
        events.getItems().stream()
            .map(
                e ->
                    new CalendarEvent(
                        e.getSummary(),
                        e.getLocation(),
                        e.getDescription(),
                        e.getStart().getDate(),
                        e.getEnd().getDate()))
            .collect(Collectors.toList());
  }

  public boolean isDuplicate(CalendarEvent event) {
    return eventsCache.stream()
        .anyMatch(
            e ->
                event.getSummary().equals(e.getSummary())
                    && event.getStartTime().equals(e.getStartTime()));
  }
}
