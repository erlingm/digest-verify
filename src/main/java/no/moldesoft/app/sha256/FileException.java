package no.moldesoft.app.sha256;

import java.nio.file.NoSuchFileException;

class FileException extends RuntimeException {

    private final String fileName;

    public FileException(NoSuchFileException e) {
        fileName = e.getFile();
    }

    public String getFileName() {
        return fileName;
    }
}
