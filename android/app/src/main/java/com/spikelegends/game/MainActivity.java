package com.spikelegends.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;

/** Spike Legends for Android: the whole game runs from the bundled page in a full-screen WebView.
 *  Saves live in the WebView's storage (same as the browser version), so they survive app restarts. */
public class MainActivity extends Activity {
  private WebView web;
  private static final String START = "https://appassets.androidplatform.net/assets/www/index.html";

  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    web = new WebView(this);
    web.setBackgroundColor(0xFF000000);
    web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setDatabaseEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    s.setLoadWithOverviewMode(true);
    s.setUseWideViewPort(true);
    s.setSupportZoom(false);
    s.setTextZoom(100);
    s.setCacheMode(WebSettings.LOAD_DEFAULT);
    s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
    final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
    web.setWebViewClient(new WebViewClient() {
      @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) { return loader.shouldInterceptRequest(r.getUrl()); }
      @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
        Uri u = r.getUrl(); if ("appassets.androidplatform.net".equals(u.getHost())) return false;
        try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, u)); } catch (Exception e) {} return true; }
    });
    web.setWebChromeClient(new WebChromeClient());
    setContentView(web);
    immersive();
    if (b != null) web.restoreState(b); else web.loadUrl(START);
  }
  private void immersive() {
    web.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
  }
  @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }
  @Override protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }
  @Override protected void onPause() { super.onPause(); web.onPause(); web.evaluateJavascript("try{Save.store()}catch(e){}", null); }
  @Override protected void onResume() { super.onResume(); web.onResume(); immersive(); }
  @Override public void onBackPressed() {
    web.evaluateJavascript("(function(){try{var m=document.getElementById('menu');return (window.game&&game.teams&&game.teams.L&&m&&m.classList.contains('hidden'))?'match':'menu'}catch(e){return 'menu'}})()", v -> {
      new AlertDialog.Builder(this).setTitle("Spike Legends").setMessage("Quit the game?")
        .setPositiveButton("Quit", (d, w) -> finish()).setNegativeButton("Keep playing", null).show(); });
  }
}
