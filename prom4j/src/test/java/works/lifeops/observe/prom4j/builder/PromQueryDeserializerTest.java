package works.lifeops.observe.prom4j.builder;

import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PromQueryDeserializerTest {

  private ObjectMapper objectMapper;

  private final String json = "{\"status\":\"success\",\"data\":{\"resultType\":\"vector\",\"result\":[{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNDLO\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNLARGE\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"HADJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNLARGE\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNSQL\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNSQLHADR\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNTEST\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"HADJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"GNTEST\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"IC21617\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73B\",\"group\":\"IC21617HA\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73C\",\"group\":\"GNDLO\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73C\",\"group\":\"GNSQLHADR\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73C\",\"group\":\"GNTEST\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"HADJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73C\",\"group\":\"GNTEST\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]},{\"metric\":{\"__name__\":\"icluster_monitor_transactions_todotrans_gauge\",\"application\":\"icw-agent\",\"backup\":\"ICTST73C\",\"group\":\"IC21617HA\",\"hadr\":\"ICTST73C\",\"instance\":\"10.112.103.18:8086\",\"job\":\"eureka\",\"journal\":\"QAUDJRN\",\"node\":\"ICTST73A\",\"primary\":\"ICTST73A\"},\"value\":[1.681353179312E9,\"0\"]}]}}";

  @BeforeEach
  public void setUpClass() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(PromQueryResponse.class, new PromQueryDeserializer());

    objectMapper = JsonMapper.builder()
        .addModule(module)
        .build();
  }

  @Test
  public void testDefaultDeserialization() throws JsonMappingException, JsonProcessingException {
    PromQueryResponse response = objectMapper.readValue(json, PromQueryResponse.class);

    PromQueryResponse.Status status = PromQueryResponse.Status.SUCCESS;
    PromQueryResponse.ResultType resultType = PromQueryResponse.ResultType.VECTOR;
    Map<String, String> metric = Maps.newHashMap();
    metric.put("__name__", "icluster_monitor_transactions_todotrans_gauge");
    metric.put("application:", "icw-agent");
    metric.put("backup", "ICTST73B");
    metric.put("group", "GNDLO");
    metric.put("hadr", "ICTST73C");
    metric.put("instance", "10.112.103.18:8086");
    metric.put("job", "eureka");
    metric.put("journal", "QAUDJRN");
    metric.put("node", "ICTST73A");
    metric.put("primary", "ICTST73A");
    List<Object> value = Lists.newArrayList();
    value.add(1.681353179312E9);
    value.add("0");
    PromQueryResponse.VectorResult resultItem = new PromQueryResponse.VectorResult(metric, value);

    System.out.println("expected: " + resultItem.toString());
    System.out.println("actual:   " + response.getData().getResult().get(0).toString());

    Assert.assertEquals("response.status is properly deserialized",
        status, response.getStatus());
    Assert.assertEquals("response.data.resultType is properly deserialized",
        resultType, response.getData().getResultType());
    // rough comparison
    Assert.assertEquals("response.data.result list is properly deserialized",
        14, response.getData().getResult().size());
    // rough comparison
    Assert.assertEquals("response.data.result item is properly deserialized",
        resultItem.getMetric().size(), response.getData().getResult().get(0).getMetric().size());
    // rough comparison
    Assert.assertEquals("response.data.result item is properly deserialized",
        resultItem.getValue().size(), response.getData().getResult().get(0).getValue().size());
  }

}
