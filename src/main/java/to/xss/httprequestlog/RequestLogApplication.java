/**
 * Copyright (C) 2016, Dmitry Holodov. All rights reserved.
 */
package to.xss.httprequestlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RequestLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestLogApplication.class, args);
    }
}
