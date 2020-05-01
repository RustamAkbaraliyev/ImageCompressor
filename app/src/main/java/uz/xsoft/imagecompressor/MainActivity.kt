package uz.xsoft.imagecompressor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import id.zelory.compressor.loadBitmap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private var compressedImage: File? = null
    private var actualImageFile: File? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img.setBackgroundColor(getRandomColor())
        setupButton()
        setCompressedImage()

    }

    private fun compressedImage() {
        actualImageFile?.let { actImg ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@MainActivity, actImg)
                setCompressedImage()
            }
        }

    }

    private fun setupButton() {
        chooseImg.setOnClickListener { chooseImage() }
        btnCompress.setOnClickListener { compressedImage() }
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            imgCompress.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            textImgCompress.text = String.format("Size : %s", getReadableFileSize(it.length()))

            Toast.makeText(this, "Compressed image save in " + it.path, Toast.LENGTH_LONG).show()
        }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (kotlin.math.log10(size.toDouble()) / kotlin.math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                showError("Failed to open picture!")
                return
            }
            try {
                actualImageFile = FileUtil.from(this, data.data)?.also {
                    img.setImageBitmap(loadBitmap(it))
                    textImg.text = String.format("Size : %s", getReadableFileSize(it.length()))
                    clearImage()
                }
            } catch (e: IOException) {
                showError("Failed to read picture data!")
                e.printStackTrace()
            }
        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun clearImage() {
        img.setBackgroundColor(getRandomColor())
        imgCompress.setImageDrawable(null)
        imgCompress.setBackgroundColor(getRandomColor())
        textImgCompress.text = ""
    }

    private fun getRandomColor() = Random().run {
        Color.argb(100, nextInt(256), nextInt(256), nextInt(256))
    }

    private fun customCompressImage() {
        actualImageFile?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@MainActivity, imageFile) {
                    resolution(1280, 720)
                    quality(80)
                    format(Bitmap.CompressFormat.WEBP)
                    size(2_097_152) // 2 MB
                }
                setCompressedImage()

            }
        }
    }
}

