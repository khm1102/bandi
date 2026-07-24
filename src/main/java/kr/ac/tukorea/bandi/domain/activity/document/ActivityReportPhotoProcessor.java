package kr.ac.tukorea.bandi.domain.activity.document;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

@Component
public class ActivityReportPhotoProcessor {

    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final int MAX_SIDE = 12_000;
    private static final int CANVAS_WIDTH = 1_600;
    private static final int CANVAS_HEIGHT = 1_200;

    public byte[] normalize(ActivityReportPhotoParam photo) {
        byte[] bytes = photo == null ? null : photo.bytes();
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw invalid("photo.size");
        }
        String expectedFormat = detectFormat(bytes);
        validateContentType(photo.contentType(), expectedFormat);

        BufferedImage decoded = decode(bytes, expectedFormat);
        BufferedImage oriented = orient(decoded,
                "jpeg".equals(expectedFormat) ? readExifOrientation(bytes) : 1);
        return encodePng(contain(oriented));
    }

    private String detectFormat(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A
                && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        throw invalid("photo.type");
    }

    private void validateContentType(String contentType, String format) {
        boolean matches = "png".equals(format) && "image/png".equals(contentType)
                || "jpeg".equals(format)
                && ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType));
        if (!matches) {
            throw invalid("photo.contentType");
        }
    }

    private BufferedImage decode(byte[] bytes, String expectedFormat) {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw invalid("photo.decode");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!(format.equals(expectedFormat)
                        || expectedFormat.equals("jpeg") && format.equals("jpg"))) {
                    throw invalid("photo.format");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_SIDE || height > MAX_SIDE
                        || (long) width * height > MAX_PIXELS) {
                    throw invalid("photo.dimensions");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw invalid("photo.decode");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalid("photo.decode");
        }
    }

    private BufferedImage orient(BufferedImage source, int orientation) {
        if (orientation == 1) {
            return source;
        }
        int width = orientation >= 5 && orientation <= 8
                ? source.getHeight() : source.getWidth();
        int height = orientation >= 5 && orientation <= 8
                ? source.getWidth() : source.getHeight();
        BufferedImage target = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        AffineTransform transform = new AffineTransform();
        switch (orientation) {
            case 2 -> {
                transform.translate(width, 0);
                transform.scale(-1, 1);
            }
            case 3 -> {
                transform.translate(width, height);
                transform.scale(-1, -1);
            }
            case 4 -> {
                transform.translate(0, height);
                transform.scale(1, -1);
            }
            case 5 -> transform.setTransform(0, 1, 1, 0, 0, 0);
            case 6 -> {
                transform.translate(width, 0);
                transform.rotate(Math.PI / 2);
            }
            case 7 -> transform.setTransform(0, -1, -1, 0, width, height);
            case 8 -> {
                transform.translate(0, height);
                transform.rotate(-Math.PI / 2);
            }
            default -> {
                return source;
            }
        }
        graphics.drawImage(source, transform, null);
        graphics.dispose();
        return target;
    }

    private BufferedImage contain(BufferedImage source) {
        BufferedImage canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        double scale = Math.min((double) CANVAS_WIDTH / source.getWidth(),
                (double) CANVAS_HEIGHT / source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int x = (CANVAS_WIDTH - width) / 2;
        int y = (CANVAS_HEIGHT - height) / 2;
        graphics.drawImage(source, x, y, width, height, null);
        graphics.dispose();
        return canvas;
    }

    private byte[] encodePng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw invalid("photo.encode");
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw invalid("photo.encode");
        }
    }

    private int readExifOrientation(byte[] jpeg) {
        int offset = 2;
        while (offset + 4 <= jpeg.length) {
            if ((jpeg[offset] & 0xFF) != 0xFF) {
                break;
            }
            int marker = jpeg[offset + 1] & 0xFF;
            offset += 2;
            if (marker == 0xDA || marker == 0xD9) {
                break;
            }
            if (offset + 2 > jpeg.length) {
                break;
            }
            int length = ((jpeg[offset] & 0xFF) << 8) | (jpeg[offset + 1] & 0xFF);
            if (length < 2 || offset + length > jpeg.length) {
                break;
            }
            if (marker == 0xE1 && length >= 14
                    && matches(jpeg, offset + 2, new byte[]{'E', 'x', 'i', 'f', 0, 0})) {
                return readTiffOrientation(jpeg, offset + 8, length - 8);
            }
            offset += length;
        }
        return 1;
    }

    private int readTiffOrientation(byte[] bytes, int start, int length) {
        if (length < 8 || start < 0 || start + length > bytes.length) {
            return 1;
        }
        ByteOrder order;
        if (bytes[start] == 'I' && bytes[start + 1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (bytes[start] == 'M' && bytes[start + 1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return 1;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes, start, length).slice().order(order);
        int ifdOffset = buffer.getInt(4);
        if (ifdOffset < 0 || ifdOffset + 2 > length) {
            return 1;
        }
        int count = Short.toUnsignedInt(buffer.getShort(ifdOffset));
        for (int index = 0; index < count; index++) {
            int entry = ifdOffset + 2 + index * 12;
            if (entry + 12 > length) {
                return 1;
            }
            int tag = Short.toUnsignedInt(buffer.getShort(entry));
            if (tag == 0x0112) {
                int orientation = Short.toUnsignedInt(buffer.getShort(entry + 8));
                return orientation >= 1 && orientation <= 8 ? orientation : 1;
            }
        }
        return 1;
    }

    private boolean matches(byte[] source, int offset, byte[] expected) {
        if (offset < 0 || offset + expected.length > source.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (source[offset + index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private InvalidActivityReportDocumentException invalid(String field) {
        return new InvalidActivityReportDocumentException(field);
    }
}
