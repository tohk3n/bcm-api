package io.tohk3n.bcm.api.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private final PathValidator pathValidator;
    private final BitjitaClient bitjitaClient;
    private final String cacheControl;

    public ProxyController(
            PathValidator pathValidator,
            BitjitaClient bitjitaClient,
            @Value("${bcm.bitjita.cache-max-age}") int cacheMaxAge
    ) {
        this.pathValidator = pathValidator;
        this.bitjitaClient = bitjitaClient;
        this.cacheControl = "s-maxage=" + cacheMaxAge;
    }

    @GetMapping("/api/proxy")
    public ResponseEntity<String> proxy(@RequestParam(required = false) String path) {
        if (path == null || path.isBlank()) {
            return badRequest("Missing path parameter");
        }

        if (!pathValidator.isAllowed(path)) {
            log.warn("Blocked disallowed path: {}", path);
            return forbidden("Path not allowed");
        }

        try {
            ResponseEntity<String> upstream = bitjitaClient.fetch(path);

            return ResponseEntity
                    .status(upstream.getStatusCode())
                    .header(HttpHeaders.CACHE_CONTROL, cacheControl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(upstream.getBody());

        } catch (RestClientException e) {
            log.error("Upstream request failed for path: {}", path, e);
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Upstream request failed\"}");
        }
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"" + message + "\"}");
    }

    private ResponseEntity<String> forbidden(String message) {
        return ResponseEntity.status(403)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"" + message + "\"}");
    }
}