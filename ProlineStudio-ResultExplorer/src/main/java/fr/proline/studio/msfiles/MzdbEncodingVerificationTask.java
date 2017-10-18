/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.openide.util.Exceptions;

/**
 *
 * @author AK249877
 */
public class MzdbEncodingVerificationTask extends AbstractDatabaseTask {

    private boolean m_result;
    private final File m_file;
    private Connection m_connection;

    public MzdbEncodingVerificationTask(AbstractDatabaseCallback callback, File file) {
        super(callback, new TaskInfo("Verify encoding of " + file.getAbsolutePath(), false, "Mzdb Verification", TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_file = file;
    }

    @Override
    public boolean fetchData() {
        
        m_result = true;

        if (connect()) {
            if (needFix()) {
                fix();
            }
            closeConnection();
        }

        if(!m_result){
            m_taskError = new TaskError("Encoding verification & repair failed.", "File is corrupted.");
        }
        
        return m_result;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }

    private boolean connect() {
        String url = "jdbc:sqlite:" + m_file.getAbsolutePath();
        m_connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            m_result = false;
            return false;
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    private boolean needFix() {
        String hcdSpectraCountQuery = "SELECT count(*) FROM spectrum where activation_type = 'HCD';";
        String dataEncodingCountQuery = "SELECT count(*) FROM data_encoding;";

        int hcdSpectraCount = -1;
        int dataEncodingCount = -1;

        //get spectra count
        try {

            Statement stmt = m_connection.createStatement();
            ResultSet rs = stmt.executeQuery(hcdSpectraCountQuery);

            // loop through the result set
            if (rs.next()) {
                hcdSpectraCount = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

        //get encoding count
        try {

            Statement stmt = m_connection.createStatement();
            ResultSet rs = stmt.executeQuery(dataEncodingCountQuery);

            // loop through the result set
            if (rs.next()) {
                dataEncodingCount = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (hcdSpectraCount != -1 && dataEncodingCount != -1) {
            if (hcdSpectraCount > 0 && dataEncodingCount == 3) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void fix() {

        //insert encoding fix
        String insertEncodingSQL = "INSERT INTO data_encoding(mode, compression, byte_order, param_tree) VALUES(?,?,?,?)";
        try {
            PreparedStatement pstmt = m_connection.prepareStatement(insertEncodingSQL);
            pstmt.setString(1, "centroided");
            pstmt.setString(2, "none");
            pstmt.setString(3, "little_endian");
            pstmt.setString(4, "<params>\n"
                    + "  <cvParams>\n"
                    + "    <cvParam cvRef=\"MS\" accession=\"1000521\" name=\"64-bit float\" value=\"64\" />\n"
                    + "    <cvParam cvRef=\"MS\" accession=\"1000521\" name=\"32-bit float\" value=\"32\" />\n"
                    + "  </cvParams>\n"
                    + "</params>\n"
                    + "");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            m_result = false;
            System.out.println(e.getMessage());
        }

        //update spectrums fix
        String sql = "UPDATE spectrum SET data_encoding_id = ? "
                + "WHERE activation_type = ? AND data_encoding_id = ?;";

        try {
            PreparedStatement pstmt = m_connection.prepareStatement(sql);

            pstmt.setInt(1, 4);
            pstmt.setString(2, "HCD");
            pstmt.setInt(3, 3);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            m_result = false;
            System.out.println(e.getMessage());
        }

    }

    private void closeConnection() {
        try {
            m_connection.close();
        } catch (SQLException ex) {
            m_result = false;
            Exceptions.printStackTrace(ex);
        }
    }

}
