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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class PromResponse<R extends PromResponse.Result> {
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
   * Maps the "data" node under the JSON root. Such structure/schema only applies to Scalar, String, Vector, and Matrix
   * responses. For Series, Labels, and Label values, the "data" is directly a type of {@link Map} or {@link List}.
   *
   * @param <R> derivative type of the {@link Result}.
   */
  @lombok.Data
  public static class Data<R extends PromResponse.Result> {
    /**
     * "resultType" exists in response for Scalar, String, Vector, and Matrix.
     */
    private PromResponse.ResultType resultType;
    /**
     * "result" exists in response for Scalar, String, Vector, and Matrix.
     */
    private List<R> result;

    public Data() {
      result = Lists.newArrayList();
    }

    public boolean addResult(R result) {
      return this.result.add(result);
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
//    protected Map<String, String> metric;
  }

  /**
   * Maps the "value" under the "result" node. This can be a single sample within a time-series.
   */
  @lombok.Data
  @lombok.ToString(exclude = "result") // When included, it will cause a circular invocation
  @lombok.EqualsAndHashCode
  public static class ResultValue<R extends Result> {
    public static <R extends Result> ResultValue<R> of(double epochDateTime, String value) {
      return new ResultValue<R>(epochDateTime, value);
    }

    public static <R extends Result> ResultValue<R> copy(ResultValue<?> resultValue) {
      return new ResultValue<R>(resultValue.epochDateTime, resultValue.value);
    }

    private R result;
    private double epochDateTime;
    private String value;

    protected ResultValue(double epochDateTime, String value) {
      this.epochDateTime = epochDateTime;
      this.value = value;
    }

    /**
     * Returns the {@link PromResponse.Result} that owns this value. Used when processing stream of values for
     * scenarios like converting them into a chart's data where it needs to add the metrics information, we can use
     * this method to reference the result that this value belongs to.
     *
     * @return the {@link PromResponse.Result} that owns this value.
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

  // TODO: public static class ScalarResult {}

  // TODO: public static class StringResult {}

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class VectorResult extends Result {
    private Map<String, String> metric; // XXX: The same one as the super, just to get the AllArgsConstructor work
    private ResultValue<VectorResult> value;

    VectorResult(Map<String, String> metric, ResultValue<VectorResult> value) {
      this.metric = metric;
      this.value = value.setResult(this); // We can allow the creation of Result first then invoke setResult
      // at each Values' creation at deserialization to avoid setting the Result here. This way the Result concrete
      // classes can back to annotated by @AllArgsConstructor
    }

    public MatrixResult toMatrixResult() {
      return new MatrixResult(metric, List.of(ResultValue.copy(VectorResult.this.value)));
    }
  }

  @lombok.Data
  @lombok.ToString
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

  /**
   * A type of {@link PromResponse.Result} can be used for both {@link ResultType#VECTOR} and {@link ResultType#MATRIX}
   *
   * Note: This type will be used to gradually phase out {@link VectorResult} and {@link MatrixResult}.
   *
   */
  @lombok.Data
  @lombok.ToString
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class VectrixResult extends Result {
    private Map<String, String> metric;
    private ResultValue<VectrixResult> value;
    private List<ResultValue<VectrixResult>> values;

    VectrixResult(Map<String, String> metric, ResultValue<VectrixResult> value) {
      this.metric = metric;
      this.value = value.setResult(this);
      this.values = List.of(value);
    }

    VectrixResult(Map<String, String> metric, List<ResultValue<VectrixResult>> values) {
      this.metric = metric;
      this.values = values;
      this.values.forEach(value -> value.setResult(this));
    }

    public VectorResult toVectorResult() {
      return new VectorResult(metric, ResultValue.copy(VectrixResult.this.value));
    }

    public MatrixResult toMatrixResult() {
      if (values.size() == 1) {
        return new MatrixResult(metric, List.of(ResultValue.copy(VectrixResult.this.value)));
      } else {
        List<ResultValue<MatrixResult>> matrixValues = values.stream()
            .map(ResultValue::<MatrixResult>copy)
            .collect(Collectors.toList());
        return new MatrixResult(metric, matrixValues);
      }
    }
  }

  // public static class SeriesResult {} no need a

  // PromResponse

  private Status status;
  private Data<R> data;

  public PromResponse() {
    data = new Data<R>();
  }

  public boolean hasResultOfType(PromResponse.ResultType resultType) {
    return getData().getResultType().is(resultType);
  }
}
