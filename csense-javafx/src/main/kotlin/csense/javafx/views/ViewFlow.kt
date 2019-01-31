package csense.javafx.views

import csense.javafx.extensions.asyncIO
import csense.javafx.views.base.OnViewSetup
import csense.kotlin.AsyncFunction0
import csense.kotlin.extensions.coroutines.asyncDefault
import kotlinx.coroutines.*


abstract class InputDataFlow<Din, DinTransformed>(scope: CoroutineScope, input: Din) {
    val result: Deferred<DinTransformed> =
        scope.asyncDefault { transformInput(input) }

    abstract suspend fun transformInput(input: Din): DinTransformed
}

abstract class ViewFlow<ViewLoad, ViewBinding>(scope: CoroutineScope) {

    val view: Deferred<ViewBinding> by lazy {
        scope.loadAndTransformMain(::loadView) {
            bindView(
                it,
                OnViewSetup.instance
            )
        }
    }

    abstract suspend fun loadView(): ViewLoad

    abstract fun bindView(loadedView: ViewLoad, onViewSetup: OnViewSetup): ViewBinding
}

private fun <T, U> CoroutineScope.loadAndTransformDefault(
    loader: suspend () -> T,
    transformer: suspend (T) -> U
): Deferred<U> = loadAndTransform(Dispatchers.Default, loader, transformer)

private fun <T, U> CoroutineScope.loadAndTransformMain(
    loader: suspend () -> T,
    transformer: suspend (T) -> U
): Deferred<U> = loadAndTransform(Dispatchers.Main, loader, transformer)


private fun <T, U> CoroutineScope.loadAndTransform(
    dispatcher: CoroutineDispatcher,
    loader: suspend () -> T,
    transformer: suspend (T) -> U
): Deferred<U> = asyncIO {
    val loaded = loader()
    withContext(dispatcher) {
        transformer(loaded)
    }
}

class DelegatingViewFlow<ViewLoad, ViewBinding>(
    scope: CoroutineScope,
    private val loadViewFunc: AsyncFunction0<ViewLoad>,
    private val bindFunction: Function2<ViewLoad, OnViewSetup, ViewBinding>
) : ViewFlow<ViewLoad, ViewBinding>(scope) {

    override suspend fun loadView(): ViewLoad {
        return loadViewFunc()
    }

    override fun bindView(loadedView: ViewLoad, onViewSetup: OnViewSetup): ViewBinding =
        bindFunction(loadedView, onViewSetup)

}

