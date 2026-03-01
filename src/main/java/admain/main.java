package admain;

import java.util.Scanner;

public class main {
   
    private  static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        boolean exitProgram = false;

        while (!exitProgram) {
            System.out.println("\n--- Admin System ---");
            System.out.println("1. Login_admin");
            System.out.println("2. Create Admin");
            System.out.println("3. Logout");
            System.out.println("4. Exit Program");

            System.out.print("Choose option: ");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1: // Login_admin
                    loginAdmin();
                    break;
                case 2: // Create_Admin
                    createAdmin();
                    break;
                case 3: // Logout
                    logoutAdmin();
                    break;
                
                default:
                    System.out.println("Invalid option! Try again.");
            }
        }

    }

    private static void loginAdmin() {
        if (session.adminId != -1) {
            System.out.println("Already logged in! Admin ID: " + session.adminId);
            return;
        }

        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        int adminId = login_foradmain.login(user, pass);
        if (adminId != -1) {
            session.adminId = adminId;
            System.out.println("Login Successful! Admin ID: " + adminId);
        } else {
            System.out.println("Invalid credentials! Try again.");
        }
    }

    private static void createAdmin() {
        System.out.print("Enter new admin username: ");
        String user = sc.nextLine();
        System.out.print("Enter new admin password: ");
        String pass = sc.nextLine();

        if (login_foradmain.createAdmin(user, pass)) {
            System.out.println("Admin created successfully!");
        } else {
            System.out.println("Failed to create Admin! Username might exist.");
        }
    }

    private static void logoutAdmin() {
        if (session.adminId != -1) {
            System.out.println("Logging out Admin ID: " + session.adminId);
            session.adminId = -1;
        } else {
            System.out.println("No admin is logged in.");
        }
    }

}