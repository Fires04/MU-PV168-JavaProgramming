package cz.muni.fi.pv168.clockcard;

import java.util.Properties;
import java.util.ArrayList;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marek Osvald
 */

public class WorkerTest {
    private static final Properties PROPERTIES = Worker.loadProperties();
    private static final String[] NAMES = {"Radek", "Jirina", "Vit", "Dita", "Petr", "Zdenek", "Ludmila", "Alena", "Barbora", "Vladan"};
    private static final String[] SURNAMES = {"Houha", "Flekova", "Pech", "Knourkova", "Pour", "Polacek", "Urvalkova", "Kahankova", "Boryskova", "Ferus" };
    private static final String[] LOGINS = {"rhouha", "jflekova", "vpech", "dknourkova", "ppour", "zpolacek", "lurvalkova", "akahankova", "bboryskova", "vferus"};
    private static final String[] PASSWORDS = {"wolverine", "kopretina", "saddam", "milacek", "sylva", "obelix", "kotatko", "inuyasha", "kreslo", "vladivostok"};
    private static final Long[] CURRENT_SHIFTS = {null, null, Long.valueOf(123), null, null, Long.valueOf(456), null, Long.valueOf(789), null, null};
    private static final boolean[] SUSPENSIONS = {false, false, false, false, false, false, false, true, false, true};
    
    private Worker joe, bill;

    public WorkerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        joe = new Worker("Joe", "Smith", "joe.smith");
        bill = new Worker("Bill", "Newman", "bill.newman");
    }

    @After
    public void tearDown() {
        joe = null;
        bill = null;
    }

    /**
     * Test of resetForgottenPassword method, of class Worker.
     */
    @Test
    public void testResetForgottenPassword() {
        String defaultPassword = PROPERTIES.getProperty("defaultPassword");
        String newPassword = "SomeSecretPassword";

        assertFalse("Joe should not be suspended.", joe.isSuspended());

        if (newPassword.equals(defaultPassword)) {
            newPassword += ":)";
        }

        assertTrue("Joe's password should be default password from the property file.",
                   joe.authenticate(PROPERTIES.getProperty("defaultPassword")));
        joe.setPassword(newPassword);
        assertFalse("Joe's password should differ from the default password",
                    joe.authenticate(PROPERTIES.getProperty(defaultPassword)));
        assertTrue("Joe's password should be the new specified ", joe.authenticate(newPassword));
    }

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(PROPERTIES.getProperty("testDatabase"),
                                                     PROPERTIES.getProperty("testLogin"),
                                                     PROPERTIES.getProperty("testPassword"));
        } catch (SQLException e) {
            fail("Unable to establish connection.");
        }
        return connection;
    }

    private void resetTable() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate("DROP TABLE APP.workers");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("42Y55")) {
                throw e;
            }
        }
        statement.close();

        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE APP.workers(ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                              + "NAME VARCHAR(30),"
                              + "SURNAME VARCHAR(30),"
                              + "LOGIN VARCHAR(30) not null,"
                              + "PASSWORD VARCHAR(30) not null,"
                              + "CURRENT_SHIFT INTEGER,"
                              + "SUSPENDED SMALLINT)");
        statement.close();
        
        for (int i = 0; i < 10; i++) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO APP.workers (name, surname, login, password, current_shift, suspended) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, NAMES[i]);
            preparedStatement.setString(2, SURNAMES[i]);
            preparedStatement.setString(3, LOGINS[i]);
            preparedStatement.setString(4, PASSWORDS[i]);
            if (CURRENT_SHIFTS[i] != null) {
                preparedStatement.setLong(5, CURRENT_SHIFTS[i]);
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }
            preparedStatement.setBoolean(6, SUSPENSIONS[i]);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
        connection.close();
    }

    /**
     * Truncates the table and then inserts predefined values. Checks whether
     * created objects are equal.
     */
    @Test
    public void testAll() {
        try {
            resetTable();
        } catch (SQLException ex) {
            fail("Unable to reset table");
        }

        ArrayList<Worker> originalWorkers = new ArrayList<Worker>();

        for(int i = 0; i < 10; i++) {
            Worker worker = new Worker(NAMES[i], SURNAMES[i], LOGINS[i]);
            worker.setPassword(PASSWORDS[i]);
            if (SUSPENSIONS[i]) {
                try {
                    worker.suspend();
                } catch (WorkerException ex) {
                    fail("Worker suspension failed");
                }
            }

            originalWorkers.add(worker);
        }

        ArrayList<Worker> dbWorkers = new ArrayList<Worker>();
        try {
            Worker.testingOn();
            dbWorkers.addAll(Worker.all());
            Worker.testingOff();
        } catch (SQLException ex) {
            fail("Workers retrieval caused an unexpected WorkerException.");
        }

        assertTrue(dbWorkers.equals(originalWorkers));
    }

    @Test
    public void testCount() {
        try {
            resetTable();
        } catch (SQLException ex) {
            fail("Unable to reset table.");
        }

        int count = 0;
        try {
            Worker.testingOn();
            count = Worker.count();
            Worker.testingOff();
        } catch (SQLException ex) {
            fail("Unable to count workers.");
        }
        assertEquals("Amount of workers do not match.", LOGINS.length, count);
    }

    @Test
    public void testFind() {
        try {
            resetTable();
        } catch (SQLException e) {
            fail("Unable to reset table.");
        }

        for (int i = 0; i < LOGINS.length; i++) {
            Worker worker = new Worker(NAMES[i], SURNAMES[i], LOGINS[i]);
            worker.setPassword(PASSWORDS[i]);
            if (SUSPENSIONS[i]) {
                try {
                    worker.suspend();
                } catch (WorkerException ex) {
                    fail("Unable to suspend worker.");
                }
            }
            try {
                Worker.testingOn();
                assertEquals(worker, Worker.find(i+1));
                Worker.testingOff();
            } catch (SQLException e) {
                fail("Unable to retrieve worker.");
            }
        }
    }


}
