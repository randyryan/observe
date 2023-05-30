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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class PromMetadataDeserializer<PM extends PromMetadata<?>> extends StdDeserializer<PromMetadata<?>> {
  private final Class<? extends PromMetadata<?>> vc;

  public PromMetadataDeserializer(Class<PM> vc) {
    super(vc);
    this.vc = vc;
  }

  public PromMetadataDeserializer() {
    this(null);
  }

  @Override
  public PromMetadata<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
    JsonNode responseNode = parser.readValueAsTree();
    JsonNode statusNode = responseNode.get("status");
    JsonNode dataNode = responseNode.get("data");

    if (vc.equals(PromMetadata.Labels.class)) {
      // metadata (root)
      PromMetadata.Labels metadata = new PromMetadata.Labels();

      // response.status
      PromResponse.Status status = PromResponse.Status.fromString(statusNode.textValue());
      metadata.setStatus(status);

      // response.data
      List<String> data = context.readTreeAsValue(dataNode, List.class);
      metadata.setData(data);

      return metadata;
    }
    if (vc.equals(PromMetadata.Series.class)) {
      // metadata (root)
      PromMetadata.Series metadata = new PromMetadata.Series();

      // response.status
      PromResponse.Status status = PromResponse.Status.fromString(statusNode.textValue());
      metadata.setStatus(status);

      // response.data
      List<Map<String, String>> data = Lists.newArrayList();
      for (Iterator<JsonNode> datumNodeIterator = dataNode.iterator(); datumNodeIterator.hasNext(); ) {
        JsonNode datumNode = datumNodeIterator.next();
        Map<String, String> datum = context.readTreeAsValue(datumNode, Map.class);
        data.add(datum);
      }
      metadata.setData(data);

      return metadata;
    }

    throw new JsonParseException("PromMetadataDeserializer cannot parse JSON into \"" + vc.getName() + "\"");
  }
}
