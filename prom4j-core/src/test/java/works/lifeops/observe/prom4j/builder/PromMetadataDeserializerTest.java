/*
 * Copyright (c) 2023 Li Wan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package works.lifeops.observe.prom4j.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PromMetadataDeserializerTest {
  private static final TypeReference<PromMetadata.Labels> LABELS_TYPE_REF = new TypeReference<PromMetadata.Labels>() {};
  private static final TypeReference<PromMetadata.Series> SERIES_TYPE_REF = new TypeReference<PromMetadata.Series>() {};

  private ObjectMapper objectMapper;

  private InputStream metadataLabels;
  private InputStream metadataSeries;

  @BeforeEach
  public void setUp() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromMetadata.Labels.class, new PromMetadataDeserializer(PromMetadata.Labels.class));
    module.addDeserializer(PromMetadata.Series.class, new PromMetadataDeserializer(PromMetadata.Series.class));

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .build();

    metadataLabels = Thread.currentThread().getContextClassLoader().getResourceAsStream("metadata_labels.json");
    metadataSeries = Thread.currentThread().getContextClassLoader().getResourceAsStream("metadata_series.json");
  }

  @Test
  public void testLabelsMetadata() throws IOException {
    PromMetadata.Labels labelsMetadata = objectMapper.readValue(metadataLabels, LABELS_TYPE_REF);

    Assertions.assertEquals(PromResponse.Status.SUCCESS, labelsMetadata.getStatus());
    Assertions.assertEquals(List.of("__name__", "group", "instance", "job"), labelsMetadata.getData());
  }

  @Test
  public void testSeriesMetadata() throws IOException {
    PromMetadata.Series seriesMetadata = objectMapper.readValue(metadataSeries, SERIES_TYPE_REF);

    Assertions.assertEquals(PromResponse.Status.SUCCESS, seriesMetadata.getStatus());
    Assertions.assertEquals(2, seriesMetadata.getData().size());

    List<Map<String, String>> data = List.of(
        Map.of("__name__", "go_threads", "group", "production", "instance", "localhost:8080", "job", "node"),
        Map.of("__name__", "go_threads", "instance", "localhost:9090", "job", "prometheus")
    );
    Assertions.assertEquals(data, seriesMetadata.getData());
  }
}
