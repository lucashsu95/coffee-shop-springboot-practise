package tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.dao.converter;

import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.enumerate.ProductType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * ProductType 枚舉與資料庫字串的轉換器
 */
@Converter(autoApply = false)
public class ProductTypeConverter implements AttributeConverter<ProductType, String> {

    @Override
    public String convertToDatabaseColumn(ProductType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ProductType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return ProductType.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無效的產品類型: " + dbData);
        }
    }
}

