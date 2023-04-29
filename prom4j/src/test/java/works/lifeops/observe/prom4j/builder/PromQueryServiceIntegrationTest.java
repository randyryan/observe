package works.lifeops.observe.prom4j.builder;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResponseMapper;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResult;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

import java.time.Instant;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PromQueryServiceIntegrationTest {
  @Autowired
  private PromQueryService promQueryService;
  @Autowired
  private PromQueryResultMapper promResultMapper;

  @Test
  public void testBlocking() {
    promQueryService.testBlocking(Optional.empty());
  }

  @Test
  public void queryBlocking() {
    Instant now = Instant.now();

    PromQuery query0 = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .time(now.toString())
        .build();
    PromQuery query1 = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .time(now.toString())
        .build();

    PromQueryResponse<PromQueryResponse.VectorResult> response1 = promQueryService
        .<PromQueryResponse.VectorResult>queryBlocking(query0)
        .getBody();
    PromQueryResponse<PromQueryResponse.VectorResult> response2 = promQueryService
        .<PromQueryResponse.VectorResult>queryBlocking(query1)
        .getBody();

    PromQueryResult.SampleResult sample1 = promResultMapper.vectorResponseToSampleResult(response1).get(0);
    System.out.println(sample1);
  }

}
