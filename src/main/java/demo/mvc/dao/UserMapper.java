package demo.mvc.dao;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Cai2yy
 * @date 2020/2/12 17:57
 */

@Singleton
@Named("dao1")
public class UserMapper implements UserRepository {

    public void add() {
        System.out.println("I am UserMapper1");
        System.out.println("在数据库的用户表中插入一条数据");
    }

    public void print() {
        System.out.println("I am UserMapper1");
    }

}
