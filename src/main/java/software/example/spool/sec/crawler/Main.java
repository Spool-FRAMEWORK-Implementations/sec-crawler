package software.example.spool.sec.crawler;

import software.spool.core.adapter.otel.OTELConfig;
import software.spool.dsl.SpoolNodeDSL;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        OTELConfig.init("juan");
        SpoolNodeDSL.fromDescriptor("/Crawler.yaml");
    }
}
