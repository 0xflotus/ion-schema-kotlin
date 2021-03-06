package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonString
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import javax.script.ScriptEngineManager

/**
 * Implements the regex constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#regex
 */
internal class Regex(
        ion: IonValue
) : ConstraintBase(ion) {

    companion object {
        private val scriptEngine = ScriptEngineManager().getEngineByName("javascript")
    }

    private val regex = if (ion !is IonString || ion.isNullValue) {
            throw InvalidSchemaException("Invalid regex constraint: $ion")
        } else {
            ion.stringValue()
                    .replace("/", "\\/")      // escape '/'
        }

    private val flags: String

    init {
        val sb = StringBuffer()
        ion.typeAnnotations.forEach {
            when (it) {
                "i", "m" -> sb.append(it)
                else -> throw InvalidSchemaException(
                        "Unrecognized flags for regex ($ion)")
            }
        }
        flags = sb.toString()
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonText>(value, issues) { v ->
            val string = v.stringValue()
                    .replace("\"", "\\\"")    // escape '"'
                    .replace("\n", "\\n")     // escape '\n' (new line)
                    .replace("\r", "\\r")     // escape '\r' (carriage return)
            val expr = "(/$regex/$flags).test(\"$string\")"
            val result = scriptEngine.eval(expr) as Boolean

            if (!result) {
                issues.add(Violation(ion, "regex_mismatch", "value doesn't match regex"))
            }
        }
    }
}

