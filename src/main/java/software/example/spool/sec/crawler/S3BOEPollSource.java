package software.example.spool.sec.crawler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.exception.SpoolException;
import software.spool.crawler.api.port.source.PollSource;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class S3BOEPollSource implements PollSource<String> {
    private final String sourceId;
    private final S3Client s3Client;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .registerModule(new JavaTimeModule());

    public S3BOEPollSource(String sourceId, S3Client s3Client) {
        this.sourceId = sourceId;
        this.s3Client = s3Client;
    }

    @Override
    public String fetch() throws SpoolException {
        try {
            ListObjectsV2Response listing = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket("spool-inbox")
                            .prefix("inbox/CAPTURED/")
                            .build()
            );
            return listing.contents().stream()
                    .map(S3Object::key)
                    .map(this::fetchDTO)
                    .filter(Objects::nonNull)
                    .map(EnvelopeDto::payload)
                    .map(e -> PayloadDeserializerFactory.json().as(EnvelopeDto.DTO.class).deserialize(e))
                    .filter(Objects::nonNull)
                    .flatMap(dto -> dto.departamento().stream())
                    .filter(d -> d.epigrafe() != null)
                    .flatMap(d -> d.epigrafe().stream())
                    .flatMap(e -> e.item().stream())
                    .filter(Objects::nonNull)
                    .map(i -> {
                        try {
                            return XmlToJsonUtils.xmlToJson(HttpUtils.get(i.url_xml()));
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(RecordSerializerFactory.record()::serialize)
                    .toList().toString();

//            List<String> list = StandardNormalizer.JSON_ARRAY.pipelineWith(List.of(), "").transform(source.poll()).toList();
//            String xml = HttpUtils.get(list.get(0).replace("\"", ""));
//            System.out.println(XmlToJsonUtils.xmlToJson(xml));
//            System.out.println(PluginRegistry.get(EventBusProvider.class, "KAFKA"));
        } catch (Exception e) {
            throw e;
        }
    }

    private EnvelopeDto fetchDTO(String key) {
        ResponseBytes<GetObjectResponse> raw = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket("spool-inbox")
                        .key(key)
                        .build()
        );
        try {
            return mapper.readValue(raw.asByteArray(), EnvelopeDto.class);
        } catch (IOException e) {
            System.err.println("Failed to deserialize S3 object with key " + key + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    record EnvelopeDto(
            String idempotencyKey,
            String metadata,
            String payload,
            String status,
            int retries,
            Instant capturedAt
    ) {
        record DTO(String codigo, String nombre, List<Departamento> departamento, Map<String, String> metadata, Map<String, Object> sumario_diario) {
            record Departamento(String codigo, String nombre, List<Epigrafe> epigrafe) {
                record Epigrafe(
                        String nombre,
                        @JsonDeserialize(using = ItemListDeserializer.class)
                        List<Item> item
                ) {
                    record Item(String identificador, String control, String titulo, URLPDF url_pdf, String url_xml, String url_html) {
                        record URLPDF(String szBytes, String szKBytes, String pagina_inicial, String pagina_final, String texto) {}
                    }
                }
            }
        }
    }

    public static class ItemListDeserializer extends JsonDeserializer<List<EnvelopeDto.DTO.Departamento.Epigrafe.Item>> {
        @Override
        public List<EnvelopeDto.DTO.Departamento.Epigrafe.Item> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {

            ObjectCodec codec = p.getCodec();
            JsonNode node = codec.readTree(p);

            ObjectMapper mapper = (ObjectMapper) codec;

            if (node.isArray()) {
                return mapper.convertValue(node, new TypeReference<List<EnvelopeDto.DTO.Departamento.Epigrafe.Item>>() {});
            } else {
                EnvelopeDto.DTO.Departamento.Epigrafe.Item single = mapper.convertValue(node, EnvelopeDto.DTO.Departamento.Epigrafe.Item.class);
                return List.of(single);
            }
        }
    }
}
