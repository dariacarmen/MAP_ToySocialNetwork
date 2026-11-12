package socialnetwork.ui;

import socialnetwork.domain.CriteriiUtilizator;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.exceptions.ServiceException;
import socialnetwork.service.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Console user interface.
 * <p>
 * It reads numbered commands from the keyboard and calls the methods of
 * {@link Service}. The exceptions of the application are handled here: their
 * message is shown to the user and the program goes on. The search commands
 * build their criteria from {@link CriteriiUtilizator} and lambda expressions.
 */
public class ConsoleUI {
    /** The service that contains the application logic. */
    private final Service service;
    /** The reader for the keyboard input. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates the user interface for the given service.
     *
     * @param service the service of the application
     */
    public ConsoleUI(Service service) {
        this.service = service;
    }

    /**
     * Shows the list of available commands.
     */
    private void printHelp() {
        System.out.println("Comenzi:");
        System.out.println("  1 - adauga utilizator");
        System.out.println("  2 - sterge utilizator");
        System.out.println("  3 - adauga prieten");
        System.out.println("  4 - sterge prieten");
        System.out.println("  5 - afiseaza utilizatori");
        System.out.println("  6 - afiseaza prietenii");
        System.out.println("  7 - numar de comunitati");
        System.out.println("  8 - cea mai sociabila comunitate");
        System.out.println("  9 - prietenii unui utilizator");
        System.out.println(" 10 - cauta utilizatori dupa nume sau prenume");
        System.out.println(" 11 - utilizatori fara prieteni");
        System.out.println(" 12 - utilizatori cu cel putin N prieteni");
        System.out.println(" 13 - utilizatori al caror nume NU incepe cu un text");
        System.out.println("  0 - iesire");
    }

    /**
     * Starts the main loop: reads and runs commands until the command
     * {@code 0} or the end of the input. Validation, service, persistence and
     * number format errors are caught and their message is shown.
     * <p>
     * The {@code while} loop reads commands from the keyboard; it does not
     * traverse a collection, so it is kept.
     */
    public void run() {
        printHelp();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) return;
            String cmd = scanner.nextLine().trim();
            if (cmd.equals("0")) return;
            try {
                runCommand(cmd);
            } catch (ValidationException e) {
                System.out.println("Date invalide: " + e.getMessage());
            } catch (ServiceException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Eroare la salvarea datelor: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Eroare: ID-ul trebuie sa fie un numar.");
            }
        }
    }

    /**
     * Runs one command.
     *
     * @param cmd the command typed by the user
     */
    private void runCommand(String cmd) {
        switch (cmd) {
            case "1":
                addUser();
                break;
            case "2":
                removeUser();
                break;
            case "3":
                addFriend();
                break;
            case "4":
                removeFriend();
                break;
            case "5":
                service.getAllUsers().forEach(System.out::println);
                break;
            case "6":
                service.getAllFriendships().forEach(System.out::println);
                break;
            case "7":
                System.out.println("Numar de comunitati: " + service.getNumberOfCommunities());
                break;
            case "8":
                mostSociable();
                break;
            case "9":
                friendsOf();
                break;
            case "10":
                searchByName();
                break;
            case "11":
                printList(service.searchUsers(CriteriiUtilizator::faraPrieteni));
                break;
            case "12":
                atLeastFriends();
                break;
            case "13":
                lastNameNotStartingWith();
                break;
            case "":
                break;
            default:
                printHelp();
        }
    }

    /**
     * Shows a message and reads a line from the keyboard.
     *
     * @param prompt the message shown before reading
     * @return the line read, without spaces at the ends
     */
    private String read(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Shows a message and reads an id from the keyboard.
     *
     * @param prompt the message shown before reading
     * @return the id read
     * @throws NumberFormatException if the text typed is not a number
     */
    private Long readId(String prompt) {
        return Long.parseLong(read(prompt));
    }

    /**
     * Shows a list of users, one per line, or a message if the list is empty.
     *
     * @param users the users to show
     */
    private void printList(List<Utilizator> users) {
        if (users.isEmpty()) {
            System.out.println("Niciun utilizator.");
        }
        users.forEach(System.out::println);
    }

    /**
     * Reads the first and last name, adds the user and shows it.
     */
    private void addUser() {
        Utilizator u = service.addUser(read("Prenume: "), read("Nume: "));
        System.out.println("Adaugat: " + u);
    }

    /**
     * Reads an id, removes the user and shows it.
     */
    private void removeUser() {
        System.out.println("Sters: " + service.removeUser(readId("ID: ")));
    }

    /**
     * Reads two ids and creates the friendship between the two users.
     */
    private void addFriend() {
        service.addFriend(readId("ID utilizator 1: "), readId("ID utilizator 2: "));
        System.out.println("Prietenie adaugata.");
    }

    /**
     * Reads two ids and removes the friendship between the two users.
     */
    private void removeFriend() {
        service.removeFriend(readId("ID utilizator 1: "), readId("ID utilizator 2: "));
        System.out.println("Prietenie stearsa.");
    }

    /**
     * Shows the most sociable community and the length of its longest path.
     */
    private void mostSociable() {
        Tuple<List<Utilizator>, Integer> result = service.getMostSociableCommunity();
        System.out.println("Cea mai sociabila comunitate (drum maxim = " + result.getRight() + "):");
        result.getLeft().stream()
                .map(u -> "  " + u)
                .forEach(System.out::println);
    }

    /**
     * Reads an id and shows the names of the friends of that user, on one line.
     */
    private void friendsOf() {
        Long id = readId("ID: ");
        String names = service.getFriendsOf(id).stream()
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .collect(Collectors.joining(", "));
        System.out.println("Prietenii lui " + id + ": " + (names.isEmpty() ? "niciunul" : names));
    }

    /**
     * Reads a text and shows the users whose last name or first name starts
     * with it (criteria combined with {@code or}).
     */
    private void searchByName() {
        String text = read("Text: ");
        Predicate<Utilizator> criterion = CriteriiUtilizator.numeIncepeCu(text)
                .or(CriteriiUtilizator.prenumeIncepeCu(text));
        printList(service.searchUsers(criterion));
    }

    /**
     * Reads a number N and shows the users that have friends and at least N
     * of them (criteria combined with {@code Predicate.not} and {@code and}),
     * from the most to the least sociable, then by last name.
     */
    private void atLeastFriends() {
        int n = Integer.parseInt(read("N: "));
        Predicate<Utilizator> criterion = Predicate.not(CriteriiUtilizator::faraPrieteni)
                .and(CriteriiUtilizator.celPutinPrieteni(n));
        Comparator<Utilizator> order = Comparator
                .comparingInt((Utilizator u) -> u.getFriends().size()).reversed()
                .thenComparing(Utilizator::getLastName);
        printList(service.searchUsers(criterion, order));
    }

    /**
     * Reads a text and shows the users whose last name does not start with it
     * (criterion combined with {@code negate}).
     */
    private void lastNameNotStartingWith() {
        String text = read("Text: ");
        printList(service.searchUsers(CriteriiUtilizator.numeIncepeCu(text).negate()));
    }
}