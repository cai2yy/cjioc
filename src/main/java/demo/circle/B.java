package demo.circle;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Cai2yy
 * @date 2020/3/20 21:58
 */
@Singleton
public class B {

    @Inject
    A a;

    public B() {
        System.out.println("B创建成功了");
    }

    public void printA() {
        System.out.println(a.val);
    }

}
