package com.poetry.hearing.service;

import com.poetry.hearing.dao.UserMapper;
import com.poetry.hearing.domain.User;
import com.poetry.hearing.domain.UserExample;
import com.poetry.hearing.util.Constant;
import com.poetry.hearing.util.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    public Msg hasLogined(HttpServletRequest request){
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user != null) {
            return Msg.success().add(Constant.MSG_IS_LOGINED, true).add(Constant.MSG_LOGINING_USER, user);
        }

        if (redisService.getUserCache() == null){
            //登录or注册
            return Msg.success().add(Constant.MSG_IS_LOGINED, false);
        }
        user = redisService.getUserCache();
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(user.getEmail());
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0){
            //登录or注册
            return Msg.success().add(Constant.MSG_IS_LOGINED, false);
        }
        redisService.setUserCache(user);
        return Msg.success().add(Constant.MSG_IS_LOGINED, true).add(Constant.MSG_LOGINING_USER, users.get(0));
    }

    public Msg updateUserInfo(String email, String name, String autograph) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);
        User user = new User();
        user.setName(name);
        user.setAutograph(autograph);
        if (userMapper.updateByExampleSelective(user, example) > 0) {
            return Msg.success().add(Constant.MSG_LOGINING_USER, userMapper.selectByExample(example).get(0));
        }
        return Msg.fail();
    }

    public Msg login(String name, String passwd, boolean loginkeeping){
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andNameEqualTo(name);
        criteria.andPasswdEqualTo(passwd);
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0){
            //无用户信息
            return Msg.fail().add(Constant.LOGIN_REGISTER_STATUS, false);
        }
        if (loginkeeping) {
            redisService.setUserCache(users.get(0));
        } else {
            redisService.clearUserCache();
        }
        return Msg.success().add(Constant.LOGIN_REGISTER_STATUS, true).add(Constant.MSG_LOGINING_USER, users.get(0));
    }

    public Msg register(User user){
        try {
            userMapper.insert(user);
        } catch (Exception e){
            return Msg.success().add(Constant.LOGIN_REGISTER_STATUS, false);
        }
        redisService.setUserCache(user);
        return Msg.success().add(Constant.LOGIN_REGISTER_STATUS, true).add(Constant.MSG_LOGINING_USER, user);
    }
}
