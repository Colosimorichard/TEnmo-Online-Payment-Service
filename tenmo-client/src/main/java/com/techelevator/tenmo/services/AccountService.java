package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;

public class AccountService {

    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;
    // Account service controller
    public AccountService(String url, AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
        BASE_URL = url;
    }
    // Making the request to the server to return the balance
    public BigDecimal getBalance() {
        BigDecimal balance = new BigDecimal(0);
        try {
            balance = restTemplate.exchange(BASE_URL + "balance/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), BigDecimal.class).getBody();
            System.out.println("Your current account balance is: $" + balance);
        } catch (RestClientException e) {
            System.out.println("Error retrieving balance");
        }
        return balance;
    }
    // Creating header and setting the authorization
    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

}