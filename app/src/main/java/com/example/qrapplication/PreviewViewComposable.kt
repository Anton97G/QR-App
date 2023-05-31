import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qrapplication.BarcodeAnalyser
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage


@Composable
fun PreviewViewComposable(applicationContext: Context) {

    var qrText by remember { mutableStateOf("")}

    AndroidView({ context ->
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val previewView = PreviewView(context).also {
            it.scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyser{
                        text -> qrText = text
//
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    context as ComponentActivity, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e("DEBUG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
        previewView
    },
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp))
            //.size(width = 250.dp, height = 250.dp))
    
    
    if (qrText.startsWith("http")){
    
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Button(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(qrText)
                )
                val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                    addNextIntentWithParentStack(intent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                    }
                }
                pendingIntent.send()

            }) {
                Text(text = "ПЕРЕЙТИ ПО ССЫЛКЕ \n $qrText")

            }
        }
        
    } else
    
    
    
    Text(
        text = qrText,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxSize(2f),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

}



