package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase para manejar la sesión del usuario
 */
public class SessionManager {
    private static final String PREF_NAME = "ProyectosAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    /**
     * Constructor
     */
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Crea la sesión de login
     */
    public void createLoginSession(int userId, String username, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    /**
     * Obtiene los datos del usuario logueado
     * @return array con [userId, username, email]
     */
    public Object[] getUserDetails() {
        Object[] userData = new Object[3];
        userData[0] = pref.getInt(KEY_USER_ID, -1);
        userData[1] = pref.getString(KEY_USERNAME, null);
        userData[2] = pref.getString(KEY_EMAIL, null);
        return userData;
    }

    /**
     * Verifica si el usuario está logueado
     * @return true si está logueado, false si no
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtiene el ID del usuario logueado
     * @return ID del usuario o -1 si no hay usuario logueado
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    /**
     * Cierra la sesión del usuario
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }
}