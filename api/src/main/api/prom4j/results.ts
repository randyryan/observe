import { DateTime, Integer, String } from "@airtasker/spot";

/**
 * A date and value pair representing a single sample in a time-series data.
 */
interface Sample {
  date: DateTime;
  value: Integer;
}

/**
 * A time-series representation of the requested iCluster Monitoring inforamtion.
 */
interface TimeSeries {
  samples: Sample[];
}

/**
 * A combination of TimeSeries and Sample type for charting.
 */
interface ChartSample extends Sample {
  /**
   * The name of the group used to identify the time-series of this sample.
   */
  group: String;
}

export { Sample, TimeSeries, ChartSample };
