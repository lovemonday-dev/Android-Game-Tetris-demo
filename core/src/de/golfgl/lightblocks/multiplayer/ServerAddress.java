package de.golfgl.lightblocks.multiplayer;

import com.github.czyzby.websocket.WebSockets;

public class ServerAddress implements IRoomLocation {
    private final String name;
    private final String address;

    public ServerAddress(String address) {
        this(null, address, 0, false);
    }

    public ServerAddress(String name, String address, int port, boolean secure) {
        int dotSlashPos = address.indexOf("://");

        if (dotSlashPos > 0) {
            this.name = name != null ? name : address.substring(dotSlashPos + 3);
            this.address = address;
        } else {
            int colonPos = address.indexOf(':');
            String contentPath;
            int resPos = address.indexOf('/');
            if (resPos > 0) {
                contentPath = address.substring(resPos + 1);
                address = address.substring(0, resPos);
            } else {
                contentPath = "";
            }
            if (colonPos > 0) {
                try {
                    port = Integer.parseInt(address.substring(colonPos + 1));
                } catch (Throwable ignored) {
                }
                address = address.substring(0, colonPos);
            }
            if (secure)
                this.address = WebSockets.toSecureWebSocketUrl(address, port != 0 ? port : 443, contentPath);
            else
                this.address = WebSockets.toWebSocketUrl(address, port != 0 ? port : 80, contentPath);
            this.name = name != null ? name : address;
        }
    }

    public ServerAddress(String name, String adressUrl) {
        this.name = name;
        this.address = adressUrl;
    }

    @Override
    public String getRoomName() {
        return name;
    }

    @Override
    public String getRoomAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getRoomName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerAddress that = (ServerAddress) o;
        return address.equals(that.address);
    }
}
