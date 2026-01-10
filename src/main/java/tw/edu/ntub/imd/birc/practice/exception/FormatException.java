package tw.edu.ntub.imd.birc.practice.exception;

import tw.edu.ntub.birc.common.exception.ProjectException;

public class FormatException extends ProjectException {

    public FormatException(String format) {
        super(format + "格式錯誤");
    }

    @Override
    public String getErrorCode() {
        return "format - error";
    }
}
