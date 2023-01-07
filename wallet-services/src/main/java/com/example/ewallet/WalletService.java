package com.example.ewallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    private final String REDIS_PREFIX_USER = "wallet::";

    @KafkaListener(topics = {"create_wallet"},groupId = "avengers")
    public void createWallet(String message) throws JsonProcessingException {

        JSONObject jsonObject = objectMapper.readValue(message,JSONObject.class);
        String userName = (String) jsonObject.get("username");

        Wallet wallet = Wallet.builder().
                userName(userName).
                balance(2000).
                build();

        walletRepository.save(wallet);

        saveInCache(wallet);

    }
        public void saveInCache(Wallet wallet){
        Map map = objectMapper.convertValue(wallet,Map.class);
        redisTemplate.opsForHash().putAll(REDIS_PREFIX_USER+wallet.getUserName(),map);
        redisTemplate.expire(REDIS_PREFIX_USER+wallet.getUserName(), Duration.ofHours(12));
    }

    @KafkaListener(topics={"create_transaction"},groupId = "avengers")
    public void updateWallet(String message) throws JsonProcessingException {

        JSONObject jsonObject = objectMapper.readValue(message,JSONObject.class);
        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");
        int amount = (int) jsonObject.get("amount");
        String transactionId = (String) jsonObject.get("transactionId");

        Wallet sender = walletRepository.findByUserName(fromUser);
        Wallet receiver = walletRepository.findByUserName(toUser);
        int balance = sender.getBalance();

        JSONObject transactionObject = new JSONObject();

        if(balance>=amount){

            Wallet fromWallet = walletRepository.findByUserName(fromUser);
            fromWallet.setBalance(balance - amount);
            walletRepository.save(fromWallet);
            saveInCache(sender);

            Wallet toWallet = walletRepository.findByUserName(toUser);
            toWallet.setBalance(balance + amount);
            walletRepository.save(toWallet);
            saveInCache(receiver);

            transactionObject.put("status","SUCCESS");
            transactionObject.put("transactionId",transactionId);
        }
        else{
            transactionObject.put("status","FAILED");
            transactionObject.put("transactionId",transactionId);
        }

        String ack = transactionObject.toString();
        kafkaTemplate.send("update_transaction",ack);
    }



    public int getBalance(String userName) {

//      Wallet wallet=  walletRepository.findByUserName(userName);
//      return wallet.getBalance();

        Map map=redisTemplate.opsForHash().entries(REDIS_PREFIX_USER+userName);

        if(map==null || map.size()==0){
            // cache miss -> search in DB
            Wallet wallet = walletRepository.findByUserName(userName);

            if(wallet!=null){
                saveInCache(wallet);
            }
            return wallet.getBalance();
        }
        else
            return objectMapper.convertValue(map,Wallet.class).getBalance();
    }
}
