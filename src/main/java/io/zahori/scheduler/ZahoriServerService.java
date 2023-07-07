package io.zahori.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZahoriServerService {

    private static final Logger LOG = LoggerFactory.getLogger(ZahoriServerService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${zahori.server.url:}")
    private String zahoriServerUrl;

    @Value("${zahori.server.healthcheck:}")
    private String healthcheckUrl;

    @Value("${zahori.server.connection-max-retries:}")
    private int connectionMaxRetries;

    @Value("${zahori.server.connection-wait-seconds:}")
    private int connectionWaitSeconds;

    public void loadTasks() {
        LOG.info("Start loading tasks from zahori server: {}", zahoriServerUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(zahoriServerUrl + "/scheduled/executions/load", String.class);
        LOG.info("End loading tasks from zahori server --> {}", response.getStatusCode());
    }

    public void runTask(Task task) {
        ResponseEntity<String> response = this.restTemplate.exchange(zahoriServerUrl + "/execution/" + task.getUuid() + "/run",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {
        });
    }

    public void waitZahoriServerHealthcheck() {
        LOG.info("Zahori server url: {}", zahoriServerUrl);
        LOG.info("Zahori server healthcheck: {}{}", zahoriServerUrl, healthcheckUrl);
        
        for (int i = 1; i <= connectionMaxRetries; i++) {

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(zahoriServerUrl + healthcheckUrl, String.class
                );
                LOG.info("Zahori server status: {}", response.getStatusCode());
                if (response.getStatusCode().is2xxSuccessful()) {
                    return;
                }
            } catch (Exception e) {
                LOG.warn(
                        "Zahori server is unreachable: it may be down, still starting or there is no network connectivity: {}", e.getMessage());
            }

            LOG.warn("Waiting " + connectionWaitSeconds + " seconds before retrying again...");
            pause(connectionWaitSeconds);

            if (i >= connectionMaxRetries) {
                String errorMessage = "Timeout waiting for Zahori server: it seems to be down or there is no network connectivity";
                LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }
    }

    private void pause(int seconds) {
        try {
            Thread.sleep((seconds * 1000));
        } catch (InterruptedException e) {
            LOG.error("Error while pausing: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
