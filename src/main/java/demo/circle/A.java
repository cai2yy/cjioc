package demo.circle;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Cai2yy
 * @date 2020/3/20 21:58
 */

@Singleton
public class A {

    @Inject
    B b;

    int val = 5;

    public A() {
        System.out.println("A创建成功了");
    }

}
