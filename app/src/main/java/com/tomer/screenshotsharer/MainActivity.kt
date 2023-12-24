package com.tomer.screenshotsharer

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.tomer.screenshotsharer.prefs.KEY_SHOW_PREVIEW
import com.tomer.screenshotsharer.prefs.PrefsRepository
import com.tomer.screenshotsharer.prefs.PrefsRepositoryMock
import com.tomer.screenshotsharer.ui.theme.ScreenshotsharerTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
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
        Column(modifier = Modifier.padding(innerPadding)) {
            SwitchSetting(
                title = context.getString(R.string.enable_service),
                checked = viewModel.isAssistantEnabled
            ) {
                context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                Toast.makeText(
                    context,
                    "Select " + context.getString(R.string.app_name) + " as your assist app",
                    Toast.LENGTH_SHORT
                ).show()
            }
            SwitchSetting(
                title = context.getString(R.string.show_preview),
                checked = viewModel.showPreviewEnabled,
                onClick = {
                    viewModel.saveSetting(KEY_SHOW_PREVIEW, it)
                })
        }
    }
}

@Composable
fun SwitchSetting(title: String, checked: State<Boolean>, onClick: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
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

    fun checkAssistantState(context: Context) {
        isAssistantEnabled.value = isAssistant(context)
    }

    fun loadSettings() {
        showPreviewEnabled.value = prefsRepository.getBoolean(KEY_SHOW_PREVIEW, false)
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