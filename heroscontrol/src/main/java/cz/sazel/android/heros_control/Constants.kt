package cz.sazel.android.heros_control

/**
 * Created by wojta on 16.5.14.
 */
object Constants {
    const val GET_IDS_URL = "http://temp.sazel.cz/app/ids.txt"
    const val GCM_API_URL = "https://android.googleapis.com/gcm/send"
    const val GCM_API_KEY = "AIzaSyAzFevc0F8Lwgkc48aerCelNR4cLGhtpEA"

    const val ID = "id"
    const val NAME = "name"
    const val IP = "ip"

    enum class Events {
        BLANK,KEEPALIVE,INSTALL,INSTALL_WITHOUT_QR
    }
}