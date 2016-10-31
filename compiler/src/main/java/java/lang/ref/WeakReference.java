package java.lang.ref;

/**
 * Project "EEEEeeee"
 * <p/>
 * Created by Lukasz Marczak
 * on 19.10.16.
 */
public class WeakReference<T> {
    T ref;

    public T get() {
        return ref;

    }

    public WeakReference(T t) {
        ref = t;
    }
}
