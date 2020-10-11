package de.zh32.pingtest;

import java.util.List;

/**
 * @author zh32 <zh32 at zh32.de>
 */
public class Response {
    private String description;
    private Players players;
    private Version version;
    private String favicon;
    private int time;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Players getPlayers() {
        return players;
    }

    public void setPlayers(Players players) {
        this.players = players;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public static class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getOnline() {
            return online;
        }

        public void setOnline(int online) {
            this.online = online;
        }

        public List<Player> getSample() {
            return sample;
        }

        public void setSample(List<Player> sample) {
            this.sample = sample;
        }
    }

    public static class Player {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Version {
        private String name;
        private String protocol;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }
}