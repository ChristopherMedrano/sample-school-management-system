package CoreJava.DAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import CoreJava.CustomExceptions.StudentRegistrationException;
import CoreJava.Models.Attending;
import CoreJava.Models.Course;
import CoreJava.Models.Student;
import CoreJava.SystemInterfaces.AttendingDAOI;

/**
 * 
 * This Data Access Object maps queries for the Attending table to the database.
 * The Attending table represents the students registered to a course. 
 * 
 * Implements the AttendingDAOI interface
 * 
 * Exceptions thrown: StudentRegistrationException
 * 
 * @author Chris Medrano
 *
 */
public class AttendingDAO implements AttendingDAOI {
	
	/*
	 * These objects are used to connect and query the database 
	 */
	protected Connection conn = null;
	protected Statement st = null;
	protected PreparedStatement ps = null;
	protected ResultSet rs = null;

	/**
	 * 
	 * This method looks for open connections to the database. 
	 * Any open connections are closed.
	 * 
	 * @param none
	 *  
	 */
	public void dispose() {
		try {
			if (!rs.equals(null)) {
				if (!rs.isClosed())
					rs.close();
			}
			if (!ps.equals(null)) {
				if (!ps.isClosed())
					ps.close();
			}
			if (!conn.equals(null)) {
				if (!conn.isClosed())
					conn.close();
			}
		} catch (SQLException e) {
			System.out.println(e);
		} catch (NullPointerException e) {
			//ignore
		}
	}

	/**
	 * 
	 * Overrides AttendingDAOI interface method
	 * This method registers student to course. 
	 * If the studentís GPA id greater or equal to the minimum GPA of the course then the student is allow to register to the course. 
	 * If not, then throw the StudentRegistrationException.
	 * 
	 * @param student		Student object
	 * @param course		Course object
	 * 
	 * @return attendingID	Primary key auto-generated by the database
	 * 
	 */
	@Override
	public int registerStudentToCourse(Student student, Course course) throws StudentRegistrationException {
		double gpa = student.getGpa();
		double courseMinGpa = course.getMinGpa();
		String query = "INSERT INTO attending (course_id, student_id) VALUES(?,?)";
		String idQuery = "SELECT \"ISEQ$$_87656\".CURRVAL FROM attending";
		int attendingID = 0, result = 0;

		if (gpa >= courseMinGpa) {

			try {
				conn = oc.getConnection();
				ps = conn.prepareStatement(query);
				ps.setInt(1, course.getID());
				ps.setInt(2, student.getID());
				result = ps.executeUpdate();
				
				if(result != 0) {
					st = conn.createStatement();
					rs = st.executeQuery(idQuery);
					if (rs.next()) {
						attendingID = rs.getInt(1);
					}
				}
				
			} catch (SQLException e) {
				//ignore
			} catch (IOException e) {
				//ignore
			} catch (ClassNotFoundException e) {
				//ignore
			}

		} else {
			throw new StudentRegistrationException("\nDid not meet the minimum GPA requirement\nRegistration Denied!");
		}

		return attendingID;
	}
	
	/**
	 * 
	 * Overrides AttendingDAOI interface method
	 * This method queries the database for all courses a student is registered to
	 * 
	 * @param 	studentID			The id of the student being queried
	 * @return	attendingCourses	A list of attending objects 
	 * 
	 */
	@Override
	public List<Attending> getStudentCourse(int studentID) {
		List<Attending> attendingCourses = new ArrayList<Attending>();
		Attending attending = null;
		String query = "SELECT course.COURSE_NAME, student.FULL_NAME, student.email FROM attending LEFT JOIN course ON attending.COURSE_ID = course.COURSE_ID LEFT JOIN student on student.STUDENT_ID = attending.STUDENT_ID WHERE attending.STUDENT_ID = ?";

		try {
			conn = oc.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, studentID);
			rs = ps.executeQuery();

			while (rs.next()) {
				attending = new Attending(rs.getString(1), rs.getString(2), rs.getString(3));
				attendingCourses.add(attending);
			}

		} catch (SQLException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}

		return attendingCourses;
	}

}
