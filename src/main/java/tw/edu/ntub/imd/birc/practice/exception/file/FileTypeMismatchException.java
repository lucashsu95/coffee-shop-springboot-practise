package tw.edu.ntub.imd.birc.practice.exception.file;

public class FileTypeMismatchException extends FileException {
    public FileTypeMismatchException(String error) {
        super(error);
    }

    @Override
    public String getReason() {
        return "TypeMismatch";
    }
}
