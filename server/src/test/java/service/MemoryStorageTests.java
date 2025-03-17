package service;


import org.junit.jupiter.api.*;
import chess.ChessGame;
import server.Server;
import passoff.model.*;
import passoff.server.StandardAPITests;
import passoff.server.TestServerFacade;
import java.net.HttpURLConnection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryStorageTests {
    private static TestServerFacade serverFacade;
    private static Server server;
    private static TestUser existingUser;
    private static TestUser newUser;
    private static TestCreateRequest createRequest;
    private static String existingAuth;

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    public static void init() {
        System.out.println("Running tests with Memory Storage...");
        StandardAPITests.init(); // Uses in-memory storage by default
        serverFacade = StandardAPITests.serverFacade;
        server = StandardAPITests.server;
        existingUser = StandardAPITests.existingUser;
        newUser = StandardAPITests.newUser;
        createRequest = StandardAPITests.createRequest;
    }

    @BeforeEach
    public void setup() {
        clearData();
        registerExistingUser();
    }

    @Test
    @Order(1)
    @DisplayName("Successful Registration")
    public void successfulRegistration() {
        TestAuthResult result = serverFacade.register(newUser);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertEquals(newUser.getUsername(), result.getUsername(), "Username mismatch");
        Assertions.assertNotNull(result.getAuthToken(), "Missing auth token");
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Registration")
    public void unsuccessfulRegistration() {
        TestAuthResult result = serverFacade.register(existingUser);
        new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
    }

    @Test
    @Order(3)
    @DisplayName("Successful Login")
    public void successfulLogin() {
        TestAuthResult result = serverFacade.login(existingUser);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertEquals(existingUser.getUsername(), result.getUsername(), "Username mismatch");
    }

    @Test
    @Order(4)
    @DisplayName("Unsuccessful Login")
    public void unsuccessfulLogin() {
        TestUser badUser = new TestUser(existingUser.getUsername(), "wrongPassword", existingUser.getEmail());
        TestAuthResult result = serverFacade.login(badUser);
        new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    @Order(5)
    @DisplayName("Successful Logout")
    public void successfulLogout() {
        TestResult result = serverFacade.logout(existingAuth);
        new StandardAPITests().assertHttpOk(result);
    }

    @Test
    @Order(6)
    @DisplayName("Unsuccessful Logout")
    public void unsuccessfulLogout() {
        TestResult result = serverFacade.logout("invalidAuthToken");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @Test
    @Order(7)
    @DisplayName("Successful Game Creation")
    public void successfulGameCreation() {
        TestCreateResult result = serverFacade.createGame(createRequest, existingAuth);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertTrue(result.getGameID() > 0, "Invalid game ID");
    }

    @Test
    @Order(8)
    @DisplayName("Unsuccessful Game Creation")
    public void unsuccessfulGameCreation() {
        TestCreateResult result = serverFacade.createGame(createRequest, "invalidAuth");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @Test
    @Order(9)
    @DisplayName("Successful Game List")
    public void successfulGameList() {
        serverFacade.createGame(createRequest, existingAuth);
        TestListResult result = serverFacade.listGames(existingAuth);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertTrue(result.getGames().length > 0, "No games returned");
    }

    @Test
    @Order(10)
    @DisplayName("Unsuccessful Game List")
    public void unsuccessfulGameList() {
        TestListResult result = serverFacade.listGames("invalidAuth");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @Test
    @Order(11)
    @DisplayName("Successful Join Game")
    public void successfulJoinGame() {
        TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        TestResult result = serverFacade.joinPlayer(joinRequest, existingAuth);
        new StandardAPITests().assertHttpOk(result);

    }

    @Test
    @Order(12)
    @DisplayName("Unsuccessful Join Game")
    public void unsuccessfulJoinGame() {
    TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
    TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
    serverFacade.joinPlayer(joinRequest, existingAuth);
    TestAuthResult newAuth = serverFacade.register(newUser);
    TestResult result = serverFacade.joinPlayer(joinRequest, newAuth.getAuthToken());
    new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");

    }

    @Test
    @Order(13)
    @DisplayName("Successful Clear")
    public void successfulClear() {
        serverFacade.createGame(createRequest, existingAuth);
        TestResult clearResult = serverFacade.clear();
        new StandardAPITests().assertHttpOk(clearResult);
        TestListResult listResult = serverFacade.listGames(existingAuth);
        new StandardAPITests().assertHttpUnauthorized(listResult);
        TestAuthResult loginResult = serverFacade.login(existingUser);
        new StandardAPITests().assertHttpUnauthorized(loginResult);
    }

    @Test
    @Order(14)
    @DisplayName("Clear Empty Database")
    public void clearEmptyDatabase() {
        TestResult clearResult = serverFacade.clear();
        new StandardAPITests().assertHttpOk(clearResult);
        TestAuthResult newAuth = serverFacade.register(newUser);
        TestListResult listResult = serverFacade.listGames(newAuth.getAuthToken());
        new StandardAPITests().assertHttpOk(listResult);
        Assertions.assertEquals(0, listResult.getGames().length, "Database not empty after clear");
    }

     private void clearData() {
        serverFacade.clear();
    }

    private void registerExistingUser() {
        TestAuthResult regResult = serverFacade.register(existingUser);
        existingAuth = regResult.getAuthToken();
    }
}
