package demo;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cai2yy
 * @date 2020/2/15 10:20
 */

public class test {

    @Named("yes, 1")
    public void named1() {
        System.out.println("named1");
    }

    @Named("no, 2")
    public void named2() {
        System.out.println("named2");
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName("demo.test");
        Method method1 = clazz.getMethod("named1");
        Method method2 = clazz.getMethod("named2");
        Annotation[] annos1 = method1.getDeclaredAnnotations();
        Annotation[] annos2 = method2.getDeclaredAnnotations();
        System.out.println(annos1[0]);
        System.out.println(annos2[0]);
        Set<Annotation> set = new HashSet<>();
        set.add(annos1[0]);
        set.add(annos2[0]);
        System.out.println(set.size());
    }
}
