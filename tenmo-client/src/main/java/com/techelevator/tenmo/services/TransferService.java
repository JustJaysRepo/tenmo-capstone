package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferStatus;
import com.techelevator.util.BasicLogger;

import java.sql.Array;
import java.util.Arrays;

import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


public class TransferService {
    public static final String API_BASE_URL = "http://localhost:8080/transfers";

    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void createTransfer(Transfer transfer) {
        try {
            restTemplate.exchange(API_BASE_URL, HttpMethod.POST, createHttpEntity(transfer), Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }

   public boolean updateTransfer(Transfer transfer) {
    String url = API_BASE_URL + "/" + transfer.getTransfer_id() + "/status/" + transfer.getTransfer_status();
    try {
        restTemplate.exchange(url, HttpMethod.PUT, createHttpEntity(null), Void.class);
        return true;
    } catch (RestClientResponseException | ResourceAccessException e) {
        BasicLogger.log(e.getMessage());
        return false;
    }
}


    public Transfer getTransferById(int id) {
        String url = API_BASE_URL + "/" + id;

        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(null), Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public Transfer[] getAllUserTransfers() {
        return getTransfers(API_BASE_URL);
    }

    public Transfer[] getUserTransferByStatus(TransferStatus status) {
        String url = API_BASE_URL + "?status=" + status.desc;
        return getTransfers(url);
    }

    private Transfer[] getTransfers(String url) {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(url, HttpMethod.GET, createHttpEntity(null), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    private <T> HttpEntity<T> createHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
