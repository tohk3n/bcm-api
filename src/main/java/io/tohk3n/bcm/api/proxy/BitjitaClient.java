package io.tohk3n.bcm.api.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
public class BitjitaClient {

    private static final Logger log = LoggerFactory.getLogger(BitjitaClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public BitjitaClient(
            RestClient.Builder builder,
            @Value("${bcm.bitjita.base-url}") String baseUrl
    ) {
        this.restClient = builder.build();
        this.baseUrl = baseUrl;
    }

    // URI.create() instead of template strings — RestClient will try to parse
    // query params as URI templates otherwise. Ask me how I know.
    public ResponseEntity<String> fetch(String path) {
        log.debug("Fetching upstream: {}", path);

        return restClient.get()
                .uri(URI.create(baseUrl + path))
                .retrieve()
                .toEntity(String.class);
    }
}