package tw.edu.ntub.imd.birc.coffeeshop.dto.file;

import tw.edu.ntub.imd.birc.coffeeshop.dto.file.directory.Directory;

import java.nio.file.StandardCopyOption;

public interface Copyable {
    void copyTo(Directory newDirectory, StandardCopyOption... options);
}

