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
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import works.lifeops.observe.prom4j.api.DemoApiDelegate;
import works.lifeops.observe.prom4j.api.dto.ChartSampleDto;
import works.lifeops.observe.prom4j.api.dto.SampleDto;
import works.lifeops.observe.prom4j.api.dto.TimeSeriesDto;
import works.lifeops.observe.prom4j.builder.PromMetadata;
import works.lifeops.observe.prom4j.builder.PromQuery;
import works.lifeops.observe.prom4j.builder.PromQueryService;
import works.lifeops.observe.prom4j.builder.PromResponse;
import works.lifeops.observe.prom4j.builder.dto.PromResponseDto;
import works.lifeops.observe.prom4j.builder.dto.PromResponseMapper;

/**
 * Showcase how you can use prom4j.
 *
 * @author Li Wan
 */
@Slf4j
@Component
public class DemoApiDelegateImpl implements DemoApiDelegate {
  private final PromQueryService promQueryService;

  DemoApiDelegateImpl(final PromQueryService promQueryService) {
    this.promQueryService = promQueryService;
  }

  @Override
  public ResponseEntity<SampleDto> getGoThreads() {
    PromQuery.InstantQuery promQuery = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .build();

    List<SampleDto> samples = promQueryService.getSamples(promQuery).stream()
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

    List<TimeSeriesDto> timeSerieses = promQueryService.getTimeSeries(promQuery).stream()
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
    PromQuery promQuery = PromQuery.builder()
        .range()
        .metric("go_threads")
        .start(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)) // Should we accept the ISO-8601 string here or the OffsetDateTime
        .end(end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .step(step)
        .build();

    PromResponse<PromResponse.MatrixResult> response = promQueryService
        .<PromResponse.MatrixResult>queryBlocking(promQuery)
        .getBody();
    List<PromResponseDto.MatrixResultDto> results = PromResponseMapper.INSTANCE.matrixResponseToDto(response);

    return DemoApiDelegate.super.getGoThreadsRangeChart(start, end, step);
  }

  @Override
  public ResponseEntity<List<String>> getSeries(OffsetDateTime start, OffsetDateTime end) {
    PromQuery promQuery = PromQuery.builder()
        .series()
        .match("go_threads")
        .start(Optional.ofNullable(start).map(start_ -> start_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .end(Optional.ofNullable(end).map(end_ -> end_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .build();

//    PromMetadata.Series series = promQueryService
//        .<PromMetadata.Series>queryMetadata(promQuery)
//        .getBody();
    PromMetadata.Series series = promQueryService
        .querySeriesMetadata(promQuery)
        .getBody();

    log.info("Got series of size {}.", series.getData().size());

    List<String> seriesNames = series.getData().stream()
        .map(m -> m.get("__name__"))
        .collect(Collectors.toList());

    return ResponseEntity.ok(seriesNames);
  }

  @Override
  public ResponseEntity<List<String>> getLabels(OffsetDateTime start, OffsetDateTime end) {
    PromQuery promQuery = PromQuery.builder()
        .labels()
        .match("go_threads")
        .start(Optional.ofNullable(start).map(start_ -> start_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .end(Optional.ofNullable(end).map(end_ -> end_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .build();

//    PromMetadata.Labels labels = promQueryService
//        .<PromMetadata.Labels>queryMetadata(promQuery)
//        .getBody();
    PromMetadata.Labels labels = promQueryService
        .queryLabelsMetadata(promQuery)
        .getBody();

    return ResponseEntity.ok(labels.getData());
  }

  @Override
  public ResponseEntity<List<String>> getLabelValues(OffsetDateTime start, OffsetDateTime end) {
    PromQuery promQuery = PromQuery.builder()
        .labelValues()
        .match("go_threads")
        .labelName("job")
        .start(Optional.ofNullable(start).map(start_ -> start_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .end(Optional.ofNullable(end).map(end_ -> end_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        .build();

//    PromMetadata.Labels labelValues = promQueryService
//        .<PromMetadata.Labels>queryMetadata(promQuery)
//        .getBody();
    PromMetadata.Labels labelValues = promQueryService
        .queryLabelsMetadata(promQuery)
        .getBody();

    return ResponseEntity.ok(labelValues.getData());
  }
}
