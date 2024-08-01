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

import java.util.List;
import java.util.Optional;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;

/**
 * Holds a piece of simple PromQL.
 *
 * @author Li Wan
 */
@Beta
@SuppressWarnings("unchecked")
public abstract class PromQuery {
  /**
   * Creator methods of the concrete builders. Although we could just be like {@code PromQuery.instantBuilder()} but
   * we are doing it this way for the sake of the {@code builder()} method norm.
   */
  public static final class QueryBuilders {
    private QueryBuilders() {}

    public PromQueryBuilder.InstantQueryBuilder instant() {
      return new PromQueryBuilder.InstantQueryBuilder();
    }

    public PromQueryBuilder.RangeQueryBuilder range() {
      return new PromQueryBuilder.RangeQueryBuilder();
    }

    public PromQueryBuilder.MetadataQueryBuilder series() {
      return new PromQueryBuilder.MetadataQueryBuilder(QueryType.SERIES);
    }

    public PromQueryBuilder.MetadataQueryBuilder labels() {
      return new PromQueryBuilder.MetadataQueryBuilder(QueryType.LABELS);
    }

    public PromQueryBuilder.MetadataQueryBuilder labelValues() {
      return new PromQueryBuilder.MetadataQueryBuilder(QueryType.LABELS_VALUES);
    }

    public PromQueryBuilder.LabelBuilder label(String label) {
      return new PromQueryBuilder.LabelBuilder(label);
    }

    public PromQueryBuilder.LabelValueBuilder value(String value) {
      return new PromQueryBuilder.LabelValueBuilder(value);
    }

    public PromQueryBuilder.LabelValueBuilder values(List<String> values) {
      return new PromQueryBuilder.LabelValueBuilder(values);
    }
  }

  public static QueryBuilders builder() {
    return QUERY_BUILDERS;
  }

  // These "value" methods are placed here for the sake of simplicity on the end-user side, no need to import and use
  // anything else to build a PromQuery other than the PromQuery itself.

  public static PromQueryBuilder.LabelBuilder label(String label) {
    return QUERY_BUILDERS.label(label);
  }

  public static PromQueryBuilder.LabelValueBuilder value(String value) {
    return QUERY_BUILDERS.value(value);
  }

  public static PromQueryBuilder.LabelValueBuilder values(List<String> values) {
    return QUERY_BUILDERS.values(values);
  }

  @Deprecated
  public static PromQueryBuilder.AggregatedQueryBuilder max(PromQueryBuilder.InstantQueryBuilder queryBuilder) {
    return new PromQueryBuilder.AggregatedQueryBuilder(queryBuilder, "max");
  }

  @Deprecated
  public static PromQueryBuilder.AggregatedQueryBuilder irate(PromQueryBuilder.InstantQueryBuilder queryBuilder) {
    return new PromQueryBuilder.AggregatedQueryBuilder(queryBuilder, "irate");
  }

  @Deprecated
  public static PromQueryBuilder.AggregatedQueryBuilder rate(PromQueryBuilder.InstantQueryBuilder queryBuilder) {
    return new PromQueryBuilder.AggregatedQueryBuilder(queryBuilder, "rate");
  }

  @Deprecated
  public static PromQueryBuilder.AggregatedQueryBuilder avg_over_time(PromQueryBuilder.InstantQueryBuilder queryBuilder) {
    return new PromQueryBuilder.AggregatedQueryBuilder(queryBuilder, "avg_over_time");
  }

  private static final QueryBuilders QUERY_BUILDERS = new QueryBuilders();

  public static enum QueryType {
    INSTANT("/query", "query"),
    RANGE("/query_range", "query"),
    SERIES("/series", "match[]"),
    LABELS("/labels", "match[]"),
    LABELS_VALUES("/label/<label_name>/values", "match[]");

    private final String endpoint;
    private final String parameter;

    private QueryType(String endpoint, String parameter) {
      this.endpoint = endpoint;
      this.parameter = parameter;
    }

    boolean is(QueryType queryType) {
      return queryType == this;
    }

    String endpoint() {
      return endpoint;
    }

    String parameter() {
      return parameter;
    }
  }

  public static enum MetadataType {

  }

  /**
   * Ranged query allows to specify a "start" (optional) and an "end" (optional).
   */
  public static abstract class RangedQuery<RQ extends RangedQuery<?>> extends PromQuery {
    private Optional<String> start;
    private Optional<String> end;

