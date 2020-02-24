package demo.mvc.service;

import demo.mvc.dao.UserMapper;
import demo.mvc.dao.UserRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Cai2yy
 * @date 2020/2/12 18:01
 */

@Singleton
@Named("service")
public class UserService {

    @Inject
    @Singleton
    @Named("dao2")
    private UserRepository userMapper;

    @Inject
    public UserService(@Named("dao2") UserRepository userMapper) {
        this.userMapper = userMapper;
        System.out.println("userService构造器1");
    }

    public void addUser() {
        userMapper.add();
        System.out.println("Service层完成操作");
    }

}
