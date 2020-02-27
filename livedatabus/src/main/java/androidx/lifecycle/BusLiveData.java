package androidx.lifecycle;

import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * @creator HJX
 * @time 2020.02.2020/2/25'
 */
public class BusLiveData<T> extends MutableLiveData<T> {

    /**
     * make START_VERSION public
     */
    public static final int START_VERSION = LiveData.START_VERSION;

    /**
     * Reflect Cache
     */
    private static Object[] mReflectCache = new Object[2];

    /**
     * Do the same thing as MutableLiveData,
     * except using BusLifecycleBoundObserver to wrapping observer
     */
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread();
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            return;
        }

        LifecycleBoundObserver wrapper = new BusLifecycleBoundObserver(owner, observer);
        LifecycleBoundObserver existing;
        try {
            existing = putIfAbsent(observer, wrapper);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return;
        }
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        owner.getLifecycle().addObserver(wrapper);
    }

    /**
     * Make getVersion() public
     */
    @Override
    public int getVersion() {
        return super.getVersion();
    }

    /**
     * When is the observer active
     */
    protected Lifecycle.State whenObserverShouldBeActive() {
        return Lifecycle.State.CREATED;
    }

    /**
     * Hook LiveData.mObserver.putIfAbsent
     */
    private LifecycleBoundObserver putIfAbsent(@NonNull Observer<? super T> observer,
                                               @NonNull LifecycleBoundObserver wrapper)
            throws ReflectiveOperationException {

        // Get LiveData.mObservers
        Field fieldObservers;
        if (mReflectCache[0] == null) {
            fieldObservers = LiveData.class.getDeclaredField("mObservers");
            fieldObservers.setAccessible(true);
            mReflectCache[0] = fieldObservers;
        } else {
            fieldObservers = (Field) mReflectCache[0];
        }
        Object mObservers = fieldObservers.get(this);
        assert mObservers != null;

        Method putIfAbsent;
        if (mReflectCache[1] == null) {
            Class<?> classOfSafeIterableMap = mObservers.getClass();
            putIfAbsent = classOfSafeIterableMap
                    .getDeclaredMethod("putIfAbsent", Object.class, Object.class);
            putIfAbsent.setAccessible(true);
            mReflectCache[1] = putIfAbsent;
        } else {
            putIfAbsent = (Method) mReflectCache[1];
        }

        //noinspection unchecked
        return (LifecycleBoundObserver) putIfAbsent.invoke(mObservers, observer, wrapper);
    }

    /**
     * Change active time to LiveDataBusSetting.INSTANCE.getActiveAtLast()
     */
    class BusLifecycleBoundObserver extends LifecycleBoundObserver {
        BusLifecycleBoundObserver(@NonNull LifecycleOwner owner,
                                  Observer<? super T> observer) {
            super(owner, observer);
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle()
                    .getCurrentState()
                    .isAtLeast(whenObserverShouldBeActive());
        }

        @Override
        void activeStateChanged(boolean newActive) {
            super.activeStateChanged(newActive);
        }
    }

    private static void assertMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Cannot invoke observe on a background thread");
        }
    }
}
