package com.example.mangojc.Fragments

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mangojc.Data.Avatar
import com.example.mangojc.Data.BottomNavItem
import com.example.mangojc.Data.UserDataBody
import com.example.mangojc.R
import com.example.mangojc.Repository.Repository
import com.example.mangojc.ViewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ChatsFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    var imagePath: Uri? = null
    private val rep = Repository()
    private var phoneNumber = ""

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map ->
        if(!map.values.all { it } && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            Toast.makeText(activity, "Permissions are not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        checkPermissions()
        view.setContent {
            //ProfileView(requireActivity().contentResolver, viewModel, requireContext())
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            requireActivity().let { ContextCompat.checkSelfPermission(it.applicationContext, permission) } == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted){
            Toast.makeText(requireActivity(), "Permissions are Granted", Toast.LENGTH_SHORT).show()
        }
        else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
    }

    companion object{
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                add(READ_MEDIA_IMAGES)
            }
        }.toTypedArray()
    }
}

  //  override fun onCreateView(
  //      inflater: LayoutInflater, container: ViewGroup?,
  //      savedInstanceState: Bundle?
  //  ): View? {
//
//        _binding = FragmentProfileBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setDefaultMode()
//
//        binding.buttonEdit.setOnClickListener {
//            setEditMode()
//        }
//
//        binding.buttonSave.setOnClickListener {
//            setDefaultMode()
//
//           // val imageFile = File(requireContext().cacheDir, "base64")
//           // if (imagePath != null) {
//           //     try {
//           //         val instream: InputStream =
//           //             requireContext().contentResolver.openInputStream(imagePath!!)!!
//           //         val output = FileOutputStream(imageFile)
//           //         val buffer = ByteArray(1024)
//           //         var size: Int
//           //         while (instream.read(buffer).also { size = it } != -1) {
//           //             output.write(buffer, 0, size)
//           //         }
//           //         instream.close()
//           //         output.close()
//           //     } catch (e: IOException) {
//           //         Log.d("TAG1", "e: ${e}")
//           //     }
//           // }
//
//            //binding.imageView.setImageURI(Uri.parse(imagePath.toString()))
//
//            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imagePath)
//            Log.d("Bitmap", bitmap.toString())
//
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
//            val byteArray = stream.toByteArray()
//
//            val imageBody = MultipartBody.Part.createFormData(
//                "base64",
//                "base64",
//                byteArray.toRequestBody("image/*".toMediaTypeOrNull(), 0, byteArray.size)
//            )
//            viewModel.putProfileData(binding.birthday.text.toString(), binding.city.text.toString(), "", "", binding.status.text.toString(), imageBody)
//            lifecycleScope.launch {
//                viewModel.loading.collect{ loading ->
//                    if (!loading){
//                        viewModel.avatars.collect{ avatars ->
//                            Log.d("AVATARS", avatars.toString())
//                        }
//                    }
//                }
//            }
//        }
//
//        binding.imageView.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, 10)
//        }
//    }
//

//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//   private fun setDefaultMode(){
//       binding.buttonEdit.visibility = View.VISIBLE
//       binding.buttonSave.visibility = View.GONE
//
//       binding.imageView.isEnabled = false
//       binding.imageView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.image_view_background)
//
//       binding.status.isEnabled = false
//       binding.status.background = null
//
//       binding.city.isEnabled = false
//       binding.city.background = null
//
//       binding.birthday.isEnabled = false
//       binding.birthday.background = null
//   }
//
//    private fun setEditMode(){
//        binding.buttonEdit.visibility = View.GONE
//        binding.buttonSave.visibility = View.VISIBLE
//
//        binding.imageView.isEnabled = true
//        binding.imageView.background = ContextCompat.getDrawable(requireActivity(), R.drawable.image_view_edit_background)
//
//        binding.status.isEnabled = true
//        binding.status.background = ContextCompat.getDrawable(requireActivity(), R.drawable.edit_text_background)
//
//        binding.city.isEnabled = true
//        binding.city.background = ContextCompat.getDrawable(requireActivity(), R.drawable.edit_text_background)
//
//        binding.birthday.isEnabled = true
//        binding.birthday.background = ContextCompat.getDrawable(requireActivity(), R.drawable.edit_text_background)
//    }
//}

