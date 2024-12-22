package io.github.jfunk.functions;

@FunctionalInterface
public interface FunctionN<R> {
    R apply(Object... args);
}