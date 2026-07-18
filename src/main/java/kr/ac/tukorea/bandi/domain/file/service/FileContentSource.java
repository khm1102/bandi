package kr.ac.tukorea.bandi.domain.file.service;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface FileContentSource {

    InputStream openStream() throws IOException;
}
