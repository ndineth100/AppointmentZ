/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intelligentz.appointmentz.controllers;

import com.intelligentz.appointmentz.database.connectToDB;
import com.mysql.jdbc.Connection;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ndine
 */
public class addSession extends HttpServlet{  
    connectToDB con;
    private String filePath;
    private int maxFileSize = 100 * 1024;
    private int maxMemSize = 4 * 1024;
    private File file ;

    public void init( ){
        // Get the file location where it would be stored.
        filePath =
                getServletContext().getInitParameter("file-upload");
    }

    @Override
    public void doPost(HttpServletRequest req,HttpServletResponse res)  throws ServletException,IOException  
    {  
        try {
            String room_id = req.getParameter("room_id");
            String doctor_id = req.getParameter("doctor_id");
            String start_time = req.getParameter("start_time");
            String date_picked = req.getParameter("date_picked");

            DiskFileItemFactory factory = new DiskFileItemFactory();
            // maximum size that will be stored in memory
            factory.setSizeThreshold(maxMemSize);
            // Location to save data that is larger than maxMemSize.
            factory.setRepository(new File(filePath+"temp"));

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // maximum file size to be uploaded.
            upload.setSizeMax( maxFileSize );

            // Parse the request to get file items.
            List fileItems = upload.parseRequest(req);

            // Process the uploaded file items
            Iterator i = fileItems.iterator();

            while ( i.hasNext () )
            {
                FileItem fi = (FileItem)i.next();
                if ( !fi.isFormField () )
                {
                    // Get the uploaded file parameters
                    String fieldName = fi.getFieldName();
                    String fileName = fi.getName();
                    String contentType = fi.getContentType();
                    boolean isInMemory = fi.isInMemory();
                    long sizeInBytes = fi.getSize();
                    // Write the file
                    if( fileName.lastIndexOf("\\") >= 0 ){
                        file = new File( filePath +
                                fileName.substring( fileName.lastIndexOf("\\"))) ;
                    }else{
                        file = new File( filePath +
                                fileName.substring(fileName.lastIndexOf("\\")+1)) ;
                    }
                    fi.write( file ) ;
                }
            }

            con = new connectToDB();
            if(con.connect()){
                Connection  connection = con.getConnection();
                Class.forName("com.mysql.jdbc.Driver");
                Statement stmt = connection.createStatement( ); 
                String SQL,SQL1;
                SQL1 = "insert into appointmentz.session ( doctor_id, room_id, date, start_time) VALUES (?,?,?,?)";
                PreparedStatement preparedStmt = connection.prepareStatement(SQL1);
                    preparedStmt.setString (1, doctor_id);
                    preparedStmt.setString (2, room_id);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
                    try {
                        java.util.Date d = formatter.parse(date_picked+"-"+start_time);
                        Date d_sql = new Date(d.getTime());
                        java.util.Date N  = new java.util.Date();
                        if(N.compareTo(d)>0){
                            res.sendRedirect("./error.jsp?error=Invalid Date!");
                        }
                        //String [] T = start_time.split(":");
                        //Time t = Time.valueOf(start_time);
                        //Time t = new Time(Integer.parseInt(T[0]),Integer.parseInt(T[1]),0);
                        
                        //java.sql.Time t_sql = new java.sql.Date(d.getTime());
                        preparedStmt.setString(4, start_time+":00");
                        preparedStmt.setDate(3, d_sql);
                    } catch (ParseException e) {
                            displayMessage(res,"Invalid Date!"+e.getLocalizedMessage());
                    }
                    

                // execute the preparedstatement
                preparedStmt.execute();
                
                SQL = "select * from appointmentz.session"; 
                ResultSet rs = stmt.executeQuery(SQL);
                
                if(rs.wasNull()){
                    displayMessage(res,"response in null");
                }
                boolean check = false;
                while ( rs.next( ) ) {
                    String db_doctor_id = rs.getString("doctor_id");
                    String db_date_picked = rs.getString("date");
                    String db_start_time = rs.getString("start_time");
                    String db_room_id = rs.getString("room_id");
                        
                    if((doctor_id == null ? db_doctor_id == null : doctor_id.equals(db_doctor_id)) && (start_time == null ? db_start_time == null : (start_time+":00").equals(db_start_time)) && (room_id == null ? db_room_id == null : room_id.equals(db_room_id)) && (date_picked == null ? db_date_picked == null : date_picked.equals(db_date_picked))){
                        check=true;
                        //displayMessage(res,"Authentication Success!");
                        
                            try {
                                connection.close();
                            } catch (SQLException e) { 
                                displayMessage(res,"SQLException");
                            }
                        
                        res.sendRedirect("./home");
                        
                    }
                }
                if(!check){
                    
                        try {
                            connection.close();
                        } catch (SQLException e) { 
                            displayMessage(res,"SQLException");
                        }
                    displayMessage(res,"SQL query Failed!");
                }
            }
            else{
                con.showErrormessage(res);
            }
            
            
            /*res.setContentType("text/html");//setting the content type
            PrintWriter pw=res.getWriter();//get the stream to write the data
            
            //writing html in the stream
            pw.println("<html><body>");
            pw.println("Welcome to servlet: "+username);
            pw.println("</body></html>");
            
            pw.close();//closing the stream
            */
        } catch (Exception ex) {
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void displayMessage (HttpServletResponse res,String s) throws IOException{
        res.setContentType("text/html");//setting the content type
        PrintWriter pw=res.getWriter();//get the stream to write the data
        //writing html in the stream
        pw.println("<html><body>");
        pw.println("Info: "+s);
        pw.println("</body></html>");

        pw.close();//closing the stream
    }
}  
