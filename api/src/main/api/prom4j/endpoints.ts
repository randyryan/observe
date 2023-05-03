import { body, DateTime, endpoint, Integer, queryParams, request, response } from "@airtasker/spot";
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
  path: '/prom4j/go-threads/range',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetGoThreadsRange {
  @request
  request(
    @queryParams
    queryParams: {
      start?: DateTime
      end?: DateTime
      step?: Integer
    }
  ) {}
  @response({ status: 200 })
  successResponse(
    @body
    body: TimeSeries[]
  ) { }
}

@endpoint({
  method: 'GET',
  path: '/prom4j/go-threads/range/chart',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetGoThreadsRangeChart {
  @request
  request(
    @queryParams
    queryParams: {
      start?: DateTime
      end?: DateTime
      step?: Integer
    }
  ) {}
  @response({ status: 200 })
  successResponse(
    @body
    body: ChartSample[]
  ) { }
}
