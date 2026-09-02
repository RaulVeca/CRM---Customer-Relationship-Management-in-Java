from pathlib import Path
import sys

import pdfplumber


def main() -> None:
    source = Path(sys.argv[1])
    destination = Path(sys.argv[2])
    chunks: list[str] = []
    with pdfplumber.open(source) as pdf:
        for page_number, page in enumerate(pdf.pages, start=1):
            text = page.extract_text(x_tolerance=2, y_tolerance=3) or ""
            chunks.append(f"\n===== PDF PAGE {page_number} =====\n{text}\n")
    destination.write_text("".join(chunks), encoding="utf-8")


if __name__ == "__main__":
    main()
