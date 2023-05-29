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

import works.lifeops.observe.prom4j.builder.PromResponse;

@Mapper
public interface PromQueryResultMapper {

  PromQueryResultMapper INSTANCE = Mappers.getMapper(PromQueryResultMapper.class);

  @SuppressWarnings("rawtypes")
  PromQueryResult.Sample resultValueToSample(PromResponse.ResultValue resultValue);

  @Mapping(expression = "java(arg0.getMetric().get(\"name\"))", target = "name")
  @Mapping(expression = "java(arg0.getMetric().get(\"job\"))", target = "job")
  @Mapping(source = "metric", target = "labels")
  @Mapping(source = "value", target = "sample")
  PromQueryResult.SampleResult vectorResultToSampleResult(PromResponse.VectorResult arg0);


  default List<PromQueryResult.SampleResult> vectorResponseToSampleResult(PromResponse<PromResponse.VectorResult> response) {
    return response.getData().getResult().stream()
        .map(this::vectorResultToSampleResult)
        .collect(Collectors.toList());
  }

  @Mapping(expression = "java(arg0.getMetric().get(\"name\"))", target="name")
  @Mapping(expression = "java(arg0.getMetric().get(\"job\"))", target = "job")
  @Mapping(source = "metric", target = "labels")
  @Mapping(source = "values", target = "samples")
  PromQueryResult.TimeSeriesResult matrixResultToTimeSeriesResult(PromResponse.MatrixResult arg0);

  default List<PromQueryResult.TimeSeriesResult> matrixResponseToTimeSeriesResult(PromResponse<PromResponse.MatrixResult> response) {
    return response.getData().getResult().stream()
        .map(this::matrixResultToTimeSeriesResult)
        .collect(Collectors.toList());
  }
}
