package fram;

import fram.filesystem.FileCopier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the caching of files
 *
 * @author Jason Leake
 */
public class Cache {

    private Connection con;
    private File cacheDirectory;
    private boolean newCache;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyy-hhmmss-SSS");
    private int counter = 0;
    private static final String CACHE_DIRECTORY = "framcache";

    /**
     * Constructor
     */
    public Cache() {
        System.out.println("setup cache");
        newCache = false;

        cacheDirectory = new File(CACHE_DIRECTORY);
        if (cacheDirectory.exists()) {
            if (cacheDirectory.isFile()) {
                cacheDirectory.delete();
                cacheDirectory.mkdirs();
                newCache = true;
            }
        } else {
            cacheDirectory.mkdirs();
            newCache = true;
        }

        if (newCache) {
            System.out.println("Cache directory created");
        }
        try {
            con = DriverManager.getConnection("jdbc:sqlite:"
                    + cacheDirectory.getAbsolutePath()
                    + File.separator + "framcache.db");
            if (con != null) {
                System.out.println("Connection established");

                createTable();
            }
        } catch (SQLException ex) {
            System.out.println("No sqlite driver found");
        }
    }

    /**
     * Get the path of the cached version of the file with the specified input
     * file name and hash
     *
     * @param file input file name
     * @param hash of cached and output file
     * @return path to cached file, or null if not present in cache
     */
    public Path getCachedFile(Path file, String hash) {
        if (hash != null) {
            PreparedStatement preparedStatement = null;
            try {
                String sql = "SELECT cachedFile FROM cachedfiles WHERE filename=? AND sha256=?";
                preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, file.toFile().getAbsolutePath());
                preparedStatement.setString(2, hash);
                // execute select SQL statement
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    // Found entry
                    String filename = rs.getString("cachedFile");
                    return Paths.get(filename);
                }

            } catch (SQLException ex) {
                System.out.println(ex.getLocalizedMessage());
            } finally {
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException ex) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * Store the specified file in the cash
     *
     * @param file input file name
     * @param hash hash of output file
     * @param fileToCache path of output file to cache
     */
    public void cacheFile(Path file, String hash, File fileToCache) {
        if (hash != null) {
            try {
                // Delete any existing cached file
                String sql = "SELECT cachedFile FROM cachedfiles WHERE filename=?";
                PreparedStatement prepStatement = con.prepareStatement(sql);
                prepStatement.setString(1, file.toFile().getAbsolutePath());
                // execute select SQL statement
                ResultSet rs = prepStatement.executeQuery();
                if (rs.next()) {
                    String filename = rs.getString("cachedFile");
                    System.out.println("Delete old cached file " + filename);
                    new File(filename).delete();
                }
                prepStatement.close();

                // Update the entry with the new file
                sql = "INSERT OR REPLACE INTO cachedfiles(filename, sha256, cachedfile) VALUES(?,?,?)";
                prepStatement = con.prepareStatement(sql);
                File outputFile = makeOutputFile();
                prepStatement.setString(1, file.toFile().getAbsolutePath());
                prepStatement.setString(2, hash);
                prepStatement.setString(3, outputFile.getAbsolutePath());
                prepStatement.executeUpdate();
                prepStatement.close();
                FileCopier.copyFile(fileToCache.toPath(), outputFile.toPath());
            } catch (SQLException ex) {
                Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Create the database table if it does not exist
     */
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS cachedfiles ("
                + "	filename TEXT NOT NULL PRIMARY KEY,"
                + "	sha256 TEXT NOT NULL,"
                + "	cachedfile TEXT NOT NULL"
                + ");";
        try {
            Statement stmt = con.createStatement();
            stmt.closeOnCompletion();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Generate a unique filename
     *
     * @return unique filename
     */
    private File makeOutputFile() {
        if (counter > 99999) {
            counter = 0;
        }
        String filename = String.format("%s-%05d.jpg",
                simpleDateFormat.format(new Date()), counter);
        return new File(String.format("%s%s%02d", CACHE_DIRECTORY,
                File.separator, (counter++ % 100)), filename);
    }

    /**
     * Remove obsolete files. Minimise the number of database queries to keep
     * the speed up.
     */
    public void clean() {
        ElapsedTime elapsedTime = new ElapsedTime();
        System.out.println();
        System.out.println("Clean cache");
        final File directory = new File(CACHE_DIRECTORY);
        SortedSet<String> filesInDatabase = new TreeSet<>();
        ResultSet rs = null;
        try {
            String sql = "SELECT cachedFile FROM cachedfiles ORDER BY cachedFile";
            Statement queryStatement = con.createStatement();
            queryStatement.closeOnCompletion();
            rs = queryStatement.executeQuery(sql);
            while (rs.next()) {
                filesInDatabase.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Check for files in the database but not in the cache directory
        SortedSet<String> inputFiles = getFiles(directory);
        PreparedStatement preparedStatement = null;
        try {
            String sql = "DELETE FROM cachedfiles WHERE cachedFile = ?";
            preparedStatement = con.prepareStatement(sql);

            for (String fileInDatabase : filesInDatabase) {
                if (inputFiles.contains(fileInDatabase)) {
                    // File exists in database and in the cache directory
                } else {
                    // File exists in database but not in the cache directory
                    System.out.println("Delete database entry " + fileInDatabase);
                    preparedStatement.setString(1, fileInDatabase);
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Check for files in the cache directory but not in the database
        for (String file : inputFiles) {
            if (filesInDatabase.contains(file)) {
                // File exists in cache directory and in the database
            } else {
                // File exists in database but not in the cache directory
                System.out.println("Delete " + file);
            }
        }
        elapsedTime.reportElapsedTime("Cache clean");
    }

    /**
     * Get the files in the directory tree as a sorted set
     *
     * @param directory directory tree
     * @return list of files
     */
    private SortedSet<String> getFiles(File directory) {
        SortedSet<String> returnSet = new TreeSet<>();
        for (File file : getFilesList(directory)) {
            returnSet.add(file.getAbsolutePath());
        }
        return returnSet;
    }

    /**
     * Get the files in the directory tree as a linked list
     *
     * @param directory directory tree
     * @return list of files
     */
    private List<File> getFilesList(File directory) {
        List<File> files = new LinkedList<>();
        for (File fileEntry : directory.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(getFilesList(fileEntry));
            } else if (!fileEntry.getName().equals("framcache.db")) {
                // Exclude the database file
                files.add(fileEntry);
            }
        }
        return files;
    }

    /**
     * Make sure the database connection is closed
     */
    public void close() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException ex) {
                Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    //  THESE ARE JUST USED BY THE UNIT TESTS
    /**
     *
     * Create a file in the cache directory - just for test purpose
     */
    public void createTestFile() {
        File outputFile = makeOutputFile();

        System.out.println("Create cached file " + outputFile);
        try {
            outputFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create a database entry - just for test purposes
     */
    public void createTestEntry() {
        File outputFile = makeOutputFile();
        System.out.println("Create dummy cache database entry " + outputFile);
        String sql = "INSERT OR REPLACE INTO cachedfiles(filename, sha256, cachedfile) VALUES(?,?,?)";
        PreparedStatement pstmt = null;
        try {
            // Update the entry with the new file
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, "test");
            pstmt.setString(2, "1234");
            pstmt.setString(3, outputFile.getAbsolutePath());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Delete cache directory
     */
    public static void deleteCache() {
        deleteDir(new File(CACHE_DIRECTORY));
    }

    /**
     * Delete directory tree
     *
     * @param directory directory tree root
     */
    private static void deleteDir(File directory) {
        File[] directoryContents = directory.listFiles();
        if (directoryContents != null) {
            for (File fileInDirectory : directoryContents) {
                // Don't follow symbolic links
                if (!Files.isSymbolicLink(fileInDirectory.toPath())) {
                    deleteDir(fileInDirectory);
                }
            }
        }
        directory.delete();
    }
}
