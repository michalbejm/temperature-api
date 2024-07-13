package com.weather.temperature.service.impl;

import com.google.cloud.ReadChannel;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.weather.temperature.service.YearlyTemperatureService;
import com.weather.temperature.service.config.GcpConfig;
import com.weather.temperature.service.dto.CityWithYear;
import com.weather.temperature.service.dto.YearlyTemperatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TemperatureUpdateServiceImpl {

    private final YearlyTemperatureService yearlyTemperatureService;
    private final GcpConfig gcpConfig;
    private final Storage storage;
    private final ReentrantLock lock;

    private final Logger logger = LoggerFactory.getLogger(TemperatureUpdateServiceImpl.class);

    @Autowired
    public TemperatureUpdateServiceImpl(YearlyTemperatureService yearlyTemperatureService, GcpConfig gcpConfig) {
        this(yearlyTemperatureService, gcpConfig, StorageOptions.getDefaultInstance().getService());
    }

    TemperatureUpdateServiceImpl(YearlyTemperatureService yearlyTemperatureService, GcpConfig gcpConfig, Storage storage) {
        this.yearlyTemperatureService = yearlyTemperatureService;
        this.gcpConfig = gcpConfig;
        this.storage = storage;
        this.lock = new ReentrantLock();
    }

    @PostConstruct
    void init() {
        processFile();
        startPubSubListener();
    }

    private void startPubSubListener() {

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
                gcpConfig.getProjectId(), gcpConfig.getPubsubSubscription());

        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            processFile();
            consumer.ack();
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
    }

    private void processFile() {
        // We use a simple locking to make sure that the same file is not processed twice in parallel,
        // we can do it this way since the Service will be singleton. In real live scenario it would make
        // more sense to use a distributed lock (e.g. using Redis).
        if (!lock.tryLock()) {
            return;
        }

        try {
            Map<CityWithYear, YearlyTemperatureData> temperatureData = new HashMap<>();

            logger.info("Start processing file: " + gcpConfig.getFileName());

            Blob blob = storage.get(gcpConfig.getBucketName(), gcpConfig.getFileName());
            try (ReadChannel channel = blob.reader()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

                byte[] bytes;
                while (channel.read(buffer) > 0) {
                    buffer.flip();
                    bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    buffer.clear();

                    try (BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(new ByteArrayInputStream(bytes)))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            processLine(temperatureData, line);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            yearlyTemperatureService.createYearlyTemperatures(temperatureData);

        } finally {
            lock.unlock();
        }
    }

    private void processLine(Map<CityWithYear, YearlyTemperatureData> temperatureData, String line) {
        String[] fields = line.split(";");
        if (fields.length != 3) {
            return;
        }

        String city = fields[0];
        int year;
        BigDecimal temperature;
        try {
            year = Integer.parseInt(fields[1].substring(0, 4));
            temperature = new BigDecimal(fields[2]);
        }
        catch (NumberFormatException | IndexOutOfBoundsException exc) {
            return;
        }

        CityWithYear cityWithYear = new CityWithYear(city, year);
        if (temperatureData.containsKey(cityWithYear)) {
            temperatureData.get(cityWithYear).addTemperature(temperature);
        } else {
            temperatureData.put(cityWithYear, new YearlyTemperatureData(temperature));
        }
    }
}
