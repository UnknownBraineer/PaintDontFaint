package com.example.paintdontfaint

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paintdontfaint.databinding.ActivityMainBinding
import com.example.paintdontfaint.utils.hasLocalPermissions
import com.example.paintdontfaint.utils.saveImage
import com.google.android.material.slider.RangeSlider
import petrov.kristiyan.colorpicker.ColorPicker
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.OutputStream
import java.lang.Exception


private const val REQUEST_PERMISSION_CODE = 0

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions()

        binding.btnUndo.setOnClickListener {
            binding.drawView.undo()
        }

        binding.btnSave.setOnClickListener {
            val bmp = binding.drawView.save()
            if (hasLocalPermissions(this)) {
                try {
                    saveImage(bmp, this, Environment.DIRECTORY_PICTURES)
                } catch (e: Exception) {
                    e.printStackTrace()
                }                }

        }

        binding.btnColor.setOnClickListener {
            val colorPicker = ColorPicker(this)
            colorPicker.setOnFastChooseColorListener(object: ColorPicker.OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(position: Int, color: Int) {
                    binding.drawView.setColor(color)
                }

                override fun onCancel() {
                    colorPicker.dismissDialog()
                }
            })
                .setColumns(5)
                .setDefaultColorButton(Color.parseColor("#000000"))
                .show()
        }

        binding.btnStroke.setOnClickListener {
            if (binding.rangebar.visibility == View.VISIBLE) {
                binding.rangebar.visibility = View.GONE
            } else {
                binding.rangebar.visibility = View.VISIBLE
            }
        }
        binding.rangebar.valueFrom = 0.0f
        binding.rangebar.valueTo = 100.0f

        binding.rangebar.addOnChangeListener(object : RangeSlider.OnChangeListener {
            @SuppressLint("RestrictedApi")
            override fun onValueChange(slider: RangeSlider, value: Float, fromUser: Boolean) {
                binding.drawView.setStrokeWidth(value.toInt())
            }
        })

        val viewTreeObs = binding.drawView.viewTreeObserver
        viewTreeObs.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.drawView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                binding.drawView.init(binding.drawView.measuredHeight, binding.drawView.measuredWidth)
            }
        })
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (hasLocalPermissions(this)) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You have to accept location permissions to use this app.",
            REQUEST_PERMISSION_CODE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}