package com.ganesha.demo;

import com.google.protobuf.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;

import java.io.IOException;

@SpringBootApplication
@RestController
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}


	@PostMapping("/user")
	public String sendUser(@Valid @RequestBody UserResource userResource) {
		return String.format("Hello %s!", userResource.getName());
	}

	@GetMapping("/getPods")
	public String getPods() throws IOException, ApiException {
		ApiClient client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
        StringBuilder sb = new StringBuilder();
		CoreV1Api api = new CoreV1Api();
		V1PodList list = api.listNamespacedPod("default",null,null,null,null,null,null,null,null,null);
		for (V1Pod item : list.getItems()) {
			sb.append(item.getMetadata().getName());
		}
		return String.format("Hello %s!", sb.toString());
	}

}
