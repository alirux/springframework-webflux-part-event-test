package partevent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;

class UploadTest {

	@Test
	void test_mock() {
		WebTestClient client =	WebTestClient.bindToController(new UploadStream()).build();
		
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
//		builder.part("fieldPart", "fieldValue");
		builder.part("filePart1", new ClassPathResource("/numbers.log"));
//		builder.part("jsonPart", new Person("Jason"));
//		builder.part("myPart", part); // Part from a server request

		MultiValueMap<String, HttpEntity<?>> parts = builder.build();
		
		client.post().uri("/stream").bodyValue(parts)
		.header("content-type", MediaType.MULTIPART_FORM_DATA.toString())
		.exchange()
		.expectStatus().isOk()
		.returnResult(String.class) // Declare that you want to handle the result
        .getResponseBody()        // Get the response body Flux
        .blockLast();             // Wait until the response stream completes;
	}

	@Test
	void test_live() {
		WebTestClient client =
				WebTestClient.bindToServer().baseUrl("http://localhost:8080")
				// Adds content-length header to the file part
				.filter(new MultipartExchangeFilterFunction())
				.build();
		
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		ClassPathResource res = new ClassPathResource("/numbers_1.log");
//		ClassPathResource res = new ClassPathResource("/minimal.pdf");
		builder.part("filePart1", res)
//		.header("content-type", MediaType.MULTIPART_FORM_DATA.toString())
		;
		
		MultiValueMap<String, HttpEntity<?>> parts = builder.build();
		
		client.post().uri("/stream").bodyValue(parts)
		.header("content-type", MediaType.MULTIPART_FORM_DATA.toString())
		.exchange()
		.expectStatus().isOk()
		.returnResult(String.class) // Declare that you want to handle the result
        .getResponseBody()        // Get the response body Flux
        .blockLast();             // Wait until the response stream completes;
	}
	
}
