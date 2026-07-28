@file:Suppress("FunctionNaming", "LongMethod", "MagicNumber")

package com.nextrank.feature.training.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Composable
internal fun WorkshopQrScanner(
    errorMessage: String?,
    onQrDetected: (String) -> Boolean,
    onClose: () -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionRequested by rememberSaveable { mutableStateOf(hasCameraPermission) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        permissionRequested = true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraScannerContent(
            errorMessage = errorMessage,
            onQrDetected = onQrDetected,
            onClose = onClose,
            onCameraError = onCameraError,
            modifier = modifier,
        )
    } else {
        CameraPermissionContent(
            permissionRequested = permissionRequested,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onClose = onClose,
            modifier = modifier,
        )
    }
}

@Composable
private fun CameraPermissionContent(
    permissionRequested: Boolean,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GamerScreen(modifier = modifier) {
        Spacer(Modifier.weight(1f))
        GamerPanel(accent = GamerAccentLime) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = GamerAccentLime,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(56.dp),
            )
            GamerHeader(
                title = "Нужен доступ к камере",
                subtitle = if (permissionRequested) {
                    "Разреши CyberGym использовать камеру в настройках разрешений, чтобы считать результат с карты."
                } else {
                    "Камера используется только для распознавания QR-кода с итогового экрана Workshop."
                },
            )
            GamerPrimaryButton(
                text = "Разрешить камеру",
                onClick = onRequestPermission,
            )
            GamerSecondaryButton(text = "Вернуться", onClick = onClose)
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun CameraScannerContent(
    errorMessage: String?,
    onQrDetected: (String) -> Boolean,
    onClose: () -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by rememberSaveable { mutableStateOf(false) }
    val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        CameraPreview(
            onQrDetected = onQrDetected,
            onCameraReady = { camera = it },
            onCameraError = onCameraError,
            modifier = Modifier.fillMaxSize(),
        )
        ScannerOverlay(Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScannerIconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Закрыть сканер")
            }
            Text(
                text = "СКАНЕР CYBERGYM",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
            )
            ScannerIconButton(
                onClick = {
                    val newValue = !torchEnabled
                    camera?.cameraControl?.enableTorch(newValue)
                    torchEnabled = newValue
                },
                enabled = hasFlash,
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Фонарик",
                    tint = if (torchEnabled) GamerAccentLime else Color.White,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(20.dp)
                .background(
                    color = Color(0xE60A151E),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Наведи камеру на QR-код",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Код распознается автоматически. Держи его внутри рамки.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF9D36),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ScannerIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xB30A151E), CircleShape),
        content = content,
    )
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanner-line")
    val lineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanner-line-progress",
    )

    Canvas(modifier = modifier) {
        val frameSize = min(size.width * 0.78f, size.height * 0.46f)
        val frameLeft = (size.width - frameSize) / 2f
        val frameTop = (size.height - frameSize) / 2f - 26.dp.toPx()
        val frameRight = frameLeft + frameSize
        val frameBottom = frameTop + frameSize
        val shade = Color.Black.copy(alpha = 0.48f)

        drawRect(shade, size = Size(size.width, frameTop))
        drawRect(
            shade,
            topLeft = Offset(0f, frameBottom),
            size = Size(size.width, size.height - frameBottom),
        )
        drawRect(
            shade,
            topLeft = Offset(0f, frameTop),
            size = Size(frameLeft, frameSize),
        )
        drawRect(
            shade,
            topLeft = Offset(frameRight, frameTop),
            size = Size(size.width - frameRight, frameSize),
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.28f),
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameSize, frameSize),
            cornerRadius = CornerRadius(18.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )

        val cornerLength = 42.dp.toPx()
        val cornerWidth = 5.dp.toPx()
        val corners = listOf(
            Triple(Offset(frameLeft, frameTop), Offset(1f, 0f), Offset(0f, 1f)),
            Triple(Offset(frameRight, frameTop), Offset(-1f, 0f), Offset(0f, 1f)),
            Triple(Offset(frameLeft, frameBottom), Offset(1f, 0f), Offset(0f, -1f)),
            Triple(Offset(frameRight, frameBottom), Offset(-1f, 0f), Offset(0f, -1f)),
        )
        corners.forEach { (origin, horizontal, vertical) ->
            drawLine(
                GamerAccentLime,
                origin,
                origin + horizontal * cornerLength,
                cornerWidth,
                StrokeCap.Round,
            )
            drawLine(
                GamerAccentLime,
                origin,
                origin + vertical * cornerLength,
                cornerWidth,
                StrokeCap.Round,
            )
        }

        val scanY = frameTop + frameSize * lineProgress
        drawLine(
            color = GamerAccentLime.copy(alpha = 0.92f),
            start = Offset(frameLeft + 14.dp.toPx(), scanY),
            end = Offset(frameRight - 14.dp.toPx(), scanY),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Boolean,
    onCameraReady: (Camera) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnQrDetected by rememberUpdatedState(onQrDetected)
    val currentOnCameraError by rememberUpdatedState(onCameraError)
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )

    DisposableEffect(lifecycleOwner, previewView) {
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val analyzer = CyberGymQrAnalyzer(
            onQrDetected = { currentOnQrDetected(it) },
            onFailure = { currentOnCameraError(it) },
        )
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var disposed = false

        cameraProviderFuture.addListener(
            listener@{
                if (disposed) return@listener
                runCatching {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(analysisExecutor, analyzer) }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }.onSuccess(onCameraReady)
                    .onFailure { error ->
                        currentOnCameraError(error.message ?: "Не удалось запустить камеру.")
                    }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            disposed = true
            analyzer.close()
            analysisExecutor.shutdown()
            if (cameraProviderFuture.isDone) {
                runCatching { cameraProviderFuture.get().unbindAll() }
            }
        }
    }
}

private class CyberGymQrAnalyzer(
    private val onQrDetected: (String) -> Boolean,
    private val onFailure: (String) -> Unit,
) : ImageAnalysis.Analyzer, AutoCloseable {
    private val processing = AtomicBoolean(false)
    private val resultDelivered = AtomicBoolean(false)
    private val errorDelivered = AtomicBoolean(false)
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )
    private var lastRejectedValue: String? = null
    private var lastRejectedAt: Long = 0L

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (resultDelivered.get() || !processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            processing.set(false)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }
                if (rawValue != null && shouldProcess(rawValue)) {
                    val accepted = onQrDetected(rawValue)
                    if (accepted) {
                        resultDelivered.set(true)
                    } else {
                        lastRejectedValue = rawValue
                        lastRejectedAt = SystemClock.elapsedRealtime()
                    }
                }
            }
            .addOnFailureListener { error ->
                if (errorDelivered.compareAndSet(false, true)) {
                    onFailure(error.message ?: "Ошибка распознавания QR-кода.")
                }
            }
            .addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }

    private fun shouldProcess(rawValue: String): Boolean =
        rawValue != lastRejectedValue ||
            SystemClock.elapsedRealtime() - lastRejectedAt >= REJECTED_CODE_RETRY_MS

    override fun close() {
        scanner.close()
    }

    private companion object {
        const val REJECTED_CODE_RETRY_MS = 1_500L
    }
}
