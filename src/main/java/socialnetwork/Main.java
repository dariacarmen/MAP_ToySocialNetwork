package socialnetwork;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.PrietenieValidator;
import socialnetwork.domain.validators.UtilizatorValidator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.repository.Repository;
import socialnetwork.repository.RepositoryFactory;
import socialnetwork.service.Service;
import socialnetwork.ui.ConsoleUI;

/**
 * Entry point of the social network application.
 * <p>
 * It builds the layers of the application from the bottom up: the validators,
 * the repositories (created by {@link RepositoryFactory} from the configuration
 * file), the service and the user interface. It knows only the
 * {@link Repository} interface, not the concrete repository classes.
 */
public class Main {
    /** The maximum length of a first name or of a last name. */
    public static final int MAX_NAME_LENGTH = 50;

    /**
     * Private constructor: the class must not be instantiated.
     */
    private Main() {
    }

    /**
     * Starts the application. If the data cannot be loaded or the configuration
     * is wrong, a clear message is shown and the application stops.
     * <p>
     * The user validator is {@link UtilizatorValidator} combined, with
     * {@link Validator#andThen(Validator)}, with a new rule written as a lambda:
     * the first and last name have at most {@value #MAX_NAME_LENGTH} characters.
     * Other characters (spaces, apostrophes, punctuation) are allowed: they are
     * saved as ordinary text, because the database repositories send the values
     * as parameters of a {@link java.sql.PreparedStatement}.
     *
     * @param args the command line arguments (not used)
     */
    public static void main(String[] args) {
        Validator<Utilizator> maxLength = u -> {
            boolean ok = u.getFirstName().length() <= MAX_NAME_LENGTH
                    && u.getLastName().length() <= MAX_NAME_LENGTH;
            if (!ok) {
                throw new ValidationException("Numele si prenumele pot avea cel mult "
                        + MAX_NAME_LENGTH + " de caractere.");
            }
        };
        Validator<Utilizator> userValidator = new UtilizatorValidator().andThen(maxLength);

        try {
            RepositoryFactory factory = new RepositoryFactory(RepositoryFactory.DEFAULT_CONFIG_FILE);
            System.out.println("Persistenta folosita: " + factory.getType());
            Repository<Long, Utilizator> userRepo = factory.createUtilizatorRepository(userValidator);
            Repository<Tuple<Long, Long>, Prietenie> friendshipRepo =
                    factory.createPrietenieRepository(new PrietenieValidator());
            Service service = new Service(userRepo, friendshipRepo);
            new ConsoleUI(service, factory.getPageSize()).run();
        } catch (RepositoryException e) {
            System.out.println("Aplicatia nu poate porni. " + e.getMessage());
        }
    }
}