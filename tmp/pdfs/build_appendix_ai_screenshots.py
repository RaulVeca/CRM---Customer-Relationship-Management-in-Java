from __future__ import annotations

import hashlib
import io
from pathlib import Path

from PIL import Image
from pypdf import PdfReader, PdfWriter
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.pdfgen import canvas


SOURCE_PDF = Path(
    r"C:\Users\raulv\OneDrive\Desktop\documente_licenta\TrainingIT_modificat.pdf"
)
SCREENSHOTS = [
    Path(r"C:\Users\raulv\Downloads\prompt1.png"),
    Path(r"C:\Users\raulv\Downloads\prompt2.png"),
    Path(r"C:\Users\raulv\Downloads\prompt3.png"),
]
OUTPUT_DIR = Path(r"D:\TrainingIT_site\output\pdf")
OUTPUT_PDF = OUTPUT_DIR / "TrainingIT_modificat_cu_screenshoturi_Appendix_A.pdf"
APPENDIX_PAGES_PDF = Path(
    r"D:\TrainingIT_site\tmp\pdfs\appendix_ai_screenshot_pages.pdf"
)


def draw_centered_text(
    pdf_canvas: canvas.Canvas,
    text: str,
    y: float,
    *,
    font_name: str = "Times-Roman",
    font_size: float = 10.5,
) -> None:
    pdf_canvas.setFont(font_name, font_size)
    x = (pdf_canvas._pagesize[0] - stringWidth(text, font_name, font_size)) / 2
    pdf_canvas.drawString(x, y, text)


def draw_screenshot(
    pdf_canvas: canvas.Canvas,
    image_path: Path,
    *,
    x: float,
    top_y: float,
    width: float,
) -> float:
    with Image.open(image_path) as image:
        image_width, image_height = image.size
    height = width * image_height / image_width
    bottom_y = top_y - height
    pdf_canvas.drawImage(
        ImageReader(str(image_path)),
        x,
        bottom_y,
        width=width,
        height=height,
        preserveAspectRatio=True,
        mask="auto",
    )
    return bottom_y


def create_appendix_pages(page_width: float, page_height: float) -> None:
    pdf_canvas = canvas.Canvas(
        str(APPENDIX_PAGES_PDF),
        pagesize=(page_width, page_height),
        pageCompression=1,
    )
    content_x = 90.0
    content_width = page_width - 2 * content_x

    pdf_canvas.setTitle("Appendix A - Generative AI Tools Usage")
    pdf_canvas.setAuthor("")
    pdf_canvas.setFont("Times-Bold", 18)
    pdf_canvas.drawString(content_x, page_height - 82, "A.2   Claude Usage Screenshots")

    first_bottom = draw_screenshot(
        pdf_canvas,
        SCREENSHOTS[0],
        x=content_x,
        top_y=page_height - 122,
        width=content_width,
    )
    draw_centered_text(
        pdf_canvas,
        "Figure A.1: Targeted code-cleanup prompt and constraints",
        first_bottom - 17,
    )

    second_top = first_bottom - 42
    second_bottom = draw_screenshot(
        pdf_canvas,
        SCREENSHOTS[1],
        x=content_x,
        top_y=second_top,
        width=content_width,
    )
    draw_centered_text(
        pdf_canvas,
        "Figure A.2: Focused review request and Claude findings",
        second_bottom - 17,
    )
    draw_centered_text(pdf_canvas, "36", 28, font_size=10)
    pdf_canvas.showPage()

    third_width = page_width - 120
    third_bottom = draw_screenshot(
        pdf_canvas,
        SCREENSHOTS[2],
        x=(page_width - third_width) / 2,
        top_y=page_height - 92,
        width=third_width,
    )
    draw_centered_text(
        pdf_canvas,
        "Figure A.3: Diff review request and command-approval step",
        third_bottom - 19,
    )
    draw_centered_text(pdf_canvas, "37", 28, font_size=10)
    pdf_canvas.showPage()
    pdf_canvas.save()


def content_stream_hash(page) -> str:
    contents = page.get_contents()
    if contents is None:
        data = b""
    else:
        data = contents.get_data()
    return hashlib.sha256(data).hexdigest()


def build_final_pdf() -> None:
    if not SOURCE_PDF.exists():
        raise FileNotFoundError(SOURCE_PDF)
    for screenshot in SCREENSHOTS:
        if not screenshot.exists():
            raise FileNotFoundError(screenshot)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    APPENDIX_PAGES_PDF.parent.mkdir(parents=True, exist_ok=True)

    source_reader = PdfReader(str(SOURCE_PDF))
    last_page = source_reader.pages[-1]
    page_width = float(last_page.mediabox.width)
    page_height = float(last_page.mediabox.height)
    create_appendix_pages(page_width, page_height)

    appendix_reader = PdfReader(str(APPENDIX_PAGES_PDF))
    writer = PdfWriter()
    writer.clone_document_from_reader(source_reader)
    for page in appendix_reader.pages:
        writer.add_page(page)

    with OUTPUT_PDF.open("wb") as output_stream:
        writer.write(output_stream)

    final_reader = PdfReader(str(OUTPUT_PDF))
    if len(final_reader.pages) != len(source_reader.pages) + 2:
        raise RuntimeError("Unexpected final page count")

    mismatches = []
    for index, original_page in enumerate(source_reader.pages):
        final_page = final_reader.pages[index]
        if content_stream_hash(original_page) != content_stream_hash(final_page):
            mismatches.append(index + 1)
        if tuple(map(float, original_page.mediabox)) != tuple(
            map(float, final_page.mediabox)
        ):
            mismatches.append(index + 1)
    if mismatches:
        raise RuntimeError(
            f"Existing page content or geometry changed: {sorted(set(mismatches))}"
        )

    print(f"Created: {OUTPUT_PDF}")
    print(f"Pages: {len(source_reader.pages)} -> {len(final_reader.pages)}")
    print("Verified: all 45 existing page content streams and page boxes are unchanged")
    print(f"SHA256: {hashlib.sha256(OUTPUT_PDF.read_bytes()).hexdigest()}")


if __name__ == "__main__":
    build_final_pdf()
