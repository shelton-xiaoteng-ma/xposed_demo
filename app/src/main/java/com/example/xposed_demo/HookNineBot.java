package com.example.xposed_demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import com.alibaba.fastjson.JSON;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HookNineBot implements IXposedHookLoadPackage {
    /*
    * demo用于简单hook方法来查看输入和输出
    * */

    private String hookPackageName = "cn.ninebot.ninebot";
    private String clientId = UUID.randomUUID().toString();
    private Object encryptClassObj = null;
    private String Tag = "nine";

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

                    Class clazz = lpparam.classLoader.loadClass("cn.ninebot.lib.network.interceptor.BaseParametersInterceptor");

                    XposedHelpers.findAndHookMethod(clazz, "convertJsonEncryption", String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            Object[] args = param.args;
                            Object result = param.getResult();
                            Log.e(Tag, "args is " + JSON.toJSONString(args));
                            Log.e(Tag, "result is  " + JSON.toJSONString(result));
                            Log.e(Tag, "方法1中hook成功");
                        }
                    });
                }
            });
        }
         /*
        hook逻辑结束
        */
    }

}
