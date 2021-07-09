package com.example.xposed_demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.virjar.sekiro.api.SekiroClient;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

//import com.example.xposedhook.VzSearch;


public class HookQQmusicSekiro implements IXposedHookLoadPackage {

    /*
    * 使用sekiro调用so文件， 共有两个方法，分别是加密和解密
    * */

    private String hookPackageName = "com.tencent.qqmusic";
    private String host = "192.168.0.165";
    private String groupName = "qqmusic";
    private String actionName = "decrypt_response";
    private String clientId = UUID.randomUUID().toString();
    private Object encryptClassObj = null;
    private String Tag = "qqmusic log: ";
    private String server_start = "false";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        /*
        hook逻辑开始
         */

        if (lpparam.packageName.equals(hookPackageName)) {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.e(Tag, "hook到QQ音乐");
                XposedBridge.log("hook到QQ音乐");

                if (encryptClassObj == null) {
                    ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                    final Class<?> clazz = cl.loadClass("com.tencent.qqmusiccommon.util.Util4Common");

                    if (server_start == "false") {
                        /*解决函数调用问题*/
                        //连接服务端并且注册处理的handler
                        try {
                            final SekiroClient sekiroClient = SekiroClient.start(host, clientId, groupName);
                            sekiroClient.registerHandler(actionName, new SekiroRequestHandler() {
                                @Override
                                public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
                                    String query = sekiroRequest.getString("query");
                                    XposedBridge.log("--------------------------------------");
                                    int p2_qqmusic = 5;
                                    byte[] p1_qqmusic = android.util.Base64.decode(query, Base64.DEFAULT);
                                    byte[] bs_qqmusic = (byte[]) XposedHelpers.callStaticMethod(clazz,"decryptData", p1_qqmusic, p2_qqmusic);
                                    String plainText_qqmusic = null;
                                    try {
                                        plainText_qqmusic = new String(bs_qqmusic, "UTF-8");
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(Tag, ("error"+e));
                                        e.printStackTrace();
                                    }
                                    String res = plainText_qqmusic;
                                    XposedBridge.log(Tag + "query: " + query + ", result: " + res);
                                    sekiroResponse.success(res);
                                }
                            });
                            XposedBridge.log("=========== sekiro服务启动成功 ===========");
                            //  server_start = "true";
                        } catch (Exception e) {
                            Log.e(Tag, Objects.requireNonNull(e.getMessage()));
                        }
                    }

//                    XposedHelpers.findAndHookMethod(clazz, "a", String[].class, new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//                        XposedBridge.log("inner");
//
//                        // final Object[] args = param.args;
//                        // Object result = param.getResult();
//                        // Log.e(Tag, args[0].getClass().toString());
//                        // Log.e(Tag, "args is  " + JSON.toJSONString(args));
//                        // Log.e(Tag, "result is  " + JSON.toJSONString(result));
//                        String result = (String) XposedHelpers.callStaticMethod(clazz,"a", param.args[0]);
//                        XposedBridge.log("hook到飞常准:" + result);
//                        // String res = JSON.toJSONString(XposedHelpers.callMethod(clazz, "a", args[0]));
//
//
//                         XposedBridge.log("方法1中hook成功");
//                         }
//                     });
                 }
                }
            });
        }
    }
    /*
    hook逻辑结束
    */

}