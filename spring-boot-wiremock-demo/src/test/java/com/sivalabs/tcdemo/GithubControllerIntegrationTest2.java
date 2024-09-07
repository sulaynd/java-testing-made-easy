package com.sivalabs.tcdemo;


import io.restassured.RestAssured;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GithubControllerIntegrationTest2{

	@LocalServerPort
	int port;
	@Autowired
	protected MockMvc mockMvc;

	static WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.5.2-alpine");

	@BeforeAll
	static void beforeAll() {
		wiremockServer.start();
		configureFor(wiremockServer.getHost(), wiremockServer.getPort());
	}
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("github.api.base-url", wiremockServer::getBaseUrl);
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

//	@DynamicPropertySource
//	static void configureProperties(DynamicPropertyRegistry registry) {
//		registry.add("github.api.base-url", wireMockServer::baseUrl);
//	}

	@Test
	void shouldGetGithubUserProfile() throws Exception {
		String username = "sivaprasadreddy";
		//mockGetGithubUserProfile(username);
//		stubFor(WireMock.get(urlMatching("/users/.*"))
//				.willReturn(
//					aResponse()
//						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
//						.withBody("""
//						{
//							"login": "%s",
//							"name": "K. Siva Prasad Reddy",
//							"twitter_username": "sivalabs",
//							"public_repos": 50
//						}
//						""".formatted(username))));


		stubFor(WireMock.get(urlMatching("/users/.*"))
				.willReturn(aResponse()
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withStatus(200)
						.withBody(
								"""
                    {
							"login": "%s",
							"name": "K. Siva Prasad Reddy",
							"twitter_username": "sivalabs",
							"public_repos": 50
						}
						""".formatted(username))));

		this.mockMvc.perform(get("/api/users/{username}", username))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.login", is(username)))
			.andExpect(jsonPath("$.name", is("K. Siva Prasad Reddy")))
			.andExpect(jsonPath("$.public_repos", is(50)));
	}

	@Test
	void shouldGetFailureResponseWhenGitHubApiFailed() throws Exception {
		String username = "sivaprasadreddy";

		stubFor(WireMock.get(urlMatching("/users/.*"))
				.willReturn(aResponse().withStatus(500)));

		String expectedError = "Fail to fetch github profile for " + username;
		this.mockMvc.perform(get("/api/users/{username}", username))
				.andExpect(status().is5xxServerError())
				.andExpect(jsonPath("$.message", is(expectedError)));
	}
}