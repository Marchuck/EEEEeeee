package com.example;

/**
 * @author Lukasz Marczak
 * @since 10.09.16.
 */
public class Three<A, B, C> {
    public final A first;
    public final B second;
    public final C third;

    public Three(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
