package server_Tracuusanpham;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Queue;

public class Repository {
	private String DB_URL = "jdbc:mysql://localhost:3306/testdb";
	private String USER_NAME = "root";
	private String PASSWORD = "";
	private Connection conn;
	public Repository(String DB_URL, String USER_NAME, String PASSWORD) {
		this.DB_URL = DB_URL;
		this.USER_NAME = USER_NAME;
		this.PASSWORD = PASSWORD;
	}
	public Boolean getConnection() {
		Boolean flag = false;
		try {
			System.out.println(this.DB_URL);
			System.out.println(this.USER_NAME);
			System.out.println(this.PASSWORD);
			this.conn = (Connection) DriverManager.getConnection(this.DB_URL, this.USER_NAME, this.PASSWORD);
			flag = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return flag;
	}
	public void closeConnection() {
			try {
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException ex){
				System.out.println(ex.getMessage());	
			}
	}
	public Queue<String> checkUpdate() {
		Queue <String> result = new LinkedList<>();
		long millis=System.currentTimeMillis();
		java.sql.Date date=new java.sql.Date(millis);
		String sql = "SELECT id FROM prices where time = CURDATE()";
		Statement st = null;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()){
				sql = "SELECT DISTINCT id FROM prices WHERE id NOT IN" +
						"(SELECT id FROM prices WHERE time = CURDATE())";
				rs = st.executeQuery(sql);
				while(rs.next()){
					result.add(rs.getString("id"));
				}
				return result;
			}else{
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}
	public Boolean updatePrice(Product p) {
		long millis=System.currentTimeMillis();  
		java.sql.Date date=new java.sql.Date(millis); 
		String sql = "INSERT INTO prices(id,time,price) "
	            + "VALUES(?,?,?)";
		
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,p.getId());
			pstmt.setDate(2,date);
			pstmt.setInt(3,p.getPrice());
			int rowAffected = pstmt.executeUpdate();
			pstmt.close();
			if (rowAffected == 1) return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
		return false;
	}
	public Queue<Product> getProductsID() throws SQLException {
		Queue<Product> products = new LinkedList<>(); 
		String sql = "SELECT id FROM products";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		 while (rs.next())
	      {
	        products.add(new Product(rs.getString("id")));
	      }
	      st.close();
		return products;
	}
	public int saveProduct(Product p) throws SQLException {
		long millis=System.currentTimeMillis();  
		java.sql.Date date=new java.sql.Date(millis);
		String sql = "SELECT * FROM products Where id ='"+p.getId()+"'";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		if (rs.next()) {
			st.close();
			return updatePrice(p)?2:0;
		}else {
			sql = "INSERT INTO products(id,create_time,name,img_url) "
		            + "VALUES(?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, p.getId());
			pstmt.setDate(2, date);
			pstmt.setString(3, p.getName());
			pstmt.setString(4, p.getImg_url());
			int rowAffected = pstmt.executeUpdate();
			
			if(rowAffected == 1) {
				st.close();
				return updatePrice(p)?1:0;
			}else {
				st.close();
				return 0;
			}
				
		}
		
		
		
	}
}
