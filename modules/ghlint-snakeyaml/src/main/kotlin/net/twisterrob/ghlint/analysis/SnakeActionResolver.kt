package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

internal class SnakeActionResolver {

	fun resolveAction(owner: String, repo: String, path: String?, ref: String): File {
		val directory = if (path == null) "" else "/${path}"
		val request = HttpRequest.newBuilder()
			.uri(URI("https://raw.githubusercontent.com/${owner}/${repo}/${ref}${directory}/action.yml"))
			.timeout(Duration.ofSeconds(@Suppress("detekt.MagicNumber") 5))
			.GET()
			.build()
		val yaml = HttpClient.newHttpClient().send(request, BodyHandlers.ofString()).body()
		return File(FileLocation("github://${owner}/${repo}${directory}/action.yml"), yaml)
	}
}
