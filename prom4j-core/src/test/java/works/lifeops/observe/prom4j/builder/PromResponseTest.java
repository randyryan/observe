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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class PromResponseTest {
  private static final TypeReference<PromResponse<PromResponse.VectorResult>> TYPE_REF =
      new TypeReference<PromResponse<PromResponse.VectorResult>>() {};

  private ObjectMapper objectMapper;
  private InputStream queryVectorMultiple;

  @BeforeEach
  public void setUp() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromResponse.class, new PromResponseDserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .addModule(new JavaTimeModule())
        .build();

    queryVectorMultiple = Thread.currentThread().getContextClassLoader().getResourceAsStream("query_vector_multiple.json");
  }

  @Test
  public void testGetOffsetDateTime() throws StreamReadException, DatabindException, IOException {
    PromResponse<PromResponse.VectorResult> response = objectMapper.readValue(queryVectorMultiple, TYPE_REF);

    Assertions.assertEquals(
        "2023-04-18T13:30:00.331Z",
        response.getData().getResult().get(0).getValue().getOffsetDateTime().toString(),
        "ISO 8601 is correctly converted");
    Assertions.assertEquals(
        "2023-04-18T13:30:00.509Z",
        response.getData().getResult().get(1).getValue().getOffsetDateTime().toString(),
        "ISO 8601 is correctly converted");
  }
}
