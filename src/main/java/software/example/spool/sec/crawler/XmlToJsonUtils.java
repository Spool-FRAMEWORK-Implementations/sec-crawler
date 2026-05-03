package software.example.spool.sec.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlToJsonUtils {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static String xmlToJson(String xml) throws Exception {
        JsonNode node = XML_MAPPER.readTree(xml.getBytes());
        return node.toString();  // JSON como String
    }

    public static JsonNode xmlToJsonNode(String xml) throws Exception {
        return XML_MAPPER.readTree(xml.getBytes());
    }
}