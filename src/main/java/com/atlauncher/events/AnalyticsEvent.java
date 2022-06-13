package com.atlauncher.events;

import java.net.URI;

public abstract class AnalyticsEvent extends Event{
    protected AnalyticsEvent(){
        super();
    }

    public static class AppEvent extends AnalyticsEvent{
        private final String label;
        private final String action;
        private final String category;
        private final int value;

        public AppEvent(final String label, final String action, final String category, final int value){
            super();
            this.label = label;
            this.action = action;
            this.category = category;
            this.value = value;
        }

        public AppEvent(final String label, final String action, final String category){
            this(label, action, category, 0);
        }

        public AppEvent(final String action, final String category){
            this(null, action, category);
        }

        public String getLabel(){
            return this.label;
        }

        public String getAction(){
            return this.action;
        }

        public String getCategory(){
            return this.category;
        }

        public int getValue(){
            return this.value;
        }
    }

    public static final class OutboundLinkEvent extends AnalyticsEvent{
        private final String destination;

        public OutboundLinkEvent(final String destination){
            super();
            this.destination = destination;
        }

        public OutboundLinkEvent(final URI uri){
            this(uri.toString());
        }

        public String getDestination(){
            return this.destination;
        }
    }

    public static final class AppExceptionEvent extends AnalyticsEvent{
        private final Throwable throwable;

        public AppExceptionEvent(final Throwable th){
            super();
            this.throwable = th;
        }

        public Throwable getThrowable(){
            return this.throwable;
        }

        public String getMessage(){
            return this.getThrowable().getMessage();
        }
    }
}
