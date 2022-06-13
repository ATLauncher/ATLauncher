package com.atlauncher.events;

public class NavigationEvent extends AnalyticsEvent.AppEvent{
    public enum Direction{
        PREVIOUS("Previus"),
        NEXT("Next");

        final String label;

        Direction(final String label){
            this.label = label;
        }

        String getLabel(){
            return this.label;
        }
    }

    public static final String ACTION = "Navigation";

    public NavigationEvent(final Direction direction, final int page, final String category){
        super(direction.getLabel(), ACTION, category, page);
    }

    public static NavigationEvent nextPage(final int page, final String category){
        return new NavigationEvent(Direction.NEXT, page, category);
    }

    public static NavigationEvent previousPage(final int page, final String category){
        return new NavigationEvent(Direction.PREVIOUS, page, category);
    }
}