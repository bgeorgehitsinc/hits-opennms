package org.opennms.server;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.client.DatabaseConnectionSettings;
import org.opennms.client.DatabaseDoesNotExistException;
import org.opennms.client.InstallService;
import org.opennms.client.LoggingEvent;
import org.opennms.client.LoggingEvent.LogLevel;
import org.opennms.install.Installer;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.dao.db.InstallerDb;
import org.opennms.netmgt.dao.db.SimpleDataSource;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
    private static final long serialVersionUID = 7921657972081359016L;

    private static final String OWNERSHIP_FILE_SESSION_ATTRIBUTE = "__install_ownership_file";
    // private static final String DATABASE_SETTINGS_SESSION_ATTRIBUTE = "__install_database_settings";
    private static final String OPENNMS_DATASOURCE_NAME = "opennms";
    private static final String OPENNMS_ADMIN_DATASOURCE_NAME = "opennms-admin";

    private static boolean m_updateIsInProgress = false;
    private static boolean m_lastUpdateSucceeded = false;

    /**
     * Fetch the OpenNMS home directory, as set in the <code>opennms.home</code>
     * system property.
     */
    protected String getOpennmsInstallPath() {
        return ConfigFileConstants.getHome();
    }

    public boolean checkOwnershipFileExists() {
        // return true;
        HttpServletRequest request = this.getThreadLocalRequest();
        if (request == null) {
            throw new IllegalStateException("No HTTP request object available.");
        }

        HttpSession session = request.getSession(true);
        if (session == null) {
            throw new IllegalStateException("No HTTP session object available.");
        }

        String attribute = (String)session.getAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE);
        if (attribute == null) {
            return false;
        } else {
            if (new File(this.getOpennmsInstallPath(), attribute).isFile()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getOwnershipFilename(){
        HttpServletRequest request = this.getThreadLocalRequest();
        if (request == null) {
            throw new IllegalStateException("No HTTP request object available.");
        }

        HttpSession session = request.getSession(true);
        if (session == null) {
            throw new IllegalStateException("No HTTP session object available.");
        }

        String attribute = (String)session.getAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE);
        if (attribute == null) {
            attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
            session.setAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE, attribute);
        }
        return attribute;
    }

    public void resetOwnershipFilename() {
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        String attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
        session.setAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE, attribute);
    }

    public boolean isAdminPasswordSet() throws IllegalStateException {
        /*
        Userinfo userinfo = null;
        try {
            userinfo = (Userinfo)Unmarshaller.unmarshal(Userinfo.class, 
                new FileReader(
                    new File(
                        new File(this.getOpennmsInstallPath(), "etc"), 
                        "users.xml"
                    )
                )
            );
        } catch (MarshalException e) {
            throw new IllegalStateException("<code>users.xml</code> file cannot be read properly.");
        } catch (ValidationException e) {
            throw new IllegalStateException("<code>users.xml</code> file is invalid and cannot be read.");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("<code>users.xml</code> file cannot be found in the OpenNMS home directory.");
        }

        for(User user : userinfo.getUsers().getUser()) {
            if ("admin".equals(user.getUserId())) {
                if (user.getPassword() == null || "".equals(user.getPassword().trim())) {
                    // If the password is null or blank, return false
                    return false;
                } else if ("21232F297A57A5A743894A0E4A801FC3".equals(user.getPassword())) {
                    // If the password is still set to the default value, return false
                    return false;
                } else {
                    return true;
                }
            }
        }
        // If there is no "admin" entry in users.xml, return false
        return true;
         */

        UserManager manager = UserFactory.getInstance();
        try {
            User user = manager.getUser("admin");
            if (user == null) {
                throw new IllegalStateException("There is no <code>admin</code> user in the <code>users.xml</code> file.");
            } else {
                if (user.getPassword() == null || "".equals(user.getPassword().trim())) {
                    // If the password is null or blank, return false
                    return false;
                } else if ("21232F297A57A5A743894A0E4A801FC3".equals(user.getPassword().trim())) {
                    // If the password is still set to the default value, return false
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not check password: " + e.getMessage());
        }
    }

    public void setAdminPassword(String password) {
        UserManager manager = UserFactory.getInstance();
        try {
            manager.setUnencryptedPassword("admin", password);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not store password: " + e.getMessage());
        }
    }

    public DatabaseConnectionSettings getDatabaseConnectionSettings() throws IllegalStateException {
        String dbName = null;
        String dbAdminUser = null;
        String dbAdminPassword = null;
        String driver = null;
        String dbAdminUrl = null;
        String dbNmsUser = null;
        String dbNmsPassword = null;
        String dbNmsUrl = null;

        File configFile = new File(new File(this.getOpennmsInstallPath(), "etc"), "opennms-datasources.xml");
        DataSourceConfiguration config = null;
        if (configFile.canRead()) {
            try {
                config = DataSourceConfiguration.unmarshal(new FileReader(configFile));
            } catch (MarshalException e) {
                throw new IllegalStateException("Cannot read from <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Invalid configuration data in <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read from <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("Location <code>" + configFile.getPath() + "</code> is not readable.");
        }

        for (JdbcDataSource ds : config.getJdbcDataSource()) {
            if (OPENNMS_DATASOURCE_NAME.equals(ds.getName())) {
                dbNmsUser = ds.getUserName();
                dbNmsPassword = ds.getPassword();
                dbNmsUrl = ds.getUrl();
            } else if (OPENNMS_ADMIN_DATASOURCE_NAME.equals(ds.getName())) {
                dbName = ds.getDatabaseName();
                dbAdminUser = ds.getUserName();
                dbAdminPassword = ds.getPassword();
                driver = ds.getClassName();
                dbAdminUrl = ds.getUrl();
            }
        }

        return new DatabaseConnectionSettings(driver, dbName, dbAdminUser, dbAdminPassword, dbAdminUrl, dbNmsUser, dbNmsPassword, dbNmsUrl);
    }

    public void connectToDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl) throws IllegalStateException {
        validateDbParameters(new DatabaseConnectionSettings(driver, dbName, dbAdminUser, dbAdminPassword, dbAdminUrl, dbNmsUser, dbNmsPassword, dbNmsUrl));
        InstallerDb db = new InstallerDb();
        db.setDatabaseName(dbName);
        // Only used when creating an OpenNMS user in the database
        db.setPostgresOpennmsUser(dbNmsUser);
        db.setPostgresOpennmsPassword(dbNmsPassword);
        try {
            db.setAdminDataSource(new SimpleDataSource(driver, dbAdminUrl, dbAdminUser, dbAdminPassword));
            db.setDataSource(new SimpleDataSource(driver, dbNmsUrl, dbNmsUser, dbNmsPassword));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        boolean databaseExists = false;
        try {
            databaseExists = db.databaseDBExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check database list: " + e.getMessage());
        }

        if (databaseExists) {
            // TODO: Change this to an appropriate connection test
            // Try to vacuum the database to test connectivity
            try {
                db.vacuumDatabase(false);
                // If the test completes, then store the database connectivity information
                this.setDatabaseConfig(driver, dbName, dbAdminUser, dbAdminPassword, dbAdminUrl, dbNmsUser, dbNmsPassword, dbNmsUrl);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Database connection failed: " + e.getMessage());
            }
        } else {
            throw new DatabaseDoesNotExistException();
        }
    }

    public void createDatabase(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword) throws DatabaseDoesNotExistException, IllegalStateException {
        validateDbParameters(new DatabaseConnectionSettings(driver, dbName, dbAdminUser, dbAdminPassword, dbAdminUrl, dbNmsUser, dbNmsPassword, null));
        InstallerDb db = new InstallerDb();
        db.setDatabaseName(dbName);
        // Only used when creating an OpenNMS user in the database
        db.setPostgresOpennmsUser(dbNmsUser);
        db.setPostgresOpennmsPassword(dbNmsPassword);
        try {
            db.setAdminDataSource(new SimpleDataSource(driver, dbAdminUrl, dbAdminUser, dbAdminPassword));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        // Sanity check to make sure that the database doesn't already exist
        boolean databaseExists = true;
        try {
            databaseExists = db.databaseDBExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check database list: " + e.getMessage());
        }

        if (databaseExists) {
            throw new IllegalStateException("Database already exists.");
        } else {
            try {
                db.databaseAddUser();
                db.databaseAddDB();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    protected void setDatabaseConfig(String driver, String dbName, String dbAdminUser, String dbAdminPassword, String dbAdminUrl, String dbNmsUser, String dbNmsPassword, String dbNmsUrl) throws IllegalStateException, IllegalArgumentException {
        validateDbParameters(new DatabaseConnectionSettings(driver, dbName, dbAdminUser, dbAdminPassword, dbAdminUrl, dbNmsUser, dbNmsPassword, dbNmsUrl));
        /*
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        session.setAttribute(DATABASE_SETTINGS_SESSION_ATTRIBUTE, new String[] {
            dbName,
            user,
            password,
            driver,
            url,
            binaryDirectory
        });
         */

        JdbcDataSource adminDs = new JdbcDataSource();
        adminDs.setClassName(driver);
        adminDs.setDatabaseName(dbName);
        adminDs.setName(OPENNMS_ADMIN_DATASOURCE_NAME);
        adminDs.setPassword(dbAdminPassword);
        adminDs.setUrl(dbNmsUrl);
        adminDs.setUserName(dbAdminUser);

        JdbcDataSource opennmsDs = new JdbcDataSource();
        opennmsDs.setClassName(driver);
        opennmsDs.setDatabaseName(dbName);
        opennmsDs.setName(OPENNMS_DATASOURCE_NAME);
        opennmsDs.setPassword(dbNmsPassword);
        opennmsDs.setUrl(dbNmsUrl);
        opennmsDs.setUserName(dbNmsUser);

        DataSourceConfiguration config = new DataSourceConfiguration();
        config.addJdbcDataSource(opennmsDs);
        config.addJdbcDataSource(adminDs);
        File configFile = new File(new File(this.getOpennmsInstallPath(), "etc"), "opennms-datasources.xml");
        if (configFile.canWrite()) {
            try {
                config.marshal(new FileWriter(configFile));
            } catch (MarshalException e) {
                throw new IllegalStateException("Cannot write to <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Invalid configuration data specified: " + e.getMessage());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot write to <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("Location <code>" + configFile.getPath() + "</code> is not writable.");
        }
    }

    public List<LoggingEvent> getDatabaseUpdateLogs(int offset){
        List<LoggingEvent> retval = new ArrayList<LoggingEvent>();
        for (org.apache.log4j.spi.LoggingEvent event : ((ListAppender)Logger.getRootLogger().getAppender("UNCATEGORIZED")).getEvents(offset, 200)) {
            LogLevel level = null;
            switch(event.getLevel().toInt()) {
            case (Level.TRACE_INT): level = LogLevel.TRACE; break;
            case (Level.DEBUG_INT): level = LogLevel.DEBUG; break;
            case (Level.INFO_INT): level = LogLevel.INFO; break;
            case (Level.WARN_INT): level = LogLevel.WARN; break;
            case (Level.ERROR_INT): level = LogLevel.ERROR; break;
            case (Level.FATAL_INT): level = LogLevel.FATAL; break;
            // If the level is set to any other value, skip this message
            default: continue;
            }
            retval.add(new LoggingEvent(event.getLoggerName(), event.getTimeStamp(), level, event.getMessage().toString()));
        }
        return retval;
    }

    public void clearDatabaseUpdateLogs(){
        ListAppender appender = ((ListAppender)Logger.getRootLogger().getAppender("UNCATEGORIZED"));
        appender.clear();
    }

    /**
     * Initiate the installer class. This will generate log messages that will be
     * relayed to the web UI by the log4j appender.
     */
    public void updateDatabase() {
        Thread thread = new Thread() {
            public void run() {
                // Don't need synchronized blocks when updating a boolean primitive
                m_updateIsInProgress = true;
                try {
                    Installer.main(new String[] { "-dis" });
                    m_lastUpdateSucceeded = true;
                } catch (Exception e) {
                    Logger.getLogger(this.getClass()).error("Installation failed: " + e.getMessage(), e);
                    m_lastUpdateSucceeded = false;
                } finally {
                    m_updateIsInProgress = false;
                }
            }
        };
        thread.start();
    }

    public boolean isUpdateInProgress() {
        // Don't need synchronized blocks when accessing a boolean primitive
        return m_updateIsInProgress;
    }

    public boolean didLastUpdateSucceed() {
        // Don't need synchronized blocks when accessing a boolean primitive
        return m_lastUpdateSucceeded;
    }

    public boolean checkIpLike() throws IllegalStateException {
        // We should have a proper opennms-datasources.xml stored at this point so try to load it
        // by using the normal {@link org.opennms.netmgt.config.DataSourceFactory} class.
        // TODO: Throw specific exceptions to provide better UI feedback
        try {
            DataSourceFactory.init();
        } catch (MarshalException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (ValidationException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded: " + e.getMessage());
        } catch (PropertyVetoException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        }

        InstallerDb db = new InstallerDb();
        db.setAdminDataSource(DataSourceFactory.getInstance(OPENNMS_ADMIN_DATASOURCE_NAME));
        db.setDataSource(DataSourceFactory.getInstance(OPENNMS_DATASOURCE_NAME));
        // TODO: Are there additional tests that we need to perform on the database?
        return db.isIpLikeUsable();
    }

    /**
     * Perform validation on database settings. The driver class, usernames, and URLs must be
     * non-null and non-blank, but the passwords can be blank.
     */
    protected static void validateDbParameters(DatabaseConnectionSettings settings) throws IllegalArgumentException {
        if (settings.getDbName() == null || "".equals(settings.getDbName().trim())) {
            throw new IllegalArgumentException("Database name cannot be blank.");
        } else if (settings.getDriver() == null || "".equals(settings.getDriver().trim())) {
            throw new IllegalArgumentException("Driver class cannot be blank.");
        } else if (settings.getAdminUser() == null || "".equals(settings.getAdminUser().trim())) {
            throw new IllegalArgumentException("Admin user cannot be blank.");
        } else if ("".equals(settings.getAdminUrl() == null ? null : settings.getAdminUrl().trim())) {
            throw new IllegalArgumentException("Admin JDBC URL cannot be blank.");
        } else if (settings.getNmsUser() == null || "".equals(settings.getNmsUser().trim())) {
            throw new IllegalArgumentException("OpenNMS user cannot be blank.");
        } else if ("".equals(settings.getNmsUrl() == null ? null : settings.getNmsUrl().trim())) {
            throw new IllegalArgumentException("OpenNMS JDBC URL cannot be blank.");
        }
    }
}
