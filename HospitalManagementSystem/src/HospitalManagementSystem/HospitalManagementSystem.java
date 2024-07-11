package HospitalManagementSystem;
import com.mysql.cj.jdbc.ConnectionGroup;
import com.mysql.cj.jdbc.PreparedStatementWrapper;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagementSystem {

    private static final String url="jdbc:mysql://localhost:3306/hospital";
    private static final String username="root";
    private static final String password="password";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        Scanner scanner=new Scanner(System.in);

        try {
            Connection connection=DriverManager.getConnection(url,username,password);
            Patient patient =new Patient(connection,scanner);
            Doctor doctor= new Doctor(connection);
            while (true){
                System.out.println("HOSPITAL MANAGEMENT SYSTEM ");
                System.out.println("1. ADD Patient");
                System.out.println("2. View Patient");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. View Appointment");
                System.out.println("6. Delete Appointment");
                System.out.println("7. Exit");
                System.out.println("Enter your choice: ");
                int choice =scanner.nextInt();

                switch (choice){
                    case 1:
                        //Add Patient
                        patient.addPatient();
                        System.out.println();
                        break;
                    case 2:
                        //View Patient
                        patient.viewPatients();
                        System.out.println();
                        break;
                    case 3:
                        //View Doctors
                        doctor.viewDoctors();
                        System.out.println();
                        break;
                    case 4:
                        //Book Appointment
                    	bookAppointment(patient,doctor,connection,scanner);

                        break;
                    case 5:
                    	viewAppointments(connection);
                        break;
                    case 6:
                        // Delete Appointment
                        deleteAppointment(connection, scanner);
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Enter a Valid Choice!!");
                        break;
                }

            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor,Connection connection,Scanner scanner){
        System.out.print("Enter Patient ID: ");
        int patientId=scanner.nextInt();
        System.out.print("Enter Doctor ID: ");
        int doctorId=scanner.nextInt();
        System.out.println("Enter Appointment date (YYYY-MM-DD): ");
        String appointmentDate=scanner.next();
        if (patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)){
            if (checkDoctorAvailability(doctorId,appointmentDate,connection)){
                String appointmentQuery="INSERT INTO appointments(patient_id,doctor_id,appointment_id) VALUES(?,?,?);";
                try {
                    PreparedStatement preparedStatement= connection.prepareStatement(appointmentQuery);
                    preparedStatement.setInt(1,patientId);
                    preparedStatement.setInt(2,doctorId);
                    preparedStatement.setString(3,appointmentDate);
                    int rowsAffected= preparedStatement.executeUpdate();
                    if (rowsAffected>0){
                        System.out.println("Appointment Successfully Booked....");
                    } else {
                        System.out.println("Failed to Book Appointment!!!");
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }
            } else {
                System.out.println("Doctor Not Available on this Date!!!!");
            }


        } else {
            System.out.println("Either Doctor or Patient doesn't exist !!!!");
        }

    }

    public static boolean checkDoctorAvailability(int doctorId,String appointmentDate,Connection connection){
        String query ="SELECT COUNT(*) FROM appointments WHERE doctor_id=? AND appointment_date=?";
        try {
            PreparedStatement preparedStatement=connection.prepareStatement(query);
            preparedStatement.setInt(1,doctorId);
            preparedStatement.setString(2,appointmentDate);
            ResultSet resultSet=preparedStatement.executeQuery();
            if (resultSet.next()){
                int count= resultSet.getInt(1);
                if (count==0){
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    
    //To View Appointment
    public static void viewAppointments(Connection connection) {
        String query = "SELECT * FROM appointments";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Appointments:");
            System.out.println("ID\tPatient ID\tDoctor ID\tAppointment Date");
            while (resultSet.next()) {
                int id = resultSet.getInt("id"); // Adjust column name if necessary
                int patientId = resultSet.getInt("patient_id");
                int doctorId = resultSet.getInt("doctor_id");
                String appointmentDate = resultSet.getString("appointment_date");
                System.out.println(id + "\t" + patientId + "\t\t" + doctorId + "\t\t" + appointmentDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Deleting Appointment
    public static void deleteAppointment(Connection connection, Scanner scanner) {
        System.out.print("Enter Doctor ID to delete appointments: ");
        int doctorId = scanner.nextInt();
        String query = "DELETE FROM appointments WHERE doctor_id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Appointments for Doctor ID " + doctorId + " deleted successfully.");
            } else {
                System.out.println("No appointments found for Doctor ID " + doctorId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
