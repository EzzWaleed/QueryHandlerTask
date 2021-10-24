package com.example.queryhandlertask

import android.net.Uri
import android.util.Patterns
import android.webkit.URLUtil
import java.util.*
import java.util.regex.Pattern


/**
 * QueryHandler class is responsible of parsing search queries and returns a valid destination for that search.
 * Example: input: github result: https://www.google.com/search?hl=github
 */
class QueryHandler {
    fun handleSearchQuery(searchQuery: String): Destination {
        val filteredUrl = smartUrlFilter(searchQuery)
        return if (filteredUrl != null) {
            val uri = Uri.parse(searchQuery)
            if (uri.scheme == "http" || uri.scheme == "https")
                Destination.Link(uri.toString())
            else
                Destination.DeepLink(uri)
        } else
            Destination.Link(composeSearchUrl(searchQuery))
    }

    private val acceptedUriSchema = Pattern.compile("\\w+:(/?/?)[^\\s]+")

    /**
     * Attempts to determine if user input is a URL.
     * Anything with a space is not considered a URL.
     * Converts to lowercase any mistakenly upper-cased schema
     * (i.e., "Http://" converts to "http://")
     *
     * @return Original URL, modified URL or null
     */
    private fun smartUrlFilter(potentialUrl: String): String? {
        var inUrl = potentialUrl.trim()
        val hasSpace = ' ' in inUrl
        val matcher = acceptedUriSchema.matcher(inUrl)
        if (matcher.matches()) {
            // force scheme to lowercase
            val scheme = matcher.group(1)
            val lcScheme = scheme?.toLowerCase(Locale.getDefault())
            if (lcScheme != scheme) {
                inUrl = lcScheme + matcher.group(2)
            }
            if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
                inUrl = inUrl.replace(" ", "%20")
            }
            return inUrl
        }
        if (!hasSpace) {
            if (Patterns.WEB_URL.matcher(inUrl).matches()) {
                return inUrl
            }
        }
        return null
    }

    sealed class Destination {
        data class DeepLink(val uri: Uri) : Destination()
        data class Link(val url: String) : Destination()
    }

    private val queryPlaceHolder = "%s"

    private val googleSearchUrl =
        "https://www.google.com/search?hl=${Locale.getDefault().language}&q=$queryPlaceHolder"

    private fun composeSearchUrl(query: String): String {
        return URLUtil.composeSearchUrl(query.trim(), googleSearchUrl, queryPlaceHolder) ?: ""
    }
}