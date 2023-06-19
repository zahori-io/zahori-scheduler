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

    public static final String HEALTHCHECK_URL = "/healthcheck";
    public static final int SECONDS_WAIT_FOR_SERVER = 5;
    public static final int MAX_RETRIES_WAIT_FOR_SERVER = 10;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${zahori.server.url:}")
    private String zahoriServerUrl;

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
        for (int i = 1; i <= MAX_RETRIES_WAIT_FOR_SERVER; i++) {

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(zahoriServerUrl + HEALTHCHECK_URL, String.class
                );
                LOG.info("Zahori server status: {}", response.getStatusCode());
                if (response.getStatusCode().is2xxSuccessful()) {
                    return;
                }
            } catch (Exception e) {
                LOG.warn(
                        "Zahori server is unreachable: it may be down, still starting or there is no network connectivity");
            }

            LOG.warn("Waiting " + SECONDS_WAIT_FOR_SERVER + " seconds before retrying again...");
            pause(SECONDS_WAIT_FOR_SERVER);

            if (i >= MAX_RETRIES_WAIT_FOR_SERVER) {
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
