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

import static works.lifeops.observe.prom4j.builder.PromQuery.label;
import static works.lifeops.observe.prom4j.builder.PromQuery.value;
import static works.lifeops.observe.prom4j.builder.PromQuery.values;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PromQueryBuilderTest {
  private static final String METRIC_NAME = "go_threads";

  @Test
  @DisplayName("PromQuery.type and PromQuery.is")
  public void instantQueryType() {
    PromQuery query = PromQuery.builder()
        .instant()
        .build();

    Assertions.assertEquals(PromQuery.QueryType.INSTANT, query.type, "PromQuery type is \"INSTANT\"");
    Assertions.assertTrue(query.is(PromQuery.QueryType.INSTANT), "PromQuery is returns true on \"INSTANT\"");
    Assertions.assertFalse(query.is(PromQuery.QueryType.RANGE), "PromQuery is returns false on \"RANGE\"");
  }

  @Test
  public void rangeQueryType() {
    PromQuery query = PromQuery.builder()
        .range()
        .build();

    Assertions.assertEquals(PromQuery.QueryType.RANGE, query.type, "PromQuery type is \"RANGE\"");
    Assertions.assertTrue(query.is(PromQuery.QueryType.RANGE), "PromQuery is returns true on \"RANGE\"");
    Assertions.assertFalse(query.is(PromQuery.QueryType.INSTANT), "PromQuery is returns false on \"INSTANT\"");
  }

  @Test
  public void metricOnly() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric(METRIC_NAME)
        .build();

    Assertions.assertEquals("go_threads", query.toString(), "PromQuery metric (only) is properly built");
  }

  @Test
  @DisplayName("Label-only query building")
  public void labelOnly() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").is("prometheus")
        .build();

    Assertions.assertEquals("{job=\"prometheus\"}", query.toString(), "PromQuery label (only) is properly built");
  }

  @Test
  @DisplayName("First-class label values building")
  public void labelValues() {
    PromQueryBuilder.LabelValueBuilder labelValue1 = PromQuery.value("prometheus");
    PromQueryBuilder.LabelValueBuilder labelValue2 = PromQuery.value("prometheus").or("eureka");
    PromQueryBuilder.LabelValueBuilder labelValue3 = PromQuery.value("prometheus").or("eureka").or("consul");

    Assertions.assertEquals("\"prometheus\"", labelValue1.toString(), "Unary value properly built");
    Assertions.assertEquals("\"prometheus|eureka\"", labelValue2.toString(), "Binary values properly built");
    Assertions.assertEquals("\"prometheus|eureka|consul\"", labelValue3.toString(), "Ternary values properly built");
  }

  @Test
  @DisplayName("First-class label value list building")
  public void labelValueList() {
    PromQueryBuilder.LabelValueBuilder labelValue0 = PromQuery.values(List.of());
    PromQueryBuilder.LabelValueBuilder labelValue1 = PromQuery.values(List.of("prometheus"));
    PromQueryBuilder.LabelValueBuilder labelValue2 = PromQuery.values(List.of("prometheus", "eureka"));
    PromQueryBuilder.LabelValueBuilder labelValue3 = PromQuery.values(List.of("prometheus", "eureka", "consul"));

    Assertions.assertEquals("\"\"", labelValue0.toString(), "Nullary values properly built");
    Assertions.assertEquals("\"prometheus\"", labelValue1.toString(), "Unary value properly built");
    Assertions.assertEquals("\"prometheus|eureka\"", labelValue2.toString(), "Binary values properly built");
    Assertions.assertEquals("\"prometheus|eureka|consul\"", labelValue3.toString(), "Ternary values properly built");
  }

  @Test
  public void labelOptionalValue() {
    PromQueryBuilder.LabelBuilder label0 = PromQuery.label("job").in(Optional.of(List.of()));
    PromQueryBuilder.LabelBuilder label1 = PromQuery.label("job").in(Optional.of(List.of("prometheus")));
    PromQueryBuilder.LabelBuilder label2 = PromQuery.label("job").in(Optional.of(List.of("prometheus", "eureka")));
    PromQueryBuilder.LabelBuilder label3 = PromQuery.label("job").in(Optional.of(List.of("prometheus", "eureka", "consul")));

    Assertions.assertEquals("job=\"\"", label0.build(), "Nullary values properly built");
    Assertions.assertEquals("job=\"prometheus\"", label1.build(), "Unary value properly built");
    Assertions.assertEquals("job=~\"prometheus|eureka\"", label2.build(), "Binary values properly built");
    Assertions.assertEquals("job=~\"prometheus|eureka|consul\"", label3.build(), "Ternary values properly built");
  }

  @Test
  public void labelOptionalOfNull() {
    PromQueryBuilder.LabelBuilder label = PromQuery.label("job").in(Optional.empty());

    Assertions.assertEquals("", label.build(), "None values properly built");
  }

  @Test
  public void labelsNewSemantics() {
    PromQuery promQuery0 = PromQuery.builder()
        .instant()
        .metric("go_metrics")
        .label(label("job").is("prometheus"))
        .build();
    PromQuery promQuery1 = PromQuery.builder()
        .instant()
        .metric("go_metrics")
        .labels(
            label("job").is("prometheus"),
            label("instance").is("localhost:9090")
        )
        .build();

    Assertions.assertEquals("go_metrics{job=\"prometheus\"}", promQuery0.toString(), "First-class label properly built");
    Assertions.assertEquals("go_metrics{job=\"prometheus\",instance=\"localhost:9090\"}", promQuery1.toString(), "Multiple labels properly built");
  }

  @Test
  public void labelsNewSemanticsWithOptionalOfNull() {
    PromQuery promQuery = PromQuery.builder()
            .instant()
            .labels(
                    label("job").is("prometheus"),
                    label("instance").in(Optional.empty())
            )
            .build();

    System.out.println(promQuery.toString());
  }

  @Test
  public void metricAndLabel() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .label("job").is("prometheus")
        .build();

    Assertions.assertEquals("go_threads{job=\"prometheus\"}", query.toString(), "PromQuery metric and label is properly built");
  }

  @Test
  public void multipleLabels() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("instance").is("localhost:9090")
        .label("job").is("prometheus")
        .build();

    Assertions.assertEquals("{instance=\"localhost:9090\",job=\"prometheus\"}", query.toString(), "PromQuery multiple labels are properly built");
  }

  @Test
  public void multipleLabelValuesOr() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").is(value("prometheus").or("eureka"))
        .build();

    Assertions.assertEquals("{job=~\"prometheus|eureka\"}", query.toString(), "PromQuery multiple labels or relationship is properly build");
  }

  @Test
  public void labelIn() {
    PromQuery query = PromQuery.builder()
        .instant()
        .label("job").in(Optional.empty())
        .build();

    System.out.println(query.toString()); // {jobnullnull}
  }

  @Test
  public void duration() {
    PromQuery query = PromQuery.builder()
        .instant()
        .metric("go_threads")
        .duration().m(1).s(30)
        .build();

    Assertions.assertEquals("go_threads[1m30s]", query.toString(), "PromQuery duration is properly built.");
  }

  @Test
  public void range() {
    PromQuery promQuery = PromQuery.builder()
        .range()
        .metric("go_threads")
        .label("job").is(values(List.of("prometheus", "eureka")))
        .start("2023-05-03T19:45:00+08:00")
        .end("2023-05-03T19:47:00+08:00")
        .step(10)
        .build();

    // start, end, and step are query parameters not the query expression
    Assertions.assertEquals("go_threads{job=~\"prometheus|eureka\"}", promQuery.toString(), "Range query expression is properly built.");
  }
}
