package cc.sovellus.vrcaa.api.models


import com.google.gson.annotations.SerializedName

class LimitedWorlds : ArrayList<LimitedWorlds.LimitedWorldItem>(){
    data class LimitedWorldItem(
        @SerializedName("authorId")
        val authorId: String,
        @SerializedName("authorName")
        val authorName: String,
        @SerializedName("capacity")
        val capacity: Int,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("favorites")
        val favorites: Int,
        @SerializedName("heat")
        val heat: Int,
        @SerializedName("id")
        val id: String,
        @SerializedName("imageUrl")
        val imageUrl: String,
        @SerializedName("labsPublicationDate")
        val labsPublicationDate: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("occupants")
        val occupants: Int,
        @SerializedName("organization")
        val organization: String,
        @SerializedName("popularity")
        val popularity: Int,
        @SerializedName("publicationDate")
        val publicationDate: String,
        @SerializedName("recommendedCapacity")
        val recommendedCapacity: Int,
        @SerializedName("releaseStatus")
        val releaseStatus: String,
        @SerializedName("tags")
        val tags: List<String>,
        @SerializedName("thumbnailImageUrl")
        val thumbnailImageUrl: String,
        @SerializedName("udonProducts")
        val udonProducts: List<String>,
        @SerializedName("unityPackages")
        val unityPackages: List<UnityPackage>,
        @SerializedName("updated_at")
        val updatedAt: String
    ) {
        data class UnityPackage(
            @SerializedName("platform")
            val platform: String,
            @SerializedName("unityVersion")
            val unityVersion: String
        )
    }
}