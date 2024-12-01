import java.util.ArrayList;
import java.util.List;

//public class Main {
//
//class BTreeNode {
//    int t;  //The b-tree should have minimal degree 10. This will give 19 key/value pairs, and 20 child pointers.
//            //Each node will be stored in a single block with some header information.
//    List<Integer> keys;  // List of the keys
//    List<BTreeNode> children;  // List of the children
//    boolean leaf;  // if node doesn't have children = true
//
//    public BTreeNode(int t, boolean leaf) {
//        this.t = t;
//        this.keys = new ArrayList<>();
//        this.children = new ArrayList<>();
//        this.leaf = leaf;
//    }
//}
//    public static void main(String[] args) {
//        System.out.println("Hello world!");
//    }
//}
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class BTreeIndexManager {

    private static final int BLOCK_SIZE = 512;
    private static final String MAGIC_NUMBER = "4337PRJ3";
    private RandomAccessFile currentFile = null;
    private String currentFileName = null;

    public static void main(String[] args) {
        new BTreeIndexManager().start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "create":
                        createFile(scanner);
                        break;
                    case "open":
                        openFile(scanner);
                        break;
                    case "insert":
                        insert(scanner);
                        break;
                    case "search":
                        search(scanner);
                        break;
                    case "load":
                        load(scanner);
                        break;
                    case "print":
                        printIndex();
                        break;
                    case "extract":
                        extract(scanner);
                        break;
                    case "quit":
                        quit();
                        return;
                    default:
                        System.out.println("Invalid command. Please try again.");
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\nCommands:");
        System.out.println("create  - Create a new index file");
        System.out.println("open    - Open an existing index file");
        System.out.println("insert  - Insert a key-value pair into the index");
        System.out.println("search  - Search for a key in the index");
        System.out.println("load    - Load key-value pairs from a file");
        System.out.println("print   - Print all key-value pairs in the index");
        System.out.println("extract - Extract all key-value pairs to a file");
        System.out.println("quit    - Exit the program");
    }

    private void createFile(Scanner scanner) throws IOException {
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine().trim();

        File file = new File(fileName);
        if (file.exists()) {
            System.out.print("File already exists. Overwrite? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Operation aborted.");
                return;
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            ByteBuffer header = ByteBuffer.allocate(BLOCK_SIZE);
            header.put(MAGIC_NUMBER.getBytes());
            header.putLong(0); // Root node block id
            header.putLong(1); // Next block id
            raf.write(header.array());
        }

        currentFile = new RandomAccessFile(file, "rw");
        currentFileName = fileName;
        System.out.println("File created and opened: " + fileName);
    }

    private void openFile(Scanner scanner) throws IOException {
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine().trim();

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Error: File does not exist.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] magic = new byte[8];
            raf.read(magic);
            if (!new String(magic).equals(MAGIC_NUMBER)) {
                System.out.println("Error: Invalid magic number in file.");
                return;
            }
        }

        currentFile = new RandomAccessFile(file, "rw");
        currentFileName = fileName;
        System.out.println("File opened: " + fileName);
    }

    private void insert(Scanner scanner) throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.print("Enter key: ");
        long key = Long.parseUnsignedLong(scanner.nextLine().trim());
        System.out.print("Enter value: ");
        long value = Long.parseUnsignedLong(scanner.nextLine().trim());

        // Simplified insert logic for demonstration
        System.out.println("Inserted key: " + key + ", value: " + value);
    }

    private void search(Scanner scanner) throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.print("Enter key: ");
        long key = Long.parseUnsignedLong(scanner.nextLine().trim());

        // Simplified search logic for demonstration
        System.out.println("Key " + key + " not found.");
    }

    private void load(Scanner scanner) throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.print("Enter file name to load from: ");
        String fileName = scanner.nextLine().trim();

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Error: File does not exist.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                long key = Long.parseUnsignedLong(parts[0].trim());
                long value = Long.parseUnsignedLong(parts[1].trim());
                System.out.println("Loaded key: " + key + ", value: " + value);
            }
        }
    }

    private void printIndex() throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.println("Printing index...");
        // Simplified print logic for demonstration
    }

    private void extract(Scanner scanner) throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.print("Enter file name to extract to: ");
        String fileName = scanner.nextLine().trim();

        File file = new File(fileName);
        if (file.exists()) {
            System.out.print("File already exists. Overwrite? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("yes")) {
                System.out.println("Operation aborted.");
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Extracted data");
        }

        System.out.println("Data extracted to " + fileName);
    }

    private void quit() {
        if (currentFile != null) {
            try {
                currentFile.close();
            } catch (IOException e) {
                System.err.println("Error closing file: " + e.getMessage());
            }
        }
        System.out.println("Exiting...");
    }
}
