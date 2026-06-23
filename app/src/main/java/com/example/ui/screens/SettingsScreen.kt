package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Workspace

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    workspaces: List<Workspace>,
    activeWorkspaceId: Int?,
    onSwitchWorkspace: (Int) -> Unit,
    onRegisterWorkspace: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRegisterDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // --- HEADER ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SaaS Workspace Console",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Switch business databases & check subscription status",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        // --- ACTIVE SUBSCRIPTION CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE PLAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("Tally Enterprise Cloud Tier", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Unlimited Ecommerce Sync • GST auto-compliance active", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("PRO MEMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // --- SWITCH CORPORATE WORKSPACE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registered Company Entities", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            TextButton(
                onClick = { showRegisterDialog = true },
                modifier = Modifier.testTag("register_entity_button")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = "Add business", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Register Brand", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(workspaces) { ws ->
                val isActive = ws.id == activeWorkspaceId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSwitchWorkspace(ws.id) }
                        .testTag("workspace_item_${ws.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                if (isActive) Icons.Default.Business else Icons.Default.MapsHomeWork,
                                contentDescription = "Branch Entity",
                                tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Column {
                                Text(ws.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("GSTIN: ${ws.gstNumber.ifEmpty { "UNREGISTERED" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE DB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        // --- NEW REGISTER BRAND MODAL ---
        if (showRegisterDialog) {
            AlertDialog(
                onDismissRequest = { showRegisterDialog = false },
                title = { Text("Register Corporate Entity", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "This triggers standard provisioning scripts. Upon registration, default COA ledgers, warehouses, and opening capital balances will be configured instantly in SQLite.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        var bizName by remember { mutableStateOf("") }
                        var bizGst by remember { mutableStateOf("") }
                        var bizAddress by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = bizName,
                            onValueChange = { bizName = it },
                            label = { Text("Company Legal Name") },
                            placeholder = { Text("e.g. Zenith D2C Brands Ltd.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("register_entity_name")
                        )

                        OutlinedTextField(
                            value = bizGst,
                            onValueChange = { bizGst = it },
                            label = { Text("Company GSTIN") },
                            placeholder = { Text("e.g. 27AAICA9911D1ZS") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("register_entity_gst")
                        )

                        OutlinedTextField(
                            value = bizAddress,
                            onValueChange = { bizAddress = it },
                            label = { Text("Headquarters Address") },
                            placeholder = { Text("e.g. 104 Tech Hub, Bangalore, IN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showRegisterDialog = false }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (bizName.isNotEmpty()) {
                                        onRegisterWorkspace(bizName, bizGst, bizAddress)
                                        showRegisterDialog = false
                                    }
                                },
                                modifier = Modifier.testTag("dialog_register_entity_confirm")
                            ) {
                                Text("Provision Entity")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}
