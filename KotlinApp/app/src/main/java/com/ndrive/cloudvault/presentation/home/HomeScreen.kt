package com.ndrive.cloudvault.presentation.home

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ndrive.cloudvault.BuildConfig
import com.ndrive.cloudvault.presentation.common.rememberShimmerColors
import com.ndrive.cloudvault.presentation.common.resolveFileIconStyle
import com.ndrive.cloudvault.presentation.home.components.AppDrawer
import com.ndrive.cloudvault.presentation.home.components.CreateNewBottomSheet
import com.ndrive.cloudvault.presentation.home.components.FileCard
import com.ndrive.cloudvault.presentation.home.components.FileRow
import com.ndrive.cloudvault.presentation.home.components.FolderCard
import com.ndrive.cloudvault.presentation.home.components.FolderGridCard
import com.ndrive.cloudvault.presentation.home.components.GridListToggle
import com.ndrive.cloudvault.presentation.home.components.NDriveBottomNav
import com.ndrive.cloudvault.presentation.home.components.TopSearchBar
import com.ndrive.cloudvault.presentation.upload.UploadProgressOverlay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/** Pre-computed mapping of folder color string → Color value. */
private val folderColorMap = mapOf(
    "red" to Color(0xFFEA4335),
    "yellow" to Color(0xFFFBBC04),
    "orange" to Color(0xFFFBBC04),
    "blue" to Color(0xFF4285F4),
    "green" to Color(0xFF34A853),
    "dark" to Color(0xFF5F6368),
    "gray" to Color(0xFF5F6368),
    "darkgray" to Color(0xFF5F6368),
)
private val defaultFolderColor = Color(0xFF5F6368)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    var isGridView by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showAppDrawer by remember { mutableStateOf(false) }
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect pre-filtered lists from ViewModel (avoids recomputing in composition)
    val visibleFolders by viewModel.visibleFolders.collectAsStateWithLifecycle()
    val visibleFiles by viewModel.visibleFiles.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh(forceRefreshUserData = true)
        }
    }

    // Stable navigation callbacks — remembered so they don't break Compose's
    // lambda equality check and cause downstream recompositions.
    val navigateToPreview: (String) -> Unit = remember {
        { fileId: String -> navController.navigate("preview/${Uri.encode(fileId)}") }
    }
    val navigateToFolder: (String) -> Unit = remember {
        { folderId: String -> navController.navigate("folder/${Uri.encode(folderId)}") }
    }

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

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .statusBarsPadding(),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TopSearchBar(
                    onMenuClick = { showAppDrawer = true },
                    onProfileClick = { navController.navigate("profile_route") },
                    isTelegramConnected = uiState.isTelegramConnected,
                    profileAvatarUrl = uiState.profileAvatarUrl,
                    profileInitial = uiState.profileInitial,
                    onSearchClick = { navController.navigate("search") },
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = backgroundColor,
                    contentColor = primaryColor,
                    divider = {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = primaryColor,
                        )
                    },
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "Suggested",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTabIndex == 0) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "Activity",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTabIndex == 1) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }
        },
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
        bottomBar = { NDriveBottomNav(navController) },
    ) { padding ->
        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Hoist shimmer animation to the list level so all loading items
            // share a single animation driver instead of each creating its own.
            val shimmerColors = rememberShimmerColors()
            val shimmerTransition = rememberInfiniteTransition(label = "list_shimmer")
            val shimmerProgress by shimmerTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "list_shimmer_progress",
            )

        LazyVerticalGrid(
            columns = if (isGridView) GridCells.Fixed(2) else GridCells.Fixed(1),
            contentPadding = PaddingValues(
                start = if (isGridView) 16.dp else 0.dp,
                end = if (isGridView) 16.dp else 0.dp,
                top = 0.dp,
                bottom = 88.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(if (isGridView) 12.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(if (isGridView) 12.dp else 0.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(
                key = "header",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "header",
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isGridView) 0.dp else 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GridListToggle(isGridView = isGridView, onToggle = { isGridView = !isGridView })
                }
            }

            uiState.errorMessage?.let { errorMessage ->
                item(
                    key = "error",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "error",
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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

            if (uiState.uploadPanel.isVisible) {
                item(
                    key = "upload_panel",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "upload_panel",
                ) {
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

            if (uiState.isLoading) {
                items(
                    count = 10,
                    key = { index -> "loading-$index" },
                    contentType = { if (isGridView) "loading-grid" else "loading-list" },
                ) {
                    if (isGridView) {
                        FileCard(
                            name = "",
                            isLoading = true,
                            shimmerProgress = shimmerProgress,
                            shimmerColors = shimmerColors,
                        ) { }
                    } else {
                        FileRow(
                            name = "",
                            subtitle = "",
                            isLoading = true,
                            shimmerProgress = shimmerProgress,
                            shimmerColors = shimmerColors,
                        ) { }
                    }
                }
            } else {
                if (visibleFolders.isNotEmpty()) {
                    if (isGridView) {
                        item(
                            key = "folders_header",
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "section_header",
                        ) {
                            Text(
                                text = "Folders",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    items(
                        count = visibleFolders.size,
                        key = { index -> visibleFolders[index].id },
                        contentType = { if (isGridView) "folder-grid" else "folder-list" },
                    ) { index ->
                        val folder = visibleFolders[index]
                        if (isGridView) {
                            FolderGridCard(name = folder.name) {
                                navigateToFolder(folder.id)
                            }
                        } else {
                            val subtitle = "Modified ${folder.updatedAt?.take(10) ?: "Unknown"}"
                            val folderTint = folder.color?.lowercase()?.let { folderColorMap[it] }
                                ?: defaultFolderColor
                            FolderCard(
                                name = folder.name,
                                subtitle = subtitle,
                                iconTint = folderTint,
                            ) {
                                navigateToFolder(folder.id)
                            }
                        }
                    }
                }

                if (visibleFiles.isNotEmpty() && isGridView) {
                    item(
                        key = "files_header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "section_header",
                    ) {
                        Text(
                            text = "Files",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                items(
                    count = visibleFiles.size,
                    key = { index -> visibleFiles[index].id },
                    contentType = { if (isGridView) "file-grid" else "file-list" },
                ) { index ->
                    val file = visibleFiles[index]
                    val fileIconStyle = remember(file.name, file.mimeType) {
                        resolveFileIconStyle(file.name, file.mimeType)
                    }
                    if (isGridView) {
                        FileCard(
                            name = file.name,
                            thumbnailUrl = file.thumbnailUrl,
                            isImage = fileIconStyle.prefersMediaPreview,
                            fileTypeIcon = fileIconStyle.icon,
                            fileTypeTint = fileIconStyle.tint,
                        ) {
                            navigateToPreview(file.id)
                        }
                    } else {
                        val subtitle = buildString {
                            if (file.isStarred) append("★ ")
                            append("Modified ")
                            append(file.updatedAt?.take(10) ?: "Unknown")
                        }

                        FileRow(
                            name = file.name,
                            subtitle = subtitle,
                            iconTint = fileIconStyle.tint,
                            iconVector = fileIconStyle.icon,
                        ) {
                            navigateToPreview(file.id)
                        }
                    }
                }

                if (visibleFolders.isEmpty() && visibleFiles.isEmpty()) {
                    item(
                        key = "empty",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "empty",
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = if (uiState.query.isBlank()) "No files yet" else "No results found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.query.isBlank()) {
                                    "Upload files or create folders to see them here."
                                } else {
                                    "Try a different search query."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(onClick = { viewModel.refresh() }) {
                                Text("Refresh")
                            }
                        }
                    }
                }
            }
        }
        } // end PullToRefreshBox
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

    AppDrawer(
        isOpen = showAppDrawer,
        onClose = { showAppDrawer = false },
        onMenuItemClick = { itemLabel ->
            if (itemLabel == "Uploads") {
                navController.navigate("uploads")
            }
        },
    )
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