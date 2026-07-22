package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.FileTooLargeException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileException;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class FileContentInspector {

    private static final int BUFFER_SIZE = 8192;
    private static final int SIGNATURE_SIZE = 16;
    private static final int MAX_OFFICE_ENTRY_COUNT = 4096;
    private static final long PROFILE_IMAGE_MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "pdf", "docx", "xlsx", "mp4");

    private final long maxUploadBytes;

    public FileContentInspector(FileStorageProperties properties) {
        this.maxUploadBytes = properties.maxUploadBytes();
    }

    public FileInspection inspect(String originalName, long declaredSize, FileContentSource source) {
        String extension = validateNameAndExtractExtension(originalName);
        validateDeclaredSize(declaredSize);
        if (source == null) {
            throw new InvalidFileException("missing-content");
        }

        InspectionBuffer buffer = hashAndReadSignature(source);
        if (buffer.sizeBytes() != declaredSize) {
            throw new InvalidFileException("declared-size-mismatch");
        }

        String detectedType = detectContentType(extension, buffer.signature(), source);
        return new FileInspection(detectedType, buffer.sizeBytes(), buffer.sha256Hash());
    }

    public FileInspection inspectProfileImage(String originalName, long declaredSize,
                                              FileContentSource source) {
        String extension = validateNameAndExtractExtension(originalName);
        if (!Set.of("jpg", "jpeg", "png", "webp").contains(extension)) {
            throw new InvalidFileException("unsupported-profile-image-extension");
        }
        if (declaredSize > PROFILE_IMAGE_MAX_BYTES) {
            throw new FileTooLargeException();
        }
        FileInspection inspection = inspect(originalName, declaredSize, source);
        if (inspection.sizeBytes() > PROFILE_IMAGE_MAX_BYTES
                || !inspection.contentType().startsWith("image/")) {
            throw new InvalidFileException("invalid-profile-image");
        }
        return inspection;
    }

    private String validateNameAndExtractExtension(String originalName) {
        if (originalName == null || originalName.isBlank() || originalName.length() > 255
                || originalName.contains("/") || originalName.contains("\\")
                || originalName.chars().anyMatch(Character::isISOControl)) {
            throw new InvalidFileException("invalid-name");
        }

        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == originalName.length() - 1) {
            throw new InvalidFileException("missing-extension");
        }
        String extension = originalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("unsupported-extension");
        }
        return extension;
    }

    private void validateDeclaredSize(long declaredSize) {
        if (declaredSize <= 0) {
            throw new InvalidFileException("empty-file");
        }
        if (declaredSize > maxUploadBytes) {
            throw new FileTooLargeException();
        }
    }

    private InspectionBuffer hashAndReadSignature(FileContentSource source) {
        MessageDigest digest = sha256Digest();
        byte[] signature = new byte[SIGNATURE_SIZE];
        int signatureLength = 0;
        long totalBytes = 0;
        byte[] bytes = new byte[BUFFER_SIZE];

        try (InputStream input = source.openStream()) {
            int read;
            while ((read = input.read(bytes)) != -1) {
                totalBytes += read;
                if (totalBytes > maxUploadBytes) {
                    throw new FileTooLargeException();
                }
                digest.update(bytes, 0, read);
                if (signatureLength < SIGNATURE_SIZE) {
                    int copyLength = Math.min(read, SIGNATURE_SIZE - signatureLength);
                    System.arraycopy(bytes, 0, signature, signatureLength, copyLength);
                    signatureLength += copyLength;
                }
            }
        } catch (IOException exception) {
            throw new InvalidFileException("unreadable-content");
        }

        if (totalBytes == 0) {
            throw new InvalidFileException("empty-file");
        }
        return new InspectionBuffer(copyOf(signature, signatureLength), totalBytes,
                HexFormat.of().formatHex(digest.digest()));
    }

    private String detectContentType(String extension, byte[] signature, FileContentSource source) {
        return switch (extension) {
            case "jpg", "jpeg" -> require(signature, new int[]{0xff, 0xd8, 0xff}, "image/jpeg");
            case "png" -> require(signature,
                    new int[]{0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}, "image/png");
            case "webp" -> requireWebp(signature);
            case "pdf" -> require(signature, new int[]{0x25, 0x50, 0x44, 0x46, 0x2d}, "application/pdf");
            case "mp4" -> requireMp4(signature);
            case "docx" -> requireOfficeDocument(source, true);
            case "xlsx" -> requireOfficeDocument(source, false);
            default -> throw new InvalidFileException("unsupported-extension");
        };
    }

    private String require(byte[] signature, int[] expected, String contentType) {
        if (signature.length < expected.length) {
            throw new InvalidFileException("signature-mismatch");
        }
        for (int index = 0; index < expected.length; index++) {
            if (Byte.toUnsignedInt(signature[index]) != expected[index]) {
                throw new InvalidFileException("signature-mismatch");
            }
        }
        return contentType;
    }

    private String requireWebp(byte[] signature) {
        if (!matchesAscii(signature, 0, "RIFF") || !matchesAscii(signature, 8, "WEBP")) {
            throw new InvalidFileException("signature-mismatch");
        }
        return "image/webp";
    }

    private String requireMp4(byte[] signature) {
        if (!matchesAscii(signature, 4, "ftyp")) {
            throw new InvalidFileException("signature-mismatch");
        }
        return "video/mp4";
    }

    private String requireOfficeDocument(FileContentSource source, boolean word) {
        boolean contentTypes = false;
        boolean documentDirectory = false;
        int entryCount = 0;
        try (ZipInputStream zip = new ZipInputStream(source.openStream())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_OFFICE_ENTRY_COUNT) {
                    throw new InvalidFileException("too-many-office-entries");
                }
                String name = entry.getName();
                contentTypes |= "[Content_Types].xml".equals(name);
                documentDirectory |= word ? name.startsWith("word/") : name.startsWith("xl/");
            }
        } catch (IOException exception) {
            throw new InvalidFileException("invalid-office-file");
        }
        if (!contentTypes || !documentDirectory) {
            throw new InvalidFileException("invalid-office-file");
        }
        return word
                ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    private boolean matchesAscii(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != (byte) expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }

    private byte[] copyOf(byte[] source, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(source, 0, copy, 0, length);
        return copy;
    }

    private record InspectionBuffer(byte[] signature, long sizeBytes, String sha256Hash) {
    }
}
