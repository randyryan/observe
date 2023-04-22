package works.lifeops.observe.prom4j.builder;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PromQueryServiceIntegrationTest {
  @Autowired
  private PromQueryService promQueryService;

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

    promQueryService.queryBlocking(query0);
    promQueryService.queryBlocking(query1);
  }

}
