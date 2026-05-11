package com.daniel.jsoneditor.controller.settings;

import com.daniel.jsoneditor.util.VersionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;


/**
 * Checks GitHub Releases API for newer versions of the application.
 * Runs the check asynchronously so it never blocks the UI thread.
 */
public final class UpdateService
{
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);
    
    private static final String GITHUB_OWNER = "DanielKispert";
    private static final String GITHUB_REPO = "JSONEditor";
    private static final String RELEASES_URL =
            "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static final int MAX_RETRIES = 1;
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    
    private UpdateService()
    {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Asynchronously checks whether a newer release exists on GitHub.
     * Calls the callback on the calling thread pool – caller is responsible for switching to the FX thread if needed.
     *
     * @param callback receives the result; never null, but fields may be null on network errors
     */
    public static void checkForUpdateAsync(final Consumer<UpdateCheckResult> callback)
    {
        final Thread thread = new Thread(() ->
        {
            try
            {
                final UpdateCheckResult result = checkForUpdate();
                callback.accept(result);
            }
            catch (Exception ex)
            {
                logger.warn("Update check failed", ex);
                callback.accept(new UpdateCheckResult(false, null));
            }
        }, "update-checker");
        thread.setDaemon(true);
        thread.start();
    }
    
    private static UpdateCheckResult checkForUpdate()
    {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RELEASES_URL))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "JSONEditor-UpdateCheck")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++)
        {
            try
            {
                final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                final int status = response.statusCode();
                
                if (status == 200)
                {
                    return parseResponse(response.body());
                }
                // don't retry client errors (403 rate-limit, 404 no releases)
                if (status >= 400 && status < 500)
                {
                    logger.warn("GitHub API returned client error {}, not retrying", status);
                    return new UpdateCheckResult(false, null);
                }
                logger.warn("GitHub API returned status {}, attempt {}/{}", status, attempt + 1, MAX_RETRIES + 1);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return new UpdateCheckResult(false, null);
            }
            catch (Exception e)
            {
                logger.warn("Update check attempt {}/{} failed", attempt + 1, MAX_RETRIES + 1, e);
            }
        }
        return new UpdateCheckResult(false, null);
    }
    
    private static UpdateCheckResult parseResponse(final String body)
    {
        try
        {
            final JsonNode root = MAPPER.readTree(body);
            final String tagName = root.path("tag_name").asText("");
            
            final String latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;
            final String currentVersion = VersionUtil.getVersion();
            
            final boolean newer = isNewer(latestVersion, currentVersion);
            logger.info("Update check: current={}, latest={}, updateAvailable={}", currentVersion, latestVersion, newer);
            
            return new UpdateCheckResult(newer, latestVersion);
        }
        catch (Exception e)
        {
            logger.warn("Failed to parse GitHub release response", e);
            return new UpdateCheckResult(false, null);
        }
    }
    
    /**
     * Simple semver comparison (major.minor.patch). Returns true if latest > current.
     */
    static boolean isNewer(final String latest, final String current)
    {
        if (latest == null || latest.isBlank() || current == null || "unknown".equals(current))
        {
            return false;
        }
        try
        {
            final int[] l = parseSemver(latest);
            final int[] c = parseSemver(current);
            for (int i = 0; i < 3; i++)
            {
                if (l[i] > c[i])
                {
                    return true;
                }
                if (l[i] < c[i])
                {
                    return false;
                }
            }
            return false;
        }
        catch (NumberFormatException e)
        {
            logger.warn("Could not parse version strings: latest={}, current={}", latest, current);
            return false;
        }
    }
    
    private static int[] parseSemver(final String version)
    {
        // Strip pre-release suffix (e.g., "1.0.0-beta" → "1.0.0")
        final String cleanVersion = version.contains("-") ? version.substring(0, version.indexOf('-')) : version;
        final String[] parts = cleanVersion.split("\\.");
        final int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++)
        {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }


}
