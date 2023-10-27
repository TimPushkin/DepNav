package ru.spbu.depnav.utils.misc

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Updates the [MutableStateFlow.value] atomically with [update] of the current value if [condition]
 * evaluates to true for it.
 *
 * [update] and [condition] may be evaluated multiple times, if the value is being concurrently
 * updated.
 */
inline fun <T> MutableStateFlow<T>.updateIf(condition: (T) -> Boolean, update: (T) -> T) {
    while (true) {
        val prevValue = value
        if (!condition(prevValue)) {
            return
        }
        val nextValue = update(prevValue)
        if (compareAndSet(prevValue, nextValue)) {
            return
        }
    }
}
