package com.example.ewallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {

    User findByUserName(String userName); //Define in

    List<User> findAllByUserNameAndAge(String userName,int age);
    boolean existsByUserName(String userName);
}