package com.chung.fluidnotes.utils

object AppConfig {
    // Change these when you switch from the test repo to the real one
    const val GITHUB_OWNER = "Chung1045"
    const val GITHUB_REPO = "FluidNotes"

    const val REPO_URL =
        "https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}"

    const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
}