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
public class PromQueryDeserializer extends StdDeserializer<PromQueryResponse<PromQueryResponse.Result>> {
  protected PromQueryDeserializer(Class<?> vc) {
    super(vc);
  }

  public PromQueryDeserializer() {
    this(null);
  }

  @Override
  public PromQueryResponse<PromQueryResponse.Result> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    JsonNode responseNode = parser.readValueAsTree();
    JsonNode statusNode = responseNode.get("status");
    JsonNode dataNode = responseNode.get("data");
    JsonNode resultTypeNode = dataNode.get("resultType");
    JsonNode resultsNode = dataNode.get("result");

    // response (root)
    PromQueryResponse<PromQueryResponse.Result> response = new PromQueryResponse<PromQueryResponse.Result>();

    // response.status
    PromQueryResponse.Status status = PromQueryResponse.Status.fromString(statusNode.textValue());
    response.setStatus(status);

    // response.data.resultType
    PromQueryResponse.ResultType resultType = PromQueryResponse.ResultType.fromString(resultTypeNode.textValue());
    response.getData().setResultType(resultType);

    // response.data.result (list)
    if (resultType.is(PromQueryResponse.ResultType.VECTOR)) {
      for (Iterator<JsonNode> resultNodeIterator = resultsNode.elements(); resultNodeIterator.hasNext(); ) {
        JsonNode resultNode = resultNodeIterator.next();

        JsonNode resultMetricNode = resultNode.get("metric");
        Map<String, String> metric = context.readTreeAsValue(resultMetricNode, Map.class);
        JsonNode resultValueNode = resultNode.get("value");
        PromQueryResponse.ResultValue<PromQueryResponse.VectorResult> value = PromQueryResponse.ResultValue.of(
            resultValueNode.get(0).asDouble(),
            resultValueNode.get(1).asText());

        response.getData().getResult().add(new PromQueryResponse.VectorResult(metric, value));
      }
    }
    if (resultType.is(PromQueryResponse.ResultType.MATRIX)) {
      for (Iterator<JsonNode> resultNodeIterator = resultsNode.elements(); resultNodeIterator.hasNext(); ) {
        JsonNode resultNode = resultNodeIterator.next();

        JsonNode resultMetricNode = resultNode.get("metric");
        Map<String, String> metric = context.readTreeAsValue(resultMetricNode, Map.class);
        JsonNode resultValuesNode = resultNode.get("values");
        List<PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult>> values = Lists.newArrayList();
        for(Iterator<JsonNode> valueNodeIterator = resultValuesNode.elements(); valueNodeIterator.hasNext(); ) {
          JsonNode valueNode = valueNodeIterator.next();
          PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult> value = PromQueryResponse.ResultValue.of(
              valueNode.get(0).asDouble(),
              valueNode.get(1).asText());
          values.add(value);
        }

        response.getData().getResult().add(new PromQueryResponse.MatrixResult(metric, values));
      }
    }

    return response;
  }
}
