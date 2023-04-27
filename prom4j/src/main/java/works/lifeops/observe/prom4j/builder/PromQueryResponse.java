package works.lifeops.observe.prom4j.builder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

/**
 * A Java representation of the response JSON returned from the Prometheus query API.
 *
 * @author Li Wan
 */
@Beta
@lombok.Data
@lombok.ToString
public class PromQueryResponse<R extends PromQueryResponse.Result> {
  /**
   * Maps the "status" node under the JSON root.
   */
  public static enum Status {
    SUCCESS("success"),
    ERROR("error");

    public static Status fromString(String status) {
      for (Status s : values()) {
        if (s.getStatus().equals(status)) {
          return s;
        }
      }
      throw new IllegalArgumentException("No such status: \"" + status + "\".");
    }

    private String status;

    private Status(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }

    @Override
    public String toString() {
      return status;
    }
  }

  /**
   * Maps the "data" node under the JSON root.
   *
   * @param <R> derivative type of the {@link Result}.
   */
  @lombok.Data
  public static class Data<R extends PromQueryResponse.Result> {
    private PromQueryResponse.ResultType resultType;
    private List<R> result;

    public Data() {
      result = Lists.newArrayList();
    }
  }

  /**
   * Maps the "resultType" node under the "data" node.
   */
  public static enum ResultType {
    MATRIX("matrix"),
    VECTOR("vector"),
    SCALAR("scalar"),
    STRING("string");

    public static ResultType fromString(String type) {
      for (ResultType t : values()) {
        if (t.getType().equals(type)) {
          return t;
        }
      }
      throw new IllegalArgumentException("No such result type: \"" + type + "\".");
    }

    private String type;

    private ResultType(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }

    // Syntactic sugar

    public boolean is(ResultType resultType) {
      return resultType == this;
    }

    // Object overrides

    @Override
    public String toString() {
      return type;
    }
  }

  /**
   * Maps the "result" node under the "data" node.
   */
  public static abstract class Result {
    protected Map<String, String> metric;
  }

  /**
   * Maps the "value" under the "result" node.
   */
  @lombok.Data
  @lombok.EqualsAndHashCode
  public static final class ResultValue<R extends Result> {
    public static <R extends Result> ResultValue<R> of(double epochDateTime, String value) {
      return new ResultValue<R>(epochDateTime, value);
    }

    private R result;
    private double epochDateTime;
    private String value;

    private ResultValue(double epochDateTime, String value) {
      this.epochDateTime = epochDateTime;
      this.value = value;
    }

    /**
     * Returns the {@link PromQueryResponse.Result} that owns this value. Used when processing stream of values for
     * scenarios like converting them into a chart's data where it needs to add the metrics information, we can use
     * this method to reference the result that this value belongs to.
     *
     * @return the {@link PromQueryResponse.Result} that owns this value.
     */
    public R getResult() {
      return result;
    }

    public ResultValue<R> setResult(R result) {
      this.result = result;
      return this;
    }

    public OffsetDateTime getOffsetDateTime() {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli((long) (epochDateTime * 1000)), ZoneOffset.UTC);
    }
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class VectorResult extends Result {
    private Map<String, String> metric; // XXX: The same one as the super, just to get the AllArgsConstructor work
    private ResultValue<VectorResult> value;

    VectorResult(Map<String, String> metric, ResultValue<VectorResult> value) {
      this.metric = metric;
      this.value = value.setResult(this); // We can allow the creation of Result first then invoke setResult
      // at each Values' creation at deserialization to avoid setting the Result here. This way the Results
      // class can remain using @AllArgsConstructor
    }
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class MatrixResult extends Result {
    private Map<String, String> metric; // XXX: The same one as the super, just to get the AllArgsConstructor work
    private List<ResultValue<MatrixResult>> values;

    MatrixResult(Map<String, String> metric, List<ResultValue<MatrixResult>> values) {
      this.metric = metric;
      this.values = values;
      this.values.forEach(value -> value.setResult(this)); // The same as the VectorResult but with more reason
      // since this even need to set each value individually.
    }
  }

  // PromQueryResponse

  private Status status;
  private Data<R> data;

  public PromQueryResponse() {
    data = new Data<R>();
  }

  public boolean hasResultOfType(PromQueryResponse.ResultType resultType) {
    return getData().getResultType().is(resultType);
  }
}
