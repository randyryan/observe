package works.lifeops.observe.prom4j.builder.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;


@Mapper
public interface PromQueryResponseMapper {
  PromQueryResponseMapper INSTANCE = Mappers.getMapper(PromQueryResponseMapper.class);

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResponseDto.VectorResultDto vectorResultToDto(PromQueryResponse.VectorResult result);

  default List<PromQueryResponseDto.VectorResultDto> vectorResponseToDto(PromQueryResponse<PromQueryResponse.VectorResult> response) {
    return response.getData().getResult().stream()
        .map(this::vectorResultToDto)
        .collect(Collectors.toList());
  }

  @Mapping(expression = "java(result.getMetric().get(\"name\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResponseDto.MatrixResultDto matrixResultToDto(PromQueryResponse.MatrixResult result);

  default List<PromQueryResponseDto.MatrixResultDto> matrixResponseToDto(PromQueryResponse<PromQueryResponse.MatrixResult> response) {
    return response.getData().getResult().stream()
        .map(this::matrixResultToDto)
        .collect(Collectors.toList());
  }
}
