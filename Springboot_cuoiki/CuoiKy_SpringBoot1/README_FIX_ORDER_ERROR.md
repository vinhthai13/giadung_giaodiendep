# Hướng dẫn sửa lỗi "User not found" khi đặt hàng

## Vấn đề

Khi người dùng chưa đăng nhập thực hiện đặt hàng, hệ thống báo lỗi "User not found" vì đang yêu cầu người dùng phải đăng nhập để đặt hàng.

## Giải pháp

Chúng ta đã thực hiện các sửa đổi sau để cho phép đặt hàng mà không cần đăng nhập:

1. Sửa đổi `Order.java` để cho phép `user_id` là null
2. Sửa đổi `OrderServiceImpl.java` để xử lý trường hợp người dùng chưa đăng nhập
3. Thêm phương thức `findAll()` vào `UserService` và `UserServiceImpl`
4. Tạo script SQL để sửa đổi cấu trúc bảng `orders` trong cơ sở dữ liệu

## Các bước thực hiện

### 1. Chạy script SQL để sửa đổi cấu trúc bảng orders

Mở phpMyAdmin hoặc MySQL Workbench và chạy script SQL sau:

```sql
-- Xóa ràng buộc khóa ngoại hiện tại
ALTER TABLE orders DROP FOREIGN KEY orders_ibfk_1;

-- Thêm lại ràng buộc khóa ngoại mới cho phép user_id là null
ALTER TABLE orders ADD CONSTRAINT orders_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Sửa đổi cột user_id để cho phép null
ALTER TABLE orders MODIFY COLUMN user_id bigint(20) NULL;
```

### 2. Khởi động lại ứng dụng

Sau khi thực hiện các sửa đổi và chạy script SQL, hãy khởi động lại ứng dụng để áp dụng các thay đổi.

## Kiểm tra

Sau khi thực hiện các bước trên, bạn có thể thử đặt hàng mà không cần đăng nhập. Hệ thống sẽ tự động tìm một người dùng mặc định (admin) hoặc bất kỳ người dùng nào trong hệ thống để gán cho đơn hàng, hoặc để trống nếu không tìm thấy người dùng nào.

## Lưu ý

- Nếu bạn muốn theo dõi đơn hàng của người dùng, vẫn nên khuyến khích người dùng đăng nhập trước khi đặt hàng.
- Đây chỉ là giải pháp tạm thời để cho phép đặt hàng mà không cần đăng nhập. Trong tương lai, bạn có thể cần phát triển một hệ thống đăng ký/đăng nhập tốt hơn. 