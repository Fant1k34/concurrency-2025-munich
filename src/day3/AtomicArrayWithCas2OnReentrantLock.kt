package day3

import java.util.concurrent.atomic.*
import java.util.concurrent.locks.*

// This implementation never stores `null` values.
class AtomicArrayWithCas2OnReentrantLock<E : Any>(size: Int, initialValue: E) {
    private val array = AtomicReferenceArray<E>(size)

    // TODO: Use these locks to protect the array cells.
    private val locks = Array(size) { ReentrantLock() }

    init {
        // Fill array with the initial value.
        for (i in 0 until size) {
            array[i] = initialValue
        }
    }

    fun get(index: Int): E {
        // TODO: Guard this function with the cell lock.
        return array[index]
    }

    fun cas2(
        index1: Int, expected1: E, update1: E,
        index2: Int, expected2: E, update2: E
    ): Boolean {
        require(index1 != index2) { "The indices should be different" }
        // TODO: Guard this function with the cell locks
        // TODO: following the fine-grained locking approach.
        if (array[index1] === expected1 && array[index2] === expected2) {
            array.set(index1, update1)
            array.set(index2, update2)
            return true
        } else {
            return false
        }
    }
}