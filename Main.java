package cmis202honors;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Main extends Application {

    static protected String[] decryptionTypes = {"Classical", "Quantum"}; // How the user wants to find the prime factors

    // See App.java for details
    // static protected Map<String, Integer> encryptionTypesMap = new HashMap<>();

    static private boolean outputVisible = false;

    public void start(Stage primaryStage) {
        
        // Begin nodes

        FlowPane fullPane = new FlowPane(Orientation.HORIZONTAL);
        fullPane.setAlignment(Pos.CENTER);
        fullPane.setPadding(new Insets(15));
        // fullPane.setHgap(-100); // I actually had no idea this would work.

        FlowPane outputPane = new FlowPane(Orientation.HORIZONTAL);
        outputPane.setAlignment(Pos.CENTER);
        // outputPane.setHgap(-250);

        FlowPane optionsPane = new FlowPane(Orientation.VERTICAL);
        optionsPane.setAlignment(Pos.CENTER);
        // optionsPane.setHgap(-250);

        FlowPane decryptionPane = new FlowPane(Orientation.HORIZONTAL);
        decryptionPane.setAlignment(Pos.CENTER);
        decryptionPane.setHgap(20);
            
        FlowPane encryptionExponentPane = new FlowPane(Orientation.HORIZONTAL);
        encryptionExponentPane.setAlignment(Pos.CENTER);
        encryptionExponentPane.setHgap(20);

        FlowPane largeNumberPane = new FlowPane(Orientation.HORIZONTAL);
        largeNumberPane.setAlignment(Pos.CENTER);
        largeNumberPane.setHgap(20);

        FlowPane cipherTextPane = new FlowPane(Orientation.HORIZONTAL);
        cipherTextPane.setAlignment(Pos.CENTER);
        cipherTextPane.setHgap(20);

        // Nodes
        Label title = new Label("Let's get cracking!");
        title.setAlignment(Pos.CENTER);

        Label decryptionLabel = new Label("Choose the decryption algorithm:");
        Button decryptionButton = new Button("[CLICK TO CHOOSE]");
        ListView<String> decryptionView = new ListView<>(FXCollections.observableArrayList(decryptionTypes));
        decryptionView.setMaxHeight(48.0); // 24 is the length in pixels of each bar. Only shows two bars at a time.
        decryptionView.setMinHeight(24.0);
        decryptionView.setVisible(false);

        Label encryptionExponentLabel = new Label("Enter an encryption exponent found in the public key here:");
        TextField encryptionExponentField = new TextField("Enter number here");
        Label encryptionExponentWarningLabel = new Label("This number is invalid."); // Shows up in red text if the number entered is not valid.
        encryptionExponentWarningLabel.setTextFill(Color.RED);
        encryptionExponentWarningLabel.setVisible(false);

        Label largeNumberLabel = new Label("Enter the large semiprime number found in the public key here:");
        TextField largeNumberField = new TextField("Enter number here");
        Label largeNumberWarningLabel = new Label("This number is invalid.");
        largeNumberWarningLabel.setTextFill(Color.RED);
        largeNumberWarningLabel.setVisible(false);

        Label cipherTextLabel = new Label("Enter the ciphertext here:");
        TextField cipherTextField = new TextField("Enter text here");
        Label cipherTextWarningLabel = new Label("This string is invalid."); // Not sure when it will be invalid, but we'll see.
        cipherTextWarningLabel.setTextFill(Color.RED);
        cipherTextWarningLabel.setVisible(false);

        // Label 

        Button calculateButton = new Button("Calculate");
        calculateButton.setAlignment(Pos.CENTER);

        Label output = new Label("WIP"); // Will be changed later to show output
        output.setAlignment(Pos.CENTER);
        output.setVisible(false);

        // End nodes

        // Begin event handlers

        // visible = not visible (logically negates the boolean value)
        decryptionButton.setOnAction(_ -> decryptionView.setVisible(!decryptionView.visibleProperty().getValue()));
        // qubitsButton.setOnAction(_ -> qubitsView.setVisible(!qubitsView.visibleProperty().getValue()));

        // Changes button text to the selection, seems like a nice QOL.
        decryptionView.getSelectionModel().selectedItemProperty().addListener(_ -> {
            decryptionButton.setText(decryptionView.getSelectionModel().getSelectedItem());
        });
        // qubitsView.getSelectionModel().selectedItemProperty().addListener(_ -> {
        //     qubitsButton.setText("" + qubitsView.getSelectionModel().getSelectedItem());
        // });

        // calculation
        calculateButton.setOnAction(_ -> {
            if (!outputVisible) 
                output.setVisible(true);

            int decryptionOption = decryptionView.getSelectionModel().getSelectedIndex();
            int largeNumber = 0;
            int encryptionExponent = 0;
            int cipherText = 0;

            if (!checkLargeNumberValidity(largeNumberField.getText())) {
                largeNumberWarningLabel.setVisible(true);
                return;
            }
            else {
                largeNumber = Integer.parseInt(largeNumberField.getText());
            }

            if (!checkEncryptionExponentValidity(encryptionExponentField.getText())) {
                encryptionExponentWarningLabel.setVisible(true);
                return;
            }
            else {
                encryptionExponent = Integer.parseInt(encryptionExponentField.getText());
            }

            if (!checkCiphertextValidity(cipherTextField.getText())) {
                cipherTextWarningLabel.setVisible(true);
                return;
            }
            else {
                cipherText = Integer.parseInt(cipherTextField.getText());
            }

            // Everything goes okay; hide any warning labels.
            largeNumberWarningLabel.setVisible(false);
            encryptionExponentWarningLabel.setVisible(false);
            
            int[] publicKey = {largeNumber, encryptionExponent};
            int[] privateKey = null;
            int message = 0;

            Timer timer = new Timer();

            // Classical
            if (decryptionOption == 0) {
                timer.start();
                privateKey = Classical.findPrivateKey(publicKey);
                timer.stop();
            }

            // Quantum
            else if (decryptionOption == 1) {
                timer.start();
                privateKey = Quantum.findPrivateKey(publicKey);
                timer.stop();
            }

            System.out.println("decryption took " + timer.getRecordedTime() + " milliseconds");

            output.setText("Private key: (" + privateKey[0] + ", " + privateKey[1] + ")");

        });
        
        // End event handlers

        // adding nodes to panes

        decryptionPane.getChildren().addAll(decryptionLabel, decryptionButton, decryptionView);
        encryptionExponentPane.getChildren().addAll(encryptionExponentLabel, encryptionExponentField, encryptionExponentWarningLabel);
        largeNumberPane.getChildren().addAll(largeNumberLabel, largeNumberField, largeNumberWarningLabel);
        cipherTextPane.getChildren().addAll(cipherTextLabel, cipherTextField, cipherTextWarningLabel);
        optionsPane.getChildren().addAll(decryptionPane, encryptionExponentPane, largeNumberPane, cipherTextPane);
        outputPane.getChildren().addAll(calculateButton, output);
        fullPane.getChildren().addAll(title, optionsPane, outputPane);

        // end adding nodes to panes

        // scene set up

        Scene scene = new Scene(fullPane);
        primaryStage.setTitle("Quantum Decryptor"); // Wow, very sci-fi.
        primaryStage.setScene(scene);
        primaryStage.show();

        // Print example to console
        printExample();

    }

    public static void main(String[] args) {
        launch(args);
    }

    // Literally the same implementation but I guess it's important to have just in case I need to deal with more specific cases
    private static boolean checkLargeNumberValidity(String largeNumber) {
        try {
            Integer.parseInt(largeNumber);
        }
        catch (Exception exception) {
            return false;
        }
        return true;
    }
    private static boolean checkEncryptionExponentValidity(String encryptionExponent) {
        try {
            Integer.parseInt(encryptionExponent);
        }
        catch (Exception exception) {
            return false;
        }
        return true;
    }
    private static boolean checkCiphertextValidity(String cipherText) {
        try {
            Integer.parseInt(cipherText);
        }
        catch (Exception exception) {
            return false;
        }
        return true;
    }

    // for debugging or just trying things out
    // printed out onto the console
    private static void printExample() {

        System.out.println("Generating example...");

        // Initialize coprimes
        HashMap<Character, Integer> coprimes = new HashMap<>();
        coprimes.put('a', 1);
        coprimes.put('b', 1);

        // go up to first 15 primes so that the coprimes aren't overwhelmingly massive.
        int[] primes = {2, 3, 5, 7, 9, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43};

        int[] currentPair = new int[5];
        for (int pairs = 0; pairs < 3; pairs++) {
            
            // Generate a pair of 5 primes
            int start = pairs * 5;
            for (int index = start; index < start + 5; index++) {
                currentPair[index - start] = primes[index];
            }

            // System.out.println("current pair: " + Arrays.toString(currentPair));

            // Select random indices from pair
            int firstSelectionIndex = (int) (Math.random() * 5);
            int secondSelectionIndex = 0;
            do {
                secondSelectionIndex = (int) (Math.random() * 5);
            } while (secondSelectionIndex == firstSelectionIndex);

            // System.out.println("first random number from pair: " + currentPair[firstSelectionIndex]);
            // System.out.println("second random number from pair: " + currentPair[secondSelectionIndex]);

            // multiply coprimes w/ random values from the pair
            coprimes.put('a', coprimes.get('a') * currentPair[firstSelectionIndex]);
            coprimes.put('b', coprimes.get('b') * currentPair[secondSelectionIndex]);

        }

        int modulus = coprimes.get('a') * coprimes.get('b');
        System.out.println("calculated modulus " + modulus + " from coprime factors " + coprimes.get('a') + " and " + coprimes.get('b'));

        // carmichael totient (lambda modulus)
        int lambdaN = Classical.lcm(coprimes.get('a') - 1, coprimes.get('b') - 1);
        System.out.println("calculated lambdaN: " + lambdaN);

        // encryption exponent
        // smaller = more efficient encryption
        // current implementation feels stupid but I can't explain why
        int encryptionExponent = 2;
        do {
            encryptionExponent++;
        } while (encryptionExponent < lambdaN && Classical.gcd(lambdaN, encryptionExponent) != 1);
        System.out.println("calculated encryption exponent: " + encryptionExponent);

        // decryption exponent
        int decryptionExponent = Classical.extendedEuclid(encryptionExponent, lambdaN)[1];
        System.out.println("calculated decryption exponent: " + decryptionExponent);

        System.out.println("Done! Try plugging these numbers into the fields to try it out!");

    }
    
}
