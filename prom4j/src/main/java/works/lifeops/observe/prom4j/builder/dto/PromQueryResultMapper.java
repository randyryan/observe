package works.lifeops.observe.prom4j.builder.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

@Mapper
public interface PromQueryResultMapper {
  PromQueryResultMapper INSTANCE = Mappers.getMapper(PromQueryResultMapper.class);

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResultDto.VectorResultDto vectorResultToDto(PromQueryResponse.VectorResult result);

  default List<PromQueryResultDto.VectorResultDto> vectorResponseToResultDtos(PromQueryResponse<PromQueryResponse.VectorResult> response) {
    return response.getData().getResult().stream()
        .map(this::vectorResultToDto)
        .collect(Collectors.toList());
  }

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResultDto.MatrixResultDto matrixResultToDto(PromQueryResponse.MatrixResult result);

  default List<PromQueryResultDto.MatrixResultDto> matrixResponseToResultDtos(PromQueryResponse<PromQueryResponse.MatrixResult> response) {
    return response.getData().getResult().stream()
        .map(this::matrixResultToDto)
        .collect(Collectors.toList());
  }
}
