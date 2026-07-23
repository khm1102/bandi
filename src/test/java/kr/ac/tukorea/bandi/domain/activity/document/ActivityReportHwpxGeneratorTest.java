package kr.ac.tukorea.bandi.domain.activity.document;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipant;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityReportHwpxGeneratorTest {

    private final ActivityReportPhotoProcessor photoProcessor =
            new ActivityReportPhotoProcessor();
    private final ActivityReportHwpxGenerator generator =
            new ActivityReportHwpxGenerator();

    @Test
    void 빈_양식은_고정값과_회장만_남기고_개인정보와_미리보기를_제거한다() throws Exception {
        byte[] hwpx = generator.generateBlank("현재회장");
        HwpxEntries entries = readEntries(hwpx);
        String section = entries.text("Contents/section0.xml");

        assertThat(entries.firstName()).isEqualTo("mimetype");
        assertThat(entries.firstMethod()).isEqualTo(ZipEntry.STORED);
        assertThat(entries.contains("Preview/PrvImage.png")).isFalse();
        assertThat(entries.contains("BinData/image1.jpg")).isFalse();
        assertThat(section).contains("반디 회장 현재회장 (인)", "참여인원 총 00명",
                "월 [ 반디 ] 동아리 활동 내역서", "제89조 1항 회의");
        assertThat(section).doesNotContain("원동연", "[P01_NAME]", "[REPRESENTATIVE]");
        assertThat(entries.text("Contents/content.hpf"))
                .contains("name=\"creator\" content=\"bandi\" />",
                        "name=\"lastsaveby\" content=\"bandi\" />",
                        "name=\"CreatedDate\" content=\"\" />");
        assertAllXmlWellFormed(entries);
    }

    @Test
    void 완성본은_입력값과_열네_명_총계를_정확히_반영한다() throws Exception {
        List<ActivityReportParticipant> participants = java.util.stream.IntStream
                .rangeClosed(1, 14)
                .mapToObj(index -> new ActivityReportParticipant("참여자" + index,
                        "학과" + index, "202600" + index, index == 14 ? "외부인" : ""))
                .toList();
        ActivityReportDocument document = ActivityReportDocument.create(
                "대표<&>", "장소 & 강당", LocalDateTime.of(2026, 2, 11, 16, 30),
                "첫 줄\n둘째 줄 <확인>", participants);
        byte[] photo = photoProcessor.normalize(new ActivityReportPhotoParam(
                png(300, 600), "image/png"));

        HwpxEntries entries = readEntries(generator.generate(document,
                "현재회장", photo));
        String section = entries.text("Contents/section0.xml");

        assertThat(section).contains("2월 [ 반디 ] 동아리 활동 내역서",
                "대표&lt;&amp;&gt;", "장소 &amp; 강당", "2026.02.11 16:30",
                "참여자14", "외부인", "참여인원 총 14명", "현재회장");
        assertThat(section).contains("lineBreak");
        assertThat(section).doesNotContain("[TITLE]", "[ACTIVITY_CONTENT]");
        assertThat(entries.bytes("BinData/activity-photo.png")).isEqualTo(photo);
        assertAllXmlWellFormed(entries);
    }

    @Test
    void 한_명_완성본의_총계는_두_자리로_표시한다() throws Exception {
        ActivityReportDocument document = ActivityReportDocument.create(
                "대표", "장소", LocalDateTime.of(2026, 11, 3, 9, 5), "내용",
                List.of(new ActivityReportParticipant("참여자", null, null, null)));

        String section = readEntries(generator.generate(document, "회장",
                photoProcessor.normalize(new ActivityReportPhotoParam(
                        png(40, 30), "image/png"))))
                .text("Contents/section0.xml");

        assertThat(section).contains("11월 [ 반디 ] 동아리 활동 내역서",
                "참여인원 총 01명", "2026.11.03 09:05");
    }

    @Test
    void 완성본_사진_개체는_회전과_반전_없이_생성한다() throws Exception {
        ActivityReportDocument document = ActivityReportDocument.create(
                "대표", "장소", LocalDateTime.of(2026, 7, 24, 0, 5), "내용",
                List.of(new ActivityReportParticipant("참여자", null, null, null)));

        String section = readEntries(generator.generate(document, "회장",
                photoProcessor.normalize(new ActivityReportPhotoParam(
                        png(40, 30), "image/png"))))
                .text("Contents/section0.xml");

        assertPhotoOrientationIsNeutral(section);
    }

    @Test
    void 빈_양식_사진_개체도_회전과_반전_없이_생성한다() throws Exception {
        String section = readEntries(generator.generateBlank("회장"))
                .text("Contents/section0.xml");

        assertPhotoOrientationIsNeutral(section);
    }

    private void assertPhotoOrientationIsNeutral(String section) {
        assertThat(section).contains(
                "<hp:flip horizontal=\"0\" vertical=\"0\"",
                "<hp:rotationInfo angle=\"0\"",
                "rotateimage=\"0\"",
                "<hc:rotMatrix e1=\"1\" e2=\"0\" e3=\"0\" e4=\"0\" e5=\"1\" e6=\"0\"",
                "<hc:scaMatrix e1=\"6.945305\" e2=\"0\" e3=\"0\" e4=\"0\" e5=\"6.945398\" e6=\"0\"");
    }

    private byte[] png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private HwpxEntries readEntries(byte[] hwpx) throws Exception {
        java.util.LinkedHashMap<String, byte[]> entries = new java.util.LinkedHashMap<>();
        String firstName = null;
        int firstMethod = -1;
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(hwpx))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (firstName == null) {
                    firstName = entry.getName();
                    firstMethod = entry.getMethod();
                }
                entries.put(entry.getName(), input.readAllBytes());
            }
        }
        return new HwpxEntries(firstName, firstMethod, entries);
    }

    private void assertAllXmlWellFormed(HwpxEntries entries) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        for (java.util.Map.Entry<String, byte[]> entry : entries.values().entrySet()) {
            if (entry.getKey().endsWith(".xml") || entry.getKey().endsWith(".hpf")) {
                factory.newDocumentBuilder().parse(new ByteArrayInputStream(entry.getValue()));
            }
        }
    }

    private record HwpxEntries(String firstName, int firstMethod,
                               java.util.Map<String, byte[]> values) {

        boolean contains(String name) {
            return values.containsKey(name);
        }

        byte[] bytes(String name) {
            return values.get(name);
        }

        String text(String name) {
            return new String(bytes(name), StandardCharsets.UTF_8);
        }
    }
}
