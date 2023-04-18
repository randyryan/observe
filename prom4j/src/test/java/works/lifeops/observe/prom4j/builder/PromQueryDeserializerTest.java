package works.lifeops.observe.prom4j.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PromQueryDeserializerTest {

  private ObjectMapper objectMapper;
  
  private InputStream queryVector;

  @BeforeEach
  public void setUpClass() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .build();
    
    queryVector = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_vector.json");
  }

  @Test
  public void testVectorResult() throws JsonMappingException, JsonProcessingException, IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response =
        objectMapper.readValue(queryVector, new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {});

    Map<String, String> metric = Maps.newLinkedHashMap();
    metric.put("__name__", "go_threads");
    metric.put("instance", "localhost:9090");
    metric.put("job", "prometheus");
    List<Object> value = Lists.newArrayList();
    value.add(1681824600);
    value.add("10");
    PromQueryResponse.VectorResult resultItem = new PromQueryResponse.VectorResult(metric, value);

    Assert.assertEquals("response.status is properly deserialized",
        PromQueryResponse.Status.SUCCESS,
        response.getStatus());
    Assert.assertEquals("response.data.resultType is properly deserialized",
        PromQueryResponse.ResultType.VECTOR,
        response.getData().getResultType());
    // rough comparison
    Assert.assertEquals("response.data.result list is properly deserialized", 1, response.getData().getResult().size());
    // rough comparison
    Assert.assertEquals("response.data.result item is properly deserialized", resultItem.getMetric().size(), response.getData().getResult().get(0).getMetric().size());
    // rough comparison
    Assert.assertEquals("response.data.result item is properly deserialized", resultItem.getValue().size(), response.getData().getResult().get(0).getValue().size());
  }

  @Test
  public void testMatrixResult() throws JsonMappingException, JsonProcessingException {
//      PromQueryResponse<PromQueryResponse.MatrixResult> response =
//              objectMapper.readValue(matrixJson, new TypeReference<PromQueryResponse<PromQueryResponse.MatrixResult>>() {});
  }

}
