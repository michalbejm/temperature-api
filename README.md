# Temperature API - Spring Boot and Google Cloud Integration

## Problem

The application should expose an endpoint which returns the yearly average temperatures for a given city. The data should be populated from a CSV file, and it's content may change while the application is running.

### Input file format

The CSV file containing temperature measurements has the following format `city;yyyy-mm-dd HH:mm:ss.SSS;temp`

```csv
Warsaw;2018-10-09 22:22:12.731;22.89
Warsaw;2018-10-10 06:09:21.884;26.66
Warsaw;2018-10-11 01:27:26.177;28.13
Warsaw;2018-10-12 11:58:49.730;19.08
Warsaw;2018-10-13 06:26:59.642;29.81
```

### Endpoint URL

<http://localhost:8080/yearly-temperatures?city=Warsaw>

### Response example

```json
[
  {
	"year": "2018",
	"averageTemperature": 12.1
  },
  {
	"year": "2019",
	"averageTemperature": 11.1
  }
]
```

## Solution

In order to fulfil the above requirements a *Spring Boot* application integrated with *Google Cloud Platform* was implemented. The CSV file containing temperature measurements is stored in a *Google Cloud Storage*. The application implements a listener to receive messages from the *Google Pub/Sub* subscription when the content of the file changes.

When the application starts, the file is being processed and the necessary data is being stored in the database. Afterward, whenever the input file changes, the data in the database will be updated.

### GCP Configuration

To configure the *GCP* services the following steps should be performed:

1. Create a *Google Cloud Storage* bucket.
2. Create a *Pub/Sub* Topic.
3. Create a *Pub/Sub* Subscription.
4. Add a *Google Cloud Storage* notification for object changes to the Pub/Sub topic: `gcloud storage buckets notifications create gs://BUCKET_NAME --topic=TOPIC_NAME`.

Afterward, the `project id`, `subscription id`, `bucket name`, `file name` should be stored as the environmental variables since the application will use these values.

### Considerations

Please note that the best possible solution may depend on the several factors. For instance:

- How big the input file will be?
- How frequently the input file will be updated?
- How the input file will be updated?
    - Will the whole file will be updated?
    - Will the new lines be appended to the existing file?
- Should the file be processed automatically?
- Can the endpoint provide not up-to-date values (eventual consistency)?
- How the errors should be handled?

### Solutions (Branches)

Depending on the answers to the above questions the three solutions (on three different branches) have been prepared:

1. **master** branch contains the most basic solution. The input file is processed at once and the results are being saved into the database. It contains no error handling whatsoever. Therefore, this solution should not be used in case the big input files are expected.
2. **save_file_processing_state** branch is more suitable for big input files. It assumes that the whole file content can be changed. The application persists the progress every given number of lines (the default value is 1,000,000 lines). In case an error occurs or the application is restarted, the application should process the file where it stopped. The application is also aware of the input's file version (generation) and in case a new file is available it should be able to process the new version instead of and old one. Moreover, a retry mechanism was implemented, in case of any error.
3. **append_new_lines** branch is a simplified version of the previous branch. It assumes the existing file's content will not be changed and the new lines will be appended to the end of the file. The application will store the position where the processing has stopped and in case of a new data it should continue from that position. It will not store the average temperature per city/year but the total temperature with the items count. The average temperature will be calculated while calling the endpoint. Saving the progress and retry mechanism from the previous version was also included here.

### Implementation details

In all the branches processing of an input file was implemented in the `TemperatureUpdateServiceImpl` class. The system collects and persists the data from an input file in the `processFile` method which at first called when the application is started and afterward after getting a message from the *Google Pub/Sub* subscription. The configuration can be obtained from the `GcpConfig` configuration class.

The endpoint for getting the average temperatures is defined in the `YearlyTemperatureController` class.

To run the application please run `TemperatureApplication` class. Before running please ensure that the *Google Cloud* credentials are correctly configured and the corresponding environmental variables were set.