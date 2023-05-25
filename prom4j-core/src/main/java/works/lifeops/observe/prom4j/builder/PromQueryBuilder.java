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

import static works.lifeops.observe.prom4j.builder.PromQuery.value;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Builds {@link PromQuery.InstantQuery} and {@link PromQuery.RangeQuery} query objects, invoked through
 * {@code PromQuery.builder()}.
 *
 * Type parameter B was added for the subtypes to inherit the {@link PromQueryBuilder#metric} and the
 * {@link PromQueryBuilder#label} methods. Need to review the practicality of doing so.
 *
 * @author Li Wan
 */
@Beta
@SuppressWarnings("unchecked")
public abstract class PromQueryBuilder<B extends PromQueryBuilder<B, PQ>, PQ extends PromQuery> {
  final PromQuery.QueryType queryType;
  private String metric;
  private List<String> criteria;

  private PromQueryBuilder(PromQuery.QueryType queryType) {
    this.queryType = queryType;
    this.criteria = Lists.newArrayList();
  }

  /**
   * Sets the metric name.
   *
   * TODO: This is to become MetricStep in DSL.
   */
  public B metric(String metric) {
    this.metric = metric;
    return (B) this;
  }

  /**
   * Sets the label.
   *
   * TODO: This is to become LabelStep in DSL.
   */
  public LabelBuilderHelper<B> label(String label) {
    return new LabelBuilderHelper<B>(label);
  }

  public B label(LabelBuilder labelBuilder) {
    String criterion = labelBuilder.build();
    if (!Strings.isNullOrEmpty(criterion)) {
      // After LabelBuilder.build() returns "", we need to exclude these.
      // TODO: Avoid "patching" like this, think of way to avoid building label when empty was passed.
      criteria.add(criterion);
    }
    return (B) this;
  }

  public B labels(LabelBuilder... labelBuilders) {
    for (LabelBuilder labelBuilder : labelBuilders) {
      String criterion = labelBuilder.build();
      if (!Strings.isNullOrEmpty(criterion)) {
        // After LabelBuilder.build() returns "", we need to exclude these.
        // TODO: Avoid "patching" like this, think of way to avoid building label when empty was passed.
        criteria.add(criterion);
      }
    }
    return (B) this;
  }

  public B labels(Collection<? extends LabelBuilder> labelBuilders) {
    for (LabelBuilder labelBuilder : labelBuilders) {
      String criterion = labelBuilder.build();
      if (!Strings.isNullOrEmpty(criterion)) {
        // After LabelBuilder.build() returns "", we need to exclude these.
        // TODO: Avoid "patching" like this, think of way to avoid building label when empty was passed.
        criteria.add(criterion);
      }
    }
    return (B) this;
  }

  public B labels(Iterable<? extends LabelBuilder> labelBuilders) {
    for (LabelBuilder labelBuilder : labelBuilders) {
      String criterion = labelBuilder.build();
      if (!Strings.isNullOrEmpty(criterion)) {
        // After LabelBuilder.build() returns "", we need to exclude these.
        // TODO: Avoid "patching" like this, think of way to avoid building label when empty was passed.
        criteria.add(criterion);
      }
    }
    return (B) this;
  }

  public B labels(Optional<List<? extends LabelBuilder>> labelBuilders) {
    labelBuilders.ifPresent(this::labels);
    return (B) this;
  }

  /**
   * Builds the query.
   *
   * TODO: This is to become FinalStep in DSL.
   */
  public abstract PQ build();

  // Internal types

  @Accessors(fluent = true)
  public static final class DurationBuilder {
    private final InstantQueryBuilder builder;
    @Setter
    private long ms;
    @Setter
    private long s;
    @Setter
    private long m;
    @Setter
    private long h;
    @Setter
    private long d;
    @Setter
    private long w;
    @Setter
    private long y;

    private DurationBuilder(InstantQueryBuilder builder) {
      this.builder = builder;
    }

    public DurationBuilder from(java.time.Duration duration) {
      throw new UnsupportedOperationException("Not implemented");
    }

    public PromQuery.InstantQuery build() {
      builder.duration = Optional.of(toString());
      return builder.build();
    }

    @Override
    public String toString() {
      /*
      return String.format("%s%s%s%s%s%s%s",
          y > 0 ? y + "y" : "",
          w > 0 ? w + "w" : "",
          d > 0 ? d + "d" : "",
          h > 0 ? h + "d" : "",
          m > 0 ? m + "m" : "",
          s > 0 ? s + "s" : "",
          ms > 0 ? ms + "ms" : "");
      */
      // StringBuilder is a faster approach according to benchmarking
      StringBuilder sb = new StringBuilder();
      if (y > 0) {
        sb.append(y);
        sb.append('y');
      }
      if (w > 0) {
        sb.append(w);
        sb.append('w');
      }
      if (d > 0) {
        sb.append(d);
        sb.append('d');
      }
      if (h > 0) {
        sb.append(h);
        sb.append('h');
      }
      if (m > 0) {
        sb.append(m);
        sb.append('m');
      }
      if (s > 0) {
        sb.append(s);
        sb.append('s');
      }
      if (ms > 0) {
        sb.append(ms);
        sb.append("ms");
      }
      return sb.toString();
    }
  }

  public static class InstantQueryBuilder extends PromQueryBuilder<InstantQueryBuilder, PromQuery.InstantQuery> {
    private Optional<String> time = Optional.empty();
    /**
     * TODO: Use {@link java.time.Duration} and {@link java.time.Period} to handle the following:
     *
     * ms - milliseconds
     * s - seconds
     * m - minutes
     * h - hours
     * d - days - assuming a day has always 24h
     * w - weeks - assuming a week has always 7d
     * y - years - assuming a year has always 365d
     */
    private Optional<String> duration = Optional.empty();

    InstantQueryBuilder() {
      super(PromQuery.QueryType.INSTANT);
    }

    public InstantQueryBuilder time(String rfc3339) {
      this.time = Optional.ofNullable(rfc3339);
      return this;
    }

    public InstantQueryBuilder time(Optional<String> rfc3339) {
      this.time = rfc3339;
      return this;
    }

    public InstantQueryBuilder duration(Optional<String> duration) {
      this.duration = duration;
      return this;
    }

    public DurationBuilder duration() {
        return new DurationBuilder(this);
    }

    @Override
    public PromQuery.InstantQuery build() {
      String selector = super.criteria.isEmpty() ?
          "" :
          String.format("{%s}", String.join(",", super.criteria));
      return new PromQuery.InstantQuery(super.metric, selector)
          .time(time)
          .duration(duration);
    }
  }

  public static class RangeQueryBuilder extends PromQueryBuilder<RangeQueryBuilder, PromQuery.RangeQuery> {
    private Optional<String> start = Optional.empty();
    private Optional<String> end = Optional.empty();
    private Optional<Integer> step = Optional.empty();

    RangeQueryBuilder() {
      super(PromQuery.QueryType.RANGE);
    }

    public RangeQueryBuilder start(String rfc3339) {
      this.start = Optional.ofNullable(rfc3339);
      return this;
    }

    public RangeQueryBuilder start(Optional<String> rfc3339) {
      this.start = rfc3339;
      return this;
    }

    public RangeQueryBuilder end(String rfc3339) {
      this.end = Optional.ofNullable(rfc3339);
      return this;
    }

    public RangeQueryBuilder end(Optional<String> rfc3339) {
      this.end = rfc3339;
      return this;
    }

    public RangeQueryBuilder step(int step) {
      this.step = Optional.of(step);
      return this;
    }

    public RangeQueryBuilder step(Optional<Integer> step) {
      this.step = step;
      return this;
    }

    @Override
    public PromQuery.RangeQuery build() {
      String selector = super.criteria.isEmpty() ?
          "" :
          String.format("{%s}", String.join(",", super.criteria));
      return new PromQuery.RangeQuery(super.metric, selector)
          .start(start)
          .end(end)
          .step(step);
    }
  }

  public static class LabelBuilder {
    private String label;
    private String operator;
    private String value;

    LabelBuilder(String label) {
      this.label = label;
    }

    public LabelBuilder is(String value) {
      return is(value(value));
    }

    /**
     * To provide the values through the value builder semantics, one of the 2 flavors that our label builder offers.
     */
    public LabelBuilder is(LabelValueBuilder value) {
      if (value.regex()) {
        this.operator = "=~";
      } else {
        this.operator = "=";
      }
      this.value = value.toString();

      return this;
    }

    public LabelBuilder isNot(String value) {
      return isNot(value(value));
    }

    /**
     * To provide the values through the value builder semantics, one of the 2 flavors that our label builder offers.
     */
    public LabelBuilder isNot(LabelValueBuilder value) {
      if (value.regex()) {
        this.operator = "!~";
      } else {
        this.operator = "!=";
      }

      return this;
    }

    public LabelBuilder in(List<String> values) {
      is(new LabelValueBuilder(values));
      return this;
    }

    // TODO: Review: Is Optional of List really necessary?
    public LabelBuilder in(Optional<List<String>> values) {
      values.map(LabelValueBuilder::new).ifPresent(this::is);
      return this;
    }

    public LabelBuilder notIn(List<String> values) {
      isNot(new LabelValueBuilder(values));
      return this;
    }

    // TODO: Review: Is Optional of List really necessary?
    public LabelBuilder notIn(Optional<List<String>> values) {
      values.map(LabelValueBuilder::new).ifPresent(this::isNot);
      return this;
    }

    public String build() {
      if (operator == null && value == null) {
        // Optional.empty() was passed to in or notIn
        return "";
      }
      return String.format("%s%s%s", label, operator, value);
    }
  }

  /**
   * A decorator to help the semantics of: {@code PromQuery.builder().label("job").is("prometheus")}.
   */
  @SuppressWarnings("hiding")
  public class LabelBuilderHelper<B extends PromQueryBuilder<B, ?>> {
    private LabelBuilder labelBuilder;

    private LabelBuilderHelper(String label) {
      this.labelBuilder = new LabelBuilder(label);
    }

    @Deprecated(forRemoval = true)
    public B equals(String value) {
      return is(value);
    }

    @Deprecated(forRemoval = true)
    public B equals(LabelValueBuilder value) {
      return is(value);
    }

    @Deprecated(forRemoval = true)
    public B notEquals(String value) {
      return isNot(value);
    }

    @Deprecated(forRemoval = true)
    public B notEquals(LabelValueBuilder value) {
      return isNot(value);
    }

    public B is(String value) {
      labelBuilder.is(value);
      return queryBuilder();
    }

    public B is(LabelValueBuilder value) {
      labelBuilder.is(value);
      return queryBuilder();
    }

    public B isNot(String value) {
      labelBuilder.is(value);
      return queryBuilder();
    }

    public B isNot(LabelValueBuilder value) {
      labelBuilder.isNot(value);
      return queryBuilder();
    }

    public B in(List<String> values) {
      labelBuilder.in(values);
      return queryBuilder();
    }

    public B in(Optional<List<String>> values) {
      labelBuilder.in(values);
      return queryBuilder();
    }

    public B notIn(List<String> values) {
      labelBuilder.notIn(values);
      return queryBuilder();
    }

    public B notIn(Optional<List<String>> values) {
      labelBuilder.notIn(values);
      return queryBuilder();
    }

    B queryBuilder() {
      PromQueryBuilder.this.criteria.add(this.labelBuilder.build());
      return (B) PromQueryBuilder.this;
    }
  }

  /**
   * Label value builder for "or" only of RE2.
   */
  public static class LabelValueBuilder {
    private String value;
    private List<String> values;

    LabelValueBuilder() {
      this.values = Lists.newArrayList();
    }

    LabelValueBuilder(List<String> values) {
      this.values = values;
    }

    LabelValueBuilder(String value) {
      this.values = Lists.newArrayList(value);
      this.value = value;
    }

    public LabelValueBuilder or(String anotherValue) {
      this.values.add(anotherValue);
      return this;
    }

    boolean regex() {
      return values.size() > 1;
    }

    @Override
    public String toString() {
      String expression = values.size() == 1 ?
          value != null ? value : values.get(0) :
          String.join("|", values);
      return String.format("\"%s\"", expression);
    }
  }
}
