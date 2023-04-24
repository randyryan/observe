package works.lifeops.observe.prom4j.builder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.google.common.annotations.Beta;

/**
 * The UriBuilderFactory implementation provides a specialized DefaultUriBuilder that allow the usage of "{" and "}" in
 * the query, these symbols present in the PromQL.
 */
@Beta
public class PromQueryUriBuilderFactory extends DefaultUriBuilderFactory implements UriBuilderFactory {
  /**
   * DefaultUriBuilderFactory.baseUri should be protected.
   */
  @Nullable
  protected final UriComponentsBuilder baseUri;

  /**
   * Image the same constructor in the super.
   */
  public PromQueryUriBuilderFactory() {
    this.baseUri = null;
  }

  /**
   * Image the same constructor in the super.
   */
  public PromQueryUriBuilderFactory(String baseUriTemplate) {
    this.baseUri = UriComponentsBuilder.fromUriString(baseUriTemplate);
  }

  /**
   * Image the same constructor in the super.
   */
  public PromQueryUriBuilderFactory(UriComponentsBuilder baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public UriBuilder uriString(String uriTemplate) {
    return new PromQueryUriBuilder(uriTemplate);
  }

  @Override
  public UriBuilder builder() {
    return new PromQueryUriBuilder("");
  }

  protected class PromQueryUriBuilder implements UriBuilder {
    private final UriComponentsBuilder uriComponentsBuilder;

    public PromQueryUriBuilder(String uriTemplate) {
      this.uriComponentsBuilder = initUriComponentsBuilder(uriTemplate);
    }

    private UriComponentsBuilder initUriComponentsBuilder(String uriTemplate) {
      UriComponentsBuilder result;
      if (!StringUtils.hasLength(uriTemplate)) {
        result = (baseUri != null ? baseUri.cloneBuilder() : UriComponentsBuilder.newInstance());
      }
      else if (baseUri != null) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uriTemplate);
        UriComponents uri = builder.build();
        result = (uri.getHost() == null ? baseUri.cloneBuilder().uriComponents(uri) : builder);
      }
      else {
        result = UriComponentsBuilder.fromUriString(uriTemplate);
      }
      if (getEncodingMode().equals(EncodingMode.TEMPLATE_AND_VALUES)) {
        result.encode();
      }
      parsePathIfNecessary(result);
      return result;
    }

    private void parsePathIfNecessary(UriComponentsBuilder result) {
      if (shouldParsePath() && getEncodingMode().equals(EncodingMode.URI_COMPONENT)) {
        UriComponents uric = result.build();
        String path = uric.getPath();
        result.replacePath(null);
        for (String segment : uric.getPathSegments()) {
          result.pathSegment(segment);
        }
        if (path != null && path.endsWith("/")) {
          result.path("/");
        }
      }
    }

    @Override
    public UriBuilder scheme(@Nullable String scheme) {
      this.uriComponentsBuilder.scheme(scheme);
      return this;
    }

    @Override
    public UriBuilder userInfo(@Nullable String userInfo) {
      this.uriComponentsBuilder.userInfo(userInfo);
      return this;
    }

    @Override
    public UriBuilder host(@Nullable String host) {
      this.uriComponentsBuilder.host(host);
      return this;
    }

    @Override
    public UriBuilder port(int port) {
      this.uriComponentsBuilder.port(port);
      return this;
    }

    @Override
    public UriBuilder port(@Nullable String port) {
      this.uriComponentsBuilder.port(port);
      return this;
    }

    @Override
    public UriBuilder path(String path) {
      this.uriComponentsBuilder.path(path);
      return this;
    }

    @Override
    public UriBuilder replacePath(@Nullable String path) {
      this.uriComponentsBuilder.replacePath(path);
      return this;
    }

    @Override
    public UriBuilder pathSegment(String... pathSegments) throws IllegalArgumentException {
      this.uriComponentsBuilder.pathSegment(pathSegments);
      return this;
    }

    @Override
    public UriBuilder query(String query) {
      // XXX: Modified behavior, added encoding
      this.uriComponentsBuilder.query(UriUtils.encodeQuery(query, Charset.defaultCharset()));
      return this;
    }

    @Override
    public UriBuilder replaceQuery(@Nullable String query) {
      this.uriComponentsBuilder.replaceQuery(query);
      return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) {
      // XXX: Modified behavior, added encoding
      Object[] encodedValues = Arrays.stream(values)
          .map(value -> UriUtils.encodeQueryParam(value.toString(), Charset.defaultCharset()))
          .toArray();
      this.uriComponentsBuilder.queryParam(name, encodedValues);
      return this;
    }

    @Override
    public UriBuilder queryParam(String name, @Nullable Collection<?> values) {
      this.uriComponentsBuilder.queryParam(name, values);
      return this;
    }

    @Override
    public UriBuilder queryParamIfPresent(String name, Optional<?> value) {
      this.uriComponentsBuilder.queryParamIfPresent(name, value);
      return this;
    }

    @Override
    public UriBuilder queryParams(MultiValueMap<String, String> params) {
      // XXX: Modified behavior, added encoding
      this.uriComponentsBuilder.queryParams(PromQueries.encodeQueryParams(params));
      return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
      this.uriComponentsBuilder.replaceQueryParam(name, values);
      return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, @Nullable Collection<?> values) {
      this.uriComponentsBuilder.replaceQueryParam(name, values);
      return this;
    }

    @Override
    public UriBuilder replaceQueryParams(MultiValueMap<String, String> params) {
      this.uriComponentsBuilder.replaceQueryParams(params);
      return this;
    }

    @Override
    public UriBuilder fragment(@Nullable String fragment) {
      this.uriComponentsBuilder.fragment(fragment);
      return this;
    }

    @Override
    public URI build(Object... uriVariables) {
      // XXX:
      // The URI variables are only what Spring "replaces" the variable declarations in the form of {var} in the
      // "URI template" syntax. Since we want to get around such mechanism and actually use the parentheses, we've
      // encoded them as soon as we set the query so that they don't get recognized by Spring as the variables,
      // then to avoid twice encoding, we need to set encoded argument to true passing to the build();
      UriComponents uric = this.uriComponentsBuilder.build(true);
      return createUri(uric);
    }

    @Override
    public URI build(Map<String, ?> uriVariables) {
      UriComponents uric = this.uriComponentsBuilder.build(true);
      return createUri(uric);
    }

    private URI createUri(UriComponents uric) {
      if (getEncodingMode().equals(EncodingMode.URI_COMPONENT)) {
        uric = uric.encode();
      }
      return URI.create(uric.toString());
    }
  }
}
