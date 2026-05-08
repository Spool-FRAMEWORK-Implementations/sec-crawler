package software.example.spool.sec.crawler.plugin;

import software.example.spool.sec.crawler.ESIOSPollSource;
import software.spool.crawler.api.port.source.PollSource;
import software.spool.infrastructure.spi.SpoolPlugin;
import software.spool.infrastructure.spi.provider.PluginConfiguration;
import software.spool.infrastructure.spi.provider.PollSourceProvider;

@SpoolPlugin(PollSourceProvider.class)
public class ESIOSPollSourceProvider implements PollSourceProvider {
    @Override
    public String name() {
        return "ESIOS_API";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean supports(PluginConfiguration configuration) {
        return configuration.has("indicatorId") &&
                configuration.has("apiKey") &&
                configuration.has("lookbackMinutes") &&
                configuration.has("timeTrunc");
    }

    @Override
    public PollSource<?> create(PluginConfiguration configuration) {
        return new ESIOSPollSource(configuration.require("sourceId"),
                Integer.parseInt(configuration.require("indicatorId")),
                Integer.parseInt(configuration.require("lookbackMinutes")),
                configuration.require("timeTrunc"),
                configuration.require("apiKey"));
    }
}
