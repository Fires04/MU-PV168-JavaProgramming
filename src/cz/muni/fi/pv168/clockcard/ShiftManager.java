package cz.muni.fi.pv168.clockcard;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * Database backend manager for handling Shift class.
 *
 * @author David Stein
 * @author Marek Osvald
 * @version 2011.0522
 */

/* TODO: Consider refactoring with usage of inheritance. */

public class ShiftManager implements IDatabaseManager {
    private static final String DATABASE_PROPERTY_FILE = "src/Database.properties";
    private static final String CLASS_PROPERTY_FILE = "src/Shift.properties";

    private static ShiftManager instance;

    private final Properties databaseProperties = loadProperties(DATABASE_PROPERTY_FILE);
    private final Properties classProperties = loadProperties(CLASS_PROPERTY_FILE);
    private boolean testingMode;
    private DataSource dataSource;

    /**
     * Returns the sole instance of ShiftManager in the program. Provided that
     * instance has not been created yet, creates one.
     *
     * @return sole ShiftManager instance in the program
     */
    public static ShiftManager getInstance() {
        if (instance == null) {
            instance = new ShiftManager();
        }

        return instance;
    }

    /**
     * Parameterless constructor. Private in order to force creating
     * of ShiftManager solely via getInstance().
     * method.
     */
    private ShiftManager() {
        testingMode = false;
        //dataSource = getProductionDataSource();
          dataSource = getTestingDataSource();
    }

    @Override
    public Shift find(long id) {
        Connection connection = null;
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Shift result = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(classProperties.getProperty("findQuery"));
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.getFetchSize() == 1 && resultSet.next()) {
                Calendar shiftStart = null, shiftEnd = null, lastBreak = null;

                shiftStart = new GregorianCalendar();
                shiftStart.setTimeInMillis(resultSet.getTimestamp("SHIFT_START").getTime());

                if (resultSet.getTimestamp("SHIFT_END") != null) {
                    shiftEnd   = new GregorianCalendar();
                    shiftEnd.setTimeInMillis(resultSet.getTimestamp("SHIFT_END").getTime());
                }
                
                if (resultSet.getTimestamp("LAST_BREAK") != null) {
                    lastBreak  = new GregorianCalendar();
                    shiftEnd.setTimeInMillis(resultSet.getTimestamp("LAST_BREAK").getTime());
                }

                result = Shift.loadShift(resultSet.getLong("ID"),
                                         resultSet.getLong("WORKER_ID"),
                                         shiftStart,
                                         shiftEnd,
                                         lastBreak,
                                         resultSet.getLong("TOTAL_BREAK_TIME"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ShiftManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ShiftManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return result;
    }
    @Override
    public List<? extends IDatabaseStoreable> getAll() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
        ArrayList<Shift> result = null;
        
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(classProperties.getProperty("selectAllQuery"));
            result = new ArrayList<Shift>();

            while (resultSet.next()) {
                Calendar shiftStart = null, shiftEnd = null, lastBreak = null;

                shiftStart = new GregorianCalendar();
                shiftStart.setTimeInMillis(resultSet.getTimestamp("SHIFT_START").getTime());

                if (resultSet.getTimestamp("SHIFT_END") != null) {
                    shiftEnd   = new GregorianCalendar();
                    shiftEnd.setTimeInMillis(resultSet.getTimestamp("SHIFT_END").getTime());
                }

                if (resultSet.getTimestamp("LAST_BREAK") != null) {
                    lastBreak  = new GregorianCalendar();
                    lastBreak.setTimeInMillis(resultSet.getTimestamp("LAST_BREAK").getTime());
                }

                Shift shift = Shift.loadShift(resultSet.getLong("ID"),
                                              resultSet.getLong("WORKER_ID"),
                                              shiftStart,
                                              shiftEnd,
                                              lastBreak,
                                              resultSet.getLong("TOTAL_BREAK_TIME"));
                result.add(shift);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            //TODO: log an exception
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                //log an exception
            }
        }

        if (result != null) {
            return Collections.unmodifiableList(result);
        }

        return null;
    }
  
