package tw.edu.ntub.imd.birc.coffeeshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 庫存操作請求物件
 */
public class StockQuantity {

    /**
     * 數量
     */
    @NotNull(message = "缺少必要欄位: quantity")
    @Min(value = 1, message = "數量必須大於0")
    @JsonProperty("quantity")
    private Integer quantity;

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
