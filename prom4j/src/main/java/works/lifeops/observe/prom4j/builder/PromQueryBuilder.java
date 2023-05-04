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
import com.google.common.collect.Lists;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * A builder of PromQuery objects, currently has no integrity checks and validations.
 *
 * XXX: Type parameter B can be removed, it only serves for the subtypes to inherit the {@link PromQueryBuilder#metric}
 *      and the {@link PromQueryBuilder#label} methods.
 *
 * @author Li Wan
 */
@Beta
@SuppressWarnings("unchecked")
public abstract class PromQueryBuilder<B extends PromQueryBuilder<?, ?>, PQ extends PromQuery> {
  /**
   * Creates a label value builder.
   */
  public static PromQueryBuilder.LabelValueBuilder value(String value) {
    return new PromQueryBuilder.LabelValueBuilder(value);
  }

  final PromQuery.QueryType queryType;
  private String metric;
  private List<String> criteria;

  private PromQueryBuilder(Class<PQ> queryClass, PromQuery.QueryType queryType) {
    this.queryType = queryType;
    this.criteria = Lists.newArrayList();
  }

  /**
   * Sets the metric name.
   */
  public B metric(String metric) {
    this.metric = metric;
    return (B) this;
  }

  public LabelBuilder<B> label(String label) {
    return new LabelBuilder<B>((B) this, label);
  }

  protected void add(String criterion) {
    criteria.add(criterion);
  }

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

    /**
     * Let the duration to be the last step of building an instant query. Another thought: Label value builder.
     */
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
      super(PromQuery.InstantQuery.class, PromQuery.QueryType.INSTANT);
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
      super(PromQuery.RangeQuery.class, PromQuery.QueryType.RANGE);
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

  public static class LabelBuilder<B extends PromQueryBuilder<?, ?>> {
    private B builder;
    private String label;
    private String operator;
    private String value;

    private LabelBuilder(B builder, String label) {
      this.builder = builder;
      this.label = label;
    }

    public B equals(String value) {
      return equals(value(value));
    }

    public B equals(LabelValueBuilder value) {
      if (value.regex) {
        this.operator = "=~";
      } else {
        this.operator = "=";
      }
      this.value = value.toString();

      return builder();
    }

    public B notEquals(String value) {
      return notEquals(value(value));
    }

    public B notEquals(LabelValueBuilder value) {
      if (value.regex) {
        this.operator = "!~";
      } else {
        this.operator = "!=";
      }

      return builder();
    }

    public B builder() {
      String criterion = String.format("%s%s%s", label, operator, value);
      builder.add(criterion);
      return builder;
    }
  }

  /**
   * An "or" only implementation of RE2.
   */
  public static class LabelValueBuilder {
    private boolean regex;
    private String value;

    LabelValueBuilder(String value) {
      this.value = value;
    }

    public LabelValueBuilder or(String value) {
      this.regex = true;
      this.value += "|";
      this.value += value;

      return this;
    }

    @Override
    public String toString() {
      return String.format("\"%s\"", value);
    }
  }
}
