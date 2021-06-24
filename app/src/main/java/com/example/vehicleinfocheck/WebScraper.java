package com.example.vehicleinfocheck;

import android.webkit.WebView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.webkit.WebViewClient;
import android.view.View;

//Class that extracts the information from a website allowing its display within an app, defining all necessary methods for the same
public class WebScraper {
    private final WebView web;                          //WebView widget to display website
    private final String userAgent;                     //String resource to be extracted from website
    private onPageLoadedListener onpageloadedlistener;  //Similar to an OnClickListener, action performed when website has loaded

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    public WebScraper(final Context context) {
        web = new WebView(context);
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setBlockNetworkImage(true);
        web.getSettings().setLoadsImagesAutomatically(false);

        JSInterface jInterface = new JSInterface();
        web.addJavascriptInterface(jInterface, "HtmlViewer");
        userAgent = web.getSettings().getUserAgentString();
        web.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // Forward the page loaded event
                if (onpageloadedlistener != null) {
                    onpageloadedlistener.loaded(url);
                }

                web.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                web.layout(0, 0, web.getMeasuredWidth(), web.getMeasuredHeight());
                web.setDrawingCacheEnabled(true);
            }
        });
    }

    //Sets the user agent string's Operating System substring to string corresponding to Linux
    public void setUserAgentToDesktop(boolean desktop){
        if (desktop){
            String osString = userAgent.substring(userAgent.indexOf("("), userAgent.indexOf(")") + 1);  //OS substring located and set to 'osString'
            web.getSettings().setUserAgentString(userAgent.replace(osString,"(X11; Linux x86_64)"));    //'osString' replaced
        }else{
            web.getSettings().setUserAgentString(userAgent);    //Default WebView user agent string used
        }
    }
    //This method is called in the WebActivity to display website
    public View getView() {
        return web;     //web is the variable that contains the WebView
    }
    //Loads layout of the webiste in the WebView
    public void setLoadImages(boolean enabled) {
        web.getSettings().setBlockNetworkImage(!enabled);
        web.getSettings().setLoadsImagesAutomatically(enabled);
    }

    public void loadURL(String URL) {
        web.loadUrl(URL);
    }

    private static class JSInterface {
        JSInterface() {
        }
    }
    protected String runJavascript(String task){
        web.evaluateJavascript(task, s -> {
        });
        return null;
    }

    protected void runAsUrl(String task){
        web.loadUrl(task);
    }
    //Find an element by its class name on the website which consists of a string name and integer ID
    public Element findElementByClassName(String classname, int id){
        return new Element(this, "document.getElementsByClassName('" + classname + "')[" + id + "]");
    }
    //Find an element by its ID on the website which is a string value
    public Element findElementById(String id){
        return new Element(this, "document.getElementById('" + id + "')" );
    }

    public void setOnPageLoadedListener(onPageLoadedListener onpageloadedlistener){
        this.onpageloadedlistener = onpageloadedlistener;
    }

    public interface onPageLoadedListener{
        void loaded(String URL);
    }
}
