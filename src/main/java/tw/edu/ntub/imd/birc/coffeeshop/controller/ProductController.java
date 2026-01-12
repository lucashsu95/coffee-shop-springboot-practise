package tw.edu.ntub.imd.birc.coffeeshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tw.edu.ntub.imd.birc.coffeeshop.util.http.ResponseEntityBuilder;

@Tag(name = "產品管理", description = "咖啡廳庫存管理系統 - 產品相關API")
@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {
    /**
     * A. 查詢所有產品
     */
    @Operation(summary = "查詢所有產品", description = "取得所有產品列表")
    @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "查詢成功", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<String> getAllProducts() {
            return ResponseEntityBuilder.success()
                            .message("查詢成功")
                            .build();
    }
}
