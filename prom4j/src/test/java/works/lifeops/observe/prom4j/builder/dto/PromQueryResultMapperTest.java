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
  public void testMappingVectorResult() throws IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response =
        objectMapper.readValue(queryVector, new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {});

    List<PromQueryResultDto.VectorResultDto> vectorResults = PromQueryResultMapper.INSTANCE.vectorResponseToResultDtos(response);
    Assert.assertEquals("Number of mapped results are equal", 1, vectorResults.size());

    PromQueryResultDto.VectorResultDto vectorResult = vectorResults.get(0);
    Assert.assertEquals(response.getData().getResult().get(0).getValue().getTime(), vectorResult.getValue().getTime(), 0);
    Assert.assertEquals("Value of mapped results are equal", response.getData().getResult().get(0).getValue().getValue(), vectorResult.getValue().getValue());
  }

  @Test
  public void testMappingMatrixResult() throws IOException {
    PromQueryResponse<PromQueryResponse.MatrixResult> response =
        objectMapper.readValue(queryMatrix, new TypeReference<PromQueryResponse<PromQueryResponse.MatrixResult>>() {});

    List<PromQueryResultDto.MatrixResultDto> matrixResults = PromQueryResultMapper.INSTANCE.matrixResponseToResultDtos(response);
    Assert.assertEquals("Number of mapped results are equal", 1, matrixResults.size());

    PromQueryResultDto.MatrixResultDto matrixResult = matrixResults.get(0);
    for (int i = 0; i < response.getData().getResult().get(0).getValues().size(); i++) {
      PromQueryResponse.ResultValue responseValue = response.getData().getResult().get(0).getValues().get(i);
      PromQueryResponse.ResultValue dtoValue = matrixResult.getValues().get(i);
      Assert.assertEquals("ResultValues are equal", responseValue, dtoValue);
    }
  }

}
