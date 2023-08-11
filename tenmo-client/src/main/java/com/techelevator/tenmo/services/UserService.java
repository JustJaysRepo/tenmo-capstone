package com.techelevator.tenmo.services;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.techelevator.tenmo.model.User;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


//@Service
public class UserService {
    public static final String API_BASE_URL = "http://localhost:8080/";


    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;


    public void setAuthToken(String authToken) {
        this.authToken = authToken;

    }
    public List<User> findAll() {
        String url = API_BASE_URL + "user/";
        ResponseEntity<User[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                User[].class
        );

        User[] userArray = response.getBody();
        return Arrays.asList(userArray);
    }
    public int findIdByUsername(String username) {
        String url = API_BASE_URL + "user/name?username=" + username;
        ResponseEntity<Integer> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                Integer.class
        );

        return response.getBody();
    }

    public User getUserById(long userId) {
        String url = API_BASE_URL + "user/" + userId;
        ResponseEntity<User> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                User.class
        );

        return response.getBody();
    }

    public User findUserByAccountId(long accountId){
        String url = API_BASE_URL + "user/account/" + accountId;
        ResponseEntity<User> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                User.class
        );

        return response.getBody();
    }
    
    public BigDecimal getBalanceByUserId(long userId) {
        String url = API_BASE_URL + "account/" + userId + "/balance";
        ResponseEntity<BigDecimal> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                makeAuthEntity(),
                BigDecimal.class
        );

        return response.getBody();
    }

    //======= needed token for authorization ========///
    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }
    // Helper method to create the HttpEntity with authentication headers and request body
    private HttpEntity<User> makeAuthEntityWithBody(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(user, headers);
    }


}
