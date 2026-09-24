package fabscreen.platform.base.instantiation;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceContainer implements IServiceContainer, IServiceIdentifier {
    private static final ServiceContainer mInstance = new ServiceContainer();
    private final Map<Class<?>, Object> mSingletons = new LinkedHashMap<>();

    private ServiceContainer() {
        mSingletons.put(IServiceContainer.class, this);
    }

    public static ServiceContainer getInstance() {
        return mInstance;
    }

    @Override
    public void registerService(Class<?> iService, Class<?> clazz) {
        try {
            List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());

            if (mSingletons.get(iService) != null) {
                Log.e("Error", "cannot re-register the same service");
                return;
            }

            if (!interfaces.contains(iService)) {
                Log.e("Type error", "class provided did not implement the interface");
                return;
            }
            if (!interfaces.contains(IServiceIdentifier.class)) {
                Log.e("Type error", "class provided did not implement IServiceIdentifier");
                return;
            }

            //FIXME: CAN WE ALWAYS GET THE PROPER ONE?
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            Class<?>[] ctrParameterTypes = constructor.getParameterTypes();
            Object[] args = new Object[ctrParameterTypes.length];
            for (int i = 0; i < ctrParameterTypes.length; i++) {
                Class<?> param = ctrParameterTypes[i];
                args[i] = mSingletons.get(param);
                if (args[i] == null) {
                    Logger.d("Wrong init param");
                }
            }
            // set this to access private constructor
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(args);
            mSingletons.put(iService, instance);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T createInstance(Class<T> cls, Object... ownArgs) {
        try {
            Constructor<?> ctor = cls.getConstructors()[0];
            Class<?>[] ctorParameterTypes = ctor.getParameterTypes();
            Object[] args = new Object[ctorParameterTypes.length];

            for (int i = 0; i < ctorParameterTypes.length; i++) {
                Class<?> param = ctorParameterTypes[i];
                if (ownArgs.length > i) {

                    if (ownArgs[i].getClass().equals(param)) {
                        args[i] = ownArgs[i];
                    } else {
                        Logger.d("Wrong init param");
                    }
                } else {
                    args[i] = mSingletons.get(param);
                    if (args[i] == null) {
                        Logger.d("Instance not found, wrong init param");
                    }
                }
            }

            return (T) ctor.newInstance(args);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T getService(Class<T> cls) {
        return (T) mSingletons.get(cls);
    }
}