    private RangedQuery(QueryType type) {
      super(type);
    }

    private RangedQuery(QueryType type, String metric) {
      super(type, metric);
    }

    private RangedQuery(QueryType type, String metric, String selector) {
      super(type, metric, selector);
    }

    RQ start(Optional<String> start) {
      this.start = start;
      return (RQ) this;
    }

    public Optional<String> start() {
      return start;
    }

    RQ end(Optional<String> end) {
      this.end = end;
      return (RQ) this;
    }

    public Optional<String> end() {
      return end;
    }
  }

  public static final class InstantQuery extends PromQuery {
    private Optional<String> time;
    private Optional<String> duration;

    InstantQuery(String metric) {
      super(QueryType.INSTANT, metric);
    }

    InstantQuery(String metric, String selector) {
      super(QueryType.INSTANT, metric, selector);
    }

    InstantQuery time(Optional<String> time) {
      this.time = time;
      return this;
    }

    public Optional<String> time() {
      return time;
    }

    InstantQuery duration(Optional<String> duration) {
      this.duration = duration;
      return this;
    }

    public Optional<String> duration() {
      return duration;
    }

    public String getQuery() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.getQuery());
      duration.ifPresent(d -> {
        sb.append('[');
        sb.append(d);
        sb.append(']');
      });
      return sb.toString();
    }
  }

  public static final class RangeQuery extends RangedQuery<RangeQuery> {
    private Optional<Integer> step;

    RangeQuery(String metric) {
      super(QueryType.RANGE, metric);
    }

    RangeQuery(String metric, String selector) {
      super(QueryType.RANGE, metric, selector);
    }

    RangeQuery step(Optional<Integer> step) {
      this.step = step;
      return this;
    }

    public Optional<Integer> step() {
      return step;
    }
  }

  /**
   * https://prometheus.io/docs/prometheus/latest/querying/operators/#aggregation-operators
   *
   * TODO: A very simple implementation for the aggregation operators that does not support by, without, and parameters.
   */
  @Deprecated
  public static class AggregatedQuery extends RangedQuery<AggregatedQuery> {

    private final PromQuery internal;
    private final String operator;
    private Optional<Integer> step;

    AggregatedQuery(PromQuery internal, String operator) {
      // Hard-coded as range query for now. It is a valid vector, something that we need to support.
      super(QueryType.RANGE, internal.metric, internal.selector);
      this.internal = internal;
      this.operator = operator;
    }

    AggregatedQuery step(Optional<Integer> step) {
      this.step = step;
      return this;
    }

    public Optional<Integer> step() {
      return step;
    }

    @Override
    public String getQuery() {
      return String.format("%s(%s)", operator, internal.getQuery());
    }

  }

  public static class MetadataQuery extends RangedQuery<MetadataQuery> {
    private List<String> matches;
    private String labelName;

    MetadataQuery(QueryType type) {
      super(type);
    }

    MetadataQuery matches(List<String> matches) {
      this.matches = matches;
      return this;
    }

    public List<String> matches() {
      return matches;
    }

    MetadataQuery labelName(String labelName) {
      this.labelName = labelName;
      return this;
    }

    public String labelName() {
      return labelName;
    }
  }

  // PromQuery

  /**
   * The type is used by PromQueries for selecting the appropriate Prometheus API and supply the appropriate query
   * parameters.
   */
  final QueryType type;
  private String metric;
  private String selector;

  private PromQuery(QueryType type) {
    this.type = type;
  }

  private PromQuery(QueryType type, String metric) {
    this.type = type;
    this.metric = metric;
  }

  private PromQuery(QueryType type, String metric, String selector) {
    this.type = type;
    this.metric = metric;
    this.selector = selector;
  }

  /**
   * Gets the value of the "query" query parameter to send to the Prometheus' API.
   */
  public String getQuery() {
    return String.format("%s%s", Strings.nullToEmpty(metric), Strings.nullToEmpty(selector));
  }

  // Syntactic sugar

  public boolean is(PromQuery.QueryType type) {
    return this.type.is(type);
  }

  public InstantQuery asInstant() {
    return (InstantQuery) this;
  }

  public RangeQuery asRange() {
    return (RangeQuery) this;
  }

  public MetadataQuery asMetadata() {
    return (MetadataQuery) this;
  }
}
