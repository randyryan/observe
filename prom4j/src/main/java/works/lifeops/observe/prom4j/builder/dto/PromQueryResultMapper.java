package works.lifeops.observe.prom4j.builder.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

@Mapper(componentModel = "spring")
public interface PromQueryResultMapper {

  PromQueryResultMapper INSTANCE = Mappers.getMapper(PromQueryResultMapper.class);

  @SuppressWarnings("rawtypes")
  PromQueryResult.Sample resultValueToSample(PromQueryResponse.ResultValue resultValue);

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target = "name")
  @Mapping(expression = "java(result.getMetric().get(\"job\"))", target = "job")
  @Mapping(source = "metric", target = "labels")
  @Mapping(source = "value", target = "sample")
  PromQueryResult.SampleResult vectorResultToSampleResult(PromQueryResponse.VectorResult result);


  default List<PromQueryResult.SampleResult> vectorResponseToSampleResult(PromQueryResponse<PromQueryResponse.VectorResult> response) {
    return response.getData().getResult().stream()
        .map(this::vectorResultToSampleResult)
        .collect(Collectors.toList());
  }

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target="name")
  @Mapping(expression = "java(result.getMetric().get(\"job\"))", target = "job")
  @Mapping(source = "metric", target = "labels")
  @Mapping(source = "values", target = "samples")
  PromQueryResult.TimeSeriesResult matrixResultToTimeSeriesResult(PromQueryResponse.MatrixResult result);

  default List<PromQueryResult.TimeSeriesResult> matrixResponseToTimeSeriesResult(PromQueryResponse<PromQueryResponse.MatrixResult> response) {
    return response.getData().getResult().stream()
        .map(this::matrixResultToTimeSeriesResult)
        .collect(Collectors.toList());
  }
}
