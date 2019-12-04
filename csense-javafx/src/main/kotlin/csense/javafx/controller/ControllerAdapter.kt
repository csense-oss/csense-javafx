@file:Suppress("NOTHING_TO_INLINE")

package csense.javafx.controller

import csense.kotlin.annotations.threading.*
import csense.javafx.viewdsl.*
import csense.javafx.views.base.*
import javafx.scene.layout.*
import kotlin.contracts.*

/**
 *
 * @receiver Pane
 * @param controller BaseView<*, *>
 */
@InUi
inline fun <T : BaseView<*, *>> Pane.useController(
        controller: T,
        crossinline action: ScopedViewDsl<T>
): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return this.useController(controller).apply(action)
}

@InUi
inline fun <T : BaseView<*, *>> Pane.useController(controller: T): T {
    val text = this.text { text = "loading" } //TODO change this..
    controller.replaceToView(this, text)
    return controller
}