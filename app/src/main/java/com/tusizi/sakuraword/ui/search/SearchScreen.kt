package com.tusizi.sakuraword.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tusizi.sakuraword.data.DictEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.viewState.collectAsState()
    var detailEntry by remember { mutableStateOf<DictEntry?>(null) }

    if (detailEntry != null) {
        DictDetailScreen(entry = detailEntry!!, onBackClick = { detailEntry = null })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("RedSun 词典", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Search Input
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.handleIntent(SearchContract.Intent.UpdateQuery(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("日文 / 读音 / 中文") },
                    trailingIcon = {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { viewModel.handleIntent(SearchContract.Intent.Search) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.handleIntent(SearchContract.Intent.Search) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    )
                )

                val displayList = if (state.query.isEmpty()) state.history else state.results
                val isHistory = state.query.isEmpty()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (isHistory && state.history.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("最近搜索", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                TextButton(onClick = { viewModel.handleIntent(SearchContract.Intent.ClearHistory) }) {
                                    Text("清空", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    items(displayList) { entry ->
                        DictResultItem(
                            entry = entry,
                            showHistoryIcon = isHistory,
                            onClick = {
                                viewModel.handleIntent(SearchContract.Intent.AddToHistory(entry))
                                detailEntry = entry
                            }
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
fun DictResultItem(
    entry: DictEntry,
    showHistoryIcon: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showHistoryIcon) {
            Icon(Icons.Default.History, null, Modifier.size(18.dp), Color.LightGray)
            Spacer(Modifier.width(12.dp))
        }
        
        Column {
            Text(
                text = entry.word,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = entry.reading,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
