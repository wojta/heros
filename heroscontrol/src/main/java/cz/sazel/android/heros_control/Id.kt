package cz.sazel.android.heros_control


/**
 * Created on 31.7.15.
 */
data class Id(var id: String, var name: String, var isIp: Boolean = false) {
    override fun toString(): String = name
}
