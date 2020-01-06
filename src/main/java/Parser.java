import com.google.api.client.util.DateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Parser {

  private static final String ACTIVITY_DATE = "activity_date";
  private static final String TIME = "time";
  private static final String TARGET = "target";
  private static final String GAME = "%s Game";
  private static final String START_BRACKET = ">";
  private static final String END_BRACKET = "<";
  private static final String PM = "PM";
  public static final String DATE_PATTERN = "MMM dd yyyy h:mma";
  private String contents;

  public Parser(String fileName) throws IOException {
    contents = new String(Files.readAllBytes(Paths.get(fileName)));
  }

  public List<CalendarEvent> parse(String teamName) {
    NavigableSet<Integer> dates = getIndices(ACTIVITY_DATE);
    NavigableSet<Integer> times = getIndices(TIME);
    NavigableSet<Integer> locations = getIndices(TARGET);
    List<Integer> teamMatches = new ArrayList<>(getIndices(teamName));
    return teamMatches.stream()
        .map(
            index -> {
              Integer dateIndex = dates.floor(index);
              Integer timeIndex = times.floor(index);
              Integer locationsIndex = locations.floor(index);
              String date = parseField(dateIndex);
              String time = parseField(timeIndex);
              String location = parseField(locationsIndex);
              String start = date + " " + time;
              Date startDate = parseStringToDate(start);
              long endTs = startDate.getTime() + Duration.ofHours(1).toMillis();
              Date endDate = new Date(endTs);
              return new CalendarEvent(
                  String.format(GAME, teamName),
                  location,
                  "",
                  new DateTime(startDate),
                  new DateTime(endDate));
            })
        .collect(Collectors.toList());
  }

  private NavigableSet<Integer> getIndices(String subString) {
    int index = contents.indexOf(subString);
    NavigableSet<Integer> set = new TreeSet<>();
    while (index >= 0) {
      set.add(index);
      index = contents.indexOf(subString, index + 1);
    }
    return set;
  }

  private String parseField(int index) {
    int start = contents.indexOf(START_BRACKET, index);
    int end = contents.indexOf(END_BRACKET, index);
    return contents.substring(start + 1, end);
  }

  private Date parseStringToDate(String raw) {
    raw = raw.substring(raw.indexOf(",") + 1);
    raw = raw.trim();
    raw += PM;

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    try {
      return sdf.parse(raw);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }
}
