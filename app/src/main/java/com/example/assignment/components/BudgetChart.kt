package com.example.assignment.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun BudgetChart(
    categories: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (categories.isEmpty()) {
                Text(
                    text = "No spending data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val total = categories.sumOf { it.second.toDouble() }.toFloat()
                val colors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF4CAF50),
                    Color(0xFFFFC107),
                    Color(0xFFE91E63),
                    Color(0xFF9C27B0),
                    Color(0xFF795548),
                    Color(0xFF607D8B),
                    Color(0xFF3F51B5)
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    drawPieChart(categories, colors)
                }

                Spacer(modifier = Modifier.height(16.dp))

                categories.forEachIndexed { index, (category, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[index % colors.size])
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "¥${String.format("%.2f", amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPieChart(
    categories: List<Pair<String, Float>>,
    colors: List<Color>
) {
    val total = categories.sumOf { it.second.toDouble() }.toFloat()
    var startAngle = 0f

    categories.forEachIndexed { index, (_, amount) ->
        val sweepAngle = (amount / total) * 360f
        drawArc(
            color = colors[index % colors.size],
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true
        )
        startAngle += sweepAngle
    }
} 