package com.wxf.distribution.trace.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributionTraceClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributionTraceClientApplication.class, args);
		TraceClient traceClient =new TraceClient("127.0.0.1",8084);
		for (int i=0;i<1000;i++){
			try {
				traceClient.send(i+"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
