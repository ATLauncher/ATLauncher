package de.zh32.pingtest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * @author zh32 <zh32 at zh32.de>
 */
public class ServerListPing17 {

    private InetSocketAddress host;
    private int timeout = 7000;
    private Gson gson = new Gson();

    public InetSocketAddress getHost() {
        return host;
    }

    public void setHost(InetSocketAddress host) {
        this.host = host;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setAddress(InetSocketAddress host) {
        this.host = host;
    }

    public InetSocketAddress getAddress() {
        return this.host;
    }

    void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    int getTimeout() {
        return this.timeout;
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public StatusResponse fetchData() throws IOException {

        Socket socket = new Socket();
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        socket.setSoTimeout(this.timeout);

        socket.connect(host, timeout);

        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);

        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(b);
        handshake.writeByte(0x00); // packet id for handshake
        writeVarInt(handshake, 4); // protocol version
        writeVarInt(handshake, this.host.getAddress().getHostAddress().length()); // host length
        handshake.writeBytes(this.host.getAddress().getHostAddress()); // host string
        handshake.writeShort(host.getPort()); // port
        writeVarInt(handshake, 1); // state (1 for handshake)

        writeVarInt(dataOutputStream, b.size()); // prepend size
        dataOutputStream.write(b.toByteArray()); // write handshake packet

        dataOutputStream.writeByte(0x01); // size is only 1
        dataOutputStream.writeByte(0x00); // packet id for ping
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int id = readVarInt(dataInputStream); // packet id

        if (id == -1) {
            socket.close();
            throw new IOException("Premature end of stream.");
        }

        if (id != 0x00) { // we want a status response
            socket.close();
            throw new IOException("Invalid packetID");
        }
        int length = readVarInt(dataInputStream); // length of json string

        if (length == -1) {
            socket.close();
            throw new IOException("Premature end of stream.");
        }

        if (length == 0) {
            socket.close();
            throw new IOException("Invalid string length.");
        }

        byte[] in = new byte[length];
        dataInputStream.readFully(in); // read json string
        String json = new String(in);
        StatusResponse response;
        try {
            response = gson.fromJson(json, StatusResponseOld.class);
        } catch (JsonSyntaxException jse) {
            response = gson.fromJson(json, StatusResponseNew.class);
        }
        if (response == null) {
            socket.close();
            throw new IOException("Cannot parse JSON.");
        }
        try {
            long now = System.currentTimeMillis();
            dataOutputStream.writeByte(0x09); // size of packet
            dataOutputStream.writeByte(0x01); // 0x01 for ping
            dataOutputStream.writeLong(now); // time!?

            readVarInt(dataInputStream);
            id = readVarInt(dataInputStream);
            if (id == -1) {
                socket.close();
                throw new IOException("Premature end of stream.");
            }
            if (id != 0x01) {
                socket.close();
                throw new IOException("Invalid packetID");
            }
            long pingtime = dataInputStream.readLong(); // read response

            response.setTime((int) (now - pingtime));
        } catch (IOException exception) {

        }

        dataOutputStream.close();
        outputStream.close();
        inputStreamReader.close();
        inputStream.close();
        socket.close();

        return response;
    }

    public interface StatusResponse {
        String getDescription();

        Players getPlayers();

        Version getVersion();

        String getFavicon();

        int getTime();

        void setTime(int time);
    }

    public static class StatusResponseOld implements StatusResponse {
        private String description;
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        public String getDescription() {
            return description;
        }

        public Players getPlayers() {
            return players;
        }

        public Version getVersion() {
            return version;
        }

        public String getFavicon() {
            return favicon;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

    }

    public static class StatusResponseNew implements StatusResponse {
        private Description description;
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        public String getDescription() {
            return description.getText();
        }

        public Players getPlayers() {
            return players;
        }

        public Version getVersion() {
            return version;
        }

        public String getFavicon() {
            return favicon;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

    }

    public static class Description {
        private String text;

        public String getText() {
            return text;
        }
    }

    public static class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getMax() {
            return max;
        }

        public int getOnline() {
            return online;
        }

        public List<Player> getSample() {
            return sample;
        }
    }

    public static class Player {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

    }

    public static class Version {
        private String name;
        private String protocol;

        public String getName() {
            return name;
        }

        public String getProtocol() {
            return protocol;
        }
    }
}
