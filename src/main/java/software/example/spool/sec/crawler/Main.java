package software.example.spool.sec.crawler;

import software.spool.core.adapter.otel.OTELConfig;
import software.spool.core.model.spool.SpoolNode;
import software.spool.dsl.SpoolNodeDSL;

public class Main {
    public static void main(String[] args) throws Exception {
        OTELConfig.init("crawler");
        SpoolNode node = SpoolNodeDSL.fromDescriptor("/synthea.yaml");
        node.start();
    }
}
