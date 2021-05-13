# Xposed demo

1. /Users/maxiaoteng/projects/xposed_demo/app/src/main/java/com/example/xposed_demo/HookNineBotSekiro.java 构建xposed类
2. /Users/maxiaoteng/projects/xposed_demo/app/src/main/assets/xposed_init 生效
3. /Users/maxiaoteng/projects/xposed_demo/app/src/main/res/values/strings.xml 修改app名称
4. HookNineBotSekiro hook 普通方法
5. HookVzSekiro hook静态方法, 传输`string[]`参数是踩坑
6. sekiro服务调用
