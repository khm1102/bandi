package kr.ac.tukorea.bandi.domain.activity.document;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 한컴의 공개 HWPX(OWPML) 포맷 안내를 기준으로 정제된 템플릿의 ZIP/XML을 생성한다.
 * 외부 엔티티를 허용하지 않고 이름이 지정된 표 셀과 문단만 수정한다.
 *
 * @see <a href="https://tech.hancom.com/hwpxformat/">한컴 HWPX 포맷 안내</a>
 */
@Component
public class ActivityReportHwpxGenerator {

    private static final String TEMPLATE_PATH =
            "templates/hwpx/bandi-activity-report-template.hwpx";
    private static final String HP_NAMESPACE =
            "http://www.hancom.co.kr/hwpml/2011/paragraph";
    private static final String HC_NAMESPACE =
            "http://www.hancom.co.kr/hwpml/2011/core";
    private static final String SECTION_PATH = "Contents/section0.xml";
    private static final String PHOTO_PATH = "BinData/activity-photo.png";
    private static final String PREVIEW_TEXT_PATH = "Preview/PrvText.txt";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final Set<String> REQUIRED_ENTRIES = Set.of(
            "mimetype", "version.xml", "Contents/header.xml", SECTION_PATH,
            "Contents/content.hpf", "settings.xml", "META-INF/container.xml",
            "META-INF/manifest.xml", PHOTO_PATH, PREVIEW_TEXT_PATH);
    private static final List<String> DYNAMIC_NAMES = dynamicNames();

    private final Map<String, byte[]> templateEntries;

    public ActivityReportHwpxGenerator() {
        this.templateEntries = loadTemplate();
        validateTemplate(templateEntries);
    }

    public byte[] generateBlank(String presidentName) {
        validatePresidentName(presidentName);
        Document section = parseXml(templateEntries.get(SECTION_PATH));
        fillCommon(section, "월 [ 반디 ] 동아리 활동 내역서",
                "", "", "", presidentName, "00명");
        clearParticipants(section);
        return write(section, templateEntries.get(PHOTO_PATH),
                "반디 동아리 활동 내역서\n반디 회장 " + presidentName);
    }

    public byte[] generate(ActivityReportDocument document, String presidentName,
                           byte[] normalizedPhoto) {
        if (document == null || normalizedPhoto == null || normalizedPhoto.length == 0) {
            throw new InvalidActivityReportDocumentException("document");
        }
        validatePresidentName(presidentName);
        Document section = parseXml(templateEntries.get(SECTION_PATH));
        fillCommon(section,
                document.activityAt().getMonthValue() + "월 [ 반디 ] 동아리 활동 내역서",
                document.representative(), document.location(),
                document.activityAt().format(DATE_TIME_FORMATTER),
                presidentName, "%02d명".formatted(document.participants().size()));
        setNamedText(section, "tc", "ACTIVITY_CONTENT", document.content());
        fillParticipants(section, document.participants());
        String preview = "%s\n대표자 %s\n활동 장소 %s\n활동 일시 %s\n참여인원 총 %02d명\n%s"
                .formatted(document.activityAt().getMonthValue()
                                + "월 [ 반디 ] 동아리 활동 내역서",
                        document.representative(), document.location(),
                        document.activityAt().format(DATE_TIME_FORMATTER),
                        document.participants().size(), document.content());
        return write(section, normalizedPhoto, preview);
    }

    private void fillCommon(Document section, String title, String representative,
                            String location, String activityDate, String presidentName,
                            String participantTotal) {
        setNamedText(section, "p", "TITLE", title);
        setNamedText(section, "tc", "REPRESENTATIVE", representative);
        setNamedText(section, "tc", "LOCATION", location);
        setNamedText(section, "tc", "ACTIVITY_DATE", activityDate);
        setNamedText(section, "tc", "ACTIVITY_CONTENT", "");
        setNamedText(section, "p", "PRESIDENT_NAME",
                "반디 회장 " + presidentName + " (인)");
        setNamedText(section, "p", "PARTICIPANT_TOTAL",
                "참여인원 총 " + participantTotal);
    }

