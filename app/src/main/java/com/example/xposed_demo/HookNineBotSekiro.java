package com.example.xposed_demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.virjar.sekiro.api.SekiroClient;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;

import java.util.Objects;
import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

//import com.example.xposedhook.NineBotSearch;


public class HookNineBotSekiro implements IXposedHookLoadPackage {

    /*
    * 使用sekiro调用so文件， 共有两个方法，分别是加密和解密
    * */

    private String hookPackageName = "cn.ninebot.ninebot";
    private String host = "52.82.6.197";
    private String groupName = "ninebot";
    private String actionName = "checkcode";
    private String clientId = UUID.randomUUID().toString();
    private Object encryptClassObj = null;
    private String Tag = "nine log: ";

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
                Log.e(Tag, "hook到了九号加电");
                XposedBridge.log("hook到了九号加电");

                if (encryptClassObj == null) {
                    Class clazz = lpparam.classLoader.loadClass("com.bangcle.comapiprotect.CheckCodeUtil");

                    XposedHelpers.findAndHookMethod(clazz, "checkcode", String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Object[] args = param.args;
                        Object result = param.getResult();
                        Log.e(Tag, "args is  " + JSON.toJSONString(args));
                        Log.e(Tag, "result is  " + JSON.toJSONString(result));

                        if (param.thisObject != null) {
                            encryptClassObj = param.thisObject;
                            /*解决函数调用问题*/
                            //连接服务端并且注册处理的handler
                            try {
                                final SekiroClient sekiroClient = SekiroClient.start(host, clientId, groupName);
                                sekiroClient.registerHandler(actionName, new SekiroRequestHandler() {
                                    @Override
                                    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
                                        String query = sekiroRequest.getString("query");
                                        String res = JSON.toJSONString(XposedHelpers.callMethod(encryptClassObj, "checkcode", query, 1));
                                        XposedBridge.log(Tag + "query: " + query + ", result: " + res);
                                        sekiroResponse.success(res);
                                    }
                                });
                                // decheckcode
                                final SekiroClient sekiroClient1 = SekiroClient.start(host, clientId, groupName);
                                sekiroClient1.registerHandler("de" + actionName, new SekiroRequestHandler() {
                                    @Override
                                    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
                                        String query = sekiroRequest.getString("query");
                                        String res = JSON.toJSONString(XposedHelpers.callMethod(encryptClassObj, "decheckcode", query));
                                        Object res_json = JSON.parse(res);
                                        XposedBridge.log(Tag + "decheckcode query: " + query + ", result: " + res_json);
                                        sekiroResponse.success(res_json);
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(Tag, Objects.requireNonNull(e.getMessage()));
                            }

                            XposedBridge.log("=========== sekiro服务启动成功 ===========");
                        }
                        XposedBridge.log("方法1中hook成功");
                        }
                    });
                }
                }
            });
        }
    }
    /*
    hook逻辑结束
    */
}
