package io.zahori.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.consul.discovery.ReregistrationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class TaskSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskSchedulerApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*
        Bean needed for reregistration if consul is restarted
        Also need to set the following properties in application.properties
        - spring.cloud.consul.discovery.heartbeat.enabled= true
        - spring.cloud.consul.discovery.heartbeat.reregister-service-on-failure=true
        See: https://github.com/spring-cloud/spring-cloud-consul/issues/727
     */
    @Bean
    public ReregistrationPredicate reRegistrationPredicate() {
        return e -> e.getStatusCode() >= 400;
    }

}
