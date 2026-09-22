#!/usr/bin/env python3
"""Generate the Ecore corpus used to expose issue #19's quadratic scan."""

from pathlib import Path


CLASS_COUNT = 4096
OUTPUT = Path(__file__).with_name("large_same_document_references.ecore")


def main() -> None:
    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" '
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
        'xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore" '
        'name="issue19" nsURI="https://eclipse.org/sirius/emfjson/issue19" nsPrefix="issue19">',
    ]
    classifiers = [f'  <eClassifiers xsi:type="ecore:EClass" name="Base{index:04d}"/>' for index in range(CLASS_COUNT)]
    lines.extend("".join(classifiers[offset : offset + 16]) for offset in range(0, CLASS_COUNT, 16))
    supertypes = " ".join(f"#//Base{index:04d}" for index in range(CLASS_COUNT))
    lines.append(f'  <eClassifiers xsi:type="ecore:EClass" name="Aggregate" eSuperTypes="{supertypes}"/>')
    lines.append("</ecore:EPackage>")
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
