package software.amazon.ionschema.internal.util

import software.amazon.ion.IonInt
import software.amazon.ion.IonList
import software.amazon.ionschema.InvalidSchemaException
import java.math.BigDecimal

/**
 * Implementation of Range<Int>, which mostly delegates to RangeBigDecimal.
 */
internal class RangeInt (
        private val ion: IonList,
        private val delegate: RangeBigDecimal = RangeBigDecimal(ion)
) : Range<Int> {

    init {
        if (!(ion[0] is IonInt || isRangeMin(ion[0]))) {
            throw InvalidSchemaException("Invalid lower bound in int $ion")
        }

        if (!(ion[1] is IonInt || isRangeMax(ion[1]))) {
            throw InvalidSchemaException("Invalid upper bound in int $ion")
        }

        if (delegate.lower.value != null && delegate.upper.value != null
                && (delegate.lower.boundaryType == RangeBoundaryType.EXCLUSIVE
                    || delegate.upper.boundaryType == RangeBoundaryType.EXCLUSIVE)) {
            val minPlusOne = delegate.lower.value.add(BigDecimal.ONE)
            if (minPlusOne == delegate.upper.value) {
                throw InvalidSchemaException("No valid values in the int range $ion")
            }
        }
    }

    override fun contains(value: Int) = delegate.contains(value.toBigDecimal())

    internal fun isAtMax(value: Int) = delegate.upper.compareTo(value.toBigDecimal()) == 0

    override fun toString() = ion.toString()
}

