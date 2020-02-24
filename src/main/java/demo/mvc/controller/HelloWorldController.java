package demo.mvc.controller;

import demo.mvc.service.UserService;

import javax.inject.Inject;

/**
 * @author Cai2yy
 * @date 2020/2/12 17:55
 */

public class HelloWorldController {

    @Inject
    private UserService userService;

    public void createAccount() {
        userService.addUser();
        System.out.println("Controller层完成操作");
    }


}
