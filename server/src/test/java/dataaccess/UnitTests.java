package dataaccess;

import org.junit.jupiter.api.*;
import chess.ChessGame;
import passoff.model.*;
import passoff.server.StandardAPITests;
import passoff.server.TestServerFacade;
import java.net.HttpURLConnection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {
    private static TestServerFacade serverFacade;
    private static TestUser existingUser;
    private static TestUser newUser;
    private static TestCreateRequest createRequest;
    private static String existingAuth;

    enum StorageType { MEMORY, SQL }

    @BeforeAll
    static void init() {
        StandardAPITests.init(); // Default to memory storage
        serverFacade = StandardAPITests.serverFacade;
        existingUser = StandardAPITests.existingUser;
        newUser = StandardAPITests.newUser;
        createRequest = StandardAPITests.createRequest;
    }

    void setupStorage(StorageType storageType) {
        if (storageType == StorageType.SQL) {
            System.out.println("Running tests with SQL Storage...");
        } else {
            System.out.println("Running tests with Memory Storage...");
        }
        serverFacade = StandardAPITests.serverFacade;
        clearData();
        registerExistingUser();
    }
    

    @BeforeEach
    void setup() {
        clearData();
        registerExistingUser();
    }


    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(1)
    @DisplayName("Successful Registration")
    public void successfulRegistration(StorageType storageType) {
        setupStorage(storageType);
        TestAuthResult result = serverFacade.register(newUser);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertEquals(newUser.getUsername(), result.getUsername(), "Username mismatch");
        Assertions.assertNotNull(result.getAuthToken(), "Missing auth token");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(2)
    @DisplayName("Unsuccessful Registration")
    public void unsuccessfulRegistration(StorageType storageType) {
        setupStorage(storageType);
        TestAuthResult result = serverFacade.register(existingUser);
        new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(3)
    @DisplayName("Successful Login")
    public void successfulLogin(StorageType storageType) {
        setupStorage(storageType);
        TestAuthResult result = serverFacade.login(existingUser);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertEquals(existingUser.getUsername(), result.getUsername(), "Username mismatch");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(4)
    @DisplayName("Unsuccessful Login")
    public void unsuccessfulLogin(StorageType storageType) {
        setupStorage(storageType);
        TestUser badUser = new TestUser(existingUser.getUsername(), "wrongPassword", existingUser.getEmail());
        TestAuthResult result = serverFacade.login(badUser);
        new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(5)
    @DisplayName("Successful Logout")
    public void successfulLogout(StorageType storageType) {
        setupStorage(storageType);
        TestResult result = serverFacade.logout(existingAuth);
        new StandardAPITests().assertHttpOk(result);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(6)
    @DisplayName("Unsuccessful Logout")
    public void unsuccessfulLogout(StorageType storageType) {
        setupStorage(storageType);
        TestResult result = serverFacade.logout("invalidAuthToken");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(7)
    @DisplayName("Successful Game Creation")
    public void successfulGameCreation(StorageType storageType) {
        setupStorage(storageType);
        TestCreateResult result = serverFacade.createGame(createRequest, existingAuth);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertTrue(result.getGameID() > 0, "Invalid game ID");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(8)
    @DisplayName("Unsuccessful Game Creation")
    public void unsuccessfulGameCreation(StorageType storageType) {
        setupStorage(storageType);
        TestCreateResult result = serverFacade.createGame(createRequest, "invalidAuth");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(9)
    @DisplayName("Successful Game List")
    public void successfulGameList(StorageType storageType) {
        setupStorage(storageType);
        serverFacade.createGame(createRequest, existingAuth);
        TestListResult result = serverFacade.listGames(existingAuth);
        new StandardAPITests().assertHttpOk(result);
        Assertions.assertTrue(result.getGames().length > 0, "No games returned");
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(10)
    @DisplayName("Unsuccessful Game List")
    public void unsuccessfulGameList(StorageType storageType) {
        setupStorage(storageType);
        TestListResult result = serverFacade.listGames("invalidAuth");
        new StandardAPITests().assertHttpUnauthorized(result);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(11)
    @DisplayName("Successful Join Game")
    public void successfulJoinGame(StorageType storageType) {
        setupStorage(storageType);
        TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        TestResult result = serverFacade.joinPlayer(joinRequest, existingAuth);
        new StandardAPITests().assertHttpOk(result);

    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(12)
    @DisplayName("Unsuccessful Join Game")
    public void unsuccessfulJoinGame(StorageType storageType) {
        setupStorage(storageType);
        TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        serverFacade.joinPlayer(joinRequest, existingAuth);
        TestAuthResult newAuth = serverFacade.register(newUser);
        TestResult result = serverFacade.joinPlayer(joinRequest, newAuth.getAuthToken());
        new StandardAPITests().assertHttpError(result, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");

    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(13)
    @DisplayName("Successful Clear")
    public void successfulClear(StorageType storageType) {
        setupStorage(storageType);
        serverFacade.createGame(createRequest, existingAuth);
        TestResult clearResult = serverFacade.clear();
        new StandardAPITests().assertHttpOk(clearResult);
        TestListResult listResult = serverFacade.listGames(existingAuth);
        new StandardAPITests().assertHttpUnauthorized(listResult);
        TestAuthResult loginResult = serverFacade.login(existingUser);
        new StandardAPITests().assertHttpUnauthorized(loginResult);
    }

    @ParameterizedTest
    @EnumSource(StorageType.class)
    @Order(14)
    @DisplayName("Clear Empty Database")
    public void clearEmptyDatabase(StorageType storageType) {
        setupStorage(storageType);
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
