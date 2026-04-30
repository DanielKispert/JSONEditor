package com.daniel.jsoneditor.controller.settings;


/**
 * Result of checking for application updates against GitHub releases.
 *
 * @param updateAvailable whether a newer version exists
 * @param latestVersion the latest version string (e.g. "0.18.0"), or null on error
 */
public record UpdateCheckResult(boolean updateAvailable, String latestVersion)
{
}


