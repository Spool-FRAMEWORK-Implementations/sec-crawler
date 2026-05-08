package software.example.spool.sec.crawler;

import software.spool.core.exception.SourcePollException;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.source.PollSource;

import java.util.Map;

public class ESIOSPollSource implements PollSource<String> {

    private final Integer indicatorId;
    private final Integer lookbackMinutes;
    private final String timeTrunc;
    private final String sourceId;
    private final Map<String, String> headers;

    public ESIOSPollSource(String sourceId, Integer indicatorId, Integer lookbackMinutes, String timeTrunc, String apiKey) {
        this.sourceId = sourceId;
        this.indicatorId = indicatorId;
        this.lookbackMinutes = lookbackMinutes;
        this.timeTrunc = timeTrunc;
        this.headers = Map.of(
                "x-api-key", apiKey,
                "Accept", "application/json; application/vnd.esios-api-v1+json",
                "Content-Type", "application/json"
        );
    }

    @Override
    public String fetch() throws SpoolException {
        try {
            String url = EsiosUrlBuilder.build(indicatorId, lookbackMinutes, timeTrunc);
            return HttpUtils.get(url, headers);
        } catch (RuntimeException e) {
            throw new SourcePollException(sourceId(), "ESIOS poll failed for indicator [" + indicatorId + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String sourceId() {
        return sourceId;
    }
}