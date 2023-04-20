package works.lifeops.observe.prom4j.builder.dto;

import java.util.List;
import java.util.Map;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

/**
 * DTO of the "data.result" part of a {@link PromQueryResponse}. The structure/hierarchy has been flattened to some
 * extent.
 */
@lombok.Data
public abstract class PromQueryResultDto {
  private String name;
  private Map<String, String> labels;

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class VectorResultDto extends PromQueryResultDto {
    private PromQueryResponse.ResultValue value;
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class MatrixResultDto extends PromQueryResultDto {
    private List<PromQueryResponse.ResultValue> values;
  }
}
