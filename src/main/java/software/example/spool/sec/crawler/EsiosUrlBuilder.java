package software.example.spool.sec.crawler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class EsiosUrlBuilder {

    private static final String BASE = "https://api.esios.ree.es/indicators/";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    private EsiosUrlBuilder() {}

    public static String build(int indicatorId, int lookbackMinutes, String timeTrunc) {
        ZonedDateTime now   = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now.minusMinutes(lookbackMinutes);

        String startEnc = URLEncoder.encode(start.format(FMT), StandardCharsets.UTF_8);
        String endEnc   = URLEncoder.encode(now.format(FMT), StandardCharsets.UTF_8);

        return BASE + indicatorId
                + "?start_date=" + startEnc
                + "&end_date="   + endEnc
                + "&time_trunc=" + timeTrunc
                + "&geo_agg=sum";
    }
}