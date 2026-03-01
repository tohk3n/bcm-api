package io.tohk3n.bcm.api.proxy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PathValidator {

    // Mirror of the frontend allowlist. If you add a route here, you're exposing it to the internet.
    private static final List<Pattern> ALLOWED_PATHS = List.of(
            Pattern.compile("^/claims$"),
            Pattern.compile("^/claims/\\d+$"),
            Pattern.compile("^/claims/\\d+/citizens$"),
            Pattern.compile("^/claims/\\d+/inventories$"),
            Pattern.compile("^/claims/\\d+/buildings$"),
            Pattern.compile("^/claims/\\d+/members$"),
            Pattern.compile("^/players/\\d+/equipment$"),
            Pattern.compile("^/players/\\d+/inventories$"),
            Pattern.compile("^/players/\\d+/vault$"),
            Pattern.compile("^/crafts$"),
            Pattern.compile("^/items$"),
            Pattern.compile("^/items/\\d+$"),
            Pattern.compile("^/buildings$"),
            Pattern.compile("^/buildings/\\d+$")
    );

    public boolean isAllowed(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        // Match against path only — query params are the caller's problem
        String pathOnly = stripQueryString(path);
        return ALLOWED_PATHS.stream().anyMatch(p -> p.matcher(pathOnly).matches());
    }

    private String stripQueryString(String path) {
        int queryStart = path.indexOf('?');
        return queryStart >= 0 ? path.substring(0, queryStart) : path;
    }
}