    private void fillParticipants(Document section,
                                  List<ActivityReportParticipant> participants) {
        clearParticipants(section);
        for (int index = 0; index < participants.size(); index++) {
            int row = index + 1;
            ActivityReportParticipant participant = participants.get(index);
            setNamedText(section, "tc", participantName(row, "NAME"), participant.name());
            setNamedText(section, "tc", participantName(row, "DEPARTMENT"),
                    optional(participant.department()));
            setNamedText(section, "tc", participantName(row, "STUDENT_NO"),
                    optional(participant.studentNo()));
            setNamedText(section, "tc", participantName(row, "NOTE"),
                    optional(participant.note()));
        }
    }

    private void clearParticipants(Document section) {
        for (int row = 1; row <= 14; row++) {
            setNamedText(section, "tc", participantName(row, "NAME"), "");
            setNamedText(section, "tc", participantName(row, "DEPARTMENT"), "");
            setNamedText(section, "tc", participantName(row, "STUDENT_NO"), "");
            setNamedText(section, "tc", participantName(row, "NOTE"), "");
        }
    }

    private byte[] write(Document section, byte[] photo, String previewText) {
        normalizePhotoOrientation(section);
        Map<String, byte[]> entries = new LinkedHashMap<>(templateEntries);
        entries.put(SECTION_PATH, serializeXml(section));
        entries.put(PHOTO_PATH, photo.clone());
        entries.put(PREVIEW_TEXT_PATH, previewText.getBytes(StandardCharsets.UTF_8));
        byte[] result = writeZip(entries);
        validateGenerated(result);
        return result;
    }

    private Map<String, byte[]> loadTemplate() {
        try {
            return readZip(new ClassPathResource(TEMPLATE_PATH).getInputStream().readAllBytes());
        } catch (IOException exception) {
            throw new IllegalStateException("활동 내역서 정본 템플릿을 읽을 수 없습니다.", exception);
        }
    }

