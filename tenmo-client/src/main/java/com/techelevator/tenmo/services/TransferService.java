package com.techelevator.tenmo.services;

import java.math.BigDecimal;
import java.util.Scanner;

import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.*;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;

public class TransferService {

    private String BASE_URL;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public TransferService(String url, AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
        BASE_URL = url;
    }
    // Communicating and making requests to the server and out-puting to console
    public Transfers[] transfersList() {
        Transfers [] output = null;
        try {
            output = restTemplate.exchange(BASE_URL + "account/transfers/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Transfers\r\n" +
                    "ID          From/To                 Amount\r\n" +
                    "-------------------------------------------\r\n");
            String fromOrTo = "";
            String name = "";
            for (Transfers i : output) {
                if (currentUser.getUser().getId() == i.getAccountFrom()) {
                    fromOrTo = "From: ";
                    name = i.getUserTo();
                } else {
                    fromOrTo = "To: ";
                    name = i.getUserFrom();
                }
                System.out.println(i.getTransferId() +"\t\t" + fromOrTo + name + "\t\t$" + i.getAmount());
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Please enter transfer ID to view details (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (Integer.parseInt(input) != 0) { //making sure the input was not = to 0
                boolean foundTransferId = false;
                for (Transfers i : output) {
                    if (Integer.parseInt(input) == i.getTransferId()) {
                        Transfers temp = restTemplate.exchange(BASE_URL + "transfers/" + i.getTransferId(), HttpMethod.GET, makeAuthEntity(), Transfers.class).getBody();
                        foundTransferId = true;
                        System.out.println("--------------------------------------------\r\n" +
                                "Transfer Details\r\n" +
                                "--------------------------------------------\r\n" +
                                " Id: "+ temp.getTransferId() + "\r\n" +
                                " From: " + temp.getUserFrom() + "\r\n" +
                                " To: " + temp.getUserTo() + "\r\n" +
                                " Type: " + temp.getTransferType() + "\r\n" +
                                " Status: " + temp.getTransferStatus() + "\r\n" +
                                " Amount: $" + temp.getAmount());
                    }
                }
                if (!foundTransferId) {
                    System.out.println("Not a valid transfer ID");
                }
            }
        } catch (Exception e) {
            System.out.println("Oops! Something went wrong... ");
        }
        return output;
    }

    public void sendBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();
        try {
            Scanner scanner = new Scanner(System.in);
            //making a request to the server for a list of users
            users = restTemplate.exchange(BASE_URL + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Users\r\n" +
                    "ID\t\tName\r\n" +
                    "-------------------------------------------");
            for (User i : users) {
                if (i.getId() != this.currentUser.getUser().getId()) { //if the id of the user sending is not the same as the person receiving
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Enter ID of the receiving user(0 to cancel): ");
            transfer.setAccountTo(Integer.parseInt(scanner.nextLine()));
            transfer.setAccountFrom(this.currentUser.getUser().getId());
            if (transfer.getAccountTo() != 0) {
                System.out.print("Enter amount: ");
                try {
                    transfer.setAmount(new BigDecimal(Double.parseDouble(scanner.nextLine()))); //parsing the number entered
                } catch (NumberFormatException e) {
                    System.out.println("Error!"); //if the amount entered is not in the correct format it will catch the mistake and print error
                }
                String output = restTemplate.exchange(BASE_URL + "transfer", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void requestBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();
        try {
            Scanner scanner = new Scanner(System.in);
            //making a request to the server for a list of users
            users = restTemplate.exchange(BASE_URL + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Users\r\n" +
                    "ID\t\tName\r\n" +
                    "-------------------------------------------");
            for (User i : users) {
                if (i.getId() != currentUser.getUser().getId()) {
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Enter ID of user you are requesting from (0 to cancel): ");
            transfer.setAccountTo(this.currentUser.getUser().getId()); //selecting the valid requested account
            transfer.setAccountFrom(Integer.parseInt(scanner.nextLine())); //selecting the valid account requesting
            if (transfer.getAccountTo() != 0) {
                System.out.print("Enter amount: ");
                try {
                  //  String transferString = scanner.nextLine();
                   // System.out.println(transferString);
                    transfer.setAmount(new BigDecimal(Double.parseDouble(scanner.next())));
                    System.out.println(transfer.getAmount());
                } catch (NumberFormatException e) {
                    System.out.println("Error when entering amount");
                } //requesting the change to the server
                String output = restTemplate.exchange(BASE_URL + "request", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Transfers[] transfersRequestList() {
        Transfers [] output = null; //making the transfer[] null to begin the list
        String results;
        try {
            //making a request to the server for the transfer request list.
            output = restTemplate.exchange(BASE_URL + "request/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Pending Transfers\r\n" +
                    "ID          From/To                 Amount\r\n" +
                    "-------------------------------------------\r\n");
            String fromOrTo = "";
            String name = "";
            for (Transfers i : output) { //going through a list of transfers and outputting the list dependant on the sending or recieving type
                if (currentUser.getUser().getId() == i.getAccountFrom()) {
                    fromOrTo = "From: ";
                    name = i.getUserTo();
                } else {
                    fromOrTo = "To: ";
                    name = i.getUserFrom();
                }
                System.out.println(i.getTransferId() +"\t\t" + fromOrTo + name + "\t\t$" + i.getAmount());
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Please enter transfer ID to approve/reject (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (Integer.parseInt(input) != 0) {
                boolean foundTransferId = false;
                for (Transfers i : output) {
                    if (i.getAccountTo() != currentUser.getUser().getId()) { //making sure the sent amount was sent to the current user approveing/rejecting
                        if (Integer.parseInt(input) == i.getTransferId()) {
                            System.out.print("-------------------------------------------\r\n" +
                                    i.getTransferId() +"\t\t" + fromOrTo + name + "\t\t$" + i.getAmount() + "\r\n" +
                                    "1: Approve\r\n" +
                                    "2: Reject\r\n" +
                                    "0: Don't approve or reject\r\n" +
                                    "--------------------------\r\n" +
                                    "Please choose an option: ");
                            try {
                                int id = 1 + Integer.parseInt(scanner.nextLine());
                                if (id != 1) { //making a request to the server for a status of transfers
                                   results = restTemplate.exchange(BASE_URL + "transfer/status/" + id, HttpMethod.POST, makeTransferEntity(i), String.class).getBody();
                                    System.out.println(results);
                                    foundTransferId = true;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Not a valid transfer option");
                            }
                            if (!foundTransferId) {
                                System.out.println("Not a valid transfer ID");
                            }
                        }
                    } else {
                        System.out.println("You can not approve/reject your own request.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return output;
    }

    public User[] getUsers() { //getting the valid users
        User[] user = null;
        try {
            user = restTemplate.exchange(BASE_URL + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            for (User i : user) {
                System.out.println(i);
            }
        } catch (RestClientResponseException e) {
            System.out.println("Error getting users");
        }
        return user;
    }
    // Creating header and setting the authorization
    private HttpEntity<Transfers> makeTransferEntity(Transfers transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.currentUser.getToken());
        HttpEntity<Transfers> entity = new HttpEntity<>(transfer, headers);
        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }

}


