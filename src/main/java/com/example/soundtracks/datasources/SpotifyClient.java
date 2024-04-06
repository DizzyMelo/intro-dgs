package com.example.soundtracks.datasources;

import com.example.soundtracks.models.MappedPlaylist;
import com.example.soundtracks.models.PlaylistCollection;
import com.example.soundtracks.models.Snapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SpotifyClient {
    private static final String spotifyApiUrl = "https://spotify-demo-api-fe224840a08c.herokuapp.com/v1";
    private final RestClient client = RestClient.builder().baseUrl(spotifyApiUrl).build();

    public PlaylistCollection featuredPlaylistsRequest() {
        return client
                .get()
                .uri("/browse/featured-playlists")
                .retrieve()
                .body(PlaylistCollection.class);
    }

    public MappedPlaylist playlistRequest(String id) {
        return client
                .get()
                .uri("/playlists/{playlist_id}", id)
                .retrieve()
                .body(MappedPlaylist.class);
    }

    public Snapshot addItemsToPlaylist(String id, String uris) {
        return client
                .post()
                .uri(uriBuilder ->
                        uriBuilder.path("/playlists/{playlist_id}/tracks")
                                .queryParam("uris", uris)
                                .build(id))
                .retrieve()
                .body(Snapshot.class);
    }

}
