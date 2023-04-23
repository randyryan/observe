package works.lifeops.observe.prom4j.builder;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class PromQueryResponseTest {
  private static final TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>> TYPE_REF =
      new TypeReference<PromQueryResponse<PromQueryResponse.VectorResult>>() {};

  private ObjectMapper objectMapper;
  private InputStream queryVectorMultiple;

  @BeforeEach
  public void setUp() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .addModule(new JavaTimeModule())
        .build();

    queryVectorMultiple = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_vector_multiple.json");
  }

  @Test
  public void testGetOffsetDateTime() throws StreamReadException, DatabindException, IOException {
    PromQueryResponse<PromQueryResponse.VectorResult> response = objectMapper.readValue(queryVectorMultiple, TYPE_REF);

    Assert.assertEquals("ISO 8601 is correctly converted",
        "2023-04-18T13:30:00.331Z",
        response.getData().getResult().get(0).getValue().getOffsetDateTime().toString());
    Assert.assertEquals("ISO 8601 is correctly converted",
        "2023-04-18T13:30:00.509Z",
        response.getData().getResult().get(1).getValue().getOffsetDateTime().toString());
  }
}
