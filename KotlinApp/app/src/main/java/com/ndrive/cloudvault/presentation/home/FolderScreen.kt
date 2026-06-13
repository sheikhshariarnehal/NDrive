package com.ndrive.cloudvault.presentation.home

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ndrive.cloudvault.BuildConfig
import com.ndrive.cloudvault.presentation.common.resolveFileIconStyle
import com.ndrive.cloudvault.presentation.home.components.CreateNewBottomSheet
import com.ndrive.cloudvault.presentation.home.components.FileCard
import com.ndrive.cloudvault.presentation.home.components.FileRow
import com.ndrive.cloudvault.presentation.home.components.FolderCard
import com.ndrive.cloudvault.presentation.home.components.FolderGridCard
import com.ndrive.cloudvault.presentation.home.components.GridListToggle
import com.ndrive.cloudvault.presentation.home.components.NDriveBottomNav
import com.ndrive.cloudvault.presentation.upload.UploadProgressOverlay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    navController: NavController,
    folderId: String,
    viewModel: FolderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isGridView by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val openDocumentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
    ) { fileUris ->
        if (fileUris.isNotEmpty()) {
            fileUris.forEach { fileUri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
            viewModel.uploadFiles(fileUris)
        }
    }

    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            bitmapToCacheUri(context, bitmap)?.let { cachedUri ->
                viewModel.uploadFiles(listOf(cachedUri))
            }
        }
    }

    LaunchedEffect(folderId) {
        viewModel.loadFolder(folderId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .clickable { showCreateSheet = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "New",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                }
            }
        },
        bottomBar = { NDriveBottomNav(navController = navController) },
    ) { paddingValues ->
        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isGridView) 2 else 1),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(if (isGridView) 16.dp else 0.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // 1. Top App Bar Style Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                val popped = navController.popBackStack()
                                if (!popped) {
                                    navController.navigate("files") {
                                        launchSingleTop = true
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }

                            Text(
                                text = uiState.currentFolder?.name ?: "Folder",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(onClick = { navController.navigate("search") }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                )
                            }

                            IconButton(onClick = { /* Menu */ }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                )
                            }
                        }

                        // 2. View Controls / Sort header row (Like the Google Drive screenshot)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "Name ^" button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { /* Sort */ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Name",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Sort ascending",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // View toggle (List vs Grid)
                            GridListToggle(
                                isGridView = isGridView,
                                onToggle = { isGridView = !isGridView },
                            )
                        }
                    }
                }

                if (uiState.uploadPanel.isVisible) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UploadProgressOverlay(
                            panel = uiState.uploadPanel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            onToggleExpanded = { viewModel.toggleUploadPanelExpanded() },
                            onDismissAll = { viewModel.clearUploadState() },
                            onDismissItem = { itemId -> viewModel.dismissUploadItem(itemId) },
                        )
                    }
                }

                uiState.errorMessage?.let { errorMessage ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(onClick = { viewModel.refresh() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    items(
                        count = 8,
                        key = { index -> "loading-$index" }
                    ) { index ->
                        if (isGridView) {
                            Box(
                                modifier = Modifier.padding(
                                    start = if (index % 2 == 0) 16.dp else 8.dp,
                                    end = if (index % 2 == 0) 8.dp else 16.dp
                                )
                            ) {
                                FileCard(name = "", isLoading = true) { }
                            }
                        } else {
                            if (index < 3) {
                                FolderCard(
                                    name = "",
                                    subtitle = "",
                                    isLoading = true,
                                ) { }
                            } else {
                                FileRow(name = "", subtitle = "", isLoading = true) { }
                            }
                        }
                    }
                } else {
                    if (uiState.folders.isNotEmpty()) {
                        items(
                            count = uiState.folders.size,
                            key = { index -> uiState.folders[index].id }
                        ) { index ->
                            val folder = uiState.folders[index]
                            if (isGridView) {
                                Box(
                                    modifier = Modifier.padding(
                                        start = if (index % 2 == 0) 16.dp else 8.dp,
                                        end = if (index % 2 == 0) 8.dp else 16.dp
                                    )
                                ) {
                                    FolderGridCard(name = folder.name) {
                                        navController.navigate("folder/${Uri.encode(folder.id)}")
                                    }
                                }
                            } else {
                                FolderCard(
                                    name = folder.name,
                                    subtitle = "Modified ${folder.updatedAt?.take(10) ?: "Unknown"}",
                                    iconTint = Color(0xFF5F6368),
                                ) {
                                    navController.navigate("folder/${Uri.encode(folder.id)}")
                                }
                            }
                        }
                    }

                    if (uiState.files.isNotEmpty()) {
                        items(
                            count = uiState.files.size,
                            key = { index -> uiState.files[index].id }
                        ) { index ->
                            val file = uiState.files[index]
                            val fileIconStyle = resolveFileIconStyle(file.name, file.mimeType)
                            if (isGridView) {
                                val globalIndex = uiState.folders.size + index
                                Box(
                                    modifier = Modifier.padding(
                                        start = if (globalIndex % 2 == 0) 16.dp else 8.dp,
                                        end = if (globalIndex % 2 == 0) 8.dp else 16.dp
                                    )
                                ) {
                                    FileCard(
                                        name = file.name,
                                        thumbnailUrl = file.thumbnailUrl,
                                        isImage = fileIconStyle.prefersMediaPreview,
                                        fileTypeIcon = fileIconStyle.icon,
                                        fileTypeTint = fileIconStyle.tint,
                                    ) {
                                        navController.navigate("preview/${Uri.encode(file.id)}")
                                    }
                                }
                            } else {
                                FileRow(
                                    name = file.name,
                                    subtitle = "Modified ${file.updatedAt?.take(10) ?: "Unknown"}",
                                    iconTint = fileIconStyle.tint,
                                    iconVector = fileIconStyle.icon,
                                ) {
                                    navController.navigate("preview/${Uri.encode(file.id)}")
                                }
                            }
                        }
                    }

                    if (uiState.folders.isEmpty() && uiState.files.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "This folder is empty",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Upload files into this folder to see them here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateNewBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showCreateSheet = false },
            onFolderClick = {
                showCreateSheet = false
                showCreateFolderDialog = true
            },
            onUploadClick = {
                showCreateSheet = false
                openDocumentsLauncher.launch(arrayOf("*/*"))
            },
            onScanClick = {
                showCreateSheet = false
                takePicturePreviewLauncher.launch(null)
            },
        )
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createFolder(newFolderName)
                    newFolderName = ""
                    showCreateFolderDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (uiState.showTelegramConnectPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTelegramConnectPrompt() },
            title = { Text("Connect Telegram") },
            text = {
                Text(
                    "Upload needs your Telegram account connection. Connect now to continue uploading files.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissTelegramConnectPrompt()
                        navController.navigate("profile_route?openTelegramDialog=true") {
                            launchSingleTop = true
                        }
                    },
                ) {
                    Text("Connect now")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissTelegramConnectPrompt() }) {
                    Text("Not now")
                }
            },
        )
    }
}

private fun bitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    val cacheDir = File(context.cacheDir, "scan-cache").apply { mkdirs() }
    val outputFile = File(cacheDir, "scan_${System.currentTimeMillis()}.jpg")

    return runCatching {
        FileOutputStream(outputFile).use { stream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                throw IOException("Unable to save scan image")
            }
        }
        FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            outputFile,
        )
    }.getOrNull()
}
