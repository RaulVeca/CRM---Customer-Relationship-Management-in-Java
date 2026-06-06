#!/bin/bash
echo "Verificare sintaxă Java..."
find src/main/java -name "*.java" | while read file; do
    javac -source 8 -target 8 -Xlint:unchecked -d /tmp/crm-compile "$file" 2>&1 | grep -i "error" && echo "EROARE în: $file"
done
echo "Verificare completă."
