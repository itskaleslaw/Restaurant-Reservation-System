package org.group4;


import org.group4.Exceptions.MenuItemException;
import org.group4.Exceptions.NoSpaceException;
import org.group4.Exceptions.ReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class RestaurantController {
    private final HashMap<String, Restaurant> restaurants = new HashMap<>();
    private final HashMap<String, Customer> customers = new HashMap<>();
    private final HashMap<String, MenuItem> menuItems = new HashMap<>();
    private final HashMap<String, Owner> owners = new HashMap<>();
    private final HashSet<String> ingredients = new HashSet<>();

    public void commandLoop() {
        Scanner commandLineInput = new Scanner(System.in);
        String wholeInputLine;
        String[] tokens;
        final String DELIMITER = ", ";

        while (true) {
            try {
                // Determine the next command and echo it to the monitor for testing purposes
                wholeInputLine = commandLineInput.nextLine();
                tokens = wholeInputLine.split(DELIMITER);
                System.out.println(" echo >> " + wholeInputLine);
                if (tokens[0].indexOf("//") == 0) {
                    // System.out.println(wholeInputLine);
                    // instructions to create simulation resources
                } else if (wholeInputLine.isEmpty()) {
                    continue;
                } else if (tokens[0].equals("create_restaurant")) {
                    handleCreateRestaurant(tokens);
                } else if (tokens[0].equals("create_customer")) {
                    handleCreateCustomer(tokens);
                } else if (tokens[0].equals("make_reservation")) {
                    handleMakeReservation(tokens);
                } else if (tokens[0].equals("customer_arrival")) {
                    handleCustomerArrival(tokens);
                } else if (tokens[0].equals("create_owner")) {
                    handleCreateOwner(tokens);
                } else if (tokens[0].equals("add_menu_item")) {
                    handleAddMenuItem(tokens);
                } else if (tokens[0].equals("create_menu_item")) {
                    handleCreateMenuItem(tokens);
                } else if (tokens[0].equals("order_menu_item")) {
                    handleOrderMenuItem(tokens);
                } else if (tokens[0].equals("customer_review")) {
                    handleCustomerReview(tokens);
                } else if (tokens[0].equals("calculate_average_price")) {
                    handleCalculateAveragePrice(tokens);
                } else if (tokens[0].equals("view_owners")) {
                    handleViewOwners(tokens);
                } else if (tokens[0].equals("view_all_restaurants")) {
                    handleViewAllRestaurants(tokens);
                } else if (tokens[0].equals("view_restaurants_owned")) {
                    handleViewRestaurantsOwned(tokens);
                }else if (tokens[0].equals("view_all_customers")) {
                    handleViewAllCustomers(tokens);
                } else if (tokens[0].equals("view_all_menu_items")) {
                    handleViewAllMenuItems(tokens);
                } else if (tokens[0].equals("view_ingredients")) {
                    handleViewIngredients(tokens);
                } else if (tokens[0].equals("view_menu_items")) {
                    handleViewMenuItems(tokens);
                } else if (tokens[0].equals("calculate_item_popularity")) {
                    handleCalculateItemPopularity(tokens);
                } else if (tokens[0].equals("exit")) {
                    System.out.println("stop acknowledged");
                    break;
                } else {
                    System.out.println("command " + tokens[0] + " NOT acknowledged");
                }
                System.out.println("Enter command: ");
            } catch (Exception e) {
                displayMessage("error", "during command loop >> execution");
                e.printStackTrace();
                System.out.println();
            }
        }

        System.out.println("simulation terminated");
        commandLineInput.close();
    }

    private void handleCreateRestaurant(String[] tokens) {
        Address address = new Address(tokens[3], tokens[4], tokens[5]);
        Owner owner = owners.get(tokens[7]);
        if (owner == null) {
            // idk why but printing "ERROR: " before the statement causes the loop to terminate
            System.out.println("ERROR: Owner does not exist");
        } else {
            License license = new License(tokens[7], tokens[1], tokens[8]);
            owner.addLicense(tokens[1], license);
            Restaurant restaurant = new Restaurant(tokens[1], tokens[2], address, -1,
                    false, Integer.parseInt(tokens[6]), owner);
            System.out.printf("Restaurant created: %s (%s) - %s, %s %s\n", tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
            System.out.printf("%s (%s %s) owns %s (%s)\n", owner.getId(), owner.getFirstName(), owner.getLastName(), restaurant.getId(), restaurant.getName());
            owner.addOwnedRestaurant(restaurant);
            restaurants.put(restaurant.getId(), restaurant);
        }
    }

    private void handleCreateCustomer(String[] tokens) {
        Address address = new Address(tokens[4], tokens[5], tokens[6]);
        Customer customer = new Customer(tokens[1], tokens[2], tokens[3], address,
                Double.parseDouble(tokens[7]));
        String lastName = tokens[3] == null ? "" : tokens[3];
        System.out.printf("Customer added: %s - %s %s\n", tokens[1], tokens[2], lastName);
        customers.put(customer.getId(), customer);
    }

    private void handleMakeReservation(String[] tokens) {
        Customer customer = customers.get(tokens[1]);
        Restaurant restaurant = restaurants.get(tokens[2]);
        // Turns the date (example: 2024-05-24) and the time (ex: 19:00) to an ISO datetime: "2024-05-24T19:00:00"
        LocalDateTime dateTime = LocalDateTime.parse("%sT%s:00".formatted(tokens[4], tokens[5]));
        try {
            Reservation res = restaurant.makeReservation(customer, Integer.parseInt(tokens[3]), dateTime,
                    Integer.parseInt(tokens[6]));
            System.out.printf("Reservation requested for %s %s", customer.getFirstName(), customer.getLastName());
            System.out.print("\nReservation confirmed");
            System.out.printf("\nReservation made for %s (%s %s) at %s\n", customer.getId(), customer.getFirstName(), customer.getLastName(), restaurant.getName());
        } catch(ReservationException.Conflict rce) {
            System.out.printf("Reservation requested for %s %s", customer.getFirstName(), customer.getLastName());
            System.out.print("\nReservation request denied, customer already has reservation with another restaurant within 2 hours of the requested time\n");
        } catch(ReservationException.FullyBooked nse) {
            System.out.printf("Reservation requested for %s %s", customer.getFirstName(), customer.getLastName());
            System.out.print("\nReservation request denied, table has another active reservation within 2 hours of the requested time\n");
        }
    }

    private void handleCustomerArrival(String[] tokens) {
        Customer customer = customers.get(tokens[1]);
        Restaurant restaurant = restaurants.get(tokens[2]);

        LocalDate reservationDate = LocalDate.parse(tokens[3]);
        LocalTime reservationTime = tokens[5].equals("null") ? null : LocalTime.parse(tokens[5]);
        LocalTime arrivalTime = LocalTime.parse(tokens[4]);

        try {
            restaurant.onCustomerArrival(customer, reservationDate, arrivalTime, reservationTime);
        } catch(NoSpaceException e) {
            // Although kinda scuffed to catch this message here it makes writing tests easier
            System.out.print(IOMessages.getNoSeatsMessage());
        }
        System.out.print(IOMessages.getCustomerInfoMessage(customer));
    }

    private void handleCreateOwner(String[] tokens) {
        if (owners.get(tokens[1]) != null) {
            System.out.println("|ERROR: duplicate unique identifier");
        } else {
            Address address = new Address(tokens[4], tokens[5], tokens[6]);
            Owner owner = new Owner(LocalDate.parse(tokens[7]), tokens[1], tokens[2], tokens[3], address, tokens[8]);
            System.out.printf("Owner added: %s - %s %s\n", tokens[1], tokens[2], tokens[3]);
            owners.put(tokens[1], owner);
        }
    }

    private void handleAddMenuItem(String[] tokens) {
        MenuItem newItem = menuItems.get(tokens[1]);
        if (newItem == null) {
            System.out.println("|ERROR: Menu item doesn't exist");
            return;
        }

        try {
            int price = Integer.parseInt(tokens[3]);
            Restaurant rest = restaurants.get(tokens[2]);
            rest.addMenuItem(newItem, price);
            System.out.printf("(%s) Menu item added: %s - $%d\n", rest.getId(), newItem.getName(), price);
        } catch (MenuItemException.AlreadyAdded aa) {
            System.out.println("|ERROR: item has already been added to this restaurant, try again");
        }
    }

    private void handleCreateMenuItem(String[] tokens) {
        String name = tokens[1];
        String[] ingredients = tokens[2].split(":");
        MenuItem menuItem = new MenuItem(name, ingredients);
        System.out.printf("%s created\n", name);
        menuItems.put(name, menuItem);
    }

    private void handleOrderMenuItem(String[] tokens) {
        Customer customer = customers.get(tokens[1]);
        Restaurant restaurant = restaurants.get(tokens[2]);
        LocalDate reservationDate = LocalDate.parse(tokens[3]);
        LocalTime reservationTime = LocalTime.parse(tokens[4]);
        MenuItem menuItem = menuItems.get(tokens[5]);
        int quantity = Integer.parseInt(tokens[6]);

        try {
            Reservation reservation = restaurant.orderItem(customer, reservationDate, reservationTime, menuItem, quantity);
            // int totalPrice = restaurant.getRestaurantMenuItems().get(menuItem.getName()).getPrice() * quantity;
            System.out.println("Menu item successfully ordered");
            System.out.printf("Total Price for ordered amount: %d\n", reservation.getLastOrderPrice());
            System.out.printf("%s bill for current reservation: %d\n", customer.getId(), reservation.getBill());
            System.out.printf("%s updated funds: %d\n", customer.getId(), customer.getCredits());
            System.out.printf("%s total revenue from all reservations: %d\n", restaurant.getId(), restaurant.getRevenue());
        } catch (ReservationException.DoesNotExist e) {
            System.out.printf("Order failed: Reservation does not exist for %s %s at %s\n", customer.getFirstName(), customer.getLastName(), restaurant.getName());
        } catch (OrderFoodException.NotInRestaurant e) {
            System.out.printf("Order failed: Item is not in the restaurant\n");
        } catch (OrderFoodException.InsufficientCredits e) {
            System.out.printf("Order failed: Insufficient credits\n");
        }
    }

    private void handleCustomerReview(String[] tokens) {
        // TODO: implement
        Customer customer = customers.get(tokens[1]);
        Restaurant restaurant = restaurants.get(tokens[2]);
        LocalDate reservationDate = LocalDate.parse(tokens[3]);
        LocalTime reservationTime = LocalTime.parse(tokens[4]);
        int rating = Integer.parseInt(tokens[5]);
        List<String> tags = Arrays.asList(tokens[6].split(":"));
        try {
            customer.reviewRestaurant(restaurant, reservationDate, reservationTime, rating, tags);
        System.out.printf("%s (%s) rating for this reservation: %d\n", restaurant.getId(), restaurant.getName(), rating);
        System.out.printf("%s (%s) average rating: %d\n", restaurant.getId(), restaurant.getName(), restaurant.getRating());
        System.out.print("Tags: ");
        for (String tag : restaurant.getTags()) {
            System.out.print(tag + ", ");
        }
        System.out.println();
        } catch (ReservationException.DoesNotExist e) {
            System.out.println("|ERROR: reservation doesn't exist");
        } catch (ReservationException.NotSuccessful e) {
            System.out.println("|ERROR: reservation wasn't successfully completed");
        }
    }

    private void handleCalculateAveragePrice(String[] tokens) {
        MenuItem item = menuItems.get(tokens[1]);
        if (item == null) {
            System.out.println("|ERROR: item doesn't exist");
            return;
        }
        try {
            int price = item.getAveragePrice();
            System.out.printf("Average price for %s: $%d\n", item.getName(), price);
        } catch (MenuItemException.NeverAdded e) {
            System.out.println("|ERROR: item was never added to a restaurant");
        }
    }

    private void handleViewOwners(String[] tokens) {
        //Kind of inefficient bc we check over every owner? do we need a restaurant group class?
        for (Owner o : owners.values()) {
            if (o.getRestaurantGroup().equals(tokens[1])) {
                System.out.printf("%s %s%n", o.getFirstName(), o.getLastName());
            }
        }
    }

    private void handleViewAllRestaurants(String[] tokens) {
        for (Restaurant r : restaurants.values()) {
            System.out.printf("%s (%s)%n", r.getId(), r.getName());
        }
    }

    private void handleViewRestaurantsOwned(String[] tokens) {
        Owner owner = owners.get(tokens[1]);
        if (owner != null) {
            for (Restaurant r : owner.getOwnedRestaurants().values()) {
                System.out.printf("%s (%s)%n", r.getId(), r.getName());
            }
        }
    }

    private void handleViewAllCustomers(String[] tokens) {
        // TODO: implement
        for (Customer customer : customers.values()) {
            String id = customer.getId();
            String firstName = customer.getFirstName();
            String lastName = customer.getLastName();
            System.out.printf("%s (%s %s)%n", id, firstName, lastName);
        }
    }

    private void handleViewAllMenuItems(String[] tokens) {
        // TODO: implement
        for (MenuItem menuItem : menuItems.values()) {
            String itemName = menuItem.getName();
            System.out.println(itemName);
        }
    }

    private void handleViewIngredients(String[] tokens) {
        MenuItem menuItem = menuItems.get(tokens[1]);
        String[] ingredientArray = menuItem.getIngredients();
        StringBuilder ingredients = new StringBuilder("Ingredients: ");
        for (int i = 0; i < ingredientArray.length; i++) {
            ingredients.append(ingredientArray[i]);
            if (i < ingredientArray.length - 1) {
                ingredients.append(", ");
            }
        }
        System.out.println(ingredients);
    }

    private void handleViewMenuItems(String[] tokens) {
        Map<String, RestaurantMenuItem> items = restaurants.get(tokens[1]).getRestaurantMenuItems();
        for (String key : items.keySet()) {
            System.out.println(items.get(key).getParentItem().getName());
        }
    }

    private void handleCalculateItemPopularity(String[] tokens) {
        // TODO: implement
        if (menuItems.get(tokens[1]) == null) {
            System.out.println("|ERROR: item doesn't exist");
        } else {
        try {
            int popularity = menuItems.get(tokens[1]).calculatePopularity();
            int totalRestaurants = restaurants.size();
            double popularityPercentage = (popularity / (double) totalRestaurants) * 100;
            if (popularityPercentage > 50) {
                System.out.printf("Popular: %s offered at %d out of %d restaurants (%.0f%%)\n", tokens[1], popularity, totalRestaurants, popularityPercentage);
            } else {
                System.out.printf("Not popular: %s offered at %d out of %d restaurants (%.0f%%)\n", tokens[1], popularity, totalRestaurants, popularityPercentage);
            }
        } catch (MenuItemException.NeverAdded e) {
                System.out.printf("ERROR: item was never added to a restaurant\n");
            }
        }
    }

    void displayMessage(String status, String text_output) {
        System.out.println(status.toUpperCase() + ": " + text_output.toLowerCase());
    }

}
