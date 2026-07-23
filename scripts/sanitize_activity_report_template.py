#!/usr/bin/env python3
"""Create the commit-safe HWPX runtime template from the local reference form."""

from __future__ import annotations

import argparse
import binascii
import hashlib
import io
import struct
import xml.etree.ElementTree as ET
import zipfile
import zlib
from pathlib import Path


HP = "http://www.hancom.co.kr/hwpml/2011/paragraph"
OPF = "http://www.idpf.org/2007/opf/"
GENERIC_METADATA_VALUES = {"", "text", "bandi"}
def register_namespaces(xml_bytes: bytes) -> None:
    for _, (prefix, uri) in ET.iterparse(io.BytesIO(xml_bytes), events=("start-ns",)):
        ET.register_namespace(prefix, uri)


def element_text(element: ET.Element) -> str:
    return "".join(node.text or "" for node in element.findall(f".//{{{HP}}}t"))


def set_marker(element: ET.Element, marker: str) -> None:
    text_nodes = element.findall(f".//{{{HP}}}t")
    if not text_nodes:
        run = element.find(f".//{{{HP}}}run")
        if run is None:
            raise ValueError(f"text run missing for {marker}")
        text_node = ET.SubElement(run, f"{{{HP}}}t")
        text_nodes = [text_node]
    text_nodes[0].text = marker
    for node in text_nodes[1:]:
        node.text = ""


def set_named_cell(cell: ET.Element, name: str, marker: str) -> None:
    cell.set("name", name)
    if marker == "" and not cell.findall(f".//{{{HP}}}t"):
        return
    set_marker(cell, marker)


def white_png(width: int = 4, height: int = 3) -> bytes:
    signature = b"\x89PNG\r\n\x1a\n"

    def chunk(kind: bytes, data: bytes) -> bytes:
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(
            ">I", binascii.crc32(body) & 0xFFFFFFFF
        )

    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    rows = b"".join(b"\x00" + b"\xff\xff\xff" * width for _ in range(height))
    return signature + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(rows)) + chunk(b"IEND", b"")


def sanitize_section(xml_bytes: bytes) -> tuple[bytes, set[str]]:
    register_namespaces(xml_bytes)
    root = ET.fromstring(xml_bytes)
    tables = root.findall(f".//{{{HP}}}tbl")
    if len(tables) != 2:
        raise ValueError("reference form must contain exactly two tables")

    info_cells = tables[0].findall(f".//{{{HP}}}tc")
    participant_cells = tables[1].findall(f".//{{{HP}}}tc")
    if len(info_cells) != 15 or len(participant_cells) != 76:
        raise ValueError("reference table shape changed")

    private_values = {
        element_text(info_cells[index]).strip()
        for index in (1, 5, 7, 14)
        if element_text(info_cells[index]).strip()
    }
    for row in range(1, 15):
        for column_index in (0, 1, 2, 4):
            value = element_text(participant_cells[row * 5 + column_index]).strip()
            if value:
                private_values.add(value)

    set_named_cell(info_cells[1], "REPRESENTATIVE", "[REPRESENTATIVE]")
    set_named_cell(info_cells[5], "LOCATION", "[LOCATION]")
    set_named_cell(info_cells[7], "ACTIVITY_DATE", "[ACTIVITY_DATE]")
    set_named_cell(info_cells[14], "ACTIVITY_CONTENT", "[ACTIVITY_CONTENT]")

    paragraphs = root.findall(f".//{{{HP}}}p")
    title = next((p for p in paragraphs if element_text(p).endswith("동아리 활동 내역서")), None)
    footer = next((p for p in paragraphs if element_text(p).startswith("반디 회장 ")), None)
    total = next((p for p in paragraphs if element_text(p).startswith("참여인원 총 ")), None)
    if title is None or footer is None or total is None:
        raise ValueError("reference dynamic paragraph missing")
    private_values.update(filter(None, (
        element_text(title).strip(),
        element_text(footer).strip(),
        element_text(total).strip(),
    )))
    title.set("name", "TITLE")
    footer.set("name", "PRESIDENT_NAME")
    total.set("name", "PARTICIPANT_TOTAL")
    set_marker(title, "[TITLE]")
    set_marker(footer, "반디 회장 [PRESIDENT_NAME] (인)")
    set_marker(total, "참여인원 총 [PARTICIPANT_TOTAL]")

    columns = ("NAME", "DEPARTMENT", "STUDENT_NO", "SIGNATURE", "NOTE")
    for row in range(1, 15):
        for column_index, column_name in enumerate(columns):
            cell = participant_cells[row * 5 + column_index]
            marker = "" if column_name == "SIGNATURE" else f"[P{row:02d}_{column_name}]"
            set_named_cell(cell, f"P{row:02d}_{column_name}", marker)

    for element in root.iter():
        if element.tag.endswith("shapeComment"):
            element.text = "활동 사진"

    return ET.tostring(root, encoding="utf-8", xml_declaration=True), private_values


