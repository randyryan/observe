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

@Beta
@SuppressWarnings({"serial", "unchecked"})
public class PromQueryDeserializer extends StdDeserializer<PromQueryResponse> {

  protected PromQueryDeserializer(Class<?> vc) {
    super(vc);
  }

  public PromQueryDeserializer() {
    this(null);
  }

  @Override
  public PromQueryResponse deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    JsonNode responseNode = parser.readValueAsTree();
    JsonNode statusNode = responseNode.get("status");
    JsonNode dataNode = responseNode.get("data");
    JsonNode resultTypeNode = dataNode.get("resultType");
    JsonNode resultsNode = dataNode.get("result");

    // response (root)
    PromQueryResponse response = new PromQueryResponse();

    // response.status
    PromQueryResponse.Status status = PromQueryResponse.Status.fromString(statusNode.textValue());
    response.setStatus(status);

    // response.data.resultType
    PromQueryResponse.ResultType resultType = PromQueryResponse.ResultType.fromString(resultTypeNode.textValue());
    response.getData().setResultType(resultType);

    // response.data.result (list)
    for (Iterator<JsonNode> resultNodeIterator = resultsNode.elements(); resultNodeIterator.hasNext(); ) {
      JsonNode resultNode = resultNodeIterator.next();

      JsonNode resultMetricNode = resultNode.get("metric");
      Map<String, String> metric = context.readTreeAsValue(resultMetricNode, Map.class);
      JsonNode resultValueNode = resultNode.get("value");
      List<Object> value = context.readTreeAsValue(resultValueNode, List.class);

      response.getData().getResult().add(new PromQueryResponse.VectorResult(metric, value));
    }

    return response;
  }

}
