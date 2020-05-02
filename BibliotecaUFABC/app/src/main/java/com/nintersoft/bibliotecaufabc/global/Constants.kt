package com.nintersoft.bibliotecaufabc.global

import android.content.ComponentName
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import com.nintersoft.bibliotecaufabc.R

object Constants {
    // Intent constants for Setters and Getters
    const val NOTIFICATION_ID = "notification_id"
    const val NOTIFICATION_MESSAGE = "notification_message"

    //WorkManager constants
    const val WORK_SYNC_NOTIFICATION = "w_sync_notification"
    const val WORK_SYNC_SERVICE_WORKER = "w_sync_service_worker"
    const val WORK_RENEWAL_NOTIFICATION_TAG = "w_renewal_notification_tag"

    const val SEARCH_QUERY = "query"
    const val SEARCH_FILTER_TYPE = "filter_type"
    const val SEARCH_FILTER_FIELD = "filter_field"
    const val SEARCH_FILTER_LIBRARY = "filter_library"

    const val BOOK_CODE = "code"
    const val BOOK_QUERY_PARAMETER = "codigo"

    // Bundle constants for Setters and Getters
    const val LOGIN_TO_FORM_USERNAME = "username"
    const val LOGIN_TO_FORM_PASSWORD = "password"
    const val LOGIN_TO_FORM_USERNAME_ERROR = "usernameMsg"
    const val LOGIN_TO_FORM_PASSWORD_ERROR = "passwordMsg"

    const val CONNECTED_STATUS_USER_NAME = "connected_status_user_name"

    // Notification channel constants
    const val CHANNEL_SYNC_ID = "SYNC_NOTIFICATION_CHANNEL"
    const val CHANNEL_RENEWAL_ID = "RENEWAL_NOTIFICATION_CHANNEL"

    const val SYNC_INTENT_SCHEDULED = "is_sync_scheduled"

    // URL Constants
    const val URL_LIBRARY_HOME = "http://biblioteca.ufabc.edu.br/mobile/busca.php"
    const val URL_LIBRARY_LOGIN_P = "http://biblioteca.ufabc.edu.br/mobile/login.php"
    const val URL_LIBRARY_LOGOUT = "http://biblioteca.ufabc.edu.br/mobile/logout.php"
    const val URL_LIBRARY_SEARCH = "http://biblioteca.ufabc.edu.br/mobile/resultado.php"
    const val URL_LIBRARY_NEWEST = "http://biblioteca.ufabc.edu.br/mobile/resultado.php?busca=3"
    const val URL_LIBRARY_RENEWAL = "http://biblioteca.ufabc.edu.br/mobile/renovacoes.php"
    const val URL_LIBRARY_DETAILS = "http://biblioteca.ufabc.edu.br/mobile/detalhe.php"
    const val URL_LIBRARY_RESERVE = "http://biblioteca.ufabc.edu.br/mobile/reservar.php"
    const val URL_LIBRARY_SERVICES = "http://biblioteca.ufabc.edu.br/mobile/servicos.php"
    const val URL_LIBRARY_BOOK_COVER = "http://biblioteca.ufabc.edu.br/mobile/capa.php"
    const val URL_LIBRARY_RESERVATION = "http://biblioteca.ufabc.edu.br/mobile/reservas.php"
    const val URL_LIBRARY_PERFORM_RENEWAL = "http://biblioteca.ufabc.edu.br/mobile/renovar.php"
    const val URL_LIBRARY_CANCEL_RESERVATION = "http://biblioteca.ufabc.edu.br/mobile/cancelar_reserva.php"

    const val MANDATORY_APPEND_URL_LIBRARY_DETAILS = "&tipo=1&detalhe=0"
    const val MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER = "?obra="

    const val SYNC_EXECUTIONER_FIRST_DELAY_MIN : Long = 15
    const val SYNC_REMINDER_NOTIFICATION_INTERVAL: Long = 5

    const val ACTIVITY_LOGIN_REQUEST_CODE = 11
    const val ACTIVITY_RENEWAL_REQUEST_CODE = 13
    const val ACTIVITY_SEARCH_FILTER_REQUEST_CODE = 15

    const val SYNC_NOTIFICATION_ID = 9000
    const val SYNC_NOTIFICATION_UPDATE_ID = 9001
    const val SYNC_NOTIFICATION_REVOKED_ID = 9002
    const val SYNC_NOTIFICATION_REMINDER_ID = 9003

    const val SYNC_PERMISSION_REQUEST_ID = 10001

    val BOOK_COVER_PLACEHOLDERS = listOf(R.drawable.ic_book_cover_fill_01,
        R.drawable.ic_book_cover_fill_02, R.drawable.ic_book_cover_fill_03)

    // Sync Worker Params
    val SYNC_CONSTRAINTS = Constraints.Builder().apply {
        setRequiresCharging(false)
        setRequiresBatteryNotLow(true)
        setRequiresDeviceIdle(false)
        setRequiresStorageNotLow(false)
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    @Suppress("SpellCheckingInspection")
    val POWER_MANAGER_INTENTS = listOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.letv.android.letvsafe",
            "com.letv.android.letvsafe.AutobootManageActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter",
            "com.coloros.safecenter.startupapp.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe",
            "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure",
            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure",
            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager",
            "com.asus.mobilemanager.entry.FunctionActivity")).
            setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    )
}