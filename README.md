# 咖啡廳庫存管理系統 - Spring Boot實作題

## 1. 情境故事

你受僱於一家剛起步的文青咖啡廳 "Code & Coffee"。老闆需要一個「庫存管理系統」來追蹤店內的咖啡豆和甜點。

老闆說：「我們賣兩種東西：**咖啡豆(Bean)** 和 **甜點(Dessert)**。我需要你幫我管理庫存，包括進貨、出貨、查詢剩餘數量。特別注意，**每次出貨時，要同時扣除庫存並記錄這筆交易**，不然會計會瘋掉！」

---

## 2. 技術要求

### 必須使用的技術
- **Spring Boot**
- **分層架構**: Controller → Service → Repository (不可在Controller直接操作Repository)
- **錯誤處理**: 使用`@ControllerAdvice`或`@ExceptionHandler`統一處理

---

## 3. 資料庫設計

### Product Table (產品主檔)
| 欄位名稱 | 型別 | 說明 | 限制 |
|---------|------|------|------|
| id | Long | 主鍵 | Auto Increment |
| name | String | 產品名稱 | NOT NULL, 最多50字 |
| type | String | 產品類型 | 只能是 "BEAN" 或 "DESSERT" |
| price | Integer | 單價 | NOT NULL, 必須 > 0 |
| stock | Integer | 庫存數量 | NOT NULL, 不可為負數 |

### Transaction Table (交易記錄)
| 欄位名稱 | 型別 | 說明 | 限制 |
|---------|------|------|------|
| id | Long | 主鍵 | Auto Increment |
| productId | Long | 關聯的產品ID | Foreign Key |
| type | String | 交易類型 | "IN" (進貨) 或 "OUT" (出貨) |
| quantity | Integer | 數量 | NOT NULL, 必須 > 0 |
| timestamp | LocalDateTime | 交易時間 | 自動記錄 |

---

## 4. API規格

### A. 查詢所有產品
```
GET /api/v1/products
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "衣索比亞耶加雪菲",
    "type": "BEAN",
    "price": 450,
    "stock": 120
  },
  {
    "id": 2,
    "name": "提拉米蘇",
    "type": "DESSERT",
    "price": 150,
    "stock": 8
  }
]
```

---

### B. 新增產品
```
POST /api/v1/products
```

**Request Body:**
```json
{
  "name": "巴拿馬藝妓",
  "type": "BEAN",
  "price": 680,
  "stock": 50
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "name": "巴拿馬藝妓",
  "type": "BEAN",
  "price": 680,
  "stock": 50
}
```

**錯誤情境:**
- `price` 是字串 → `400 Bad Request`: `"欄位格式錯誤"`
- 缺少 `name` 欄位 → `400 Bad Request`: `"缺少必要欄位: name"`
- `type` 不是 BEAN 或 DESSERT → `400 Bad Request`: `"type只能是BEAN或DESSERT"`
- `price` 或 `stock` 是負數 → `400 Bad Request`: `"價格和庫存必須大於0"`

---

### C. 進貨 (增加庫存)
```
POST /api/v1/products/{id}/stock/in
```

**Request Body:**
```json
{
  "quantity": 30
}
```

**功能說明:**
1. 將產品庫存增加 30
2. 在 Transaction 表新增一筆 `type="IN"` 的記錄

**Response (200 OK):**
```json
{
  "message": "進貨成功",
  "productId": 1,
  "newStock": 150
}
```

**錯誤情境:**
- 產品不存在 → `404 Not Found`: `"產品不存在"`
- `quantity` 是負數 → `400 Bad Request`: `"數量必須大於0"`

---

### D. 出貨 (扣除庫存)
```
POST /api/v1/products/{id}/stock/out
```

**Request Body:**
```json
{
  "quantity": 20
}
```

**功能說明:**
1. 檢查庫存是否足夠
2. 扣除產品庫存 20
3. 在 Transaction 表新增一筆 `type="OUT"` 的記錄
4. **以上兩個動作必須同時成功或同時失敗** (這就是需要用到特殊機制的地方)

**Response (200 OK):**
```json
{
  "message": "出貨成功",
  "productId": 1,
  "remainingStock": 100
}
```

**錯誤情境:**
- 產品不存在 → `404 Not Found`: `"產品不存在"`
- 庫存不足 → `400 Bad Request`: `"庫存不足，目前庫存: 10"`
- `quantity` 是負數 → `400 Bad Request`: `"數量必須大於0"`

---

### E. 查詢產品的交易記錄
```
GET /api/v1/products/{id}/transactions
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "productId": 1,
    "type": "IN",
    "quantity": 50,
    "timestamp": "2026-01-10T10:30:00"
  },
  {
    "id": 2,
    "productId": 1,
    "type": "OUT",
    "quantity": 20,
    "timestamp": "2026-01-10T14:20:00"
  }
]
```

**錯誤情境:**
- 產品不存在 → `404 Not Found`: `"產品不存在"`

---

## 5. 評分標準

| 項目 | 配分 | 檢查重點 |
|------|------|----------|
| **功能正確性** | 30% | 所有API能正常運作，資料正確寫入DB |
| **錯誤處理** | 25% | 所有錯誤情境都有對應的HTTP Status和訊息 |
| **分層架構** | 20% | Controller只處理HTTP、Service包含商業邏輯、Repository只做資料存取 |
| **資料一致性** | 15% | 出貨時「扣庫存」和「記錄交易」必須同時成功(提示:在Service層的方法上加上`@Transactional`註解) |
| **JPA使用** | 10% | Entity設計正確、使用Repository而非原生SQL |

---

## 6. 提示與注意事項

### 關於「同時成功或同時失敗」
當你執行出貨功能時，需要做兩件事:
1. 更新Product的stock欄位
2. 新增一筆Transaction記錄

**問題:** 如果第1步成功，但第2步失敗(例如資料庫寫入錯誤)，會發生什麼事?
→ 庫存已經扣了，但沒有交易記錄！帳對不起來！

**解決方法:** 在你的Service方法上加上 `@Transactional` 註解，Spring會幫你確保:
- 兩個操作都成功 → 一起儲存
- 任何一個失敗 → 全部取消(Rollback)

範例:
```java
@Transactional
public StockResponse processStockOut(Long productId, int quantity) {
    // 1. 扣庫存
    // 2. 記錄交易
    // 如果任何一步出錯，整個方法的資料庫變更都會被取消
}
```

### 錯誤處理範例
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400)
            .body(new ErrorResponse("欄位格式錯誤"));
    }
}
```

---

## 7. 測試方式

請使用 **Postman** 或 **curl** 測試以下情境:

1. ✅ 新增一個產品，stock設為100
2. ✅ 進貨50，檢查stock變成150
3. ✅ 出貨30，檢查stock變成120，且Transaction有記錄
4. ✅ 查詢交易記錄，應該看到一筆IN和一筆OUT
5. ❌ 嘗試出貨200(超過庫存)，應回傳錯誤
6. ❌ 新增產品時price給字串，應回傳"欄位格式錯誤"
7. ❌ 查詢不存在的產品ID，應回傳404

---

## 8. 繳交內容

1. 完整的Spring Boot專案(含build.gradle)
2. application.properties設定檔

---

**截止日期:** 一週後  
**有問題請立即提問，不要最後一天才說不會寫！**

## 開發環境

* Spring Boot：2.6.6
* Java(Open JDK 11.0.2)
* Gradle
* lombok

## 注意事項

* 需複製resources裡面的application-local-example.yml命名為"application-local.yml"，再根據個人需求修改裡面的值