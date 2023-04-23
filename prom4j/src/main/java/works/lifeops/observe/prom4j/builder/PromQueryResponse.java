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
  @lombok.AllArgsConstructor
  @lombok.EqualsAndHashCode
  public static final class ResultValue {
    public static ResultValue of(double epochDateTime, String value) {
      return new ResultValue(epochDateTime, value);
    }

    private double epochDateTime;
    private String value;

    public OffsetDateTime getOffsetDateTime() {
      return OffsetDateTime.ofInstant(Instant.ofEpochMilli((long) (epochDateTime * 1000)), ZoneOffset.UTC);
    }
  }

  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class VectorResult extends Result {
    private Map<String, String> metric; // XXX: The same one as the super, just to get the AllArgsConstructor work
    private ResultValue value;
  }

  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class MatrixResult extends Result {
    private Map<String, String> metric; // XXX: The same one as the super, just to get the AllArgsConstructor work
    private List<ResultValue> values;
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
