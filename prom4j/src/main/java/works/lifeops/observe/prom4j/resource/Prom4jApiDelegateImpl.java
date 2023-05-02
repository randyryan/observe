package works.lifeops.observe.prom4j.resource;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import works.lifeops.observe.prom4j.api.Prom4jApiDelegate;
import works.lifeops.observe.prom4j.api.dto.ChartSampleDto;
import works.lifeops.observe.prom4j.api.dto.SampleDto;
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
    PromQueryResponse<PromQueryResponse.VectorResult> response = promQueryService.
        <PromQueryResponse.VectorResult>queryBlocking(promQuery)
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
  public ResponseEntity<List<ChartSampleDto>> getGoThreadsChart() {
    return Prom4jApiDelegate.super.getGoThreadsChart();
  }
}
