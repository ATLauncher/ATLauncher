package de.zh32.pingtest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ServerListPing16 {
    private InetSocketAddress address;
    private int timeout = 2500;
    private int pingVersion = -1;
    private int protocolVersion = -1;
    private String gameVersion;
    private String motd;
    private int playersOnline = -1;
    private int maxPlayers = -1;

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPingVersion() {
        return pingVersion;
    }

    public void setPingVersion(int pingVersion) {
        this.pingVersion = pingVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public int getPlayersOnline() {
        return playersOnline;
    }

    public void setPlayersOnline(int playersOnline) {
        this.playersOnline = playersOnline;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean fetchData() throws Exception {
        Socket socket = new Socket();
        OutputStream outputStream;
        DataOutputStream dataOutputStream;
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        socket.setSoTimeout(timeout);
        socket.setKeepAlive(false);

        socket.connect(address, this.getTimeout());
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);

        inputStream = socket.getInputStream();
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_16BE);
        dataOutputStream.write(new byte[]{(byte) 0xFE, (byte) 0x01, (byte) 0xFA

        });
        dataOutputStream.write("MC|PingHost".getBytes(StandardCharsets.UTF_16BE));
        dataOutputStream.writeShort((address.getAddress().getHostAddress().length() * 2) + 7);
        dataOutputStream.write(74);
        dataOutputStream.writeShort(address.getAddress().getHostAddress().length());
        dataOutputStream.write(address.getAddress().getHostAddress().getBytes(StandardCharsets.UTF_16BE));
        dataOutputStream.writeInt(address.getPort());

        int packetId = inputStream.read();

        if (packetId == -1) {
            socket.close();
            throw new IOException("Premature end of stream.");
        }

        if (packetId != 0xFF) {
            socket.close();
            throw new IOException("Invalid packet ID (" + packetId + ").");
        }

        int length = inputStreamReader.read();

        if (length == -1) {
            socket.close();
            throw new IOException("Premature end of stream.");
        }

        if (length == 0) {
            socket.close();
            throw new IOException("Invalid string length.");
        }

        char[] chars = new char[length];

        if (inputStreamReader.read(chars, 0, length) != length) {
            socket.close();
            throw new IOException("Premature end of stream.");
        }

        String string = new String(chars);

        String[] data = string.split("\00");

        this.pingVersion = Integer.parseInt(data[0].substring(1));
        this.protocolVersion = Integer.parseInt(data[1]);
        this.gameVersion = data[2];
        this.motd = data[3];
        this.playersOnline = Integer.parseInt(data[4]);
        this.maxPlayers = Integer.parseInt(data[5]);

        dataOutputStream.close();
        outputStream.close();

        inputStreamReader.close();
        inputStream.close();
        socket.close();

        return true;
    }
}
