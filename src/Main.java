import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Absolute paths to the text and data files
        //String textFilePath = "C:\\Users\\jvull\\IdeaProjects\\PROG_2\\src\\EvenOrOdd.txt";
        //String dataFilePath = "C:\\Users\\jvull\\IdeaProjects\\PROG_2\\src\\EvenOrOdd.data";

        String textFilePath = args[0];
        String dataFilePath = args[1];

        try {
            MIPSsimulator simulator = new MIPSsimulator();
            simulator.loadTextSegment(textFilePath);
            simulator.loadDataSegment(dataFilePath);
            simulator.run(); // Start execution
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }
    }
}