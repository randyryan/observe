package works.lifeops.observe.prom4j.builder;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

@Beta
@lombok.Data
@lombok.ToString
public class PromQueryResponse {

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

    @Override
    public String toString() {
      return type;
    }
  }

  public static abstract class Result {

    protected Map<String, String> metric;

  }

  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.EqualsAndHashCode
  public static class VectorResult extends Result {

    private Map<String, String> metric;
    private List<Object> value;

  }

  @lombok.Data
  @lombok.AllArgsConstructor
  public static class MatrixResult extends Result {

    private List<List<Object>> values;

    public Map<String, String> getMetric() {
      return super.metric;
    }

  }

  @lombok.Data
  public static class Data {

    private ResultType resultType;
    private List<VectorResult> result;

    public Data() {
      result = Lists.newArrayList();
    }

  }

  private Status status;
  private Data data;

  public PromQueryResponse() {
    data = new Data();
  }

}
