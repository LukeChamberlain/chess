package client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import chess.ChessGame;
import server.Server;
import passoff.model.*;
import passoff.server.TestServerFacade;
import java.net.HttpURLConnection;



public class ServerFacadeTests {
    private static TestServerFacade serverFacade;
    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        String host = "localhost";
        serverFacade = new TestServerFacade(host, Integer.toString(port));
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() {
        serverFacade.clear(); 
    }

    @Test
    @Order(1)
    @DisplayName("Successful Registration")
    void successfulRegister() throws Exception {
        TestUser newUser = new TestUser("newUser", "password", "new@email.com");
        TestAuthResult authResult = serverFacade.register(newUser);
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertNotNull(authResult.getAuthToken());
        assertTrue(authResult.getAuthToken().length() > 10);
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Registration")
    void unsuccessfulRegister() throws Exception {
        TestUser existingUser = new TestUser("existingUser", "password", "existing@email.com");
        serverFacade.register(existingUser);
        TestAuthResult duplicateResult = serverFacade.register(existingUser);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
        assertEquals("Error: already taken", duplicateResult.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("Successful Login")
    void successfulLogin() throws Exception {
        TestUser user = new TestUser("validUser", "validpassword", "valid@email.com");
        serverFacade.register(user);
        TestAuthResult loginResult = serverFacade.login(user);
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertNotNull(loginResult.getAuthToken());
        assertTrue(loginResult.getAuthToken().length() > 10);
    }

    @Test
    @Order(4)
    @DisplayName("Unsuccessful Login")
    void unsuccessfulLogin() throws Exception {
        TestUser invalidUser = new TestUser("nonExistentUser", "wrongPass", "bad@email.com");
        TestAuthResult loginResult = serverFacade.login(invalidUser);
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        assertEquals("Error: unauthorized", loginResult.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("Successful Logout")
    void successfulLogout() throws Exception {
        TestUser user = new TestUser("logoutUser", "logoutPass", "logout@email.com");
        TestAuthResult authResult = serverFacade.register(user);
        TestResult logoutResult = serverFacade.logout(authResult.getAuthToken());
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Unsuccessful Logout")
    void unsuccessfulLogout() throws Exception {
        TestResult logoutResult = serverFacade.logout("invalidAuthToken");
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        assertEquals("Error: unauthorized", logoutResult.getMessage());
    }

    @Test
    @Order(7)
    @DisplayName("Successful Create")
    void successfulCreate() throws Exception {
        TestUser user = new TestUser("createUser", "createPass", "create@email.com");
        TestAuthResult authResult = serverFacade.register(user);
        TestCreateResult createResult = serverFacade.createGame(
            new TestCreateRequest("Test Game"), 
            authResult.getAuthToken()
        );
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertTrue(createResult.getGameID() > 0);
    }

    @Test
    @Order(8)
    @DisplayName("Unsuccessful Create")
    void unsuccessfulCreate() throws Exception {
        TestCreateResult createResult = serverFacade.createGame(
            new TestCreateRequest("Invalid Game"), 
            "badToken"
        );
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        assertEquals("Error: unauthorized", createResult.getMessage());
    }

    @Test
    @Order(9)
    @DisplayName("Successful List")
    void successfulList() throws Exception {
        TestUser user = new TestUser("listUser", "listPass", "list@email.com");
        TestAuthResult authResult = serverFacade.register(user);
        serverFacade.createGame(new TestCreateRequest("List Game"), authResult.getAuthToken());
        TestListResult listResult = serverFacade.listGames(authResult.getAuthToken());
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertEquals(1, listResult.getGames().length);
    }

    @Test
    @Order(10)
    @DisplayName("Unsuccessful List")
    void unsuccessfulList() throws Exception {
        TestListResult listResult = serverFacade.listGames("invalidToken");
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        assertEquals("Error: unauthorized", listResult.getMessage());
    }

    @Test
    @Order(11)
    @DisplayName("Successful Join")
    void successfulJoin() throws Exception {
        TestUser user = new TestUser("joinUser", "joinPass", "join@email.com");
        TestAuthResult authResult = serverFacade.register(user);
        TestCreateResult game = serverFacade.createGame(
            new TestCreateRequest("Join Game"), 
            authResult.getAuthToken()
        );
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, game.getGameID());
        TestResult joinResult = serverFacade.joinPlayer(joinRequest, authResult.getAuthToken());
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
    }

    @Test
    @Order(12)
    @DisplayName("Unsuccessful Join")
    void unsuccessfulJoin() throws Exception {
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, 9999);
        TestResult joinResult = serverFacade.joinPlayer(joinRequest, "invalidToken");
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        assertEquals("Error: unauthorized", joinResult.getMessage());
    }
}
