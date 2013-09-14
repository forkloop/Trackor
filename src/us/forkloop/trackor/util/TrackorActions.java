package us.forkloop.trackor.util;

public enum TrackorActions {
    CAMERA_ACTION ("us.forkloop.trackor.CAMERA"),
    WIDGET_ACTION ("us.forkloop.trackor.WIDGET");

    private String action;

    TrackorActions(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}