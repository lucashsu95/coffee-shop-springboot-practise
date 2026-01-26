package tw.edu.ntub.imd.birc.coffeeshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tw.edu.ntub.imd.birc.coffeeshop.config.TestSecurityConfig;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.dao.ProductDAO;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.dao.TransactionDAO;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.entity.Product;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.entity.Transaction;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.enumerate.ProductType;
import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.enumerate.TransactionType;
import tw.edu.ntub.imd.birc.coffeeshop.bean.ProductBean;
import tw.edu.ntub.imd.birc.coffeeshop.dto.StockQuantity;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 產品 Controller 整合測試
 * 涵蓋所有 API 端點的成功與錯誤情境
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private ProductDAO productDAO;

        @Autowired
        private TransactionDAO transactionDAO;

        @BeforeEach
        void setUp() {
                // 清空資料庫
                transactionDAO.deleteAll();
                productDAO.deleteAll();
        }

        // ========================================
        // A. 查詢所有產品 - 成功情境
        // ========================================

        @Test
        @DisplayName("測試 GET /api/v1/products - 查詢所有產品（空列表）")
        void testGetAllProducts_Empty() throws Exception {
                mockMvc.perform(get("/api/v1/products"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.message", is("查詢成功")))
                                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("測試 GET /api/v1/products - 查詢所有產品（有資料）")
        void testGetAllProducts_WithData() throws Exception {
                // 準備測試資料
                Product product1 = new Product();
                product1.setName("衣索比亞耶加雪菲");
                product1.setType(ProductType.BEAN);
                product1.setPrice(450);
                product1.setStock(120);
                productDAO.save(product1);

                Product product2 = new Product();
                product2.setName("提拉米蘇");
                product2.setType(ProductType.DESSERT);
                product2.setPrice(150);
                product2.setStock(8);
                productDAO.save(product2);

                mockMvc.perform(get("/api/v1/products"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].name", is("衣索比亞耶加雪菲")))
                                .andExpect(jsonPath("$.data[0].type", is("BEAN")))
                                .andExpect(jsonPath("$.data[0].price", is(450)))
                                .andExpect(jsonPath("$.data[0].stock", is(120)))
                                .andExpect(jsonPath("$.data[1].name", is("提拉米蘇")))
                                .andExpect(jsonPath("$.data[1].type", is("DESSERT")))
                                .andExpect(jsonPath("$.data[1].price", is(150)))
                                .andExpect(jsonPath("$.data[1].stock", is(8)));
        }

        // ========================================
        // B. 新增產品 - 成功情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products - 新增產品成功")
        void testCreateProduct_Success() throws Exception {
                ProductBean productBean = new ProductBean();
                productBean.setName("巴拿馬藝妓");
                productBean.setType(ProductType.BEAN);
                productBean.setPrice(680);
                productBean.setStock(50);

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productBean)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.message", is("新增成功")));

                // 驗證資料庫
                java.util.List<Product> products = productDAO.findAll();
                org.junit.jupiter.api.Assertions.assertEquals(1, products.size());
                Product savedProduct = products.get(0);
                org.junit.jupiter.api.Assertions.assertEquals("巴拿馬藝妓", savedProduct.getName());
                org.junit.jupiter.api.Assertions.assertEquals(ProductType.BEAN, savedProduct.getType());
                org.junit.jupiter.api.Assertions.assertEquals(680, savedProduct.getPrice());
                org.junit.jupiter.api.Assertions.assertEquals(50, savedProduct.getStock());
        }

        // ========================================
        // B. 新增產品 - 錯誤情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products - 缺少必要欄位 name")
        void testCreateProduct_MissingName() throws Exception {
                String json = "{" +
                                "\"type\": \"BEAN\"," +
                                "\"price\": 680," +
                                "\"stock\": 50" +
                                "}";

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("name")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products - price 是字串（欄位格式錯誤）")
        void testCreateProduct_PriceIsString() throws Exception {
                String json = "{" +
                                "\"name\": \"巴拿馬藝妓\"," +
                                "\"type\": \"BEAN\"," +
                                "\"price\": \"invalid\"," +
                                "\"stock\": 50" +
                                "}";

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("單價")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products - type 不是 BEAN 或 DESSERT")
        void testCreateProduct_InvalidType() throws Exception {
                String json = "{" +
                                "\"name\": \"巴拿馬藝妓\"," +
                                "\"type\": \"INVALID\"," +
                                "\"price\": 680," +
                                "\"stock\": 50" +
                                "}";

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("產品類型")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products - price 是負數")
        void testCreateProduct_NegativePrice() throws Exception {
                ProductBean productBean = new ProductBean();
                productBean.setName("巴拿馬藝妓");
                productBean.setType(ProductType.BEAN);
                productBean.setPrice(-100);
                productBean.setStock(50);

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productBean)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("價格和庫存必須大於0")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products - stock 是負數")
        void testCreateProduct_NegativeStock() throws Exception {
                ProductBean productBean = new ProductBean();
                productBean.setName("巴拿馬藝妓");
                productBean.setType(ProductType.BEAN);
                productBean.setPrice(680);
                productBean.setStock(-10);

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(productBean)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("價格和庫存必須大於0")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
        }

        // ========================================
        // C. 進貨 - 成功情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/in - 進貨成功")
        void testStockIn_Success() throws Exception {
                // 準備測試資料
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(100);
                product = productDAO.save(product);

                StockQuantity request = new StockQuantity();
                request.setQuantity(30);

                mockMvc.perform(post("/api/v1/products/" + product.getId() + "/stock/in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.message", is("進貨成功")));

                // 驗證資料庫中的庫存已更新
                Product updatedProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(130, updatedProduct.getStock());

                // 驗證交易記錄已建立
                org.junit.jupiter.api.Assertions.assertEquals(1,
                                transactionDAO.findByProductIdOrderByTimestampDesc(product.getId()).size());

                // 清理測試資料
                transactionDAO.deleteAll();
                productDAO.deleteById(product.getId());
        }

        // ========================================
        // C. 進貨 - 錯誤情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/in - 產品不存在")
        void testStockIn_ProductNotFound() throws Exception {
                StockQuantity request = new StockQuantity();
                request.setQuantity(30);

                mockMvc.perform(post("/api/v1/products/9999/stock/in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", is("產品不存在")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/in - quantity 是負數")
        void testStockIn_NegativeQuantity() throws Exception {
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(100);
                product = productDAO.save(product);

                StockQuantity request = new StockQuantity();
                request.setQuantity(-10);

                mockMvc.perform(post("/api/v1/products/" + product.getId() + "/stock/in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("數量必須大於0")));

                // 驗證庫存未變動且未新增交易
                Product unchangedProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(100, unchangedProduct.getStock());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        // ========================================
        // D. 出貨 - 成功情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/out - 出貨成功")
        void testStockOut_Success() throws Exception {
                // 準備測試資料
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(100);
                product = productDAO.save(product);

                StockQuantity request = new StockQuantity();
                request.setQuantity(20);

                mockMvc.perform(post("/api/v1/products/" + product.getId() + "/stock/out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.message", is("出貨成功")));

                // 驗證資料庫中的庫存已更新
                Product updatedProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(80, updatedProduct.getStock());

                // 驗證交易記錄已建立
                org.junit.jupiter.api.Assertions.assertEquals(1,
                                transactionDAO.findByProductIdOrderByTimestampDesc(product.getId()).size());

                // 清理測試資料
                transactionDAO.deleteAll();
                productDAO.deleteById(product.getId());
        }

        // ========================================
        // D. 出貨 - 錯誤情境
        // ========================================

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/out - 產品不存在")
        void testStockOut_ProductNotFound() throws Exception {
                StockQuantity request = new StockQuantity();
                request.setQuantity(20);

                mockMvc.perform(post("/api/v1/products/9999/stock/out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", is("產品不存在")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/out - 庫存不足")
        void testStockOut_InsufficientStock() throws Exception {
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(10);
                product = productDAO.save(product);

                StockQuantity request = new StockQuantity();
                request.setQuantity(200);

                mockMvc.perform(post("/api/v1/products/" + product.getId() + "/stock/out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", is("庫存不足，目前庫存: 10")));

                // 驗證庫存未變動且未新增交易
                Product unchangedProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(10, unchangedProduct.getStock());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        @Test
        @DisplayName("測試 POST /api/v1/products/{id}/stock/out - quantity 是負數")
        void testStockOut_NegativeQuantity() throws Exception {
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(100);
                product = productDAO.save(product);

                StockQuantity request = new StockQuantity();
                request.setQuantity(-10);

                mockMvc.perform(post("/api/v1/products/" + product.getId() + "/stock/out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", containsString("數量必須大於0")));

                // 驗證庫存未變動且未新增交易
                Product unchangedProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(100, unchangedProduct.getStock());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        // ========================================
        // E. 查詢交易記錄 - 成功情境
        // ========================================

        @Test
        @DisplayName("測試 GET /api/v1/products/{id}/transactions - 查詢交易記錄")
        void testGetTransactions_Success() throws Exception {
                // 準備測試資料 - 直接建立交易紀錄
                Product product = new Product();
                product.setName("衣索比亞耶加雪菲");
                product.setType(ProductType.BEAN);
                product.setPrice(450);
                product.setStock(120);
                product = productDAO.save(product);

                Transaction inTx = new Transaction();
                inTx.setProductId(product.getId());
                inTx.setType(TransactionType.IN);
                inTx.setQuantity(50);
                transactionDAO.save(inTx);

                Transaction outTx = new Transaction();
                outTx.setProductId(product.getId());
                outTx.setType(TransactionType.OUT);
                outTx.setQuantity(30);
                transactionDAO.save(outTx);

                // 查詢交易記錄
                mockMvc.perform(get("/api/v1/products/" + product.getId() + "/transactions"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result", is(true)))
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].productId", is(product.getId().intValue())))
                                .andExpect(jsonPath("$.data[0].type", is("OUT")))
                                .andExpect(jsonPath("$.data[0].quantity", is(30)))
                                .andExpect(jsonPath("$.data[1].type", is("IN")))
                                .andExpect(jsonPath("$.data[1].quantity", is(50)));

                // 驗證資料庫中的交易與庫存
                org.junit.jupiter.api.Assertions.assertEquals(2,
                                transactionDAO.findByProductIdOrderByTimestampDesc(product.getId()).size());
                Product finalProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(120, finalProduct.getStock());

                // 清理測試資料
                transactionDAO.deleteAll();
                productDAO.deleteById(product.getId());
        }

        // ========================================
        // E. 查詢交易記錄 - 錯誤情境
        // ========================================

        @Test
        @DisplayName("測試 GET /api/v1/products/{id}/transactions - 產品不存在")
        void testGetTransactions_ProductNotFound() throws Exception {
                mockMvc.perform(get("/api/v1/products/9999/transactions"))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.result", is(false)))
                                .andExpect(jsonPath("$.message", is("產品不存在")));

                // 驗證資料庫未新增資料
                org.junit.jupiter.api.Assertions.assertEquals(0, productDAO.count());
                org.junit.jupiter.api.Assertions.assertEquals(0, transactionDAO.count());
        }

        // ========================================
        // 綜合測試：完整流程
        // ========================================

        @Test
        @DisplayName("綜合測試 - 完整的產品生命週期")
        void testCompleteProductLifecycle() throws Exception {
                // 準備資料：直接建立產品與交易紀錄，避免透過其它端點作前置操作
                Product product = new Product();
                product.setName("測試咖啡豆");
                product.setType(ProductType.BEAN);
                product.setPrice(500);
                product.setStock(120);
                product = productDAO.save(product);

                Transaction inTx = new Transaction();
                inTx.setProductId(product.getId());
                inTx.setType(TransactionType.IN);
                inTx.setQuantity(50);
                transactionDAO.save(inTx);

                Transaction outTx = new Transaction();
                outTx.setProductId(product.getId());
                outTx.setType(TransactionType.OUT);
                outTx.setQuantity(30);
                transactionDAO.save(outTx);

                // 查詢交易記錄
                mockMvc.perform(get("/api/v1/products/" + product.getId() + "/transactions"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].type", is("OUT")))
                                .andExpect(jsonPath("$.data[1].type", is("IN")));

                // 查詢所有產品，確認庫存正確
                mockMvc.perform(get("/api/v1/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].stock", is(120)));

                // 驗證庫存與交易數量
                Product finalProduct = productDAO.findById(product.getId()).orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals(120, finalProduct.getStock());
                org.junit.jupiter.api.Assertions.assertEquals(2,
                                transactionDAO.findByProductIdOrderByTimestampDesc(product.getId()).size());

                // 清理測試資料
                transactionDAO.deleteAll();
                productDAO.deleteById(product.getId());
        }
}
