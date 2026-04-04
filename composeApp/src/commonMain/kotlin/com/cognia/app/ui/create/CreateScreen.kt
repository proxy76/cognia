package com.cognia.app.ui.create

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateScreen() {
    val uploadViewModel: UploadViewModel = viewModel { UploadViewModel() }
    UploadScreen(viewModel = uploadViewModel)
}
