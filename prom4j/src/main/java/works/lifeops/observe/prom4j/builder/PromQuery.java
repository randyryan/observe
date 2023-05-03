package works.lifeops.observe.prom4j.builder;

import java.util.Optional;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;

/**
 * Holds a piece of simple PromQL.
 *
 * @author Li Wan
 */
@Beta
public abstract class PromQuery {
  /**
   * Creators of the concrete builders.
   *
   * XXX: We could totally place these methods directly under PromQuery, but for the sake of the builder() method norm,
   *      we'll just be a little bit "chatty" here.
   */
  public static final class QueryBuilders {
    public PromQueryBuilder.InstantQueryBuilder instant() {
      return new PromQueryBuilder.InstantQueryBuilder();
    }

    public PromQueryBuilder.RangeQueryBuilder range() {
      return new PromQueryBuilder.RangeQueryBuilder();
    }

    /**
     * Same as the {@link PromQueryBuilder#value}
     */
    public PromQueryBuilder.LabelValueBuilder value(String value) {
      return new PromQueryBuilder.LabelValueBuilder(value);
    }
  }

  public static QueryBuilders builder() {
    return QUERY_BUILDERS;
  }

  private static final QueryBuilders QUERY_BUILDERS = new QueryBuilders();

  public static PromQueryBuilder.LabelValueBuilder value(String value) {
    return QUERY_BUILDERS.value(value);
  }

  // PromQuery

  final QueryType type;
  private String metric;
  private String selector;

  private PromQuery(QueryType type, String metric) {
    this.type = type;
    this.metric = metric;
  }

  private PromQuery(QueryType type, String metric, String selector) {
    this.type = type;
    this.metric = metric;
    this.selector = selector;
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

  // Object overrides

  @Override
  public String toString() {
    return String.format("%s%s", Strings.nullToEmpty(metric), Strings.nullToEmpty(selector));
  }

  // Internal types

  public static enum QueryType {
    INSTANT("instant"), RANGE("range");

    private String type;

    private QueryType(String value) {
      this.type = value;
    }

    @Override
    public String toString() {
      return type;
    }

    boolean is(QueryType queryType) {
      return queryType == this;
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

    // Object overrides

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      duration.ifPresent(d -> {
        sb.append('[');
        sb.append(d);
        sb.append(']');
      });
      return sb.toString();
    }
  }

  public static final class RangeQuery extends PromQuery {
    private Optional<String> start;
    private Optional<String> end;
    private Optional<Integer> step;

    RangeQuery(String metric) {
      super(QueryType.RANGE, metric);
    }

    RangeQuery(String metric, String selector) {
      super(QueryType.RANGE, metric, selector);
    }

    RangeQuery start(Optional<String> start) {
      this.start = start;
      return this;
    }

    public Optional<String> start() {
      return start;
    }

    RangeQuery end(Optional<String> end) {
      this.end = end;
      return this;
    }

    public Optional<String> end() {
      return end;
    }

    RangeQuery step(Optional<Integer> step) {
      this.step = step;
      return this;
    }

    public Optional<Integer> step() {
      return step;
    }
  }
}
