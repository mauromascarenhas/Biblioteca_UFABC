package com.nintersoft.bibliotecaufabc.utilities;

public class GlobalConstants {
    // SharedPreferences options
    public static boolean keepCache = true;
    public static boolean showShare = true;
    public static boolean ringAlarm = true;
    public static boolean showExtWarning = true;
    public static boolean storeUserFormData = true;

    public static int ringAlarmOffset = 0;

    // Intent constants for Getters
    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION_MESSAGE = "notification_message";

    /**
     * Gets notification content as parcelable extra from intent
     * You must use NOTIFICATION_MESSAGE instead
     *
     * @see #NOTIFICATION_MESSAGE
     */
    @Deprecated
    public static String NOTIFICATION_CONTENT = "notification_content";

    // Notification channel constants
    static String CHANNEL_ID = "DEFAULT_NOTIFICATION_CHANNEL";

    // URL constants and Connection-wise variables
    public static boolean isUserConnected = false;

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
}
