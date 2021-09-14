package com.aljodomo;

import java.util.Scanner;

/**
 * @author Aljoscha Domonell
 */
public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Source folders by , seperated: ");
        String source = s.nextLine();
        System.out.println("Destination folder:");
        String destination = s.nextLine();
        System.out.println("Move files? true/false");
        boolean move = s.nextBoolean();
        new MediaFolderOrganizer(source, destination, move).apply();
    }
}
