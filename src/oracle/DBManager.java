package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	static private DBManager instance;
	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@localhost:1521:XE";
	private String user = "batman";
	private String password = "1234";
	
	//윈도우 창이 열리면 이미 접속을확보해 놓자
	Connection con;// 접속 후 그 정보를 담는 객체

	// new 막기 위함
	
	/*
	 * 1.드라이버로드
	 * 2.접속
	 * 3.쿼리실행
	 * 4.반납
	 */
	private DBManager() {
		try {
			Class.forName(driver);
			try {
				con=DriverManager.getConnection(url,user,password);
		
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static public DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	// 접속 객체 반환
	public  Connection getConnection() {
		return con;
	}

	// 접속해제
	public void disConnect(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
