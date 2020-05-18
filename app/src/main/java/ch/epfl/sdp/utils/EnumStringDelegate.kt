package ch.epfl.sdp.utils

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class EnumStringDelegate<T : Enum<T>>(
        private val enumClass: Class<T>,
        private val otherProperty: KMutableProperty<T>,
        private val enumNameToString: (String) -> String,
        private val stringToEnumName: (String) -> String) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return enumNameToString(otherProperty.call(thisRef).toString())
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val enumValue = java.lang.Enum.valueOf(enumClass, stringToEnumName(value))
        otherProperty.setter.call(thisRef, enumValue)
    }
}

inline fun <reified T : Enum<T>> enumStringLowerCase(
        property: KMutableProperty<T>) = EnumStringDelegate(
        T::class.java,
        property,
        String::toLowerCase,
        String::toUpperCase)
