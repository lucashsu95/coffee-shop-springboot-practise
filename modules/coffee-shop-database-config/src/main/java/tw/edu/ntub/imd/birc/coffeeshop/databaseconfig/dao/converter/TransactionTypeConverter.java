package tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.dao.converter;

import tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.enumerate.TransactionType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * TransactionType 枚舉與資料庫字串的轉換器
 */
@Converter(autoApply = false)
public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public TransactionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return TransactionType.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無效的交易類型: " + dbData);
        }
    }
}


