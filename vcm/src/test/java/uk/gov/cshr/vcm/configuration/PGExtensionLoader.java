package uk.gov.cshr.vcm.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PGExtensionLoader {
    private static final Logger log = LoggerFactory.getLogger(PGExtensionLoader.class);

    /**
     * This method is responsible for running any initialisation on the Postgres instance such as extensions required to perform search queries.
     *
     * @param connection connection used to load the extensions required
     */
    public static void loadExtensions(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE EXTENSION cube;");
            statement.execute("CREATE EXTENSION earthdistance;");
        } catch (SQLException e) {
            log.error("An unexpected error occurred trying to initialise Postgres for the test run.", e);
        }
    }
}
