package com.fundatechperu.shareway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Entry point of the ShareWay monolith, the baseline before the extraction to microservices. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SharewayMonolithApplication {

  public static void main(String[] args) {
    SpringApplication.run(SharewayMonolithApplication.class, args);
  }
}
