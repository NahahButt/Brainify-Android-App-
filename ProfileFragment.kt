package com.brainify.quizapp.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.brainify.quizapp.R
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    // UI Components (Linked to XML)
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvEditProfile: TextView
    private lateinit var tvEditUsername: TextView
    private lateinit var etUsername: EditText
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEditPassword: TextView
    private lateinit var etPassword: EditText
    private lateinit var ivEye: ImageView
    private lateinit var btnCancel: Button
    private lateinit var btnSaveChanges: Button

    private var isPasswordVisible = false
    private var isEditMode = false
    private var editType: EditType = EditType.NONE
    private var profileFile: File? = null

    private enum class EditType {
        NONE, USERNAME, PASSWORD, PROFILE
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = uriToBitmap(uri)
                bitmap?.let { bmp ->
                    val circularBmp = getCircularBitmap(bmp)
                    ivProfilePic.setImageBitmap(circularBmp)
                    saveBitmapToInternalStorage(circularBmp)
                    editType = EditType.PROFILE
                    showActionButtons()
                    showToast("Profile picture updated")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // XML layout ko inflate karein
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Views ko XML IDs ke saath link karein
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvEditProfile = view.findViewById(R.id.tvEditProfile)
        tvEditUsername = view.findViewById(R.id.tvEditUsername)
        etUsername = view.findViewById(R.id.etUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvEditPassword = view.findViewById(R.id.tvEditPassword)
        etPassword = view.findViewById(R.id.etPassword)
        ivEye = view.findViewById(R.id.ivEye)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)

        setupClickListeners()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun setupClickListeners() {
        tvEditProfile.setOnClickListener {
            if (isEditMode) return@setOnClickListener
            editType = EditType.PROFILE
            setEditMode(true)
            showActionButtons()
            pickImageLauncher.launch("image/*")
        }

        tvEditUsername.setOnClickListener {
            if (isEditMode) return@setOnClickListener
            editType = EditType.USERNAME
            setEditMode(true)
            showActionButtons()
            etUsername.requestFocus()
        }

        tvEditPassword.setOnClickListener {
            if (isEditMode) return@setOnClickListener
            editType = EditType.PASSWORD
            setEditMode(true)
            showActionButtons()
            etPassword.requestFocus()
        }

        ivEye.setOnClickListener { togglePasswordVisibility() }
        btnCancel.setOnClickListener { cancelEdit() }
        btnSaveChanges.setOnClickListener { saveChanges() }
    }

    private fun loadUserData() {
        val pref = requireActivity().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)

        etUsername.setText(pref.getString("user_name", ""))
        tvEmail.text = pref.getString("user_email", "email@example.com")
        tvPhone.text = pref.getString("user_phone", "0300XXXXXXX")
        etPassword.setText(pref.getString("user_password", ""))

        val filePath = pref.getString("user_dp_path", null)
        filePath?.let {
            val file = File(it)
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(it)
                ivProfilePic.setImageBitmap(getCircularBitmap(bitmap))
            }
        }
    }

    private fun setEditMode(enable: Boolean) {
        isEditMode = enable
        etUsername.isEnabled = enable
        etPassword.isEnabled = enable
        ivEye.visibility = if (enable && editType == EditType.PASSWORD) View.VISIBLE else View.GONE
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        } else {
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text.length)
    }

    private fun showActionButtons() {
        btnCancel.visibility = View.VISIBLE
        btnSaveChanges.visibility = View.VISIBLE
        tvEditUsername.visibility = View.GONE
        tvEditPassword.visibility = View.GONE
        tvEditProfile.visibility = View.GONE
    }

    private fun hideActionButtons() {
        btnCancel.visibility = View.GONE
        btnSaveChanges.visibility = View.GONE
        tvEditUsername.visibility = View.VISIBLE
        tvEditPassword.visibility = View.VISIBLE
        tvEditProfile.visibility = View.VISIBLE
    }

    private fun saveChanges() {
        val pref = requireActivity().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
        val editor = pref.edit()

        when (editType) {
            EditType.USERNAME -> {
                val name = etUsername.text.toString().trim()
                if (name.isEmpty()) { showToast("Name required"); return }
                editor.putString("user_name", name)
            }
            EditType.PASSWORD -> {
                val pass = etPassword.text.toString().trim()
                if (pass.length < 6) { showToast("Min 6 chars"); return }
                editor.putString("user_password", pass)
            }
            else -> {}
        }
        editor.apply()
        setEditMode(false)
        hideActionButtons()
        showToast("Changes saved successfully")
    }

    private fun cancelEdit() {
        loadUserData()
        setEditMode(false)
        hideActionButtons()
        showToast("Changes cancelled")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val stream = requireActivity().contentResolver.openInputStream(uri)
            val bmp = android.graphics.BitmapFactory.decodeStream(stream)
            stream?.close()
            bmp
        } catch (e: Exception) { null }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        canvas.drawOval(RectF(rect), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap) {
        try {
            val file = File(requireContext().filesDir, "profile_pic.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            requireActivity().getSharedPreferences("BrainifyPrefs", Context.MODE_PRIVATE)
                .edit().putString("user_dp_path", file.absolutePath).apply()
        } catch (e: Exception) { e.printStackTrace() }
    }
}