package com.example.mangojc.Fragments

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mangojc.App
import com.example.mangojc.Data.Avatar
import com.example.mangojc.Data.BottomNavItem
import com.example.mangojc.Data.DBProfileData
import com.example.mangojc.Data.LoadingState
import com.example.mangojc.Data.ProfileData
import com.example.mangojc.Data.UserDataBody
import com.example.mangojc.R
import com.example.mangojc.ViewModels.DBViewModel
import com.example.mangojc.ViewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private val dbViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val userDao = (requireActivity().application as App).db.userDao()
                return DBViewModel(userDao) as T
            }
        }
    }
    private var phoneNumber: String? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map ->
        if(!map.values.all { it } && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            Toast.makeText(activity, "Permissions are not Granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneNumber = it.getString(resources.getString(R.string.phone_number_key))
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        checkPermissions()
        if (!viewModel.externalStorageImagesGranted.value)
            CoroutineScope(Dispatchers.IO).launch {
                getImages(requireActivity().contentResolver, viewModel)
                viewModel.externalStorageImagesGranted.collect{
                    if (it)
                        this.cancel()
                }
            }
        view.setContent {
            BottomNavMenu(viewModel, dbViewModel, requireContext(), phoneNumber ?: "")
            WatchProfileData(viewModel, dbViewModel)
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

@Composable
fun WatchProfileData(viewModel: MainViewModel, dbViewModel: DBViewModel){
    val profileDataLoading = viewModel.profileDataLoading.collectAsState()
    val profileData = viewModel.profileData.collectAsState()
    val data = profileData.value.profileData
    if (profileDataLoading.value == LoadingState.Success){
        LaunchedEffect(Unit){
            val avatar = dbViewModel.getProfileData(profileData.value.profileData.phone ?: "")?.avatar ?: ""
            dbViewModel.addProfileData(DBProfileData
                (data.phone ?: "", data.name, data.userName, data.birthday, data.city, data.status, avatar))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileView(viewModel: MainViewModel){
    val profileData = viewModel.profileData.collectAsState()
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .padding(20.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SetImage(
                image = if (profileData.value.profileData.avatar != null) profileData.value.profileData.avatar else null,
                modifier = Modifier.size(150.dp),
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                contentScale = ContentScale.Crop
            )

            SetTextDataColumn(profileData.value.profileData)
        }
        (if (profileData.value.profileData.userName != null) profileData.value.profileData.userName else "Глад Валакас")?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
        (if (profileData.value.profileData.phone != null) profileData.value.profileData.phone else "+79999999999")?.let {
            Text(
                text = it
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetTextDataColumn(profileData: ProfileData){
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .padding(start = 20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.personal_status),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = profileData.status ?: stringResource(id = R.string.personal_status_default),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.city),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = profileData.city ?: stringResource(id = R.string.city_default),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row (
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(end = 20.dp)
            ) {
                val birthday = profileData.birthday ?: stringResource(id = R.string.date_of_birth_default)

                Text(
                    text = stringResource(id = R.string.date_of_birth),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = getDate(birthday)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SetImage(
    image: Any?,
    modifier: Modifier,
    shape: RoundedCornerShape,
    contentScale: ContentScale
){
    Card(
        shape = shape,
        //modifier = Modifier.background(colorResource(id = R.color.dark_gray))
    ) {
        if (image == null || !image.toString().startsWith("content"))
            Image(
                painter = painterResource(id = R.drawable.glad),
                contentDescription = null,
                modifier = modifier,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )
        else
            GlideImage(
                model = image,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileView(viewModel: MainViewModel, dbViewModel: DBViewModel, context: Context){
    val profileData = viewModel.profileData.collectAsState()
    val currentImage = profileData.value.profileData.avatar ?: ""
    val currentName = profileData.value.profileData.name ?: ""
    val currentStatus = profileData.value.profileData.status ?: ""
    val currentCity = profileData.value.profileData.city ?: ""
    val currentVK = profileData.value.profileData.vk ?: ""
    val currentInstagram = profileData.value.profileData.instagram ?: ""
    val currentBirthday = profileData.value.profileData.birthday ?: stringResource(id = R.string.date_of_birth_default)

    var showImages by remember {
        mutableStateOf(false)
    }
    var selectedImage by remember {
        mutableStateOf(currentImage)
    }
    var name by remember {
        mutableStateOf(currentName)
    }
    var status by remember {
        mutableStateOf(currentStatus)
    }
    var vk by remember {
        mutableStateOf(currentVK)
    }
    var instagram by remember {
        mutableStateOf(currentInstagram)
    }
    var city by remember {
        mutableStateOf(currentCity)
    }
    var birthday by remember {
        mutableStateOf(currentBirthday)
    }
    var dateDialogOpen by remember {
        mutableStateOf(false)
    }
    Log.d("Birthday", birthday)

    if (!showImages)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(end = 24.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.done),
                    contentDescription = "",
                    tint = Color.Blue,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(
                            onClick = {
                                var encodedImage = ""
                                if (selectedImage != "")
                                    encodedImage =
                                        encodeImage(selectedImage.toUri(), context)
                                val userDataBody = UserDataBody(
                                    name,
                                    profileData.value.profileData.userName ?: "",
                                    birthday,
                                    city,
                                    vk,
                                    instagram,
                                    status,
                                    Avatar(
                                        selectedImage,
                                        encodedImage
                                    )
                                )
                                dbViewModel.addProfileData(
                                    DBProfileData
                                        (
                                        profileData.value.profileData.phone ?: "",
                                        name,
                                        profileData.value.profileData.userName,
                                        birthday,
                                        city,
                                        status,
                                        vk,
                                        instagram,
                                        selectedImage
                                    )
                                )
                                viewModel.putProfileData(userDataBody, context)
                            }
                        )
                )
            }

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ){
                SetImage(
                    image = if (selectedImage != "") selectedImage else null,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                showImages = true
                            }
                        )
                        .size(150.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentScale = ContentScale.Crop
                )

                if (!dateDialogOpen)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(top = 20.dp)
                    ) {
                        OutlinedTextField(
                            maxLines = 1,
                            textStyle = TextStyle(
                                fontSize = 22.sp
                            ),
                            modifier = Modifier
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.enter_name),
                                    color = Color.LightGray
                                )
                            },
                            value = name,
                            onValueChange = {
                                name = it
                            }
                        )

                        OutlinedTextField(
                            maxLines = 1,
                            textStyle = TextStyle(
                                fontSize = 22.sp
                            ),
                            modifier = Modifier
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.personal_status),
                                    color = Color.LightGray
                                )
                            },
                            value = status,
                            onValueChange = {
                                status = it
                            }
                        )

                        OutlinedTextField(
                            maxLines = 1,
                            textStyle = TextStyle(
                                fontSize = 22.sp
                            ),
                            modifier = Modifier
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.vk),
                                    color = Color.LightGray
                                )
                            },
                            value = vk,
                            onValueChange = {
                                vk = it
                            }
                        )

                        OutlinedTextField(
                            maxLines = 1,
                            textStyle = TextStyle(
                                fontSize = 22.sp
                            ),
                            modifier = Modifier
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.instagram),
                                    color = Color.LightGray
                                )
                            },
                            value = instagram,
                            onValueChange = {
                                instagram = it
                            }
                        )

                        OutlinedTextField(
                            maxLines = 1,
                            textStyle = TextStyle(
                                fontSize = 22.sp
                            ),
                            modifier = Modifier
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.city),
                                    color = Color.LightGray
                                )
                            },
                            value = city,
                            onValueChange = {
                                city = it
                            }
                        )

                        OutlinedButton(
                            border = BorderStroke(3.dp, Color.Blue),
                            content = {
                                Text(
                                    text = stringResource(id = R.string.set_date_of_birth)
                                )
                            },
                            onClick = {
                                dateDialogOpen = true
                            }
                        )

                        Text(
                            fontSize = 22.sp,
                            text = getDate(birthday)
                        )

                        Spacer(modifier = Modifier.height(300.dp))
                    }
                else MyDatePickerDialog(
                    onDateSelected = {
                        birthday = it
                    },
                    onDismiss = {
                        dateDialogOpen = false
                    }
                )
            }
        }
    else ShowImages(
        viewModel,
        onDismiss = { showImages = false },
        onImageSelected = { selectedImage = it }
    )
}