    private Map<String, byte[]> readZip(byte[] bytes) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entries.put(entry.getName(), input.readAllBytes()) != null) {
                    throw new IllegalStateException("HWPX ZIP 항목이 중복되었습니다.");
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("HWPX ZIP을 읽을 수 없습니다.", exception);
        }
        return entries;
    }

    private byte[] writeZip(Map<String, byte[]> entries) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            byte[] mimetype = entries.get("mimetype");
            ZipEntry first = new ZipEntry("mimetype");
            first.setMethod(ZipEntry.STORED);
            first.setSize(mimetype.length);
            first.setCompressedSize(mimetype.length);
            CRC32 crc = new CRC32();
            crc.update(mimetype);
            first.setCrc(crc.getValue());
            zip.putNextEntry(first);
            zip.write(mimetype);
            zip.closeEntry();

            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                if (entry.getKey().equals("mimetype")) {
                    continue;
                }
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("HWPX ZIP을 만들 수 없습니다.", exception);
        }
    }

    private void validateTemplate(Map<String, byte[]> entries) {
        if (!entries.keySet().containsAll(REQUIRED_ENTRIES)
                || entries.containsKey("Preview/PrvImage.png")
                || entries.containsKey("BinData/image1.jpg")) {
            throw new IllegalStateException("활동 내역서 정본 템플릿 구성이 올바르지 않습니다.");
        }
        Document section = parseXml(entries.get(SECTION_PATH));
        for (String name : DYNAMIC_NAMES) {
            boolean participantCell = name.length() > 2 && name.charAt(0) == 'P'
                    && Character.isDigit(name.charAt(1));
            String tag = participantCell || Set.of("REPRESENTATIVE", "LOCATION",
                    "ACTIVITY_DATE", "ACTIVITY_CONTENT").contains(name)
                    ? "tc" : "p";
            findNamedElement(section, tag, name);
        }
        String packageText = new String(entries.get("Contents/content.hpf"),
                StandardCharsets.UTF_8);
        if (!packageText.contains(PHOTO_PATH)) {
            throw new IllegalStateException("활동 사진 manifest 참조가 없습니다.");
        }
    }

    private void validateGenerated(byte[] hwpx) {
        Map<String, byte[]> entries = readZip(hwpx);
        if (!entries.keySet().containsAll(REQUIRED_ENTRIES)) {
            throw new IllegalStateException("생성 HWPX의 필수 항목이 없습니다.");
        }
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().endsWith(".xml") || entry.getKey().endsWith(".hpf")) {
                parseXml(entry.getValue());
            }
        }
        String section = new String(entries.get(SECTION_PATH), StandardCharsets.UTF_8);
        for (String marker : DYNAMIC_NAMES) {
            if (section.contains("[" + marker + "]")) {
                throw new IllegalStateException("치환되지 않은 HWPX 표식이 있습니다.");
            }
        }
        validatePhotoOrientation(parseXml(entries.get(SECTION_PATH)));
    }

    private void normalizePhotoOrientation(Document document) {
        Element picture = findPhotoPicture(document);
        picture.setAttribute("reverse", "0");

        Element flip = findSingleDescendant(picture, HP_NAMESPACE, "flip");
        flip.setAttribute("horizontal", "0");
        flip.setAttribute("vertical", "0");

        Element rotation = findSingleDescendant(picture, HP_NAMESPACE,
                "rotationInfo");
        rotation.setAttribute("angle", "0");
        rotation.setAttribute("rotateimage", "0");

        Element rendering = findSingleDescendant(picture, HP_NAMESPACE,
                "renderingInfo");
        setIdentityMatrix(findSingleDescendant(rendering, HC_NAMESPACE,
                "transMatrix"));
        Element scale = findSingleDescendant(rendering, HC_NAMESPACE, "scaMatrix");
        scale.setAttribute("e2", "0");
        scale.setAttribute("e3", "0");
        scale.setAttribute("e4", "0");
        scale.setAttribute("e6", "0");
        setIdentityMatrix(findSingleDescendant(rendering, HC_NAMESPACE,
                "rotMatrix"));
    }

    private void validatePhotoOrientation(Document document) {
        Element picture = findPhotoPicture(document);
        Element flip = findSingleDescendant(picture, HP_NAMESPACE, "flip");
        Element rotation = findSingleDescendant(picture, HP_NAMESPACE,
                "rotationInfo");
        Element rendering = findSingleDescendant(picture, HP_NAMESPACE,
                "renderingInfo");
        Element translation = findSingleDescendant(rendering, HC_NAMESPACE,
                "transMatrix");
        Element scale = findSingleDescendant(rendering, HC_NAMESPACE, "scaMatrix");
        Element rotationMatrix = findSingleDescendant(rendering, HC_NAMESPACE,
                "rotMatrix");

        if (!"0".equals(flip.getAttribute("horizontal"))
                || !"0".equals(flip.getAttribute("vertical"))
                || !"0".equals(rotation.getAttribute("angle"))
                || !"0".equals(rotation.getAttribute("rotateimage"))
                || !isIdentityMatrix(translation)
                || !"0".equals(scale.getAttribute("e2"))
                || !"0".equals(scale.getAttribute("e3"))
                || !"0".equals(scale.getAttribute("e4"))
                || !"0".equals(scale.getAttribute("e6"))
                || !isIdentityMatrix(rotationMatrix)) {
            throw new IllegalStateException("생성 HWPX의 활동 사진 방향이 올바르지 않습니다.");
        }
    }

    private Element findPhotoPicture(Document document) {
        NodeList pictures = document.getElementsByTagNameNS(HP_NAMESPACE, "pic");
        Element found = null;
        for (int index = 0; index < pictures.getLength(); index++) {
            Element picture = (Element) pictures.item(index);
            NodeList images = picture.getElementsByTagNameNS(HC_NAMESPACE, "img");
            if (images.getLength() == 1
                    && "image1".equals(((Element) images.item(0))
                    .getAttribute("binaryItemIDRef"))) {
                if (found != null) {
                    throw new IllegalStateException("활동 사진 개체가 중복되었습니다.");
                }
                found = picture;
            }
        }
        if (found == null) {
            throw new IllegalStateException("활동 사진 개체를 찾을 수 없습니다.");
        }
        return found;
    }

    private Element findSingleDescendant(Element parent, String namespace,
                                         String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
        if (nodes.getLength() != 1) {
            throw new IllegalStateException("HWPX 사진 속성이 올바르지 않습니다: "
                    + localName);
        }
        return (Element) nodes.item(0);
    }

    private void setIdentityMatrix(Element matrix) {
        matrix.setAttribute("e1", "1");
        matrix.setAttribute("e2", "0");
        matrix.setAttribute("e3", "0");
        matrix.setAttribute("e4", "0");
        matrix.setAttribute("e5", "1");
        matrix.setAttribute("e6", "0");
    }

    private boolean isIdentityMatrix(Element matrix) {
        return "1".equals(matrix.getAttribute("e1"))
                && "0".equals(matrix.getAttribute("e2"))
                && "0".equals(matrix.getAttribute("e3"))
                && "0".equals(matrix.getAttribute("e4"))
                && "1".equals(matrix.getAttribute("e5"))
                && "0".equals(matrix.getAttribute("e6"));
    }

    private Document parseXml(byte[] bytes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new ByteArrayInputStream(bytes)));
        } catch (Exception exception) {
            throw new IllegalStateException("HWPX XML을 읽을 수 없습니다.", exception);
        }
    }

    private byte[] serializeXml(Document document) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("HWPX XML을 만들 수 없습니다.", exception);
        }
    }

    private void setNamedText(Document document, String tag, String name, String value) {
        Element target = findNamedElement(document, tag, name);
        NodeList runs = target.getElementsByTagNameNS(HP_NAMESPACE, "run");
        if (runs.getLength() == 0) {
            throw new IllegalStateException("HWPX 텍스트 run이 없습니다: " + name);
        }
        Element firstRun = (Element) runs.item(0);
        List<Node> removable = new ArrayList<>();
        for (int runIndex = 0; runIndex < runs.getLength(); runIndex++) {
            NodeList children = runs.item(runIndex).getChildNodes();
            for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                Node child = children.item(childIndex);
                if (HP_NAMESPACE.equals(child.getNamespaceURI())
                        && ("t".equals(child.getLocalName())
                        || "lineBreak".equals(child.getLocalName()))) {
                    removable.add(child);
                }
            }
        }
        removable.forEach(node -> node.getParentNode().removeChild(node));

        String[] lines = optional(value).split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            if (index > 0) {
                firstRun.appendChild(document.createElementNS(HP_NAMESPACE, "hp:lineBreak"));
            }
            Element text = document.createElementNS(HP_NAMESPACE, "hp:t");
            text.setTextContent(lines[index]);
            firstRun.appendChild(text);
        }
    }

    private Element findNamedElement(Document document, String tag, String name) {
        NodeList nodes = document.getElementsByTagNameNS(HP_NAMESPACE, tag);
        Element found = null;
        for (int index = 0; index < nodes.getLength(); index++) {
            Element element = (Element) nodes.item(index);
            if (name.equals(element.getAttribute("name"))) {
                if (found != null) {
                    throw new IllegalStateException("HWPX 이름이 중복되었습니다: " + name);
                }
                found = element;
            }
        }
        if (found == null) {
            throw new IllegalStateException("HWPX 이름을 찾을 수 없습니다: " + name);
        }
        return found;
    }

    private void validatePresidentName(String presidentName) {
        if (presidentName == null || presidentName.isBlank() || presidentName.length() > 20) {
            throw new InvalidActivityReportDocumentException("presidentName");
        }
    }

    private static List<String> dynamicNames() {
        List<String> names = new ArrayList<>(List.of("TITLE", "REPRESENTATIVE",
                "LOCATION", "ACTIVITY_DATE", "ACTIVITY_CONTENT",
                "PRESIDENT_NAME", "PARTICIPANT_TOTAL"));
        for (int row = 1; row <= 14; row++) {
            names.add(participantName(row, "NAME"));
            names.add(participantName(row, "DEPARTMENT"));
            names.add(participantName(row, "STUDENT_NO"));
            names.add(participantName(row, "NOTE"));
        }
        return List.copyOf(names);
    }

    private static String participantName(int row, String field) {
        return "P%02d_%s".formatted(row, field);
    }

    private static String optional(String value) {
        return value == null ? "" : value;
    }
}
