package tw.edu.ntub.imd.birc.practice.dto.file.uploader;

import org.springframework.lang.NonNull;
import tw.edu.ntub.imd.birc.practice.dto.file.directory.Directory;
import tw.edu.ntub.imd.birc.practice.exception.file.FileException;

@FunctionalInterface
public interface Uploader {
    @NonNull
    UploadResult upload(@NonNull Directory uploadTo) throws FileException;
}
