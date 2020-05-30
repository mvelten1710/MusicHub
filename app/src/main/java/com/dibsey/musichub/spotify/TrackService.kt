package com.dibsey.musichub.spotify

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest

class TrackService(queue: RequestQueue, private val spotifyService: SpotifyService) {

    private val TAG = "TrackService"
    private var mQueue: RequestQueue? = queue

    private val SEARCH_ENDPOINT = "https://api.spotify.com/v1/search"

    private var track = Message()

    fun track(): Message?{
        return track
    }

    fun searchTrack(searchTitle: String){
        val jsonObjectRequest =
            object : JsonObjectRequest(Method.GET, "$SEARCH_ENDPOINT?q=$searchTitle&type=track", null, { response ->
                if (response.getJSONObject("tracks").getJSONArray("items").length() > 0) {
                    try {
                        track.name(response.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("name"))
                        track.uri(response.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("uri"))
                        track.duration(response.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("duration_ms"))
                        track.artist(response.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name"))
                    }catch (e: IllegalStateException){
                        Log.d(TAG, "No Track Found!")
                    }
                }else{
                    Log.d(TAG, "No Track Found!")
                }
            }, {
                //TODO handle error

            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val token = spotifyService.token()
                    val auth = "Bearer $token"
                    headers["Authorization"] = auth
                    return headers
                }
            }
        mQueue?.add(jsonObjectRequest)
    }
}