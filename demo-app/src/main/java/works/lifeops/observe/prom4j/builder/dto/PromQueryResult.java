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
import java.util.Map;

import works.lifeops.observe.prom4j.builder.PromQueryResponse;

/**
 * A more end-user form of {@link PromQueryResponseDto}. This class could be a "template" of generated end-user types.
 *
 * @author Li Wan
 */
@lombok.Data
public abstract class PromQueryResult {
  private String name;
  private String job;
  private Map<String, String> labels;

  /**
   * A sample within (or not) a time-series.
   *
   * Note: This type is only to remove the excessive generics introduced by referencing back to Result in ResultValue.
   */
  @SuppressWarnings("rawtypes")
  public static final class Sample extends PromQueryResponse.ResultValue {
    protected Sample(double epochDateTime, String value) {
      super(epochDateTime, value);
    }
  }

  /**
   * An end-user form of {@link PromQueryResponseDto.VectorResultDto}.
   */
  @lombok.Data
  @lombok.ToString
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class SampleResult extends PromQueryResult {
    private PromQueryResult.Sample sample;
  }

  /**
   * An end-user form of {@link PromQueryResponseDto.MatrixResultDto}
   */
  @lombok.Data
  @lombok.ToString
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class TimeSeriesResult extends PromQueryResult {
    private List<PromQueryResult.Sample> samples;
  }
}
