package com.epicwindmill.webpexiftestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.epicwindmill.webpexiftestapp.ui.theme.WebPExifTestAppTheme
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebPExifTestAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }
}

private val photos = listOf(
    R.raw.case1_portait,
    R.raw.case1_portait_webp,
    R.raw.case2_landscape_turned_left,
    R.raw.case2_landscape_turned_left_webp,
    R.raw.case3_upside_down_portrait,
    R.raw.case3_upside_down_portrait_webp,
    R.raw.case4_landscape_turned_right,
    R.raw.case4_landscape_turned_right_webp,
    R.raw.test_no_exif, // https://developers.google.com/speed/webp/gallery1
    R.raw.test_no_exif_webp,
)

@Composable
private fun Content() {
    val useCustomExifInterface = remember {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier.background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 64.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = useCustomExifInterface.value,
                onCheckedChange = { useCustomExifInterface.value = it }
            )
            Text(
                modifier = Modifier.clickable {
                    useCustomExifInterface.value = !useCustomExifInterface.value
                },
                text = if (useCustomExifInterface.value) {
                    "Using Laurence's Exif Interface"
                } else {
                    "Using Google's Exif Interface"
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Green
                )
            )
        }

        LazyVerticalGrid(
            modifier = Modifier
                .padding(horizontal = 80.dp),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            label(label = "JPEG")
            label(label = "WEBP")
            items(photos) { photo ->
                PhotoItem(photo, useCustomExifInterface.value)
            }
        }
    }
}

fun LazyGridScope.label(label: String) {
    item {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Green
                )
            )
        }
    }
}

@Composable
fun PhotoItem(@RawRes image: Int, useCustomExifInterface: Boolean) {
    val painter = rememberAsyncImagePainter(image)
    val context = LocalContext.current
    val imageStream: InputStream = context.resources.openRawResource(image)

    var orientation: String? = "not_set"
    var rotation = 0
    var fileType = -1
    try {
        imageStream.use { inputStream ->
            if (useCustomExifInterface) {
                val exif = com.epicwindmill.webpexiftestapp.media.ExifInterface(inputStream)
                orientation = exif.getAttributeInt(
                    com.epicwindmill.webpexiftestapp.media.ExifInterface.TAG_ORIENTATION,
                    com.epicwindmill.webpexiftestapp.media.ExifInterface.ORIENTATION_NORMAL
                ).toString()
                rotation = exif.getRotationDegrees()
                fileType =
                    exif.mMimeType // Note: for prod this isnt needed, for this sample we want to know the file type
            } else {
                val exif = androidx.exifinterface.media.ExifInterface(inputStream)
                orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                ).toString()
                rotation = exif.getRotationDegrees()
            }

            orientation = if (orientation == "0") {
                "no exif"
            } else {
                orientation
            }
        }
    } catch (e: IOException) {
        println(e)
        e.printStackTrace()
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .background(Color.White)
                .aspectRatio(1f)
                .rotate(
                    // Manually rotate the webp ones based on the reported rotation
                    // Normally the correct value would propagate to coil
                    if (fileType == 14) {
                        rotation.toFloat()
                    } else {
                        0.0f
                    }
                )
        )
        Text(
            modifier = Modifier.background(Color(0x60000000)),
            text = "$rotation degrees\norientation: $orientation",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Green
            )
        )
    }
}
