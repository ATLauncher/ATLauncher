package com.atlauncher.data;

public abstract class CheckState {

    public static class NotChecking extends CheckState {
    }

    public static class CheckPending extends CheckState {
    }

    public static class Checking extends CheckState {
    }

    public static class Checked extends CheckState {
        public final boolean valid;

        public Checked(boolean valid) {
            this.valid = valid;
        }
    }
}