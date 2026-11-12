# Gia Sư Lý 9

Nền tảng học trực tuyến môn Vật lý 9 (Chương trình GDPT 2024 - KHTN 9).
Xây dựng bằng **Jakarta EE 10** (Servlets + JSP/JSTL), chạy trên **Apache Tomcat 10.1**,
kết nối **SQL Server**.

## Tính năng

| Vai trò | Chức năng |
|---|---|
| **Học sinh** | Xem bài giảng (text + video YouTube), làm bài trắc nghiệm & tự luận, tự chấm điểm, xem tiến độ học, chat với AI gia sư (có thể gửi ảnh đề) |
| **Giáo viên** | Thêm/sửa bài giảng, soạn câu hỏi, sinh câu hỏi bằng AI, xóa câu hỏi AI không đạt, thống kê câu hỏi học sinh làm sai nhiều |
| **Quản trị** | Quản lý tài khoản, khoá/mở khoá user, quản lý chủ đề, xem thống kê lỗi học sinh |

## Yêu cầu môi trường

- **JDK 11+**
- **Apache Tomcat 10.1**
- **SQL Server 2019+** (hoặc Azure SQL)
- **Ant** (build) hoặc NetBeans IDE

## Cài đặt

### 1. Tạo database

File SQL nằm **ngoài thư mục gốc repo** (theo yêu cầu nộp bài), tên: `GiaSuLy9.sql`.

Chạy bằng SQL Server Management Studio, hoặc bằng dòng lệnh:

```bash
sqlcmd -S localhost -E -i GiaSuLy9.sql -f 65001
```

> Script có điều kiện `IF NOT EXISTS` ở mọi đoạn insert nên **chạy lại nhiều lần vẫn an toàn**,
> không nhân bản dữ liệu.

### 2. Cấu hình kết nối database

Copy file mẫu và sửa thông tin kết nối:

```bash
cp web/WEB-INF/ConnectDB.properties.example web/WEB-INF/ConnectDB.properties
```

```properties
url=jdbc:sqlserver://localhost:1433;databaseName=GiaSuLy9;trustServerCertificate=true
userID=sa
password=YOUR_PASSWORD_HERE
```

> `ConnectDB.properties` đã được thêm vào `.gitignore` — không commit mật khẩu lên GitHub.

### 3. Cấu hình AI (tuỳ chọn)

Key Gemini được lưu trong bảng `AI_SETTINGS` của database, quản lý tại
**Admin → Cấu hình AI**, không hardcode trong mã nguồn.

Biến môi trường cũng được hỗ trợ: `GEMINI_API_KEY`, `OPENAI_API_KEY`, `AI_API_KEY`.

### 4. Build & Deploy

```bash
ant clean dist
```

Sinh file `build/dist/GiaSuLy9.war`, copy vào thư mục `webapps` của Tomcat rồi khởi động Tomcat.

Truy cập: `http://localhost:8080/GiaSuLy9/`

## Tài khoản mặc định

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Admin | `admin` | `admin123` |
| Giáo viên | `giaovien` | `giaovien123` |
| Học sinh | `hocsinh` | `hocsinh123` |

## Cấu trúc thư mục

```
GiaSuLy9/
├── src/java/com/giasuly9/
│   ├── controller/   # Servlet xử lý request theo vai trò
│   ├── dao/          # Truy cập dữ liệu (SQL Server)
│   ├── model/        # JavaBean
│   ├── filter/       # AuthFilter, RoleFilter
│   └── util/         # Tiện ích: SecurityUtil, AiService...
├── web/
│   ├── views/        # JSP: student / teacher / admin / common
│   ├── assets/       # CSS, JS, hình ảnh
│   └── WEB-INF/      # web.xml, ConnectDB.properties
├── allowedlib/       # JAR dependency (JSTL, JDBC driver)
└── nbproject/        # Cấu hình build NetBeans/Ant
```

## Công nghệ

- Java 11 · Jakarta Servlet 6.0 · JSP + JSTL 2.0
- SQL Server + JDBC (`mssql-jdbc`)
- YouTube IFrame API (theo dõi tiến độ xem video)
- Gemini AI (OpenAI-compatible endpoint) cho trợ giảng AI
