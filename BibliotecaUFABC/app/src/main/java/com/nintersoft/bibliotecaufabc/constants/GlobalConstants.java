package com.nintersoft.bibliotecaufabc.constants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;

public class GlobalConstants {
    // SharedPreferences options
    public static boolean keepCache = true;
    public static boolean showShare = true;
    public static boolean showExtWarning = true;
    public static boolean storeUserFormData = true;

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

    @SuppressLint("SetJavaScriptEnabled")
    public static void configureStandardWebView(@NonNull WebView v){
        WebSettings webSettings = v.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
    }

    public static void executeScript(WebView v, String js){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            v.evaluateJavascript(js, null);
        else v.loadUrl(js);
    }

    @SuppressWarnings("all")
    public static String getScriptFromAssets(Context context, String filePath) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = context.getResources().getAssets().open(filePath);

            BufferedReader br;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            else br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String str;
            while ((str = br.readLine()) != null) sb.append(str).append("\n");
            br.close();
            return  sb.toString();
        } catch (Exception e){
            return null;
        }
    }

    /*public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }*/
}
