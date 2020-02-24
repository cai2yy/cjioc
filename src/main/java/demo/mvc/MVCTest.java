package demo.mvc;

import iockids.Injector;
import demo.mvc.controller.HelloWorldController;

/**
 * @author Cai2yy
 * @date 2020/2/12 18:08
 */

public class MVCTest {

    public static void main(String[] args) throws ClassNotFoundException {
        var injector = new Injector();
        // 启动时扫描目录下所有java文件，并使容器加载所有Beans到qualifiedClasses列表
        /* 这一步对于初始化1接口-N实现的实例注入而言是必不可少的，否则需要用户手动注册（如下所示）:
            Class<?> parentClazz = Class.forName("UserRepository");
            Class<?> clazz = Class.forName("UserMapper");
            injector.registerQualifiedClass(parentClazz, clazz);
         */
        injector.printQualifiedClasses();
        System.out.println("------------------");

        // 调用Bean，容器将递归进行依赖注入
        var hello = injector.getInstance(HelloWorldController.class);
        // 直接创建Controller类并执行方法则会报错
        // HelloWorldController hello = new HelloWorldController();
        hello.createAccount();
    }
}
