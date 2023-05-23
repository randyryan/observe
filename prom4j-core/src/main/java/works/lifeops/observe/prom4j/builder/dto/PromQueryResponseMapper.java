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

  @Mapping(expression = "java(arg0.getMetric().get(\"__name__\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResponseDto.VectorResultDto vectorResultToDto(PromQueryResponse.VectorResult arg0);

  default List<PromQueryResponseDto.VectorResultDto> vectorResponseToDto(PromQueryResponse<PromQueryResponse.VectorResult> response) {
    return response.getData().getResult().stream()
        .map(this::vectorResultToDto)
        .collect(Collectors.toList());
  }

  @Mapping(expression = "java(arg0.getMetric().get(\"__name__\"))", target="name")
  @Mapping(source = "metric", target = "labels")
  PromQueryResponseDto.MatrixResultDto matrixResultToDto(PromQueryResponse.MatrixResult arg0);

  default List<PromQueryResponseDto.MatrixResultDto> matrixResponseToDto(PromQueryResponse<PromQueryResponse.MatrixResult> response) {
    return response.getData().getResult().stream()
        .map(this::matrixResultToDto)
        .collect(Collectors.toList());
  }
}
