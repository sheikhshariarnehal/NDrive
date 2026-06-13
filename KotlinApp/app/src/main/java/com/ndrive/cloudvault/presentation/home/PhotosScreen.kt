package com.ndrive.cloudvault.presentation.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ndrive.cloudvault.domain.model.DriveFile
import com.ndrive.cloudvault.presentation.common.resolveFileIconStyle
import com.ndrive.cloudvault.presentation.common.shimmerEffect
import com.ndrive.cloudvault.presentation.home.components.FileRow
import com.ndrive.cloudvault.presentation.home.components.GridListToggle
import com.ndrive.cloudvault.presentation.home.components.NDriveBottomNav
import com.ndrive.cloudvault.presentation.home.components.CreateNewBottomSheet
import com.ndrive.cloudvault.presentation.home.components.TopSearchBar
import com.ndrive.cloudvault.presentation.home.components.AppDrawer

@Composable
fun PhotoThumbnail(
    file: DriveFile,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
        } else {
            val thumbnailUrl = file.thumbnailUrl
            if (thumbnailUrl != null) {
                val finalUrl = if (thumbnailUrl.startsWith("http") || thumbnailUrl.startsWith("data:")) {
                    thumbnailUrl
                } else {
                    "https://pub-99b846451dcc4c879db177b7e8b60c2f.r2.dev/$thumbnailUrl"
                }
                val context = LocalContext.current
                val imageRequest = remember(finalUrl, context) {
                    ImageRequest.Builder(context)
                        .data(finalUrl)
                        .crossfade(300)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                }
                AsyncImage(
                    model = imageRequest,
                    contentDescription = file.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    navController: NavController,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isGridView by rememberSaveable { mutableStateOf(true) } // Photos usually default to grid

    var showCreateSheet by remember { mutableStateOf(false) }
    var showAppDrawer by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val pullRefreshState = rememberPullToRefreshState()

    val backgroundColor = MaterialTheme.colorScheme.background

    // Group photos by date
    val groupedPhotos = remember(uiState.files) {
        uiState.files.groupBy { file ->
            file.updatedAt?.substringBefore('T') ?: "Unknown Date"
        }.toSortedMap(compareByDescending { it })
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TopSearchBar(
                    onMenuClick = { showAppDrawer = true },
                    onProfileClick = { navController.navigate("profile_route") },
                    onSearchClick = { navController.navigate("search") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        floatingActionButton = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .clickable { showCreateSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "New",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        },
        bottomBar = { NDriveBottomNav(navController) }
    ) { padding ->
        AppDrawer(
            isOpen = showAppDrawer,
            onClose = { showAppDrawer = false },
            onMenuItemClick = { itemLabel ->
                if (itemLabel == "Uploads") {
                    navController.navigate("uploads")
                }
            },
        )

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyVerticalGrid(
                columns = if (isGridView) GridCells.Fixed(3) else GridCells.Fixed(1),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 88.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Main toggle header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Photos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        GridListToggle(isGridView = isGridView, onToggle = { isGridView = !isGridView })
                    }
                }

                // Error Message
                uiState.errorMessage?.let { error ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    items(12) {
                        if (isGridView) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect()
                            )
                        }
                    }
                } else if (uiState.files.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "No photos",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No photos yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Upload images to your drive and they will appear here automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    groupedPhotos.forEach { (date, photosList) ->
                        // Date header section
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = formatDateHeader(date),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }

                        // Photos list/grid items
                        items(
                            count = photosList.size,
                            key = { index -> photosList[index].id },
                            contentType = { "photo" }
                        ) { index ->
                            val file = photosList[index]
                            if (isGridView) {
                                PhotoThumbnail(
                                    file = file,
                                    onClick = { navController.navigate("preview/${Uri.encode(file.id)}") }
                                )
                            } else {
                                val fileIconStyle = resolveFileIconStyle(file.name, file.mimeType)
                                FileRow(
                                    name = file.name,
                                    subtitle = formatUpdatedAt(file.updatedAt),
                                    iconTint = fileIconStyle.tint,
                                    iconVector = fileIconStyle.icon,
                                    isLoading = false,
                                ) {
                                    navController.navigate("preview/${Uri.encode(file.id)}")
                                }
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
            onDismissRequest = { showCreateSheet = false }
        )
    }
}

private fun formatDateHeader(dateStr: String): String {
    if (dateStr.isBlank() || dateStr == "Unknown Date") return "Unknown Date"
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val monthInt = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val monthName = months.getOrNull(monthInt - 1) ?: "Unknown"
            "$monthName $day, $year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatUpdatedAt(updatedAt: String?): String {
    if (updatedAt.isNullOrBlank()) return "Modified recently"
    val date = updatedAt.substringBefore('T').takeIf { it.length == 10 } ?: updatedAt.take(10)
    return "Modified $date"
}
