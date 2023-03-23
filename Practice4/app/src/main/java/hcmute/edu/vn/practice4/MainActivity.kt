package hcmute.edu.vn.practice4

import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Integer.min

class MainActivity : AppCompatActivity() {

    // Declaring the UI elements from the layout file
    private lateinit var buttonCrop: Button
    private lateinit var buttonSave: Button
    private lateinit var imageView: ImageView

    // Declaring the Bitmap
    private lateinit var bitmap: Bitmap

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializing the UI elements
        imageView = findViewById(R.id.iv)
        buttonCrop = findViewById(R.id.btnCrop)
        buttonSave = findViewById(R.id.btnSave)

        // Declaring resource address ( type integer)
        val bitmapResourceID: Int = R.drawable.ttt

        // Setting the ImageView with the Image
        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, bitmapResourceID))
        bitmap = BitmapFactory.decodeResource(resources, bitmapResourceID)

        // When Crop button is clicked
        buttonCrop.setOnClickListener {
            // runs a custom function on the original image
            bitmap = getCircularBitmap(bitmap)

            // Sets the ImageView with the editted/cropped Image
            imageView.setImageBitmap(bitmap)
        }

        // When Save button is clicked
        buttonSave.setOnClickListener {
            // Save whatever the bitmap is (edited/uneditted) into the device.
            saveMediaToStorage(bitmap)
        }
    }

    // Function to crop the image in a circle
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCircularBitmap(srcBitmap: Bitmap?): Bitmap {

        // Select whichever of width or height is minimum
        val squareBitmapWidth = min(srcBitmap!!.width, srcBitmap.height)

        // Generate a bitmap with the above value as dimensions
        val dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth,
            squareBitmapWidth,
            Bitmap.Config.ARGB_8888
        )

        // Initializing a Canvas with the above generated bitmap
        val canvas = Canvas(dstBitmap)

        // initializing Paint
        val paint = Paint()
        paint.isAntiAlias = true

        // Generate a square (rectangle with all sides same)
        val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
        val rectF = RectF(rect)

        // Operations to draw a circle
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = ((squareBitmapWidth - srcBitmap.width) / 2).toFloat()
        val top = ((squareBitmapWidth - srcBitmap.height) / 2).toFloat()
        canvas.drawBitmap(srcBitmap, left, top, paint)
        srcBitmap.recycle()

        // Return the bitmap
        return dstBitmap
    }

    // Function to save an Image
    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "ttt/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }
    }
}
