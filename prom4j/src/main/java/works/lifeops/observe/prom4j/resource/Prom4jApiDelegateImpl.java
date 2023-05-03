package works.lifeops.observe.prom4j.resource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import works.lifeops.observe.prom4j.api.Prom4jApiDelegate;
import works.lifeops.observe.prom4j.api.dto.ChartSampleDto;
import works.lifeops.observe.prom4j.api.dto.SampleDto;
import works.lifeops.observe.prom4j.api.dto.TimeSeriesDto;
import works.lifeops.observe.prom4j.builder.PromQuery;
import works.lifeops.observe.prom4j.builder.PromQueryResponse;
import works.lifeops.observe.prom4j.builder.PromQueryService;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResult;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

@Slf4j
@Component
public class Prom4jApiDelegateImpl implements Prom4jApiDelegate {
  private final PromQueryService promQueryService;
  private final PromQueryResultMapper promResultMapper;

  Prom4jApiDelegateImpl(final PromQueryService promQueryService,
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
    return Prom4jApiDelegate.super.getGoThreadsRangeChart(start, end, step);
  }
}
