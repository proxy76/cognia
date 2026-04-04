package com.cognia.app.ui.create

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UploadViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateHasEmptyFields() {
        val viewModel = UploadViewModel()
        val state = viewModel.state.value

        assertNull(state.selectedFileName)
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.selectedCategoryId)
        assertNull(state.selectedDifficulty)
        assertFalse(state.isLicensedCreator)
        assertNull(state.titleError)
        assertNull(state.categoryError)
        assertNull(state.fileError)
        assertFalse(state.isUploading)
        assertEquals(0f, state.uploadProgress)
        assertFalse(state.uploadSuccess)
        assertNull(state.error)
    }

    @Test
    fun selectFileUpdatesSelectedFileName() {
        val viewModel = UploadViewModel()

        viewModel.selectFile("my_video.mp4")

        assertEquals("my_video.mp4", viewModel.state.value.selectedFileName)
    }

    @Test
    fun updateTitleUpdatesState() {
        val viewModel = UploadViewModel()

        viewModel.updateTitle("My Video Title")

        assertEquals("My Video Title", viewModel.state.value.title)
    }

    @Test
    fun updateDescriptionUpdatesState() {
        val viewModel = UploadViewModel()

        viewModel.updateDescription("A great video about science")

        assertEquals("A great video about science", viewModel.state.value.description)
    }

    @Test
    fun selectCategoryUpdatesState() {
        val viewModel = UploadViewModel()

        viewModel.selectCategory("3")

        assertEquals("3", viewModel.state.value.selectedCategoryId)
    }

    @Test
    fun selectDifficultyUpdatesState() {
        val viewModel = UploadViewModel()

        viewModel.selectDifficulty("HARD")

        assertEquals("HARD", viewModel.state.value.selectedDifficulty)
    }

    @Test
    fun uploadWithMissingTitleShowsError() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.selectCategory("1")

        viewModel.upload()

        assertEquals("Title is required", viewModel.state.value.titleError)
        assertFalse(viewModel.state.value.isUploading)
        assertFalse(viewModel.state.value.uploadSuccess)
    }

    @Test
    fun uploadWithMissingCategoryShowsError() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.updateTitle("My Title")

        viewModel.upload()

        assertEquals("Please select a category", viewModel.state.value.categoryError)
        assertFalse(viewModel.state.value.uploadSuccess)
    }

    @Test
    fun uploadWithMissingFileShowsError() {
        val viewModel = UploadViewModel()
        viewModel.updateTitle("My Title")
        viewModel.selectCategory("1")

        viewModel.upload()

        assertEquals("Please select a file", viewModel.state.value.fileError)
        assertFalse(viewModel.state.value.uploadSuccess)
    }

    @Test
    fun uploadWithValidDataSetsSuccess() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.updateTitle("My Title")
        viewModel.selectCategory("1")

        viewModel.upload()

        assertFalse(viewModel.state.value.isUploading)
        assertTrue(viewModel.state.value.uploadSuccess)
        assertEquals(1f, viewModel.state.value.uploadProgress)
    }

    @Test
    fun clearStateResetsEverything() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.updateTitle("My Title")
        viewModel.updateDescription("Desc")
        viewModel.selectCategory("1")
        viewModel.selectDifficulty("EASY")

        viewModel.clearState()

        val state = viewModel.state.value
        assertNull(state.selectedFileName)
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertNull(state.selectedCategoryId)
        assertNull(state.selectedDifficulty)
        assertFalse(state.uploadSuccess)
    }

    @Test
    fun updateTitleClearsTitleError() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.selectCategory("1")
        viewModel.upload() // triggers title error

        assertEquals("Title is required", viewModel.state.value.titleError)

        viewModel.updateTitle("Now has title")

        assertNull(viewModel.state.value.titleError)
    }

    @Test
    fun selectFileClearsFileError() {
        val viewModel = UploadViewModel()
        viewModel.updateTitle("Title")
        viewModel.selectCategory("1")
        viewModel.upload() // triggers file error

        assertEquals("Please select a file", viewModel.state.value.fileError)

        viewModel.selectFile("video.mp4")

        assertNull(viewModel.state.value.fileError)
    }

    @Test
    fun selectCategoryClearsCategoryError() {
        val viewModel = UploadViewModel()
        viewModel.selectFile("video.mp4")
        viewModel.updateTitle("Title")
        // Don't select category
        viewModel.upload()

        assertEquals("Please select a category", viewModel.state.value.categoryError)

        viewModel.selectCategory("2")

        assertNull(viewModel.state.value.categoryError)
    }
}
