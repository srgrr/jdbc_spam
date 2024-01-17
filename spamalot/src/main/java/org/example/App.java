package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {

    private final String URL = "jdbc:mysql://localhost:3306/example";
    private final String USERNAME = "root";
    private final String PASSWORD = "root";

    private final int numThreads;
    private final int sleepMillis;

    private class ConnectionOpener extends Thread {

        private final int threadId;
        private final int sleepMillis;

        ConnectionOpener(int threadId, int sleepMillis) {
            this.threadId = threadId;
            this.sleepMillis = sleepMillis;
        }

        public void run() {
            Connection conn = null;
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException("Get connection failed in thread" + threadId + " with:\n" + e);
            }
            // Sleep for a while to enforce connection dropout
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Query the DB
            try {
                final String query = "SELECT * FROM MY_TABLE";
                Statement stmt = conn.createStatement();
                stmt.execute(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Thread " + threadId + " finished");
        }
    }

    public App(int numThreads, int sleepMillis) {
        this.numThreads = numThreads;
        this.sleepMillis = sleepMillis;
    }

    public void run() {
        List< Thread > connectionOpeners = new ArrayList<>();
        for (int i = 0; i < numThreads; ++i) {
            connectionOpeners.add(new ConnectionOpener(i, sleepMillis));
        }
        connectionOpeners.forEach(Thread::start);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        int numThreads = Integer.parseInt(args[0]);
        int sleepMillis = Integer.parseInt(args[1]);
        new App(numThreads, sleepMillis).run();
    }
}
