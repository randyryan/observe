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
import java.util.Objects;

import works.lifeops.observe.prom4j.builder.PromResponse;

/**
 * DTO of the "data.result" part of a {@link PromResponse}.
 * This type is more response-centric than it is with {@link PromResult}.
 *
 * The structure/hierarchy has been flattened to some extent compared to PromResponse.
 *
 * @author Li Wan
 */
@lombok.Data
public abstract class PromResponseDto {
  private String name;
  private Map<String, String> labels;

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class VectorResultDto extends PromResponseDto {
    private PromResponse.ResultValue<PromResponse.VectorResult> value;
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static final class VectrixResultDto extends PromResponseDto {
    private PromResponse.ResultValue<PromResponse.VectrixResult> value;
    private List<PromResponse.ResultValue<PromResponse.VectrixResult>> values;

    public List<PromResponse.ResultValue<PromResponse.VectrixResult>> getValues() {
      return Objects.requireNonNullElseGet(values, () -> List.of(value));
    }
  }

  @lombok.Data
  @lombok.EqualsAndHashCode(callSuper = false)
  public static class MatrixResultDto extends PromResponseDto {
    private List<PromResponse.ResultValue<PromResponse.MatrixResult>> values;
  }
}
