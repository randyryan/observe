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
package works.lifeops.observe.demo.resource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import works.lifeops.observe.prom4j.api.DemoApiDelegate;
import works.lifeops.observe.prom4j.api.dto.ChartSampleDto;
import works.lifeops.observe.prom4j.api.dto.SampleDto;
import works.lifeops.observe.prom4j.api.dto.TimeSeriesDto;
import works.lifeops.observe.prom4j.builder.PromQuery;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;
import works.lifeops.observe.prom4j.builder.PromQueryService;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResult;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

/**
 * Showcase how you can use prom4j.
 *
 * @author Li Wan
 */
@Slf4j
@Component
public class DemoApiDelegateImpl implements DemoApiDelegate {
  private final PromQueryService promQueryService;
  private final PromQueryResultMapper promResultMapper;

  DemoApiDelegateImpl(final PromQueryService promQueryService,
                      final PromQueryResultMapper promResultMapper) {
    this.promQueryService = promQueryService;
    this.promResultMapper = promResultMapper;
  }

  @Override
  public ResponseEntity<SampleDto> getGoThreads() {
    PromQuery promQuery = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .build();
    PromQueryResponse<PromQueryResponse.VectorResult> response = promQueryService
        .<PromQueryResponse.VectorResult>queryBlocking(promQuery)
        .getBody();
    List<PromQueryResult.SampleResult> results = promResultMapper.vectorResponseToSampleResult(response);
    List<SampleDto> samples = results.stream()
        .map(result -> {
          SampleDto sample = new SampleDto();
          sample.setDate(result.getSample().getOffsetDateTime());
          sample.setValue(Integer.parseInt(result.getSample().getValue()));
          return sample;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(samples.get(0));
  }

  @Override
  public ResponseEntity<List<TimeSeriesDto>> getGoThreadsRange(OffsetDateTime start, OffsetDateTime end, Integer step) {
    PromQuery promQuery = PromQuery.builder()
        .range()
        .metric("go_threads")
        .start(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)) // Should we accept the ISO-8601 string here or the OffsetDateTime/
        .end(end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .step(step)
        .build();
    PromQueryResponse<PromQueryResponse.MatrixResult> response = promQueryService
        .<PromQueryResponse.MatrixResult>queryBlocking(promQuery)
        .getBody();
    List<PromQueryResult.TimeSeriesResult> results = promResultMapper.matrixResponseToTimeSeriesResult(response);
    List<TimeSeriesDto> timeSerieses = results.stream()
        .map(result -> {
          TimeSeriesDto timeSeries = new TimeSeriesDto();
          result.getSamples().forEach(sample -> {
            SampleDto dto = new SampleDto();
            dto.setValue(Integer.parseInt(sample.getValue()));
            dto.setDate(sample.getOffsetDateTime());

            timeSeries.addSamplesItem(dto);
          });
          return timeSeries;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(timeSerieses);
  }

  @Override
  public ResponseEntity<List<ChartSampleDto>> getGoThreadsRangeChart(OffsetDateTime start, OffsetDateTime end, Integer step) {
    return DemoApiDelegate.super.getGoThreadsRangeChart(start, end, step);
  }
}
