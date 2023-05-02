import { body, endpoint, response } from "@airtasker/spot";
import { ChartSample, Sample, TimeSeries } from "./results";

@endpoint({
  method: 'GET',
  path: '/prom4j/go-threads',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetGoThreads {
  @response({ status: 200 })
  successResponse(
    @body
    body: Sample
  ) { }
}

@endpoint({
  method: 'GET',
  path: '/prom4j/go-threads/chart',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetGoThreadsChart {
  @response({ status: 200 })
  successResponse(
    @body
    body: ChartSample[]
  ) { }
}
