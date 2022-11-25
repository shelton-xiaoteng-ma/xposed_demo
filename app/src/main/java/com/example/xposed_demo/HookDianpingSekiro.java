package com.example.xposed_demo;

import com.alibaba.fastjson.JSON;
import com.virjar.sekiro.api.SekiroClient;
import com.virjar.sekiro.api.SekiroRequest;
import com.virjar.sekiro.api.SekiroRequestHandler;
import com.virjar.sekiro.api.SekiroResponse;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class HookDianpingSekiro implements IXposedHookLoadPackage {

    /*
    * 使用sekiro调用so文件， 共有两个方法，分别是加密和解密
    * */

    private String hookPackageName = "com.dianping.v1";
    private String host = "52.82.6.197";
    private String groupName = "dianping";
    private String actionName = "callc";
    private String clientId = UUID.randomUUID().toString();
    private Object encryptClassObj = null;
    private String Tag = "dianping log: ";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        /*
        hook逻辑开始
         */

        if (lpparam.packageName.equals(hookPackageName)) {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.meituan.android.common.mtguard.NBridge", lpparam.classLoader), "main", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("hook到了 NBridge");
                    super.afterHookedMethod(param);
                    XposedBridge.log("param.thisObject != null"+param.thisObject);

                    if (XposedHelpers.findClass("com.meituan.android.common.mtguard.NBridge", lpparam.classLoader) != null) {
                        final Class classz = XposedHelpers.findClass("com.meituan.android.common.mtguard.NBridge", lpparam.classLoader);
                        /*解决函数调用问题*/
                        //连接服务端并且注册处理的handler
                        XposedBridge.log("=========== sekiro服务启动 ===========");
                        try {
                            final SekiroClient sekiroClient = SekiroClient.start(host, clientId, groupName);
                            sekiroClient.registerHandler("execute", new SekiroRequestHandler() {
                                @Override
                                public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
                                    String sign = sekiroRequest.getString("sign");
                                    String host = sekiroRequest.getString("host");
                                    XposedBridge.log("execute sign: " + sign + ", host: " + host);
                                    Object arg2[] = new Object[3];
                                    String input2_1 = "4069cb78-e02b-45f6-9f0a-b34ddccf389c";
                                    byte[] input2_2 = sign.getBytes();
                                    byte[] input2_3 = host.getBytes();
                                    arg2[0] = input2_1;
                                    arg2[1] = input2_2;
                                    arg2[2] = input2_3;
                                    Object res_origin = XposedHelpers.callStaticMethod(classz, "main", 2, arg2);
                                    String res = new String();
                                    if(res_origin != null){
                                        res = JSON.toJSONString(res_origin);
                                    }
                                    XposedBridge.log(Tag + "execute sign: " + sign + ", host: " + host + ", result: " + res);
                                    sekiroResponse.success(res);
                                }
                            });
                        } catch (Exception e) {
                            XposedBridge.log(Objects.requireNonNull(e.getMessage()));
                        }

                        XposedBridge.log("=========== sekiro服务启动成功 ===========");
                    }
                    Object[] args = param.args;
                    XposedBridge.log("args is  ");
                    XposedBridge.log(Arrays.toString((byte[]) args[0]));
                    XposedBridge.log(JSON.toJSONString(args[1]));
                    XposedBridge.log(JSON.toJSONString(args[2]));
                    Object result = param.getResult();
                    XposedBridge.log("result is  "+ JSON.toJSONString(result));
                }
            });
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.meituan.android.common.mtguard.NBridge", lpparam.classLoader), "main", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("hook到了main");
                    Object[] args = param.args;
                    XposedBridge.log("main args is  " + args[0]);
                    Object result = param.getResult();
                    super.beforeHookedMethod(param);
                }
            });
        }
    }
    /*
    hook逻辑结束
    */
}
