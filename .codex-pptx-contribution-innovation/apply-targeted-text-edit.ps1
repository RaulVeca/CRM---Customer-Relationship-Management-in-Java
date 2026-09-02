param(
    [Parameter(Mandatory = $true)][string]$SourcePptx,
    [Parameter(Mandatory = $true)][string]$OutputPptx
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$oldText = 'TrainingIT combines a course marketplace and CRM in one Java web application. Each customer action is stored once and drives event-based workflows; the Claude AI layer remains optional.'
$newText = $oldText + ' Major market innovations include real-time commerce-to-CRM automation, training-specific workflows from lead scoring to invoicing, and explainable recommendations for learners and corporate teams.'

$sourceResolved = (Resolve-Path -LiteralPath $SourcePptx).Path
$outputDirectory = Split-Path -Parent $OutputPptx
if (-not (Test-Path -LiteralPath $outputDirectory -PathType Container)) {
    throw "Output directory does not exist: $outputDirectory"
}

Copy-Item -LiteralPath $sourceResolved -Destination $OutputPptx -Force

$fileStream = [System.IO.File]::Open($OutputPptx, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
try {
    $archive = [System.IO.Compression.ZipArchive]::new($fileStream, [System.IO.Compression.ZipArchiveMode]::Update, $false)
    try {
        $entry = $archive.GetEntry('ppt/slides/slide4.xml')
        if ($null -eq $entry) {
            throw 'Missing PowerPoint package entry: ppt/slides/slide4.xml'
        }

        $entryStream = $entry.Open()
        try {
            $reader = [System.IO.StreamReader]::new($entryStream, [System.Text.UTF8Encoding]::new($false), $true, 4096, $true)
            try {
                $xml = $reader.ReadToEnd()
            }
            finally {
                $reader.Dispose()
            }

            $occurrences = ([regex]::Matches($xml, [regex]::Escape($oldText))).Count
            if ($occurrences -ne 1) {
                throw "Expected exactly one applied-contribution paragraph, found $occurrences."
            }

            $updatedXml = $xml.Replace($oldText, $newText)
            $entryStream.SetLength(0)
            $entryStream.Position = 0
            $writer = [System.IO.StreamWriter]::new($entryStream, [System.Text.UTF8Encoding]::new($false), 4096, $true)
            try {
                $writer.Write($updatedXml)
                $writer.Flush()
            }
            finally {
                $writer.Dispose()
            }
        }
        finally {
            $entryStream.Dispose()
        }
    }
    finally {
        $archive.Dispose()
    }
}
finally {
    $fileStream.Dispose()
}

Write-Output $OutputPptx
