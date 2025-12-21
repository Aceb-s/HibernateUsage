package org.example.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DiscoveryApplication.class);

        app.setDefaultProperties(java.util.Collections.singletonMap("server.port", "8761"));

        app.run(args);
    }
}
