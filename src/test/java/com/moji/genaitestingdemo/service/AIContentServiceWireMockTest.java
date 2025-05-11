package com.moji.genaitestingdemo.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.moji.genaitestingdemo.config.WireMockConfig;
import com.theokanning.openai.client.OpenAiApi; // Import OpenAiApi
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient; // Import OkHttpClient
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// Remove @MockBean for OpenAiService if you are providing a real one pointing to WireMock
// import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import retrofit2.Retrofit; // Import Retrofit

import java.time.Duration; // Import Duration

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat; // For assertions

@SpringBootTest
@Import(WireMockConfig.class)
@ActiveProfiles("test")
public class AIContentServiceWireMockTest {

    @Autowired
    private WireMockServer wireMockServer;

    // If you are testing the actual HTTP call to WireMock,
    // you should not @MockBean OpenAiService. Instead, you'll construct one.
    // @MockBean
    // private OpenAiService openAiService;

    private AIContentService aiContentService;
    private OpenAiService customOpenAiService; // To be configured

    @BeforeEach
    void setUp() {
        // Configure WireMock client to point to the running server
        configureFor("localhost", wireMockServer.port());

        // Create an OkHttpClient (you can customize timeout)
        OkHttpClient client = OpenAiService.defaultClient("test-api-key", Duration.ofSeconds(30))
                .newBuilder()
                // Add any specific client configurations here if needed
                .build();

        // Create a Retrofit instance pointing to the WireMock server
        Retrofit retrofit = OpenAiService.defaultRetrofit(client, OpenAiService.defaultObjectMapper())
                .newBuilder()
                .baseUrl(wireMockServer.baseUrl() + "/") // IMPORTANT: Point to WireMock
                .build();

        // Create the OpenAiApi interface with Retrofit
        OpenAiApi openAiApi = retrofit.create(OpenAiApi.class);

        // Create the OpenAiService instance using the custom OpenAiApi
        customOpenAiService = new OpenAiService(openAiApi);

        // Now inject this customOpenAiService into your aiContentService
        // Assuming AIContentService has a constructor or setter for OpenAiService
        aiContentService = new AIContentService(customOpenAiService);

        // Set other properties for aiContentService as before
        ReflectionTestUtils.setField(aiContentService, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(aiContentService, "temperature", 0.7);
        ReflectionTestUtils.setField(aiContentService, "maxTokens", 500);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll(); // Reset WireMock stubs after each test
    }

    @Test
    void testContentGenerationWithMockedOpenAI() {
        // Arrange: Stub the OpenAI API endpoint on WireMock
        String expectedResponseContent = "Mocked AI response for testing purposes";
        stubFor(post(urlPathEqualTo("/v1/chat/completions")) // Use urlPathEqualTo for exact match
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\n" +
                                "  \"id\": \"chatcmpl-123\",\n" +
                                "  \"object\": \"chat.completion\",\n" +
                                "  \"created\": 1677652288,\n" +
                                "  \"model\": \"gpt-3.5-turbo\",\n" + // Added model to match typical response
                                "  \"choices\": [{\n" +
                                "    \"index\": 0,\n" +
                                "    \"message\": {\n" +
                                "      \"role\": \"assistant\",\n" +
                                "      \"content\": \"" + expectedResponseContent + "\"\n" +
                                "    },\n" +
                                "    \"finish_reason\": \"stop\"\n" +
                                "  }],\n" +
                                "  \"usage\": {\n" +
                                "    \"prompt_tokens\": 13,\n" +
                                "    \"completion_tokens\": 7,\n" +
                                "    \"total_tokens\": 20\n" +
                                "  }\n" +
                                "}")));

        // Act: Call your service method that uses OpenAiService
        String prompt = "Tell me a joke";
        String actualContent = aiContentService.generateContent(prompt);

        // Assert: Verify the content returned by your service
        assertThat(actualContent).isEqualTo(expectedResponseContent);

        // Verify that the request was made to WireMock as expected
//        verify(postRequestedFor(urlPathEqualTo("/v1/chat/completions"))
//                .withRequestBody(containing("\"prompt\":\"" + prompt + "\"")) // Example: check if prompt is in body
//                .withHeader("Authorization", equalTo("Bearer test-api-key")));
    }
}