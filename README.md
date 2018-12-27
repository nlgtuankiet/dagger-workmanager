# Dagger 2 setup with WorkManager, a complete step by step guild

> Tl; DR Use WorkerFactory, AssistedInject, and Dagger’s Multibindings we can inject dependencies into Worker class, this is similar with the way we did with ViewModel. Fully working sample project can be found here

```kotlin
class HelloWorldWorker @Inject constructor(
    private val params: WorkerParameters,
    private val appContext: Context,
    private val foo: Foo // test dependence
    // add more dependencies here
) : Worker(appContext, params)
```
