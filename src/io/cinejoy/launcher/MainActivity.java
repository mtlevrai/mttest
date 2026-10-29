package io.cinejoy.launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Lanceur sans interface : récupère l'adresse actuelle du site (fichier url.txt
 * du dépôt GitHub), la mémorise, puis l'ouvre dans TV Bro.
 * Hors ligne ou GitHub injoignable : dernière adresse connue, sinon DEFAULT_URL.
 */
public class MainActivity extends Activity {

    // Remplacé par build.sh ; si le placeholder reste, la récupération distante est ignorée.
    private static final String CONFIG_URL = "__CONFIG_URL__";
    private static final String DEFAULT_URL = "https://cinejoy.pk/";
    private static final String[] BROWSERS = {
            "com.phlox.tvwebbrowser.gecko", "com.phlox.tvwebbrowser"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences prefs = getSharedPreferences("launcher", MODE_PRIVATE);
        new Thread(() -> {
            String url = fetchRemoteUrl();
            if (url != null) {
                prefs.edit().putString("url", url).apply();
            } else {
                url = prefs.getString("url", DEFAULT_URL);
            }
            final String target = url;
            runOnUiThread(() -> open(target));
        }).start();
    }

    private String fetchRemoteUrl() {
        if (!CONFIG_URL.startsWith("https://")) return null;
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(CONFIG_URL).openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(3000);
            c.setUseCaches(false);
            if (c.getResponseCode() != 200) return null;
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                boolean ok = line.startsWith("https://") || line.startsWith("http://");
                return ok ? line : null;
            }
        } catch (Exception e) {
            // hors ligne : on retombe sur l'adresse mémorisée
        } finally {
            if (c != null) c.disconnect();
        }
        return null;
    }

    private void open(String url) {
        if (isFinishing()) return;
        Uri uri = Uri.parse(url);
        boolean started = false;
        for (String pkg : BROWSERS) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, uri)
                        .setPackage(pkg)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                started = true;
                break;
            } catch (ActivityNotFoundException ignored) {
                // essaie le navigateur suivant
            }
        }
        if (!started) {
            Toast.makeText(getApplicationContext(),
                    "TV Bro introuvable : installe-le d'abord", Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
