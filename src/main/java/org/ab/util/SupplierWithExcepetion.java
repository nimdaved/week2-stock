package org.ab.util;


@FunctionalInterface
public interface SupplierWithExcepetion<T, E extends Exception> {
	T get() throws E;
}
