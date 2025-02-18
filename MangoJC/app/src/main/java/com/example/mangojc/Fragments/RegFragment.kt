package com.example.mangojc.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.blue
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.arpitkatiyarprojects.countrypicker.CountryPickerOutlinedTextField
import com.arpitkatiyarprojects.countrypicker.models.PickerTextStyles
import com.arpitkatiyarprojects.countrypicker.utils.CountryPickerUtils
import com.example.mangojc.App
import com.example.mangojc.Data.DBProfileData
import com.example.mangojc.R
import com.example.mangojc.Repository.Repository
import com.example.mangojc.ViewModels.DBViewModel
import com.example.mangojc.ViewModels.MainViewModel

class RegFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val userDao = (requireActivity().application as App).db.userDao()
                return DBViewModel(userDao) as T
            }
        }
    }
    private val rep = Repository()
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneNumber = it.getString(resources.getString(R.string.phone_number_key))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            phoneNumber?.let { Registration(it, viewModel, dBViewModel, rep, requireContext(), findNavController()) }
        }
        return view
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Registration(phoneNumber: String, viewModel: MainViewModel, dbViewModel: DBViewModel, rep: Repository, context: Context, navController: NavController){
    val regLoading = viewModel.regLoading.collectAsState()
    val pattern = Regex("^[a-zA-Z0-9_-]*\$")
    var name by remember {
        mutableStateOf("")
    }
    var userName by remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    val (a, b) = FocusRequester.createRefs()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 20.dp),
            text = stringResource(id = R.string.registration),
            fontSize = 36.sp
        )
        
        Text(
            modifier = Modifier
                .padding(bottom = 20.dp),
            text = phoneNumber,
            fontSize = 30.sp
        )

        OutlinedTextField(
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.new_product_blue),
                unfocusedBorderColor = colorResource(R.color.text_grey_composable),
                cursorColor = colorResource(R.color.text_black_composable),
            ),
            maxLines = 1,
            textStyle = TextStyle(
                fontSize = 22.sp
            ),
            modifier = Modifier
                .focusRequester(a)
                .focusProperties {
                    next = b
                }
                .padding(bottom = 16.dp),
            placeholder = {
                Text(text = stringResource(id = R.string.enter_name))
            },
            value = name,
            onValueChange = {
                name = it
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                focusManager.moveFocus(FocusDirection.Next)
            })
        )

        OutlinedTextField(
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.new_product_blue),
                unfocusedBorderColor = colorResource(R.color.text_grey_composable),
                cursorColor = colorResource(R.color.text_black_composable),
            ),
            maxLines = 1,
            textStyle = TextStyle(
                fontSize = 22.sp
            ),
            modifier = Modifier
                .focusRequester(b)
                .focusProperties {
                    previous = a
                }
                .padding(bottom = 16.dp),
            placeholder = {
                Text(text = stringResource(id = R.string.enter_nick_name))
            },
            value = userName,
            onValueChange = {
                if (pattern.matches(it))
                    userName = it
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )
        if (name != "" && userName != ""){
            Button(
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.new_product_blue),
                ),
                onClick = {
                if (!regLoading.value)
                    viewModel.sendUserRegData(phoneNumber, name, userName)
            }) {
                Text(
                    color = colorResource(R.color.white),
                    text = stringResource(id = R.string.register)
                )
            }
        }
    }
    HandleRegData(viewModel, dbViewModel, rep, context, navController, phoneNumber, name, userName)
}

@Composable
fun HandleRegData(viewModel: MainViewModel, dbViewModel: DBViewModel, rep: Repository, context: Context, navController: NavController, phoneNumber: String, name: String, userName: String){
    val regLoading = viewModel.regLoading.collectAsState()
    val regData = viewModel.userRegData.collectAsState()
    val dbBusy = dbViewModel.dbBusy.collectAsState()

    if (regData.value.access_token != ""){
        rep.saveAccessToken(context, regData.value.access_token.toString())
        rep.saveRefreshToken(context, regData.value.refresh_token.toString())
        dbViewModel.addProfileData(DBProfileData(phoneNumber, name, userName))
        val bundle = bundleOf(stringResource(id = R.string.phone_number_key) to phoneNumber)
        if (!regLoading.value && !dbBusy.value)
            navController.navigate(R.id.action_regFragment_to_profileFragment, bundle)
    }
}