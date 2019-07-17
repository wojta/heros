package cz.sazel.android.heros.base

import cz.sazel.android.heros.BuildConfig

/**
 * Created by wojta on 16.5.14.
 */
object Constants {
    const val ACTION_MSG_RECEIVED = "cz.sazel.larpos.ACTION_MSG_RECEIVED"
    const val REG_IDS_URL = "http://temp.sazel.cz/app/regid.php"

    const val PACKAGE = BuildConfig.APPLICATION_ID

    const val CHANGE_OS_REQUEST = "1"
    const val OTHER_REQUEST = "2"
    const val MSG_TYPE = "msgType"
    const val NAME = "name"
    const val COLOR_VARIANT = "colorVariant"
    const val OTHER_EVENT = "otherEvent"
}