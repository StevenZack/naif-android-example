package io.github.naife.stevenzack.myapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import naif.Naif;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ImageView imageView;
    private static final String TAG="[[main]]";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        imageView=(ImageView)findViewById(R.id.imageView);
        webView=(WebView)findViewById(R.id.mwv);

        new Thread(new Runnable() {
            @Override
            public void run() {
                File file=new File("/data/data/"+getPackageName()+"/dir/");
                if (!file.exists()){
                    file.mkdirs();
                    copyFileOrDir("dir");
                }
                final long port= Naif.start(file.getAbsolutePath());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.setWebViewClient(new WebViewClient());
                        webView.setWebChromeClient(new WebChromeClient());
                        webView.getSettings().setJavaScriptEnabled(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
                        }
                        webView.setDownloadListener(new DownloadListener() {
                            public void onDownloadStart(String url, String userAgent,
                                                        String contentDisposition, String mimetype,
                                                        long contentLength) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        });
                        webView.loadUrl("http://127.0.0.1:"+String.valueOf(port)+"/");
                        Log.d(TAG, "run: port = "+String.valueOf(port));
                    }
                });
                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void copyFileOrDir(String path) {
        AssetManager assetManager = getResources().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = "/data/data/" + this.getPackageName() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag2", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = getResources().getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag1", e.getMessage());
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack()){
            webView.goBack();
            return true;
        }
        finish();
        return true;
    }
}
