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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * PromQuery utilities.
 *
 * @author Li Wan
 */
public final class PromQueries {
  private PromQueries() {}

  public static final PromQuery TEST_QUERY = PromQuery.builder()
      .instant()
      .metric("1")
      .build();

  /**
   * {@link UriUtils#encodeQueryParams(MultiValueMap)} doesn't encode ':' into {@code %3A} and '+' into {@code %2B"}
   * by design, but they should be in order to send legitimate requests to the Prometheus query API.
   * The {@code org.springframework.web.util.HierarchicalUriComponents$Type} has package access, so we can't extend it
   * to customize.
   * Given these reason we implement our own encoding.
   */
  public static final Escaper PROM_QUERY_ESCAPER = Escapers.builder()
      .addEscape('[', "%5B")
      .addEscape(']', "%5D")
      .addEscape('{', "%7B")
      .addEscape('}', "%7D")
      .addEscape('|', "%7C")
      .addEscape('"', "%22")
      .addEscape('+', "%2B")
      .addEscape(':', "%3A")
      .addEscape('=', "%3D")
      .build();

  /**
   * Encode the query parameters from the given {@code MultiValueMap} with the {@link PromQueries#PROM_QUERY_ESCAPER}.
   * Only used in {@link PromQueryUriBuilderFactory.PromQueryUriBuilder#queryParams(MultiValueMap)} because we can
   * make a {@code URI} for a Prometheus query with only just {@cdoe queryParams(MultiValueMap)}.
   */
  public static MultiValueMap<String, String> encodeQueryParams(MultiValueMap<String, String> queryParams) {
    MultiValueMap<String, String> result = new LinkedMultiValueMap<>(queryParams.size());
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      for (String value : entry.getValue()) {
        result.add(PROM_QUERY_ESCAPER.escape(entry.getKey()), PROM_QUERY_ESCAPER.escape(value));
      }
    }
    return result;
  }

  /**
   * Creates a MultiValueMap object to pass to a {@link UriBuilder#queryParams(MultiValueMap)}, with query parameters
   * converted from the provided {@link PromQuery} object.
   *
   * @param promQuery to convert to the query parameters and create the {@link MultiValueMap} from.
   * @return {@link MultiValueMap} contains query parameters converted from the specified {@link PromQuery}.
   */
  public static MultiValueMap<String, String> toMultiValueMap(PromQuery promQuery) {
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    if (promQuery.is(PromQuery.QueryType.INSTANT)) {
      queryParams.add(promQuery.type.parameter(), promQuery.getQuery());
      promQuery.asInstant().time()
          .ifPresent(time -> queryParams.add("time", time));
    }
    if (promQuery.is(PromQuery.QueryType.RANGE)) {
      queryParams.add(promQuery.type.parameter(), promQuery.getQuery());
      promQuery.asRange().start()
          .ifPresent(start -> queryParams.add("start", start));
      promQuery.asRange().end()
          .ifPresent(end -> queryParams.add("end", end));
      promQuery.asRange().step()
          .ifPresent(step -> queryParams.add("step", step.toString()));
    }
    if (promQuery.is(PromQuery.QueryType.SERIES) || promQuery.is(PromQuery.QueryType.LABELS) ||
        promQuery.is(PromQuery.QueryType.LABELS_VALUES)) {
      queryParams.addAll(promQuery.type.parameter(), promQuery.asMetadata().matches());
    }
    // TODO: Support the "timeout" parameter for both Instant and Range queries

    return queryParams;
  }

  /**
   * The {@link PromQueries#toMultiValueMap} in a more semantically correct name.
   */
  public static MultiValueMap<String, String> toQueryParams(PromQuery promQuery) {
    return toMultiValueMap(promQuery);
  }

  public static URI createUri(UriBuilder uriBuilder, PromQuery promQuery) {
    final String path = promQuery.is(PromQuery.QueryType.LABELS_VALUES) ?
        promQuery.type.endpoint().replace("<label_name>", promQuery.asMetadata().labelName()) :
        promQuery.type.endpoint();
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
        "PromQuery of type: \"" + promQuery.type + "\" is not supported.");

    return uriBuilder
        .path(path)
        .queryParams(toQueryParams(promQuery))
        .build();
  }

  public static Function<UriBuilder, URI> createUriFunc(PromQuery promQuery) {
    return uriBuilder -> createUri(uriBuilder, promQuery);
  }

}
