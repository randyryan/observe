package works.lifeops.observe.prom4j.builder;

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

}
