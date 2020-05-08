package com.wxf.distribution.trace.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributionTraceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributionTraceServerApplication.class, args);
		CenterServer.inst().start();
	}

}
