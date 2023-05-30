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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

@Beta
@SuppressWarnings({"serial", "unchecked"})
public class PromResponseDeserializer extends StdDeserializer<PromResponse<PromResponse.Result>> {
  protected PromResponseDeserializer(Class<?> vc) {
    super(vc);
  }

  public PromResponseDeserializer() {
    this(null);
  }

  @Override
  public PromResponse<PromResponse.Result> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    JsonNode responseNode = parser.readValueAsTree();
    JsonNode statusNode = responseNode.get("status");
    JsonNode dataNode = responseNode.get("data");
    JsonNode resultTypeNode = dataNode.get("resultType");
    JsonNode resultsNode = dataNode.get("result");

    // response (root)
    PromResponse<PromResponse.Result> response = new PromResponse<PromResponse.Result>();

    // response.status
    PromResponse.Status status = PromResponse.Status.fromString(statusNode.textValue());
    response.setStatus(status);

    // response.data.resultType
    PromResponse.ResultType resultType = PromResponse.ResultType.fromString(resultTypeNode.textValue());
    response.getData().setResultType(resultType);

    // response.data.result (list)
    if (resultType.is(PromResponse.ResultType.VECTOR)) {
      for (Iterator<JsonNode> resultNodeIterator = resultsNode.elements(); resultNodeIterator.hasNext(); ) {
        JsonNode resultNode = resultNodeIterator.next();

        JsonNode resultMetricNode = resultNode.get("metric");
        Map<String, String> metric = context.readTreeAsValue(resultMetricNode, Map.class);
        JsonNode resultValueNode = resultNode.get("value");
        PromResponse.ResultValue<PromResponse.VectorResult> value = PromResponse.ResultValue.of(
            resultValueNode.get(0).asDouble(),
            resultValueNode.get(1).asText());
        response.getData().getResult().add(new PromResponse.VectorResult(metric, value));
      }
    }
    if (resultType.is(PromResponse.ResultType.MATRIX)) {
      for (Iterator<JsonNode> resultNodeIterator = resultsNode.elements(); resultNodeIterator.hasNext(); ) {
        JsonNode resultNode = resultNodeIterator.next();

        JsonNode resultMetricNode = resultNode.get("metric");
        Map<String, String> metric = context.readTreeAsValue(resultMetricNode, Map.class);
        JsonNode resultValuesNode = resultNode.get("values");
        List<PromResponse.ResultValue<PromResponse.MatrixResult>> values = Lists.newArrayList();
        for (Iterator<JsonNode> valueNodeIterator = resultValuesNode.elements(); valueNodeIterator.hasNext(); ) {
          JsonNode valueNode = valueNodeIterator.next();
          PromResponse.ResultValue<PromResponse.MatrixResult> value = PromResponse.ResultValue.of(
              valueNode.get(0).asDouble(),
              valueNode.get(1).asText());
          values.add(value);
        }

        response.getData().getResult().add(new PromResponse.MatrixResult(metric, values));
      }
    }

    return response;
  }
}
