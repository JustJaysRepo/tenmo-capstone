package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/user/")
public class UserController {

    private final UserDao userDao;
    @Autowired
    public UserController(UserDao userDao){
        this.userDao=userDao;
    }
    //===endpoint is already set at base =======//
    @GetMapping("")
    public List<User>getAllUsers(){
        return userDao.findAll();
    }
    //======
    @GetMapping("{id}")
    public User getUserById(@PathVariable int id) {
        return userDao.getUserById(id);
    }

    //===endpoint is name?username, Finds users id by their name =======//
    @GetMapping("name")
    public int findByUsername(String username){
        return userDao.findIdByUsername(username);
    }
    @GetMapping("id")
    public int findIdByUsername(@RequestParam String username) {
        return userDao.findIdByUsername(username);
    }
    @PostMapping("")
    public boolean createUser(@RequestBody User user) {
        return userDao.create(user.getUsername(), user.getPassword());
    }

    @GetMapping("account/{accountId}")
    public User findByAccountId(@PathVariable int accountId) {
        User user = userDao.findByAccountId(accountId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
        return user;
        
    }

//    @GetMapping("{id}/balance")
//    public BigDecimal getBalanceByUserId(@PathVariable int id) {
//        return userDao.getBalanceByUserId(id);
//    }

}
