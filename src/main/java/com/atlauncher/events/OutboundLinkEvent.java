package com.atlauncher.events;

import java.net.URI;

public final class OutboundLinkEvent implements AnalyticsEvent{
    private final String destination;

    private OutboundLinkEvent(final String destination){
        this.destination = destination;
    }

    public String getDestination(){
        return this.destination;
    }

    public static OutboundLinkEvent forDestination(final String destination){
        return new OutboundLinkEvent(destination);
    }

    public static OutboundLinkEvent forUri(final URI uri){
        return forDestination(uri.toString());
    }
}