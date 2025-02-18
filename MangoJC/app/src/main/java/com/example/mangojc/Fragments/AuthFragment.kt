package com.example.mangojc.Fragments


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.arpitkatiyarprojects.countrypicker.CountryPicker
import com.arpitkatiyarprojects.countrypicker.CountryPickerOutlinedTextField
import com.arpitkatiyarprojects.countrypicker.models.CountryDetails
import com.arpitkatiyarprojects.countrypicker.models.CountryPickerProperties
import com.arpitkatiyarprojects.countrypicker.models.PickerTextStyles
import com.arpitkatiyarprojects.countrypicker.utils.CountryPickerUtils
import com.example.mangojc.Data.CCP
import com.example.mangojc.Data.VerifCodeElement
import com.example.mangojc.R
import com.example.mangojc.Repository.Repository
import com.example.mangojc.ViewModels.MainViewModel
import kotlinx.coroutines.cancel

class AuthFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private val rep = Repository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            val ccp = CCP(context = requireContext())
            EditPhone(viewModel, requireContext(), ccp, rep, findNavController())
        }
        return view
    }
}

@Composable
fun EditPhone(viewModel: MainViewModel, context: Context, ccp: CCP, rep: Repository, navController: NavController){
    val loading = viewModel.loading.collectAsState()

    var number by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 20.dp, bottom = 20.dp)
            .fillMaxWidth()
    ) {
        CountryPickerOutlinedTextField(

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.new_product_blue),
                unfocusedBorderColor = colorResource(R.color.text_grey_composable),
                cursorColor = colorResource(R.color.text_black_composable),
            ),
            textStyle = TextStyle(
                fontSize = 22.sp
            ),
            pickerTextStyles = PickerTextStyles(
                countryPhoneCodeTextStyle = TextStyle(fontSize = 22.sp)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            defaultCountryCode = ccp.selectedCountryNameCode,
            mobileNumber = number,
            onMobileNumberChange = {
                number = it
                ccp.setText(it)
                viewModel.phoneNumberValid.value = ccp.getPhoneNumberValidity()
            },
            onCountrySelected = {
                ccp.setCountryForNameCode(it.countryCode)
            },
            onDone = {
                if (ccp.getPhoneNumberValidity()){
                    if (!loading.value)
                        viewModel.checkUserPhone(ccp.fullNumber)
                }
                else Toast.makeText(context, context.getString(R.string.incorrect_number), Toast.LENGTH_SHORT).show()
            }
        )
        EditCode(viewModel, context)
        Text(
            fontSize = 18.sp,
            color = Color.Red,
            text = stringResource(id = R.string.disclaimer),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        GetAuthData(
            viewModel,
            ccp.fullNumberWithPlus,
            context,
            rep,
            navController,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditCode(viewModel: MainViewModel, context: Context){
    val success = viewModel.isSuccess.collectAsState()
    if (success.value.isSuccess){
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ){
            val focusManager = LocalFocusManager.current
            val (a, b, c, d, e, f) = FocusRequester.createRefs()
            for (i in 0..5){
                when(i){
                    0 ->{
                        val modifier = Modifier
                            .focusRequester(a)
                            .focusProperties {
                                next = b
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }

                    1 ->{
                        val modifier = Modifier
                            .focusRequester(b)
                            .focusProperties {
                                next = c
                                previous = a
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }

                    2 ->{
                        val modifier = Modifier
                            .focusRequester(c)
                            .focusProperties {
                                next = d
                                previous = b
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }

                    3 ->{
                        val modifier = Modifier
                            .focusRequester(d)
                            .focusProperties {
                                next = e
                                previous = c
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }

                    4 ->{
                        val modifier = Modifier
                            .focusRequester(e)
                            .focusProperties {
                                next = f
                                previous = d
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }

                    5 ->{
                        val modifier = Modifier
                            .focusRequester(f)
                            .focusProperties {
                                previous = e
                            }
                        CodeCellView(i, viewModel, modifier, focusManager)
                    }
                }
            }
        }
    }
}

@Composable
fun CodeCellView(id: Int, viewModel: MainViewModel, modifier: Modifier, focusMan: FocusManager){
    val verifCodeElements = viewModel.verifCodeElements.collectAsState()
    var digit by remember {
        mutableStateOf("")
    }

    if (verifCodeElements.value.isEmpty())
        digit = ""

    LaunchedEffect(Unit){
        focusMan.moveFocus(FocusDirection.Down)
        focusMan.moveFocus(FocusDirection.Left)
    }

    OutlinedTextField(
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(R.color.new_product_blue),
            unfocusedBorderColor = colorResource(R.color.text_grey_composable),
            cursorColor = colorResource(R.color.text_black_composable),
        ),
        maxLines = 1,
        textStyle = TextStyle(
            letterSpacing = TextUnit(0f, TextUnitType(0)),
            textAlign = TextAlign.Center,
            fontSize = 30.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier
            .size(width = 55.dp, height = 65.dp)
            .padding(start = 3.dp, end = 3.dp),
        value = digit,
        onValueChange = {
            if (it.length <= 1)
                digit = it

            when(it.length){
                0 ->{
                    viewModel.verifCodeElements.value.forEach { codeElement ->
                        if (codeElement.id == id)
                            viewModel.verifCodeElements.value = listOf()
                    }
                   repeat(6){
                       focusMan.moveFocus(FocusDirection.Left)
                   }
                }

                1 -> {
                    val codeElements = getCodeElementsList(viewModel)
                    val newCodeElement = VerifCodeElement(id, it)
                    if (!codeElements.contains(newCodeElement)){
                        codeElements.add(newCodeElement)
                        viewModel.verifCodeElements.value = codeElements
                    }
                    if (id != 5)
                        focusMan.moveFocus(FocusDirection.Next)
                }
            }
        }
    )
}

@Composable
fun GetAuthData(viewModel: MainViewModel, phoneNumber: String, context: Context, rep: Repository, navController: NavController){
    val verifCodeElements = viewModel.verifCodeElements.collectAsState()
    var verifCode = ""
    if (verifCodeElements.value.size == 6){
        val codeDigitsArray = IntArray(6)
        verifCodeElements.value.forEach {
            codeDigitsArray[it.id] = it.cellContent.toInt()
        }
        codeDigitsArray.forEach {
            verifCode += it.toString()
        }
        if (verifCode == stringResource(id = R.string.verification_code)){
            viewModel.sendUserAuthData(phoneNumber, verifCode)
            HandleAuthData(viewModel, context, rep, navController, phoneNumber)
        }
    }
}

@Composable
fun HandleAuthData(viewModel: MainViewModel, context: Context, rep: Repository, navController: NavController, phoneNumber: String){
    val userAuthData = viewModel.userAuthData.collectAsState()
    val userAuthDataLoading = viewModel.authLoading.collectAsState()
    if (userAuthData.value.is_user_exists == true){
        rep.saveAccessToken(context, userAuthData.value.access_token.toString())
        rep.saveRefreshToken(context, userAuthData.value.refresh_token.toString())
        val bundle = bundleOf(stringResource(id = R.string.phone_number_key) to phoneNumber)
        if (!userAuthDataLoading.value)
            navController.navigate(R.id.action_authFragment_to_profileFragment, bundle)
    }
    else{
        val bundle = bundleOf(stringResource(id = R.string.phone_number_key) to phoneNumber)
        if (!userAuthDataLoading.value)
            navController.navigate(R.id.action_authFragment_to_regFragment, bundle)
    }
}


fun getCodeElementsList(viewModel: MainViewModel): MutableList<VerifCodeElement>{
    val codeElements = mutableListOf<VerifCodeElement>()
    viewModel.verifCodeElements.value.forEach{ codeElement ->
        codeElements.add(codeElement)
    }
    return codeElements
}
