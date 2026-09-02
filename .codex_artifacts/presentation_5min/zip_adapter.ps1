# Adapter for the Unix-style unzip calls used by the presentation inspector.
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ArgsList
)

Add-Type -AssemblyName System.IO.Compression.FileSystem

if ($ArgsList.Count -lt 2) {
    [Console]::Error.WriteLine("Unsupported unzip invocation")
    exit 2
}

$mode = $ArgsList[0]
$archivePath = $ArgsList[1]
$archive = [System.IO.Compression.ZipFile]::OpenRead($archivePath)

try {
    if ($mode -eq '-Z1') {
        foreach ($entry in $archive.Entries) {
            [Console]::Out.WriteLine($entry.FullName)
        }
        exit 0
    }

    if ($mode -eq '-p' -and $ArgsList.Count -ge 3) {
        $entryName = $ArgsList[2]
        $entry = $archive.GetEntry($entryName)
        if ($null -eq $entry) {
            [Console]::Error.WriteLine("Entry not found: $entryName")
            exit 11
        }
        $inputStream = $entry.Open()
        try {
            $outputStream = [Console]::OpenStandardOutput()
            $inputStream.CopyTo($outputStream)
            $outputStream.Flush()
        }
        finally {
            $inputStream.Dispose()
        }
        exit 0
    }

    [Console]::Error.WriteLine("Unsupported unzip invocation: $($ArgsList -join ' ')")
    exit 2
}
finally {
    $archive.Dispose()
}
