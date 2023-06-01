import { body, DateTime, defaultResponse, endpoint, Integer, queryParams, request, response, String } from "@airtasker/spot";
import { ChartSample, Sample, TimeSeries } from "./results";

@endpoint({
  method: 'GET',
  path: '/demo/go-threads',
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
  path: '/demo/go-threads/range',
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
  path: '/demo/go-threads/range/chart',
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

@endpoint({
  method: 'GET',
  path: '/demo/go-threads/series',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetSeries {
  @request
  request(
    @queryParams
    queryParams: {
      start?: DateTime
      end?: DateTime
    }
  ) {}
  @response({ status: 200 })
  successResponse(
    @body
    // SPOT is so dumb it can't even support one of most common API practice:
    //    https://swagger.io/docs/specification/data-models/data-types/#object (Free-Form Object)
    // body: Object[]
    body: String[]
  ) { }
}

@endpoint({
  method: 'GET',
  path: '/demo/go-threads/labels',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetLabels {
  @request
  request(
    @queryParams
    queryParams: {
      start?: DateTime
      end?: DateTime
    }
  ) {}
  @response({ status: 200 })
  successResponse(
    @body
    body: String[]
  ) { }
}

@endpoint({
  method: 'GET',
  path: '/demo/go-threads/label-values',
  tags: ['Prom4j'],
  server: 'Prom4j'
})
class GetLabelValues {
  @request
  request(
    @queryParams
    queryParams: {
      start?: DateTime
      end?: DateTime
    }
  ) {}
  @response({ status: 200 })
  successResponse(
    @body
    body: String[]
  ) { }
}
