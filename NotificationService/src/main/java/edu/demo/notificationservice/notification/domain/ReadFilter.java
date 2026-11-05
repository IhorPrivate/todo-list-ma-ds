package edu.demo.notificationservice.notification.domain;

/**
 * Restricts a notification search by read state.
 *
 * @since 0.0.1
 */
public enum ReadFilter {

    /** Read and unread notifications. */
    ANY,

    /** Only notifications not yet read. */
    UNREAD,

    /** Only notifications already read. */
    READ;

    /**
     * @param status a notification's read status
     * @return {@code true} if a notification in that state passes the filter
     */
    public boolean matches(ReadStatus status) {
        return switch (this) {
            case ANY -> true;
            case UNREAD -> status instanceof ReadStatus.Unread;
            case READ -> status instanceof ReadStatus.Read;
        };
    }
}
