package socialnetwork.ui;

import socialnetwork.domain.CriteriiUtilizator;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.exceptions.ServiceException;
import socialnetwork.service.Service;
import socialnetwork.util.paging.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Console user interface.
 * <p>
 * It reads numbered commands from the keyboard and calls the methods of
 * {@link Service}. The exceptions of the application are handled here: their
 * message is shown to the user, without a stack trace, and the program goes on
 * (for example when the database server is stopped). The search commands
 * build their criteria from {@link CriteriiUtilizator} and lambda expressions.
 * <p>
 * When the persistence supports paging (database), the list of users and the
 * list of friends of a user are shown page by page, with forward / back
 * navigation and the text "Pagina x din y". The page size comes from the
 * configuration file. The user interface contains no SQL: it asks the service
 * for a page.
 */
public class ConsoleUI {
    /** The service that contains the application logic. */
    private final Service service;
    /** The number of elements on a page of a list. */
    private final int pageSize;
    /** The reader for the keyboard input. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates the user interface for the given service.
     *
     * @param service  the service of the application
     * @param pageSize the number of elements on a page of a list
     */
    public ConsoleUI(Service service, int pageSize) {
        this.service = service;
        this.pageSize = pageSize;
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
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Eroare: ID-ul trebuie sa fie un numar.");
            } catch (IllegalArgumentException e) {
                System.out.println("Date invalide: " + e.getMessage());
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
                showUsers();
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
     * Reads the first and last name, adds the user and shows it, together with
     * the id received from the storage (for {@code DATABASE}, from the database).
     */
    private void addUser() {
        Utilizator u = service.addUser(read("Prenume: "), read("Nume: "));
        System.out.println("Adaugat, cu id-ul primit de la stocare " + u.getId() + ": " + u);
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
     * Shows the users: page by page if the persistence supports paging,
     * otherwise the whole list.
     */
    private void showUsers() {
        if (service.supportsPaging()) {
            browse("Utilizatori:", pageNumber -> service.getUsersOnPage(pageNumber, pageSize),
                    Utilizator::toString);
        } else {
            service.getAllUsers().forEach(System.out::println);
        }
    }

    /**
     * Shows a list page by page and lets the user move between pages.
     * Every page is read again from the service, so the number of pages is
     * updated after an addition or a deletion. Commands: {@code n} - next page,
     * {@code p} - previous page, {@code g} - go to a page, {@code 0} - back to
     * the menu. Going to a page after the last one shows an empty page.
     * <p>
     * The {@code while} loop reads commands from the keyboard; it does not
     * traverse a collection.
     *
     * @param title     the title shown above every page
     * @param loader    returns the page with the given number (starting from 0)
     * @param formatter converts a user into the line shown on the page
     */
    private void browse(String title, IntFunction<Page<Utilizator>> loader,
                        Function<Utilizator, String> formatter) {
        int pageNumber = 0;
        int lastShown = 0;
        while (true) {
            Page<Utilizator> page;
            try {
                page = loader.apply(pageNumber);
            } catch (IllegalArgumentException e) {
                System.out.println("Date invalide: " + e.getMessage());
                pageNumber = lastShown;
                continue;
            }
            lastShown = pageNumber;
            long numberOfPages = page.getNumberOfPages(pageSize);
            System.out.println(title);
            if (page.getElementsOnPage().isEmpty()) {
                System.out.println("  (pagina goala)");
            }
            page.getElementsOnPage().stream()
                    .map(formatter)
                    .map(line -> "  " + line)
                    .forEach(System.out::println);
            System.out.println("Pagina " + (pageNumber + 1) + " din " + numberOfPages
                    + " (" + page.getTotalNumberOfElements() + " in total)");
            String cmd = read("n - urmatoarea, p - anterioara, g - mergi la pagina, 0 - inapoi: ");
            switch (cmd) {
                case "n":
                    if (pageNumber + 1 < numberOfPages) {
                        pageNumber++;
                    } else {
                        System.out.println("Esti deja pe ultima pagina.");
                    }
                    break;
                case "p":
                    if (pageNumber > 0) {
                        pageNumber--;
                    } else {
                        System.out.println("Esti deja pe prima pagina.");
                    }
                    break;
                case "g":
                    pageNumber = readPageNumber(pageNumber);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Comanda necunoscuta.");
            }
        }
    }

    /**
     * Reads the number of a page, as the user sees it (starting from 1).
     *
     * @param current the current page number, kept if the text is not a number
     * @return the page number starting from 0 (it may be negative; the service
     *         rejects it)
     */
    private int readPageNumber(int current) {
        String text = read("Pagina: ");
        try {
            return Integer.parseInt(text) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Eroare: numarul paginii trebuie sa fie un numar.");
            return current;
        }
    }

    /**
     * Reads an id and shows the friends of that user: page by page if the
     * persistence supports paging, otherwise the names on one line.
     */
    private void friendsOf() {
        Long id = readId("ID: ");
        if (service.supportsPaging()) {
            service.getUser(id);
            browse("Prietenii lui " + id + ":", pageNumber -> service.getFriendsOnPage(id, pageNumber, pageSize),
                    u -> u.getId() + " | " + u.getFirstName() + " " + u.getLastName());
            return;
        }
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