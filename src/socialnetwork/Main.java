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
 * It builds the layers of the application from the bottom up: the validators,
 * the repositories (created by {@link RepositoryFactory} from the configuration
 * file), the service and the user interface. It knows only the
 * {@link Repository} interface, not the concrete repository classes.
 */
public class Main {
    /**
     * Private constructor: the class must not be instantiated.
     */
    private Main() {
    }
    /**
     * Starts the application. If the data cannot be loaded or the configuration
     * is wrong, a clear message is shown and the application stops.
     * The user validator is {@link UtilizatorValidator} combined, with
     * {@link Validator#andThen(Validator)}, with a new rule written as a lambda:
     * the first and last name may contain only letters.
     * @param args the command line arguments (not used)
     */
    public static void main(String[] args) {
        Validator<Utilizator> onlyLetters = u -> {
            boolean ok = u.getFirstName().chars().allMatch(Character::isLetter)
                    && u.getLastName().chars().allMatch(Character::isLetter);
            if (!ok) {
                throw new ValidationException("Numele si prenumele trebuie sa contina doar litere.");
            }
        };
        Validator<Utilizator> userValidator = new UtilizatorValidator().andThen(onlyLetters);

        try {
            RepositoryFactory factory = new RepositoryFactory(RepositoryFactory.DEFAULT_CONFIG_FILE);
            System.out.println("Persistenta folosita: " + factory.getType());
            Repository<Long, Utilizator> userRepo = factory.createUtilizatorRepository(userValidator);
            Repository<Tuple<Long, Long>, Prietenie> friendshipRepo =
                    factory.createPrietenieRepository(new PrietenieValidator());
            Service service = new Service(userRepo, friendshipRepo);
            new ConsoleUI(service).run();
        } catch (RepositoryException e) {
            System.out.println("Aplicatia nu poate porni: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Cauza: " + e.getCause());
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Persistenta aleasa nu este disponibila inca: " + e.getMessage());
        }
    }
}