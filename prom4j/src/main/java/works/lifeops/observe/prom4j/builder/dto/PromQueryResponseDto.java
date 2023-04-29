package works.lifeops.observe.prom4j.builder.dto;

import java.util.List;
import java.util.Map;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

/**
 * DTO of the "data.result" part of a {@link PromQueryResponse}.
 *
 * The structure/hierarchy has been flattened to some extent compared to PromQueryResponse.
 *
 * @author Li Wan
 */
@lombok.Data
public abstract class PromQueryResponseDto {
  private String name;
  private Map<String, String> labels;

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class VectorResultDto extends PromQueryResponseDto {
    private PromQueryResponse.ResultValue<PromQueryResponse.VectorResult> value;
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class MatrixResultDto extends PromQueryResponseDto {
    private List<PromQueryResponse.ResultValue<PromQueryResponse.MatrixResult>> values;
  }
}
