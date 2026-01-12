package com.peizhe.todo.list


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String? = null,

    @SerialName("release_date")
    val releaseDate: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("first_air_date")
    val firstAirDate: String? = null,

    @SerialName("overview")
    val overview: String = "",

    @SerialName("poster_path")
    val posterPath: String? = null,

    @SerialName("backdrop_path")
    val backdropPath: String? = null,

    @SerialName("vote_average")
    val voteAverage: Double = 0.0,

    @SerialName("vote_count")
    val voteCount: Int = 0
): java.io.Serializable{
    val displayTitle: String
        get() = title ?: name ?: "Unknown"

    val displayDate: String
        get() = releaseDate ?: firstAirDate ?: ""

    val isMovie: Boolean
        get() = title != null
}
@Serializable
data class ListResponse(
    @SerialName("page")
    val page: Int,

    @SerialName("results")
    val results: List<Item>,

    @SerialName("total_pages")
    val totalPages: Int,
)

@Serializable
data class FavoriteBody(
    @SerialName("media_type") val mediaType: String, // "movie" or "tv"
    @SerialName("media_id") val mediaId: Int,
    @SerialName("favorite") val isFavorite: Boolean
)

@Serializable
data class WatchlistBody(
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_id") val mediaId: Int,
    @SerialName("watchlist") val isWatchlist: Boolean
)
@Serializable
data class RequestTokenResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("request_token") val requestToken: String
)

@Serializable
data class CreateSessionBody(
    @SerialName("request_token") val requestToken: String
)

@Serializable
data class SessionResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("session_id") val sessionId: String
)

@Serializable
data class AccountDetails(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String
)

@Serializable
data class LoginBody(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("request_token") val requestToken: String
)
@Serializable
data class RatingBody(
    @SerialName("value") val value: Double
)