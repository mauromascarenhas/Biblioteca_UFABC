package com.nintersoft.bibliotecaufabc.utilities;

@SuppressWarnings("WeakerAccess")
public class GlobalConstants {
    // Intent constants for Setters and Getters
    public static final String NOTIFICATION_ID = "notification_id";
    public static final String NOTIFICATION_MESSAGE = "notification_message";

    public static final String CONNECTED_STATUS_USER_NAME = "connected_status_user_name";

    // Notification channel constants
    public static final String CHANNEL_SYNC_ID = "SYNC_NOTIFICATION_CHANNEL";
    static final String CHANNEL_RENEWAL_ID = "RENEWAL_NOTIFICATION_CHANNEL";

    public static final String SYNC_INTENT_SCHEDULED = "is_sync_scheduled";

    // URL constants
    public static final String URL_ACCESS_PAGE = "https://acesso.ufabc.edu.br/";
    public static final String URL_LIBRARY_HOME = "http://biblioteca.ufabc.edu.br/mobile/busca.php";
    public static final String URL_LIBRARY_LOGIN = "http://biblioteca.ufabc.edu.br/login.php";
    public static final String URL_LIBRARY_LOGOUT = "http://biblioteca.ufabc.edu.br/mobile/logout.php";
    public static final String URL_LIBRARY_SEARCH = "http://biblioteca.ufabc.edu.br/mobile/resultado.php";
    public static final String URL_LIBRARY_NEWEST = "http://biblioteca.ufabc.edu.br/mobile/resultado.php?busca=3";
    public static final String URL_LIBRARY_RENEWAL = "http://biblioteca.ufabc.edu.br/mobile/renovacoes.php";
    public static final String URL_LIBRARY_DETAILS = "http://biblioteca.ufabc.edu.br/mobile/detalhe.php";
    public static final String URL_LIBRARY_RESERVE = "http://biblioteca.ufabc.edu.br/mobile/reservar.php";
    public static final String URL_LIBRARY_BOOK_COVER = "http://biblioteca.ufabc.edu.br/mobile/capa.php";
    public static final String URL_LIBRARY_RESERVATION = "http://biblioteca.ufabc.edu.br/mobile/reservas.php";
    public static final String URL_LIBRARY_PERFORM_RENEWAL = "http://biblioteca.ufabc.edu.br/mobile/renovar.php";
    public static final String URL_LIBRARY_CANCEL_RESERVATION = "http://biblioteca.ufabc.edu.br/mobile/cancelar_reserva.php";

    public static final String MANDATORY_APPEND_URL_LIBRARY_DETAILS = "&tipo=1&detalhe=0";
    public static final String MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER = "?obra=";

    public static final int ACTIVITY_LOGIN_REQUEST_CODE = 11;
    public static final int ACTIVITY_SEARCH_REQUEST_CODE = 12;
    public static final int ACTIVITY_RENEWAL_REQUEST_CODE = 13;
    public static final int ACTIVITY_SETTINGS_REQUEST_CODE = 14;
    public static final int ACTIVITY_SEARCH_FILTER_REQUEST_CODE = 15;

    public static final int SYNC_NOTIFICATION_ID = 9000;
    public static final int SYNC_NOTIFICATION_UPDATE_ID = 9001;
    public static final int SYNC_NOTIFICATION_REVOKED_ID = 9002;

    public static final int SYNC_EXECUTIONER_INTENT_ID = 10000;
    public static final int SYNC_EXECUTIONER_INTENT_RETRY_ID = 10010;
    public static final int SYNC_PERMISSION_REQUEST_ID = 10001;
}
