package mchorse.bbs.events.base;

import java.lang.reflect.Method;

/**
 * Class for all subscribers.
 */
public class Subscription {
    public Object target;
    public Method method;

    public Subscription(Object target, Method method) {
        this.target = target;
        this.method = method;

        method.setAccessible(true);
    }
}