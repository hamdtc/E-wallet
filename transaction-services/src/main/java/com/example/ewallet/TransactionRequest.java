package com.example.ewallet;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {

    private String fromUser;
    private String toUser;
    private int amount;
}
