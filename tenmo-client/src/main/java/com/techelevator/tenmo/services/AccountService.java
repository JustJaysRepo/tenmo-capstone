package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;

import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class AccountService {
    public static final String API_BASE_URL = "http://localhost:8080/";

    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;

    }

    public Account getAccountByUserID(int userId) {
        String url = API_BASE_URL + "account/user/" + userId;
        ResponseEntity<Account> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                Account.class);

        return response.getBody();
    }

    public boolean updateAccountBalance(int ID, Account account) {
        String url = API_BASE_URL + "account/" + ID;
        boolean success = false;
        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    createAccountEntity(account),
                    Account.class);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;

    }

    private HttpEntity<Account> createAccountEntity(Account account) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(account, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    public Account getAccountByID(int Id) {
        String url = API_BASE_URL + "account/" + Id;
        ResponseEntity<Account> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                Account.class);

        return response.getBody();
    }

}
