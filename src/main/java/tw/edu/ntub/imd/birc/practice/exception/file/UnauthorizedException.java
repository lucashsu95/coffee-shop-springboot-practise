package tw.edu.ntub.imd.birc.practice.exception.file;

public class UnauthorizedException extends FileException {

    public UnauthorizedException(Throwable cause) {
        super("檔案權限不足", cause);
    }

    @Override
    public String getReason() {
        return "Unauthorized";
    }
}
