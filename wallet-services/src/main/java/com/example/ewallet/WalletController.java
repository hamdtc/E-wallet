package com.example.ewallet;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {

    @Autowired
    WalletService walletService;

    @GetMapping("/wallet_balance")
    public int checkBalance(@RequestParam("userName") String userName) throws Exception{
        return walletService.getBalance(userName);
    }

}
