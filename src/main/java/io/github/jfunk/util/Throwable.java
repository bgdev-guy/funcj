package io.github.jfunk.util;

@FunctionalInterface
public interface Throwable {

    /**
     * Apply this function
     *
     * @throws Exception the exception
     */
    void apply() throws Exception;
}