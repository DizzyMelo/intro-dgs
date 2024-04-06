package com.example.soundtracks.datafetchers;

import com.example.soundtracks.datasources.SpotifyClient;
import com.example.soundtracks.generated.types.AddItemsToPlaylistInput;
import com.example.soundtracks.generated.types.AddItemsToPlaylistResponse;
import com.example.soundtracks.generated.types.Playlist;
import com.example.soundtracks.models.MappedPlaylist;
import com.example.soundtracks.models.Snapshot;
import com.netflix.graphql.dgs.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@DgsComponent
public class PlaylistDataFetcher {

    private final SpotifyClient spotifyClient;

    @Autowired
    public PlaylistDataFetcher(SpotifyClient spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    @DgsQuery
    public List<MappedPlaylist> featuredPlaylists() {
        return spotifyClient.featuredPlaylistsRequest().getPlaylists();
    }

    @DgsQuery
    public MappedPlaylist playlist(@InputArgument String playlistId) {
        return spotifyClient.playlistRequest(playlistId);
    }

    @DgsMutation
    public AddItemsToPlaylistResponse addItemsToPlaylist(@InputArgument AddItemsToPlaylistInput input) {
        String playlistId = input.getPlaylistId();
        AddItemsToPlaylistResponse response = new AddItemsToPlaylistResponse();
        Snapshot snapshot = spotifyClient.addItemsToPlaylist(playlistId, String.join(",", input.getUris()));

        if (snapshot != null) {
            String snapshotId = snapshot.id();

            if (Objects.equals(snapshotId, playlistId)) {
                Playlist playlist = spotifyClient.playlistRequest(playlistId);

                response.setCode(200);
                response.setMessage("success");
                response.setSuccess(true);
                response.setPlaylist(playlist);

                return response;
            }
        }

        response.setCode(500);
        response.setMessage("Could not update playlist");
        response.setSuccess(false);
        response.setPlaylist(null);

        return response;
    }

    @DgsData(parentType = "AddItemsToPlaylistResponse", field = "playlist")
    public MappedPlaylist getResponsePlaylist(DgsDataFetchingEnvironment dfe) {
        AddItemsToPlaylistResponse response = dfe.getSource();
        Playlist playlist = response.getPlaylist();
        if (playlist != null) {
            return spotifyClient.playlistRequest(playlist.getId());
        }

        return null;
    }
}
