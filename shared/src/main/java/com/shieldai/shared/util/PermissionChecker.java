// ...new file...
package com.shieldai.shared.util;

import java.util.Optional;
import java.util.function.Supplier;

/** Small wrapper to run an inspection operation and capture permission/blocking reason instead of throwing. */
public final class PermissionChecker {

    private PermissionChecker() {}

    /**
     * Run inspector and return Optional.empty() on success; otherwise returns a reason string.
     * Use to wrap platform process/tab inspection calls which might throw SecurityException/IO/etc.
     */
    public static Optional<String> runAndCaptureReason(Runnable inspector) {
        try {
            inspector.run();
            return Optional.empty();
        } catch (SecurityException se) {
            return Optional.of("permission_denied: " + se.getMessage());
        } catch (UnsupportedOperationException uo) {
            return Optional.of("unsupported_operation: " + uo.getMessage());
        } catch (Throwable t) {
            // generic catch — don't crash the scanner; log the reason upstream
            return Optional.of("inspection_failed: " + t.getClass().getSimpleName() + ":" + t.getMessage());
        }
    }

    /**
     * Convenience form for suppliers that return a value; if blocked returns empty Optional and a reason can be logged.
     */
    public static <T> java.util.Optional<T> runSafely(Supplier<T> supplier, java.util.function.Consumer<String> onBlocked) {
        try {
            return java.util.Optional.ofNullable(supplier.get());
        } catch (Throwable t) {
            if (onBlocked != null) onBlocked.accept("inspection_failed: " + t.getClass().getSimpleName() + ":" + t.getMessage());
            return java.util.Optional.empty();
        }
    }
}