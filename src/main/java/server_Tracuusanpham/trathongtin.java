package server_Tracuusanpham;


import java.util.Scanner;

public class trathongtin {
	private static Services services;
	private static Scanner sc;
	public static void action1 (){
		sc.nextLine();
		while (true) {
			try {
				System.out.println("Nhập vào id category");
				String s = sc.nextLine();
				System.out.println("Nhập vào số lượng sản phẩm muốn lấy");
				int i = sc.nextInt();
				System.out.println(services.saveProducts(s,i));
				break;
			}catch(Exception e){
				System.out.println("Vui lòng nhập đúng id category và số lượng sản phẩm muốn lấy ");
			}
		}
	}

	public static void action2 (){
		System.out.println(services.updateProducts());
	}

	public static void action3 (){
		String t = sc.nextLine();
		while(!t.equalsIgnoreCase("end")) {
			if (t.equalsIgnoreCase("")){
				System.out.println("Vui lòng nhập id sản phẩm cần cập nhật lại hoặc nhập 'end' để về menu.");
			}
			t = sc.nextLine();
			if (t.equalsIgnoreCase("end")) break;
			try {
				if (services.updateProduct(t)) {
					System.out.println("ID: "+t+" - Update thành công");
				}else{
					System.out.println("ID: "+t+" - Update thất bại");
				};
			} catch (Exception e) {
				System.out.println("ID: "+t+" - Update thất bại");
			}
		}
	}

	public static void action4 (){
		services.close();
		sc.close();
	}

	public static void work() {

		System.out.println("begin **********************************");
		Boolean flag = true;
		while(flag) {
			System.out.println("1. Lấy dữ liệu seek\n2. Update giá\n3. Fix product price\n4. Exit");
			int choice = sc.nextInt();
			switch (choice) {
				case 1:
					action1();
					break;
				case 2:
					action2();
					break;
				case 3:
					action3();
					break;
				case 4:
					action4();
					return;
				default:
					System.out.println("Vui lòng chọn 1 trong các mục");
			}
			
				
		}
	}
	public static void main(String [] args) {
		sc = new Scanner(System.in);
		services = new Services("jdbc:mysql://localhost:3306/price_statistics","root","");
		if (!services.getConnection()) {
			System.out.println("Kiểm tra lại thông số database và jar");
			System.exit(0);
		}
		work();
		return;
	}


}