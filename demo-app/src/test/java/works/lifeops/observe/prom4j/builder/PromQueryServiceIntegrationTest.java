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
package works.lifeops.observe.prom4j.builder;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import works.lifeops.observe.prom4j.builder.dto.PromQueryResult;
import works.lifeops.observe.prom4j.builder.dto.PromQueryResultMapper;

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
