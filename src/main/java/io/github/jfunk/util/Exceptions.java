package io.github.jfunk.util;

public class Exceptions {

    public static void wrap(Throwable throwable) {
        try {
            throwable.apply();
        } catch (Exception e) {
            throwUnchecked(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <X extends Exception> void throwUnchecked(Exception ex) throws X {
        throw (X) ex;
    }

}
