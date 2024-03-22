package screens.home


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.seiko.imageloader.rememberImagePainter

import film_simulator.shared.generated.resources.Res
import film_simulator.shared.generated.resources.ic_image_add_24

import film_simulator.shared.generated.resources.select_image
import io.github.yahiaangelo.filmsimulator.FilmLut
import io.github.yahiaangelo.filmsimulator.data.source.network.GITHUB_BASE_URL
import io.github.yahiaangelo.filmsimulator.screens.settings.SettingsScreen
import io.github.yahiaangelo.filmsimulator.view.AppScaffold
import io.github.yahiaangelo.filmsimulator.view.ProgressDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


data class HomeScreen(
    val userMessage: Int = 0
): Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()
        val sheetState = rememberModalBottomSheetState()
        val navigator = LocalNavigator.currentOrThrow

        val vm = getScreenModel<HomeScreenModel>()
        val uiState by vm.uiState.collectAsState()


        val singleImagePicker = rememberImagePickerLauncher(
            selectionMode = SelectionMode.Single,
            scope = scope,
            onResult = vm::onImagePickerResult
        )



        AppScaffold(
            onVisibilityClick = vm::showOriginalImage,
            onImageChooseClick = { singleImagePicker.launch() },
            onImageResetClick = vm::resetImage,
            onSettingsClick = { navigator.push(SettingsScreen()) },
            onImageExportClick = vm::exportImage
            ) { innerPadding ->

           HomeContent(
               loading = uiState.isLoading,
               imageBitmap = uiState.image,
               selectedFilm = uiState.lut,
               loadingMessage = uiState.loadingMessage,
               onRefresh = vm::refresh,
               onImageChooseClick = { singleImagePicker.launch() },
               onFilmBoxClick = vm::showFilmLutsBottomSheet,
               modifier = Modifier.padding(innerPadding)
           )
        }

        FilmLutsListBottomSheet(sheetState = sheetState, showBottomSheet = uiState.showBottomSheet, filmLuts = uiState.filmLutsList, onDismissRequest = vm::dismissFilmLutBottomSheet, onItemClick = vm::selectFilmLut)


        uiState.userMessage?.let { message ->
            LaunchedEffect(scaffoldState, vm, message, message) {
                scaffoldState.snackbarHostState.showSnackbar(message)
                vm.snackbarMessageShown()
            }
        }

    }

    @Composable
    private fun HomeContent(
        loading: Boolean,
        imageBitmap: ImageBitmap?,
        selectedFilm : FilmLut?,
        loadingMessage: String,
        onRefresh: () -> Unit,
        onImageChooseClick: () -> Unit,
        onFilmBoxClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {

        if (loading) ProgressDialog(loadingMessage = loadingMessage)

        Column(modifier = modifier) {

            Spacer(modifier = Modifier.size(23.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard (modifier = Modifier.align(Alignment.Center).size(360.dp, 360.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        imageBitmap?.let {
                            Image(modifier = Modifier.fillMaxSize(), bitmap = imageBitmap, contentDescription =  null)
                        } ?: IconButton(modifier = Modifier.align(Alignment.Center).size(150.dp), onClick = onImageChooseClick) {
                            Column {
                                Icon(painter = painterResource(Res.drawable.ic_image_add_24),
                                    null,
                                    modifier = Modifier.size(65.dp, 65.dp).align(Alignment.CenterHorizontally),
                                )
                                Text(text = stringResource(Res.string.select_image),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                    }

                }
            }
            Spacer(modifier = Modifier.size(23.dp))

            Divider(modifier = Modifier.padding(46.dp, 0.dp))

            Spacer(modifier = Modifier.size(23.dp))

            FilmLutBox(modifier = Modifier.fillMaxWidth(), selectedFilm = selectedFilm, onFilmBoxClick = onFilmBoxClick)


        }
    }

    @Composable
    private fun FilmLutBox(modifier: Modifier = Modifier, selectedFilm: FilmLut?, onFilmBoxClick: () -> Unit) {
        Box(modifier = modifier) {
            OutlinedCard(
                modifier = Modifier.align(Alignment.Center).size(360.dp, 80.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = CardDefaults.outlinedCardBorder(),
                onClick = onFilmBoxClick
            ) {

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Film",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 2.dp)
                    )

                    Text(text = selectedFilm?.name ?: "Select Your Film",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 2.dp)
                    )
                }
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FilmLutsListBottomSheet(sheetState: SheetState, showBottomSheet: Boolean, filmLuts: List<FilmLut>, onDismissRequest: () -> Unit, onItemClick: (film: FilmLut) -> Unit) {

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                sheetState = sheetState
            ) {
                FilmLutsList(filmLuts = filmLuts, onItemClick = onItemClick)
            }
        }

    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun FilmLutsList(filmLuts: List<FilmLut>, onItemClick: (film: FilmLut) -> Unit) {
        // State to hold the current search query
        var searchQuery by remember { mutableStateOf("") }

        // Filter filmLuts based on the search query
        val filteredFilmLuts = filmLuts.filter {
            searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
        }

        // Group the filtered list by category
        val sortedAndGrouped = filteredFilmLuts.groupBy { it.category }

        Column {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            // List
            LazyColumn {
                sortedAndGrouped.forEach { (category, films) ->
                    stickyHeader {
                        CategoryHeader(category)
                    }
                    items(films) { film ->
                        FilmItem(film = film, onItemClick = onItemClick)
                    }
                }
            }
        }
    }



    @Composable
    fun CategoryHeader(category: String) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 3.dp)
                    .fillMaxWidth()
            )
        }

    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun FilmItem(film: FilmLut, onItemClick: (film: FilmLut) -> Unit) {
        Surface(color = MaterialTheme.colorScheme.surface, onClick = {onItemClick(film)}) {
            Row(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
                Image(
                    painter = rememberImagePainter(GITHUB_BASE_URL + film.image_url),
                    contentDescription = film.name,
                    modifier = Modifier.height(64.dp).width(114.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = film.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

    }

}

