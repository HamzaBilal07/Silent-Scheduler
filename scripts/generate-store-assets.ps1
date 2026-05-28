$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$AssetsDir = Join-Path $ProjectRoot "store-assets"
$ScreenshotsDir = Join-Path $AssetsDir "screenshots"
New-Item -ItemType Directory -Force -Path $AssetsDir | Out-Null
New-Item -ItemType Directory -Force -Path $ScreenshotsDir | Out-Null

function New-RoundedRectPath([float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    if ($r -le 0) {
        $path.AddRectangle((New-Object System.Drawing.RectangleF($x, $y, $w, $h)))
        return $path
    }
    $d = $r * 2
    $path.AddArc($x, $y, $d, $d, 180, 90)
    $path.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $path.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $path.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    return $path
}

function Save-Png($bitmap, [string]$path) {
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function New-Canvas([int]$width, [int]$height, [scriptblock]$draw) {
    $bitmap = New-Object System.Drawing.Bitmap($width, $height)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    & $draw $g
    $g.Dispose()
    return $bitmap
}

$Green = [System.Drawing.Color]::FromArgb(11, 79, 96)
$Teal = [System.Drawing.Color]::FromArgb(14, 109, 124)
$Gold = [System.Drawing.Color]::FromArgb(255, 214, 107)
$Deep = [System.Drawing.Color]::FromArgb(6, 26, 30)
$Mint = [System.Drawing.Color]::FromArgb(221, 242, 234)
$Ink = [System.Drawing.Color]::FromArgb(27, 35, 32)
$Muted = [System.Drawing.Color]::FromArgb(95, 106, 102)
$Surface = [System.Drawing.Color]::FromArgb(249, 250, 248)

$icon = New-Canvas 512 512 {
    param($g)
    $g.Clear($Deep)
    $white = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $greenBrush = New-Object System.Drawing.SolidBrush($Green)
    $tealBrush = New-Object System.Drawing.SolidBrush($Teal)
    $goldBrush = New-Object System.Drawing.SolidBrush($Gold)
    $deepBrush = New-Object System.Drawing.SolidBrush($Deep)
    $outlinePen = New-Object System.Drawing.Pen($Deep, 8)
    $outlinePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $outlinePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $card = New-RoundedRectPath 86 128 294 292 34
    $g.FillPath($greenBrush, $card)
    $top = New-RoundedRectPath 86 128 294 86 34
    $g.FillPath($tealBrush, $top)
    $g.FillRectangle($tealBrush, 86, 172, 294, 52)

    $binderPen = New-Object System.Drawing.Pen($Teal, 34)
    $binderPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $binderPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($outlinePen, 164, 106, 164, 162)
    $g.DrawLine($outlinePen, 292, 106, 292, 162)
    $g.DrawLine($binderPen, 164, 106, 164, 162)
    $g.DrawLine($binderPen, 292, 106, 292, 162)

    $whitePen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, 18)
    $whitePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $whitePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $clockPen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, 22)
    $clockPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $clockPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawEllipse($clockPen, 124, 194, 210, 210)
    $handPen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, 20)
    $handPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $handPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($handPen, 228, 300, 176, 248)
    $g.DrawLine($handPen, 228, 300, 278, 300)
    $g.FillEllipse($white, 210, 282, 36, 36)

    $tickPen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, 12)
    $tickPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $tickPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($tickPen, 228, 216, 228, 232)
    $g.DrawLine($tickPen, 228, 368, 228, 384)
    $g.DrawLine($tickPen, 144, 300, 160, 300)

    $bell = New-Object System.Drawing.Drawing2D.GraphicsPath
    $bell.StartFigure()
    $bell.AddBezier(316, 216, 338, 206, 354, 224, 354, 252)
    $bell.AddLine(354, 252, 354, 286)
    $bell.AddBezier(354, 286, 354, 314, 380, 324, 394, 344)
    $bell.AddLine(394, 344, 270, 344)
    $bell.AddBezier(270, 344, 284, 324, 304, 314, 304, 286)
    $bell.AddLine(304, 286, 304, 252)
    $bell.AddBezier(304, 252, 304, 236, 308, 224, 316, 216)
    $bell.CloseFigure()
    $g.FillPath($white, $bell)
    $bellOutline = New-Object System.Drawing.Pen($Deep, 6)
    $g.DrawPath($bellOutline, $bell)
    $g.FillEllipse($white, 328, 342, 52, 44)

    $slashOutline = New-Object System.Drawing.Pen($Deep, 42)
    $slashOutline.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $slashOutline.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($slashOutline, 300, 244, 426, 370)
    $slashPen = New-Object System.Drawing.Pen([System.Drawing.Color]::White, 28)
    $slashPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $slashPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($slashPen, 300, 244, 426, 370)

    $g.FillEllipse($goldBrush, 352, 86, 128, 144)
    $g.FillEllipse($deepBrush, 402, 78, 136, 140)

    $card.Dispose(); $top.Dispose(); $white.Dispose(); $greenBrush.Dispose(); $tealBrush.Dispose(); $goldBrush.Dispose(); $deepBrush.Dispose()
    $outlinePen.Dispose(); $binderPen.Dispose(); $whitePen.Dispose(); $clockPen.Dispose(); $handPen.Dispose(); $tickPen.Dispose()
    $bell.Dispose(); $bellOutline.Dispose(); $slashOutline.Dispose(); $slashPen.Dispose()
}
Save-Png $icon (Join-Path $AssetsDir "icon-512.png")

