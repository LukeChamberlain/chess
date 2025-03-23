package client;

import static client.EscapeSequences.*;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_YELLOW + "Welcome to Chess! Type 'help' to begin." + RESET_TEXT_COLOR);

        try (Scanner scanner = new Scanner(System.in)) {
            var result = "";
            while (!result.equals("quit")) {
                printPrompt();
                String line = scanner.nextLine().trim();

                try {
                    result = client.eval(line);
                    if (result.startsWith("Chess board displayed")) {
                        System.out.print(ERASE_SCREEN); 
                    }
                    System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR);
                } catch (Throwable e) {
                    System.out.print(SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + RESET_TEXT_COLOR);
                }
            }
        }
        System.out.println(SET_TEXT_COLOR_GREEN + "\nThanks for playing!" + RESET_TEXT_COLOR);
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + SET_BG_COLOR_DARK_GREY + "♔ " 
                        + SET_TEXT_COLOR_GREEN + "chess" 
                        + SET_TEXT_COLOR_WHITE + " → " + RESET_TEXT_COLOR);
    }
}