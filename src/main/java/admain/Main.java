package admain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
	 static Scanner sc = new Scanner(System.in);
	 private static SlotService slotService = new SlotService();
	 private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static void main(String[] args) {

    	        while (true) {
    	            System.out.println("=== Main Menu ===");
    	            System.out.println("1- Login as User");
    	            System.out.println("2- Login as Admin");
    	            System.out.println("3- Exit");

    	            int choice = sc.nextInt();

    	            switch (choice) {
    	                case 1: 
    	            	    UserMenu.showMenu();
                            break;
    	            
    	                case 2:
    	                    adminMenu();
    	                    break;
    	                case 3:
    	                    System.out.println("Goodbye!");
    	                    System.exit(0);
    	            }
    	        }
    	    }

    	    public static void adminMenu() {

    	        while (true) {
    	            System.out.println("\n--- Admin Menu ---");
    	            System.out.println("1- Login");
    	            System.out.println("2- Create Account");
    	            System.out.println("3- Back");

    	            int choice = sc.nextInt();
    	            sc.nextLine();

    	            switch (choice) {

    	                case 1:
    	                    System.out.print("Username: ");
    	                    String username = sc.nextLine();

    	                    System.out.print("Password: ");
    	                    String password = sc.nextLine();

    	                    Admin admin = login_foradmain.login(username, password);

    	                    if (admin != null) {
    	                        session.currentAdmin = admin;
    	                        System.out.println("Login Successful!");
    	                        adminSession();
    	                    } else {
    	                        System.out.println("Login Failed!");
    	                    }
    	                    break;

    	                case 2:
    	                    System.out.print("New Username: ");
    	                    String newUser = sc.nextLine();

    	                    System.out.print("New Password: ");
    	                    String newPass = sc.nextLine();

    	                    if (login_foradmain.register(newUser, newPass)) {
    	                        System.out.println("Account created successfully!");
    	                    }
    	                    break;

    	                case 3:
    	                    return;
    	            }
    	        }
    	    }

    	    public static void adminSession() {

    	        while (session.currentAdmin != null) {

    	            System.out.println("\nWelcome Admin: " + session.currentAdmin.getUsername());
    	            System.out.println("1- Logout");
    	            System.out.println("2- book");
    	            int choice = sc.nextInt();

    	            if (choice == 1) {
    	                session.logout();
    	            }
    	            else if(choice ==2 ){
    	                bookAppointment();
                      
    	            }
    	        }
    	    }
    	    private static void bookAppointment() {

    	        try {
    	            System.out.print("Enter Slot ID: ");
    	            int slotId = Integer.parseInt(sc.nextLine());

    	            System.out.print("Enter Start Time (yyyy-MM-dd HH:mm): ");
    	            LocalDateTime start = LocalDateTime.parse(sc.nextLine(), formatter);

    	            System.out.print("Enter End Time (yyyy-MM-dd HH:mm): ");
    	            LocalDateTime end = LocalDateTime.parse(sc.nextLine(), formatter);

    	            System.out.print("Enter Number of Participants: ");
    	            int participants = Integer.parseInt(sc.nextLine());

    	            slotService.bookSlot(slotId, start, end, participants);

    	            System.out.println("Appointment booked successfully!");

    	        } catch (NumberFormatException e) {
    	            System.out.println("Invalid number input.");
    	        } catch (Exception e) {
    	            System.out.println("Error: " + e.getMessage());
    	        }
    	    }
    	
    }
