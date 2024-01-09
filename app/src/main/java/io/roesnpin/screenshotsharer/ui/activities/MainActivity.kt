package io.rosenpin.screenshotsharer.ui.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.rosenpin.screenshotsharer.R
import io.rosenpin.screenshotsharer.assist.AssistLoggerService
import io.rosenpin.screenshotsharer.prefs.KEY_SAVE_SCREENSHOT
import io.rosenpin.screenshotsharer.prefs.KEY_SHOW_PREVIEW
import io.rosenpin.screenshotsharer.prefs.PrefsRepository
import io.rosenpin.screenshotsharer.prefs.PrefsRepositoryMock
import io.rosenpin.screenshotsharer.ui.theme.ScreenshotsharerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadSettings()
        setContent {
            ScreenshotsharerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAssistantState(this)
    }
}

fun isAssistant(context: Context): Boolean {
    val currentAssistant =
        Settings.Secure.getString(
            context.contentResolver,
            "voice_interaction_service"
        )
    return currentAssistant != null && (currentAssistant == context.packageName + "/." + AssistLoggerService::class.java.simpleName || currentAssistant.contains(
        context.packageName
    ))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(viewModel: MainViewModel) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animation),
        imageAssetsFolder = "images"
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        useCompositionFrameRate = true
    )
    val openDialog = remember { mutableStateOf(!isAssistant(context)) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            content = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(16.dp)
                        )) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = context.getString(R.string.enable_service_step_1),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = context.getString(R.string.enable_service_step_1_desc),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Image(
                            painter = painterResource(id = R.drawable.step1),
                            contentDescription = "My Image"
                        )
                        Box(modifier = Modifier.height(16.dp))
                        Divider()
                        Box(modifier = Modifier.height(16.dp))
                        Text(
                            text = context.getString(R.string.enable_service_step_2),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = context.getString(R.string.enable_service_step_2_desc),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Image(
                            painter = painterResource(id = R.drawable.step2),
                            contentDescription = "My Image"
                        )
                        Box(modifier = Modifier.height(16.dp))
                        Divider()
                        Box(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            openDialog.value = false
                            context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                            Toast.makeText(
                                context,
                                "Select " + context.getString(R.string.app_name) + " as your assist app",
                                Toast.LENGTH_LONG
                            ).show()
                        }) {
                            Text(text = context.getString(R.string.ready))
                        }
                    }
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(context.getString(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/rosenpin/Screenshot-Sharer")
                            )
                            context.startActivity(intent)
                        } catch (ignored: ActivityNotFoundException) {
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = "GitHub"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           Box(
               Modifier
                   .height(350.dp)
                   .padding(16.dp)
                   .background(Color.Black, shape = RoundedCornerShape(16.dp))
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
            }
            Divider()
            SwitchSetting(
                title = context.getString(R.string.enable_service),
                description = context.getString(R.string.enable_service_description),
                checked = viewModel.isAssistantEnabled
            ) {
                openDialog.value = true
            }
            Divider()
            SwitchSetting(
                title = context.getString(R.string.show_preview),
                description = context.getString(R.string.show_preview_description),
                checked = viewModel.showPreviewEnabled,
                onClick = {
                    viewModel.saveSetting(KEY_SHOW_PREVIEW, it)
                })
            Divider()
            SwitchSetting(
                title = context.getString(R.string.save_screenshot),
                description = context.getString(R.string.save_screenshot_description),
                checked = viewModel.saveScreenshot,
                onClick = {
                    viewModel.saveSetting(KEY_SAVE_SCREENSHOT, it)
                })
            Divider()
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    description: String,
    checked: State<Boolean>,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.Center, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(
            checked = checked.value, onCheckedChange = onClick
        )
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefsRepository: PrefsRepository
) : ViewModel() {

    val isAssistantEnabled = mutableStateOf(false)
    val showPreviewEnabled = mutableStateOf(false)
    val saveScreenshot = mutableStateOf(true)

    fun checkAssistantState(context: Context) {
        isAssistantEnabled.value = isAssistant(context)
    }

    fun loadSettings() {
        showPreviewEnabled.value = prefsRepository.getBoolean(KEY_SHOW_PREVIEW, false)
        saveScreenshot.value = prefsRepository.getBoolean(KEY_SAVE_SCREENSHOT, true)
    }

    fun saveSetting(key: String, value: Boolean) {
        prefsRepository.setSetting(key, value)
        loadSettings()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScreenshotsharerTheme {
        Content(MainViewModel(PrefsRepositoryMock()))
    }
}