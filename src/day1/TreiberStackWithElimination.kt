package day1

import java.util.concurrent.*
import java.util.concurrent.atomic.*

open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()

    // TODO: Try to optimize concurrent push and pop operations,
    // TODO: synchronizing them in an `eliminationArray` cell.
    private val eliminationArray = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushElimination(element: E): Boolean {
        // TODO: Choose a random cell in `eliminationArray`
        // TODO: and try to install the element there.
        // TODO: Wait `ELIMINATION_WAIT_CYCLES` loop cycles
        // TODO: in hope that a concurrent `pop()` grabs the
        // TODO: element. If so, clean the cell and finish,
        // TODO: returning `true`. Otherwise, move the cell
        // TODO: to the empty state and return `false`.

        val randomCellId = randomCellIndex()

        val isAddedToEliminationArray = eliminationArray.compareAndSet(randomCellId, CELL_STATE_EMPTY, element)
        if (!isAddedToEliminationArray) return false

        repeat(ELIMINATION_WAIT_CYCLES) {
            val isCellRetrieved = eliminationArray.compareAndSet(randomCellId, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)

            if (isCellRetrieved) return true
        }

        val isRetrievedForTheLastTime = eliminationArray.getAndUpdate(randomCellId) {
            when (it) {
                CELL_STATE_EMPTY -> error("Cell could not be in EMPTY, another thread modified it!")
                CELL_STATE_RETRIEVED -> CELL_STATE_EMPTY
                else -> CELL_STATE_EMPTY
            }
        }

        return isRetrievedForTheLastTime == CELL_STATE_RETRIEVED
    }

    override fun pop(): E? = tryPopElimination() ?: stack.pop()

    private fun tryPopElimination(): E? {
        // TODO: Choose a random cell in `eliminationArray`
        // TODO: and try to retrieve an element from there.
        // TODO: On success, return the element.
        // TODO: Otherwise, if the cell is empty, return `null`.
        val randomCellId = randomCellIndex()

        val result = eliminationArray.getAndUpdate(randomCellId) {
            when (it) {
                CELL_STATE_EMPTY -> it
                CELL_STATE_RETRIEVED -> it
                else -> CELL_STATE_RETRIEVED
            }
        }

        return when (result) {
            CELL_STATE_EMPTY -> null
            CELL_STATE_RETRIEVED -> null
            else -> result as E?
        }
    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(eliminationArray.length())

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 2 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        // Initially, all cells are in EMPTY state.
        private val CELL_STATE_EMPTY = null

        // `tryPopElimination()` moves the cell state
        // to `RETRIEVED` if the cell contains element.
        private val CELL_STATE_RETRIEVED = Any()
    }
}