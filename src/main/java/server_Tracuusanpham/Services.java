package server_Tracuusanpham;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;

public class Services {
	private Repository repository;
	public Services(String DB_URL, String USER_NAME, String PASSWORD) {
		this.repository = new Repository(DB_URL, USER_NAME, PASSWORD);
	}

	public Boolean getConnection() {
		return repository.getConnection();
	}

	public void close(){
		this.repository.closeConnection();
	}

	public String saveProducts(String s ,int quantity ) {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		System.out.println(s+quantity);
		int count=0;
		int countS=0;
		int countU=0;
		int countDup=0;
		int countF=0;
		Boolean flag = true;
		int j=1;
		while (flag && (countS < quantity)) {
			String apiUrl="https://tiki.vn/api/personalish/v1/blocks/listings?limit=48&category="+s.trim()+"&page="+j;
			System.out.println(apiUrl);
			try {
				Connection.Response response= Jsoup.connect(apiUrl)
						.ignoreContentType(true)
						.method(Method.GET)
						.execute();
				JSONObject products= new JSONObject(response.body());
				JSONArray content= products.getJSONArray("data");
				if(content.length()<1)
					return "Id category không tồn tại";
				for(int i=0;i<content.length();i++) {
					int id=content.getJSONObject(i).getInt("id");
					if (map.containsKey(id)) {
						System.out.println("^^^^^^Trùng Id^^^^^^");
						countDup++;
					}else {
						map.put(id,"");

						Product p = new Product(String.valueOf(id));
						String urlapiProduct= "https://tiki.vn/api/v2/products/"+id;
						response= Jsoup.connect(urlapiProduct)
								.ignoreContentType(true)
								.method(Method.GET)
								.execute();
						JSONObject productInfo= new JSONObject(response.body());
						p.setName((String) productInfo.get("name"));
						p.setPrice( (Integer) productInfo.get("price"));
						p.setImg_url((String) productInfo.get("thumbnail_url"));
						System.out.println(++count+"#"+p.getId()+"#"+p.getName()+"#"+p.getPrice());
						int rs=repository.saveProduct(p);
						if(rs!=0) {
							if(rs == 1)
								countS++;
							else
								countU++;

						}else {
							System.out.println("!!!!!! Fail !!!!!!");
							countF++;
						}
						if ((countS)>=500) break;
					}
				}
				j++;
			}catch (Exception e) {
				return e.getMessage();
			}
		}
		String result = "Tổng chạy: "+count+"\n";
		result += "Tổng sản phẩm trùng: "+countDup+"\n"; 
		result += "Tổng sản phẩm update thành công: "+countU+"\n";
		result += "Tổng sản phẩm lưu trữ thành công: "+countS+"\n";
		result += "Tổng sản phẩm thất bại: "+countF+"\n"; 
		return result;
	}
	public Boolean updateProduct(String id) {
		Product p = new Product(id);
		String urlapiProduct= "https://tiki.vn/api/v2/products/"+p.getId();
		Connection.Response response;
		try {
			response = Jsoup.connect(urlapiProduct)
					.ignoreContentType(true)
					.method(Method.GET)
					.execute();
			JSONObject productInfo= new JSONObject(response.body());
			p.setPrice( (Integer) productInfo.get("price"));
			if(repository.updatePrice(p)) {
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	public String updateProducts()  {
		Queue<String> fails = repository.checkUpdate();
		if (fails != null){
			String s = "Hôm nay đã update rồi\n ID của các sản phẩm chưa được cập nhật:\n";
			for (String fail : fails){
				s += fail + "\n";
			}
			return s;
		}
		fails = new LinkedList<>();
		Queue<Product> products = null;
		try {
			products = repository.getProductsID();
		} catch (SQLException e) {
			return "Không thể lấy danh sách từ database";
		}
		int count=0;
		int countF=0;
		int countS=0;
		for(Product p : products) {
		    //do something with each element
			System.out.println(++count+"#"+p.getId());
			String urlapiProduct= "https://tiki.vn/api/v2/products/"+p.getId();
			Connection.Response response;
			try {
				response = Jsoup.connect(urlapiProduct)
						.ignoreContentType(true)
						.method(Method.GET)
						.execute();
				JSONObject productInfo= new JSONObject(response.body());
				p.setPrice( (Integer) productInfo.get("price"));
				if(repository.updatePrice(p)) {
					countS++;
				}else {
					System.out.println("!!! Có lỗi xảy ra ở đây");
					countF++;
					fails.add(p.getId());
				}
			} catch (Exception e) {
				System.out.println("Lấy gía sản phẩm thất bại !!");
				countF++;
				fails.add(p.getId());
			}
		}
		String result = "Tổng sản phẩm Update thành công: "+countS+"\n";
		result += "Tổng sản phẩm Update thất bại: "+countF+"\n";
		for (String fail: fails) {
			result += fail+ "\n";
		}
		return result;
	}
}
