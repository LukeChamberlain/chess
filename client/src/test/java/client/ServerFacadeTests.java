package client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;
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
    void SuccessfulRegister() throws Exception {
        TestUser newUser = new TestUser("newUser", "password", "new@email.com");
        TestAuthResult authResult = serverFacade.register(newUser);
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertNotNull(authResult.getAuthToken());
        assertTrue(authResult.getAuthToken().length() > 10);
    }

    @Test
    @Order(2)
    @DisplayName("Unsuccessful Registration")
    void UnsuccessfulRegister() throws Exception {
        TestUser existingUser = new TestUser("existingUser", "password", "existing@email.com");
        serverFacade.register(existingUser);
        TestAuthResult duplicateResult = serverFacade.register(existingUser);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
        assertEquals("Error: already taken", duplicateResult.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("Successful Login")
    void SuccessfulLogin() throws Exception {
        TestUser newUser = new TestUser("newUser", "password", "new@email.com");
        TestAuthResult authResult = serverFacade.register(newUser);
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertNotNull(authResult.getAuthToken());
        assertTrue(authResult.getAuthToken().length() > 10);
    }

    @Test
    @Order(4)
    @DisplayName("Unsuccessful Login")
    void UnsuccessfulLogin() throws Exception {
        TestUser existingUser = new TestUser("existingUser", "password", "existing@email.com");
        serverFacade.register(existingUser);
        TestAuthResult duplicateResult = serverFacade.register(existingUser);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
        assertEquals("Error: already taken", duplicateResult.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("Successful Logout")
    void SuccessfulLogout() throws Exception {
        TestUser newUser = new TestUser("newUser", "password", "new@email.com");
        TestAuthResult authResult = serverFacade.register(newUser);
        assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode());
        assertNotNull(authResult.getAuthToken());
        assertTrue(authResult.getAuthToken().length() > 10);
    }

    @Test
    @Order(6)
    @DisplayName("Unsuccessful Logout")
    void UnsuccessfulLogout() throws Exception {
        TestUser existingUser = new TestUser("existingUser", "password", "existing@email.com");
        serverFacade.register(existingUser);
        TestAuthResult duplicateResult = serverFacade.register(existingUser);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
        assertEquals("Error: already taken", duplicateResult.getMessage());
    }
}