    public long count() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
        long result = 0;

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(classProperties.getProperty("countQuery"));
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            //TODO: Log error
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    result = -1;
                    //TODO: Log error
                }
            }
        }
        
        return result;
    }
    @Override
    public boolean getTestingMode() {
        return testingMode;
    }
    @Override
    public void testingOn() {
        if (!testingMode) {
            dataSource = getTestingDataSource();
            testingMode = true;
        }
    }
    @Override
    public void testingOff() {
        if (testingMode) {
            dataSource = getProductionDataSource();
            testingMode = false;
        }
    }
    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
    @Override
    public Properties loadProperties(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName cannot be null.");
        }

        FileInputStream inputStream = null;
        Properties _properties = null;

        try {
            inputStream = new FileInputStream(fileName);
            _properties = new Properties();
            _properties.load(inputStream);
        } catch (IOException e) {
            //TODO: LOG fatal error, Property file not found.
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    //TODO: Log error
                }
            }
        }

        if (_properties != null) {
            return _properties;
        }

        return new Properties();
    }

    /**
     * Retrieves all shifts assgined to the worker with given unique ID. Provided
     * that worker's ID is less or equal to zero, throw IllegalArgumentException.
     *
     * @param workerid unique ID of the worker
     *
     * @return shifts assigned to worker with given unique ID
     */
    public List<Shift> findByWorkerID(long workerid) {
        if (workerid < 1) {
            throw new IllegalArgumentException("Worker's ID must be greater than zero.");
        }

        Connection connection = null;
        PreparedStatement statement;
        ResultSet resultSet;
        ArrayList<Shift> result = null;
        try {
            connection = dataSource.getConnection();
            result = new ArrayList<Shift>();
            statement = connection.prepareStatement(classProperties.getProperty("findByWorkerIDQuery"));
            statement.setLong(1, workerid);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Calendar shiftStart = null, shiftEnd = null, lastBreak = null;
                
                shiftStart = new GregorianCalendar();
                shiftStart.setTimeInMillis(resultSet.getTimestamp("SHIFT_START").getTime());

                if (resultSet.getTimestamp("SHIFT_END") != null) {
                    shiftEnd = new GregorianCalendar();
                    shiftEnd.setTimeInMillis(resultSet.getTimestamp("SHIFT_END").getTime());
                }

                if (resultSet.getTimestamp("LAST_BREAK") != null) {
                    lastBreak = new GregorianCalendar();
                    lastBreak.setTimeInMillis(resultSet.getTimestamp("LAST_BREAK").getTime());
                }

                Shift shift = Shift.loadShift(resultSet.getLong("ID"),
                                              resultSet.getLong("WORKER_ID"),
                                              shiftStart,
                                              shiftEnd,
                                              lastBreak,
                                              resultSet.getLong("TOTAL_BREAK_TIME"));
                result.add(shift);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ShiftManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(ShiftManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (result != null) {
            return Collections.unmodifiableList(result);
        }
        return null;
    }
    /**
     * Returns a list of shifts that start between given parameters.
     *
     * @param begin begin of the interval
     * @param end end of the interval
     * @return list of shifts that start between given parameters
     */
    public List<Shift> findStartBetween(Timestamp begin, Timestamp end) {
    Connection connection = null;
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        ArrayList<Shift> result = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(classProperties.getProperty("findBetweenQuery"));
            preparedStatement.setTimestamp(1, begin);
            preparedStatement.setTimestamp(2, end);

            resultSet = preparedStatement.executeQuery();
            result = new ArrayList<Shift>();

            while (resultSet.next()) {
                Calendar shiftStart = null, shiftEnd = null, lastBreak = null;

                shiftStart = new GregorianCalendar();
                shiftStart.setTimeInMillis(resultSet.getTimestamp("SHIFT_START").getTime());

                if (resultSet.getTimestamp("SHIFT_END") != null) {
                    shiftEnd   = new GregorianCalendar();
                    shiftEnd.setTimeInMillis(resultSet.getTimestamp("SHIFT_END").getTime());
                }

                if (resultSet.getTimestamp("LAST_BREAK") != null) {
                    lastBreak  = new GregorianCalendar();
                    lastBreak.setTimeInMillis(resultSet.getTimestamp("LAST_BREAK").getTime());
                }

                Shift shift = Shift.loadShift(resultSet.getLong("ID"),
                                              resultSet.getLong("WORKER_ID"),
                                              shiftStart,
                                              shiftEnd,
                                              lastBreak,
                                              resultSet.getLong("TOTAL_BREAK_TIME"));
                result.add(shift);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            //TODO: log an exception
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                //log an exception
            }
        }

        if (result != null) {
            return Collections.unmodifiableList(result);
        }

        return new ArrayList<Shift>();
    }

    /**
     * Returns new DataSource representing a connection to the testing database.
     * 
     * @return new DataSource representing a connection to the testing database
     */
    private DataSource getTestingDataSource() {
        BasicDataSource testingDataSource = new BasicDataSource();
        testingDataSource.setDriverClassName(databaseProperties.getProperty("driverName"));
        testingDataSource.setUrl(databaseProperties.getProperty("testDatabase"));
        testingDataSource.setUsername(databaseProperties.getProperty("testLogin"));
        testingDataSource.setPassword(databaseProperties.getProperty("testPassword"));
        return testingDataSource;
    }
    /**
     * Returns new DataSource representing a connection to the production database.
     *
     * @return new DataSource representing a connection to the production database
     */
    private DataSource getProductionDataSource() {
        BasicDataSource productionDataSource = new BasicDataSource();
        productionDataSource.setDriverClassName(databaseProperties.getProperty("driverName"));
        productionDataSource.setUrl(databaseProperties.getProperty("productionDatabase"));
        productionDataSource.setUsername(databaseProperties.getProperty("productionLogin"));
        productionDataSource.setPassword(databaseProperties.getProperty("productionPassword"));
        return productionDataSource;
    }
}
