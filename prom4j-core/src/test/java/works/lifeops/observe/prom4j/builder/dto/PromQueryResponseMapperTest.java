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
package works.lifeops.observe.prom4j.builder.dto;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import works.lifeops.observe.prom4j.builder.PromQueryDeserializer;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;

/**
 * The {@link PromQueryResponse} is a Java representation of JSON in the response body from the Prometheus query API,
 * it is a type for the querying parts and is not very suitable for us to write logics with. Therefore, 2 extra types
 * are introduced for various situations: {@link PromQueryResponseDto} represents only the "data.result" node of the
 * response intended for scenarios like transmitting the responses between microservices, whereas the
 * {@link PromQueryResult} is intended for direct use such as analyzing and returning them in a REST resource.<br>
 * Both types can be directly mapped from {@link PromQueryResponse} by their respective mapper.
 *
 * This test tests the mapping from {@link PromQueryResponse} to {@link PromQueryResponseDto}.
 *
 * @author Li Wan
 */
public class PromQueryResponseMapperTest {
  private static final TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>> VECTOR_TYPE_REF =
      new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {};
  private static final TypeReference<PromQueryResponse<PromQueryResponse.MatrixResult>> MATRIX_TYPE_REF =
      new TypeReference<PromQueryResponse<PromQueryResponse.MatrixResult>>() {};

  private ObjectMapper objectMapper;

  private InputStream queryVector;
  private InputStream queryMatrix;

  @BeforeEach
  public void setUpClass() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .build();

    queryVector = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_vector.json");
    queryMatrix = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_matrix.json");
  }

  @Test
  @DisplayName("\"result\" node mapping")
  public void resultMapping() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response = objectMapper.readValue(queryVector, VECTOR_TYPE_REF);

    List<PromQueryResponseDto.VectorResultDto> vectorResults = PromQueryResponseMapper.INSTANCE
        .vectorResponseToDto(response);
    Assertions.assertEquals(1, vectorResults.size(), "Number of mapped results are equal");
  }

  @Test
  @DisplayName("Metric label \"__name__\" special mapping")
  public void metric__name__Mapping() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response = objectMapper.readValue(queryVector, VECTOR_TYPE_REF);

    PromQueryResponseDto.VectorResultDto vectorResult = PromQueryResponseMapper.INSTANCE
        .vectorResponseToDto(response)
        .get(0);

    String __name__ = "go_threads";
    Assertions.assertEquals(__name__, vectorResult.getName(), "metric label \"__name__\" is properly mapped to \"name\".");
  }

  @Test
  @DisplayName("Metric all labels and values mapping")
  public void metricToLabelsMapping() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response = objectMapper.readValue(queryVector, VECTOR_TYPE_REF);

    PromQueryResponseDto.VectorResultDto vectorResult = PromQueryResponseMapper.INSTANCE
        .vectorResponseToDto(response)
        .get(0);

    Map<String, String> metrics = Map.of("__name__", "go_threads", "instance", "localhost:9090", "job", "prometheus");
    Assertions.assertEquals(metrics, vectorResult.getLabels(), "metric labels and values are properly mapped to \"labels\" map.");
  }

  @Test
  @DisplayName("Single value mapping (Vector)")
  public void valueMapping() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response = objectMapper.readValue(queryVector, VECTOR_TYPE_REF);

    PromQueryResponse.ResultValue<PromQueryResponse.VectorResult> vectorResultValue = PromQueryResponseMapper.INSTANCE
        .vectorResponseToDto(response)
        .get(0)
        .getValue();

    Assertions.assertEquals(1681824600, vectorResultValue.getEpochDateTime(), "UNIX epoch seconds and milliseconds are properly mapped.");
    Assertions.assertEquals("10", vectorResultValue.getValue(), "The value is properly mapped");
  }

  @Test
  @DisplayName("Multiple values mapping (Matrix)")
  public void valuesMapping() throws IOException {
    PromQueryResponse<PromQueryResponse.MatrixResult> response = objectMapper.readValue(queryMatrix, MATRIX_TYPE_REF);

    PromQueryResponseDto.MatrixResultDto matrixResult = PromQueryResponseMapper.INSTANCE
        .matrixResponseToDto(response)
        .get(0);

    // TODO: Review this test method and see if there's any necessary to revise.
    for (int i = 0; i < response.getData().getResult().get(0).getValues().size(); i++) {
      PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult> responseValue = response.getData().getResult().get(0).getValues().get(i);
      PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult> dtoValue = matrixResult.getValues().get(i);
      Assertions.assertEquals(responseValue, dtoValue, "ResultValues are equal");
    }
  }

}
