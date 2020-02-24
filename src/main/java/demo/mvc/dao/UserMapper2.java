package demo.mvc.dao;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Cai2yy
 * @date 2020/2/15 0:55
 */

@Singleton
@Named("dao2")
public class UserMapper2 implements UserRepository {

    public void add() {
        System.out.println("I am UserMapper2");
        System.out.println("在数据库的用户表中插入一条数据2");
    }

    public void print() {
        System.out.println("I am UserMapper2");
    }
}
