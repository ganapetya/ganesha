package com.ganesha.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DemoApplication.class)
class DemoApplicationTests {
    @Autowired
	private MockMvc mockMvc;
    @Autowired
	private ObjectMapper objectMapper;

    @Test
	void helloTest() throws Exception {
		mockMvc.perform(get("/hello")
				.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	void userTest() throws Exception {
		UserResource user = new UserResource("Ganesha", "ganesh@galaxy.net");
		mockMvc.perform(post("/user", 42L)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk());
	}

	@Test
	void userTestInvalidUserObject() throws Exception {
		UserResource user = new UserResource(null, null);
		mockMvc.perform(post("/user", 42L)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isBadRequest());
	}
}
