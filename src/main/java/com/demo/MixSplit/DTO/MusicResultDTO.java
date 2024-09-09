package com.demo.MixSplit.DTO;

import java.util.List;
public class MusicResultDTO {
    private String id;
    private int uid;
    private int cid;
    private String name;
    private String spotifyId;
    private List<String> artistNames;


    public List<String> getArtistNames() {
        return artistNames;
    }

    public void setArtistNames(List<String> artistNames) {
        this.artistNames = artistNames;
    }


    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpotify() {
        return spotifyId;
    }

    public void setSpotify(String id) {
        this.spotifyId = id;
    }

    @Override
    public String toString() {
        return "MusicResultDTO{" +
                "id='" + id + '\'' +
                ", uid=" + uid +
                ", cid=" + cid +
                ", name='" + name + '\'' +
                ", spotifyId='" + spotifyId + '\'' +
                '}';
    }


    public static class SpotifyDTO {
        private TrackDTO track;

        public TrackDTO getTrack() {
            return track;
        }

        public void setTrack(TrackDTO track) {
            this.track = track;
        }
    }

    public static class TrackDTO {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}