$feature = New-Canvas 1024 500 {
    param($g)
    $g.Clear($Surface)
    $greenBrush = New-Object System.Drawing.SolidBrush($Green)
    $inkBrush = New-Object System.Drawing.SolidBrush($Ink)
    $mutedBrush = New-Object System.Drawing.SolidBrush($Muted)
    $mintBrush = New-Object System.Drawing.SolidBrush($Mint)
    $titleFont = New-Object System.Drawing.Font("Segoe UI", 54, [System.Drawing.FontStyle]::Bold)
    $bodyFont = New-Object System.Drawing.Font("Segoe UI", 24, [System.Drawing.FontStyle]::Regular)
    $path = New-RoundedRectPath 650 70 260 360 42
    $g.FillPath($mintBrush, $path)
    $g.FillRectangle($greenBrush, 690, 130, 180, 18)
    $g.FillRectangle($greenBrush, 690, 190, 145, 18)
    $g.FillRectangle($greenBrush, 690, 250, 165, 18)
    $g.DrawString("Silent Scheduler", $titleFont, $inkBrush, 72, 130)
    $g.DrawString("Silent for prayers, lectures, and events.", $bodyFont, $mutedBrush, 78, 210)
    $g.DrawString("Automatic prayer timings with simple DND scheduling.", $bodyFont, $mutedBrush, 78, 252)
    $path.Dispose(); $greenBrush.Dispose(); $inkBrush.Dispose(); $mutedBrush.Dispose(); $mintBrush.Dispose(); $titleFont.Dispose(); $bodyFont.Dispose()
}
Save-Png $feature (Join-Path $AssetsDir "feature-graphic-1024x500.png")

function Draw-PhoneScreen([string]$path, [string]$title, [string[]]$lines, [string]$tab) {
    $screen = New-Canvas 1080 1920 {
        param($g)
        $g.Clear($Surface)
        $inkBrush = New-Object System.Drawing.SolidBrush($Ink)
        $mutedBrush = New-Object System.Drawing.SolidBrush($Muted)
        $greenBrush = New-Object System.Drawing.SolidBrush($Green)
        $mintBrush = New-Object System.Drawing.SolidBrush($Mint)
        $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
        $titleFont = New-Object System.Drawing.Font("Segoe UI", 54, [System.Drawing.FontStyle]::Bold)
        $rowFont = New-Object System.Drawing.Font("Segoe UI", 34, [System.Drawing.FontStyle]::Regular)
        $smallFont = New-Object System.Drawing.Font("Segoe UI", 26, [System.Drawing.FontStyle]::Regular)
        $g.DrawString($title, $titleFont, $inkBrush, 72, 86)
        $y = 210
        foreach ($line in $lines) {
            $card = New-RoundedRectPath 56 $y 968 132 18
            $g.FillPath($whiteBrush, $card)
            $g.DrawString($line, $rowFont, $inkBrush, 92, ($y + 38))
            $card.Dispose()
            $y += 156
        }
        $bar = New-RoundedRectPath 0 1760 1080 160 0
        $g.FillPath($whiteBrush, $bar)
        $tabs = @("Events", "Timings", "Settings")
        for ($i = 0; $i -lt $tabs.Count; $i++) {
            $x = 150 + ($i * 330)
            $brush = if ($tabs[$i] -eq $tab) { $greenBrush } else { $mutedBrush }
            if ($tabs[$i] -eq $tab) {
                $pill = New-RoundedRectPath ($x - 34) 1782 160 64 32
                $g.FillPath($mintBrush, $pill)
                $pill.Dispose()
            }
            $g.DrawString($tabs[$i], $smallFont, $brush, $x, 1800)
        }
        $bar.Dispose(); $inkBrush.Dispose(); $mutedBrush.Dispose(); $greenBrush.Dispose(); $mintBrush.Dispose(); $whiteBrush.Dispose(); $titleFont.Dispose(); $rowFont.Dispose(); $smallFont.Dispose()
    }
    Save-Png $screen $path
}

Draw-PhoneScreen (Join-Path $ScreenshotsDir "01-events.png") "Events" @(
    "Sunrise 5:31 AM",
    "Daily ayah with Urdu translation",
    "Quick DND: 15m  30m  60m",
    "No events yet",
    "Tap Add to create your first DND schedule"
) "Events"

Draw-PhoneScreen (Join-Path $ScreenshotsDir "02-prayer-timings.png") "Prayer timings" @(
    "Fajr   5:10 AM - Sunrise",
    "Zuhr   1:30 PM - Asr",
    "Asr   5:15 PM - Maghrib",
    "Maghrib   7:05 PM - Isha",
    "Isha   8:45 PM - Fajr"
) "Timings"

Draw-PhoneScreen (Join-Path $ScreenshotsDir "03-settings.png") "Settings" @(
    "Restore previous sound mode",
    "Auto update prayer event times",
    "Fiqah: Hanafi / Jafria",
    "Permission shortcuts",
    "Clear schedules"
) "Settings"

Write-Host "Store assets generated in $AssetsDir"