@Composable
fun ShowImages(viewModel: MainViewModel, onDismiss: () -> Unit, onImageSelected: (String) -> Unit){
    val images = viewModel.externalStorageImages.collectAsState()
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(top = 20.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.backspace),
                contentDescription = "",
                tint = Color.Blue,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable(
                        onClick = {
                            onDismiss()
                        }
                    )
            )
            LazyVerticalGrid(
                modifier = Modifier.padding(20.dp),
                columns = GridCells.Fixed(3),
                content = {
                    items(images.value) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp)),
                        ){
                            SetImage(
                                image = if (it.toString() != "") it else null,
                                modifier = Modifier
                                    .clickable(
                                        onClick = {
                                            onImageSelected(it.toString())
                                            onDismiss()
                                        }
                                    )
                                    .size(150.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= System.currentTimeMillis()
        }
    })

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onDateSelected(selectedDate)
                onDismiss()
            }

            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(text = "Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomNavMenu(viewModel: MainViewModel, dbViewModel: DBViewModel, context: Context, phoneNumber: String){
    val items = listOf(
        BottomNavItem(
            title = "Профиль",
            selectedColor = Color.Blue,
            unselectedColor = Color.LightGray,
            icon = R.drawable.profile_icon
        ),
        BottomNavItem(
            title = "Чаты",
            selectedColor = Color.Blue,
            unselectedColor = Color.LightGray,
            icon = R.drawable.chats
        ),
        BottomNavItem(
            title = "Редактировать",
            selectedColor = Color.Blue,
            unselectedColor = Color.LightGray,
            icon = R.drawable.baseline_edit_24
        )
    )

    GetProfileData(viewModel, dbViewModel, context, phoneNumber)

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    val profileDataUploading = viewModel.profileDataUploading.collectAsState()
    if (profileDataUploading.value == LoadingState.Success)
        selectedItemIndex = 0

    Surface {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, bottomNavItem ->
                        NavigationBarItem(
                            selected = selectedItemIndex == index,
                            onClick = {
                                selectedItemIndex = index
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = bottomNavItem.icon),
                                    contentDescription = bottomNavItem.title,
                                    tint = if (selectedItemIndex == index) bottomNavItem.selectedColor else bottomNavItem.unselectedColor
                                )
                            },
                            label = {
                                Text(
                                    color = if (selectedItemIndex == index) bottomNavItem.selectedColor else bottomNavItem.unselectedColor,
                                    text = bottomNavItem.title
                                )
                            }
                        )
                    }
                }
            }
        ) {
                when(selectedItemIndex){
                    0 ->  Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GetProfileData(viewModel, dbViewModel, context, phoneNumber)
                        ProfileView(viewModel)
                    }
                    1 -> {}
                    2 -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GetProfileData(viewModel, dbViewModel, context, phoneNumber)
                        EditProfileView(viewModel, dbViewModel, context)
                    }
            }
        }
    }
}

