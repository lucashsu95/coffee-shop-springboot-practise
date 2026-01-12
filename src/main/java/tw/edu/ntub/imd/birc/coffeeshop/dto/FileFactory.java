package tw.edu.ntub.imd.birc.coffeeshop.dto;

import lombok.experimental.UtilityClass;
import tw.edu.ntub.imd.birc.coffeeshop.exception.file.FileExtensionNotFoundException;
import tw.edu.ntub.imd.birc.coffeeshop.util.file.FileUtils;

import java.nio.file.Path;

@UtilityClass
public class FileFactory {
    public File create(Path path) {
        Path fullFileNamePath = path.getFileName();
        try {
            String fileExtension = FileUtils.getFileExtension(fullFileNamePath.toString());
            switch (fileExtension) {
                case "txt":
                case "log":
                    return new TextFile(path);
            }
        } catch (FileExtensionNotFoundException ignore) {
        }
        return new CommonFile(path);
    }
}

