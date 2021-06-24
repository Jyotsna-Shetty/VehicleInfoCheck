//This class is not used in this project as the required methods did not work
package com.example.vehicleinfocheck;
//An element is a field present in the website: an input field, a button, etc.

import android.util.Log;

//This class defines all methods pertaining to elements
public class Element {
    private final String elementLocator;    //String to find an element in the website
    private final WebScraper web;           //Instance of the WebScraper class

    Element (WebScraper web, String elementLocator){
        this.web = web;
        this.elementLocator = elementLocator;
    }

    public void setText(String text){
        String task = "javascript:" + elementLocator + ".value='" + text + "';void(0);";
        Log.i("Logmsg",task);
        web.runAsUrl(task);
    }

    public void setAttribute(String attribute,String text){
        String task = "javascript:" + elementLocator + "."+attribute+"='" + text + "';void(0);";
        Log.i("Logmsg",task);
        web.runAsUrl(task);
    }

    public void click(){
        web.runAsUrl("javascript:" + elementLocator + ".click();void(0);");
    }

    public String getName(){
        return web.runJavascript("javascript:window.HtmlViewer.processContent(" + elementLocator + ".name);");
    }
}
