package com.ndrive.cloudvault.presentation.upload

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ndrive.cloudvault.presentation.home.UploadItemStatus
import com.ndrive.cloudvault.presentation.home.UploadItemUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UploadsScreen(
    navController: NavController,
    viewModel: UploadViewModel = hiltViewModel(),
) {
    val panel by viewModel.panelState.collectAsState()

    val total = panel.items.size
    val succeeded = panel.items.count { it.status == UploadItemStatus.SUCCESS }
    val failed = panel.items.count { it.status == UploadItemStatus.ERROR }
    val inProgress = panel.items.count {
        it.status == UploadItemStatus.UPLOADING || it.status == UploadItemStatus.QUEUED
    }
    val progressFraction = if (total == 0) {
        0f
    } else {
        panel.items.sumOf { it.progressPercent }.toFloat() / (total * 100f)
    }.coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uploads") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                total = total,
                inProgress = inProgress,
                succeeded = succeeded,
                failed = failed,
                progressFraction = progressFraction,
                summaryMessage = panel.summaryMessage,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearAll() },
                    enabled = !panel.isRunning,
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text("Clear completed")
                }
            }

            if (panel.items.isEmpty()) {
                EmptyUploadsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(panel.items, key = { it.id }) { item ->
                        UploadItemCard(
                            item = item,
                            onDismiss = { viewModel.dismissItem(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    total: Int,
    inProgress: Int,
    succeeded: Int,
    failed: Int,
    progressFraction: Float,
    summaryMessage: String?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = summaryMessage ?: if (inProgress > 0) "$inProgress uploading" else "Upload queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatPill(label = "Total", value = total.toString())
                StatPill(label = "Uploading", value = inProgress.toString())
                StatPill(label = "Done", value = succeeded.toString())
                StatPill(label = "Failed", value = failed.toString())
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UploadItemCard(
    item: UploadItemUiState,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    StatusIcon(item = item)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                        Text(
                            text = subtitle(item),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }

                val canDismiss = item.status == UploadItemStatus.SUCCESS || item.status == UploadItemStatus.ERROR
                if (canDismiss) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }

            if (item.status == UploadItemStatus.UPLOADING || item.status == UploadItemStatus.QUEUED) {
                LinearProgressIndicator(
                    progress = {
                        if (item.status == UploadItemStatus.QUEUED) 0f
                        else item.progressPercent.coerceIn(0, 100) / 100f
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(item: UploadItemUiState) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (item.status) {
            UploadItemStatus.SUCCESS -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Uploaded",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            UploadItemStatus.ERROR -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = MaterialTheme.colorScheme.error,
                )
            }

            UploadItemStatus.UPLOADING -> {
                CircularProgressIndicator(
                    progress = { item.progressPercent.coerceIn(0, 100) / 100f },
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp),
                )
            }

            UploadItemStatus.QUEUED -> {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Queued",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun subtitle(item: UploadItemUiState): String {
    return when (item.status) {
        UploadItemStatus.QUEUED -> "Waiting in queue"
        UploadItemStatus.UPLOADING -> {
            val phase = item.phase?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Uploading"
            "${item.progressPercent}% - $phase"
        }

        UploadItemStatus.SUCCESS -> "Uploaded"
        UploadItemStatus.ERROR -> item.message ?: "Upload failed"
    }
}

@Composable
private fun EmptyUploadsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No uploads yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Start an upload from Home and track it here in real time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
