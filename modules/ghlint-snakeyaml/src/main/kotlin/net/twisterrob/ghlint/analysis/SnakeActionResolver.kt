package net.twisterrob.ghlint.analysis

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.FileLocation
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

internal class SnakeActionResolver(
	private val httpClient: HttpClient = HttpClient.newHttpClient()
) {

	fun resolveAction(owner: String, repo: String, path: String?, ref: String): File {
		val directory = if (path == null) "" else "/${path}"
		val uri = URI("https://raw.githubusercontent.com/${owner}/${repo}/${ref}${directory}/action.yml")
		println(uri)
		val request = HttpRequest.newBuilder()
			.uri(uri)
			.timeout(Duration.ofSeconds(@Suppress("detekt.MagicNumber") 5))
			.GET()
			.build()
		val response = httpClient.send(request, BodyHandlers.ofString())
		if (response.statusCode() != 200) {
			throw IllegalArgumentException(
				"Failed to fetch action.yml for ${owner}/${repo}/${directory}@${ref}:\n" +
						"Status: ${response.statusCode()}\n" +
						response.body()
			)
		} else {
			val yaml = response.body()
			return File(FileLocation("github://${owner}/${repo}${directory}/action.yml"), yaml)
		}
	}
}
