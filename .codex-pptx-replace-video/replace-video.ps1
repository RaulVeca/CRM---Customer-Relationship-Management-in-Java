param(
    [Parameter(Mandatory = $true)][string]$SourcePptx,
    [Parameter(Mandatory = $true)][string]$ReplacementVideo,
    [Parameter(Mandatory = $true)][string]$ReplacementPoster,
    [Parameter(Mandatory = $true)][string]$OutputPptx
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$sourceResolved = (Resolve-Path -LiteralPath $SourcePptx).Path
$videoResolved = (Resolve-Path -LiteralPath $ReplacementVideo).Path
$posterResolved = (Resolve-Path -LiteralPath $ReplacementPoster).Path
$outputDirectory = Split-Path -Parent $OutputPptx
if (-not (Test-Path -LiteralPath $outputDirectory -PathType Container)) {
    throw "Output directory does not exist: $outputDirectory"
}

Copy-Item -LiteralPath $sourceResolved -Destination $OutputPptx -Force

function Replace-ZipEntryBytes {
    param(
        [System.IO.Compression.ZipArchive]$Archive,
        [string]$EntryName,
        [string]$ReplacementPath
    )

    $entry = $Archive.GetEntry($EntryName)
    if ($null -eq $entry) {
        throw "Missing PowerPoint package entry: $EntryName"
    }

    $replacement = [System.IO.File]::OpenRead($ReplacementPath)
    try {
        $entryStream = $entry.Open()
        try {
            $entryStream.SetLength(0)
            $replacement.CopyTo($entryStream)
        }
        finally {
            $entryStream.Dispose()
        }
    }
    finally {
        $replacement.Dispose()
    }
}

$fileStream = [System.IO.File]::Open($OutputPptx, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
try {
    $archive = [System.IO.Compression.ZipArchive]::new($fileStream, [System.IO.Compression.ZipArchiveMode]::Update, $false)
    try {
        Replace-ZipEntryBytes -Archive $archive -EntryName 'ppt/media/media1.mp4' -ReplacementPath $videoResolved
        Replace-ZipEntryBytes -Archive $archive -EntryName 'ppt/media/image5.png' -ReplacementPath $posterResolved
    }
    finally {
        $archive.Dispose()
    }
}
finally {
    $fileStream.Dispose()
}

Write-Output $OutputPptx
