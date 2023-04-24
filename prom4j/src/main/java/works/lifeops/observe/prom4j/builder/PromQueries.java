package works.lifeops.observe.prom4j.builder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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
   * Creates a MultiValueMap object to pass to a {@link UriBuilder#queryParams(MultiValueMap)}, with query parameters
   * from the provided {@link Map} object.
   *
   * @param map contains the query parameters to create to the {@link MultiValueMap} object.
   * @return {@link MultiValueMap} having the same keys and their values put into a {@link List}.
   */
  public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, V> map) {
    MultiValueMap<K, V> multiValueMap = new LinkedMultiValueMap<K, V>();
    map.keySet().forEach(key -> multiValueMap.put(key, Arrays.asList(map.get(key))));

    return multiValueMap;
  }

  /**
   * Creates a MultiValueMap object to pass to a {@link UriBuilder#queryParams(MultiValueMap)}, with query parameters
   * converted from the provided {@link PromQuery} object.
   *
   * @param promQuery to convert to the query parameters and create the {@link MultiValueMap} from.
   * @return {@link MultiValueMap} contains query parameters converted from the specified {@link PromQuery}.
   */
  public static MultiValueMap<String, String> toMultiValueMap(PromQuery promQuery) {
    return toMultiValueMap(promQuery, false);
  }

  /**
   * Creates a MultiValueMap object to pass to a {@link UriBuilder#queryParams(MultiValueMap)}, with query parameters
   * converted from the provided {@link PromQuery} object.
   *
   * @param promQuery to convert to the query parameters and create the {@link MultiValueMap} from.
   * @param encode whether to encode the query parameter values.
   * @return {@link MultiValueMap} contains query parameters converted from the specified {@link PromQuery}.
   */
  public static MultiValueMap<String, String> toMultiValueMap(PromQuery promQuery, boolean encode) {
    Map<String, String> queryParameters = Maps.newHashMap();
    queryParameters.put("query", promQuery.toString());
    if (promQuery.is(PromQuery.QueryType.INSTANT)) {
      // Because we removed the encoding behavior from the PromQueryUriBuilderFactory.PromQueryUriBuilder.build, we need
      // to encode the query parameter values ourselves.
      promQuery.asInstant().time()
          .map(encodeRfc3339(encode))
          .ifPresent(time -> queryParameters.put("time", time));
    }
    if (promQuery.is(PromQuery.QueryType.RANGE)) {
      promQuery.asRange().start()
          .map(encodeRfc3339(encode))
          .ifPresent(start -> queryParameters.put("start", start));
      promQuery.asRange().end()
          .map(encodeRfc3339(encode))
          .ifPresent(end -> queryParameters.put("end", end));
      // TODO: Support the "step" parameter for Range queries
    }
    // TODO: Support the "timeout" parameter for both Instant and Range queries

    return toMultiValueMap(queryParameters);
  }

  public static Function<UriBuilder, URI> createUri(PromQuery promQuery) {
    return createUri(promQuery, false);
  }

  public static Function<UriBuilder, URI> createUri(PromQuery promQuery, boolean encode) {
    final String path = promQuery.is(PromQuery.QueryType.INSTANT) ?
        "/query" :
        promQuery.is(PromQuery.QueryType.RANGE) ?
            "/query_range" :
            "";
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
        "PromQuery of type: \"" + promQuery.type + "\" is not supported.");

    return uriBuilder -> uriBuilder
        .path(path)
        .queryParams(toMultiValueMap(promQuery, encode))
        .build();
  }

  private static Function<String, String> encodeRfc3339(boolean encode) {
    return rfc3339 -> encode ? UriUtils.encode(rfc3339, Charset.defaultCharset()) : rfc3339;
  }

  /**
   * The {@link UriUtils#encodeQueryParams(MultiValueMap)} doesn't encode ':' and '+' into {@code %3A} and
   * {@code %2B"} respectively by design, but they should be encoded to send to the Prometheus query API, so we have
   * to implement our own encodeQueryParams.
   */
  public static MultiValueMap<String, String> encodeQueryParams(MultiValueMap<String, String> params) {
    MultiValueMap<String, String> result = new LinkedMultiValueMap<>(params.size());
    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
      for (String value : entry.getValue()) {
        
        result.add(entry.getKey(), PROM_QUERY_ESCAPER.escape(value));
      }
    }
    return result;
  }
}
