# invflow — Inventory System (Spring Boot + Thymeleaf)

invflow 是一個以實務ERP為出發點而設計的「進銷存 / 庫存異動」迷你練習專案。
採用 Spring Boot + Spring Security + Spring Data JPA + Thymeleaf 實作後台管理系統，
聚焦於清楚的權限分工與庫存異動流程，包含商品、供應商、使用者權限與庫存異動紀錄等模組。

---

## Tech Stack
- Java 21
- Spring Boot 3.5.9
- Spring MVC + Thymeleaf
- Spring Data JPA (Hibernate)
- Spring Security (Form Login)
- MySQL
- Validation (Jakarta Validation)

---

## Key Features

### 1) RBAC 權限管理（角色分級）
系統內建四種角色：
- `ADMIN` 管理員
- `MANAGER` 主管
- `OPERATOR` 專員
- `VIEWER` 訪客（只讀）

### 2) Supplier / Item 基本資料管理
- 列表查詢、關鍵字搜尋
- 詳情頁
- 新增 / 編輯（依角色限制）
- 啟用 / 停用（依角色限制）
- 自動記錄 created_by / updated_by
- SKU, WineType 等資料結構完整

### 3) Inventory Logs 庫存異動（核心）
- 異動類型 `MovementType`（進貨 / 銷貨 / 報廢 / 轉倉等）
- IN / OUT 方向與數量正負號處理
- 新增異動明細（create log）
- 庫存調整（adjust stock）

### 4) 報表（Summary）
- 單品庫存彙總（區間）
- 全品項彙總（區間）
- 明細列表可依關鍵字、異動類型、日期區間篩選

---

## Role Permissions (Summary)

> 實際權限以 `SecurityConfig` 與各 Controller `@PreAuthorize` 為準。

| Module                    | VIEWER | OPERATOR | MANAGER | ADMIN  |
----------------------------------------------------------------------------
| Dashboard / 查詢           |   ✅   |    ✅    |    ✅    |   ✅   |
| Suppliers：查詢/詳情        |   ✅   |    ✅    |    ✅    |   ✅   |
| Suppliers：新增/編輯        |   ❌   |    ❌    |    ✅    |   ❌（可視需求） |
| Suppliers：啟用/停用        |   ❌   |    ❌    |    ✅    |   ✅   |
| Items：查詢/詳情            |   ✅   |    ✅    |    ✅    |   ✅   |
| Items：新增/編輯            |   ❌   |    ✅    |    ✅    |   ❌（可視需求） |
| Items：啟用/停用            |   ❌   |    ❌    |    ✅    |   ✅   |
| Inventory Logs：查詢       |   ✅   |    ✅    |    ✅    |   ✅   |
| Inventory Logs：新增明細    |   ❌   |    ✅    |    ✅    |   ❌   |
| Inventory Adjust：庫存調整  |   ❌   |    ❌    |    ✅    |   ❌   |
| Users 管理                 |   ❌   |    ❌    |    ❌    |   ✅   |

---

## Environment Variables
本專案使用環境變數設定 MySQL 連線（見 `application.properties`）：

- `DB_URL` 例如：`jdbc:mysql://localhost:3306/invflow?useSSL=false&serverTimezone=Asia/Taipei`
- `DB_USERNAME`
- `DB_PASSWORD`

## Future Improvements / Roadmap
目前專案著重於核心進銷存流程與角色權限控管。
以下功能刻意未納入本次實作範圍，以維持系統結構清晰與可維護性，作為未來可延伸的方向。

### 1) JWT 驗證
- 導入 JWT 驗證機制，支援前後端分離或第三方系統整合

### 2) 審核流程 (Approval Workflow)
- 為庫存調整或大量異體加入審核流程
- 需經 MANAGER 或更高層級確認後才實際生效

### 3) 報表與分析功能
- Excel 匯出或排程報表功能