@Composable
fun GetProfileData(viewModel: MainViewModel, dbViewModel: DBViewModel, context: Context, phoneNumber: String){
    LaunchedEffect(Unit){
        val dbProfileData = dbViewModel.getProfileData(phoneNumber)
        viewModel.getProfileData(dbProfileData, context)
    }
}

fun getImages(contentResolver: ContentResolver, viewModel: MainViewModel){
    val projection = arrayOf(
        MediaStore.Images.Media._ID
    )
    val selection = "${MediaStore.Images.Media._ID}"

    contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null
    )?.use {cursor ->
        val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val imagesUri = mutableListOf<Uri>()
        while (cursor.moveToNext()){
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            imagesUri.add(uri)
        }
        viewModel.externalStorageImagesGranted.value = true
        viewModel.externalStorageImages.value = imagesUri
        Log.d("Images", imagesUri.toString())
    }
}

@SuppressLint("SimpleDateFormat")
private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    return formatter.format(Date(millis))
}

@RequiresApi(Build.VERSION_CODES.O)
private fun convertDateToMillis(date: String): Long {
    val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE).atStartOfDay()
    val calendar = Calendar.getInstance()
    calendar.set(localDate.year, localDate.monthValue, localDate.dayOfMonth)
    return calendar.timeInMillis
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getDate(date: String): String{
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = convertDateToMillis(date)
    var day = calendar.get(Calendar.DAY_OF_MONTH).toString()
    if (day.length == 1)
        day = "0$day"
    var month = calendar.get(Calendar.MONTH).toString()
    if (month.length == 1)
        month = "0$month"
    return "$day.$month.${calendar.get(Calendar.YEAR)}"
}

fun encodeImage(imageUri: Uri, context: Context): String{
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArrayImage = stream.toByteArray()
    return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)
}
