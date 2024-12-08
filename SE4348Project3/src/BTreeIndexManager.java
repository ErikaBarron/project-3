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
            printMenu(); // print menu
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase(); // trim removes whitespace from front and back
            // and converts to lowercase

            try {
                switch (command) {
                    case "create":
                        createFile(scanner); // works
                        break;
                    case "open":
                        openFile(scanner);//works
                        break;
                    case "insert":
                        insert(scanner);// works
                        break;
                    case "search":
                        search(scanner);//works
                        break;
                    case "load":
                        load(scanner);//works for other files(test2, output) except for test which was created here
                        break;
                    case "print":
                        printIndex();//works
                        break;
                    case "extract":
                        extract(scanner);// i think works
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
                //The error "Invalid magic number in file" occurs when the open command validates the file by
            //checking the first 8 bytes (magic number) and finds that it does not match the expected value (4337PRJ3)
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

        // Read the root block ID from the header
        currentFile.seek(8); // Offset to root node block ID in the header
        long rootBlockId = currentFile.readLong();

        if (rootBlockId == 0) {
            // If the tree is empty, create the root node
            rootBlockId = createNode(0); // Root node has no parent
            currentFile.seek(8); // Update the root node ID in the header
            currentFile.writeLong(rootBlockId);
        }

        // Insert the key-value pair into the tree
        boolean success = insertIntoNode(rootBlockId, key, value);
        if (success) {
            System.out.println("Inserted key: " + key + ", value: " + value);
        } else {
            System.out.println("Error: Key " + key + " already exists in the index.");
        }
    }

    private long createNode(long parentBlockId) throws IOException {
        currentFile.seek(16); // Offset to the next block ID in the header
        long nextBlockId = currentFile.readLong();

        // Create a new node
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        buffer.putLong(nextBlockId); // Block ID
        buffer.putLong(parentBlockId); // Parent block ID
        buffer.putLong(0); // Number of key-value pairs (initially 0)
        for (int i = 0; i < 19; i++) buffer.putLong(0); // Empty keys
        for (int i = 0; i < 19; i++) buffer.putLong(0); // Empty values
        for (int i = 0; i < 20; i++) buffer.putLong(0); // Empty child pointers

        currentFile.seek(nextBlockId * BLOCK_SIZE);
        currentFile.write(buffer.array());

        // Update the next block ID in the header
        currentFile.seek(16);
        currentFile.writeLong(nextBlockId + 1);

        return nextBlockId;
    }

    private boolean insertIntoNode(long blockId, long key, long value) throws IOException {
        currentFile.seek(blockId * BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        currentFile.read(buffer.array());

        long nodeBlockId = buffer.getLong(); // Block ID of this node
        buffer.getLong(); // Parent block ID
        int numPairs = (int) buffer.getLong(); // Number of key-value pairs

        long[] keys = new long[19];
        long[] values = new long[19];
        long[] childPointers = new long[20];

        // Read keys, values, and child pointers
        for (int i = 0; i < 19; i++) keys[i] = buffer.getLong();
        for (int i = 0; i < 19; i++) values[i] = buffer.getLong();
        for (int i = 0; i < 20; i++) childPointers[i] = buffer.getLong();

        // Find the position to insert
        int pos = 0;
        while (pos < numPairs && keys[pos] < key) pos++;

        if (pos < numPairs && keys[pos] == key) {
            // Key already exists
            return false;
        }

        if (childPointers[pos] == 0) {
            // Leaf node: insert key-value pair here
            for (int i = numPairs; i > pos; i--) {
                keys[i] = keys[i - 1];
                values[i] = values[i - 1];
            }
            keys[pos] = key;
            values[pos] = value;

            numPairs++;
            buffer.position(16); // Reset buffer to numPairs position
            buffer.putLong(numPairs);
            for (int i = 0; i < 19; i++) buffer.putLong(keys[i]);
            for (int i = 0; i < 19; i++) buffer.putLong(values[i]);
            for (int i = 0; i < 20; i++) buffer.putLong(childPointers[i]);

            currentFile.seek(blockId * BLOCK_SIZE);
            currentFile.write(buffer.array());
            return true;
        } else {
            // Recur to the appropriate child node
            return insertIntoNode(childPointers[pos], key, value);
        }
    }


    private void search(Scanner scanner) throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        System.out.print("Enter key to search: ");
        long key = Long.parseUnsignedLong(scanner.nextLine().trim());

        // Read the root block ID from the header
        currentFile.seek(8); // Offset to root node block ID in the header
        long rootBlockId = currentFile.readLong();

        if (rootBlockId == 0) {
            System.out.println("The index is empty.");
            return;
        }

        // Perform the search starting from the root node
        long value = searchInNode(rootBlockId, key);
        if (value == -1) {
            System.out.println("Key " + key + " not found.");
        } else {
            System.out.println("Found key: " + key + ", value: " + value);
        }
    }

    private long searchInNode(long blockId, long key) throws IOException {
        if (blockId == 0) return -1; // No node to search

        currentFile.seek(blockId * BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        currentFile.read(buffer.array());

        buffer.getLong(); // Block ID of this node
        buffer.getLong(); // Parent block ID
        int numPairs = (int) buffer.getLong(); // Number of key-value pairs

        long[] keys = new long[19];
        long[] values = new long[19];
        long[] childPointers = new long[20];

        // Read keys, values, and child pointers
        for (int i = 0; i < 19; i++) keys[i] = buffer.getLong();
        for (int i = 0; i < 19; i++) values[i] = buffer.getLong();
        for (int i = 0; i < 20; i++) childPointers[i] = buffer.getLong();

        // Perform binary search or linear search to find the key
        for (int i = 0; i < numPairs; i++) {
            if (keys[i] == key) {
                return values[i]; // Key found
            } else if (key < keys[i]) {
                // Traverse left child pointer
                return searchInNode(childPointers[i], key);
            }
        }

        // Traverse rightmost child pointer if key is greater than all keys in this node
        return searchInNode(childPointers[numPairs], key);
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
                if (parts.length != 2) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                try {
                    long key = Long.parseUnsignedLong(parts[0].trim());
                    long value = Long.parseUnsignedLong(parts[1].trim());
                    // Call the insert logic here
                    System.out.println("Loaded key: " + key + ", value: " + value);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid line (not numbers): " + line);
                }
            }
        }
    }


    private void printIndex() throws IOException {
        if (currentFile == null) {
            System.out.println("Error: No file is open.");
            return;
        }

        currentFile.seek(8); // Offset to the root node block ID in the header
        long rootBlockId = currentFile.readLong();

        if (rootBlockId == 0) {
            System.out.println("The index is empty.");
            return;
        }

        System.out.println("Index contents:");
        printNode(rootBlockId);
    }

    private void printNode(long blockId) throws IOException {
        if (blockId == 0) return; // No node to process

        currentFile.seek(blockId * BLOCK_SIZE); // Seek to the start of the block
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        currentFile.read(buffer.array());

        long nodeBlockId = buffer.getLong(); // Block ID of this node
        buffer.getLong(); // Skip parent block ID
        int numPairs = (int) buffer.getLong(); // Number of key-value pairs

        long[] keys = new long[19];
        long[] values = new long[19];
        long[] childPointers = new long[20];

        // Read keys, values, and child pointers
        for (int i = 0; i < 19; i++) {
            keys[i] = buffer.getLong();
        }
        for (int i = 0; i < 19; i++) {
            values[i] = buffer.getLong();
        }
        for (int i = 0; i < 20; i++) {
            childPointers[i] = buffer.getLong();
        }

        // Print child pointers before the keys
        for (int i = 0; i <= numPairs; i++) {
            printNode(childPointers[i]); // Recursively print the child
            if (i < numPairs) {
                System.out.println("Key: " + keys[i] + ", Value: " + values[i]);
            }
        }
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
                System.out.println("Extraction aborted.");
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            currentFile.seek(8); // Offset to the root block ID in the header
            long rootBlockId = currentFile.readLong();

            if (rootBlockId == 0) {
                System.out.println("The index is empty. No data to extract.");
                return;
            }

            // Perform extraction starting from the root node
            extractNode(rootBlockId, writer);
            System.out.println("Index successfully extracted to " + fileName);
        } catch (IOException e) {
            System.out.println("Error during extraction: " + e.getMessage());
        }
    }

    private void extractNode(long blockId, BufferedWriter writer) throws IOException {
        if (blockId == 0) return; // No node to process

        currentFile.seek(blockId * BLOCK_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        currentFile.read(buffer.array());

        buffer.getLong(); // Block ID of this node
        buffer.getLong(); // Parent block ID
        int numPairs = (int) buffer.getLong(); // Number of key-value pairs

        long[] keys = new long[19];
        long[] values = new long[19];
        long[] childPointers = new long[20];

        // Read keys, values, and child pointers
        for (int i = 0; i < 19; i++) keys[i] = buffer.getLong();
        for (int i = 0; i < 19; i++) values[i] = buffer.getLong();
        for (int i = 0; i < 20; i++) childPointers[i] = buffer.getLong();

        // Process child pointers and write key-value pairs
        for (int i = 0; i <= numPairs; i++) {
            extractNode(childPointers[i], writer); // Recursively process children
            if (i < numPairs) {
                writer.write(keys[i] + "," + values[i]);
                writer.newLine();
            }
        }
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
