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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.annotations.Beta;

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
      StreamSupport.stream(resultsNode.spliterator(), false)
          .map(suppressIOException(resultNode -> {
            Map<String, String> metric = context.readTreeAsValue(resultNode.get("metric"), Map.class);
            PromResponse.ResultValue<PromResponse.VectrixResult> value = getValueFromNode(resultNode.get("value"));
            return new PromResponse.VectrixResult(metric, value);
          }))
          .forEach(result -> response.getData().addResult(result));
    }
    if (resultType.is(PromResponse.ResultType.MATRIX)) {
      StreamSupport.stream(resultsNode.spliterator(), false)
          .map(suppressIOException(resultNode -> {
            Map<String, String> metric = context.readTreeAsValue(resultNode.get("metric"), Map.class);
            JsonNode valuesNode = resultNode.get("values");
            List<PromResponse.ResultValue<PromResponse.VectrixResult>> values = StreamSupport.stream(valuesNode.spliterator(), false)
                .map(this::<PromResponse.VectrixResult>getValueFromNode)
                .collect(Collectors.toList());
            return new PromResponse.VectrixResult(metric, values);
          }))
          .forEach(result -> response.getData().addResult(result));
    }

    return response;
  }

  /**
   * A modified {@link java.util.function.Consumer} that adds a throws declaration on the {@code accept()} method. Use
   * for receiving a {@link java.util.function.Consumer} written in lambda in the
   * {@link PromResponseDeserializer#consumeIOException(IOExceptionConsumer)}.
   */
  private interface IOExceptionConsumer<T, IOE extends IOException> {
    void accept(T t) throws IOE;
  }

  /**
   * A modified {@link java.util.function.Function} that adds a throws declaration on the {@code apply(T)} method.
   */
  private interface IOExceptionSuppressor<T, R, IOE extends IOException>  {
    R apply(T t) throws IOE;
  }

  /**
   * A method for converting a consumer that throws an exception to one that doesn't for
   * {@link java.util.stream.Stream#forEach(Consumer)} to use.
   */
  private static <T> Consumer<T> consumeIOException(IOExceptionConsumer<T, IOException> ioExceptionConsumer) {
    return t -> {
      try {
        ioExceptionConsumer.accept(t);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    };
  }

  /**
   * A method for converting a function that throws an exception to one that doesn't for
   * {@link java.util.stream.Stream#map(Function)} to use.
   */
  private static <T, R> Function<T, R> suppressIOException(IOExceptionSuppressor<T, R, IOException> ioExceptionSuppressor) {
    return t -> {
      try {
        return ioExceptionSuppressor.apply(t);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    };
  }

  private <R extends PromResponse.Result> PromResponse.ResultValue<R> getValueFromNode(JsonNode valueNode) {
    return PromResponse.ResultValue.of(valueNode.get(0).asDouble(), valueNode.get(1).asText());
  }
}
