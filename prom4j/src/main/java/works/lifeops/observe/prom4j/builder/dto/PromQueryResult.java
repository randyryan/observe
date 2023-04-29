package works.lifeops.observe.prom4j.builder.dto;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

import java.util.List;
import java.util.Map;

/**
 * A more end-user form of {@link PromQueryResponseDto}. This class could be a "template" of generated end-user types.
 *
 * @author Li Wan
 */
@lombok.Data
public abstract class PromQueryResult {
  private String name;
  private String job;
  private Map<String, String> labels;

  /**
   * A sample within (or not) a time-series.
   *
   * Note: This type is only to remove the excessive generics introduced by referencing back to Result in ResultValue.
   */
  @SuppressWarnings("rawtypes")
  public static final class Sample extends PromQueryResponse.ResultValue {
    protected Sample(double epochDateTime, String value) {
      super(epochDateTime, value);
    }
  }

  /**
   * An end-user form of {@link PromQueryResponseDto.VectorResultDto}.
   */
  @lombok.Data
  @lombok.ToString
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class SampleResult extends PromQueryResult {
    private PromQueryResult.Sample sample;
  }

  /**
   * An end-user form of {@link PromQueryResponseDto.MatrixResultDto}
   */
  @lombok.Data
  @lombok.ToString
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class TimeSeriesResult extends PromQueryResult {
    private List<PromQueryResult.Sample> samples;
  }
}
