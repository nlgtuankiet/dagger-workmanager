# Dagger 2 setup with WorkManager, a complete step by step guide

> **Tl; DR:** Use [WorkerFactory](https://developer.android.com/reference/androidx/work/WorkerFactory), [AssistedInject](https://github.com/square/AssistedInject), and [Dagger’s Multibindings](https://google.github.io/dagger/multibindings) we can inject dependencies into Worker class, this is similar with the way we did with ViewModel.


WorkManager just hit beta a few days ago, in the release note, the team has mentioned:

> This release contains no API changes; moving forward, WorkManager is expected to stay API stable until the next version
I guess this is a good time to write about it, more specifically, how to inject dependence into Worker class?

In this tutorial, we‘re not discussing the basic of WorkManager ratter than a proper Dagger 2 setup. So if you are new to WorkManager, I recommend checking out the [official document](https://developer.android.com/topic/libraries/architecture/workmanager/)

### Goal
Inject dependence into Worker class using constructor injection. Something like the below code snippet
```kotlin
class HelloWorldWorker @Inject constructor(
    private val params: WorkerParameters,
    private val appContext: Context,
    private val foo: Foo // test dependence
    // add more dependencies here
) : Worker(appContext, params)
```
### Problems
The first problem is that Workers are instantiated by WorkerManager (like Activity and Fragment is instantiate by Android framework) not by us. This means you can’t pass any other parameter as the dependencies in the constructor expect the Context and WorkerParameters, therefore, it is impossible to perform constructor injection. This left out for us the only option is field injection.

```kotlin
class HelloWorldWorker(
    params: WorkerParameters, 
    appContext: Context
) : Worker(appContext, params) {

    @Inject lateinit var foo: Foo

    override fun doWork(): Result {
        TODO()
    }
}
```
In the alpha 9 release, Android team introduce a new abstract class called [WorkerFactory](https://developer.android.com/reference/androidx/work/WorkerFactory)
> A factory object that creates ListenableWorker instances. The factory is invoked every time a work runs

In a nutshell, if there is a custom factory registered to WorkManager (let call it SampleWorkerFactory), every time a new worker is requested, WorkerManager will ask for SampleWorkerFactory to construct new worker instance. This is great because through our custom factory we can now decide how to construct worker instance, not restricted to the default constructor anymore.

> **TL; DR:** with the introduction of WorkerFactory we can now perform constructor inject in our worker.

The idea is simple. Each worker will have an inner class called Factory, this factory responsible for supply dependencies for the parent worker. We will annotate this factory with Inject, all of the worker’s dependencies will go there left out only the WorkerParameters. Then in the create method, we instantiate our worker with all the parameter we need. And since every worker have this common method it is reasonable to make an interface for it (let call it ChildWorkerFactory, this interface, later on, become useful since we will work with Dagger Multibind)

```kotlin
interface ChildWorkerFactory<T : ListenableWorker> {
    fun create(params: WorkerParameters): T
}
```

```kotlin
class Foo @Inject constructor() // test dependence

class HelloWorldWorker(
    params: WorkerParameters,
    private val appContext: Context,
    private val foo: Foo // test dependence
    // add more dependencies here
) : Worker(appContext, params) {
    override fun doWork(): Result {
      TODO()
    }
  
    class Factory @Inject constructor(
        // left out params: WorkerParameters for the create() method
        private val appContext: Provider<Context>,
        private val foo: Provider<Foo>
    ) : ChildWorkerFactory<HelloWorldWorker> {
        override fun create(params: WorkerParameters): HelloWorldWorker {
            return HelloWorldWorker(
                params,
                appContext.get(),
                foo.get()
            )
        }
    }
}
```

In this step dagger already know how to inject the HelloWorldWorker.Factory since all of its dependence is fulfilled (notice how we left out the WorkerParameters)

Move on to the Dagger 2 multibind setup for WorkManager

```kotlin
@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

@Module
interface WorkerBindingModule {
    @Binds
    @IntoMap
    @WorkerKey(HelloWorldWorker::class)
    fun bindHelloWorldWorker(factory: HelloWorldWorker.Factory): ChildWorkerFactory<out ListenableWorker>
}

@Component(
    modules = [
        WorkerBindingModule::class,
    ]
)
interface SampleComponent {
    // other method
}
```
The setup is straightforward, we bind this HelloWorldWorker.Factory (a.k.a ChildWorkerFactory) into Dagger Multibind map with a WorkerKey

Finally, the SampleWorkerFactory, our custom factory that we will register with WorkerManager.

```kotlin
class SampleWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory<out ListenableWorker>>>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry =
            workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
        val factory = foundEntry?.value
            ?: throw IllegalArgumentException("unknown worker class name: $workerClassName")
        return factory.get().create(workerParameters)
    }
}
```
> **Note:** remember to register this factory inside your Application and AndroidManifest.xml, more on that here or look at the source code in the end of this post.

We then hit the run button…

```
D/HelloWorldWorker: Hello world!
D/HelloWorldWorker: Injected foo: com.sample.daggerworkmanagersample.Foo@215b58d0
I/WM-WorkerWrapper: Worker result SUCCESS for Work [ id=c1628749-ed19-4b11-b027-95031d3b3bae, tags={ com.sample.daggerworkmanagersample.HelloWorldWorker } ]
```

Yay…!!!

### Factories for days
The problem is not stopping there. We now end up with a double factory setup. SampleWorkerFactory lookup for ChildWorkerFactory then uses that factory the construct worker instance. Writing those factories is annoying, it is still acceptable if your worker doesn’t have many dependencies. But imagine your app need 10 workers, each worker requires 10 dependence, that means 10 extra ChildWorkerFactory needed to implement manually. Now that becomes a big problem. How can we solve this?

This is where [AssistedInject](https://github.com/square/AssistedInject) comes to play. A library by Square that compatible with Dagger 2, it generates all of the ChildWorkerFactory implementations for us and also bind the generated implementation to Dagger. Read more about it [here](https://jakewharton.com/helping-dagger-help-you/).

Setup our existing code base with AssistedInject is simple. Annotate worker class with AssistedInject. Any parameters that we want to create with the generated factory, annotate it with Assisted. And for the factory (originally class that now become interface), annotate it with AssistedInject.Factory, let AssistedInject do the work. Our worker classes now look a lot of cleaners and the most fun part is we now don't have to write these boilerplate codes anymore.

```kotlin

class HelloWorldWorker @AssistedInject constructor(
    @Assisted private val params: WorkerParameters,
    private val appContext: Context,
    private val foo: Foo
) : Worker(appContext, params) {
    private val TAG = "HelloWorldWorker"
    override fun doWork(): Result {
        Log.d(TAG, "Hello world!")
        Log.d(TAG, "Injected foo: $foo")
        return Result.success()
    }

    @AssistedInject.Factory
    interface Factory  : ChildWorkerFactory<HelloWorldWorker>
}
```
Since AssistedInject generates and binds those factories. Declare a module that includes the generated module, annotate it with AssistedModule, add it to our Component. Our DI setup now looks like this
```kotlin
@Module(includes = [AssistedInject_SampleAssistedInjectModule::class])
@AssistedModule
interface SampleAssistedInjectModule

@Component(
    modules = [
        SampleAssistedInjectModule::class,
        WorkerBindingModule::class
    ]
)
interface SampleComponent {
    // setup
}
```

We hit the fun button again and all is working as expected expect

### Understand the generated code
It’s important to understand what is going under the hood, so let dive into the generated source code
```java
public final class HelloWorldWorker_AssistedFactory implements HelloWorldWorker.Factory {
  private final Provider<Context> appContext;

  private final Provider<Foo> foo;

  @Inject
  public HelloWorldWorker_AssistedFactory(Provider<Context> appContext, Provider<Foo> foo) {
    this.appContext = appContext;
    this.foo = foo;
  }

  @Override
  public HelloWorldWorker create(WorkerParameters params) {
    return new HelloWorldWorker(
        params,
        appContext.get(),
        foo.get());
  }
}

@Module
public abstract class AssistedInject_SampleAssistedInjectModule {
  private AssistedInject_SampleAssistedInjectModule() {
  }

  @Binds
  abstract HelloWorldWorker.Factory bind_com_sample_daggerworkmanagersample_HelloWorldWorker(
      HelloWorldWorker_AssistedFactory factory);
}
```
First off, the generated implementation of HelloWorldWorker.Factory, look almost the same as our original code. Next, generated module (a.k.a AssistedInject_SampleAssistedInjectModule) AssistedInject simply binds the HelloWorldWorker_AssistedFactory to HelloWorldWorker.Factory, that is how Dagger know about HelloWorldWorker.Factory.

### Conclusion
I have consulted many ways to solve this problem, including subcomponent, member injects inside worker class. This is by far IMHO the most efficient way. The source code is available. If you run into trouble, feel free to open an issue, I will try my best I answer it all. Thanks for reading and happy coding!

### Further reading
* [AssistedInject — Guice Wiki](https://github.com/google/guice/wiki/AssistedInject)
* [Helping Dagger Help You by JAKE WHARTON](https://jakewharton.com/helping-dagger-help-you/)