def sanitize_package(xml_bytes: bytes) -> tuple[bytes, set[str]]:
    register_namespaces(xml_bytes)
    root = ET.fromstring(xml_bytes)
    metadata = root.find(f"{{{OPF}}}metadata")
    manifest = root.find(f"{{{OPF}}}manifest")
    if metadata is None or manifest is None:
        raise ValueError("content.hpf package structure missing")

    title = metadata.find(f"{{{OPF}}}title")
    if title is not None:
        title.text = "반디 동아리 활동 내역서"
    private_values: set[str] = set()
    for meta in metadata.findall(f"{{{OPF}}}meta"):
        name = meta.get("name")
        if name in {"creator", "lastsaveby"}:
            private_values.update(value for value in (
                (meta.get("content") or "").strip(),
                (meta.text or "").strip(),
            ) if value not in GENERIC_METADATA_VALUES)
            meta.set("content", "bandi")
            meta.text = None
        elif name in {"CreatedDate", "ModifiedDate", "date"}:
            meta.set("content", "")
            meta.text = None

    image = next((item for item in manifest.findall(f"{{{OPF}}}item")
                  if item.get("id") == "image1"), None)
    if image is None:
        raise ValueError("activity image manifest item missing")
    image.set("href", "BinData/activity-photo.png")
    image.set("media-type", "image/png")
    return ET.tostring(root, encoding="utf-8", xml_declaration=True), private_values


def build(source: Path, output: Path) -> None:
    with zipfile.ZipFile(source) as archive:
        entries = {info.filename: archive.read(info.filename) for info in archive.infolist()}
        infos = {info.filename: info for info in archive.infolist()}

    section, section_private_values = sanitize_section(entries["Contents/section0.xml"])
    package, package_private_values = sanitize_package(entries["Contents/content.hpf"])
    source_photo = entries.get("BinData/image1.jpg")
    entries["Contents/section0.xml"] = section
    entries["Contents/content.hpf"] = package
    entries["Preview/PrvText.txt"] = "반디 동아리 활동 내역서".encode("utf-8")
    entries["BinData/activity-photo.png"] = white_png()

    excluded = {"BinData/image1.jpg", "Preview/PrvImage.png"}
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w") as archive:
        mimetype = zipfile.ZipInfo("mimetype")
        mimetype.compress_type = zipfile.ZIP_STORED
        archive.writestr(mimetype, b"application/hwp+zip")
        for name, data in entries.items():
            if name == "mimetype" or name in excluded or name == "BinData/activity-photo.png":
                continue
            info = infos[name]
            copied = zipfile.ZipInfo(name, info.date_time)
            copied.external_attr = info.external_attr
            copied.compress_type = info.compress_type
            archive.writestr(copied, data)
        image = zipfile.ZipInfo("BinData/activity-photo.png")
        image.compress_type = zipfile.ZIP_DEFLATED
        archive.writestr(image, entries["BinData/activity-photo.png"])

    with zipfile.ZipFile(output) as archive:
        names = archive.namelist()
        if names[0] != "mimetype" or archive.getinfo("mimetype").compress_type != zipfile.ZIP_STORED:
            raise ValueError("mimetype must be the first stored entry")
        combined = b"\n".join(archive.read(name) for name in names
                              if name.endswith((".xml", ".hpf", ".txt")))
        for private_value in section_private_values | package_private_values:
            if private_value.encode("utf-8") in combined:
                raise ValueError("private example remains in output")
        if "Preview/PrvImage.png" in names or "BinData/image1.jpg" in names:
            raise ValueError("private preview or source photo remains")
        if source_photo is not None:
            source_hash = hashlib.sha256(source_photo).digest()
            for name in names:
                if name.startswith("BinData/") and hashlib.sha256(
                        archive.read(name)).digest() == source_hash:
                    raise ValueError("source photo binary remains")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    build(args.source, args.output)


if __name__ == "__main__":
    main()
