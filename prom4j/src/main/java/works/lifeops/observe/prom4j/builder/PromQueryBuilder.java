package works.lifeops.observe.prom4j.builder;

import java.util.List;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

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

  public static class InstantQueryBuilder extends PromQueryBuilder<InstantQueryBuilder, PromQuery.InstantQuery> {
    private String time;

    InstantQueryBuilder() {
      super(PromQuery.InstantQuery.class, PromQuery.QueryType.INSTANT);
    }

    public InstantQueryBuilder time(String rfc3339) {
      this.time = rfc3339;
      return this;
    }

    @Override
    public PromQuery.InstantQuery build() {
      String selector = super.criteria.isEmpty() ?
          "" :
          String.format("{%s}", String.join(",", super.criteria));
      return new PromQuery.InstantQuery(super.metric, selector)
          .time(time);
    }
  }

  public static class RangeQueryBuilder extends PromQueryBuilder<RangeQueryBuilder, PromQuery.RangeQuery> {
    private String start;
    private String end;

    RangeQueryBuilder() {
      super(PromQuery.RangeQuery.class, PromQuery.QueryType.RANGE);
    }

    public RangeQueryBuilder start(String rfc3339) {
      this.start = rfc3339;
      return this;
    }

    public RangeQueryBuilder end(String rfc3339) {
      this.end = rfc3339;
      return this;
    }

    @Override
    public PromQuery.RangeQuery build() {
      String selector = super.criteria.isEmpty() ?
          "" :
          String.format("{%s}", String.join(",", super.criteria));
      return new PromQuery.RangeQuery(super.metric, selector)
          .start(start)
          .end(end);
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
