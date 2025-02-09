package cc.sovellus.vrcaa.api.models.pipeline


import com.google.gson.annotations.SerializedName

data class FriendOnline(
    @SerializedName("canRequestInvite")
    val canRequestInvite: Boolean,
    @SerializedName("location")
    val location: String,
    @SerializedName("travelingToLocation")
    val travelingToLocation: String,
    @SerializedName("user")
    val user: User,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("worldId")
    val worldId: String
) {
    data class User(
        @SerializedName("allowAvatarCopying")
        val allowAvatarCopying: Boolean,
        @SerializedName("bio")
        val bio: String,
        @SerializedName("bioLinks")
        val bioLinks: List<String>,
        @SerializedName("currentAvatarImageUrl")
        val currentAvatarImageUrl: String,
        @SerializedName("currentAvatarTags")
        val currentAvatarTags: List<Any>,
        @SerializedName("currentAvatarThumbnailImageUrl")
        val currentAvatarThumbnailImageUrl: String,
        @SerializedName("date_joined")
        val dateJoined: String,
        @SerializedName("developerType")
        val developerType: String,
        @SerializedName("displayName")
        val displayName: String,
        @SerializedName("friendKey")
        val friendKey: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("isFriend")
        val isFriend: Boolean,
        @SerializedName("last_activity")
        val lastActivity: String,
        @SerializedName("last_login")
        val lastLogin: String,
        @SerializedName("last_platform")
        val lastPlatform: String,
        @SerializedName("profilePicOverride")
        val profilePicOverride: String,
        @SerializedName("state")
        val state: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("statusDescription")
        val statusDescription: String,
        @SerializedName("tags")
        val tags: List<String>,
        @SerializedName("userIcon")
        val userIcon: String
    )
}