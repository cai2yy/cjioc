package utils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Cai2yy
 * @date 2020/2/22 13:06
 */

public class ScannerTest {

    public static void main(String[] args) {
        List<Class<?>> classes = Scanner.getClasses();
        for (Class<?> clazz : classes) {
            System.out.println(clazz.getSimpleName());
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println("  -> " + method.getName());
            }
        }
    }
}
