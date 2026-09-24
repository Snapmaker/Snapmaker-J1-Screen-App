package fabscreen.platform.base.instantiation;

/**
 * ServiceContainer saves all global services
 * services are singleton here.
 */
public interface IServiceContainer {
    /**
     * register a service for a global service
     * Notice: Existed Service can not be register again
     *
     * @param iService service interface need to be register
     * @param cls      class implementation of service
     */
    void registerService(Class<?> iService, Class<?> cls);

    /**
     * Create an instance for classes depends on Services
     * Target class not need to be managed by Container
     *
     * @param cls
     * @param ownArgs
     * @param <T>
     * @return
     */
    <T> T createInstance(Class<T> cls, Object... ownArgs);

    /**
     * Notice: Do NOT use in new code, just used for old code to get old classes
     *
     * @param cls
     * @param <T>
     * @return
     */
    @Deprecated
    <T> T getService(Class<T> cls);

}
