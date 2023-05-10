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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PromQueryDeserializerTest {
  private ObjectMapper objectMapper;

  private InputStream queryVector;
  private InputStream queryMatrix;

  @BeforeEach
  public void setUp() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .build();

    queryVector = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_vector.json");
    queryMatrix = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_matrix.json");
  }

  @Test
  public void testVectorResult() throws JsonMappingException, JsonProcessingException, IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response =
        objectMapper.readValue(queryVector, new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {});

    Map<String, String> metric = Maps.newLinkedHashMap();
    metric.put("__name__", "go_threads");
    metric.put("instance", "localhost:9090");
    metric.put("job", "prometheus");
    PromQueryResponse.ResultValue<PromQueryResponse.VectorResult> value =
        PromQueryResponse.ResultValue.of(1681824600, "10");
    PromQueryResponse.VectorResult resultItem = new PromQueryResponse.VectorResult(metric, value);

    Assertions.assertEquals(PromQueryResponse.Status.SUCCESS, response.getStatus(),
        "response.status is properly deserialized");
    Assertions.assertEquals(PromQueryResponse.ResultType.VECTOR, response.getData().getResultType(),
        "response.data.resultType is properly deserialized");
    // rough comparison
    Assertions.assertEquals(1, response.getData().getResult().size(),
        "response.data.result list is properly deserialized");
    // rough comparison
    Assertions.assertEquals(resultItem.getMetric().size(), response.getData().getResult().get(0).getMetric().size(),
        "response.data.result item is properly deserialized");
    // rough comparison
    Assertions.assertEquals(resultItem.getValue().toString(),
        response.getData().getResult().get(0).getValue().toString(),
        "response.data.result item is properly deserialized");
  }

  @Test
  public void testMatrixResult() throws JsonMappingException, JsonProcessingException, IOException {
    Map<String, String> metric = Maps.newLinkedHashMap();
    metric.put("__name__", "go_threads");
    metric.put("instance", "localhost:9090");
    metric.put("job", "prometheus");
    List<PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult>> values = Lists.newArrayList();
    values.add(PromQueryResponse.ResultValue.of(1681824540, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824550, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824560, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824570, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824580, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824590, "10"));
    values.add(PromQueryResponse.ResultValue.of(1681824600, "10"));
    PromQueryResponse.MatrixResult resultItem = new PromQueryResponse.MatrixResult(metric, values);

    PromQueryResponse<PromQueryResponse.MatrixResult> response =
        objectMapper.readValue(queryMatrix, new TypeReference<PromQueryResponse<PromQueryResponse.MatrixResult>>() {});

    Assertions.assertEquals(PromQueryResponse.Status.SUCCESS, response.getStatus(),
        "response.status is properly deserialized");
    Assertions.assertEquals(PromQueryResponse.ResultType.MATRIX, response.getData().getResultType(),
        "response.data.resultType is properly deserialized");
    // rough comparison
    Assertions.assertEquals(1, response.getData().getResult().size(),
        "response.data.result list is properly deserialized");
    // rough comparison
    Assertions.assertEquals(resultItem.getMetric().size(), response.getData().getResult().get(0).getMetric().size(),
        "response.data.result item is properly deserialized");
    // rough comparison
    Assertions.assertEquals(resultItem.getValues().size(), response.getData().getResult().get(0).getValues().size(),
        "response.data.result item is properly deserialized");
  }
}
