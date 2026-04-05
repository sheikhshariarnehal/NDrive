package com.ndrive.cloudvault.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ndrive.cloudvault.presentation.home.components.FileCard
import com.ndrive.cloudvault.presentation.home.components.FileRow
import com.ndrive.cloudvault.presentation.home.components.NDriveBottomNav
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var isGridView by remember { mutableStateOf(false) } // Default to List View matching mockup
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(1200) // Simulate network load
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Replicated Search in Drive UI
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search in Drive") },
                    leadingIcon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                    trailingIcon = { 
                        Surface(
                            shape = CircleShape, 
                            color = MaterialTheme.colorScheme.primary, 
                            modifier = Modifier.size(32.dp).padding(end = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) { 
                                Text("R", color = MaterialTheme.colorScheme.onPrimary) 
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {}
                
                Spacer(modifier = Modifier.height(16.dp))

                // Replicated Suggested & Activity Tabs
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1f), 
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Suggested", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary, 
                            thickness = 3.dp,
                            modifier = Modifier.width(64.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f), 
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(11.dp))
                    }
                }
                HorizontalDivider()
            }
        },
        floatingActionButton = {
            // '+ New' FAB 
            ExtendedFloatingActionButton(
                onClick = { /* New Action */ },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New")
                Spacer(modifier = Modifier.width(12.dp))
                Text("New")
            }
        },
        bottomBar = { NDriveBottomNav() } // Render new 4 item Bottom Nav
    ) { padding ->
        LazyVerticalGrid(
            columns = if (isGridView) GridCells.Fixed(2) else GridCells.Fixed(1),
            contentPadding = PaddingValues(
                start = if (isGridView) 16.dp else 0.dp,
                end = if (isGridView) 16.dp else 0.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp // Space for scrolling under FAB
            ),
            horizontalArrangement = Arrangement.spacedBy(if (isGridView) 12.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(if (isGridView) 12.dp else 0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Component "Files   [Grid/List Toggle]"
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Files", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // File Items
            if (isLoading) {
                items(8) { 
                    if (isGridView) FileCard(name = "", isLoading = true) {}
                    else FileRow(name = "", subtitle = "", isLoading = true) {}
                }
            } else {
                val mockFiles = listOf(
                    Triple("Monthly Notes", "You edited • 10:23 AM", Color(0xFF4285F4)),
                    Triple("Leadership & Organization...", "Mustafa Krishnamurthy replied...", Color(0xFFF4B400)),
                    Triple("Monthly Forecast", "You edited • Nov 1, 2022", Color(0xFF0F9D58)),
                    Triple("Monthly Revenue", "You edited • Nov 1, 2022", Color(0xFF0F9D58)),
                    Triple("Q4 Proposal", "Rose James commented • Oct 31...", Color(0xFFDB4437)),
                    Triple("Project Harrison Tracker", "You opened • Oct 31, 2022", Color(0xFF0F9D58)),
                    Triple("Acme_ExpenseForm", "You edited • Oct 31, 2022", Color(0xFF4285F4))
                )
                
                items(mockFiles.size) { index ->
                    if (isGridView) {
                        FileCard(name = mockFiles[index].first, isImage = false) {}
                    } else {
                        FileRow(
                            name = mockFiles[index].first,
                            subtitle = mockFiles[index].second,
                            iconTint = mockFiles[index].third
                        ) {}
                    }
                }
            }
        }
    }
}
