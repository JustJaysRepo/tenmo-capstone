package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    //=============new user service ============//
    private final UserService userService = new UserService();
    //=============new account service ============//
    private final AccountService accountService = new AccountService();
    private final TransferService transferService = new TransferService();
    //================ Authenticated user ========//
    private AuthenticatedUser currentUser;
    private final RestTemplate restTemplate = new RestTemplate();
    //======trying to handle the login early in the code for convenience(check handle login)======//
    public static String AUTH_TOKEN = "";
    public static String USERNAME = "";
    public static int ID = 0;
//========================================================================//


    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    //=========new login to receive your bearer token/username/and id in the terminal========//
    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser != null) {
            //========set token/username/and id as variables (for use in later code)=======//
            App.AUTH_TOKEN = currentUser.getToken();
            App.USERNAME = currentUser.getUser().getUsername();
            App.ID = currentUser.getUser().getId();

            //set up token for services so we don't need to repeat
            userService.setAuthToken(App.AUTH_TOKEN);
            accountService.setAuthToken(App.AUTH_TOKEN);
            transferService.setAuthToken(App.AUTH_TOKEN);

            //==================== login print outs ==========================================//
            System.out.println("Login successful.");
            System.out.println(/*"Authentication Token: " + App.AUTH_TOKEN + "\n" +*/
                    "UserName: " + App.USERNAME + "\n" +
                    "User_ID: " + App.ID);
        } else {
            consoleService.printErrorMessage();
        }
    }


    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {


//        userService.setAuthToken(App.AUTH_TOKEN);
        long userId = currentUser.getUser().getId();
        BigDecimal balance = userService.getBalanceByUserId(userId);
        System.out.println("Your current account balance is: $" + balance);
    }

    private void viewTransferHistory() {
    // Get all the transfers
    Transfer[] transfers = transferService.getAllUserTransfers();
    if (transfers.length == 0) {
        System.out.println("No transfers registered currently.");
        return;
    }

    // Print the transfers
    System.out.println("Here is your transfer history:");
    printTransfers(transfers);

    // grabs transfer ID
    int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
    if (transferId == 0) {
        return;
    }
    
    // Check if entered transferId exists in our list of transfers
    for (Transfer transfer : transfers) {
        if (transfer.getTransfer_id() == transferId) {
            printTransferDetail(transfer);
            return;
        }
    }
    
    // If we get here, it means the entered id was not found in our list of transfers
    System.out.println("Transfer ID not found. Please try again.");
}


    private void viewPendingRequests() {
        // Get all the transfers
        Transfer[] pendingTransfers = transferService.getUserTransferByStatus(TransferStatus.PENDING);

        // Check if there are no pending transfers
        if (pendingTransfers.length == 0) {
            System.out.println("You currently have no pending transfers.");
            return;
        }

        // Print the services
        System.out.println("Here are your pending transfers:");
        printTransfers(pendingTransfers);

        // Prompt user to select a transfer ID for approval
        int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject/ignore (0 to cancel): ");
        if (transferId == 0) {
            // Cancel option selected, return to the main menu
            return;
        }
        // Retrieve the transfer by ID
        Transfer transfer = transferService.getTransferById(transferId);

        // Check if the transfer does not exist
        if (transfer == null) {
            consoleService.printErrorMessage();
            consoleService.printErrorMessage_custom("ERROR, PLEASE ENSURE THAT TRANSFER I.D. is VALID ");
            return;
        }

        // Check the status of the transfer
        TransferStatus status = transfer.getTransfer_status();

        // Check if the transfer is not pending
        if (status != TransferStatus.PENDING) {
            consoleService.printErrorMessage();
            return;
        }

        // Retrieve the source and destination accounts
        Account fromAccount = accountService.getAccountByID(transfer.getAccount_from());
        Account toAccount = accountService.getAccountByID(transfer.getAccount_to());

        // Check if either the source or destination account is null
        if (fromAccount == null || toAccount == null) {
            consoleService.printErrorMessage();
            return;
        }

        // Check if the user is authorized to approve or reject the transfer
        int userId = App.ID; // Replace with the code to get the current user's ID
        if (userId == toAccount.getUser_id() ) {
            System.out.println("User is not authorized to approve or reject their own transfer request.");
            return;
        }

        // Prompt for action: Approve, Reject, or Don't approve or reject
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: (Ignore)Don't approve or reject");
        System.out.println("---------");
        int option = consoleService.promptForInt("Please choose an option: ");

        if (option == 1) { // Approve

            transfer.setTransfer_status(TransferStatus.APPROVED);

            // Update the transfer in the database
            if (transferService.updateTransfer(transfer)) {
                // Update account balances
                // Update the source account balance
                updateAccountBalance(fromAccount.getUser_id(), fromAccount, transfer.getAmount().negate());
                // Update the destination account balance
                updateAccountBalance(toAccount.getUser_id(), toAccount, transfer.getAmount());


                System.out.println("Transfer approved successfully. Account balances updated.");
            } else {
                consoleService.printErrorMessage();
            }
        } else if (option == 2) { // Reject
            // Update transfer status to 'REJECTED'
            transfer.setTransfer_status(TransferStatus.REJECTED);

            // Update the transfer in the database
            if (transferService.updateTransfer(transfer)) {
                System.out.println("Transfer rejected successfully.");
            } else {
                consoleService.printErrorMessage();
            }
        } else { // Don't approve or reject
            System.out.println("No action taken. Transfer remains pending.");
        }

    }


    private void printTransfers(Transfer[] transfers) {
        int headerLength = 64;// Length of the header text including spaces and dashes
        String dashedLine = "-".repeat(headerLength);
        System.out.println(dashedLine);
        System.out.println("Transfers");
        System.out.printf("%-10s %-40s %s\n", "ID", "From/To", "Amount");
        System.out.println(dashedLine);



        for (Transfer transfer : transfers) {
            User sender = userService.findUserByAccountId(transfer.getAccount_from());
            User receiver = userService.findUserByAccountId(transfer.getAccount_to());

            // Check if sender and receiver exist
            if (sender == null || receiver == null) {
                consoleService.printErrorMessage();
                consoleService.printErrorMessage_custom("Make Sure Sender or Receiver Accounts Are Valid.");
                return;
            }

            String fromTo = sender.getUsername() + "/" + receiver.getUsername();
            System.out.printf("%-10d %-40s $%.2f\n",
                    transfer.getTransfer_id(),
                    fromTo,
                    transfer.getAmount());
        }


        System.out.println(dashedLine);
    }

    private void printTransferDetail(Transfer transfer) {
        User sender = userService.findUserByAccountId(transfer.getAccount_from());
        User receiver = userService.findUserByAccountId(transfer.getAccount_to());

        System.out.println("--------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("--------------------------------------------");
        System.out.println("Id: " + transfer.getTransfer_id());
        System.out.println("From: " + sender.getUsername());
        System.out.println("To: " + receiver.getUsername());
        System.out.println("Type: " + transfer.getTransfer_type().desc);
        System.out.println("Status: " + transfer.getTransfer_status().desc);
        System.out.printf("Amount: $%.2f\n", transfer.getAmount());
    }



    private void sendBucks() {
        // Get the list of users
        List<User> users = userService.findAll();

        Account myAccount = accountService.getAccountByUserID(App.ID);

        // Print the list of users
        System.out.println("Available Users:");
        for (User user : users) {
            System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername());
        }

        // Prompt for the user ID to send money to
        int userTo = consoleService.promptForUserID();
        if (userTo == 0) {
            // Cancel option selected, return to the main menu
            return;
        }

        // Check if the entered user ID is valid
        boolean isValidUser = false;
        for (User user : users) {
            if (user.getId() == userTo) {
                isValidUser = true;
                break;
            }
        }

        if (!isValidUser) {
            System.out.println("Invalid user ID. Please enter a valid user ID.");
            return;
        }



        // Check if the user is trying to send money to themselves
        if (userTo == App.ID) {
            consoleService.printErrorMessage_custom("Sender And recipient Accounts Must Be Different.");
            return;
        }

        // Retrieve the account of the user to send money to
        Account userToAccount = accountService.getAccountByUserID(userTo);



        // Prompt for the amount to send
        BigDecimal amount = consoleService.promptForAmount();

        // Check if the amount is valid (greater than zero)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            consoleService.printErrorMessage_custom("Amount Is Not valid: Must Be Greater Than Zero.");
            return;
        }

        // Check if the current user has sufficient balance to send the amount
        if (amount.compareTo(myAccount.getBalance()) > 0) {
            consoleService.printErrorMessage_custom("Insufficient Balance To Send the Specified Amount.");
            return;
        }
        //trying something new //
       // Update account balance for current user and userTo
        updateAccountBalance(App.ID, myAccount, amount.negate());
        updateAccountBalance(userTo, userToAccount, amount);
        /////////////////////////

        // Create a new transfer
        Transfer transfer = new Transfer();
        transfer.setAccount_to(userToAccount.getAccount_id());
        transfer.setAccount_from(myAccount.getAccount_id());
        transfer.setTransfer_type(TransferType.SEND);
        transfer.setTransfer_status(TransferStatus.APPROVED);
        transfer.setAmount(amount);
        transferService.createTransfer(transfer);

        System.out.println("Successfully sent $" + amount + " to user ID " + userTo);
        System.out.println("Your current account balance is: $" + myAccount.getBalance());
    }


    //trying something new //
    private void updateAccountBalance(int userId, Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountService.updateAccountBalance(userId, account);
    }
    /////////////////////////

    private void requestBucks() {
        // Get the list of users
        List<User> users = userService.findAll();
        Account myAccount = accountService.getAccountByUserID(App.ID);

        // Print the list of users
        System.out.println("Available Users:");
        for (User user : users) {
            System.out.println("ID: " + user.getId() + ", Username: " + user.getUsername());
        }

        // Prompt for the user ID to request money fro
        int userFrom = consoleService.promptForUserID();
        if (userFrom == 0) {
            // Cancel option selected, return to the main menu
            return;
        }
        // Check if the entered user ID is valid
        boolean isValidUser = false;
        for (User user : users) {
            if (user.getId() == userFrom) {
                isValidUser = true;
                break;
            }
        }

        if (!isValidUser) {
            System.out.println("Invalid user ID. Please enter a valid user ID.");
            return;
        }


        // Check if the user is trying to request money from themselves
        if (userFrom == App.ID) {
            consoleService.printErrorMessage_custom("Unable To Complete, Cannot Send Funds To Self");
            return;
        }

        // Retrieve the account of the user to request money from
        Account userFromAccount = accountService.getAccountByUserID(userFrom);

        // Prompt for the amount to request
        BigDecimal amount = consoleService.promptForAmount();

        // Check if the amount is valid (greater than zero)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            consoleService.printErrorMessage_custom("Amount Is Not Valid: Must Be Greater Than Zero.");
            return;
        }
        Transfer transfer = new Transfer();
        transfer.setAccount_to(myAccount.getAccount_id());
        transfer.setAccount_from(userFromAccount.getAccount_id());
        transfer.setTransfer_type(TransferType.REQUEST);
        transfer.setTransfer_status(TransferStatus.PENDING);
        transfer.setAmount(amount);
        transferService.createTransfer(transfer);

        System.out.println("Successfully requested $" + amount + " from user ID " + userFrom);
        System.out.println("Please check the status of your request in the main menu options (View pending requests).");
    }


    private HttpEntity<?> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }

}
