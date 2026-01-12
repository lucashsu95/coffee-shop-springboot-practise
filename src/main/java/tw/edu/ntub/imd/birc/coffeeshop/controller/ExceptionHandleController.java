package tw.edu.ntub.imd.birc.coffeeshop.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import tw.edu.ntub.birc.common.exception.ProjectException;
import tw.edu.ntub.birc.common.exception.UnknownException;
import tw.edu.ntub.birc.common.exception.date.ParseDateException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.ConvertPropertyException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.MethodNotSupportedException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.NullRequestBodyException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.RequiredParameterException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.file.FileNotExistException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.file.UploadFileTooLargeException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.form.InvalidFormDateFormatException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.form.InvalidFormException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.form.InvalidFormNumberFormatException;
import tw.edu.ntub.imd.birc.coffeeshop.exception.form.InvalidRequestFormatException;
import tw.edu.ntub.imd.birc.coffeeshop.util.http.ResponseEntityBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Field;

@Log4j2
@ControllerAdvice
public class ExceptionHandleController {
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<String> handleProjectException(ProjectException e) {
        return ResponseEntityBuilder.error(e).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidFormatException(HttpMessageNotReadableException e) {
        Throwable rootCause = e.getRootCause();

        // 1. 處理基礎類型轉換錯誤 (如 String 轉 int 失敗) - 保持 Snippet 2 的簡潔
        if (rootCause instanceof NumberFormatException) {
            return ResponseEntityBuilder.error(new InvalidFormNumberFormatException((NumberFormatException) rootCause)).build();
        }
        if (rootCause instanceof ParseDateException) {
            return ResponseEntityBuilder.error(new InvalidFormDateFormatException((ParseDateException) rootCause)).build();
        }

        // 2. 處理 Jackson 解析錯誤 (主要邏輯)
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ex = (InvalidFormatException) e.getCause();
            Class<?> targetType = ex.getTargetType();
            List<JsonMappingException.Reference> path = ex.getPath();

            // A. 獲取欄位名稱與描述 (組裝邏輯)
            String fieldName = path.isEmpty() ? "" : path.get(0).getFieldName();
            String displayName = fieldName; // 預設使用變數名

            // 嘗試透過反射獲取 @Schema 的中文描述 (加入 Snippet 1 的優勢)
            if (!path.isEmpty()) {
                Object fromObject = path.get(0).getFrom();
                if (fromObject != null) {
                    // 呼叫下方提取出的輔助方法
                    String schemaDesc = getFieldDescription(fromObject.getClass(), fieldName);
                    if (StringUtils.hasText(schemaDesc)) {
                        displayName = schemaDesc; // 如果有中文描述，覆蓋變數名
                    }
                }
            }

            // B. 根據目標類型構建錯誤訊息 (保持 Snippet 2 的 Enum 支援)
            boolean isNumberTarget = Number.class.isAssignableFrom(targetType) || targetType.isPrimitive();

            // 情況一：數字錯誤
            if (isNumberTarget) {
                String message = displayName + " - \"" + ex.getValue() + "\"輸入的文字中包含非數字文字";
                return ResponseEntityBuilder.error(new InvalidRequestFormatException(message)).build();
            }

            // 情況二：Enum 錯誤
            if (targetType.isEnum()) {
                Object[] enumConstants = targetType.getEnumConstants();
                String validValues = java.util.Arrays.stream(enumConstants)
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.joining(", "));
                String message = displayName + " - \"" + ex.getValue() +
                        "\"不是有效的值，有效值為：[" + validValues + "]";
                return ResponseEntityBuilder.error(new InvalidRequestFormatException(message)).build();
            }

            // 情況三：其他類型錯誤 (兜底)
            return ResponseEntityBuilder.error(new InvalidRequestFormatException(ex.getOriginalMessage())).build();
        }

        // 3. 未知或 Body 為空
        return ResponseEntityBuilder.error(new NullRequestBodyException(e)).build();
    }

    private String getFieldDescription(Class<?> clazz, String fieldName) {
        Field declaredField = null;
        Class<?> currentClass = clazz;

        // 向上查找父類 (Inheritance support)
        while (currentClass != null && declaredField == null) {
            try {
                declaredField = currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                currentClass = currentClass.getSuperclass();
            }
        }

        if (declaredField != null) {
            // 讀取 @Schema (User friendly support)
            if (declaredField.isAnnotationPresent(Schema.class)) {
                Schema schema = declaredField.getAnnotation(Schema.class);
                return schema.description();
            }
        }
        return null; // 沒找到或沒註解，回傳 null 讓主程式決定用 fieldName
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotExistException.class)
    public void handleFileNotExistException(FileNotExistException e) {
        log.error("找不到檔案", e);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntityBuilder.error()
                .errorCode("User - AccessDenied")
                .message("您並無此操作之權限，請嘗試重新登入")
                .build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntityBuilder.error(new UploadFileTooLargeException(e)).build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException e) {
        return ResponseEntityBuilder.error(new MethodNotSupportedException(
                request.getRequestURL().toString(),
                request.getMethod(),
                e)).build();
    }

    @ExceptionHandler(InvalidPropertyException.class)
    public ResponseEntity<String> handleInvalidPropertyException(InvalidPropertyException e) {
        return ResponseEntityBuilder.error(new ConvertPropertyException(e)).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        ConstraintViolation<?> constraintViolation = constraintViolations.stream().findAny().orElseThrow();
        return ResponseEntityBuilder.error(new InvalidFormException(constraintViolation.getMessage())).build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        return ResponseEntityBuilder.error(new RequiredParameterException(e.getParameterName())).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnknownException(Exception e) {
        return ResponseEntityBuilder.error(new UnknownException(e)).build();
    }
}

