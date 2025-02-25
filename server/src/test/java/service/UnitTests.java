package service;

import org.junit.jupiter.api.*;
import chess.ChessGame;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.*;
import java.util.Locale;
import java.net.HttpURLConnection;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {
    private String existingAuth;
    private static TestUser existingUser;
    private static TestUser newUser;
    private static TestCreateRequest createRequest;
    private static TestServerFacade serverFacade;
    private static Server server;

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
        existingUser = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new TestUser("NewUser", "newUserPassword", "nu@mail.com");
        createRequest = new TestCreateRequest("testGame");
    }

    @BeforeEach
    public void setup() {
        serverFacade.clear();
        TestAuthResult regResult = serverFacade.register(existingUser);
        existingAuth = regResult.getAuthToken();
    }

    @Test
    @Order(1)
    @DisplayName("Successful Registration")
    public void SuccessfulRegistration() {
        TestAuthResult result = serverFacade.register(newUser);
        assertHttpOk(result);
        Assertions.assertEquals(newUser.getUsername(), result.getUsername(), "Username mismatch");
        Assertions.assertNotNull(result.getAuthToken(), "Missing auth token");
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Registration")
    public void UnsuccessfulRegistration() {
        TestAuthResult result = serverFacade.register(existingUser);
        assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
    }

    @Test
    @Order(3)
    @DisplayName("Successful Login")
    public void SuccessfulLogin() {
        TestAuthResult result = serverFacade.login(existingUser);
        assertHttpOk(result);
        Assertions.assertEquals(existingUser.getUsername(), result.getUsername(), "Username mismatch");
    }

    @Test
    @Order(4)
    @DisplayName("Unsuccessful Login")
    public void UnsuccessfulLogin() {
        TestUser badUser = new TestUser(existingUser.getUsername(), "wrongPassword", existingUser.getEmail());
        TestAuthResult result = serverFacade.login(badUser);
        assertHttpError(result, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    @Order(5)
    @DisplayName("Successful Logout")
    public void SuccessfulLogout() {
        TestResult result = serverFacade.logout(existingAuth);
        assertHttpOk(result);
    }

    @Test
    @Order(6)
    @DisplayName("Unsuccessful Logout")
    public void UnsuccessfulLogout() {
        TestResult result = serverFacade.logout("invalidAuthToken");
        assertHttpUnauthorized(result);
    }

    @Order(7)
    @DisplayName("Successful Game Creation")
    public void SuccessfulGameCreation() {
        TestCreateResult result = serverFacade.createGame(createRequest, existingAuth);
        assertHttpOk(result);
        Assertions.assertTrue(result.getGameID() > 0, "Invalid game ID");
    }

    @Test
    @Order(8)
    @DisplayName("Unsuccessful Game Creation")
    public void UnsuccessfulGameCreation() {
        TestCreateResult result = serverFacade.createGame(createRequest, "invalidAuth");
        assertHttpUnauthorized(result);
    }

    @Order(9)
    @DisplayName("Successful Game List")
    public void SuccessfulGameList() {
        serverFacade.createGame(createRequest, existingAuth);
        TestListResult result = serverFacade.listGames(existingAuth);
        assertHttpOk(result);
        Assertions.assertTrue(result.getGames().length > 0, "No games returned");
    }

    @Test
    @Order(10)
    @DisplayName("Unsuccessful Game List")
    public void UnsuccessfulGameList() {
        TestListResult result = serverFacade.listGames("invalidAuth");
        assertHttpUnauthorized(result);
    }

    @Order(11)
    @DisplayName("Successful Join Game")
    public void SuccessfulJoinGame() {
        TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        TestResult result = serverFacade.joinPlayer(joinRequest, existingAuth);
        assertHttpOk(result);

    }

    @Test
    @Order(12)
    @DisplayName("Unsuccessful Join Game")
    public void UnsuccessfulJoinGame() {
    TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
    TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
    serverFacade.joinPlayer(joinRequest, existingAuth);
    TestAuthResult newAuth = serverFacade.register(newUser);
    TestResult result = serverFacade.joinPlayer(joinRequest, newAuth.getAuthToken());
    assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");

    }


    private void assertHttpOk(TestResult result) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK (message: %s)".formatted(result.getMessage()));
        Assertions.assertFalse(result.getMessage() != null &&
                        result.getMessage().toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");
    }

    private void assertHttpUnauthorized(TestResult result) {
        assertHttpError(result, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
    }

    private void assertHttpError(TestResult result, int statusCode, String message) {
        Assertions.assertEquals(statusCode, serverFacade.getStatusCode(),
                "Server response code was not %d %s (message: %s)".formatted(statusCode, message, result.getMessage()));
        Assertions.assertNotNull(result.getMessage(), "Invalid Request didn't return an error message");
        Assertions.assertTrue(result.getMessage().toLowerCase(Locale.ROOT).contains("error"),
                "Error message didn't contain the word \"Error\"");
    }
}
