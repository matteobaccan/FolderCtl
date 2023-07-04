/*
 * Matteo Baccan
 * http://www.baccan.it
 *
 * Distributed under the GPL v3 software license, see the accompanying
 * file LICENSE or http://www.gnu.org/licenses/gpl.html.
 *
 */
package it.baccan.folderctl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Matteo
 */
@Slf4j
public class H2Storage {

    private long seed = 0;
    private Connection h2;

    /**
     *
     * @param seed
     */
    public H2Storage(final long seed) {
        this.seed = seed;
        try {
            Class.forName("org.h2.Driver");
            h2 = DriverManager.getConnection("jdbc:h2:~/folderCtl", "sa", "");
            try (Statement t = h2.createStatement()) {
                // Cre la struttura dati che serve
                t.execute("CREATE TABLE IF NOT EXISTS folderCtl"
                    + "("
                    + "SEED BIGINT"
                    + ", FILE VARCHAR(1024) UNIQUE"
                    + ", SIZE BIGINT"
                    + ", LASTMODIFIED BIGINT"
                    + ")");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error connecting cache database [{}]", ex.getMessage());
        }
    }

    /**
     *
     * @param path
     * @param length
     * @param lastModified
     * @return
     */
    public boolean updateFile(final String path, final long length, final long lastModified) {
        log.info("UpdateFile [{}]-[{}][{}][{}]", seed, path, length, lastModified);
        boolean ret = false;
        try {
            try (PreparedStatement t2 = h2.prepareStatement("MERGE INTO folderCtl(seed, file, size, lastmodified) KEY (FILE) VALUES(?,?,?,?)")) {
                t2.setLong(1, seed);
                t2.setString(2, path);
                t2.setLong(3, length);
                t2.setLong(4, lastModified);
                if (t2.execute()) {
                    ret = t2.getUpdateCount() > 0;
                }
            }
        } catch (SQLException sQLException) {
            log.error("sQLException", sQLException);
        }
        return ret;
    }

    /**
     *
     * @param path
     * @param length
     * @param lastModified
     * @return
     */
    public boolean checkFile(final String path, final long length, final long lastModified) {
        log.trace("checkFile [{}]-[{}][{}][{}]", seed, path, length, lastModified);
        boolean ret = false;
        try {
            try (PreparedStatement t2 = h2.prepareStatement("select size, lastmodified from folderCtl where file=?")) {
                t2.setString(1, path);
                try (ResultSet r2 = t2.executeQuery()) {
                    String error = "Error with file [" + path + "]";
                    if (r2.next()) {
                        boolean lenOk = r2.getLong(1) == length;
                        if (!lenOk) {
                            error += " Size changed current [" + length + "] saved [" + r2.getLong(1) + "]";
                            ret = false;
                        }
                        boolean modOk = r2.getLong(2) == lastModified;
                        if (!modOk) {
                            error += " Last modification changed current [" + lastModified + "] saved [" + r2.getLong(2) + "]";
                            ret = false;
                        }

                        ret = lenOk && modOk;
                    } else {
                        error += " not found";
                    }
                    if (!ret) {
                        log.error(error);
                    }
                }
            }
        } catch (SQLException sQLException) {
            log.error("sQLException", sQLException);
        }
        return ret;
    }

    /**
     *
     * @param path
     * @return
     */
    public boolean cleanOldScan(final String path) {
        log.trace("cleanOldScan [{}]-[{}][{}][{}]", seed, path);
        boolean ret = false;
        try {
            try (PreparedStatement t2 = h2.prepareStatement("DELETE folderCtl WHERE seed!=? AND file like ?")) {
                t2.setLong(1, seed);
                t2.setString(2, path + "%");
                if (t2.execute()) {
                    ret = t2.getUpdateCount() > 0;
                }
            }
        } catch (SQLException sQLException) {
            log.error("sQLException", sQLException);
        }
        return ret;
    }

}
