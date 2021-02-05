package de.kekru.sonarwaitqualitygate.utils;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
  R apply(T t) throws Exception;
}
