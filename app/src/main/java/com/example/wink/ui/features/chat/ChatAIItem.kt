package com.example.wink.ui.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wink.R

@Composable
fun ChatAIItem(navController: NavController) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Blue.copy(alpha = 0.6f),
            Color.Red.copy(alpha = 0.6f),
            Color.Magenta.copy(alpha = 0.6f),
            Color.Yellow.copy(alpha = 0.6f)
        )
    )

    ListItem(
        headlineContent = {
            Text(
                text = "Wink AI",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        supportingContent = {
            Text(
                text = "Bắt đầu cuộc trò chuyện với AI của chúng tôi!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.ic_launcher_background) // Using a default drawable for AI avatar
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_launcher_background), // Replace with your default avatar
                contentDescription = "AI Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .background(gradientBrush)
            .clickable { navController.navigate("message/ai_chat") }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun ChatAIItemPreview() {
    val navController = rememberNavController()
    ChatAIItem(navController = navController)
}
