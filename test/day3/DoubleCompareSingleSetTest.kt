package day3

import TestBase
import day3.DoubleCompareSingleSet.*
import org.jetbrains.lincheck.datastructures.*


@Param(name = "value", gen = IntGen::class, conf = "0:2")
class DoubleCompareSingleSetTest : TestBase(
    sequentialSpecification = DoubleCompareSingleSetIntSequential::class,
    checkObstructionFreedom = true,
    scenarios = 1000,
) {
    private val dcss = DoubleCompareSingleSet(0)

    @Operation
    fun getCell() = dcss.getCell()

    @Operation
    fun dcss(
        expectedCellState: Int, updateCellState: Int, expectedCas2Status: DoubleCompareSingleSet.Cas2Status
    ) = dcss.dcss(expectedCellState, updateCellState, expectedCas2Status)

    @Operation
    fun getStatus() = dcss.getStatus()

    @Operation
    fun updateStatus(
        @Param(gen = EnumGen::class, conf = "SUCCESS,FAILURE") status: DoubleCompareSingleSet.Cas2Status
    ) = dcss.updateStatus(status)
}

class DoubleCompareSingleSetIntSequential {
    private var cell: Int = 0
    private var cas2status = Cas2Status.UNDECIDED

    /**
     * Reads the Cell value.
     */
    fun getCell(): Int = cell

    /**
     * Performs a Double Compare Single Set operation.
     * Atomically updates the Cell value to [updateCellState]
     * if it equals [expectedCellState] and `cas2status` equals [expectedStatus].
     *
     * @return true if the operation was successful, false otherwise
     */
    fun dcss(
        expectedCellState: Int, updateCellState: Int, expectedCas2Status: Cas2Status
    ): Boolean {
        if (cell == expectedCellState && cas2status === expectedCas2Status) {
            cell = updateCellState
            return true
        } else {
            return false
        }
    }

    /**
     * Tries to update the Statue value from UNDECIDED to [status].
     * Returns `true` on success and `false` otherwise.
     */
    fun updateStatus(status: Cas2Status): Boolean {
        if (cas2status !== Cas2Status.UNDECIDED) return false
        cas2status = status
        return true
    }

    /**
     * Gets the current value of B.
     */
    fun getStatus(): Cas2Status {
        return cas2status
    }
}