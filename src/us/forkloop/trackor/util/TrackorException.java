package us.forkloop.trackor.util;

public class TrackorException extends RuntimeException {

    private static final long serialVersionUID = -7720915849866513427L;

    public TrackorException(Exception e) {
        super(e);
    }
}