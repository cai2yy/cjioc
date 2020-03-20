package demo.circle;

import iockids.Injector;

/**
 * @author Cai2yy
 * @date 2020/3/20 22:00
 */

public class test {

    public static void main(String[] args) {
        Injector injector = new Injector();
        injector.getInstance(demo.circle.A.class);
    }
}
