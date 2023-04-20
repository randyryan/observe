package works.lifeops.observe.prom4j.builder.dto;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import works.lifeops.observe.prom4j.builder.PromQueryDeserializer;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;

public class PromQueryResultMapperTest {

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
  public void testMapper() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response =
        objectMapper.readValue(queryVector, new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {});

    List<PromQueryResultDto.VectorResultDto> vectorResults = PromQueryResultMapper.INSTANCE.vectorResponseToResultDtos(response);
    Assert.assertEquals("Number of mapped results are equal", 1, vectorResults.size());

    PromQueryResultDto.VectorResultDto vectorResult = vectorResults.get(0);
    Assert.assertEquals(response.getData().getResult().get(0).getValue().getTime(), vectorResult.getValue().getTime(), 0);
    Assert.assertEquals("Value of mapped results are equal", response.getData().getResult().get(0).getValue().getValue(), vectorResult.getValue().getValue());
  }

}
